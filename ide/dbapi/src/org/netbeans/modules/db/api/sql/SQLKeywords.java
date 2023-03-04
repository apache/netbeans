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

package org.netbeans.modules.db.api.sql;

import java.util.Arrays;

/**
 * An utility class for working with SQL keywords. Currently it provides
 * the list of reserved and non-reserved SQL-99 keywords.
 *
 * @author Andrei Badea
 *
 * @since 1.2
 */
public final class SQLKeywords {

    // the following lists are searched using binary search
    // so they should be lexicographically sorted

    // the source of the SQL-99 keyword lists is
    // the SQL-99 Complete book by Gulutzan and Pelzer

    /**
     * The list of SQL-99 reserved keywords. Not private because of the tests.
     */
    static final String[] SQL99_RESERVED = {
        "ABSOLUTE", //NOI18N
        "ACTION", //NOI18N
        "ADD", //NOI18N
        "ADMIN", //NOI18N
        "AFTER", //NOI18N
        "AGGREGATE", //NOI18N
        "ALIAS", //NOI18N
        "ALL", //NOI18N
        "ALLOCATE", //NOI18N
        "ALTER", //NOI18N
        "AND", //NOI18N
        "ANY", //NOI18N
        "ARE", //NOI18N
        "ARRAY", //NOI18N
        "AS", //NOI18N
        "ASC", //NOI18N
        "ASSERTION", //NOI18N
        "AT", //NOI18N
        "AUTHORIZATION", //NOI18N
        "BEFORE", //NOI18N
        "BEGIN", //NOI18N
        "BINARY", //NOI18N
        "BIT", //NOI18N
        "BLOB", //NOI18N
        "BOOLEAN", //NOI18N
        "BOTH", //NOI18N
        "BREADTH", //NOI18N
        "BY", //NOI18N
        "CALL", //NOI18N
        "CASCADE", //NOI18N
        "CASCADED", //NOI18N
        "CASE", //NOI18N
        "CAST", //NOI18N
        "CATALOG", //NOI18N
        "CHAR", //NOI18N
        "CHARACTER", //NOI18N
        "CHECK", //NOI18N
        "CLASS", //NOI18N
        "CLOB", //NOI18N
        "CLOSE", //NOI18N
        "COLLATE", //NOI18N
        "COLLATION", //NOI18N
        "COLUMN", //NOI18N
        "COMMIT", //NOI18N
        "COMPLETION", //NOI18N
        "CONDITION", //NOI18N
        "CONNECT", //NOI18N
        "CONNECTION", //NOI18N
        "CONSTRAINT", //NOI18N
        "CONSTRAINTS", //NOI18N
        "CONSTRUCTOR", //NOI18N
        "CONTAINS", //NOI18N
        "CONTINUE", //NOI18N
        "CORRESPONDING", //NOI18N
        "CREATE", //NOI18N
        "CROSS", //NOI18N
        "CUBE", //NOI18N
        "CURRENT", //NOI18N
        "CURRENT_DATE", //NOI18N
        "CURRENT_PATH", //NOI18N
        "CURRENT_ROLE", //NOI18N
        "CURRENT_TIME", //NOI18N
        "CURRENT_TIMESTAMP", //NOI18N
        "CURRENT_USER", //NOI18N
        "CURSOR", //NOI18N
        "CYCLE", //NOI18N
        "DATA", //NOI18N
        "DATALINK", //NOI18N
        "DATE", //NOI18N
        "DAY", //NOI18N
        "DEALLOCATE", //NOI18N
        "DEC", //NOI18N
        "DECIMAL", //NOI18N
        "DECLARE", //NOI18N
        "DEFAULT", //NOI18N
        "DEFERRABLE", //NOI18N
        "DEFERRED", //NOI18N
        "DELETE", //NOI18N
        "DEPTH", //NOI18N
        "DEREF", //NOI18N
        "DESC", //NOI18N
        "DESCRIBE", //NOI18N
        "DESCRIPTOR", //NOI18N
        "DESTROY", //NOI18N
        "DESTRUCTOR", //NOI18N
        "DETERMINISTIC", //NOI18N
        "DIAGNOSTICS", //NOI18N
        "DICTIONARY", //NOI18N
        "DISCONNECT", //NOI18N
        "DISTINCT", //NOI18N
        "DO", //NOI18N
        "DOMAIN", //NOI18N
        "DOUBLE", //NOI18N
        "DROP", //NOI18N
        "DYNAMIC", //NOI18N
        "EACH", //NOI18N
        "ELSE", //NOI18N
        "ELSEIF", //NOI18N
        "END", //NOI18N
        "END-EXEC", //NOI18N
        "EQUALS", //NOI18N
        "ESCAPE", //NOI18N
        "EVERY", //NOI18N
        "EXCEPT", //NOI18N
        "EXCEPTION", //NOI18N
        "EXEC", //NOI18N
        "EXECUTE", //NOI18N
        "EXIT", //NOI18N
        "EXPAND", //NOI18N
        "EXPANDING", //NOI18N
        "EXTERNAL", //NOI18N
        "FALSE", //NOI18N
        "FETCH", //NOI18N
        "FIRST", //NOI18N
        "FLOAT", //NOI18N
        "FOR", //NOI18N
        "FOREIGN", //NOI18N
        "FOUND", //NOI18N
        "FREE", //NOI18N
        "FROM", //NOI18N
        "FULL", //NOI18N
        "FUNCTION", //NOI18N
        "GENERAL", //NOI18N
        "GET", //NOI18N
        "GLOBAL", //NOI18N
        "GO", //NOI18N
        "GOTO", //NOI18N
        "GRANT", //NOI18N
        "GROUP", //NOI18N
        "GROUPING", //NOI18N
        "HANDLER", //NOI18N
        "HASH", //NOI18N
        "HAVING", //NOI18N
        "HOST", //NOI18N
        "HOUR", //NOI18N
        "IDENTITY", //NOI18N
        "IF", //NOI18N
        "IGNORE", //NOI18N
        "IMMEDIATE", //NOI18N
        "IN", //NOI18N
        "INDICATOR", //NOI18N
        "INITIALIZE", //NOI18N
        "INITIALLY", //NOI18N
        "INNER", //NOI18N
        "INOUT", //NOI18N
        "INPUT", //NOI18N
        "INSERT", //NOI18N
        "INT", //NOI18N
        "INTEGER", //NOI18N
        "INTERSECT", //NOI18N
        "INTERVAL", //NOI18N
        "INTO", //NOI18N
        "IS", //NOI18N
        "ISOLATION", //NOI18N
        "ITERATE", //NOI18N
        "JOIN", //NOI18N
        "KEY", //NOI18N
        "LANGUAGE", //NOI18N
        "LARGE", //NOI18N
        "LAST", //NOI18N
        "LATERAL", //NOI18N
        "LEADING", //NOI18N
        "LEAVE", //NOI18N
        "LEFT", //NOI18N
        "LESS", //NOI18N
        "LEVEL", //NOI18N
        "LIKE", //NOI18N
        "LIMIT", //NOI18N
        "LOCAL", //NOI18N
        "LOCALTIME", //NOI18N
        "LOCALTIMESTAMP", //NOI18N
        "LOCATOR", //NOI18N
        "LOOP", //NOI18N
        "MATCH", //NOI18N
        "MEETS", //NOI18N
        "MINUTE", //NOI18N
        "MODIFIES", //NOI18N
        "MODIFY", //NOI18N
        "MODULE", //NOI18N
        "MONTH", //NOI18N
        "NAMES", //NOI18N
        "NATIONAL", //NOI18N
        "NATURAL", //NOI18N
        "NCHAR", //NOI18N
        "NCLOB", //NOI18N
        "NEW", //NOI18N
        "NEXT", //NOI18N
        "NO", //NOI18N
        "NONE", //NOI18N
        "NORMALIZE", //NOI18N
        "NOT", //NOI18N
        "NULL", //NOI18N
        "NUMERIC", //NOI18N
        "OBJECT", //NOI18N
        "OF", //NOI18N
        "OFF", //NOI18N
        "OLD", //NOI18N
        "ON", //NOI18N
        "ONLY", //NOI18N
        "OPEN", //NOI18N
        "OPERATION", //NOI18N
        "OPTION", //NOI18N
        "OR", //NOI18N
        "ORDER", //NOI18N
        "ORDINALITY", //NOI18N
        "OUT", //NOI18N
        "OUTER", //NOI18N
        "OUTPUT", //NOI18N
        "PAD", //NOI18N
        "PARAMETER", //NOI18N
        "PARAMETERS", //NOI18N
        "PARTIAL", //NOI18N
        "PATH", //NOI18N
        "PERIOD", //NOI18N
        "POSTFIX", //NOI18N
        "PRECEDES", //NOI18N
        "PRECISION", //NOI18N
        "PREFIX", //NOI18N
        "PREORDER", //NOI18N
        "PREPARE", //NOI18N
        "PRESERVE", //NOI18N
        "PRIMARY", //NOI18N
        "PRIOR", //NOI18N
        "PRIVILEGES", //NOI18N
        "PROCEDURE", //NOI18N
        "PUBLIC", //NOI18N
        "READ", //NOI18N
        "READS", //NOI18N
        "REAL", //NOI18N
        "RECURSIVE", //NOI18N
        "REDO", //NOI18N
        "REF", //NOI18N
        "REFERENCES", //NOI18N
        "REFERENCING", //NOI18N
        "RELATIVE", //NOI18N
        "REPEAT", //NOI18N
        "RESIGNAL", //NOI18N
        "RESTRICT", //NOI18N
        "RESULT", //NOI18N
        "RETURN", //NOI18N
        "RETURNS", //NOI18N
        "REVOKE", //NOI18N
        "RIGHT", //NOI18N
        "ROLE", //NOI18N
        "ROLLBACK", //NOI18N
        "ROLLUP", //NOI18N
        "ROUTINE", //NOI18N
        "ROW", //NOI18N
        "ROWS", //NOI18N
        "SAVEPOINT", //NOI18N
        "SCHEMA", //NOI18N
        "SCROLL", //NOI18N
        "SEARCH", //NOI18N
        "SECOND", //NOI18N
        "SECTION", //NOI18N
        "SELECT", //NOI18N
        "SEQUENCE", //NOI18N
        "SESSION", //NOI18N
        "SESSION_USER", //NOI18N
        "SET", //NOI18N
        "SETS", //NOI18N
        "SIGNAL", //NOI18N
        "SIZE", //NOI18N
        "SMALLINT", //NOI18N
        "SOME", //NOI18N
        "SPACE", //NOI18N
        "SPECIFIC", //NOI18N
        "SPECIFICTYPE", //NOI18N
        "SQL", //NOI18N
        "SQLEXCEPTION", //NOI18N
        "SQLSTATE", //NOI18N
        "SQLWARNING", //NOI18N
        "START", //NOI18N
        "STATE", //NOI18N
        "STATEMENT", //NOI18N
        "STATIC", //NOI18N
        "STRUCTURE", //NOI18N
        "SYSTEM_USER", //NOI18N
        "TABLE", //NOI18N
        "TEMPORARY", //NOI18N
        "TERMINATE", //NOI18N
        "THAN", //NOI18N
        "THEN", //NOI18N
        "TIME", //NOI18N
        "TIMESTAMP", //NOI18N
        "TIMEZONE_HOUR", //NOI18N
        "TIMEZONE_MINUTE", //NOI18N
        "TO", //NOI18N
        "TRAILING", //NOI18N
        "TRANSACTION", //NOI18N
        "TRANSLATION", //NOI18N
        "TREAT", //NOI18N
        "TRIGGER", //NOI18N
        "TRUE", //NOI18N
        "UNDER", //NOI18N
        "UNION", //NOI18N
        "UNIQUE", //NOI18N
        "UNKNOWN", //NOI18N
        "UNTIL", //NOI18N
        "UPDATE", //NOI18N
        "USAGE", //NOI18N
        "USER", //NOI18N
        "USING", //NOI18N
        "VALUE", //NOI18N
        "VALUES", //NOI18N
        "VARCHAR", //NOI18N
        "VARIABLE", //NOI18N
        "VARYING", //NOI18N
        "VIEW", //NOI18N
        "WHEN", //NOI18N
        "WHENEVER", //NOI18N
        "WHERE", //NOI18N
        "WHILE", //NOI18N
        "WITH", //NOI18N
        "WITHOUT", //NOI18N
        "WORK", //NOI18N
        "WRITE", //NOI18N
        "YEAR", //NOI18N
        "ZONE", //NOI18N
    };

