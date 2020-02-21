/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.toolchain.compilers;

/**
 *
 */
public class FingerprintScanner {
    public static final String OUTPUT_FORMAT_VERSION = "__output_format_version"; //NOI18N
    public static final String TOOL_VERSION_STRING = "__tool_version_string"; //NOI18N
    public static final String TOOL_VERSION_NUMBER = "__tool_version_number"; //NOI18N
    public static final String TOOL_BUILD_NUMBER = "__tool_build_number"; //NOI18N
    public static final String SYSTEM_MACRO = "__system_macro"; //NOI18N
    public static final String SYSTEM_INCLUDE = "__system_include"; //NOI18N
    public static final String SYSTEM_INCLUDE_HEADER = "__dash_include"; //NOI18N
    
    public enum Kind {
        SystemMacro,
        UserMacro,
        SystemPath,
        UserPath,
        SystemIncludeHeader
    }
    
    public interface Result {
        Kind getKind();
        String getResult();
    }
    
    public static Result scaneLine(String line) {
        if (!line.startsWith("#")) { //NOI18N
            return null;
        }
        line = line.substring(1).trim();
        if (line.startsWith(SYSTEM_INCLUDE)) {
            line = line.substring(SYSTEM_INCLUDE.length()).trim();
            return new ResultImpl(Kind.SystemPath, removeQuotes(line));
        } else if (line.startsWith(SYSTEM_MACRO)) {
            line = line.substring(SYSTEM_MACRO.length()).trim();
            String[] macro = CCCCompiler.getMacro(line);
            if (CCCCompiler.isValidMacroName(macro[0])) {
                if (macro[1] != null) {
                    line = macro[0] + "=" + macro[1]; // NOI18N
                } else {
                    line = macro[0];
                }
                return new ResultImpl(Kind.SystemMacro, line);
            }
        } else if (line.startsWith(SYSTEM_INCLUDE_HEADER)) {
            line = line.substring(SYSTEM_INCLUDE_HEADER.length()).trim();
            return new ResultImpl(Kind.SystemIncludeHeader, removeQuotes(line));
        }
        return null;
    }
    
    private static String removeQuotes(String line) {
        if (line.startsWith("\"") && line.endsWith("\"") && line.length() > 1) { //NOI18N
            return line.substring(1, line.length() - 1);
        }
        return line;
    }
    
    private static final class ResultImpl implements Result {
        private final Kind kind;
        private final String result;
        
        private ResultImpl(Kind kind, String result) {
            this.kind = kind;
            this.result = result;
        }

        @Override
        public Kind getKind() {
            return kind;
        }

        @Override
        public String getResult() {
            return result;
        }
    }
}
