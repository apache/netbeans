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

import java.util.Locale;
import org.netbeans.modules.dbschema.ColumnElement;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;
import org.openide.util.*;

//TODO move static methods into a separate util class or into code generator

/**
 * This class represents an instance of an member in an entity bean class. Each
 * entity member has certain properties that are independant of the backing.
 * These properties are represented by abstract methods in this class. This
 * class also serves as the abstract factory for creation of concrete backed
 * instances of this class.
 * @author Christopher Webster
 */
public abstract class EntityMember {
    
    /**
     * Name to use for member in bean and primary key class
     */
    private String memberName;
    
    /**
     * Class to use for member in bean and primary key class
     */
    private String memberClass;
    
    /**
     * Provide a mapping algorithm from an arbitrary string into a java
     * recommend field name (of the form lllUlll, where l = lower case U =
     * uppercase). The algorithm adds capitalization where a non alpha numeric
     * character appears. If the resultant field name would be a reserved java
     * identifier an integer will be appended to cause the result not to be a
     * reserved word.
     * @param fieldName name of field to transform
     */
    public static String makeFieldName(String fieldName) {
        StringBuilder retName = makeName(fieldName);
        retName.setCharAt(0, Character.toLowerCase(retName.charAt(0)));
        String returnS = retName.toString();
        if (!Utilities.isJavaIdentifier(returnS)) {
            returnS += '1';
        }
        return returnS;
    }

    /**
     * Provide a mapping algorithm from an arbitrary string into a java
     * recommend relationship field name (of the form lllUlll, where l =
     * lower case U = uppercase). The algorithm adds a suffix of "Collection"
     * if the second parameter is true.  In any case, it adds capitalization
     * where a non alpha numeric character appears. If the resultant field
     * name would be a reserved java identifier an integer will be appended
     * to cause the result not to be a reserved word.
     * @param fieldName name of field to transform
     * @param isCollection <code>true</code> if the relationship is a
     * a collection, <code>false</code> otherwise
     */
    public static String makeRelationshipFieldName(String fieldName,
            boolean isCollection) {
        return makeRelationshipFieldName(fieldName, CollectionType.COLLECTION, isCollection);
    }
        /**
     * Provide a mapping algorithm from an arbitrary string into a java
     * recommend relationship field name (of the form lllUlll, where l =
     * lower case U = uppercase). The algorithm adds a suffix dependent on collectionType argument
     * if the second parameter is true.  In any case, it adds capitalization
     * where a non alpha numeric character appears. If the resultant field
     * name would be a reserved java identifier an integer will be appended
     * to cause the result not to be a reserved word.
     * @param fieldName name of field to transform
     * @param isCollection <code>true</code> if the relationship is a
     * a collection, <code>false</code> otherwise
     */
    public static String makeRelationshipFieldName(String fieldName,
            CollectionType collectionType,
            boolean isCollection) {
        if (isCollection){
            fieldName += collectionType.getShortName();
        }
        return makeFieldName(fieldName);
    }

    /**
     * Fix the relationship field name to be more related to the collection type.
     *
     * @param orgName The original name
     * @param colType The collection type, such as, java.util.List, java.util.Set
     * @return The nicer name
     * @deprecated it was a fix for bad name, use makeRelationshipFieldName with collection type parameter to generate proper name initially
     */
    @Deprecated
    public static String fixRelationshipFieldName(String orgName, CollectionType colType) {
        String newName = orgName;
        if (orgName.endsWith("Collection")) { // NOI18N
            int ix = orgName.lastIndexOf("Collection"); // NOI18N
            newName = orgName.substring(0, ix) + colType.getShortName();
        }
        
        return newName;
    }

    private static StringBuilder makeName(String fieldName) {
        if (fieldName == null || fieldName.length() == 0) {
            fieldName = "a";    //NOI18N
        }
        
        if (!Character.isLetter(fieldName.charAt(0))) {
            StringBuilder removed = new StringBuilder(fieldName);
            while (removed.length() > 0 &&
                    !Character.isLetter(removed.charAt(0))) {
                removed.deleteCharAt(0);
            }
            return makeName(removed.toString());
        }
        
        String lower = fieldName.toLowerCase(Locale.ENGLISH);//see #157943
        String upper = fieldName.toUpperCase(Locale.ENGLISH);
        boolean mixedCase = !(fieldName.equals(lower) ||
                fieldName.equals(upper));
        
        return mapName(new StringBuilder(mixedCase?fieldName:lower),
                !mixedCase);
    }
    
