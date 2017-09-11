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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.lexer.gen.util;

/**
 * Utility methods.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class LexerGenUtilities {

    /**
     * Append given number of spaces to the given string buffer.
     */
    public static void appendSpaces(StringBuffer sb, int spaceCount) {
        while (--spaceCount >= 0) {
            sb.append(' ');
        }
    }

    /** Split class full name into package name and class name.
     * @param full name of the class
     * @return array containing package name and the class name.
     */
    public static String[] splitClassName(String classFullName) {
        int lastDotIndex = classFullName.lastIndexOf('.');
        return new String[] {
            (lastDotIndex >= 0) ? classFullName.substring(0, lastDotIndex) : "", // pkg name
            classFullName.substring(lastDotIndex + 1) // class name
        };
    }
    
    /** @return original string converted to uppercase with
     * hyphens converted to underscores.
     * @param s original string.
     */
    public static String idToUpperCase(String s) {
        return s.toUpperCase().replace('-', '_');
    }
    
    /** @return original string converted to lowercase with
     * underscores converted to hyphens.
     * @param s original string.
     */
    public static String idToLowerCase(String s) {
        return s.toLowerCase().replace('_', '-');
    }

    /** Convert string from the shape as it appears
     * in the source file into regular java string.
     */
    public static String fromSource(String s) {
        return LexerGenUtilitiesImpl.fromSource(s);
    }

    /**
     * Escape passed string as XML element content (<code>&lt;</code>, 
     * <code>&amp;</code> and <code>><code> in <code>]]></code> sequences).
     * @param text non-null string to be escaped
     * @return escaped text for xml
     */    
    public static String toElementContent(String text) {
        return LexerGenUtilitiesImpl.toElementContent(text);
    }

}
