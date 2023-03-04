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
package org.netbeans.modules.spellchecker.bindings.php;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import static junit.framework.TestCase.assertEquals;
import org.junit.Test;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.spellchecker.spi.language.TokenList;

/**
 * Based on JavaTokenListTest.
 */
public class PHPTokenListTest extends NbTestCase {

    private static final String PHPTAG = "<?php\n";

    public PHPTokenListTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 10000;
    }

    public void testSimpleWordBroker() throws Exception {
        tokenListTest(
                "  /**\n"
                + " * test description.\n"
                + " */\n"
                + "/* multi line comment */"
                + "/** apple orange*/",
                "test", "description", "apple", "orange");
    }

    public void testPairTags() throws Exception {
        tokenListTest(
                "/** tes <code>test</code> <pre>testt</pre> <a href='testtt'>testttt</a> testttttt*/",
                "tes", "testttttt"
        );
    }

    public void testMultiLineComment() throws Exception {
        tokenListTest(
                "  /*\n"
                + " * test description.\n"
                + " */"
        );
    }

    public void testSimplewriting() throws Exception {
        tokenListTestWithWriting(
                "/** tes test*/ testt testtt /*testttt*//** testtttt*//** testttttt*/",
                15, "bflmpsvz", 14,
                "testtttt", "testttttt"
        );
    }

    public void testDot() throws Exception {
        tokenListTest(
                "/** tes.test */",
                "tes", "test"
        );
    }

    public void testTagHandling() throws Exception {
        tokenListTest(
                "  /**\n"
                + " * @see http://example.com/my/bar/qwertyuio aaa.\n"
                + " * @see MyClass::$items aab .\n"
                + " * @author abi abj abk abl\n"
                + " * @something array|int somethingdesc.\n"
                + " */",
                "aaa", "aab", "array", "int", "somethingdesc"
        );
    }

    public void testTypeTagHandling() throws Exception {
        tokenListTest(
                "  /**\n"
                + " * @param string|int $a paramdesc.\n"
                + " * @param string|int $b \n"
                + " * @property MyClass $myClass propertydesc.\n"
                + " * @property-read Foo $foo propertyrdesc.\n"
                + " * @property-write Bar $bar propertywdesc.\n"
                + " * @var array vardesc\n"
                + " * @return $this|int returndesc\n"
                + " */",
                "paramdesc", "propertydesc", "propertyrdesc", "propertywdesc", "vardesc", "returndesc"
        );
    }

    public void testIssue257953() throws Exception {
        tokenListTest(
                "  /**\n"
                + " * @param\n"
                + " */"
        );
    }

    public void testMethodTagHandling() throws Exception {
        tokenListTest(
                "  /**\n"
                + " * @method int aaa() methoddesca.\n"
                + " * @method int aaa_bbb() methoddescb.\n"
                + " * @method int aaa_bbb2() methoddescc.\n"
                + " * @method int bbb(array $a, $b) methoddescd.\n"
                + " * @method string[] ccc() ccc(array $a, $b) methoddesce.\n"
                + " */",
                "methoddesca", "methoddescb", "methoddescc", "methoddescd", "methoddesce"
        );
    }

    public void testLinkHandling() throws Exception {
        tokenListTest(
                "/** {@link aba abb abc} {abd }abe*/",
                "abd", "abe"
        );
    }

    public void testInheritdocHandling() throws Exception {
        tokenListTest(
                "/** {@inheritdoc} child. */",
                "child"
        );
    }

    public void testVariableHandling() throws Exception {
        tokenListTest(
                "/** something $var variable $var_name */",
                "something", "variable"
        );
    }

    public void testEntities() throws Exception {
        tokenListTest(
                "/** &gt; &#62; */"
        );
    }

    public void testIssue257977() throws Exception {
        tokenListTest(
                "  /**\n"
                + " * @since   2016-02-12\n"
                + " *\n"
                + " * Test\n"
                + " */",
                "Test"
        );
    }

    public void testPositions() throws Exception {
        Document doc = new PlainDocument();
        doc.putProperty(Language.class, PHPTokenId.language());
        doc.insertString(0, "<?php /** tes test <pre>testt</pre> <a href='testtt'>testttt</a> testttttt*/", null);
        TokenList tokenList = new PHPTokenList(doc);
        tokenList.setStartOffset(16); // <?php /** tes te|st
        assertTrue(tokenList.nextWord());
        assertEquals(14, tokenList.getCurrentWordStartOffset()); // <?php /** tes |test
        assertTrue("test".equals(tokenList.getCurrentWordText().toString()));
    }

    /**
     * Test of isIdentifierLike method, of class PHPTokenList.
     */
    @Test
    public void testIsIdentifierLike() {
        assertTrue(PHPTokenList.isIdentifierLike("ArrayAccess"));
        assertTrue(PHPTokenList.isIdentifierLike("getData"));
        assertTrue(PHPTokenList.isIdentifierLike("setTestingData"));

        assertFalse(PHPTokenList.isIdentifierLike("test"));
        assertFalse(PHPTokenList.isIdentifierLike("code"));
        assertFalse(PHPTokenList.isIdentifierLike("data"));
    }

    /**
     * Check a token list.
     *
     * @param documentContent
     * @param golden expected words
     * @throws Exception
     */
    private void tokenListTest(String documentContent, String... golden) throws Exception {
        Document doc = new PlainDocument();
        doc.putProperty(Language.class, PHPTokenId.language());
        doc.insertString(0, PHPTAG + documentContent, null);
        List<String> words = new ArrayList<>();
        TokenList tokenList = new PHPTokenList(doc);
        tokenList.setStartOffset(0);
        while (tokenList.nextWord()) {
            words.add(tokenList.getCurrentWordText().toString());
        }
        assertEquals(Arrays.asList(golden), words);
    }

    /**
     * Check a token list with writing.
     *
     * @param documentContent
     * @param offset
     * @param text
     * @param startOffset
     * @param golden expected words
     * @throws Exception
     */
    private void tokenListTestWithWriting(String documentContent, int offset, String text, int startOffset, String... golden) throws Exception {
        Document doc = new PlainDocument();
        doc.putProperty(Language.class, PHPTokenId.language());
        doc.insertString(0, PHPTAG + documentContent, null);
        List<String> words = new ArrayList<>();
        TokenList tokenList = new PHPTokenList(doc);
        while (tokenList.nextWord()) {
        }

        doc.insertString(offset + PHPTAG.length(), text, null);
        tokenList.setStartOffset(startOffset + PHPTAG.length());
        while (tokenList.nextWord()) {
            words.add(tokenList.getCurrentWordText().toString());
        }
        assertEquals(Arrays.asList(golden), words);
    }
}
