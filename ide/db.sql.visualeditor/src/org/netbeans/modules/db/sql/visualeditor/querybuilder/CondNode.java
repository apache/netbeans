/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.sql.SQLException;

import java.beans.PropertyEditorSupport;
import java.beans.PropertyEditor;
import java.util.List;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.Reflection;

import org.openide.util.NbBundle;

import org.netbeans.modules.db.sql.visualeditor.querymodel.Expression;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Predicate;

import org.openide.util.Exceptions;

// A class for providing properties to arcs (joins)
public class CondNode extends AbstractNode
{
    // Private variable

    private String              _table1="";  // NOI18N
    private String              _column1="";  // NOI18N
    private String              _table2="";  // NOI18N
    private String              _column2="";  // NOI18N
    private QueryBuilder        _queryBuilder;


    // Constructors

    CondNode(String table1, String column1, String table2, String column2, QueryBuilder queryBuilder)
    {
        super(Children.LEAF);
        _table1 = table1;
        _column1 = column1;
        _table2 = table2;
        _column2 = column2;
        _queryBuilder = queryBuilder;
    }


    // Accessors/mutators

    public String getTable1() {
        return _table1;
    }

    public String getColumn1() {
        return _column1;
    }

    public void setColumn1(String column1) {
        Expression cond = findCond(_table1, _column1, _table2, _column2);
        _column1 = column1;
        updateModel(cond);
    }

    public String getTable2() {
        return _table2;
    }

    public String getColumn2() {
        return _column2;
    }

    public void setColumn2(String column2) {
        Expression cond = findCond(_table1, _column1, _table2, _column2);
        _column2 = column2;
        updateModel(cond);
    }


    // Create minimal property sheet
    // Column names are editable, nothing else is

    protected Sheet createSheet() {
        Sheet s = Sheet.createDefault();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        try {
            PropertySupport.Reflection p;
            p = new Reflection(this, String.class, "getTable1", null); // NOI18N
            p.setName("table1"); // NOI18N
//            p.setDisplayName("Table 1"); // NOI18N
//            p.setShortDescription("first table for join"); // NOI18N

            String table1 = NbBundle.getMessage(CondNode.class, "TABLE_1");    // NOI18N
            p.setDisplayName(table1);

            String table1ShortDescription = NbBundle.getMessage(CondNode.class, "TABLE_1_SHORT_DESCRIPTION");    // NOI18N
            p.setShortDescription(table1ShortDescription);
            ss.put(p);
            p = new Reflection(this, String.class, "getColumn1", "setColumn1") {
                    public PropertyEditor getPropertyEditor () {
                        return new ColumnPropertyEditor1 ();
                    }}; // NOI18N
            p.setName("column1"); // NOI18N
//            p.setDisplayName("Column 1"); // NOI18N
//            p.setShortDescription("First column for join"); // NOI18N

            String column1 = NbBundle.getMessage(CondNode.class, "COLUMN_1");    // NOI18N
            p.setDisplayName(column1);

            String column1ShortDescription = NbBundle.getMessage(CondNode.class, "COLUMN_1_SHORT_DESCRIPTION");    // NOI18N
            p.setShortDescription(column1ShortDescription);
//          p.setPropertyEditorClass(ColumnPropertyEditor.class);
            ss.put(p);
            p = new Reflection(this, String.class, "getTable2", null); // NOI18N
            p.setName("table2"); // NOI18N
//            p.setDisplayName("Table 2"); // NOI18N
//            p.setShortDescription("second table for join"); // NOI18N

            String table2 = NbBundle.getMessage(CondNode.class, "TABLE_2");    // NOI18N
            p.setDisplayName(table2);

            String table2ShortDescription = NbBundle.getMessage(CondNode.class, "TABLE_2_SHORT_DESCRIPTION");    // NOI18N
            p.setShortDescription(table2ShortDescription); 

            ss.put(p);
            p = new Reflection(this, String.class, "getColumn2", "setColumn2") {
                    public PropertyEditor getPropertyEditor () {
                        return new ColumnPropertyEditor2 ();
                    }}; // NOI18N
            p.setName("column2"); // NOI18N
//            p.setDisplayName("Column 2"); // NOI18N
//            p.setShortDescription("Second column for join"); // NOI18N

            String column2 = NbBundle.getMessage(CondNode.class, "COLUMN_2");    // NOI18N
            p.setDisplayName(column2);

            String column2ShortDescription = NbBundle.getMessage(CondNode.class, "COLUMN_2_SHORT_DESCRIPTION");    // NOI18N
            p.setShortDescription(column2ShortDescription); 
//          p.setPropertyEditorClass(ColumnPropertyEditor.class);
            ss.put(p);
        } catch (NoSuchMethodException nsme) {
            Exceptions.printStackTrace(nsme);
        }
        return s;
    }


    // Suppress label printing in graph
    
    public String toString() {
        return "";  // NOI18N
    }

    
    // Find the JoinTable object behind this edge
    // We may eventually add links from graph to model to simplify operations like this
    Expression findCond(String _table1, String _column1, String _table2, String _column2) {
        return _queryBuilder._queryModel.findCond(_table1, _column1, _table2, _column2);
    }


    private void updateModel(Expression cond) {

        // Set the appropriate fields in this Expression
        if (cond instanceof Predicate) {
            Predicate pred = (Predicate) cond;
            pred.setFields(_table1, _column1, _table2, _column2);
        }

        // And rebuild the text query
        _queryBuilder.generateText();
    }


    // Property Editors
    
    public class JoinTypePropertyEditor extends PropertyEditorSupport {

        private String[] tags    = 
            new String[] { "INNER",             // NOI18N
                           "LEFT OUTER",        // NOI18N
                           "RIGHT OUTER"        // NOI18N
                           /*, "FULL OUTER" */  // Dropped b/c Pointbase doesn't support it
                           /*, "CROSS" */ }; 
        public String[] getTags() {
            return tags;
        }
    }

    public class ColumnPropertyEditor1 extends PropertyEditorSupport {

        public String[] getTags() {
            List<String> columnNames ;
            try {
                columnNames = _queryBuilder.getColumnNames(_table1);
            } catch(SQLException sqle) {
                return new String[0] ;
            }
            return columnNames.toArray(new String[0]);
            // return new String[] {"PERSONID", "NAME" };
        }
    }

    public class ColumnPropertyEditor2 extends PropertyEditorSupport {

        @Override
        public String[] getTags() {

           List<String> columnNames ;
            try {
                columnNames = _queryBuilder.getColumnNames(_table2);
            } catch (SQLException sqle) {
                return new String[0] ;
            }
            return columnNames.toArray(new String[0]);
        }
    }
}
