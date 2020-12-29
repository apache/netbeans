/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.python.source;

import org.openide.util.NbBundle;

public enum NameStyle {
    NO_PREFERENCE(NbBundle.getMessage(NameStyle.class, "NoPreference")),
    LOWERCASE("lowercase"),
    LOWERCASE_WITH_UNDERSCORES("lowercase_with_underscores"),
    UPPERCASE("UPPERCASE"),
    UPPERCASE_WITH_UNDERSCORES("UPPERCASE_WITH_UNDERSCORES"),
    CAPITALIZED_WORDS("CapitalizedWords"),
    MIXED_CASE("mixedCase"),
    CAPITALIZED_WITH_UNDERSCORES("Capitalized_With_Underscores");

    private String displayName;

    NameStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean complies(String name) {
        if (name.length() == 0) {
            return true;
        }

        // Always allow one or two "_" at the beginning and end of the name since
        // these are used to indicate private, builtin, etc.
        int start = 0;
        int end = name.length();
        if (name.startsWith("__")) { // NOI18N
            start = 2;
        } else if (name.startsWith("_")) { // NOI18N
            start = 1;
        }

        if (name.endsWith("__")) { // NOI18N
            end -= 2;
        } else if (name.endsWith("_")) { // NOI18N
            end -= 1;
        }

        if (start >= end) {
            return false;
        }

        switch (this) {
            case NO_PREFERENCE:
                return true;

            case LOWERCASE_WITH_UNDERSCORES:
            case LOWERCASE:
                for (int i = start; i < end; i++) {
                    char c = name.charAt(i);
                    if (c == '_') {
                        if (this != LOWERCASE_WITH_UNDERSCORES) {
                            return false;
                        }
                    } else if (Character.isDigit(c)) {
                    } else if (!Character.isLowerCase(c)) {
                        return false;
                    }
                }
                
                return true;
                

            case UPPERCASE:
            case UPPERCASE_WITH_UNDERSCORES:
                for (int i = start; i < end; i++) {
                    char c = name.charAt(i);
                    if (c == '_') {
                        if (this != UPPERCASE_WITH_UNDERSCORES) {
                            return false;
                        }
                    } else if (Character.isDigit(c)) {
                    } else if (!Character.isUpperCase(c)) {
                        return false;
                    }
                }

                return true;

            case MIXED_CASE:
            // TODO - require that characters after _ are capitalized?
            case CAPITALIZED_WORDS:
            case CAPITALIZED_WITH_UNDERSCORES:
                if (this == MIXED_CASE) {
                    // Must begin with lowercase
                    if (!Character.isLowerCase(name.charAt(start))) {
                        return false;
                    }
                } else if (this == CAPITALIZED_WORDS || this == CAPITALIZED_WITH_UNDERSCORES) {
                    // Must begin with uppercase
                    if (!Character.isUpperCase(name.charAt(start))) {
                        return false;
                    }
                }
                for (int i = start+1; i < end; i++) {
                    char c = name.charAt(i);
                    if (c == '_') {
                        if (this != CAPITALIZED_WITH_UNDERSCORES) {
                           return false;
                        }
                    } else if (Character.isDigit(c)) {
                    } else if (!Character.isUpperCase(c) && !Character.isLowerCase(c)) {
                        return false;
                    }

                    // What about digits?
                }

                return true;
            default:
                assert false : this;
        }
        
        return true;
    }
    
    public static boolean isProtectedName(String name) {
        // Protected variable starts with a single _
        // this is a convention only
        if (!name.startsWith("__")) {
            // NOI18N
            if (name.startsWith("_")) {
                // NOI18N
                return true;
            }
        }
        return false;
    }

    public static boolean isPrivateName(String name) {
        // Private variables: start with __ but doesn't end with __
        // Section 9.6 Private Variables - http://docs.python.org/tut/node11.html
        if (name != null && name.startsWith("__") && !name.endsWith("__")) {
            // NOI18N
            return true;
        } else if (name != null && name.startsWith("_") && !name.endsWith("_")) {
            // NOI18N
            // From PEP8: Single_leading_underscore: weak "internal use" indicator
            // (e.g. "from M import *" does not import objects whose name
            // starts with an underscore).
            return true;
        }
        return false;
    }
}
