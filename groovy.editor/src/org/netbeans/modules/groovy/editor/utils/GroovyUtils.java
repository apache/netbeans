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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.groovy.editor.utils;

import javax.swing.text.BadLocationException;

/**
 *
 * @author Tor Norbye
 * @author Martin Adamek
 * @author Gopala Krishnan Sankaran
 */
public final class GroovyUtils {

    private GroovyUtils() {
    }

    /**
     * Return substring after last dot.
     * @param fqn fully qualified type name
     * @return singe typename without package, or method without type
     */
    public static String stripPackage(String fqn) {
        if (fqn.contains(".")) {
            int idx = fqn.lastIndexOf(".");
            fqn = fqn.substring(idx + 1);
        }
        return fqn.replace(";", "");
    }

    /**
     * Gets only package name for the given fully qualified name.
     *
     * @param fqn fully qualified name
     * @return only package name or empty string if the default package is used
     */
    public static String stripClassName(String fqn) {
        // In case of default package
        if (!fqn.contains(".")) { // NOI18N
            return ""; // NOI18N
        } else {
            return fqn.substring(0, fqn.lastIndexOf(".") + 1); // NOI18N
        }
    }

    /**
     * Gets only package name for the given fully qualified name.
     *
     * @param fqn fully qualified name
     * @return only package name or empty string if the default package is used
     */
    public static String getPackageName(String fqn) {
        // In case of default package
        if (!fqn.contains(".")) { // NOI18N
            return ""; // NOI18N
        } else {
            return fqn.substring(0, fqn.lastIndexOf(".")); // NOI18N
        }
    }

    public static boolean isRowWhite(String text, int offset) throws BadLocationException {
        try {
            // Search forwards
            for (int i = offset; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
            // Search backwards
            for (int i = offset-1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
            
            return true;
        } catch (IndexOutOfBoundsException ex) {
            throw getBadLocationException(ex, text, offset);
        }
    }

    public static boolean isRowEmpty(String text, int offset) throws BadLocationException {
        try {
            if (offset < text.length()) {
                char c = text.charAt(offset);
                if (!(c == '\n' || (c == '\r' && (offset == text.length()-1 || text.charAt(offset+1) == '\n')))) {
                    return false;
                }
            }
            
            if (!(offset == 0 || text.charAt(offset-1) == '\n')) {
                // There's previous stuff on this line
                return false;
            }

            return true;
        } catch (IndexOutOfBoundsException ex) {
            throw getBadLocationException(ex, text, offset);
        }
    }

    public static int getRowLastNonWhite(String text, int offset) throws BadLocationException {
        try {
            // Find end of line
            int i = offset;
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n' || (c == '\r' && (i == text.length()-1 || text.charAt(i+1) == '\n'))) {
                    break;
                }
            }
            // Search backwards to find last nonspace char from offset
            for (i--; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (IndexOutOfBoundsException ex) {
            throw getBadLocationException(ex, text, offset);
        }
    }

    public static int getRowFirstNonWhite(String text, int offset) throws BadLocationException {
        try {
            // Find start of line
            int i = offset-1;
            if (i < text.length()) {
                for (; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        break;
                    }
                }
                i++;
            }
            // Search forwards to find first nonspace char from offset
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (IndexOutOfBoundsException ex) {
            throw getBadLocationException(ex, text, offset);
        }
    }

    public static int getRowStart(String text, int offset) throws BadLocationException {
        try {
            // Search backwards
            for (int i = offset-1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return i+1;
                }
            }

            return 0;
        } catch (IndexOutOfBoundsException ex) {
            throw getBadLocationException(ex, text, offset);
        }
    }
    
    private static BadLocationException getBadLocationException(IndexOutOfBoundsException ex, String text, int offset) {
        BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
        ble.initCause(ex);
        return ble;
    }

}