    /**
     * Provide a mapping algorithm from an arbitrary string into a java
     * recommend class name (of the form UllUlll, where l = lower case U =
     * uppercase).  The algorithm adds capitalization where a non alpha numeric
     * character appears.
     * @param className name of class to transform
     */
    public static String makeClassName(String className) {
        StringBuilder fieldName = makeName(className);
        if (JavaPersistenceQLKeywords.isKeyword(fieldName.toString())) {
            fieldName.append('1');
        }
        fieldName.setCharAt(0, Character.toUpperCase(fieldName.charAt(0)));
        return fieldName.toString();
    }
    
    private static StringBuilder mapName(StringBuilder mappedName,
            boolean convertUpper) {
        int i = 0;
        while (i < mappedName.length()) {
            if (!Character.isLetterOrDigit(mappedName.charAt(i))) {
                if (convertUpper && ((i+1) < mappedName.length())) {
                    mappedName.setCharAt(i+1,
                            Character.toUpperCase(mappedName.charAt(i+1)));
                }
                mappedName.deleteCharAt(i);
            } else {
                i++;
            }
        }
        
        return mappedName;
    }
    
    /**
     * Create new instance of Entity Member given a column element
     * @param columnElement column element to populate Entity Member using
     * @throws IllegalArgumentException if columnElement == null
     */
    public static EntityMember create(ColumnElement columnElement) {
        if (columnElement == null) {
            throw new IllegalArgumentException("columnElement == null");//NOI18N
        }
        return new DbSchemaEntityMember(columnElement);
    }
    
    /**
     * Return name to use for member variable in EJB classes.
     * @return name to use for member variable in EJB classes
     */
    public String getMemberName() {
        return memberName;
    }
    
    /**
     * set name to use for member variable in EJB classes
     * @param name to use for member variable
     * @throws IllegalArgumentException if name is not valid java identifier
     */
    public void setMemberName(String name) {
        if (!Utilities.isJavaIdentifier(name)) {
            throw new IllegalArgumentException("isJavaIdentifier()==false");    //NOI18N
        }
        
        memberName = name;
    }
    
    /**
     * Return full qualified type name to use for member declaration
     * @return type to use for member declaration
     */
    public String getMemberType() {
        return memberClass;
    }
    
    /**
     * Set class to use for member declaration.
     * @param aType to use for member declaration
     * @throws IllegalArgumentException if aClass == null
     */
    public void setMemberType(String aType) {
        if (aType == null) {
            throw new IllegalArgumentException("aType == null");    //NOI18N
        }
        memberClass = aType;
    }

    /**
     * Determine if this member is a large object type.
     * @return true if member is a large object type
     */
    public abstract boolean isLobType();

    /**
     * Determine if this member is part of the primary key.
     * @return true if member is part of the primary key
     */
    public abstract boolean isPrimaryKey();
    
    public abstract void setPrimaryKey(boolean isPk, boolean isPkField);
    
    /**
     * Determine if its value is automatically generated/incremented by the database
     */
    public abstract boolean isAutoIncrement();
    
    /**
     * @return true if underlying type supports finder equal queries
     */
    public abstract boolean supportsFinder();
    
    /** 
     * Get the length of the column - for character type fields only.
     * 
     * @return the length, <code>null</code> if it is not a character type
     * field or there is no length.
     */
    public abstract Integer getLength();

    /** 
     * Get the precision of the column - for numeric type fields only.
     * 
     * @return the precision, <code>null</code> if it is not a numeric type
     * field or there is no precision.
     */
    public abstract Integer getPrecision();

    /** 
     * Get the scale of the column - for numeric type fields only.
     * 
     * @return the scale, <code>null</code> if it is not a numeric type
     * field or there is no scale.
     */
    public abstract Integer getScale();
    
    /**
     * override java.lang.Object#equals based on member name.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null || !(other.getClass().isInstance(getClass()))) {
            return false;
        }
        return ((EntityMember) other).getMemberName().equals(getMemberName());
    }
    
    /**
     * override java.lang.Object#hashCode
     */
    @Override
    public int hashCode() {
        return getMemberName().hashCode();
    }
    
    public abstract boolean isNullable();
    public abstract String getColumnName();
    public abstract String getTableName();
}