    /**
     * The list of SQL-99 non-reserved keywords. Not private because of the tests.
     */
    static final String[] SQL99_NON_RESERVED = {
        "ABS", //NOI18N
        "ADA", //NOI18N
        "ASENSITIVE", //NOI18N
        "ASSIGNMENT", //NOI18N
        "ASYMMETRIC", //NOI18N
        "ATOMIC", //NOI18N
        "AVG", //NOI18N
        "BETWEEN", //NOI18N
        "BITVAR", //NOI18N
        "BIT_LENGTH", //NOI18N
        "BLOCKED", //NOI18N
        // removed due to 191983 - "C", //NOI18N
        "CARDINALITY", //NOI18N
        "CATALOG_NAME", //NOI18N
        "CHAIN", //NOI18N
        "CHARACTER_LENGTH", //NOI18N
        "CHARACTER_SET_CATALOG", //NOI18N
        "CHARACTER_SET_NAME", //NOI18N
        "CHARACTER_SET_SCHEMA", //NOI18N
        "CHAR_LENGTH", //NOI18N
        "CHECKED", //NOI18N
        "CLASS_ORIGIN", //NOI18N
        "COALESCE", //NOI18N
        "COBOL", //NOI18N
        "COLLATION_CATALOG", //NOI18N
        "COLLATION_NAME", //NOI18N
        "COLLATION_SCHEMA", //NOI18N
        "COLUMN_NAME", //NOI18N
        "COMMAND_FUNCTION", //NOI18N
        "COMMAND_FUNCTION_CODE", //NOI18N
        "COMMITTED", //NOI18N
        "CONCATENATE", //NOI18N
        "CONDITION_NUMBER", //NOI18N
        "CONNECTION_NAME", //NOI18N
        "CONSTRAINT_CATALOG", //NOI18N
        "CONSTRAINT_NAME", //NOI18N
        "CONSTRAINT_SCHEMA", //NOI18N
        "CONTAINS", //NOI18N
        "CONTROL", //NOI18N
        "CONVERT", //NOI18N
        "COUNT", //NOI18N
        "CURSOR_NAME", //NOI18N
        "DATABASE", // NOI18N
        "DATETIME_INTERVAL_CODE", //NOI18N
        "DATETIME_INTERVAL_PRECISION", //NOI18N
        "DB", //NOI18N
        "DELIMITER", // NOI18N
        "DISPATCH", //NOI18N
        "DLCOMMENT", //NOI18N
        "DLFILESIZE", //NOI18N
        "DLFILESIZEEXACT", //NOI18N
        "DLLINKTYPE", //NOI18N
        "DLURLCOMPLETE", //NOI18N
        "DLURLPATH", //NOI18N
        "DLURLPATHONLY", //NOI18N
        "DLURLSCHEMA", //NOI18N
        "DLURLSERVER", //NOI18N
        "DLVALUE", //NOI18N
        "DYNAMIC_FUNCTION", //NOI18N
        "DYNAMIC_FUNCTION_CODE", //NOI18N
        "EXISTING", //NOI18N
        "EXISTS", //NOI18N
        "EXTRACT", //NOI18N
        "FILE", //NOI18N
        "FINAL", //NOI18N
        "FORTRAN", //NOI18N
        "GENERATED", //NOI18N
        "HOLD", //NOI18N
        "INFIX", //NOI18N
        "INSENSITIVE", //NOI18N
        "INSTANTIABLE", //NOI18N
        "INTEGRITY", //NOI18N
        "KEY_MEMBER", //NOI18N
        "KEY_TYPE", //NOI18N
        "LENGTH", //NOI18N
        "LINK", //NOI18N
        "LOWER", //NOI18N
        "MAX", //NOI18N
        "MEDIUMINT", // MySQL data type (#152751) //NOI18N
        "MESSAGE_LENGTH", //NOI18N
        "MESSAGE_OCTET_LENGTH", //NOI18N
        "MESSAGE_TEXT", //NOI18N
        "METHOD", //NOI18N
        "MIN", //NOI18N
        "MOD", //NOI18N
        "MORE", //NOI18N
        "MUMPS", //NOI18N
        "NAME", //NOI18N
        "NULLABLE", //NOI18N
        "NULLIF", //NOI18N
        "NUMBER", //NOI18N
        "OCTET_LENGTH", //NOI18N
        "OPTIONS", //NOI18N
        "OVERLAPS", //NOI18N
        "OVERLAY", //NOI18N
        "OVERRIDING", //NOI18N
        "PARAMETER_MODE", //NOI18N
        "PARAMETER_NAME", //NOI18N
        "PARAMETER_ORDINAL_POSITION", //NOI18N
        "PARAMETER_SPECIFIC_CATALOG", //NOI18N
        "PARAMETER_SPECIFIC_NAME", //NOI18N
        "PARAMETER_SPECIFIC_SCHEMA", //NOI18N
        "PASCAL", //NOI18N
        "PERMISSION", //NOI18N
        "PLI", //NOI18N
        "POSITION", //NOI18N
        "RECOVERY", //NOI18N
        "REPEATABLE", //NOI18N
        "REPLACE", // NOI18N
        "RESTORE", //NOI18N
        "RETURNED_LENGTH", //NOI18N
        "RETURNED_OCTET_LENGTH", //NOI18N
        "RETURNED_SQLSTATE", //NOI18N
        "ROUTINE_CATALOG", //NOI18N
        "ROUTINE_NAME", //NOI18N
        "ROUTINE_SCHEMA", //NOI18N
        "ROW_COUNT", //NOI18N
        "ROW_TYPE_CATALOG", //NOI18N
        "ROW_TYPE_NAME", //NOI18N
        "ROW_TYPE_SCHEMA", //NOI18N
        "SCALE", //NOI18N
        "SCHEMA_NAME", //NOI18N
        "SELECTIVE", //NOI18N
        "SELF", //NOI18N
        "SENSITIVE", //NOI18N
        "SERIALIZABLE", //NOI18N
        "SERVER_NAME", //NOI18N
        "SIMILAR", //NOI18N
        "SIMPLE", //NOI18N
        "SOURCE", //NOI18N
        "SPECIFIC_NAME", //NOI18N
        "STRUCTURE", //NOI18N
        "STYLE", //NOI18N
        "SUBCLASS_ORIGIN", //NOI18N
        "SUBLIST", //NOI18N
        "SUBSTRING", //NOI18N
        "SUM", //NOI18N
        "SYMMETRIC", //NOI18N
        "SYSTEM", //NOI18N
        "TABLE_NAME", //NOI18N
        "TRANSACTIONS_COMMITTED", //NOI18N
        "TRANSACTIONS_ROLLED_BACK", //NOI18N
        "TRANSACTION_ACTIVE", //NOI18N
        "TRANSFORM", //NOI18N
        "TRANSLATE", //NOI18N
        "TRIGGER_CATALOG", //NOI18N
        "TRIGGER_NAME", //NOI18N
        "TRIGGER_SCHEMA", //NOI18N
        "TRIM", //NOI18N
        "TYPE", //NOI18N
        "UNCOMMITTED", //NOI18N
        "UNLINK", //NOI18N
        "UNNAMED", //NOI18N
        "UPPER", //NOI18N
        "USER_DEFINED_TYPE_CATALOG", //NOI18N
        "USER_DEFINED_TYPE_NAME", //NOI18N
        "USER_DEFINED_TYPE_SCHEMA", //NOI18N
        "YES", //NOI18N
    };

