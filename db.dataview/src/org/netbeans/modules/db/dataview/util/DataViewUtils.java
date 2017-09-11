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
package org.netbeans.modules.db.dataview.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBForeignKey;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.output.SQLConstant;
import org.openide.util.NbBundle;

/**
 * Utility class supplying lookup and conversion methods for SQL-related tasks.
 * 
 * @author Ahimanikya Satapathy
 */
public class DataViewUtils {

    public static boolean isNumeric(int jdbcType) {
        switch (jdbcType) {
            case Types.BIT:
            case Types.BIGINT:
            case Types.BOOLEAN:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.DECIMAL:
            case Types.NUMERIC:
                return true;

            default:
                return false;
        }
    }

    public static boolean isPrecisionRequired(int jdbcType, boolean isdb2) {
        if (isdb2 && jdbcType == Types.BLOB || jdbcType == Types.CLOB) {
            return true;
        } else {
            return isPrecisionRequired(jdbcType);
        }
    }

    public static boolean isPrecisionRequired(int jdbcType) {
        switch (jdbcType) {
            case Types.BIGINT:
            case Types.BOOLEAN:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.JAVA_OBJECT:
            case Types.LONGVARCHAR:
            case Types.LONGVARBINARY:
            case Types.BLOB:
            case Types.CLOB:
            case Types.ARRAY:
            case Types.STRUCT:
            case Types.DISTINCT:
            case Types.REF:
            case Types.DATALINK:
                return false;

            default:
                return true;
        }
    }

    public static boolean isScaleRequired(int type) {
        switch (type) {
            case java.sql.Types.DECIMAL:
            case java.sql.Types.NUMERIC:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBinary(int jdbcType) {
        switch (jdbcType) {
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return true;
            default:
                return false;
        }
    }

    public static boolean isString(int jdbcType) {
        switch (jdbcType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case -9:  //NVARCHAR
            case -8:  //ROWID
            case -15: //NCHAR
            case -16: //NLONGVARCHAR
                return true;
            default:
                return false;
        }
    }

    // NULL, DEFAULT, CURRENT_TIMESTAMP etc.
    @Deprecated
    public static boolean isSQLConstantString(Object value) {
        return isSQLConstantString(value, null);
    }

    // NULL, DEFAULT, CURRENT_TIMESTAMP etc.
    public static boolean isSQLConstantString(Object value, DBColumn col) {
        return (value == null || value instanceof SQLConstant);
    }

    public static boolean isNullString(String str) {
        return (str == null || str.trim().length() == 0);
    }

    public static void closeResources(PreparedStatement pstmt) {
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException ex) {
            //ignore
        }
    }

    public static void closeResources(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException sqex) {
                // ignore
            }
        }
    }

    public static void closeResources(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException sqex) {
                // ignore
            }
        }
    }

    public static String getColumnToolTip(DBColumn column) {
        boolean pk = column.isPrimaryKey();
        boolean fk = column.isForeignKey();
        boolean isNullable = column.isNullable();
        boolean generated = column.isGenerated();
        StringBuilder strBuf = new StringBuilder("<html> " +
                "<table border=0 cellspacing=0 cellpadding=0 >");

        strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_name")).append("</td> <td> &nbsp; : &nbsp; <b>");
        strBuf.append(column.getName()).append("</b> </td> </tr>");

        strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_type")).append("</td> <td> &nbsp; : &nbsp; <b>");
        strBuf.append(column.getTypeName()).append("</b> </td> </tr>");

        if (isString(column.getJdbcType())) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_length")).append("</td> <td> &nbsp; : &nbsp; <b>");
        } else {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_precision")).append("</td> <td> &nbsp; : &nbsp; <b>");
        }
        strBuf.append(column.getPrecision()).append("</b> </td> </tr>");

        if (isScaleRequired(column.getJdbcType())) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_scale")).append("</td> <td> &nbsp; : &nbsp; <b>");
            strBuf.append(column.getScale()).append("</b> </td> </tr>");
        }

        if (pk) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_PK")).append("</td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr>");
        }
        if (fk) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_FK")).append("</td> <td> &nbsp; : &nbsp; <b>" + getForeignKeyString(column)).append("</b>").append("</td> </tr>");
        }

        if (!isNullable) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_nullable")).append("</td> <td> &nbsp; : &nbsp; <b> No </b> </td> </tr>");
        }

        if (generated) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_generated")).append("</td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr>");
        }

        if (column.hasDefault()) {
            strBuf.append("<tr> <td>&nbsp;").append(NbBundle.getMessage(DataViewUtils.class, "TOOLTIP_column_default")).append("</td> <td> &nbsp; : &nbsp; <b>").append(column.getDefaultValue()).append("</b> </td> </tr>");
        }

        strBuf.append("</table> </html>");
        return strBuf.toString();
    }

    public static String getForeignKeyString(DBColumn column) {
        String refString = column.getName() + " --> "; // NOI18N
        StringBuilder str = new StringBuilder(refString);
        DBTable table = column.getParentObject();
        boolean firstReferencedColumn = false;

        for(DBForeignKey fk: table.getForeignKeys()) {
            if (fk.contains(column)) {
                for(String pkColName: fk.getPKColumnNames()) {
                    str.append(pkColName);
                    if (firstReferencedColumn) {
                        firstReferencedColumn = false;
                    } else {
                        str.append(", "); // NOI18N
                    }
                }
            }
        }

        return str.toString();
    }
    public static final String[] HTML_ALLOWABLES = {"&amp;", "&quot;", "&lt;", "&gt;"}; // NOI18N
    public static final String[] HTML_ILLEGALS = {"&", "\"", "<", ">"}; // NOI18N

    public static String escapeHTML(String string) {
        return replaceInString(string, HTML_ILLEGALS, HTML_ALLOWABLES);
    }

    public static String replaceInString(String originalString, String[] victims, String[] replacements) {
        StringBuilder resultBuffer = new StringBuilder();
        boolean bReplaced = false;

        for (int charPosition = 0; charPosition < originalString.length(); charPosition++) {
            for (int nSelected = 0; !bReplaced && (nSelected < victims.length); nSelected++) {
                if (originalString.startsWith(victims[nSelected], charPosition)) {
                    resultBuffer.append(replacements[nSelected]);
                    bReplaced = true;
                    charPosition += victims[nSelected].length() - 1;
                }
            }

            if (!bReplaced) {
                resultBuffer.append(originalString.charAt(charPosition));
            } else {
                bReplaced = false;
            }
        }
        return resultBuffer.toString();
    }

    private DataViewUtils() {
    }
}
