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

import java.util.HashMap;
import java.util.Map;

import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.util.CommandFormatter;

/**
* Describes trigger. Encapsulates name, timing (when it fires; when user INSERTs of
* some data, after UPDATE or DELETE). In trigger descriptor this values should be
* combined together.
*/
public class TriggerEvent {
    public static final int INSERT = 1;
    public static final int UPDATE = 2;
    public static final int DELETE = 3;

    /** Converts code into string representation */
    public static String getName(int code)
    {
        switch (code) {
        case INSERT: return "INSERT"; // NOI18N
        case UPDATE: return "UPDATE"; // NOI18N
        case DELETE: return "DELETE"; // NOI18N
        }

        return null;
    }

    /** Event */
    private String name;

    /** Column */
    private String col;

    /** Format */
    private String format;

    /** Returns name */
    public String getName()
    {
        return name;
    }

    /** Sets name */
    public void setName(String aname)
    {
        name = aname;
    }

    /** Returns name of column */
    public String getFormat()
    {
        return format;
    }

    /** Sets name of column */
    public void setFormat(String fmt)
    {
        format = fmt;
    }

    /** Returns name of column */
    public String getColumn()
    {
        return col;
    }

    /** Sets name of column */
    public void setColumn(String column)
    {
        col = column;
    }

    /**
    * Returns properties and it's values supported by this object.
    * event.name	Name of event 
    * event.column	Name of column 
    * Throws DDLException if object name is not specified.
    */
    public Map getColumnProperties(AbstractCommand cmd) throws DDLException {
        HashMap args = new HashMap();
        args.put("event.name", cmd.quote(name)); // NOI18N
        args.put("event.column", cmd.quote(col)); // NOI18N
        
        return args;
    }

    /** Returns string representation of event
    * @param cmd Command context
    */
    public String getCommand(AbstractCommand cmd)
    throws DDLException
    {
        Map cprops;
        if (format == null) throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_NoFormatSpec")); //NOI18N
        try {
            cprops = getColumnProperties(cmd);
            return CommandFormatter.format(format, cprops);
        } catch (Exception e) {
            throw new DDLException(e.getMessage());
        }
    }
}
