/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.serasoft.di.databases.mysql;

import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.plugins.DatabaseMetaPlugin;
import org.pentaho.di.core.row.ValueMetaInterface;

// the annotation allows PDI to recognize this class as a database plug-in 
@DatabaseMetaPlugin(
        type = "MySQL-Latest",
        typeDescription = "MySQL Driver - Latest"
)
// The BaseDatabaseMeta class provides common implementations for most DatabaseInterface methods.
// Be sure however to check if the default implementation is a good choice for the database in question.
public class MySQLDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {

    /**
     * Returns the list of possible access types for a database.
     * Most common choices are JDBC and JNDI.
     */
    public int[] getAccessTypeList() {
        return new int[]{DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI};
    }

    public int getDefaultDatabasePort() {
        if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
            return 3306;
        }
        return -1;
    }

    /**
     * Returns the SQL query to execute when PDI needs to determine the field layout of a table
     */
    public String getSQLQueryFields(String tableName) {
        return "SELECT * FROM " + tableName + " WHERE 1=0";
    }

    /**
     * Returns the SQL query to execute in order to determine if a table exists. If an exception is
     * thrown in the process, PDI will assume that the table does not exist.
     */
    public String getSQLTableExists(String tablename) {
        return getSQLQueryFields(tablename);
    }

    /**
     * Returns the SQL query to execute in order to determine if a field in a table exists. If an
     * exception is thrown in the process, PDI will assume that the field does not exist.
     */
    public String getSQLColumnExists(String columnname, String tablename) {
        return "SELECT " + columnname + " FROM " + tablename + " WHERE 1=0";
    }

    /**
     * Returns the name of the JDBC driver class to use for this type of database
     */
    public String getDriverClass() {
        return "com.mysql.jdbc.Driver";
    }

    /**
     * @param hostname     ignored in this implementation
     * @param port         ignored in this implementation
     * @param databaseName the directory containing CSV files.
     * @return the connection string based on hostname, port and databasename.
     */
    public String getURL(String hostname, String port, String databaseName) throws KettleDatabaseException {

        return "jdbc:mysql://:" + hostname + "/" + (port != null && port.length() > 0 ? port : "") + "/" + databaseName;
    }

    public boolean supportsOptionsInURL() {
        return true;
    }

    /**
     * Returns an URL showing the supported options in the "options" section of the database connection dialog.
     */
    public String getExtraOptionsHelpText() {
        return "https://dev.mysql.com/doc/connector-j/";
    }

    /**
     * Returns reserved words for CsvJdbc
     */
    public String[] getReservedWords() {
        return new String[]{"SELECT", "DISTINCT", "AS", "FROM", "WHERE", "NOT", "AND", "OR", "ORDER", "BY", "ASC", "DESC",
                "NULL", "COUNT", "LOWER", "MAX", "MIN", "ROUND", "UPPER", "BETWEEN", "IS", "LIKE"};
    }

    /**
     * Returns the jar files required for the driver to work.
     */
    public String[] getUsedLibraries() {
        return new String[]{"mysql-connector-java-8.0.18.jar"};
    }

    /**
     * Returns whether a prepared JDBC statement is enough to determine the result field layout.
     */
    public boolean supportsPreparedStatementMetadataRetrieval() {
        return false;
    }

    /**
     * Returns whether the only way to get the field layout of a query, is to actually look at the result set.
     * Some databases provide more efficient ways (looking at a prepared or executed statement for instance)
     */
    public boolean supportsResultSetMetadataRetrievalOnly() {
        return true;
    }

    /**
     * Returns whether the database in question supports release of savepoints.
     */
    public boolean releaseSavepoint() {
        return false;
    }

    /**
     * Returns whether the database in question supports transactions.
     */
    public boolean supportsTransactions() {
        return false;
    }

    /**
     * This method is used to generate DDL for create table statements etc. in Spoon.
     * Creating and modifying fields is not supported by the csv driver
     */
    public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr) {
        return "";
    }

    /**
     * This method is used to generate DDL for create table statements etc. in Spoon.
     * Adding fields is not supported by the csv driver
     */
    public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon) {
        return "";
    }

    /**
     * This method is used to generate DDL for create table statements etc. in Spoon.
     * Modifying fields is not supported by the csv driver
     */
    public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon) {
        return "";
    }
}
