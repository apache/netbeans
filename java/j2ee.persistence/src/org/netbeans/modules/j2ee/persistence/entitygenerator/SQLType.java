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
    @Override
    public boolean equals(Object other) {
        if (other == null || !getClass().isInstance(other)){
	    return false;
        }  
	return ((SQLType)other).sqlType == sqlType;
    }

    /**
     * Override Object.hashCode
     */
    @Override
    public int hashCode() {
        return sqlType;
    }
  
    /**
     * Provide string representation of sql type. For example, 
     * java.sql.Types.BIT returns BIT.
     */
    @Override
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
                if ((length != null) && (length == 1)) {
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
        int precValue = (precision == null) ? -1 : precision;
        int scaleValue = (scale == null) ? -1 : scale;

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
    
