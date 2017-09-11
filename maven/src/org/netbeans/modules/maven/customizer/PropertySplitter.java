/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.customizer;

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
        
        private int location = 0;
        private char quoteChar = 0;
        private boolean inQuote = false;
        private boolean escapeNext = false;
        
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

        void setSeparator(char sep) {
            separator = sep;
        }
        
        
        public String nextPair() {
            StringBuilder buffer = new StringBuilder();
            if (location >= line.length()) {
                return null;
            }
            //TODO should probably also handle (ignore) spaces before or after the = char somehow
            while (location < line.length()
                    && ((line.charAt(location) != separator && line.charAt(location) != newline) 
                                                           || inQuote || escapeNext)) {
                char c = line.charAt(location);
                if (escapeNext) {
                    if (c == newline) {
                        //just continue.. equals to \ + newline
                    } else {
                        buffer.append(escape).append(c);
                    }
                    escapeNext = false;
                } else if (!inQuote && c == escape) {
                    escapeNext = true;
                } else if (inQuote) {
                    if (c == quoteChar) {
                        inQuote = false;
                    } 
                    buffer.append(c);
                } else {
                    if (isQuoteChar(c)) {
                        inQuote = true;
                        quoteChar = c;
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
