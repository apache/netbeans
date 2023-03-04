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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.openide.util.Utilities;

/**
 *
 * @author sdedic
 */
public final class FxXmlSymbols {
    /**
     * Import processing instruction
     */
    public static final String FX_IMPORT = "import";
    
    /**
     * Star marker at the end of the import
     */
    public static final String FX_IMPORT_STAR = "*"; 
    
    /**
     * Include processing instruction
     */
    public static final String FX_INCLUDE = "include";
    
    
    public static final String FX_DEFINITIONS = "define";
    
    public static final String FX_COPY = "copy";
    
    public static final String FX_REFERENCE = "reference";
    
    public static final String FX_ATTR_ID = "id";
    
    public static final String FX_LANGUAGE = "language";
    
    public static final String FX_ATTR_REFERENCE_SOURCE = "source";
    
    public static final String FX_ATTR_TYPE = "type";
    
    public static final String FX_ATTR_CONSTANT = "constant";
    
    public static final String FX_SCRIPT = "script";
    
    public static final String FX_ROOT = "root";
    
    /**
     * The fx:value attribute
     */
    public static final String FX_VALUE = "value";
    
    /**
     * The fx:id attribute
     */
    public static final String FX_ID = "id";
    
    /**
     * The fx:controller attribute permitted on the root element
     */
    public static final String FX_CONTROLLER = "controller"; // NOI18N
    
    /**
     * The fx:factory attribute
     */
    public static final String FX_FACTORY = "factory";
    /**
     * Name of the value-of factory method, as per FXML guide
     */
    public static final String NAME_VALUE_OF = "valueOf"; // NOI18N
    public static final String SETTER_PREFIX = "set"; // NOI18N
    
    public static final String SCENEBUILDER_PI_PREFIX = "scenebuilder-"; // NOI18N

    /**
     * Determines, whether a character sequence (local tag name) is a classname tag.
     * FXML guide <b>specifies</b> that class tags must start with uppercase (or their
     * last part must start with uppercase) letter.
     * 
     * @param s
     * @return 
     */
    public static boolean isClassTagName(CharSequence s) {
        if (s.length() == 0) {
            return false;
        }
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '.') {
                if (i < s.length() - 1) {
                    char c = s.charAt(i + 1);
                    return Character.isUpperCase(c);
                }
            }
        }
        return Character.isUpperCase(s.charAt(0));
    }
    
    /**
     * Determines whether the name corresponds to an event handler.
     * Event handlers start with "on", followed by capitalized event name.
     * 
     * @param s
     * @return event name or {@code null}.
     */
    public static String getEventHandlerName(CharSequence s) {
       if (s.length() < 3) {
           return null;
       } 
       if (!((s.charAt(0) == 'o' ) && s.charAt(1) == 'n' &&
             Character.isUpperCase(s.charAt(2)))) {
           return null;
       }
       return Character.toLowerCase(s.charAt(2)) + s.subSequence(3, s.length()).toString();
    }
    
    public static int findStaticProperty(CharSequence s) {
        boolean allIdentifiers = true;
        
        for (int i = s.length() - 1; allIdentifiers && i >= 0; i--) {
            char c = s.charAt(i);
            if (c == '.') {
                // check that the starting subsequence forms a class name
                if (!isClassTagName(s.subSequence(0, i))) {
                    return -2;
                }
                if (i >= s.length() - 1) {
                    return -2;
                }
                return Character.isLowerCase(s.charAt(i + 1)) ? i : -2;
            } else {
                if (!Character.isJavaIdentifierPart(c)) {
                    allIdentifiers = false;
                }
            }
        }
        return allIdentifiers ? -1 : -2;
    }
    
    public CharSequence[] splitPackageAndName(CharSequence qn) {
        for (int i = qn.length() - 1 ; i > 0; i--) {
            if (qn.charAt(i) == '.') {
                return new CharSequence[] { qn.subSequence(0, i), qn.subSequence(i + 1, qn.length()) };
            }
        }
        return null;
    }
    
    public static boolean isQualifiedIdentifier(String qn) {
        StringTokenizer tukac = new StringTokenizer(qn, ".");
        if (!tukac.hasMoreTokens()) {
            return false;
        }
        while (tukac.hasMoreTokens()) {
            if (!Utilities.isJavaIdentifier(tukac.nextToken())) {
                return false;
            }
        }
        return true;
    }
    
    private static final Set<String> fxReservedAttributes;
    
    static {
        Set<String> h = new HashSet<String>();
        h.add(FX_ATTR_CONSTANT);
        h.add(FX_ATTR_ID);
        h.add(FX_VALUE);
        h.add(FX_FACTORY);
        h.add(FX_CONTROLLER);
        
        fxReservedAttributes = h;
    }
    
    /**
     * Checks if the attribute is a FX-reserved attribute name, assuming
     * the attribute comes from FX namespace.
     * <p/>
     * The reserved names are:
     * <ul>
     * <li>id
     * <li>controller
     * <li>value
     * <li>factory
     * <li>constant
     * </ul>
     * @param fxLocalName
     * @return 
     */
    public static boolean isFxReservedAttribute(String fxLocalName) {
        return fxReservedAttributes.contains(fxLocalName);
    }
}
