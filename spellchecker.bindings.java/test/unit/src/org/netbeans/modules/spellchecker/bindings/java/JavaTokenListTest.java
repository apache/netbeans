/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.spellchecker.bindings.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.spellchecker.spi.language.TokenList;

/**
 *
 * @author Jan Lahoda
 */
public class JavaTokenListTest extends NbTestCase {
    
    public JavaTokenListTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testSimpleWordBroker() throws Exception {
        tokenListTest(
            "/**tes test*/ testt testtt /*testttt*//**testtttt*//**testttttt*/",
            "tes", "test", "testtttt", "testttttt"
        );
    }

    public void testPairTags() throws Exception {
        tokenListTest(
            "/**tes <code>test</code> <pre>testt</pre> <a href='testtt'>testttt</a> testttttt*/",
            "tes", "testttttt"
        );
    }

    public void testSimplewriting() throws Exception {
        tokenListTestWithWriting(
            "/**tes test*/ testt testtt /*testttt*//**testtttt*//**testttttt*/",
            14, "bflmpsvz", 13,
            "testtttt", "testttttt"
        );
    }

    public void testDotDoesNotSeparateWords() throws Exception {
        tokenListTest(
                "/**tes.test*/",
                "tes", "test"
        );
    }
    
    public void testTagHandling() throws Exception {
        tokenListTest(
                "/**@see aba.abb.abc.abd abe @param abf abg abh @author abi abj abk abl\n abm abn @throws abo.abp abq*/",
                "abe", "abg", "abh", "abm", "abn", "abq"
        );
    }
    
    public void testLinkHandling() throws Exception {
        tokenListTest(
                "/**{@link aba abb abc} {abd }abe*/",
                "abd", "abe"
        );
    }

    public void testEntities() throws Exception {
        tokenListTest(
                "/** &gt; &#62; */"
        );
    }
    
    public void testIsIdentifierLike() throws Exception {
        assertTrue(JavaTokenList.isIdentifierLike("JTable"));
        assertTrue(JavaTokenList.isIdentifierLike("getData"));
        assertTrue(JavaTokenList.isIdentifierLike("setTestingData"));

        assertFalse(JavaTokenList.isIdentifierLike("test"));
        assertFalse(JavaTokenList.isIdentifierLike("code"));
        assertFalse(JavaTokenList.isIdentifierLike("data"));
    }

    public void testPositions() throws Exception {
        Document doc = new PlainDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        doc.insertString(0, "/**tes test <pre>testt</pre> <a href='testtt'>testttt</a> testttttt*/", null);
        
        TokenList l = new JavaTokenList(doc);
        
        l.setStartOffset(9);
        assertTrue(l.nextWord());
        assertEquals(7, l.getCurrentWordStartOffset());
        assertTrue("test".equals(l.getCurrentWordText().toString()));
    }
    
    private void tokenListTest(String documentContent, String... golden) throws Exception {
        Document doc = new PlainDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        doc.insertString(0, documentContent, null);
        
        List<String> words = new ArrayList<String>();
        TokenList l = new JavaTokenList(doc);
        
        l.setStartOffset(0);
        
        while (l.nextWord()) {
            words.add(l.getCurrentWordText().toString());
        }
        
        assertEquals(Arrays.asList(golden), words);
    }

    private void tokenListTestWithWriting(String documentContent, int offset, String text, int startOffset, String... golden) throws Exception {
        Document doc = new PlainDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        doc.insertString(0, documentContent, null);
        
        List<String> words = new ArrayList<String>();
        TokenList l = new JavaTokenList(doc);
        
        while (l.nextWord()) {
        }

        doc.insertString(offset, text, null);
        
        l.setStartOffset(startOffset);
        
        while (l.nextWord()) {
            words.add(l.getCurrentWordText().toString());
        }

        assertEquals(Arrays.asList(golden), words);
    }

}
