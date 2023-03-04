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
package org.netbeans.modules.java.nativeimage.debugger.displayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;

/**
 * Various static utilities.
 */
final class Utils {

    private Utils() {
    }

    static Map<String, NIVariable> getVarsByName(NIVariable[] vars) {
        switch (vars.length) {
            case 0:
                return Collections.emptyMap();
            case 1:
                return Collections.singletonMap(vars[0].getName(), vars[0]);
            default:
                Map<String, NIVariable> varsByName = new HashMap<>(vars.length);
                for (NIVariable var : vars) {
                    varsByName.put(var.getName(), var);
                }
                return Collections.unmodifiableMap(varsByName);
        }
    }

    @CheckForNull
    static NIVariable findChild(NIVariable[] children, String... names) {
        NIVariable ch = null;
        for (String name : names) {
            if (ch != null) {
                children = ch.getChildren();
            }
            ch = findChild(name, children);
            if (ch == null) {
                return null;
            }
        }
        return ch;
    }

    @CheckForNull
    private static NIVariable findChild(String name, NIVariable[] children) {
        for (NIVariable var : children) {
            if (name.equals(var.getName())) {
                return var;
            }
        }
        return null;
    }

    // Quote all types which follow the class keyword and variables having dots in name.
    // This is crucial for types containing dots like Java class names.
    static String quoteJavaTypes(String expr) {
        final String clazz = "(class "; // NOI18N
        int i = expr.indexOf(clazz);
        if (i >= 0) {
            StringBuilder quoted = new StringBuilder();
            int j = 0;
            do {
                i += clazz.length();
                quoted.append(quoteJavaVarNames(expr.substring(j, i)));
                j = expr.indexOf(')', i);
                if (j > 0) {
                    while (expr.charAt(j - 1) == '*') {
                        j--;
                    }
                    quoted.append('\'');
                    quoted.append(expr.substring(i, j));
                    quoted.append('\'');
                } else {
                    // Inconsistent parenthesis
                    return expr;
                }
                i = expr.indexOf(clazz, j);
            } while (i > 0);
            quoted.append(quoteJavaVarNames(expr.substring(j)));
            return quoted.toString();
        } else {
            return expr;
        }
    }

    // Variables that contain dots are quoted
    private static String quoteJavaVarNames(String expr) {
        int i = expr.indexOf('.');
        if (i > 0) {
            StringBuilder quoted = new StringBuilder();
            int i0 = 0;
            do {
                char c = 0;
                int i1 = i - 1;
                while (i1 >= i0 && ((c = expr.charAt(i1)) == '.' || Character.isJavaIdentifierPart(c))) {
                    i1--;
                }
                if (i1 >= i0) {
                    i1++;
                    c = expr.charAt(i1);
                }
                if (!Character.isJavaIdentifierStart(c)) {
                    i++;
                    quoted.append(expr.substring(i0, i));
                    i0 = i;
                    continue;
                }
                int i2 = i + 1;
                while (i2 < expr.length() && ((c = expr.charAt(i2)) == '.' || Character.isJavaIdentifierPart(c))) {
                    i2++;
                }
                quoted.append(expr.substring(i0, i1));
                quoted.append('\'');
                quoted.append(expr.substring(i1, i2));
                quoted.append('\'');
                i0 = i2;
            } while ((i = expr.indexOf('.', i0)) > 0);
            quoted.append(expr.substring(i0));
            return quoted.toString();
        } else {
            return expr;
        }
    }

}
