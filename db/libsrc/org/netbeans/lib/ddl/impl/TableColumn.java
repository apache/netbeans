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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.netbeans.lib.ddl.CheckConstraintDescriptor;
import org.netbeans.lib.ddl.DatabaseSpecification;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.TableColumnDescriptor;

/**
* Implementation of table column.
*/
public class TableColumn extends AbstractTableColumn implements Serializable, TableColumnDescriptor, CheckConstraintDescriptor {
    /** String constant for column type */
    public static final String COLUMN = "COLUMN"; // NOI18N
    /** String constant for column check */
    public static final String CHECK = "CHECK"; // NOI18N
    /** String constant for unique column type */
    public static final String UNIQUE = "UNIQUE"; // NOI18N
    /** String constant for primary key */
    public static final String PRIMARY_KEY = "PRIMARY_KEY"; // NOI18N
    /** String constant for foreign key */
    public static final String FOREIGN_KEY = "FOREIGN_KEY"; // NOI18N
    /** String constant for check constraint */
    public static final String CHECK_CONSTRAINT = "CHECK_CONSTRAINT"; // NOI18N
    /** String constant for unique constraint */
    public static final String UNIQUE_CONSTRAINT = "UNIQUE_CONSTRAINT"; // NOI18N
    /** String constant for primary key constraint */
    public static final String PRIMARY_KEY_CONSTRAINT = "PRIMARY_KEY_CONSTRAINT"; // NOI18N
    /** String constant for foreign key constraint */
    public static final String FOREIGN_KEY_CONSTRAINT = "FOREIGN_KEY_CONSTRAINT"; // NOI18N

    /** Column type */
    int type;

    /** Column size */
    int size;

    /** Column decimal size */
    int decsize;

    /** Null allowed */
    boolean nullable;

    /** Default value */
    String defval;

    /** Check expression */
    String checke;
    
    /** Table constraint columns */
    Vector constraintColumns;

    static final long serialVersionUID =4298150043758715392L;
    /** Constructor */
    public TableColumn() {
        size = 0;
        decsize = 0;
        nullable = true;
    }

    /** Returns type of column */
    public int getColumnType() {
        return type;
    }

    /** Sets type of column */
    public void setColumnType(int columnType) {
        type = columnType;
    }

    /** Returns column size */
    public int getColumnSize() {
        return size;
    }

    /** Sets size of column */
    public void setColumnSize(int csize) {
        size = csize;
    }

    /** Returns decimal digits of column */
    public int getDecimalSize() {
        return decsize;
    }

    /** Sets decimal digits of column */
    public void setDecimalSize(int dsize) {
        decsize = dsize;
    }

    /** Nulls allowed? */
    public boolean isNullAllowed() {
        return nullable;
    }

    /** Sets null property */
    public void setNullAllowed(boolean flag) {
        nullable = flag;
    }

    /** Returns default value of column */
    public String getDefaultValue() {
        return defval;
    }

    /** Sets default value of column */
    public void setDefaultValue(String val) {
        defval = val;
    }

    /** Returns column check condition */
    public String getCheckCondition() {
        return checke;
    }

    /** Sets column check condition */
    public void setCheckCondition(String val) {
        checke = val;
    }

    /** Returns table constraint columns */
    public Vector getTableConstraintColumns() {
        return constraintColumns;
    }

    /** Sets column check condition */
    public void setTableConstraintColumns(Vector columns) {
        constraintColumns = columns;
    }

    /**
    * Returns properties and it's values supported by this object.
    * object.name		Name of the object; use setObjectName() 
    * object.owner		Name of the object; use setObjectOwner() 
    * column.size		Size of column 
    * column.decsize	Deimal size of size 
    * column.type		Type of column 
    * default.value		Condition of column 
    * Throws DDLException if object name is not specified.
    */
    public Map getColumnProperties(AbstractCommand cmd) throws DDLException {
        DatabaseSpecification spec = cmd.getSpecification();
        Map args = super.getColumnProperties(cmd);
        String stype = spec.getType(type);
        Vector charactertypes = (Vector)spec.getProperties().get("CharacterTypes"); // NOI18N
        String strdelim = (String)spec.getProperties().get("StringDelimiter"); // NOI18N
        Vector sizelesstypes = (Vector)spec.getProperties().get("SizelessTypes"); // NOI18N
        String coldelim = (String)spec.getProperties().get("ArgumentListDelimiter"); // NOI18N

        // Decimal size for sizeless type
        if (sizelesstypes != null && size > 0) {
            if (!sizelesstypes.contains(stype)) {
                if (size > 0)
                    args.put("column.size", String.valueOf(size)); // NOI18N
                if (decsize > 0)
                    args.put("column.decsize", String.valueOf(decsize)); // NOI18N
            }
        }

        String qdefval = defval;

        if (qdefval != null && charactertypes.contains(spec.getType(type)) && !qdefval.startsWith(strdelim) && !qdefval.endsWith(strdelim))
            if (!qdefval.startsWith("(" + strdelim) && !qdefval.endsWith(strdelim + ")")) //hack for MSSQLServer, default value is encapsulated in () so I can't generate '()'
                qdefval = strdelim + defval + strdelim;
        
        String dbType = spec.getType(type);
        String dbTypeSuffix = null;
        Map suffixTypeMap = (Map)spec.getProperties().get("TypePrefixSuffixMap"); // NOI18N
        if (suffixTypeMap != null) {
            Map dbTypePrefixSuffix = (Map)suffixTypeMap.get(dbType);
            if (dbTypePrefixSuffix != null) {
                dbType = (String)dbTypePrefixSuffix.get("Prefix"); // NOI18N
                dbTypeSuffix = (String)dbTypePrefixSuffix.get("Suffix"); // NOI18N
            }
        }
        args.put("column.type", dbType);
        if (dbTypeSuffix != null) {
            args.put("column.type.suffix", dbTypeSuffix);
        }
        
        if (!nullable)
            args.put("column.notnull", ""); // NOI18N
        
        if (!(! nullable && qdefval != null && (qdefval.equalsIgnoreCase("null") || qdefval.equalsIgnoreCase("'null'") || qdefval.equalsIgnoreCase("\"null\"")))) // NOI18N
            if (defval != null && !defval.equals(""))
                args.put("default.value", qdefval); // NOI18N

        if (checke != null)
            args.put("check.condition", checke); // NOI18N
        if (constraintColumns != null) {
            String cols = "";
            Enumeration col_e = constraintColumns.elements();
            while (col_e.hasMoreElements()) {
                Object zrus = col_e.nextElement();
                Hashtable col = (Hashtable)zrus;
                boolean inscomma = col_e.hasMoreElements();
                String colname = (String)col.get("name");
                cols = cols + 
                    (isNewColumn() ? colname : cmd.quote(colname)) + 
                    (inscomma ? coldelim : "" ); //NOI18N
            }
            args.put("constraint.columns", cols); // NOI18N
        }
        return args;
    }

    /** Reads object from stream */
    public void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        super.readObject(in);
        type = in.readInt();
        size = in.readInt();
        decsize = in.readInt();
        nullable = in.readBoolean();
        defval = (String)in.readObject();
        checke = (String)in.readObject();
    }

    /** Writes object to stream */
    public void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        super.writeObject(out);
        out.writeInt(type);
        out.writeInt(size);
        out.writeInt(decsize);
        out.writeBoolean(nullable);
        out.writeObject(defval);
        out.writeObject(checke);
    }
}