    private SQLKeywords() {
    }

    /**
     * Returns true if the given identifier is a SQL-99 reserved keyword.
     *
     * @param  identifier the identifier to test; case does not matter; cannot be null.
     * @return true if <code>identifier</code> is a SQL-99 reserved keyword,
     *         false otherwise.
     * @throws NullPointerException if <code>identifier</code> is null.
     */
    public static boolean isSQL99ReservedKeyword(String identifier) {
        if (identifier == null) {
            throw new NullPointerException("The identifier cannot be null"); // NOI18N
        }
        return Arrays.binarySearch(SQL99_RESERVED, identifier.toUpperCase()) >= 0;
    }

    /**
     * Returns true if the given identifier is a SQL-99 non-reserved keyword.
     *
     * @param  identifier the identifier to test; case does not matter; cannot be null.
     * @return true if <code>identifier</code> is a SQL-99 non-reserved keyword,
     *         false otherwise.
     * @throws NullPointerException if <code>identifier</code> is null.
     */
    public static boolean isSQL99NonReservedKeyword(String identifier) {
        if (identifier == null) {
            throw new NullPointerException("The identifier cannot be null"); // NOI18N
        }
        return Arrays.binarySearch(SQL99_NON_RESERVED, identifier.toUpperCase()) >= 0;
    }

