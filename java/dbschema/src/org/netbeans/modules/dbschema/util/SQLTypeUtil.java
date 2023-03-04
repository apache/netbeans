/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.dbschema.util;

import java.sql.Types;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLTypeUtil extends Object {

    // ===================== i18n utilities ===========================
    /**
     * Computes the localized string for the key.
     *
     * @param key The key of the string.
     * @return the localized string.
     */
    public static String getString(String key) {
        return ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString(key);
    }

	// ===================== sql type utilities ===========================
    /**
     * Convert sql types to String for display
     *
     * @param sqlType the type number from java.sql.Types
     * @return the type name
     */
    public static String getSqlTypeString(int sqlType) {
        switch (sqlType) {
            case Types.BIGINT:
                return "BIGINT"; //NOI18N
            case Types.BINARY:
                return "BINARY"; //NOI18N
            case Types.BIT:
                return "BIT"; //NOI18N
            case Types.NCHAR:
                return "NCHAR"; //NOI18N
            case Types.CHAR:
                return "CHAR"; //NOI18N
            case Types.DATE:
                return "DATE"; //NOI18N
            case Types.DECIMAL:
                return "DECIMAL"; //NOI18N
            case Types.DOUBLE:
                return "DOUBLE"; //NOI18N
            case Types.FLOAT:
                return "FLOAT"; //NOI18N
            case Types.INTEGER:
                return "INTEGER"; //NOI18N
            case Types.LONGVARBINARY:
                return "LONGVARBINARY"; //NOI18N
            case Types.LONGNVARCHAR:
                return "LONGNVARCHAR"; //NOI18N
            case Types.LONGVARCHAR:
                return "LONGVARCHAR"; //NOI18N
            case Types.NULL:
                return "NULL"; //NOI18N
            case Types.NUMERIC:
                return "NUMERIC"; //NOI18N
            case Types.OTHER:
                return "OTHER"; //NOI18N
            case Types.REAL:
                return "REAL"; //NOI18N
            case Types.SMALLINT:
                return "SMALLINT"; //NOI18N
            case Types.TIME:
                return "TIME"; //NOI18N
            case Types.TIMESTAMP:
                return "TIMESTAMP"; //NOI18N
            case Types.TINYINT:
                return "TINYINT"; //NOI18N
            case Types.VARBINARY:
                return "VARBINARY"; //NOI18N
            case Types.NVARCHAR:
                return "NVARCHAR";
            case Types.VARCHAR:
                return "VARCHAR"; //NOI18N
            case Types.JAVA_OBJECT:
                return "JAVA_OBJECT"; //NOI18N
            case Types.DISTINCT:
                return "DISTINCT"; //NOI18N
            case Types.STRUCT:
                return "STRUCT"; //NOI18N
            case Types.ARRAY:
                return "ARRAY"; //NOI18N
            case Types.BLOB:
                return "BLOB"; //NOI18N
            case Types.NCLOB:
                return "NCLOB";
            case Types.CLOB:
                return "CLOB"; //NOI18N
            case Types.REF:
                return "REF"; //NOI18N
            default:
                Logger.getLogger(SQLTypeUtil.class.getName()).log(Level.WARNING, "Unknown JDBC column type: {0}. Returns null.", sqlType);
                return "UNKNOWN"; //NOI18N
        }
    }

    /**
     * Returns if the given data type is numeric type or not.
     *
     * @param type the type from java.sql.Types
     * @return true if the given type is numeric type; false otherwise
     */
    public static boolean isNumeric(int type) {
        switch (type) {
            case Types.BIGINT:
            case Types.BIT:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.INTEGER:
            case Types.NUMERIC:
            case Types.REAL:
            case Types.SMALLINT:
            case Types.TINYINT:
                return true;
        }

        return false;
    }

    /**
     * Returns if the given data type is character type or not.
     *
     * @param type the type from java.sql.Types
     * @return true if the given type is character type; false otherwise
     */
    public static boolean isCharacter(int type) {
        switch (type) {
            case Types.BINARY:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
            case Types.VARBINARY:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return true;
        }

        return false;
    }

    /**
     * Return if a given data type is blob type or not. Note: CLOB should really
     * not be in this list, use isLob method for that.
     *
     * @param type the type from java.sql.Types return true if the give type is
     * blob type; false otherwise
     */
    public static boolean isBlob(int type) {
        switch (type) {
            case Types.BLOB:
            case Types.CLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.OTHER:
            case Types.NCLOB:
                return true;
        }

        return false;
    }

    /**
     * Return if a given data type is LOB (large object) type or not. Note:
     * Implementation of this method uses isBlob method but also duplicates the
     * check of CLOB because CLOB should really not return true from isBlob.
     * However, there might be other non-IDE callers of the isBlob method (like
     * appserver) so the CLOB check is left in both places for now.
     *
     * @param type the type from java.sql.Types return true if the give type is
     * lob type; false otherwise
     */
    public static boolean isLob(int type) {
        return (isBlob(type) 
                || (Types.CLOB == type) 
                || (Types.LONGVARCHAR == type));
    }

    /**
     * Returns if two data types are compatible or not.
     *
     * @param type1 first type to compare
     * @param type2 second type to compare
     * @return true if the types are compatible; false otherwise
     */
    public static boolean isCompatibleType(int type1, int type2) {
        return ((type1 == type2)
                || (isCharacter(type1) && isCharacter(type2))
                || (isNumeric(type1) && isNumeric(type2))
                || (isBlob(type1) && isBlob(type2)));
    }
}
