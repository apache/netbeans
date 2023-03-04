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
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.DDLException;

/**
* Instances of this command operates with column list (e.g. create column).
* To process command with one column use ColumnCommand.
*
* @author Slavek Psenicka
*/

public class ColumnListCommand extends AbstractCommand
{
    /** Used columns */
    private Vector columns;

    static final long serialVersionUID =3646663278680222131L;
    /** Constructor */
    public ColumnListCommand()
    {
        columns = new Vector();
    }

    /** Returns list of columns */
    public Vector getColumns()
    {
        return columns;
    }

    /** Creates specification of command
    * @param type Type of column
    * @param name Name of column
    * @param cmd Command
    * @param newObject indicates whether the object for this column
    *   (e.g. a table) is an existing object or a new object.  This affects
    *   whether we quote the object or not.
    * @param newColumn indicates whether this column refers to a column
     *   being created as part of this command or it is an existing column
    */	
    public TableColumn specifyColumn(String type, String name, String cmd,
        boolean newObject, boolean newColumn)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        TableColumn column;
        Map gprops = (Map)getSpecification().getProperties();
        Map props = (Map)getSpecification().getCommandProperties(cmd);
        Map bindmap = (Map)props.get("Binding"); // NOI18N
        String tname = (String)bindmap.get(type);
        if (tname != null) {
            Map typemap = (Map)gprops.get(tname);
            if (typemap != null) {
                Class typeclass = Class.forName((String)typemap.get("Class")); // NOI18N
                String format = (String)typemap.get("Format"); // NOI18N
                column = (TableColumn)typeclass.newInstance();
                column.setObjectName(name);
                column.setObjectType(type);
                column.setColumnName(name);
                column.setFormat(format);
                column.setNewObject(newObject);
                column.setNewColumn(newColumn);
                columns.add(column);
            } else throw new InstantiationException(
                    MessageFormat.format(
                        NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableLocateType"), // NOI18N
                        new String[] {tname, props.keySet().toString() } ));
        } else throw new InstantiationException(
                    MessageFormat.format(
                        NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableToBind"), // NOI18N
                        new String[] {type, bindmap.toString() } ));
        return column;
    }

    /**
    * Returns properties and it's values supported by this object.
    * columns	Specification of columns served by this object
    */
    public Map getCommandProperties()
    throws DDLException
    {
        Map props = (Map)getSpecification().getProperties();
        String cols = (String)props.get("ColumnListHeader"); // NOI18N
        String coldelim = (String)props.get("ColumnListDelimiter"); // NOI18N
        Map args = super.getCommandProperties();

        // Construct string

        Enumeration col_e = columns.elements();
        while (col_e.hasMoreElements()) {
            AbstractTableColumn col = (AbstractTableColumn)col_e.nextElement();
            boolean inscomma = col_e.hasMoreElements();
            cols = cols + col.getCommand(this)+(inscomma ? coldelim : "");
        }

        args.put("columns", cols); // NOI18N
        return args;
    }

    /** Reads object from stream */
    public void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        super.readObject(in);
        columns = (Vector)in.readObject();
    }

    /** Writes object to stream */
    public void writeObject(java.io.ObjectOutputStream out)
    throws java.io.IOException
    {
        super.writeObject(out);
        out.writeObject(columns);
    }
}