    /**
     * Returns true if the given identifier is a SQL-99 keyword (reserved or
     * non-reserved).
     *
     * @param  identifier the identifier to test; case does not matter; cannot be null.
     * @return true if <code>identifier</code> is a SQL-99 non-reserved keyword,
     *         false otherwise.
     * @throws NullPointerException if <code>identifier</code> is null.
     */
    public static boolean isSQL99Keyword(String identifier) {
        return isSQL99ReservedKeyword(identifier) || isSQL99NonReservedKeyword(identifier);
    }

    /**
     * Returns true if the given identifier is a SQL-99 keyword (reserved or
     * non-reserved).
     *
     * @param  identifier the identifier to test; use upper case only; cannot be null.
     * @param upperOnly only upper case allowed
     * @return true if <code>identifier</code> is a SQL-99 non-reserved keyword,
     *         false otherwise.
     * @throws NullPointerException if <code>identifier</code> is null.
     */
    public static boolean isSQL99Keyword(String identifier, boolean upperOnly) {
        if (identifier == null) {
            throw new NullPointerException("The identifier cannot be null"); // NOI18N
        }
        if (upperOnly) {
            return Arrays.binarySearch(SQL99_RESERVED, identifier) >= 0 || Arrays.binarySearch(SQL99_NON_RESERVED, identifier) >= 0;
        } else {
            return isSQL99ReservedKeyword(identifier) || isSQL99NonReservedKeyword(identifier);
        }
    }
}
