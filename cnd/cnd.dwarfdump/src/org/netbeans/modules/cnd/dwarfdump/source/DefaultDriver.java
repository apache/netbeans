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
package org.netbeans.modules.cnd.dwarfdump.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class DefaultDriver implements Driver {
    
    public List<String> splitCommandLine(String line, CompileLineOrigin isScriptOutput) {
        List<String> res = new ArrayList<String>();
        int i = 0;
        StringBuilder current = new StringBuilder();
        boolean isSingleQuoteMode = false;
        boolean isDoubleQuoteMode = false;
        while (i < line.length()) {
            char c = line.charAt(i);
            i++;
            switch (c) {
                case '\'': // NOI18N
                    if (isSingleQuoteMode) {
                        isSingleQuoteMode = false;
                    } else if (!isDoubleQuoteMode) {
                        isSingleQuoteMode = true;
                    }
                    current.append(c);
                    break;
                case '\"': // NOI18N
                    if (isDoubleQuoteMode) {
                        isDoubleQuoteMode = false;
                    } else if (!isSingleQuoteMode) {
                        isDoubleQuoteMode = true;
                    }
                    current.append(c);
                    break;
                case ' ': // NOI18N
                case '\t': // NOI18N
                case '\n': // NOI18N
                case '\r': // NOI18N
                    if (isSingleQuoteMode || isDoubleQuoteMode) {
                        current.append(c);
                        break;
                    } else {
                        if (current.length() > 0) {
                            res.add(current.toString());
                            current.setLength(0);
                        }
                    }
                    break;
                default:
                    current.append(c);
                    break;
            }
        }
        if (current.length() > 0) {
            res.add(current.toString());
        }
        return res;
    }

    public Artifacts gatherCompilerLine(String compileLine, CompileLineOrigin isScriptOutput, boolean isCpp) {
        if (compileLine.length() > 0) {
            return gatherCompilerLine(splitCommandLine(compileLine, isScriptOutput).listIterator(), isScriptOutput, isCpp);
        } else {
            return new ArtifactsImpl();
        }

    }

    public Artifacts gatherCompilerLine(ListIterator<String> st, CompileLineOrigin isScriptOutput, boolean isCpp) {
        ArtifactsImpl res = new ArtifactsImpl();
        while (st.hasNext()) {
            String option = st.next();
            if (option.startsWith("--")) { // NOI18N
                option = option.substring(1);
            }
            if (option.startsWith("-D")) { // NOI18N
                String macro = option.substring(2);
                int i = macro.indexOf('=');
                if (i > 0) {
                    String value = macro.substring(i + 1).trim();
                    if (value.length() >= 2
                            && (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'' || // NOI18N
                            value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')) { // NOI18N
                        value = value.substring(1, value.length() - 1);
                    }
                    res.userMacros.put(macro.substring(0, i), value);
                } else {
                    res.userMacros.put(macro, null);
                }
            } else if (option.startsWith("-I")) { // NOI18N
                String path = option.substring(2);
                if (path.length() == 0 && st.hasNext()) {
                    path = st.next();
                }
                res.userIncludes.add(removeQuotes(path));
            } else if (option.startsWith("-F")) { // NOI18N
                if (option.equals("-F") && st.hasNext()) { // NOI18N
                    String path = st.next();
                    res.userIncludes.add(removeQuotes(path)+FRAMEWORK); //NOI18N
                }
            } else if (option.startsWith("-U")) { // NOI18N
                String macro = option.substring(2);
                if (macro.length() == 0 && st.hasNext()) {
                    macro = st.next();
                }
                res.undefinedMacros.add(removeQuotes(macro));
            } else if (option.startsWith("-Y")) { // NOI18N
                String defaultSearchPath = option.substring(2);
                if (defaultSearchPath.length() == 0 && st.hasNext()) {
                    defaultSearchPath = st.next();
                }
                if (defaultSearchPath.startsWith("I,")) { // NOI18N
                    defaultSearchPath = defaultSearchPath.substring(2);
                    res.userIncludes.add(removeQuotes(defaultSearchPath));
                }
            } else if (option.startsWith("-isystem")) { // NOI18N
                String path = option.substring(8);
                if (path.length() == 0 && st.hasNext()) {
                    path = st.next();
                }
                res.userIncludes.add(removeQuotes(path));
            } else if (option.startsWith("-include")) { // NOI18N
                String path = option.substring(8);
                if (path.length() == 0 && st.hasNext()) {
                    path = st.next();
                }
                res.userFiles.add(removeQuotes(path));
            } else if (option.startsWith("-imacros")) { // NOI18N
                String path = option.substring(8);
                if (path.length() == 0 && st.hasNext()) {
                    path = st.next();
                }
                res.userFiles.add(removeQuotes(path));
            }
        }
        return res;
    }

    private String removeQuotes(String str) {
        if (str.length() >= 2 && (str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'' || // NOI18N
            str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"')) {// NOI18N
            str = str.substring(1, str.length() - 1); // NOI18N
        }
        return str;
    }

    private static final class ArtifactsImpl implements Artifacts {

        public final List<String> input = new ArrayList<String>();
        public final List<String> userIncludes = new ArrayList<String>();
        public final List<String> userFiles = new ArrayList<String>();
        public final Map<String, String> userMacros = new HashMap<String, String>();
        public final List<String> undefinedMacros = new ArrayList<String>();
        public final Set<String> libraries = new HashSet<String>();
        public final List<String> languageArtifacts = new ArrayList<String>();
        public final List<String> importantFlags = new ArrayList<String>();
        public String output;

        @Override
        public List<String> getInput() {
            return input;
        }

        @Override
        public List<String> getUserIncludes() {
            return userIncludes;
        }

        @Override
        public List<String> getUserFiles() {
            return userFiles;
        }

        @Override
        public Map<String, String> getUserMacros() {
            return userMacros;
        }

        @Override
        public List<String> getUserUndefinedMacros() {
            return undefinedMacros;
        }

        @Override
        public Set<String> getLibraries() {
            return libraries;
        }

        @Override
        public List<String> getLanguageArtifacts() {
            return languageArtifacts;
        }

        @Override
        public List<String> getImportantFlags() {
            return importantFlags;
        }

        @Override
        public String getOutput() {
            return output;
        }
    }
}
