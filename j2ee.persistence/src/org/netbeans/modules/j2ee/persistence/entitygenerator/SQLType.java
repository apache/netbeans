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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.util.*;
import org.netbeans.modules.dbschema.ColumnElement;

/**
 * provides class wrapper for java.sql.Types.
 * @see java.sql.Types
 * @author Christopher Webster
 */
class SQLType {
    private int sqlType;
    private String stringValue;
    /* list of classes that could be used to represent this sql type in Java */
    private Class[] typeList;
    private boolean supportsFinder;
    private static final SQLType UNKNOWN = new SQLType(Types.OTHER, 
                                                       "UNKNOWN", // NOI18N
                                                       new Class[0], false);
        
    private SQLType(int type, String value, Class[] tList, boolean supportsFinder) {
        sqlType = type;
        stringValue = value;
        typeList = tList;
        this.supportsFinder = supportsFinder;
    }

   /** 
    * Override Object.equals
    */
    public boolean equals(Object other) {
        if (other == null || !getClass().isInstance(other)){
	    return false;
        }  
	return ((SQLType)other).sqlType == sqlType;
    }

    /**
     * Override Object.hashCode
     */
    public int hashCode() {
        return sqlType;
    }
  
    /**
     * Provide string representation of sql type. For example, 
     * java.sql.Types.BIT returns BIT.
     */
    public String toString() {
        return stringValue;
    }
        
    /**
     * provide original sql type
     * @return sql type from java.sql.Types
     */
    public int sqlType() {
        return sqlType;
    }    
    
    /**
     * Return the string value associated with this sql type
     * @return String value used in the method signature 
     */
    String getMemberType(ColumnElement element) {
        String memberType = java.io.Serializable.class.getName();
        Class memberTypeClass = null;
        
        if (element.isCharacterType()) {
            memberTypeClass = getClassForCharType(element.getLength(), 
                    element.isNullable());
        } else if (element.isNumericType()) {
            memberTypeClass = getClassForNumericType(element.getPrecision(), 
                    element.getScale(), element.isNullable());
        }

        if (memberTypeClass != null) {
            return memberTypeClass.getName();
        }

        if (typeList.length > 0) {
            memberType = typeList[0].getName();
        }
        
        /* Alters the name in the case of byte arrays */
        if (memberType.equals("[B")) { // NOI18N
            memberType = "byte []"; // NOI18N
        }
        return memberType;
    }
    
    public String getFirstNonPrimitiveType() {
        String typeName = null;
        for (Class clazz : typeList) {
            if (!clazz.isPrimitive()) {
                typeName = clazz.getName();
                break;
            }
        }
        /* Alters the name in the case of byte arrays */
        if ("[B".equals(typeName)) { // NOI18N
            typeName = "byte []"; // NOI18N
        }
        return typeName;
    }

    // returns the relevant Class from the typeList if it is dependent on
    // length, <code>null</code> if the rules in the no-arg getMemberType 
    // method are sufficient
    private Class getClassForCharType (Integer length, boolean isNullable) {
        switch (sqlType) {
            case Types.CHAR:
                if ((length != null) && (length.intValue() == 1)) {
                     return typeList[1];//typeList[isNullable ? 1 : 2] is replaced with String always because of issue #195674 (openjpa fo Character handling)
                }
            default:
                return null;
        }
    }

    // returns the relevant Class from the typeList if it is dependent on
    // precision, scale, or nullability, <code>null</code> if the rules in 
    // the no-arg getMemberType method are sufficient
    private Class getClassForNumericType(Integer precision, 
            Integer scale, boolean isNullable) {
        int precValue = ((precision == null) ? -1 : precision.intValue());
        int scaleValue = ((scale == null) ? -1 : scale.intValue());

        switch (sqlType) {
            case Types.DECIMAL:
            case Types.NUMERIC:
                // some jdbc drivers put in null or zero for a numeric
                // specified without precision or scale
                // return BigInteger in that case
                if ((precValue <= 0) && (scaleValue <= 0)){
                    return BigInteger.class;
                }

                if (scaleValue > 0) {
                    return BigDecimal.class;
                }
                if (precValue > 18) {
                    return BigInteger.class;
                }
                if (precValue > 9) {
                    return (isNullable ? Long.class : Long.TYPE);
                }
                if (precValue > 4) {
                    return (isNullable ? Integer.class : Integer.TYPE);
                }
                return (isNullable ? Short.class : Short.TYPE);
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.REAL:
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.BIT:
                return typeList[isNullable ? 0 : 1];
            default:
                return null;
        }
    }
    
    public boolean supportsFinder() {
        return supportsFinder;
    }
    
    /**
     * Return create SQLType.
     * @param sqlType value from java.sql.Types
     * @return SQLType or null if type cannot be created
     */
    public static SQLType getSQLType(int sqlType) {
        List types = getSQLTypes();
        int ind = types.indexOf(new SQLType(sqlType, null, null, false));
	return ind==-1?UNKNOWN:(SQLType)types.get(ind);
    }
    
