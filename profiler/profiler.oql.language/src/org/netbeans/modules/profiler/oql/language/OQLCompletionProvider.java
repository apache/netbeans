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

package org.netbeans.modules.profiler.oql.language;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 * @author Jaroslav Bachorik
 */
public class OQLCompletionProvider implements CompletionProvider {
    private final Set<String> keywords = new HashSet<String>();
    private final Set<String> functions = new HashSet<String>();
    private final Set<String> heapMethods = new HashSet<String>();
    
    public OQLCompletionProvider() {
        keywords.add("select"); // NOI18N
        keywords.add("from"); // NOI18N
        
        functions.add("map"); // NOI18N
        functions.add("filter"); // NOI18N
        functions.add("sort"); // NOI18N
        functions.add("top"); // NOI18N
        functions.add("classof"); // NOI18N
        functions.add("forEachReferrer"); // NOI18N
        functions.add("identical"); // NOI18N
        functions.add("objectid"); // NOI18N
        functions.add("reachables"); // NOI18N
        functions.add("referrers"); // NOI18N
        functions.add("referees"); // NOI18N
        functions.add("refers"); // NOI18N
        functions.add("root"); // NOI18N
        functions.add("sizeof"); // NOI18N
        functions.add("rsizeof"); // NOI18N
        functions.add("toHtml"); // NOI18N
        functions.add("concat"); // NOI18N
        functions.add("contains"); // NOI18N
        functions.add("count"); // NOI18N
        functions.add("filter"); // NOI18N
        functions.add("length"); // NOI18N
        functions.add("map"); // NOI18N
        functions.add("max"); // NOI18N
        functions.add("min"); // NOI18N
        functions.add("sort"); // NOI18N
        functions.add("top"); // NOI18N
        functions.add("sum"); // NOI18N
        functions.add("toArray"); // NOI18N
        functions.add("unique"); // NOI18N

        heapMethods.add("objects"); // NOI18N
        heapMethods.add("classes"); // NOI18N
        heapMethods.add("forEachClass"); // NOI18N
        heapMethods.add("forEachObject"); // NOI18N
        heapMethods.add("findClass"); // NOI18N
        heapMethods.add("findObject"); // NOI18N
        heapMethods.add("finalizables"); // NOI18N
        heapMethods.add("livepaths"); // NOI18N
        heapMethods.add("roots"); // NOI18N
    }

    public CompletionTask createTask(int queryType, final JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) return null;
        final Document document = component.getDocument();
        final TokenHierarchy<Document> th = TokenHierarchy.get(document);

