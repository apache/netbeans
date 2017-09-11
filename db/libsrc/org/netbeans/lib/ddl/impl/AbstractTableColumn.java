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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.util.CommandFormatter;

/**
* Default implementation of database column. It handles name, column name, it's
* format and type. If used, column can handle referenced table and column.
* User can add custom properties into format.
*/
public class AbstractTableColumn implements Serializable {
    /** Name and column name. */
    private String name, cname, format;

    /** Type, usually column, primary or foreign key */
    private String otype;

    /** Additional properties */
    private Map addprops;

    /** Referenced table */
    String reftab;

    /** Referenced column */
    String refcol;
    
    /** Is this a new column or an existing one */
    boolean newColumn = false;
    
    /** Is this a column for an existing object or one that is being
     * newly created?
     */
    boolean newObject = false;

    static final long serialVersionUID =-5128289937199572117L;
    /** Returns name of object */
    public String getObjectName()
    {
        return name;
    }

    /** Sets name of object */
    public void setObjectName(String oname)
    {
        name = oname;
    }

    /** Returns type of object */
    public String getObjectType()
    {
        return otype;
    }

    /** Sets name of column */
    public void setObjectType(String type)
    {
        otype = type;
    }

    /** Returns name of column */
    public String getColumnName()
    {
        return cname;
    }

    /** Sets name of column */
    public void setColumnName(String columnName)
    {
        cname = columnName;
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

    /** Returns referenced table */
    public String getReferencedTableName()
    {
        return reftab;
    }

    /** Sets referenced table */
    public void setReferencedTableName(String table)
    {
        reftab = table;
    }

    /** Returns referenced column name */
    public String getReferencedColumnName()
    {
        return refcol;
    }

    /** Sets referenced column name */
    public void setReferencedColumnName(String col)
    {
        refcol = col;
    }

    public boolean isNewColumn() {
        return newColumn;
    }

    public void setNewColumn(boolean newColumn) {
        this.newColumn = newColumn;
    }
    
    public boolean isNewObject() {
        return newObject;
    }
    
    public void setNewObject(boolean newObject) {
        this.newObject = newObject;
    }
    

    /** Returns custom property identified by name */
    public Object getProperty(String pname)
    {
        return addprops.get(pname);
    }

    /** Sets property identified by name */
    public void setProperty(String pname, Object pval)
    {
        if (addprops == null) addprops = new HashMap();
        addprops.put(pname, pval);
    }

    /** Returns colum properties.
    * It first copies all custom properties, then sets:
    * object.name		Name of the object
    * column.name		Name of the column
    * These properties are required; an DDLException will throw if you
    * forgot to set it up.
    * fkobject.name		Referenced object name
    * fkcolumn.name		Referenced column name
    */
    public Map getColumnProperties(AbstractCommand cmd)
    throws DDLException
    {
        HashMap args = new HashMap();
        String oname = getObjectName();
        String cname = getColumnName();

        if (addprops != null) args.putAll(addprops);
        if (oname != null) args.put("object.name", 
            newObject ? oname : cmd.quote(oname)); // NOI18N
        else throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_Unknown")); // NOI18N
        if (cname != null) args.put("column.name",
            newColumn ? cname : cmd.quote(cname)); // NOI18N
        else throw new DDLException(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_Unknown")); // NOI18N

        if (reftab != null) args.put("fkobject.name", cmd.quote(reftab)); // NOI18N
        if (refcol != null) args.put("fkcolumn.name", cmd.quote(refcol)); // NOI18N
        
        return args;
    }

    /**
    * Returns full string representation of column. This string needs no 
    * additional formatting. Throws DDLException if format is not specified 
    * or CommandFormatter can't format it (it uses MapFormat to process entire 
    * lines and can solve [] enclosed expressions as optional.
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

    /** Reads object from stream */
    public void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        name = (String)in.readObject();
        cname = (String)in.readObject();
        format = (String)in.readObject();
        otype = (String)in.readObject();
        addprops = (Map)in.readObject();
        reftab = (String)in.readObject();
        refcol = (String)in.readObject();
    }

    /** Writes object to stream */
    public void writeObject(java.io.ObjectOutputStream out)
    throws java.io.IOException
    {
        out.writeObject(name);
        out.writeObject(cname);
        out.writeObject(format);
        out.writeObject(otype);
        out.writeObject(addprops);
        out.writeObject(reftab);
        out.writeObject(refcol);
    }
}
