/******************************************************************************
 *
 * File:    StringCheckDynamicStepMeta.java
 * Author:  <A HREF="mailto:sergio.ramazzina@serasoft.it">Sergio Ramazzina</A>
 *
 ******************************************************************************/

package org.serasoft.di.steps.stringcheckdynamic;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Step(
        id = "SeraStringCheckDynamicStep",
        image = "org/serasoft/di/steps/stringcheckdynamic/resources/demo.svg",
        name = "StringCheckDynamic.Step.Name",
        description = "StringCheckDynamic.Step.TooltipDesc",
        i18nPackageName = "org.serasoft.di.steps.stringcheckdynamic",
        categoryDescription = "StringCheckDynamic.Category"
)
@InjectionSupported(localizationPrefix = "StringCheckDynamic.Step.Injection.")
public class StringCheckDynamicStepMeta extends BaseStepMeta implements StepMetaInterface {

    /**
     * The PKG member is used when looking up internationalized strings.
     * The properties file with localized keys is expected to reside in
     * {the package of the class specified}/messages/messages_{locale}.properties
     */
    private static final Class<?> PKG = StringCheckDynamicStepMeta.class; // for i18n purposes
    private List<? extends SharedObjectInterface> databases;
    private DatabaseMeta databaseMeta;
    /**
     * Fields in the table to insert
     */
    private StringItemToCheck[] itemsToCheck;

    /**
     * Fields containing the values in the input stream to insert
     */
    private String[] fieldStream;

    /**
     * Constructor should call super() to make sure the base class has a chance to initialize properly.
     */
    public StringCheckDynamicStepMeta() {
        super();
        fieldStream = new String[0];
        itemsToCheck = new StringItemToCheck[0];
    }

    @Injection(name = "CONNECTIONNAME")
    public void setConnection(String connectionName) {
        databaseMeta = DatabaseMeta.findDatabase(this.databases, connectionName);
    }

    /**
     * @return Returns the fieldName.
     */
    public StringItemToCheck[] getItemsToCheck() {
        return itemsToCheck;
    }

    public void allocate(int nrRows) {
        fieldStream = new String[nrRows];
        itemsToCheck = new StringItemToCheck[nrRows];
    }

    /**
     * @return Returns the database.
     */
    public DatabaseMeta getDatabaseMeta() {
        return databaseMeta;
    }

    /**
     * @param database The database to set.
     */
    public void setDatabaseMeta(DatabaseMeta database) {
        this.databaseMeta = database;
    }

