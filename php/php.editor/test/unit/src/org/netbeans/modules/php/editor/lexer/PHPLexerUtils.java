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

package org.netbeans.modules.php.editor.lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Petr Pisl
 */
public class PHPLexerUtils extends TestCase {
    
    public static class LexerResultItem {
        private PHPTokenId tokenId;
        private String text;

        public LexerResultItem(PHPTokenId tokenId, String text) {
            this.tokenId = tokenId;
            this.text = text;
        }

        /**
         * @return the tokenId
         */
        public PHPTokenId getTokenId() {
            return tokenId;
        }

        /**
         * @return the text
         */
        public String getText() {
            return text;
        }

        /**
         * @param text the text to set
         */
        public void setText(String text) {
            this.text = text;
        }
    }
    
    public static <T extends TokenId> TokenSequence<T> seqForText(String text, Language<T> language) {
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language);
        return hi.tokenSequence(language);
    }

    public static void next(TokenSequence<?> ts, TokenId id, String fixedText) {
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals("Token index[" + ts.index() + "]", ts, id, fixedText, -1);
    }
    
    
    /** This is used for debugging purposes
     * 
     * @param ts
     * @param name
     */
    public static void printTokenSequence (TokenSequence<?> ts, String name) {
        System.out.println("--- " + name + " ---");
        while (ts.moveNext()) {
            System.out.println(ts.token().id()+"\t"+ts.token());
        }
        System.out.println("-----------------------");
    }
    
    public static String getFileContent (File file) throws Exception{
        StringBuffer sb = new StringBuffer();
        String lineSep = "\n";//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }
    
    public static List<LexerResultItem> getExpectedResults (File file) throws Exception {
        List<LexerResultItem> results = new ArrayList<LexerResultItem>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line = br.readLine();
        LexerResultItem resultItem = null;
        while (line != null) {
            if (line.startsWith("#->[")) {
                int index = line.indexOf(':');
                resultItem = null;
                if (index > 0) {
                    String tokenIdName = line.substring(line.indexOf(']') + 1, index);
                    String text = line.substring(index+1);
                    resultItem = new LexerResultItem(PHPTokenId.valueOf(tokenIdName), text);
                    results.add(resultItem);
                }
            }
            else {
               resultItem.setText(resultItem.getText() + "\n" + line);
            }
            line = br.readLine();
        }
        br.close();
        return results;
    }
    
    /**
     * Formats a given string to an XML file
     * @param input 
     * @return String the formatted string
     */
    public static String getXmlStringValue(String input) {
        String escapedString = input;
        escapedString = escapedString.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = escapedString.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = escapedString.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = escapedString.replaceAll("'", "&apos;"); //$NON-NLS-1$ //$NON-NLS-2$
        escapedString = replaceLinesAndTabs(escapedString);
        return escapedString;
    }
    
    public static String replaceLinesAndTabs(String input) {
        String escapedString = input;
        escapedString = escapedString.replaceAll("\n","\\\\n");
        escapedString = escapedString.replaceAll("\r","\\\\r");
        escapedString = escapedString.replaceAll("\t","\\\\t");
        return escapedString;
    }
}
