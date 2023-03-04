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

package org.netbeans.api.lexer;

import java.util.HashMap;
import java.util.Map;

/**
 * Supplementary information about particular input
 * that may be used to influence the lexer's operation.
 * <br/>
 * For example there may be a version of the language
 * to be used when lexing the input. The following code
 * will interpret the "assert" as an identifier:
 * <pre>
 *  InputAttributes attrs = new InputAttributes();
 *  attrs.setValue(JavaTokenId.language(), "version", "1.3", true);
 *  TokenHierarchy.create("assert", false, JavaTokenId.language(), null, attrs);
 * </pre>
 *
 * <p>
 * The properties are attached to a concrete language path only
 * or they may be applied globally to all of the occurrences
 * of the given path as a sub-path of the target path.
 * <br/>
 * See the "global" argument of
 * {@link #setValue(Language,Object,Object,boolean)}.
 * </p>
 *
 * <p>
 * This class may safely be operated by multiple threads.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class InputAttributes {
    
    private final Map<LanguagePath,LPAttrs> lp2attrs;
    
    public InputAttributes() {
        lp2attrs = new HashMap<LanguagePath,LPAttrs>();
    }

    /**
     * Get value for the given key for the particular language path.
     * <br/>
     * If (for the given key) there was an explicit value set
     * directly for the given language path then it will be returned.
     * <br/>
     * If not and there was a global value set for one of the sub-paths
     * (from the largest to the smallest) then it will be returned.
     *
     * @param languagePath non-null language path.
     * @param attributeKey non-null key of the attribute.
     * @return value of the key for the given language path (or a global
     *  value for one of the subpaths).
     */
    public Object getValue(LanguagePath languagePath, Object attributeKey) {
        checkAttributeKeyNotNull(attributeKey);
        synchronized (lp2attrs) {
            LPAttrs attrs = lp2attrs.get(languagePath);
            Object value = null;
            if (attrs != null) {
                value = attrs.getSpecific(attributeKey);
                if (value == null) { // try global value
                    value = attrs.getGlobal(attributeKey);
                }
            }
            // Try global values for subpaths
            while (value == null && languagePath.size() > 1) {
                languagePath = languagePath.subPath(1);
                attrs = lp2attrs.get(languagePath);
                if (attrs != null) {
                    value = attrs.getGlobal(attributeKey);
                }
            }
            return value;
        }
    }
    
    /**
     * Assign a new value to a property for the language path constructed
     * from the given language.
     *
     * @see #setValue(LanguagePath, Object, Object, boolean)
     */
    public void setValue(Language<?> language,
    Object attributeKey, Object attributeValue, boolean global) {
        setValue(LanguagePath.get(language), attributeKey, attributeValue, global);
    }

    /**
     * Assign a new value to a property for the given language path.
     *
     * @param languagePath non-null language path.
     * @param attributeKey non-null key of the attribute.
     * @param attributeValue value of the key for the given language path.
     * @param global if set to true then the value will be used not only for the given
     *  language path but also as a default value (if the value is not overwritten explicitly)
     *  for all the cases where the given path is embedded into the target language path.
     *  <br/>
     *  The following code
     *  <pre>
     *  attrs.setValue(LanguagePath.get(JavaTokenId.language()),
     *      "version", Integer.valueOf(5), true);
     *  </pre>
     *  sets the version 5 (it means java 1.5) to all the java code snipets
     *  regardless of where they are embedded in the token hierarchy.
     */
    public void setValue(LanguagePath languagePath,
    Object attributeKey, Object attributeValue, boolean global) {
        checkAttributeKeyNotNull(attributeKey);
        synchronized (lp2attrs) {
            LPAttrs attrs = lp2attrs.get(languagePath);
            if (attrs == null) {
                attrs = new LPAttrs();
                lp2attrs.put(languagePath, attrs);
            }
            if (global) {
                attrs.putGlobal(attributeKey, attributeValue);
            } else {
                attrs.putSpecific(attributeKey, attributeValue);
            }
        }
    }
    
    private void checkAttributeKeyNotNull(Object attributeKey) {
        if (attributeKey == null) {
            throw new IllegalArgumentException("attributeKey cannot be null");
        }
    }
    
    private static final class LPAttrs {
        
        private Map<Object,Object> specifics;
        
        private Map<Object,Object> globals;
        
        public Object getSpecific(Object key) {
            return (specifics != null) ? specifics.get(key) : null;
        }
        
        public Object getGlobal(Object key) {
            return (globals != null) ? globals.get(key) : null;
        }
        
        public void putSpecific(Object key, Object value) {
            if (specifics == null) {
                specifics = new HashMap<Object,Object>(4);
            }
            specifics.put(key, value);
        }
        
        public void putGlobal(Object key, Object value) {
            if (globals == null) {
                globals = new HashMap<Object,Object>(4);
            }
            globals.put(key, value);
        }
        
    }
    
}
