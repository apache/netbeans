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

package org.netbeans.modules.j2ee.dd.util;

/**
 * Utility methods for working with annotations.
 * This is a helper class; all methods are static.
 * @author Tomas Mysik
 */
public class AnnotationUtils {
    
    private AnnotationUtils() {
    }
    
    /**
     * Get <tt>JavaBeans</tt> property name for given <tt>setter</tt> method.
     * Return <code>null</code> in case of incorrect method name.
     * @param methodName name of the method.
     * @return <tt>JavaBeans</tt> property name or <code>null</code>.
     */
    public static String setterNameToPropertyName(String methodName) {
        // a setter name starts with "set" and
        // is longer than 3, respectively
        // i.e. "set()" is not a property setter
        if (methodName.length() > 3
                && methodName.startsWith("set")) { // NOI18N
            return toLowerCaseFirst(methodName.substring(3));
        }
        return null;
    }
    
    private static String toLowerCaseFirst(String value) {
        if (value.length() > 0) {
            // XXX incorrect wrt surrogate pairs
            char[] characters = value.toCharArray();
            // XXX locale
            characters[0] = Character.toLowerCase(characters[0]);
            return new String(characters);
        }
        return value;
    }
}
