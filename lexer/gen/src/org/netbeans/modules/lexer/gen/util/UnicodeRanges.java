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
 * Program that generates Unicode character ranges array
 * depending on the method being used.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class UnicodeRanges {

    public static final int IS_JAVA_IDENTIFIER_START = 1;

    public static final int IS_JAVA_IDENTIFIER_PART = 2;

    public static char[] findRanges(int testedMethod) {
        char[] ranges = new char[16]; // will grow very likely
        int rangesCount = 0;
        int rangeStart = -1;
        for (int i = 0; i < 65536; i++) {
            boolean valid = false;
            switch (testedMethod) {
                case IS_JAVA_IDENTIFIER_START:
                    valid = Character.isJavaIdentifierStart((char)i);
                    break;
                    
                case IS_JAVA_IDENTIFIER_PART:
                    valid = Character.isJavaIdentifierPart((char)i);
                    break;
                    
            }
            
            // The following code gets rid of post-handling code after for loop
            if (i == 65535 && valid) {
                if (rangeStart < 0) {
                    rangeStart = i;
                }
                i++;
                valid = false;
            }
                
            if (valid) {
                if (rangeStart < 0) {
                    rangeStart = i;
                }
                
            } else { // not valid
                if (rangeStart >= 0) {
                    // Check sufficient space in ranges array
                    if (ranges.length - rangesCount < 2) {
                        char[] tmp = new char[ranges.length * 2];
                        System.arraycopy(ranges, 0, tmp, 0, rangesCount);
                        ranges = tmp;
                    }
                    ranges[rangesCount++] = (char)rangeStart;
                    ranges[rangesCount++] = (char)(i - 1);

                    rangeStart = -1;
                }
            }
        }
        
        if (rangesCount < ranges.length) {
            char[] tmp = new char[rangesCount];
            System.arraycopy(ranges, 0, tmp, 0, rangesCount);
            ranges = tmp;
        }
        return ranges;
    }
    
    public static void appendUnicodeChar(StringBuffer sb, char ch, char quoteChar) {
        String ret = Integer.toHexString(ch);
        while (ret.length() < 4) {
            ret = "0" + ret;
        }
        sb.append(quoteChar);
        sb.append("\\u");
        sb.append(ret);
        sb.append(quoteChar);
    }
    
    public static void indent(StringBuffer sb, int indent) {
        while (indent-- > 0) {
            sb.append(' ');
        }
    }
    
    protected static String usage() {
        return "Prints ranges of characters belonging to selected category\n"
            + "arg0=Tested method:\n"
            + "        1 - Character.isJavaIdentifierStart()\n"
            + "        2 - Character.isJavaIdentifierPart()\n"
            + "arg1=Indentation e.g. 8\n";
    }
    
}
