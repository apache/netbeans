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

package org.netbeans.lib.ddl.impl;

import java.text.MessageFormat;
import java.util.Map;

import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.DDLException;

/**
* Instances of this command operates with one column.
*
* @author Slavek Psenicka
*/

public class ColumnCommand extends AbstractCommand
{
    /** Column */
    private TableColumn column;

    static final long serialVersionUID =-4554975764392047624L;
    /** Creates specification of command
    * @param type Type of column
    * @param name Name of column
    * @param cmd Command
    * @param newObject set to true if this column is for a new object (table)
    *   and set to false if this column is for an existing object (table)
    * @param newColumn set to true if this is a new column, false if this
    *   is an existing column.
    */	
    public TableColumn specifyColumn(String type, String name, String cmd,
        boolean newObject, boolean newColumn)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        Map gprops = (Map)getSpecification().getProperties();
        Map props = (Map)getSpecification().getCommandProperties(cmd);
        Map bindmap = (Map)props.get("Binding"); // NOI18N
        String tname = (String)bindmap.get(type);
        if (tname != null) {
            Map typemap = (Map)gprops.get(tname);
            if (typemap == null) throw new InstantiationException(
                                                MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableLocateObject"), // NOI18N
                                                    new String[] {tname}));
            Class typeclass = Class.forName((String)typemap.get("Class")); // NOI18N
            String format = (String)typemap.get("Format"); // NOI18N
            column = (TableColumn)typeclass.newInstance();
            column.setObjectName(name);
            column.setObjectType(type);
            column.setColumnName(name);
            column.setFormat(format);
            column.setNewObject(newObject);
            column.setNewColumn(newColumn);
        } else throw new InstantiationException(MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableLocateType"), // NOI18N
                                                    new String[] {type, bindmap.toString() }));

        return column;
    }

    public TableColumn getColumn()
    {
        return column;
    }

    public void setColumn(TableColumn col)
    {
        column = col;
    }

    /**
    * Returns properties and it's values supported by this object.
    * column	Specification of the column 
    */
    public Map getCommandProperties()
    throws DDLException
    {
        Map args = super.getCommandProperties();
        args.put("column", column.getCommand(this)); // NOI18N
        return args;
    }
}
