/******************************************************************************
 *
 * File:    StringCheckDynamicStep.java
 * Author:  <A HREF="mailto:sergio.ramazzina@serasoft.it">Sergio Ramazzina</A>
 *
 ******************************************************************************/

package org.serasoft.di.steps.stringcheckdynamic;

import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.util.Arrays;
import java.util.List;

import static org.pentaho.di.ui.trans.step.BaseStepDialog.loggingObject;

public class StringCheckDynamicStep extends BaseStep implements StepInterface {

    private static final Class<?> PKG = StringCheckDynamicStepMeta.class; // for i18n purposes
    private List<StringItemToCheck> fieldsToProcessList = null;
    private long overallStartTimeMillesec;
    private long lineStartTimeMillisec;

    /**
     * The constructor should simply pass on its arguments to the parent class.
     *
     * @param s                 step description
     * @param stepDataInterface step data class
     * @param c                 step copy
     * @param t                 transformation description
     * @param dis               transformation executing
     */
    public StringCheckDynamicStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
        super(s, stepDataInterface, c, t, dis);
    }

    /**
     * This method is called by PDI during transformation startup.
     * <p>
     * It should initialize required for step execution.
     * <p>
     * The meta and data implementations passed in can safely be cast
     * to the step's respective implementations.
     * <p>
     * It is mandatory that super.init() is called to ensure correct behavior.
     * <p>
     * Typical tasks executed here are establishing the connection to a database,
     * as wall as obtaining resources, like file handles.
     *
     * @param smi step meta interface implementation, containing the step settings
     * @param sdi step data interface implementation, used to store runtime information
     * @return true if initialization completed successfully, false if there was an error preventing the step from working.
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {

        // Casting to step-specific implementation classes is safe
        StringCheckDynamicStepMeta meta = (StringCheckDynamicStepMeta) smi;
        StringCheckDynamicStepData data = (StringCheckDynamicStepData) sdi;

        // Init database connection
        if (meta.getDatabaseMeta() != null) {
            data.db = new Database(loggingObject, meta.getDatabaseMeta());
            try {
                if (getTransMeta().isUsingUniqueConnections()) {
                    synchronized (getTrans()) {
                        data.db.connect(getTrans().getTransactionId(), getPartitionID());
                    }
                } else {
                    data.db.connect(getPartitionID());
                }
                logDebug("Going to use database connection for " + meta.getDatabaseMeta().getName());

            } catch (KettleDatabaseException e) {
                logError("Unable to open database connection for " + meta.getDatabaseMeta().getName() + "! - ", e.getMessage());
                setErrors(1);
                stopAll();
            }
        }

        if (meta.getItemsToCheck() != null && meta.getItemsToCheck().length > 0) {
            meta.retrieveTableFieldsMetadata(Arrays.asList(meta.getItemsToCheck()));
        }

        if (!super.init(meta, data)) {
            return false;
        }

        // Add any step-specific initialization that may be needed here
        return true;
    }

    /**
     * @param smi the step meta interface containing the step settings
     * @param sdi the step data interface that should be used to store
     * @return true to indicate that the function should be called again, false if the step is done
     */
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

        // safely cast the step settings (meta) and runtime info (data) to specific implementations
        StringCheckDynamicStepMeta meta = (StringCheckDynamicStepMeta) smi;
        StringCheckDynamicStepData data = (StringCheckDynamicStepData) sdi;

        // get incoming row, getRow() potentially blocks waiting for more rows, returns null if no more rows expected
        Object[] r = getRow();

        // if no more rows are expected, indicate step is finished and processRow() should not be called again
        if (r == null) {
            setOutputDone();
            logDebug("CV001 Conversions Finished - Duration: " + (System.currentTimeMillis() - overallStartTimeMillesec) / 1000 + " sec.");
            return false;
        }

        // the "first" flag is inherited from the base step implementation
        // it is used to guard some processing tasks, like figuring out field indexes
        // in the row structure that only need to be done once
        if (first) {
            first = false;

            if (meta.getItemsToCheck() != null && meta.getItemsToCheck().length > 0) {
                fieldsToProcessList = Arrays.asList(meta.getItemsToCheck());
            } else {
                data.db.disconnect();
                throw new KettleException("The list for items to be resized is empty!");
            }

            // Check that all fields we want to clone are really there to be sure that
            // everything works as expected
            for (StringItemToCheck item : fieldsToProcessList) {
                int fieldPos = getInputRowMeta().indexOfValue(item.getFieldname());
                if (fieldPos == -1) {
                    throw new KettleException("Field " + item.getFieldname() + " is not present in incoming rowset");
                }
            }

            // clone the input row structure and place it in our data object
            data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
            // Set conversion's execution start time
            overallStartTimeMillesec = System.currentTimeMillis();
        }

        Object[] outputRowData = RowDataUtil.resizeArray(r, data.outputRowMeta.size());

        String inErrorFieldsList = "";
        String inErrorsDescList = "";
        int numOfErrors = 0;
        boolean isInError = false;
        try {
            for (StringItemToCheck item : fieldsToProcessList) {

                int fieldPos = getInputRowMeta().indexOfValue(item.getFieldname());
                String fieldValue = getInputRowMeta().getString(r, fieldPos);

                if (fieldValue != null) {
                    int trimTypeCode = item.getTrimType();
                    if (trimTypeCode == 1) { // Left Trim
                        fieldValue = StringUtils.stripStart(fieldValue, null);
                    } else if (trimTypeCode == 2) { // Right Trim
                        fieldValue = StringUtils.stripEnd(fieldValue, null);
                    } else if (trimTypeCode == 3) { // Trim Both
                        fieldValue = StringUtils.trim(fieldValue);
                    } else {
                        logDetailed("No trim required on field " + item.getFieldname() + " before length check.");
                    }

                    int fieldLength = fieldValue.length();
                    if (fieldLength > item.getTargetSize()) {
                        // TODO rows to error rows but check eny field specified before leaving
                        // This condition works for ZREQUIRED = 1 or 2! Means the record wasn't found in tables
                        numOfErrors += 1;
                        inErrorFieldsList += (inErrorFieldsList.length() == 0 ? "" : ",") + item.getFieldname();
                        inErrorsDescList += (inErrorsDescList.length() == 0 ? "" : ",")
                                + "SF: " + item.getFieldname()
                                + " - SFV: " + fieldValue
                                + " - RFT: " + item.getTableName()
                                + " - RF: " + item.getColumnName()
                                + " - RFL: " + item.getTargetSize()
                                + " - AFL: " + fieldLength;
                        isInError = true;
                    }
                }
            }
        } catch (Exception e) {
            data.db.disconnect();
            throw new KettleException("SQL Exception while executing query on connection " + meta.getDatabaseMeta().getName() + "! Exiting!", e);
        }

        if (isInError) {
            if (getStepMeta().isDoingErrorHandling()) {
                putError(data.outputRowMeta, outputRowData, numOfErrors, inErrorsDescList, inErrorFieldsList, "");
                // Same message present in error description fields is written to console log
                logBasic(inErrorsDescList);
            } else {
                logDetailed("Row is in error");
                throw new KettleException("Fields: " + inErrorFieldsList + " - Fields do not passed the string length check validations! ");
            }
        } else {
            putRow(data.outputRowMeta, outputRowData);
        }

        return true;
    }

    /**
     * This method is called by PDI once the step is done processing.
     * <p>
     * The dispose() method is the counterpart to init() and should release any resources
     * acquired for step execution like file handles or database connections.
     * <p>
     * The meta and data implementations passed in can safely be cast
     * to the step's respective implementations.
     * <p>
     * It is mandatory that super.dispose() is called to ensure correct behavior.
     *
     * @param smi step meta interface implementation, containing the step settings
     * @param sdi step data interface implementation, used to store runtime information
     */
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

        // Casting to step-specific implementation classes is safe
        StringCheckDynamicStepMeta meta = (StringCheckDynamicStepMeta) smi;
        StringCheckDynamicStepData data = (StringCheckDynamicStepData) sdi;

        /* try {
            closePreviousQuery();
        } catch ( KettleException e ) {
            logError( "Unexpected error closing query : " + e.toString() );
            setErrors( 1 );
            stopAll();
        } finally {*/
        if (data.db != null) {
            data.db.disconnect();
        }
        //}

        // Call superclass dispose()
        super.dispose(meta, data);
    }
}
