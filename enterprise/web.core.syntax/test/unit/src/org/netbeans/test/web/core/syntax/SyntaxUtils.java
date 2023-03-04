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

package org.netbeans.test.web.core.syntax;

import java.io.PrintStream;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/** Syntax utils class.
 *
 * @author  mf100882
 */
public class SyntaxUtils {

    /** Dumps token context names for given {@link org.netbeans.editor.TokenContextPath} */
    public static void dumpTokenContextPath(TokenContextPath tcp, PrintStream out) {
        TokenContext[] tcs = tcp.getContexts();
        for(int i = 0; i < tcs.length; i++ ) {
            String tcClassName = tcs[i].getClass().getName();
            tcClassName = tcClassName.substring(tcClassName.lastIndexOf(".") + 1);
            out.print(tcClassName + ( (i < (tcs.length - 1)) ? ", " : ""));
        }
    }
    
    /** converts \n to <NL> \t to <TAB> etc... */
    public static String normalize(String s, String[][] translationTable) {
        StringBuffer normalized = new StringBuffer();
        for(int i = 0; i < s.length(); i++) {
            String ch = s.substring(i,i+1);
            for(int j = 0; j < normalizeTable.length; j++) {
                if(ch.equals(normalizeTable[j][0])) ch = normalizeTable[j][1];
            }
            normalized.append(ch);
        }
        return normalized.toString();
    }
    
    /** the some as {@ling normalize(String s, String[][] translationTable)} 
     * but uses default translation table. */
    public static String normalize(String s) {
        return normalize(s, normalizeTable);
    }
    
    public static final String[][] normalizeTable = {{"\n", "<NL>"},
                                                      {"\t", "<TAB>"}};
    
}
