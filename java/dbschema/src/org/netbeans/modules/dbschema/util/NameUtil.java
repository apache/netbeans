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

public class NameUtil {

    /** Column names separator in the column pair name. */
    private static final char columnPairSeparator = ';';

    /** Element namess separator in the fully qualified name. */
    public static final char dbElementSeparator = '.';

    /** Returns schema resource name.
     * @return the schema resource name
     */
    public static String getSchemaResourceName(String schemaName) {
        return (schemaName + ".dbschema"); //NOI18N
    }

    /**
     * Returns the table name from a given db member name. 
     * The member might be a column or a column pair.
     * @param memberName the full qualified member name
     * @return the table name of this member name
     */
    public static String getTableName (String memberName) {
        int index = memberName.indexOf(columnPairSeparator);
        String tempString = ((index != -1) ? memberName.substring(0, index) : memberName);

        return tempString.substring(0, tempString.lastIndexOf(dbElementSeparator));
    }

    /**
     * Returns the schema name from a given db member name. 
     * The member might be a column, a table or a column pair.
     * @param memberName the full qualified member name
     * @return the schema name of this member name
     */
    public static String getSchemaName(String memberName)
    {
        if (memberName == null)
            return null;
        int index = memberName.indexOf(columnPairSeparator);
        String tempString = ((index != -1) ? memberName.substring(0, index) : memberName);

        return tempString.substring(0, tempString.indexOf(dbElementSeparator));
    }

    /**
     * The method returns the relative name for the specified table name. 
     * If the specified name is already relative, the method returns it as it is. 
     * Otherwise it strips the schema name from the given table name.
     * @param tableName a table name
     * @return the relative table name
     */
    public static String getRelativeTableName(String tableName)
    {
        if (tableName == null)
            return null;

        if (isRelativeTableName(tableName))
            return tableName;

        return tableName.substring(tableName.indexOf(dbElementSeparator) + 1); 
    }

    /**
     * The method returns the relative name for the specified member name. 
     * If the specified name is already relative, the method returns member name 
     * equal to the input argument. 
     * Otherwise it strips the schema name from the given member name. 
     * In the case of a column pair it stips the schema name from both column names.
     * @param tableName a member name
     * @return the relative member name
     */
    public static String getRelativeMemberName(String memberName)
    {
        if (memberName == null)
            return null;

        int semicolonIndex = memberName.indexOf(columnPairSeparator);
        if (semicolonIndex != -1)
        {
            String firstColumn = memberName.substring(0, semicolonIndex);
            String secondColumn = memberName.substring(semicolonIndex + 1);
            return getRelativeMemberNameInternal(firstColumn) + columnPairSeparator + 
                getRelativeMemberNameInternal(secondColumn);
        }
        else
        {
            return getRelativeMemberNameInternal(memberName);
        }
    }

    /**
     * The method returns the relative name for the specified member name. 
     * Note, this is an internal helper method which fails for a column pair name.
     * If the specified name is already relative, the method returns it as it is. 
     * Otherwise it strips the schema name from the given member name.
     * @param memberName a non column pair member name
     * @return the relative member name
     * @see #getRelativeMemberName
     */
    private static String getRelativeMemberNameInternal(String memberName)
    {
        if (memberName == null)
            return null;
        
        if (isRelativeMemberName(memberName))
            return memberName;
        
        return memberName.substring(memberName.indexOf(dbElementSeparator) + 1); 
    }
    
    /**
     * The method returns a absolute (full qualified) name for the specified table name 
     * using the specified schema name. 
     * If the specified name is already absolute, the method returns it as it is, 
     * without checking the schema part of the name beeing equal to the specified schema name.
     * @param schemaName the schema name to be prepended
     * @param tableName the table name
     * @return the absolute table name
     */
    public static String getAbsoluteTableName(String schemaName, String tableName)
    {
        if (tableName == null)
            return null;

        if (!isRelativeTableName(tableName))
            return tableName;
        
        return schemaName + dbElementSeparator + tableName;
    }

    /**
     * The method returns a absolute (full qualified) name for the specified member name 
     * using the specified schema name. 
     * If the specified name is already absolute, the method returns a member name equal to the input
     * argument,without checking the schema part of the name beeing equal to the specified schema name.
     * In the case of a column pair it prepends the schemaName to both columns included in the pair.
     * @param schemaName the schema name to be prepended
     * @param memberName the member name
     * @return the absolute member name
     */
    public static String getAbsoluteMemberName(String schemaName, String memberName)
    {
        if (memberName == null)
            return null;

        int semicolonIndex = memberName.indexOf(columnPairSeparator);
        if (semicolonIndex != -1)
        {
            String firstColumn = memberName.substring(0, semicolonIndex);
            String secondColumn = memberName.substring(semicolonIndex + 1);
            return getAbsoluteMemberNameInternal(schemaName, firstColumn) + 
                   columnPairSeparator + 
                   getAbsoluteMemberNameInternal(schemaName, secondColumn);
        }
        else
        {
            return getAbsoluteMemberNameInternal(schemaName, memberName);
        }
    }

    /**
     * The method returns a absolute (full qualified) name for the specified member name 
     * using the specified schema name. 
     * Note, this is an internal helper method, that fails if it gets a column pair name.
     * If the specified name is already absolute, the method returns it as it is, 
     * without checking the schema part of the name beeing equal to the specified schema name.
     * @param schemaName the schema name to be prepended
     * @param memberName the non column pair member name
     * @return the absolute member name
     * @see #getAbsoluteMemberName
     */
    private static String getAbsoluteMemberNameInternal(String schemaName, String memberName)
    {
        if (memberName == null)
            return null;
        if (!isRelativeMemberName(memberName))
            return memberName;

        return schemaName + dbElementSeparator + memberName;
    }

    /**
     * Returns true if the specified table name is relative.
     * Note, this method fails for member names, 
     * use isRelativeMemberName instead.
     * @param tableName relative or absolute table name
     * @return true if tableName is relative; false otherwise
     * @see #isRelativeMemberName
     */
    private static boolean isRelativeTableName(String tableName)
    {
        // A relative table name does not contain a dbElementSeparator
        return tableName.indexOf(dbElementSeparator) == -1;
    }
    
    /**
     * Returns true if the specified member name is relative.
     * Note, the method fails for a relative table name, please use isRelativeTableName instead. 
     * Note, the method fails for a column pair name (both relative or absolute), 
     * split the column pair name first and then call this method.
     * @param columnName relative or absolute column name
     * @return true if columnName is relative; false otherwise
     * @see #isRelativeTableName
     */
    private static boolean isRelativeMemberName(String columnName)
    {
        // A relative column name contains a single dbElementSeparator
        int first = columnName.indexOf(dbElementSeparator);
        return columnName.indexOf(dbElementSeparator, first + 1) == -1;
    }
}
