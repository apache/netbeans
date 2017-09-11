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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
                ProcedureArgument arg = (ProcedureArgument)typeclass.newInstance();
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
