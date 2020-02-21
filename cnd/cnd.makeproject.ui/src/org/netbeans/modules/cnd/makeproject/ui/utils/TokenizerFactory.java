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
package org.netbeans.modules.cnd.makeproject.ui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 * list <--> converter
 *
 */
public final class TokenizerFactory {

    public interface Converter {

        String convertToString(List<String> list);

        List<String> convertToList(String text);
    }

    public static final Converter MACRO_CONVERTER = new Converter() {
            private final PathResolver resolver = new TrivialPathResolver();
            @Override
            public String convertToString(List<String> list) {
                return TokenizerFactory.convertToString(list,  ' ');
            }

            @Override
            public List<String> convertToList(String text) {
                return TokenizerFactory.tokenize(text, new String[]{"-D"}, " ", resolver); // NOI18N
            }
        };

    public static final Converter UNDEF_CONVERTER = new Converter() {
            private final PathResolver resolver = new TrivialPathResolver();
            @Override
            public String convertToString(List<String> list) {
                return TokenizerFactory.convertToString(list,  ' ');
            }

            @Override
            public List<String> convertToList(String text) {
                return TokenizerFactory.tokenize(text, new String[]{"-U"}, " ", resolver); // NOI18N
            }
        };

    public static final Converter DEFAULT_CONVERTER = new Converter() {
            @Override
            public String convertToString(List<String> list) {
                return TokenizerFactory.convertToString(list,  ';');
            }

            @Override
            public List<String> convertToList(String text) {
                List<String> newList = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(text, ";"); // NOI18N
                while (st.hasMoreTokens()) {
                    newList.add(st.nextToken());
                }
                return newList;
            }
        };

    public static Converter getPathConverter(final Project project, final Folder folder, final Item item, final String flag) {
        return new Converter() {
            private final PathResolver resolver = (String from) -> {
                if (CndPathUtilities.isPathAbsolute(from)) {
                    return from;
                }
                if (project != null) {
                    return from;
                } else if (folder != null) {
                    if (folder.isDiskFolder()) {
                        String folderPath = folder.getRootPath();
                        folder.isDiskFolder();
                        if (folderPath != null) {
                            from = folderPath+"/"+from; // NOI18N
                            if (CndPathUtilities.isPathAbsolute(from)) {
                                return CndPathUtilities.toAbsoluteOrRelativePath(folder.getConfigurationDescriptor().getBaseDir(), from);
                            } else {
                                return from;
                            }
                        }
                    } else {
                        // TODO: can we suggest more useful?
                        return from;
                    }
                } else if (item != null) {
                    // TODO: should use compile dir?
                    from = CndPathUtilities.getDirName(item.getAbsolutePath())+"/"+from; // NOI18N
                    return CndPathUtilities.toAbsoluteOrRelativePath(item.getFolder().getConfigurationDescriptor().getBaseDir(), from);
                }
                return from;                
            };
            @Override
            public String convertToString(List<String> list) {
                return TokenizerFactory.convertToString(list,  ';');
            }

            @Override
            public List<String> convertToList(String text) {
                return TokenizerFactory.tokenize(text, new String[]{flag}, "; ", resolver); // NOI18N
            }
        };
    }

    private interface PathResolver {
        String resolve(String from);
    }
    
    private static final class TrivialPathResolver implements PathResolver {

        @Override
        public String resolve(String from) {
            return from;
        }
    }
    
    // This is naive implementation of tokenizer for strings like this (without ordinal quotes):
    // ' 111   222  333=444       555'
    // '111 "222 333" "44 4=555" "666=777 888" 999=000 "a"'
    // '111 "222 333"   "44 4=555"   "666=777 888"   999=000 "a" b'
    // Should work in most real-word case, but you can easily broke it if you want.
    // If token is started with -D, then -D is removed.
    private static List<String> tokenize(String text, String[] keys, String SEPARATOR, PathResolver resolver) {
        List<String> splitLine = splitLine(text, SEPARATOR);
        List<String> result = new ArrayList<>();
        for(String s : splitLine) {
            if (s.startsWith("'") && s.endsWith("'") || // NOI18N
                    s.startsWith("\"") && s.endsWith("\"")) { // NOI18N
                if (s.length() >= 2) {
                    s = s.substring(1, s.length() - 1);
                }
            }
            addItem(result, keys, s, resolver);
        }
        return result;
    }

    
    private static List<String> splitLine(String line, String SEPARATOR) {
        List<String> res = new ArrayList<>();
        int i = 0;
        StringBuilder current = new StringBuilder();
        boolean isSingleQuoteMode = false;
        boolean isDoubleQuoteMode = false;
        char prev = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            i++;
            if (SEPARATOR.indexOf(c) >= 0) {
                if (isSingleQuoteMode || isDoubleQuoteMode) {
                    current.append(c);
                } else {
                    if (current.length() > 0) {
                        res.add(current.toString());
                        current.setLength(0);
                    }
                }
            } else switch (c) {
                case '\'': // NOI18N
                    if (prev != '\\') {
                        if (isSingleQuoteMode) {
                            isSingleQuoteMode = false;
                        } else if (!isDoubleQuoteMode) {
                            isSingleQuoteMode = true;
                        }
                    }
                    current.append(c);
                    break;
                case '\"': // NOI18N
                    if (prev != '\\') {
                        if (isDoubleQuoteMode) {
                            isDoubleQuoteMode = false;
                        } else if (!isSingleQuoteMode) {
                            isDoubleQuoteMode = true;
                        }
                    }
                    current.append(c);
                    break;
                case ' ': // NOI18N
                case '\t': // NOI18N
                case '\n': // NOI18N
                case '\r': // NOI18N
                    if (isSingleQuoteMode || isDoubleQuoteMode) {
                        current.append(c);
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
            prev = c;
        }
        if (current.length() > 0) {
            res.add(current.toString());
        }
        return res;
    }
   
    private static void addItem(List<String> result, String[] keys, String value, PathResolver resolver) {
        for(String key : keys) {
            if (value.startsWith(key)) {
                String s = removePrefix(key, value, resolver);
                if (!s.isEmpty()) {
                    if (s.contains("\\\"")) { // NOI18N
                        s = s.replace("\\\"", "\""); // NOI18N
                    }
                    result.add(s);
                }
                return;
            }
        }
        if (value.startsWith("-")) { // NOI18N
            //ugnore other keys
        } else {
            if (!value.isEmpty()) {
                if (value.contains("\\\"")) { // NOI18N
                    value = value.replace("\\\"", "\""); // NOI18N
                }
                result.add(value);
            }
        }
    }
    
    private static String removePrefix(String prefix, String str, PathResolver resolver) {
        return str.startsWith(prefix) ? resolver.resolve(str.substring(prefix.length())) : str;
    }

    private static String convertToString(List<String> list, char separator) {
        boolean addSep = false;
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (addSep) {
                ret.append(separator);
            }
            ret.append(CndPathUtilities.quoteIfNecessary(list.get(i)));
            addSep = true;
        }
        return ret.toString();
    }
}
