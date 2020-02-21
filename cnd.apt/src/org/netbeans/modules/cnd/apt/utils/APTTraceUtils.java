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

package org.netbeans.modules.cnd.apt.utils;

import java.io.IOException;
import java.io.Writer;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 */
public class APTTraceUtils {
    
    /** Creates a new instance of APTTraceUtils */
    private APTTraceUtils() {
    }

    public static String toFileString(APTFile aptFile) {
        if (aptFile == null) {
            return "<no file>"; // NOI18N
        } else {
            String file = aptFile.getPath().toString();
            if (!CndUtils.isUnitTestMode()) {
                return file;
            }
            String parentFile = CndPathUtilities.getDirName(file);
            String name = CndPathUtilities.getBaseName(file);
            if (parentFile == null) {
                return name;
            }
            return CndPathUtilities.getBaseName(parentFile) + "/" + name; // NOI18N
        }
    }
    
    /** Print out a child-sibling tree in LISP notation */
    public static String toStringList(APT t) {
        StringBuilder ts = new StringBuilder();
        if (t.getFirstChild() != null) {
            ts.append(" ("); // NOI18N
        }
        ts.append(" ").append(t.toString()); // NOI18N
        if (t.getFirstChild() != null) {
            ts.append(toStringList(t.getFirstChild()));
        }
        if (t.getFirstChild() != null) {
            ts.append(" )"); // NOI18N
        }
        if (t.getNextSibling() != null) {
            ts.append(toStringList(t.getNextSibling()));
        }
        return ts.toString();
    }

    public static String toStringTree(APT t) {
        StringBuilder ts = new StringBuilder();
        if (t.getFirstChild() != null) {
            ts.append(" ("); // NOI18N
        }
        ts.append(" ").append(t.toString()); // NOI18N
        if (t.getFirstChild() != null) {
            ts.append(toStringTree(t.getFirstChild()));
        }
        if (t.getFirstChild() != null) {
            ts.append(" )"); // NOI18N
        }
        return ts.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // xml output support    
    
    public static void xmlSerialize(APT t, Writer out) throws IOException {
        // print out this node and all siblings
        for (APT node = t; node != null; node = node.getNextSibling()) {
            if (node.getFirstChild() == null) {
                // print closed element (class name, attributes)
                xmlSerializeNode(node, out);
            } else {
                // print open tag
                xmlSerializeRootOpen(node, out);

                // print children
                xmlSerialize(node.getFirstChild(), out);

                // print end tag
                xmlSerializeRootClose(node, out);
            }
        }
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // help implementations
    
    private static void xmlSerializeNode(APT t, Writer out) throws IOException {
        out.write("<" + xmlNodeText(t) + "\"/>\n"); // NOI18N
    }

    private static void xmlSerializeRootOpen(APT t, Writer out) throws IOException {
        out.write("<" + xmlNodeText(t) + "\">\n"); // NOI18N
    }
    
    private static void xmlSerializeRootClose(APT t, Writer out) throws IOException {
        out.write("</" + t.getClass().getSimpleName() + ">\n"); // NOI18N
    }

    private static String xmlNodeText(APT t) {
        StringBuilder buf = new StringBuilder(100);
        buf.append(t.getClass().getSimpleName());
        buf.append(" type=\"").append(encode(getTypeName(t))); // NOI18N
        buf.append("\" text=\"").append(encode(t.toString())); // NOI18N
        return buf.toString();
    }
    
//    private static String decode(String text) {
//        char c, c1, c2, c3, c4, c5;
//        StringBuilder n = new StringBuilder();
//        for (int i = 0; i < text.length(); i++) {
//            c = text.charAt(i);
//            if (c == '&') {
//                c1 = text.charAt(i + 1);
//                c2 = text.charAt(i + 2);
//                c3 = text.charAt(i + 3);
//                c4 = text.charAt(i + 4);
//                c5 = text.charAt(i + 5);
//
//                if (c1 == 'a' && c2 == 'm' && c3 == 'p' && c4 == ';') {
//                    n.append("&"); // NOI18N
//                    i += 5;
//                }
//                else if (c1 == 'l' && c2 == 't' && c3 == ';') {
//                    n.append("<"); // NOI18N
//                    i += 4;
//                }
//                else if (c1 == 'g' && c2 == 't' && c3 == ';') {
//                    n.append(">"); // NOI18N
//                    i += 4;
//                }
//                else if (c1 == 'q' && c2 == 'u' && c3 == 'o' &&
//                    c4 == 't' && c5 == ';') {
//                    n.append("\""); // NOI18N
//                    i += 6;
//                }
//                else if (c1 == 'a' && c2 == 'p' && c3 == 'o' &&
//                    c4 == 's' && c5 == ';') {
//                    n.append("'"); // NOI18N
//                    i += 6;
//                }
//                else
//                    n.append("&"); // NOI18N
//            }
//            else
//                n.append(c);
//        }
//        return new String(n);
//    }

    private static String encode(String text) {
        char c;
        StringBuilder n = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            switch (c) {
                case '&':
                    {
                        n.append("&amp;"); // NOI18N
                        break;
                    }
                case '<':
                    {
                        n.append("&lt;"); // NOI18N
                        break;
                    }
                case '>':
                    {
                        n.append("&gt;"); // NOI18N
                        break;
                    }
                case '"':
                    {
                        n.append("&quot;"); // NOI18N
                        break;
                    }
                case '\'':
                    {
                        n.append("&apos;"); // NOI18N
                        break;
                    }
                default :
                    {
                        n.append(c);
                        break;
                    }
            }
        }
        return new String(n);
    }

    public static String getTypeName(APT t) {
        int/*APT.Type*/ type = t.getType();
        String str="<unknown>"; // NOI18N
        switch (type) {
            case APT.Type.INVALID:
                str = "INVALID"; // NOI18N
                break;
            case APT.Type.FILE:
                str = "FILE"; // NOI18N
                break;
            case APT.Type.TOKEN_STREAM:
                str = "TOKEN_STREAM"; // NOI18N
                break;
            case APT.Type.INCLUDE:
                str = "INCLUDE"; // NOI18N
                break;
            case APT.Type.INCLUDE_NEXT:
                str = "INCLUDE_NEXT"; // NOI18N
                break;
            case APT.Type.DEFINE:
                str = "DEFINE"; // NOI18N
                break;
            case APT.Type.UNDEF:
                str = "UNDEF"; // NOI18N
                break;
            case APT.Type.IFDEF:
                str = "IFDEF"; // NOI18N
                break;
            case APT.Type.IFNDEF:
                str = "IFNDEF"; // NOI18N
                break;
            case APT.Type.IF:
                str = "IF"; // NOI18N
                break;
            case APT.Type.ELIF:
                str = "ELIF"; // NOI18N
                break;
            case APT.Type.ELSE:
                str = "ELSE"; // NOI18N
                break;
            case APT.Type.ENDIF:
                str = "ENDIF"; // NOI18N
                break;
            case APT.Type.PRAGMA:
                str = "PRAGMA"; // NOI18N
                break;
            case APT.Type.LINE:
                str = "LINE"; // NOI18N
                break;
            case APT.Type.ERROR:
                str = "ERROR"; // NOI18N
                break;
            case APT.Type.PREPROC_UNKNOWN:
                str = "PREPROC_UNKNOWN"; // NOI18N
                break;
            default:
        }
        return str;
    }    
}
