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

package org.netbeans.modules.lexer.gen;

import java.lang.reflect.Field;
import java.lang.SecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import org.netbeans.modules.lexer.gen.util.LexerGenUtilities;

/**
 * The lexer generators often generate a class or interface
 * that contains integer fields of token types
 * named e.g. xxxConstants or xxxTokenTypes etc.
 * <BR>The <CODE>TokenConstants</CODE> class encapsulates the information
 * contained in such token types class.
 * <P>The reflection is used to collect
 * the "public static final int" fields in the token types class.
 * All these fields are collected but subclasses
 * may wish to hide some of the fields (e.g. some fields
 * may be related to states of an automaton instead of token types
 * identification).
 * 
 * @author Miloslav Metelka
 * @version 1.00
 */

public class TokenTypes {
    
    private final Class tokenTypesClass;
    
    private boolean inspected;

    /** Map of [tokenTypeName, tokenTypeValue] */
    protected final Map<String, Integer> name2value = new HashMap<>();
    
    /** Map of [tokenTypeValue, tokenTypeName] */
    protected final Map<Integer, String> value2name = new HashMap<>();
    
    public TokenTypes(Class tokenTypesClass) {
        this.tokenTypesClass = tokenTypesClass;
    }
    
    /**
     * Called by <CODE>LanguageData.registerTokenTypes()</CODE>
     * to update the language data into which it's being registered.
     * By default it adds mutable token ids that correspond
     * to the constants discovered in token types.
     * Can be overriden by subclasses to provide some more functionality.
     */
    protected void updateData(LanguageData languageData) {
        inspect();

        for (Iterator<String> it = tokenTypeNamesIterator(); it.hasNext();) {
            String tokenTypeName = it.next();
            MutableTokenId id = languageData.findIdByTokenTypeName(tokenTypeName);
            
            if (id == null) {
                String idName = LexerGenUtilities.idToLowerCase(tokenTypeName);
                id = languageData.newId(idName);
                id.updateByTokenType(tokenTypeName); // updateId() called automatically

            } else {
                updateId(id);
            }
        }
    }
    
    /**
     * Update a newly created or an existing token-id by the information
     * contained in this token-types.
     * The passed token-id already has tokenTypeName
     * filled in.
     */
    protected void updateId(MutableTokenId id) {
        String tokenTypeName = id.getTokenTypeName();
        if (tokenTypeName != null) { // no associated tokenTypeName
            Integer value = getTokenTypeValue(tokenTypeName);
            if (value == null) {
                throw new IllegalArgumentException("tokenTypeName=" + tokenTypeName
                    + " is not declared in " + getTokenTypesClass().getName());
            }

            // assign intId
            id.setIntId(value.intValue());
        }
    }
    
    public Class getTokenTypesClass() {
        return tokenTypesClass;
    }
    
    /**
     * @return Integer value of the static field with the given name
     *  or null if the field does not exist.
     */
    public Integer getTokenTypeValue(String tokenTypeName) {
        inspect();

        return name2value.get(tokenTypeName);
    }

    public String getTokenTypeName(int tokenTypeValue) {
        inspect();

        return value2name.get(Integer.valueOf(tokenTypeValue));
    }

    /**
     * @return all the field names 
     */
    public Iterator<String> tokenTypeNamesIterator() {
        inspect();

        return name2value.keySet().iterator();
    }
    
    
    public int findMaxTokenTypeValue() {
        inspect();

        int maxValue = 0;
        for (Iterator<Integer> it = value2name.keySet().iterator(); it.hasNext();) {
            Integer i = it.next();
            maxValue = Math.max(maxValue, i);
        }
        return maxValue;
    }
            
    /** Inspect the token types class.
     * This method can be overriden by children if necessary.
     * The method goes through the class
     * and puts the [field-name, integer-constant-value]
     * for all the static fields into the info map.
     * The <CODE>null</CODE> key is mapped to maximum constant value
     * found in the token types class.
     * The <CODE>List.class</CODE> key is mapped to the list
     * of all the field names in the order in which they
     * were found in the token types class.
     * @return true if the inspection was really done
     *  or false if the inspection was already done previously.
     */
    protected boolean inspect() {
        if (inspected) {
            return false;
        }
        inspected = true;

        try {
            Field[] fields = getTokenTypesClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                if (f.getType() == int.class) {
                    int value = f.getInt(null);
                    String fieldName = f.getName();
                    if (isAccepted(fieldName, value)) {
                        name2value.put(fieldName, value);
                        value2name.put(value, fieldName);
                    }
                }
            }
            
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.toString());

        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalStateException(e.toString());
        }
        
        return true; // inspection really done
    }

    /**
     * Whether it's ok to add the given field name to the list
     * of the [tokenTypeName, Integer] pairs.
     * <BR>Subclasses can exclude some field(s) if necessary.
     */
    protected boolean isAccepted(String tokenTypeName, int tokenTypeValue) {
        return true;
    }

}

