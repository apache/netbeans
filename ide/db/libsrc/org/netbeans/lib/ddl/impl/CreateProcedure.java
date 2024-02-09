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

package org.netbeans.lib.ddl.impl;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.Argument;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.ProcedureDescriptor;

/**
* Interface of database action command. Instances should remember connection
* information of DatabaseSpecification and use it in execute() method. This is a base interface
* used heavily for sub-interfacing (it is not subclassing :)
*
* @author Slavek Psenicka
*/

public class CreateProcedure extends AbstractCommand implements ProcedureDescriptor
{
    /** Catalog */
    private String cat;

    /** Body of the procedure */
    private String body;

    /** Arguments */
    private Vector args;

    static final long serialVersionUID =1316633286943440734L;
    public CreateProcedure()
    {
        args = new Vector();
        setNewObject(true);
    }

    /** Returns catalog */
    public String getCatalog()
    {
        return cat;
    }

    /** Sets catalog */
    public void setCatalog(String cname)
    {
        cat = cname;
    }

    /** Returns text of procedure */
    public String getText()
    {
        return body;
    }

    /** Sets name of table */
    public void setText(String text)
    {
        body = text;
    }

    /** Returns arguments */
    public Vector getArguments()
    {
        return args;
    }

    public Argument getArgument(int index)
    {
        return (Argument)args.get(index);
    }

    /** Sets argument array */
    public void setArguments(Vector argarr)
    {
        args = argarr;
    }

    public void setArgument(int index, Argument arg)
    {
        args.set(index, arg);
    }

    public Argument createArgument(String name, int type, int datatype)
    throws DDLException
    {
        try {
            Map gprops = (Map)getSpecification().getProperties();
            Map props = (Map)getSpecification().getCommandProperties(Specification.CREATE_PROCEDURE);
            Map bindmap = (Map)props.get("Binding"); // NOI18N
            String tname = (String)bindmap.get("ARGUMENT"); // NOI18N
            if (tname != null) {
                Map typemap = (Map)gprops.get(tname);
                if (typemap == null) throw new InstantiationException(
                    MessageFormat.format(
                        NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableLocateObject"), // NOI18N
                        new String[] {tname}));
                Class typeclass = Class.forName((String)typemap.get("Class")); // NOI18N
                String format = (String)typemap.get("Format"); // NOI18N
                ProcedureArgument arg = (ProcedureArgument)typeclass.getDeclaredConstructor().newInstance();
                arg.setName(name);
                arg.setType(type);
                arg.setDataType(datatype);
                arg.setFormat(format);
                return (Argument)arg;
            } else throw new InstantiationException(
                        MessageFormat.format(
                            NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableLocateType"), // NOI18N
                            new String[] {String.valueOf(type), bindmap.toString() }));
        } catch (Exception e) {
            throw new DDLException(e.getMessage());
        }
    }

    public void addArgument(String name, int type, int datatype)
    throws DDLException
    {
        Argument arg = createArgument(name, type, datatype);
        if (arg != null) args.add(arg);
    }

    public Map getCommandProperties()
    throws DDLException
    {
        Map props = (Map)getSpecification().getProperties();
        String cols = "", argdelim = (String)props.get("ArgumentListDelimiter"); // NOI18N
        Map cmdprops = super.getCommandProperties();

        Enumeration col_e = args.elements();
        while (col_e.hasMoreElements()) {
            ProcedureArgument arg = (ProcedureArgument)col_e.nextElement();
            boolean inscomma = col_e.hasMoreElements();
            cols = cols + arg.getCommand(this)+(inscomma ? argdelim : "");
        }

        cmdprops.put("arguments", cols); // NOI18N
        cmdprops.put("body", body); // NOI18N
        return cmdprops;
    }
}
