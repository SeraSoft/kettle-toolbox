/******************************************************************************
 *
 * File:    StringCheckDynamicStepDialog.java
 * Author:  <A HREF="mailto:sergio.ramazzina@serasoft.it">Sergio Ramazzina</A>
 *
 ******************************************************************************/

package org.serasoft.di.steps.stringcheckdynamic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringCheckDynamicStepDialog extends BaseStepDialog implements StepDialogInterface {

    private static Class<?> PKG = StringCheckDynamicStepDialog.class; // Needed for i18n purposes
    private CCombo wDBConnection;
    private ColumnInfo[] ciFields;

    private Label wlFieldsToCheck;
    private FormData fdlFieldsToCheck;
    private TableView wFieldsToCheck;

    private Button wGetFields;
    private FormData fdGetSelect;
    private FormData fdFields;

    private boolean gotPreviousFields = false;

    /**
     * List of ColumnInfo that should have the field names to convert
     */
    private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();
    /**
     * Previous fields are read asynchonous because this might take some time and the user is able to do other things,
     * where he will not need the previous fields
     */
    private boolean bPreviousFieldsLoaded = false;
    private boolean bTablesLoaded = false;

    /**
     * Fields from previous step
     */
    private RowMetaInterface prevFields;

    // this is the object the stores the step's settings
    // the dialog reads the settings from it when opening
    // the dialog writes the settings to it when confirmed
    private StringCheckDynamicStepMeta stringCheckDynamicStepMeta;

    /**
     * The constructor should simply invoke super() and save the incoming meta
     * object to a local variable, so it can conveniently read and write settings
     * from/to it.
     *
     * @param parent    the SWT shell to open the dialog in
     * @param in        the meta object holding the step's settings
     * @param transMeta transformation description
     * @param sname     the step name
     */
    public StringCheckDynamicStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
        super(parent, (BaseStepMeta) in, transMeta, sname);
        stringCheckDynamicStepMeta = (StringCheckDynamicStepMeta) in;
    }

    /**
     * This method is called by Spoon when the user opens the settings dialog of the step.
     * It should open the dialog and return only once the dialog has been closed by the user.
     * <p>
     * If the user confirms the dialog, the meta object (passed in the constructor) must
     * be updated to reflect the new step settings. The changed flag of the meta object must
     * reflect whether the step configuration was changed by the dialog.
     * <p>
     * If the user cancels the dialog, the meta object must not be updated, and its changed flag
     * must remain unaltered.
     * <p>
     * The open() method must return the name of the step after the user has confirmed the dialog,
     * or null if the user cancelled the dialog.
     */
    public String open() {
        // store some convenient SWT variables
        Shell parent = getParent();
        Display display = parent.getDisplay();

        // SWT code for preparing the dialog
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, stringCheckDynamicStepMeta);

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Declare fields' list

        // Save the value of the changed flag on the meta object. If the user cancels
        // the dialog, it will be restored to this saved value.
        // The "changed" variable is inherited from BaseStepDialog
        changed = stringCheckDynamicStepMeta.hasChanged();

        // The ModifyListener used on all controls. It will update the meta object to
        // indicate that changes are being made.
        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                stringCheckDynamicStepMeta.setChanged();
            }
        };

        SelectionListener lsSelection = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                stringCheckDynamicStepMeta.setChanged();
                // setTableFieldCombo();
                // validateSelection();
            }
        };


        lsGet = new Listener() {
            public void handleEvent(Event e) {
                try {
                    RowMetaInterface r = transMeta.getPrevStepFields(stepname);
                    BaseStepDialog.getFieldsFromPrevious(r, wFieldsToCheck, 1, new int[]{1}, new int[]{}, -1, -1, null);
                } catch (KettleException ke) {
                    new ErrorDialog(
                            shell, BaseMessages.getString(PKG, "Skyway.Commons.FailedToGetFields.DialogTitle"), BaseMessages
                            .getString(PKG, "Skyway.Commons.FailedToGetFields.DialogMessage"), ke);
                }
            }
        };

        // ------------------------------------------------------- //
        // SWT code for building the actual settings dialog        //
        // ------------------------------------------------------- //
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "StringCheckDynamic.Step.Name"));

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);

        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        // Connection line
        wDBConnection = addConnectionLine(shell, wStepname, middle, margin);
        if (stringCheckDynamicStepMeta.getDatabaseMeta() == null && transMeta.nrDatabases() == 1) {
            wDBConnection.select(0);
        }
        wDBConnection.addModifyListener(lsMod);
        wDBConnection.addSelectionListener(lsSelection);

        // Fields Table
        wlFieldsToCheck = new Label(shell, SWT.NONE);
        wlFieldsToCheck.setText(BaseMessages.getString(PKG, "StringCheckDynamic.FieldsToCheck.Label"));
        props.setLook(wlFieldsToCheck);
        fdlFieldsToCheck = new FormData();
        fdlFieldsToCheck.left = new FormAttachment(0, 0);
        fdlFieldsToCheck.top = new FormAttachment(wDBConnection, margin);
        wlFieldsToCheck.setLayoutData(fdlFieldsToCheck);

        final int FieldsCols = 4;

        ciFields = new ColumnInfo[FieldsCols];
        ciFields[0] =
                new ColumnInfo(
                        BaseMessages.getString(PKG, "StringCheckDynamic.ColumnInfo.StreamField"),
                        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{BaseMessages.getString(
                        PKG, "Skyway.Commons.ColumnInfo.Loading")}, false);
        ciFields[1] =
                new ColumnInfo(
                        BaseMessages.getString(PKG, "StringCheckDynamic.ColumnInfo.TargetTable"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false);

        ciFields[2] =
                new ColumnInfo(
                        BaseMessages.getString(PKG, "StringCheckDynamic.ColumnInfo.TargetColumn"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false);

        ciFields[3] =
                new ColumnInfo(
                        BaseMessages.getString( PKG, "StringCheckDynamic.ColumnInfo.Trim"),
                        ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaString.trimTypeDesc );

        wFieldsToCheck =
                new TableView(
                        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
                        ciFields, stringCheckDynamicStepMeta.getItemsToCheck().length,
                        lsMod, props);

        fieldColumns.add(ciFields[0]);

        wGetFields = new Button(shell, SWT.PUSH);
        wGetFields.setText(BaseMessages.getString(PKG, "StringCheckDynamic.GetFields.Button"));
        wGetFields.addListener(SWT.Selection, lsGet);
        fdGetSelect = new FormData();
        fdGetSelect.right = new FormAttachment(100, 0);
        fdGetSelect.top = new FormAttachment(wlFieldsToCheck, margin);
        wGetFields.setLayoutData(fdGetSelect);

        fdFields = new FormData();
        fdFields.left = new FormAttachment(0, 0);
        fdFields.top = new FormAttachment(wlFieldsToCheck, margin);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(100, -50);
        wFieldsToCheck.setLayoutData(fdFields);

        // OK and cancel buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[]{wOK, wCancel, wGetFields}, margin, null);

        // Add listeners for cancel and OK
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        // default listener (for hitting "enter")
        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        // Set/Restore the dialog size based on last position on screen
        // The setSize() method is inherited from BaseStepDialog
        setSize();

        // populate the dialog with the values from the meta object
        populateDialog();

        setFieldsComboValues();
        // TODO add reference getting table names from database
        // setTablesComboValues();

        // restore the changed flag to original value, as the modify listeners fire during dialog population
        stringCheckDynamicStepMeta.setChanged(changed);

        // open dialog and enter event loop
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        // at this point the dialog has closed, so either ok() or cancel() have been executed
        // The "stepname" variable is inherited from BaseStepDialog
        return stepname;
    }

    private void setFieldsComboValues() {
        Runnable fieldsComboLoader = new Runnable() {
            public void run() {
                try {
                    prevFields = transMeta.getPrevStepFields(stepname);
                } catch (KettleException e) {
                    prevFields = new RowMeta();
                    String msg = BaseMessages.getString(PKG, "StringCheckDynamic.DoMapping.UnableToFindInput");
                    logError(msg);
                }
                String[] prevStepFieldNames = prevFields != null ? prevFields.getFieldNames() : new String[0];
                Arrays.sort(prevStepFieldNames);
                bPreviousFieldsLoaded = true;
                fieldColumns.get(0).setComboValues(prevStepFieldNames);
            }
        };
        shell.getDisplay().asyncExec(fieldsComboLoader);
    }

    /**
     * This helper method puts the step configuration stored in the meta object
     * and puts it into the dialog controls.
     */
    private void populateDialog() {
        wStepname.selectAll();

        if (stringCheckDynamicStepMeta.getDatabaseMeta() != null) {
            wDBConnection.setText(stringCheckDynamicStepMeta.getDatabaseMeta().getName());
        } else if (transMeta.nrDatabases() == 1) {
            wDBConnection.setText(transMeta.getDatabase(0).getName());
        }

        for (int i = 0; i < stringCheckDynamicStepMeta.getItemsToCheck().length; i++) {
            TableItem item = wFieldsToCheck.table.getItem(i);

            item.setText(1, stringCheckDynamicStepMeta.getItemsToCheck()[i].getFieldname());
            item.setText(2, stringCheckDynamicStepMeta.getItemsToCheck()[i].getTableName());
            item.setText(3, stringCheckDynamicStepMeta.getItemsToCheck()[i].getColumnName());
            item.setText(4, ValueMetaString.getTrimTypeCode(stringCheckDynamicStepMeta.getItemsToCheck()[i].getTrimType()));
        }

        wFieldsToCheck.setRowNums();
        wFieldsToCheck.optWidth(true);

    }

    /**
     * Called when the user cancels the dialog.
     */
    private void cancel() {
        // The "stepname" variable will be the return value for the open() method.
        // Setting to null to indicate that dialog was cancelled.
        stepname = null;
        // Restoring original "changed" flag on the meta object
        stringCheckDynamicStepMeta.setChanged(changed);
        // close the SWT dialog window
        dispose();
    }

    /**
     * Called when the user confirms the dialog
     */
    private void ok() {
        if (Utils.isEmpty(wStepname.getText())) {
            return;
        }

        stepname = wStepname.getText(); // return value

        stringCheckDynamicStepMeta.setDatabaseMeta(transMeta.findDatabase(wDBConnection.getText()));

        int count = wFieldsToCheck.nrNonEmpty();
        stringCheckDynamicStepMeta.allocate(count);


        for (int i = 0; i < count; i++) {
            TableItem item = wFieldsToCheck.getNonEmpty(i);
            stringCheckDynamicStepMeta.getItemsToCheck()[i] = new StringItemToCheck(Utils.isEmpty(item.getText(1)) ? null : item.getText(1),
                    Utils.isEmpty(item.getText(2)) ? null : item.getText(2)
                    , Utils.isEmpty(item.getText(3)) ? null : item.getText(3)
                    , ValueMetaString.getTrimTypeByDesc( item.getText(4)));
        }

        dispose();
    }

    private void getFields(CCombo wControl) {
        if (!gotPreviousFields) {
            gotPreviousFields = true;
            try {
                String fieldname = wControl.getText();

                wControl.removeAll();
                RowMetaInterface r = transMeta.getPrevStepFields(stepname);
                if (r != null) {
                    wControl.setItems(r.getFieldNames());
                    if (fieldname != null) {
                        wControl.setText(fieldname);
                    }
                }
            } catch (KettleException ke) {
                new ErrorDialog(
                        shell, BaseMessages.getString(PKG, "StringCheckDynamic.FailedToGetFields.DialogTitle"), BaseMessages
                        .getString(PKG, "StringCheckDynamic.FailedToGetFields.DialogMessage"), ke);
            }
        }
    }
}
