/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
