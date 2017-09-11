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

import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.Argument;
import org.netbeans.lib.ddl.DatabaseSpecification;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.util.CommandFormatter;

/**
* Argument of procedure. Encapsulates name, type (in/out) and datatype.
*/
public class ProcedureArgument implements Argument {
    /** Argument name */
    private String name;

    /** Argument type */
    private int type;

    /** Argument datatype */
    private int dtype;

    /** Format */
    private String format;

    /** Additional properties */
    private Map addprops;

    public static String getArgumentTypeName(int type)
    {
        String typename = null;
        switch (type) {
        case java.sql.DatabaseMetaData.procedureColumnIn: typename = "IN"; break; // NOI18N
        case java.sql.DatabaseMetaData.procedureColumnOut: typename = "OUT"; break; // NOI18N
        case java.sql.DatabaseMetaData.procedureColumnInOut: typename = "INOUT"; break; // NOI18N
        }

        return typename;
    }

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

    /** Returns general property */
    public Object getProperty(String pname)
    {
        return addprops.get(pname);
    }

    /** Sets general property */
    public void setProperty(String pname, Object pval)
    {
        if (addprops == null) addprops = new HashMap();
        addprops.put(pname, pval);
    }

    /** Describes type of argument: in, out, in/out or return value
    * of procedure. Particular values you can find in DatabaseMetadata;
    */
    public int getType()
    {
        return type;
    }

    /** Translates numeric representation of type into IN/OUT/INOUT strings.
    */
    public String getTypeName()
    {
        return getArgumentTypeName(type);
    }

    /** Sets type of argument */
    public void setType(int atype)
    {
        type = atype;
    }

    /** Returns datatype of argument */
    public int getDataType()
    {
        return dtype;
    }

    /** Sets datatype of argument */
    public void setDataType(int atype)
    {
        dtype = atype;
    }

    /**
    * Returns properties and it's values supported by this object.
    * argument.name		Name of argument
    * argument.type		Type of argument 
    * argument.datatype	Datatype of argument
    * Throws DDLException if object name is not specified.
    */
    public Map getColumnProperties(AbstractCommand cmd) throws DDLException {
        HashMap args = new HashMap();
        DatabaseSpecification spec = cmd.getSpecification();
        Map typemap = (Map)spec.getProperties().get("ProcedureArgumentMap"); // NOI18N
        String typename = (String)typemap.get(getArgumentTypeName(type));
        args.put("argument.name", cmd.quote(name)); // NOI18N
        args.put("argument.type", typename); // NOI18N
        args.put("argument.datatype", spec.getType(dtype)); // NOI18N
        return args;
    }

    /**
    * Returns full string representation of argument.
    */
    public String getCommand(CreateProcedure cmd)
    throws DDLException
    {
        Map cprops;
        if (format == null) throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_NoFormatSpec")); // NOI18N
        try {
            cprops = getColumnProperties(cmd);
            return CommandFormatter.format(format, cprops);
        } catch (Exception e) {
            throw new DDLException(e.getMessage());
        }
    }
}
