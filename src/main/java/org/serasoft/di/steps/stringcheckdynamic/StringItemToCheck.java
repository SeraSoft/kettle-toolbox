/******************************************************************************
 *
 * File:    StringItemToResize.java
 * Author:  <A HREF="mailto:sergio.ramazzina@serasoft.it">Sergio Ramazzina</A>
 *
 ******************************************************************************/

package org.serasoft.di.steps.stringcheckdynamic;

public class StringItemToCheck {

    private String fieldname;
    private String tableName;
    private String columnName;
    private int trimType;
    private int targetSize;

    public StringItemToCheck(String fieldname, String tableName, String columnName, int trimType) {
        this.fieldname = fieldname;
        this.tableName = tableName;
        this.columnName = columnName;
        this.trimType = trimType;
    }

    public int getTargetSize() {
        return targetSize;
    }

    public void setTargetSize(int targetSize) {
        this.targetSize = targetSize;
    }

    public int getTrimType() {
        return trimType;
    }

    public void setTrimType(int trimType) {
        this.trimType = trimType;
    }

    public String getFieldname() {
        return fieldname;
    }

    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String toString() {
        return "StringItemToResize{" +
                "fieldname='" + fieldname + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", trimType=" + trimType +
                '}';
    }
}