    private static List getSQLTypes() {
        List types = (List) typeCache.get();
        if (types == null) {
            types = getAllSQLTypes();
            typeCache = new SoftReference(types);
        }
        return types;
    }
    
    private static List getAllSQLTypes() {
        return Arrays.asList(new SQLType[] {
            new SQLType(Types.ARRAY, "ARRAY",   //NOI18N
                new Class[0], false), 
            new SQLType(Types.BIGINT, "BIGINT",     //NOI18N
                new Class[] {java.math.BigInteger.class, Long.TYPE}, true),
            new SQLType(Types.DECIMAL, "DECIMAL",   //NOI18N
                new Class[] {java.math.BigDecimal.class, java.math.BigInteger.class, 
                    Short.class, Short.TYPE, Integer.class, Integer.TYPE, 
                    Long.class, Long.TYPE}, true),
            new SQLType(Types.NUMERIC, "NUMERIC",   //NOI18N
                new Class[] {java.math.BigDecimal.class, java.math.BigInteger.class, 
                    Short.class, Short.TYPE, Integer.class, Integer.TYPE, 
                    Long.class, Long.TYPE}, true),
            new SQLType(Types.BLOB, "BLOB",         //NOI18N
                new Class[0], false),
            new SQLType(Types.CHAR, "CHAR",         //NOI18N
                new Class[] {java.lang.String.class, Character.class, Character.TYPE}, true),
            new SQLType(Types.NCHAR, "NCHAR",         //NOI18N
                new Class[] {java.lang.String.class, Character.class, Character.TYPE}, true),
            new SQLType(Types.LONGVARCHAR, "LONGVARCHAR",   //NOI18N
                new Class[] {java.lang.String.class}, true),
            new SQLType(Types.LONGNVARCHAR, "LONGNVARCHAR",   //NOI18N
                new Class[] {java.lang.String.class}, true),
            new SQLType(Types.VARCHAR, "VARCHAR",           //NOI18N
                new Class[] {java.lang.String.class}, true),
            new SQLType(Types.NVARCHAR, "NVARCHAR",           //NOI18N
                new Class[] {java.lang.String.class}, true),
            new SQLType(Types.CLOB, "CLOB",                 //NOI18N
                new Class[] {java.lang.String.class}, false),
            new SQLType(Types.NCLOB, "NCLOB",                 //NOI18N
                new Class[] {java.lang.String.class}, false),
            new SQLType(Types.DATE, "DATE",                 //NOI18N
                new Class[] {java.sql.Date.class}, false),
            new SQLType(Types.FLOAT, "FLOAT",               //NOI18N
                new Class[] {Double.class, Double.TYPE}, true),
            new SQLType(Types.DOUBLE, "DOUBLE",             //NOI18N
                new Class[] {Double.class, Double.TYPE}, true),
            new SQLType(Types.REAL, "REAL",                 //NOI18N
                new Class[] {Float.class, Float.TYPE}, true),   
            new SQLType(Types.INTEGER, "INTEGER",           //NOI18N
                new Class[] {Integer.class, Integer.TYPE}, true),
            new SQLType(Types.JAVA_OBJECT, "JAVA_OBJECT",   //NOI18N
                new Class[0], false),
	    new SQLType(Types.NULL, "NULL",                 //NOI18N
                new Class[0], false),
            new SQLType(Types.OTHER, "OTHER",               //NOI18N
                new Class[]{Object.class}, true),    
            new SQLType(Types.STRUCT, "STRUCT",             //NOI18N
                new Class[0], false),
            new SQLType(Types.DISTINCT, "DISTINCT",         //NOI18N
                new Class[0], false),
            new SQLType(Types.BINARY, "BINARY",             //NOI18N
                new Class[] { byte[].class}, false),
            new SQLType(Types.BIT, "BIT",                   //NOI18N
                new Class[] {Boolean.class, Boolean.TYPE}, true),
            new SQLType(Types.BOOLEAN, "BOOLEAN",           //NOI18N
                new Class[] {Boolean.class, Boolean.TYPE}, true),
            new SQLType(Types.VARBINARY, "VARBINARY",       //NOI18N
                new Class[] {byte[].class}, false),
            new SQLType(Types.LONGVARBINARY, "LONGVARBINARY",   //NOI18N
                new Class[] {byte[].class}, false),
            new SQLType(Types.REF, "REF",                   //NOI18N
                new Class[0], false),
            new SQLType(Types.SMALLINT, "SMALLINT",         //NOI18N
                new Class[] {Short.class, Short.TYPE}, true),
            new SQLType(Types.TINYINT, "TINYINT",           //NOI18N
                new Class[] {Short.class, Short.TYPE}, true),
            new SQLType(Types.TIME, "TIME",                 //NOI18N
                new Class[] {java.sql.Time.class}, false),
            new SQLType(Types.TIMESTAMP, "TIMESTAMP",       //NOI18N
                new Class[] {java.sql.Timestamp.class}, false)
        });
    }
    
    private static SoftReference typeCache = new SoftReference(null);
}
    
