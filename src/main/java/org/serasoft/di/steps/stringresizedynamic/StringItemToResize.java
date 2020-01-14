/******************************************************************************
 *
 * File:    StringItemToResize.java
 * Author:  <A HREF="mailto:sergio.ramazzina@serasoft.it">Sergio Ramazzina</A>
 *
 ******************************************************************************/

package org.serasoft.di.steps.stringresizedynamic;

public class StringItemToResize {

    private String fieldname;
    private String tableName;
    private String columnName;
    private int targetSize;

    public StringItemToResize(String fieldname, String tableName, String columnName) {
        this.fieldname = fieldname;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public int getTargetSize() {
        return targetSize;
    }

    public void setTargetSize(int targetSize) {
        this.targetSize = targetSize;
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
                ", targetSize=" + targetSize +
                '}';
    }
}
