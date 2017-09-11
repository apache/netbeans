/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.sql.SQLException;
import java.util.List;

import java.beans.PropertyEditorSupport;
import java.beans.PropertyEditor;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.Reflection;

import org.openide.util.NbBundle;

import org.netbeans.modules.db.sql.visualeditor.querymodel.JoinTable;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Predicate;

import org.openide.util.Exceptions;

// A class for providing properties to arcs (joins)
public class JoinNode extends AbstractNode
{
    // Private variable

    private String              _table1="";  // NOI18N
    private String              _column1="";  // NOI18N
    private String              _table2="";  // NOI18N
    private String              _column2="";  // NOI18N
    private String              _type="";  // NOI18N
    private QueryBuilder        _queryBuilder;


    // Constructors

    JoinNode(String table1, String column1, String table2, String column2, String type,
             QueryBuilder queryBuilder)
    {
        super(Children.LEAF);
        _table1 = table1;
        _column1 = column1;
        _table2 = table2;
        _column2 = column2;
        _type = type;
        _queryBuilder = queryBuilder;
    }

    JoinNode(String table1, String column1, String table2, String column2, QueryBuilder queryBuilder)
    {
        this(table1, column1, table2, column2, "INNER", queryBuilder);  // NOI18N
    }

    // Accessors/mutators

    public String getTable1() {
        return _table1;
    }

    public String getColumn1() {
        return _column1;
    }

    public void setColumn1(String column1) {
        JoinTable jt = findJoinTable(_table1, _column1, _table2, _column2);
        _column1 = column1;
        updateModel(jt);
    }

    public String getTable2() {
        return _table2;
    }

    public String getColumn2() {
        return _column2;
    }

    public void setColumn2(String column2) {
        JoinTable jt = findJoinTable(_table1, _column1, _table2, _column2);
        _column2 = column2;
        updateModel(jt);
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        JoinTable jt = findJoinTable(_table1, _column1, _table2, _column2);
        _type = type;
        updateModel(jt);
    }

    // Create minimal property sheet
    // Join type and column names are editable, table names aren't

    protected Sheet createSheet() {
        Sheet s = Sheet.createDefault();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        try {
            PropertySupport.Reflection p;
            p = new Reflection(this, String.class, "getTable1", null); // NOI18N
            p.setName("table1"); // NOI18N
//            p.setDisplayName("Table 1"); // NOI18N
//            p.setShortDescription("first table for join"); // NOI18N

            String table1 = NbBundle.getMessage(JoinNode.class, "TABLE_1");    // NOI18N
            p.setDisplayName(table1);

            String table1ShortDescription = NbBundle.getMessage(JoinNode.class, "TABLE_1_SHORT_DESCRIPTION");    // NOI18N
            p.setShortDescription(table1ShortDescription);
            ss.put(p);
            p = new Reflection(this, String.class, "getColumn1", "setColumn1") {
                    public PropertyEditor getPropertyEditor () {
                        return new ColumnPropertyEditor1 ();
                    }}; // NOI18N
            p.setName("column1"); // NOI18N
//            p.setDisplayName("Column 1"); // NOI18N
//            p.setShortDescription("First column for join"); // NOI18N

            String column1 = NbBundle.getMessage(JoinNode.class, "COLUMN_1");    // NOI18N
            p.setDisplayName(column1);

            String column1ShortDescription = NbBundle.getMessage(JoinNode.class, "COLUMN_1_SHORT_DESCRIPTION");    // NOI18N
            p.setShortDescription(column1ShortDescription); 
//          p.setPropertyEditorClass(ColumnPropertyEditor.class);
            ss.put(p);
            p = new Reflection(this, String.class, "getTable2", null); // NOI18N
            p.setName("table2"); // NOI18N
//            p.setDisplayName("Table 2"); // NOI18N
//            p.setShortDescription("second table for join"); // NOI18N
            String table2 = NbBundle.getMessage(JoinNode.class, "TABLE_2");    // NOI18N
            p.setDisplayName(table2);

            String table2ShortDescription = NbBundle.getMessage(JoinNode.class, "TABLE_2_SHORT_DESCRIPTION");    // NOI18N
            p.setShortDescription(table2ShortDescription); 
            ss.put(p);
            p = new Reflection(this, String.class, "getColumn2", "setColumn2") {
                    public PropertyEditor getPropertyEditor () {
                        return new ColumnPropertyEditor2 ();
                    }}; // NOI18N
            p.setName("column2"); // NOI18N
//            p.setDisplayName("Column 2"); // NOI18N
//            p.setShortDescription("Second column for join"); // NOI18N

            String column2 = NbBundle.getMessage(JoinNode.class, "COLUMN_2");    // NOI18N
            p.setDisplayName(column2);

            String column2ShortDescription = NbBundle.getMessage(JoinNode.class, "COLUMN_2_SHORT_DESCRIPTION");    // NOI18N
            p.setShortDescription(column2ShortDescription); 
//          p.setPropertyEditorClass(ColumnPropertyEditor.class);
            ss.put(p);
            p = new Reflection(this, String.class, "getType", "setType") {      // NOI18N
                    public PropertyEditor getPropertyEditor () {
                        return new JoinTypePropertyEditor ();
                    }}; // NOI18N
            p.setName("type"); // NOI18N
//            p.setDisplayName("Type"); // NOI18N
//            p.setShortDescription("Type of join"); // NOI18N
            String type = NbBundle.getMessage(JoinNode.class, "TYPE");    // NOI18N
            p.setDisplayName(type);

            String typeShortDescription = NbBundle.getMessage(JoinNode.class, "TYPE_SHORT_DESCRIPTION");    // NOI18N
            p.setShortDescription(typeShortDescription); 

//          p.setPropertyEditorClass(JoinTypePropertyEditor.class);
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
    JoinTable findJoinTable(String _table1, String _column1, String _table2, String _column2) {
        return _queryBuilder._queryModel.findJoinTable(_table1, _column1, _table2, _column2);
    }


    private void updateModel(JoinTable jt) {

        // Set the appropriate fields in this Join Expression
        // Update the join type in the Join Table, and the fields of the condition
        jt.setJoinType(_type);
        if (_type.equals("CROSS"))  // NOI18N
            jt.setExpression(null);
        else
            ((Predicate)jt.getExpression()).setFields(_table1, _column1, _table2, _column2);

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
            List columnNames ;
            try {
                columnNames = _queryBuilder.getColumnNames(_table1);
            } catch(SQLException sqle) {
                return new String[0] ;
            }
            return (String[])columnNames.toArray(new String[columnNames.size()]);
        }
    }

    public class ColumnPropertyEditor2 extends PropertyEditorSupport {

        public String[] getTags() {

            List columnNames ;
            try {
                columnNames = _queryBuilder.getColumnNames(_table2);
            } catch(SQLException sqle) {
                return new String[0] ;
            }
            return (String[])columnNames.toArray(new String[columnNames.size()]);
        }
    }
}