        AsyncCompletionQuery query = new AsyncCompletionQuery() {

            @Override
            protected void query(final CompletionResultSet resultSet, Document doc, int caretOffset) {
                final TokenSequence ts = th.tokenSequence();
                final Token<OQLTokenId> currentToken = findCurrentToken(component, ts);

                // sanity test
                if (currentToken == null) {
                    resultSet.finish();
                    return;
                }

                final String tokentext = currentToken.text().toString();
                final int tokenLen = tokentext.length();
                final String tokentextTrim = tokentext.trim();
                final int tokenTrimLen = tokentextTrim.length();
                switch (currentToken.id()) {
                    case UNKNOWN: {
                        if ("instanceof".startsWith(tokentextTrim)) { // NOI18N
                            resultSet.addItem(new KeywordCompletionItem("00", "instanceof", ts.offset() + tokenTrimLen, tokenLen)); // NOI18N
                        }
                        break;
                    }
                    case SELECT: {
                        resultSet.addItem(new KeywordCompletionItem("00", "select", ts.offset() + tokenLen, tokenTrimLen)); // NOI18N
                        break;
                    }
                    case FROM: {
                        resultSet.addItem(new KeywordCompletionItem("00", "from", ts.offset() + tokenLen, tokenTrimLen)); // NOI18N
                        break;
                    }
                    case INSTANCEOF: {
                        resultSet.addItem(new KeywordCompletionItem("00", "instanceof", ts.offset() + tokenLen)); // NOI18N
                        break;
                    }
                    case WHERE: {
                        resultSet.addItem(new KeywordCompletionItem("00", "where", ts.offset() + tokenLen, tokenTrimLen)); // NOI18N
                        break;
                    }
                    case ERROR: {
                        for(String keyword : keywords) {
                            if (tokenTrimLen == 0 || keyword.startsWith(tokentextTrim)) {
                                KeywordCompletionItem kci = new KeywordCompletionItem("00", keyword, ts.offset() + tokenTrimLen, tokenTrimLen);  // NOI18N
                                resultSet.addItem(kci);
                            }
                        }
                        break;
                    }
                    case JSBLOCK: {
                        boolean isHeap = false;
                        int backout = 0;
                        if (ts.movePrevious()) backout++;
                        if (ts.movePrevious()) backout++; // check for "heap.somet[...]"
                        isHeap = ts.token().text().toString().trim().equalsIgnoreCase("heap"); // NOI18N
                        // get to the current token
                        for(int i=backout;i>0;i--) {
                            ts.moveNext();
                        }

                        int wsPosDiff = tokenTrimLen==0 ? tokenLen-1 : tokentext.indexOf(tokentextTrim);
                        for(String function : functions) {
                            if (tokenTrimLen == 0 || function.startsWith(tokentextTrim)) {
                                resultSet.addItem(new FunctionCompletionItem("00", function, ts.offset() + tokenTrimLen + wsPosDiff, tokenTrimLen)); // NOI18N
                            }
                        }
                        if ("heap".startsWith(tokentextTrim)) { // NOI18N
                            resultSet.addItem(new KeywordCompletionItem("00", "heap", ts.offset() + tokenTrimLen + wsPosDiff, tokenTrimLen)); // NOI18N
                        }

                        if (isHeap) {
                            for(String method : heapMethods) {
                                if (tokenTrimLen == 0 || method.startsWith(tokentextTrim)) {
                                    resultSet.addItem(new FunctionCompletionItem("00", method, ts.offset() + tokenTrimLen, tokenTrimLen)); // NOI18N
                                }
                            }
                        }

                        // special hack for "from" keyword
                        // kind of space-magick; in the same place as "from" keyword there may be a valid javascript
                        // not exactly the best designed language but, hey, it's just a script ...
                        if (tokentextTrim.isEmpty()) {
                            resultSet.addItem(new KeywordCompletionItem("01", "from", ts.offset() + tokenLen, tokenTrimLen)); // NOI18N
                        } else {
                            StringTokenizer t = new StringTokenizer(tokentext, " ");  // NOI18N
                            while (t.hasMoreTokens()) {
                                String tt = t.nextToken();
                                if ("FROM".startsWith(tt.trim().toUpperCase())) {  // NOI18N
                                    int pos = tokentext.indexOf(tt);
                                    int wsPos = tokentext.indexOf(' ', pos);
                                    int ttTrimLen = tt.trim().length();
                                    if (ttTrimLen == 3) {
                                        pos++;
                                    }
                                    resultSet.addItem(new KeywordCompletionItem("01", "from", ts.offset() + pos + (wsPos > -1 ? 1 : 2), ttTrimLen)); // NOI18N
                                    break;
                                }
                            }
                        }

                        break;
                    }
                    case DOT: {
                        ts.movePrevious();
                        if (ts.token().text().toString().trim().equalsIgnoreCase("heap")) { // NOI18N
                            ts.moveNext();

                            for(String method : heapMethods) {
                                resultSet.addItem(new FunctionCompletionItem("00", method, ts.offset() + 1)); // NOI18N
                            }
                        }
                        break;
                    }
                    case CLAZZ_E:
                    case CLAZZ: {
                        OQLEngine e = (OQLEngine)document.getProperty(OQLEngine.class);

                        String regex = ".*?" + tokentext.replace("[", "\\[").replace("]", "\\]").replace("$", "\\$") + ".*"; // NOI18N
                        String camel = null;
                        if (tokentextTrim.equals(tokentextTrim.toUpperCase())) {
                            // prepare camel-case completion
                            StringBuilder sb = new StringBuilder(".*?"); // NOI18N
                            for(int i=0;i<tokenTrimLen;i++) {
                                if (tokentextTrim.charAt(i) >= 'A' && tokentextTrim.charAt(i) <= 'Z') { // NOI18N
                                    sb.append(tokentextTrim.charAt(i));
                                    sb.append("[a-z]*?"); // NOI18N
                                } else {
                                    sb = null;
                                    break;
                                }
                            }
                            if (sb != null) {
                                sb.append(".*"); // NOI18N
                                camel = sb.toString();
                            }
                            
                        }
                        String regexBody = tokentext.replace("[", "\\[").replace("]", "\\]").replace("$", "\\$"); // NOI18N
                        String prefix = "^" + regexBody + ".*"; // NOI18N
                        Set<String> pkgCompletions = new HashSet<String>();
                        Set<String> completions = new HashSet<String>();

                        Iterator<JavaClass> clzs = e.getHeap().getJavaClassesByRegExp(regex).iterator();
                        while(clzs.hasNext()) {
                            String className = clzs.next().getName();
                            String[] sig = splitClassName(className);
                            if (sig[1].startsWith(tokentext)) {
                                completions.add("00 " + className); // NOI18N
                            } else if (sig[1].contains(tokentext)) {
                                completions.add("01 " + className); // NOI18N
                            }
                        }

                        clzs = e.getHeap().getJavaClassesByRegExp(prefix).iterator();
                        while(clzs.hasNext()) {
                            String className = clzs.next().getName();

                            String[] sig = splitClassName(className);

                            if (sig[0].length() > tokenTrimLen && sig[0].startsWith(tokentextTrim)) {
                                int pkgSepPos = sig[0].indexOf('.', tokenTrimLen + 1); // NOI18N
                                if (pkgSepPos == -1) {
                                    pkgCompletions.add(sig[0]);
                                } else {
                                    pkgCompletions.add(sig[0].substring(0, pkgSepPos));
                                }
                            }
                            if (sig[0].indexOf('.', tokenTrimLen - 1) == -1) { // NOI18N
                                completions.add("01 " + className); // NOI18N
                            }
                        }

                        if (camel != null) {
                            clzs = e.getHeap().getJavaClassesByRegExp(camel).iterator();
                            while(clzs.hasNext()) {
                                String className = clzs.next().getName();
                                completions.add("02 " + className); // NOI18N
                            }
                        }

                        Set<String> usedTypeNames = new HashSet<String>();
                        for(String completion : completions) {
                            StringTokenizer tok = new StringTokenizer(completion);
                            String sortPre = tok.nextToken();
                            String clzName = tok.nextToken();
                            if (!usedTypeNames.contains(clzName)) {
                                resultSet.addItem(new ClassnameCompletionItem(sortPre, clzName, ts.offset(), tokenLen));
                                usedTypeNames.add(clzName);
                            }
                        }
                        for(String completion : pkgCompletions) {
                            if (!usedTypeNames.contains(completion)) {
                                resultSet.addItem(new PackageCompletionItem(completion, ts.offset(), tokenLen));
                            }
                        }
                        break;
                    }
                }
                resultSet.finish();
            }
        };


        return query != null ? new AsyncCompletionTask(query) : null;
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (typedText.endsWith(".")) return CompletionProvider.COMPLETION_QUERY_TYPE;  // NOI18N
        return CompletionProvider.COMPLETION_ALL_QUERY_TYPE;
    }

    private Token<OQLTokenId> findCurrentToken(JTextComponent component, TokenSequence<OQLTokenId> ts) {
        Token<OQLTokenId> currentToken = null;
        ts.moveStart();
        int forPosition = component.getCaretPosition();
        int position = 0;
        while(ts.moveNext()) {
            position = ts.offset();
            if (position >= forPosition) {
                ts.movePrevious();
                break;
            }
            currentToken = ts.token();
        }
        return currentToken;
    }

    private static String[] splitClassName(String className) {
        String pkgName, typeName;
        int pkgPos = className.lastIndexOf('.'); // NOI18N
        if (pkgPos > -1) {
            pkgName = className.substring(0, pkgPos);
            typeName = className.substring(pkgPos + 1);
        } else {
            pkgName = ""; // NOI18N
            typeName = className;
        }
        return new String[]{pkgName, typeName};
    }
}
