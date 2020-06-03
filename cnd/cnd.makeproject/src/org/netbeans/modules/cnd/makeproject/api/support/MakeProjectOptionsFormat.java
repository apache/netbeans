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
package org.netbeans.modules.cnd.makeproject.api.support;

import java.util.ArrayList;

/**
 *
 */
public class MakeProjectOptionsFormat {
    
    private MakeProjectOptionsFormat() {
    }
    
    public static String reformatWhitespaces(String string)  {
        return reformatWhitespaces(string, ""); // NOI18N
    }
    
    public static String reformatWhitespaces(String string, String prepend)  {
        return reformatWhitespaces(string, prepend, ""); // NOI18N
    }
    
    public static String reformatWhitespaces(String string, String prepend, String delimiter)  {
        if (string == null || string.length() == 0) {
            return string;
        }
        
        final String FAKE_ENDING = "##!?!##"; // NOI18N        
        boolean firstToken = true;
        // We need to take case about the following strings: "str\\    " (without quotes)
        // Should be converted to: "str\\ " (without quotes)
        boolean endsWithSpace = string.endsWith(" ") && string.trim().endsWith("\\"); // NOI18N
        if (endsWithSpace) {
            string = string.trim() + ' ' + FAKE_ENDING; // NOI18N
        }
        ArrayList<String> tokens = tokenizeString(string);
        StringBuilder formattedString = new StringBuilder(string.length());
        for (String token : tokens) {
            if (!firstToken) {
                formattedString.append(delimiter);
                formattedString.append(" "); // NOI18N
            }
            formattedString.append(prepend);
            formattedString.append(token);
            firstToken = false;
        }
       
        return endsWithSpace? formattedString.toString().replace(FAKE_ENDING, ""): formattedString.toString(); // NOI18N
    }
    
    public static ArrayList<String> tokenizeString(String string)  {
        ArrayList<String> list = new ArrayList<>(0);
        
        if (string == null || string.length() == 0) {
            return list;
        }
        StringBuilder token = new StringBuilder();
        boolean inToken = false;
        boolean inQuote = false;
        char quoteChar = '\0';
        for (int i = 0; i <= string.length(); i++) {
            boolean eol = (i == string.length());
            if (eol || inToken) {
                if (!eol && inQuote) {
                    token.append(string.charAt(i));
                    if (string.charAt(i) == quoteChar) {
                        inQuote = false;
                    }
                } else {
                    if (eol || Character.isWhitespace(string.charAt(i))) {
                        if (token.length() > 0) {
                            list.add(token.toString());
                            }
                        inToken = false;
                        token = new StringBuilder();
                    } else {
                        token.append(string.charAt(i));
                        if (string.charAt(i) == '"' || string.charAt(i) == '`' || string.charAt(i) == '\'') {
                            inQuote = true;
                            quoteChar = string.charAt(i);
                        }
                    }
                }
            } else {
                if (!Character.isWhitespace(string.charAt(i))) {
                    token.append(string.charAt(i));
                    inToken = true;
                }
            }
        }
        if (token.length() > 0) {
            list.add(token.toString());
        }
        
        return list;
    }


}
