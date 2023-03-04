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

package org.netbeans.lib.ddl.adaptors;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.SimpleBeanInfo;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class DefaultAdaptorBeanInfo extends SimpleBeanInfo
{
    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        PropertyDescriptor[] desc = null;
        try {
            
            desc = new PropertyDescriptor[] {
                
                // Basic properties: 65 items
                
                new PropertyDescriptor(DefaultAdaptor.PROP_PROCEDURES_ARE_CALLABLE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_TABLES_ARE_SELECTABLE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_READONLY, DefaultAdaptor.class, "getreadOnly", "setreadOnly"), // NOI18N
                new PropertyDescriptor(DefaultAdaptor.PROP_LOCAL_FILES, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_FILE_PER_TABLE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MIXEDCASE_IDENTIFIERS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MIXEDCASE_QUOTED_IDENTIFIERS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_ALTER_ADD, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_ALTER_DROP, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_COLUMN_ALIASING, DefaultAdaptor.class),
                
                new PropertyDescriptor(DefaultAdaptor.PROP_NULL_PLUS_NULL_IS_NULL, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CONVERT, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_TABLE_CORRELATION_NAMES, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_DIFF_TABLE_CORRELATION_NAMES, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_EXPRESSIONS_IN_ORDERBY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_ORDER_BY_UNRELATED, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_GROUP_BY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_UNRELATED_GROUP_BY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_BEYOND_GROUP_BY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_ESCAPE_LIKE, DefaultAdaptor.class),
                
                new PropertyDescriptor(DefaultAdaptor.PROP_MULTIPLE_RS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MULTIPLE_TRANSACTIONS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_NON_NULL_COLUMNSS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MINUMUM_SQL_GRAMMAR, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CORE_SQL_GRAMMAR, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_EXTENDED_SQL_GRAMMAR, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_ANSI_SQL_GRAMMAR, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_INTERMEDIATE_SQL_GRAMMAR, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_FULL_SQL_GRAMMAR, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_INTEGRITY_ENHANCEMENT, DefaultAdaptor.class),
                
                new PropertyDescriptor(DefaultAdaptor.PROP_OUTER_JOINS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_FULL_OUTER_JOINS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_LIMITED_OUTER_JOINS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CATALOG_AT_START, DefaultAdaptor.class, "getcatalogAtStart", "setcatalogAtStart"), // NOI18N
                new PropertyDescriptor(DefaultAdaptor.PROP_SCHEMAS_IN_DML, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SCHEMAS_IN_PROCEDURE_CALL, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SCHEMAS_IN_TABLE_DEFINITION, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SCHEMAS_IN_INDEX, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CATALOGS_IN_DML, DefaultAdaptor.class),
                
                new PropertyDescriptor(DefaultAdaptor.PROP_CATALOGS_IN_PROCEDURE_CALL, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CATALOGS_IN_TABLE_DEFINITION, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CATALOGS_IN_INDEX, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CATALOGS_IN_PRIVILEGE_DEFINITION, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_POSITIONED_DELETE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_POSITIONED_UPDATE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SELECT_FOR_UPDATE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_STORED_PROCEDURES, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SUBQUERY_IN_COMPARSIONS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SUBQUERY_IN_EXISTS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SUBQUERY_IN_INS, DefaultAdaptor.class),
                
                new PropertyDescriptor(DefaultAdaptor.PROP_SUBQUERY_IN_QUANTIFIEDS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CORRELATED_SUBQUERIES, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_UNION, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_UNION_ALL, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_COMMIT, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_OPEN_CURSORS_ACROSS_ROLLBACK, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_COMMIT, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_ROWSIZE_INCLUDING_BLOBS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_TRANSACTIONS, DefaultAdaptor.class),
                
                new PropertyDescriptor(DefaultAdaptor.PROP_DDL_AND_DML_TRANSACTIONS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_DML_TRANSACTIONS_ONLY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_DDL_CAUSES_COMMIT, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_DDL_IGNORED_IN_TRANSACTIONS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_BATCH_UPDATES, DefaultAdaptor.class),
                
                // Integer properties: 24 items
                
                new PropertyDescriptor(DefaultAdaptor.PROP_NULL_SORT, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_IDENTIFIER_STORE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_QUOTED_IDENTS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_BINARY_LITERAL_LENGTH, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_CHAR_LITERAL_LENGTH, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_COLUMN_NAME_LENGTH, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_COLUMNS_IN_GROUPBY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_COLUMNS_IN_INDEX, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_COLUMNS_IN_ORDERBY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_COLUMNS_IN_SELECT, DefaultAdaptor.class),
                
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_COLUMNS_IN_TABLE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_CONNECTIONS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_CURSORNAME_LENGTH, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_INDEX_LENGTH, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_SCHEMA_NAME, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_PROCEDURE_NAME, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_CATALOG_NAME, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_ROW_SIZE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_STATEMENT_LENGTH, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_STATEMENTS, DefaultAdaptor.class),
                
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_TABLENAME_LENGTH, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_TABLES_IN_SELECT, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_MAX_USERNAME, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_DEFAULT_ISOLATION, DefaultAdaptor.class),
                
                // String properties: 20 items
                
                new PropertyDescriptor(DefaultAdaptor.PROP_URL, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_USERNAME, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_PRODUCTNAME, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_PRODUCTVERSION, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_DRIVERNAME, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_DRIVER_VERSION, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_DRIVER_MAJOR_VERSION, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_DRIVER_MINOR_VERSION, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_IDENTIFIER_QUOTE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SQL_KEYWORDS, DefaultAdaptor.class),
                
                new PropertyDescriptor(DefaultAdaptor.PROP_NUMERIC_FUNCTIONS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_STRING_FUNCTIONS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SYSTEM_FUNCTIONS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_TIME_FUNCTIONS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_STRING_ESCAPE, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_EXTRA_CHARACTERS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SCHEMA_TERM, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_PROCEDURE_TERM, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CATALOG_TERM, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CATALOGS_SEPARATOR, DefaultAdaptor.class),
                
                // Extensions
                
                new PropertyDescriptor(DefaultAdaptor.PROP_CAPITALIZE_USERNAME, DefaultAdaptor.class),
                
                // Queries
                
                new PropertyDescriptor(DefaultAdaptor.PROP_PROCEDURES_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_PROCEDURE_COLUMNS_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_SCHEMAS_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CATALOGS_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_TABLES_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_TABLE_TYPES_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_COLUMNS_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_COLUMNS_PRIVILEGES_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_TABLE_PRIVILEGES_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_BEST_ROW_IDENTIFIER, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_VERSION_COLUMNS, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_PK_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_IK_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_EK_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_CROSSREF_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_TYPE_INFO_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_INDEX_INFO_QUERY, DefaultAdaptor.class),
                new PropertyDescriptor(DefaultAdaptor.PROP_UDT_QUERY, DefaultAdaptor.class)
            };
            
            for (int i = 0; i < desc.length; i++) {
                try {
                    String name = "PROP_"+desc[i].getName(); // NOI18N
                    if (i>109) desc[i].setDisplayName(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString(name)); //NOI18N
                    if (i<65) desc[i].setPropertyEditorClass(BooleanEditor.class);
                    if (i<110) desc[i].setExpert(true);
                } catch (Exception ex) {}
            }
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return desc;
    }

    public static class BooleanEditor extends CommonEditor
    {
        public BooleanEditor()
        {
            super (
                new int[] {	DefaultAdaptor.NOT_SET, DefaultAdaptor.YES, DefaultAdaptor.NO },
                new String[] { NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("NotSet"), 
                               NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("Yes"),
                               NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("No") }
            );
        }
    }

    private static class CommonEditor implements PropertyEditor
    {
        private PropertyChangeSupport support;
        private int[] constants;
        private String[] names;
        private int index;
        private String name;

        public CommonEditor(int[] c, String[] n)
        {
            support = new PropertyChangeSupport(this);
            constants = c;
            names = n;
        }

        public Object getValue()
        {
            return Integer.valueOf(constants[index]);
        }

        public void setValue(Object object)
        {
            int value, k = constants.length;

            if (object == null) return;
            if (object instanceof Integer) {
                value = ((Integer)object).intValue();
            } else throw new IllegalArgumentException();

            for (int i = 0; i < k; i++) {
                if (constants[i] == value) {
                    index = i;
                    name = names[i];
                    support.firePropertyChange ("", null, null);
                    return;
                }
            }

            throw new IllegalArgumentException();
        }

        public String getAsText()
        {
            if (index == 0) return "";
            return name;
        }

        public void setAsText(String string) throws IllegalArgumentException
        {
            if (string == null) return;
            int k = names.length;
            for (int i = 0; i < k; i++) {
                if (names[i].equals(string)) {
                    index = i;
                    name = names[i];
                    support.firePropertyChange("", null, null);
                    return;
                }
            }

            throw new IllegalArgumentException ();
        }

        public String getJavaInitializationString()
        {
            return ""+index;
        }

        public String[] getTags()
        {
            return names;
        }

        public boolean isPaintable()
        {
            return false;
        }

        public void paintValue(Graphics g, Rectangle rectangle)
        {
        }

        public boolean supportsCustomEditor()
        {
            return false;
        }

        public Component getCustomEditor()
        {
            return null;
        }

        public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener)
        {
            support.addPropertyChangeListener (propertyChangeListener);
        }

        public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener)
        {
            support.removePropertyChangeListener (propertyChangeListener);
        }
    }
}
