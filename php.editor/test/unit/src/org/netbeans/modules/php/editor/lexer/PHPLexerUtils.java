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

package org.netbeans.modules.php.editor.lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
