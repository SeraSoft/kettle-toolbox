/******************************************************************************
 *
 * File:    StringResizeDynamicData.java
 * Author:  <A HREF="mailto:sergio.ramazzina@serasoft.it">Sergio Ramazzina</A>
 *
 ******************************************************************************/

package org.serasoft.di.steps.stringresizedynamic;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class StringResizeDynamicStepData extends BaseStepData implements StepDataInterface {

  public Database db;

  RowMetaInterface outputRowMeta;

  int outputFieldIndex = -1;

  public StringResizeDynamicStepData() {
    super();
  }
}
