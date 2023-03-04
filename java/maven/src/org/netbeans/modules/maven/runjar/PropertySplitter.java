/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.maven.runjar;

/**
 *
 * @author mkleint
 */
public class PropertySplitter {
    
        private String line;
        private char[] quotes;
        private char separator;
        private char newline;
        private boolean trim = true;
        private char escape;
        private boolean outputQuotes = true;
        
        private int location = 0;
        private char quoteChar = 0;
        private boolean inQuote = false;
        private boolean escapeNext = false;
        private boolean preserveWhitespace = true;

        public PropertySplitter(String line) {
            this(line, new char[] { '"', '\'' } , '\\', '\n', '\n'); //NOI18N
        }

        private PropertySplitter(String line, char[] quotes, char escape, char separator, char nl) {
            this.line = line;
            this.quotes = quotes;
            this.separator = separator;
            this.escape = escape;
            newline = nl;
        }

        public void setSeparator(char sep) {
            separator = sep;
        }
        
        
        public void setOutputQuotes(boolean outputQuotes) {
            this.outputQuotes = outputQuotes;
        }

        public String nextPair() {
            StringBuilder buffer = new StringBuilder();
            if (location >= line.length()) {
                return null;
            }
            //TODO should probably also handle (ignore) spaces before or after the = char somehow
            while (location < line.length()) {
                if (!(inQuote || escapeNext)) {
                    if (line.charAt(location) == separator || line.charAt(location) == newline) {
                        if (preserveWhitespace || buffer.length() > 0) {
                            break;
                        } else {
                            location++;
                            continue;
                        }
                    }
                }
                char c = line.charAt(location);
                X: if (escapeNext) {
                    if (c == newline) {
                        //just continue.. equals to \ + newline
                    } else {
                        buffer.append(escape).append(c);
                    }
                    escapeNext = false;
                } else if (c == escape) {
                    escapeNext = true;
                } else if (inQuote) {
                    if (c == quoteChar) {
                        inQuote = false;
                        if (!outputQuotes) {
                            break X;
                        }
                    }
                    buffer.append(c);
                } else {
                    if (isQuoteChar(c)) {
                        inQuote = true;
                        quoteChar = c;
                        if (!outputQuotes) {
                            break X;
                        }
                    }
                    buffer.append(c);
                }
                location++;
            }
            location++;
            return trim ? buffer.toString().trim() : buffer.toString();
        }

        private boolean isQuoteChar(char c) {
            for (int i = 0; i < quotes.length; i++) {
                char quote = quotes[i];
                if (c == quote) {
                    return true;
                }
            }
            return false;
        }
    
}
