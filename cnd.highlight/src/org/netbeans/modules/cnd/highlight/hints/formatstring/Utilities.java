/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.hints.formatstring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.NbBundle;

/**
 *
 */
class Utilities {    
    // check if object is a function which accepts format string
    // return the position of format string in list of arguments
    // returns -1 if function does not accepts format string
    static int checkFormattedPrintFunction(CsmObject object) {
        if (CsmKindUtilities.isFunction(object)) {
            CsmFunction function = (CsmFunction) object;
            String functionName = function.getName().toString();
            int position = checkPrintf(functionName);
            if (position != -1) {
                CsmFile srcFile = function.getContainingFile();
                for (CsmInclude include : CsmFileInfoQuery.getDefault().getIncludeStack(srcFile)) {
                    if (include.getIncludeName().toString().equals("stdio.h")) {  // NOI18N
                        return position;
                    }
                }
            }
        }
        return -1;
    }

    static int checkPrintf(CharSequence functionName) {
        int position = -1;
        if (CharSequenceUtils.contentEquals(functionName, "printf") || CharSequenceUtils.contentEquals(functionName, "vprintf")) {  // NOI18N
            position = 0;
        } else if (CharSequenceUtils.contentEquals(functionName, "snprintf") || CharSequenceUtils.contentEquals(functionName, "vsnprintf")) {  // NOI18N
            position = 2;
        } else if (CharSequenceUtils.endsWith(functionName, "printf")) {  // NOI18N
            position = 1;
        }
        return position;
    }
    
    // take const modifier into account
    static List<String> typeToFormat(String type) {
        if (type.contains("*")) {                           // NOI18N
            if (type.contains("wchar_t")) {                 // NOI18N
                return Arrays.asList("s", "p", "ls", "S");  // NOI18N
            } else if (type.contains("char")) {             // NOI18N
                return Arrays.asList("s", "p", "hhn");      // NOI18N
            } else if (type.contains("short")) {            // NOI18N
                return Arrays.asList("p", "hn");            // NOI18N
            } else if (type.contains("long long")) {        // NOI18N
                return Arrays.asList("p", "lln");           // NOI18N
            } else if (type.contains("long")) {             // NOI18N
                return Arrays.asList("p", "ln");            // NOI18N
            } else if (type.contains("intmax_t")) {         // NOI18N
                return Arrays.asList("p", "jn");            // NOI18N
            } else if (type.contains("int")) {              // NOI18N
                return Arrays.asList("p", "n");             // NOI18N
            } else if (type.contains("size_t")) {           // NOI18N
                return Arrays.asList("p", "zn");            // NOI18N
            } else if (type.contains("ptrdiff_t")) {        // NOI18N
                return Arrays.asList("p", "tn");            // NOI18N
            } else {
                return Collections.singletonList("p");      // NOI18N
            } 
        } else if (type.startsWith("unsigned")) {                  // NOI18N
            if (type.contains("char")) {                           // NOI18N
                return Arrays.asList("hhu", "hho", "hhx", "hhX");  // NOI18N
            } else if (type.contains("short")) {                   // NOI18N
                return Arrays.asList("hu", "ho", "hx", "hX");      // NOI18N
            } else if (type.contains("long long")) {               // NOI18N
                return Arrays.asList("llu", "llo", "llx", "llX");  // NOI18N
            } else if (type.contains("long")) {                    // NOI18N
                return Arrays.asList("lu", "lo", "lx", "lX");      // NOI18N
            } else if (type.contains("int")) {                     // NOI18N
                return Arrays.asList("u", "o", "x", "X");          // NOI18N
            }
        } else {
            if (type.contains("signed char")) {                           // NOI18N
                return Arrays.asList("hhd", "hhi");                       // NOI18N
            } else if (type.contains("wchar_t")) {                        // NOI18N
                return Arrays.asList("lc", "C");                               // NOI18N
            } else if (type.contains("char")) {                           // NOI18N
                return Arrays.asList("c");                                // NOI18N
            } else if (type.contains("short")) {                          // NOI18N
                return Arrays.asList("hd", "hi");                         // NOI18N
            } else if (type.contains("uintmax_t")) {                      // NOI18N
                return Arrays.asList("jo", "ju", "jx", "jX");             // NOI18N
            } else if (type.contains("intmax_t")) {                       // NOI18N
                return Arrays.asList("jd", "ji");                         // NOI18N
            } else if (type.contains("size_t")) {                         // NOI18N
                return Arrays.asList("zd", "zi","zo", "zu", "zx", "zX");  // NOI18N
            } else if (type.contains("ptrdiff_t")) {                      // NOI18N
                return Arrays.asList("td", "ti","to", "tu", "tx", "tX");  // NOI18N
            } else if (type.contains("wint_t")) {                         // NOI18N
                return Arrays.asList("c", "lc", "C");                     // NOI18N
            } else if (type.contains("float")) {  // NOI18N
                return Arrays.asList("f", "lf", "llf", "F", "lF", "llF",   // NOI18N
                                     "e", "le", "lle", "E", "lE", "llE",   // NOI18N
                                     "g", "lg", "llg", "G", "lG", "llG",   // NOI18N
                                     "a", "la", "lla", "A", "lA", "llA");  // NOI18N
            } else if (type.contains("long double")) {                                 // NOI18N
                return Arrays.asList("f", "lf", "llf", "Lf", "F", "lF", "llF", "LF",   // NOI18N
                                     "e", "le", "lle", "Le", "E", "lE", "llE", "LE",   // NOI18N
                                     "g", "lg", "llg", "Lg", "G", "lG", "llG", "LG",   // NOI18N
                                     "a", "la", "lla", "La", "A", "lA", "llA", "LA");  // NOI18N
            } else if (type.contains("double")) {  // NOI18N
                return Arrays.asList("f", "lf", "llf", "F", "lF", "llF",   // NOI18N
                                     "e", "le", "lle", "E", "lE", "llE",   // NOI18N
                                     "g", "lg", "llg", "G", "lG", "llG",   // NOI18N
                                     "a", "la", "lla", "A", "lA", "llA");  // NOI18N
            } else if (type.contains("long long")) {  // NOI18N
                return Arrays.asList("lld", "lli");   // NOI18N
            } else if (type.contains("long")) {       // NOI18N
                return Arrays.asList("ld", "li");     // NOI18N
            } else if (type.contains("int")) {        // NOI18N
                return Arrays.asList("d", "i", "c");  // NOI18N
            }
        }
        return Collections.EMPTY_LIST;
    }
    
    static String getMessageForError(FormatError error) {
        switch (error.getType()) {
            case FLAG:
                return NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.message.incompatibleFlag", error.getFlag(), error.getSpecifier()); // NOI18N
            case LENGTH:
                return NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.message.incompatibleLength", error.getFlag(), error.getSpecifier()); // NOI18N
            case TYPE_MISMATCH:
                return NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.message.type", error.getFlag(), error.getSpecifier()); // NOI18N
            case TYPE_NOTEXIST:
                return NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.message.notexist", error.getSpecifier()); // NOI18N
            case TYPE_WILDCARD:
                return NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.message.wildcard", error.getFlag()); // NOI18N
            case ARGS:
                return NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.message.argnum"); // NOI18N
        }
        return null;
    }
    
}
