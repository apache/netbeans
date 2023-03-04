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
package org.netbeans.modules.db.sql.visualeditor.querybuilder;


import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.Reflection;

import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.modules.db.sql.visualeditor.Log;
import org.openide.util.Exceptions;

/**
 * Provides a place to hang properties for a node (table) in the query graph.
 * <p>
 *
 * @author  Jim Davidson
 */

public class TableNode extends AbstractNode
{
    // Private variable
    private int                         SQL_IDENTIFIER_LENGTH = 32;

    private boolean                     DEBUG = false;
    private String                      _fullTableName=null;
    private String                      _corrName=null;
    private QueryBuilder                _queryBuilder;

    // Constructor

    TableNode(String fullTableName)
    {
        super(Children.LEAF);
        _fullTableName = fullTableName;
    }

    TableNode(String fullTableName, String corrName, QueryBuilder queryBuilder)
    {
        super(Children.LEAF);
        _fullTableName = fullTableName;
        _corrName = corrName;
        _queryBuilder = queryBuilder;
    }

    // Accessors/mutators

    public String getTableName() {
        return _fullTableName;
    }

    public String getCorrName() {
        return _corrName;
    }

    public void setCorrName(String corrName) {

	Log.getLogger().entering("TableNode", "setCorrName",  corrName); // NOI18N

        // Note the old value
        String oldCorrName = getCorrName();
        String oldTableSpec = (oldCorrName==null) ? getTableName() : oldCorrName;

        // Sometimes we are called when the user has not made any changes.  Just return
        if ( ((corrName == null) && (oldCorrName==null)) ||
             ((corrName !=null) && (corrName.equals(oldCorrName))))
            return;

        // Save the new value
        if (corrName.trim().length()==0)  {
            _corrName=null;
        }
        else {
            // Modify the corrName if necessary, to ensure that it's unique
            // Addresses 5005528 Setting same alias for two tables produces incorrect query.
            // A return value of null means the name was already unique
            if ( ! isAliasValid ( corrName.trim() ) ) {
                // display an error message "Invalid alias name"
                // return without changing anything.
                String msg = NbBundle.getMessage(TableNode.class, "INVALID_ALIAS");    // NOI18N
                NotifyDescriptor d = new NotifyDescriptor.Message(msg + "\n\n" +corrName, NotifyDescriptor.ERROR_MESSAGE); // NOI18N
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            String tmp = _queryBuilder._queryModel.genUniqueName(corrName);
            _corrName= (tmp==null) ? corrName : tmp;
        }

        // Update the entire model
        _queryBuilder._queryModel.renameTableSpec(oldTableSpec, _corrName);
        // _queryBuilder.setTableColumnCorrName(oldTableSpec, _corrName);

        // Clear all panes and generate fresh
        _queryBuilder.generate();
    }

    // Create minimal property sheet

    protected Sheet createSheet() {
        Sheet s = Sheet.createDefault();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        try {
            PropertySupport.Reflection p;
            p = new Reflection(this, String.class, "getTableName", null); // NOI18N
            p.setName("tableName"); // NOI18N
            String tableDisplayName = NbBundle.getMessage(TableNode.class, "TABLE_DISPLAY_NAME");    // NOI18N
            // p.setDisplayName("Table Name"); // NOI18N
            p.setDisplayName(tableDisplayName);

            String tableShortDescription = NbBundle.getMessage(TableNode.class, "TABLE_SHORT_DESCRIPTION");    // NOI18N
            // p.setShortDescription("Table name"); // NOI18N
            p.setShortDescription(tableShortDescription); 
            ss.put(p);
            p = new Reflection(this, String.class, "getCorrName", "setCorrName"); // NOI18N
            p.setName("aliasName"); // NOI18N
            String aliasDisplayName = NbBundle.getMessage(TableNode.class, "ALIAS_DISPLAY_NAME");    // NOI18N
            // p.setDisplayName("Table Alias"); // NOI18N
            p.setDisplayName(aliasDisplayName); 
            String aliasShortDescription = NbBundle.getMessage(TableNode.class, "ALIAS_SHORT_DESCRIPTION");    // NOI18N
            // p.setShortDescription("Alias name for the table"); // NOI18N
            p.setShortDescription(aliasShortDescription); 
            ss.put(p);
        } catch (NoSuchMethodException nsme) {
            Exceptions.printStackTrace(nsme);
        }
        return s;
    }

    public boolean isAliasValid ( String aliasName ) {
        // As per the SQL 92, 
        // SQL syntax requires users to supply names for elements such as 
        // tables, aliases, views, cursors, and columns when they define 
        // them. SQL statements must use those names to refer to the table, 
        // view, or other element. 
        //
        // The maximum length for SQL identifiers is 32 characters.

        // There are two types of SQL identifiers:
        //
        // * Conventional identifiers
        //   Conventional SQL identifiers must:
        //    * Begin with an uppercase or lowercase letter.
        //    * Contain only letters, digits, or the underscore character ( _ ).
        //    * Not be reserved words.
        //    * Use ASCII characters only.
        //    * SQL does not distinguish between uppercase and lowercase 
        //      letters in SQL identifiers. It converts all names specified 
        //      as conventional identifiers to uppercase, but statements 
        //      can refer to the names in mixed case.
        //
        // * Delimited identifiers enclosed in double quotation marks
        //    * Delimited identifiers are strings of no more than 32 ASCII 
        //      characters enclosed in double quotation marks ( " " ). 
        //      Enclosing a name in double quotation marks preserves the 
        //      case of the name and allows it to be a reserved word or to 
        //      contain special characters. Special characters are any 
        //      characters other than letters, digits, or the underscore 
        //      character. Subsequent references to a delimited identifier 
        //      must also use enclosing double quotation marks. To include 
        //      a double quotation mark character in a delimited identifier, 
        //      precede it with another double quotation mark.
        //

        if ( aliasName.startsWith("\"") &&  aliasName.endsWith("\"")  ) {    // NOI18N
            return  isValidDelimitedIdentifier ( aliasName.substring (1, (aliasName.length()-1) ) ) ;
        }
        else {
            return  isValidConventionalIdentifier ( aliasName ) ;
        }
    }

    boolean isValidDelimitedIdentifier ( String identifier ) {
        if ( identifier.length() > SQL_IDENTIFIER_LENGTH )
            return false;

        return true;
    }

    boolean  isValidConventionalIdentifier ( String identifier ) {

        if ( identifier.length() > SQL_IDENTIFIER_LENGTH )
            return false;

        char[] charArray = identifier.toCharArray();

        // has to begin with uppercase or lower case letter
        if (! Character.isLetter ( charArray[0] ) ) {
            if (DEBUG)
                System.out.println("isValidConventionalIdentifier called. charArray[0] = " + charArray[0] + "\n" ); // NOI18N
            return false;
        }

        for ( int i=1; i<charArray.length; i++ ) {
            // Contain only letters, digits, or the underscore character ( _ ).
            if ( ( ! Character.isLetter ( charArray [i] ) ) && 
                 ( ! Character.isDigit ( charArray [i] ) ) &&
                 ( charArray [i] != '_' ) ) {
                return false;
            }
        }
        return true;
    }
}