    /**
     * Called by Spoon to get a new instance of the SWT dialog for the step.
     * A standard implementation passing the arguments to the constructor of the step dialog is recommended.
     *
     * @param shell     an SWT Shell
     * @param meta      description of the step
     * @param transMeta description of the the transformation
     * @param name      the name of the step
     * @return new instance of a dialog for this step
     */
    public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
        return new StringCheckDynamicStepDialog(shell, meta, transMeta, name);
    }

    /**
     * Called by PDI to get a new instance of the step implementation.
     * A standard implementation passing the arguments to the constructor of the step class is recommended.
     *
     * @param stepMeta          description of the step
     * @param stepDataInterface instance of a step data class
     * @param cnr               copy number
     * @param transMeta         description of the transformation
     * @param disp              runtime implementation of the transformation
     * @return the new instance of a step implementation
     */
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
                                 Trans disp) {
        return new StringCheckDynamicStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
    }

    /**
     * Called by PDI to get a new instance of the step data class.
     */
    public StepDataInterface getStepData() {
        return new StringCheckDynamicStepData();
    }

    /**
     * This method is called every time a new step is created and should allocate/set the step configuration
     * to sensible defaults. The values set here will be used by Spoon when a new step is created.
     */
    public void setDefault() {
    }

    /**
     * @return Fields containing the values in the input stream to insert.
     */
    public String[] getFieldStream() {
        return fieldStream;
    }

    @Override
    public DatabaseMeta[] getUsedDatabaseConnections() {
        if (databaseMeta != null) {
            return new DatabaseMeta[]{databaseMeta};
        } else {
            return super.getUsedDatabaseConnections();
        }
    }

    /**
     * This method is used when a step is duplicated in Spoon. It needs to return a deep copy of this
     * step meta object. Be sure to create proper deep copies if the step configuration is stored in
     * modifiable objects.
     * <p>
     * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an example on creating
     * a deep copy.
     *
     * @return a deep copy of this
     */
    public StringCheckDynamicStepMeta clone() {
        StringCheckDynamicStepMeta retval = (StringCheckDynamicStepMeta) super.clone();

        int nrRows = fieldStream.length;
        retval.allocate(nrRows);
        System.arraycopy(fieldStream, 0, retval.fieldStream, 0, nrRows);
        System.arraycopy(itemsToCheck, 0, retval.itemsToCheck, 0, nrRows);

        return retval;
    }

    /**
     * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected
     * return value is an XML fragment consisting of one or more XML tags.
     * <p>
     * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
     *
     * @return a string containing the XML serialization of this step
     */
    public String getXML() {
        StringBuilder retval = new StringBuilder();

        retval.append("    "
                + XMLHandler.addTagValue("connection", databaseMeta == null ? "" : databaseMeta.getName()));
        retval.append("    <fields_to_check>").append(Const.CR);

        for (int i = 0; i < itemsToCheck.length; i++) {
            retval.append("    <field_to_check>").append(Const.CR);
            retval.append("     ").append(XMLHandler.addTagValue("field_name", itemsToCheck[i].getFieldname()));
            retval.append("     ").append(XMLHandler.addTagValue("ref_table_name", itemsToCheck[i].getTableName()));
            retval.append("     ").append(XMLHandler.addTagValue("ref_column_name", itemsToCheck[i].getColumnName()));
            retval.append("     ").append(XMLHandler.addTagValue("trim_type", ValueMetaString.getTrimTypeCode(itemsToCheck[i].getTrimType())));
            retval.append("    </field_to_check>").append(Const.CR);
        }
        retval.append("    </fields_to_check>").append(Const.CR);

        return retval.toString();
    }

    /**
     * This method is called by PDI when a step needs to load its configuration from XML.
     * <p>
     * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
     * XML node passed in.
     *
     * @param stepnode  the XML node containing the configuration
     * @param databases the databases available in the transformation
     * @param metaStore the metaStore to optionally read from
     */
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
        try {
            String connName = XMLHandler.getTagValue(stepnode, "connection");
            databaseMeta = DatabaseMeta.findDatabase(databases, connName);

            Node fields = XMLHandler.getSubNode(stepnode, "fields_to_check");
            int count = XMLHandler.countNodes(fields, "field_to_check");

            allocate(count);

            for (int i = 0; i < count; i++) {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field_to_check", i);
                itemsToCheck[i] = new StringItemToCheck(XMLHandler.getTagValue(fnode, "field_name"),
                        XMLHandler.getTagValue(fnode, "ref_table_name"),
                        XMLHandler.getTagValue(fnode, "ref_column_name"),
                        ValueMetaString.getTrimTypeByCode( XMLHandler.getTagValue(
                                fnode, "trim_type" )));
            }
        } catch (Exception e) {
            throw new KettleXMLException("StringResizeDynamic step: unable to read step info from XML node", e);
        }
    }

    /**
     * This method is called by Spoon when a step needs to serialize its configuration to a repository.
     * The repository implementation provides the necessary methods to save the step attributes.
     *
     * @param rep               the repository to save to
     * @param metaStore         the metaStore to optionally write to
     * @param id_transformation the id to use for the transformation when saving
     * @param id_step           the id to use for the step  when saving
     */
    public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step)
            throws KettleException {
        try {
            rep.saveStepAttribute(id_transformation, id_step, "connection", databaseMeta.getName());

            for (int i = 0; i < itemsToCheck.length; i++) {
                rep.saveStepAttribute(id_transformation, id_step, i, "item_to_resize_field_name", getNullOrEmpty(itemsToCheck[i].getFieldname()));
                rep.saveStepAttribute(id_transformation, id_step, i, "item_to_resize_ref_table_name", getNullOrEmpty(itemsToCheck[i].getTableName()));
                rep.saveStepAttribute(id_transformation, id_step, i, "item_to_resize_ref_column_name", getNullOrEmpty(itemsToCheck[i].getColumnName()));
                rep.saveStepAttribute(id_transformation, id_step, i, "item_to_resize_trim_type", ValueMetaString
                        .getTrimTypeCode(itemsToCheck[i].getTrimType()));
            }
        } catch (Exception e) {
            throw new KettleException("Unable to save step into repository: " + id_step, e);
        }
    }

    @Override
    public boolean supportsErrorHandling() {
        return true;
    }

    private String getNullOrEmpty(String str) {
        return str == null ? StringUtils.EMPTY : str;
    }

    /**
     * This method is called by PDI when a step needs to read its configuration from a repository.
     * The repository implementation provides the necessary methods to read the step attributes.
     *
     * @param rep       the repository to read from
     * @param metaStore the metaStore to optionally read from
     * @param id_step   the id of the step being read
     * @param databases the databases available in the transformation
     */
    public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases)
            throws KettleException {
        try {
            String connName = rep.getStepAttributeString(id_step, "connection");
            databaseMeta = DatabaseMeta.findDatabase(databases, connName);

            int nrfields = rep.countNrStepAttributes(id_step, "item_to_resize");

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++) {
                itemsToCheck[i] = new StringItemToCheck(rep.getStepAttributeString(id_step, i, "field_name"),
                        rep.getStepAttributeString(id_step, i, "ref_table_name"),
                        rep.getStepAttributeString(id_step, i, "ref_column_name"),
                        ValueMetaString.getTrimTypeByCode( rep.getStepAttributeString(
                                id_step, i, getRepCode( "trim_type" ))));
            }
        } catch (Exception e) {
            throw new KettleException("Unable to load step from repository", e);
        }
    }

    /**
     * This method is called to determine the changes the step is making to the row-stream.
     * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering
     * the step. This method must apply any changes the step makes to the row stream. Usually a step adds fields to the
     * row-stream.
     *
     * @param rowMeta    the row structure coming in to the step
     * @param name       the name of the step making the changes
     * @param info       row structures of any info steps coming in
     * @param nextStep   the description of a step this step is passing rows to
     * @param space      the variable space for resolving variables
     * @param repository the repository instance optionally read from
     * @param metaStore  the metaStore to optionally read from
     */
    public void getFields(RowMetaInterface rowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
                          VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException {

        RowMetaInterface clone = rowMeta.clone();
        rowMeta.clear();
        rowMeta.addRowMeta(clone);
    }

    public void retrieveTableFieldsMetadata(List<StringItemToCheck> itemsToResize) {

        Map<String, ResultSet> tablesDM = new HashMap<>();
        Database db = null;

        try {
            db = new Database(loggingObject, databaseMeta);
            db.connect();
            logDebug("Used for testing purpose. Generate temporary connection to database to retrieve DB metadata");

            DatabaseMetaData dm = db.getConnection().getMetaData();
            for (StringItemToCheck item : itemsToResize) {
                ResultSet rs = dm.getColumns(null, null, item.getTableName(), item.getColumnName());
                if (rs != null) {
                    if (rs.next()) {
                        item.setTargetSize(Integer.parseInt(rs.getString("COLUMN_SIZE")));
                    } else {
                        // TODO what to do if column's infos aren't found?
                    }
                }
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        } catch (KettleDatabaseException e2) {
            e2.printStackTrace();
        } finally {
            db.disconnect();
        }

    }

    /**
     * This method is called when the user selects the "Verify Transformation" option in Spoon.
     * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
     * The method should perform as many checks as necessary to catch design-time errors.
     * <p>
     * Typical checks include:
     * - verify that all mandatory configuration is given
     * - verify that the step receives any input, unless it's a row generating step
     * - verify that the step does not receive any input if it does not take them into account
     * - verify that the step finds fields it relies on in the row-stream
     *
     * @param remarks   the list of remarks to append to
     * @param transMeta the description of the transformation
     * @param stepMeta  the description of the step
     * @param prev      the structure of the incoming row-stream
     * @param input     names of steps sending input to the step
     * @param output    names of steps this step is sending output to
     * @param info      fields coming in from info steps
     * @param metaStore metaStore to optionally read from
     */
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
                      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
                      IMetaStore metaStore) {
    /* CheckResult cr;

    // See if there are input streams leading to this step!
    if ( input != null && input.length > 0 ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK,
        BaseMessages.getString( PKG, "Demo.CheckResult.ReceivingRows.OK" ), stepMeta );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
        BaseMessages.getString( PKG, "Demo.CheckResult.ReceivingRows.ERROR" ), stepMeta );
      remarks.add( cr );
    }*/
    }
}
