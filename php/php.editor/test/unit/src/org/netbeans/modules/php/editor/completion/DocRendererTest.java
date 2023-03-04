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

package org.netbeans.modules.php.editor.completion;

import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.modules.php.editor.completion.DocRenderer.PHPDocExtractor;

public class DocRendererTest extends PHPTestBase {

    public DocRendererTest(String name) {
        super(name);
    }

    public void testProcespsPhpDoc() {
        // text => result
        final Map<String, String> testCases = new LinkedHashMap<>();
        testCases.put(
                "<b>test1</b>",
                "<b>test1</b>");
        testCases.put(
                "<b>te\nst2</b>",
                "<b>te\nst2</b>");
        testCases.put(
                "<b>te\n\r\nst3</b>",
                "<b>te<br><br>st3</b>");
        testCases.put(
                "<b>te\n\n\nst4</b>",
                "<b>te<br><br>st4</b>");
        testCases.put(
                "<b1>test5</ b>",
                "&lt;b1>test5</ b>");
        testCases.put(
                "<input>",
                "&lt;input>");
        // #183594
        testCases.put(
                "List:\n- minus\n+ plus\n# hash\no circle\n3 number\n3. number with dot",
                "List:<br>&nbsp;&nbsp;&nbsp;&nbsp;- minus<br>&nbsp;&nbsp;&nbsp;&nbsp;+ plus<br>&nbsp;&nbsp;&nbsp;&nbsp;# hash<br>&nbsp;&nbsp;&nbsp;&nbsp;o circle<br>&nbsp;&nbsp;&nbsp;&nbsp;3 number<br>&nbsp;&nbsp;&nbsp;&nbsp;3. number with dot");
        testCases.put(
                "NoList:\n-minus\n+plus\n#hash\nocircle\n3-number\n3.number with dot",
                "NoList:\n-minus\n+plus\n#hash\nocircle\n3-number\n3.number with dot");
        testCases.put(
                "NoList:\n/**\n * @assert (0, 0) == 0\n */\n",
                "NoList:\n/**\n * @assert (0, 0) == 0\n */\n");

        for (Map.Entry<String, String> entry : testCases.entrySet()) {
            String expected = entry.getValue();
            String processed = DocRenderer.PHPDocExtractor.processPhpDoc(entry.getKey());
            if (!expected.equals(processed)) {
                System.err.println("[" + expected + "] => [" + processed + "]");
            }
            assertEquals(expected, processed);
        }
    }

    public void testLinksInDescription01() {

        String tested = "Sort the given array of {@link MyObject}s by ORDER field.";
        String expected = "Sort the given array of <a href=\"MyObject\">MyObject</a>s by ORDER field.";

        DocRenderer.PHPDocExtractor extractor = new DocRenderer.PHPDocExtractor(null, null, null, null);

        String result = extractor.processDescription(tested);

        if (!expected.equals(result)) {
            System.err.println("[" + result + "] => [" + expected + "]");
        }
        assertEquals(expected, result);
    }

    public void testHasInlineInheritdoc() {
        assertTrue(PHPDocExtractor.hasInlineInheritdoc("Description {@inheritdoc}"));
        assertTrue(PHPDocExtractor.hasInlineInheritdoc("{@inheritDoc} Child Description."));
        assertTrue(PHPDocExtractor.hasInlineInheritdoc("{@inheritDoc } Child Description."));

        assertFalse(PHPDocExtractor.hasInlineInheritdoc("@inheritdoc"));
        assertFalse(PHPDocExtractor.hasInlineInheritdoc(""));
        assertFalse(PHPDocExtractor.hasInlineInheritdoc(null));
    }

    public void testRemoveDescriptionHeader_01() {
        /**
         * Header of PHPDoc comment.
         */
        checkRemoveDescriptionHeader("", "\nHeader of PHPDoc comment.\n");
    }

    public void testRemoveDescriptionHeader_02() {
        /** Header of PHPDoc comment.
         */
        checkRemoveDescriptionHeader("", "Header of PHPDoc comment.\n");
    }

    public void testRemoveDescriptionHeader_03() {
        /**
         * Header of PHPDoc comment. 
         * Description.
         */
        checkRemoveDescriptionHeader("Description.\n", "\nHeader of PHPDoc comment. \nDescription.\n");
    }

    public void testRemoveDescriptionHeader_04() {
        /**
         * Header of PHPDoc comment
         * Description.
         */
        checkRemoveDescriptionHeader("", "\nHeader of PHPDoc comment\nDescription.\n");
    }

    public void testRemoveDescriptionHeader_05() {
        /**
         * Header of PHPDoc comment
         * Description.
         * Something.
         */
        checkRemoveDescriptionHeader("Something\n", "\nHeader of PHPDoc comment\nDescription.\nSomething\n");
    }

    public void testRemoveDescriptionHeader_06() {
        /**
         * Header of PHPDoc comment
         *
         * Description.
         */
        checkRemoveDescriptionHeader("Description.\n", "\nHeader of PHPDoc comment\n\nDescription.\n");
    }

    public void testRemoveDescriptionHeader_07() {
        /**
         * Header of PHPDoc comment.
         *
         * Description.
         */
        checkRemoveDescriptionHeader("Description.\n", "\nHeader of PHPDoc comment.\n\nDescription.\n");
    }

    // has tags
    public void testRemoveDescriptionHeader_08() {
        /**
         * Header of PHPDoc comment.
         * @param string $parameter description.
         */
        checkRemoveDescriptionHeader("", "\r\nHeader of PHPDoc comment.");
    }

    public void testRemoveDescriptionHeader_09() {
        /** Header of PHPDoc comment.
         * @param string $parameter description.
         */
        checkRemoveDescriptionHeader("", "Header of PHPDoc comment.");
    }

    public void testRemoveDescriptionHeader_10() {
        /**
         * Header of PHPDoc comment.
         * Description.
         * @param string $parameter description.
         */
        checkRemoveDescriptionHeader("Description.", "\nHeader of PHPDoc comment.\nDescription.");
    }

    public void testRemoveDescriptionHeader_11() {
        /**
         * Header of PHPDoc comment
         * Description.
         * @param string $parameter description.
         */
        checkRemoveDescriptionHeader("", "\nHeader of PHPDoc comment\nDescription.");
    }

    public void testRemoveDescriptionHeader_12() {
        /**
         * Header of PHPDoc comment
         * Description.
         * Something.
         * @param string $parameter description.
         */
        checkRemoveDescriptionHeader("Something", "\nHeader of PHPDoc comment\nDescription.\nSomething");
    }

    public void testRemoveDescriptionHeader_13() {
        /**
         * Header of PHPDoc comment
         *
         * Description.
         * @param string $parameter description.
         */
        checkRemoveDescriptionHeader("Description.", "\nHeader of PHPDoc comment\n\nDescription.");
    }

    public void testRemoveDescriptionHeader_14() {
        /**
         * Header of PHPDoc comment.
         *
         * Description.
         * @param string $parameter description.
         */
        checkRemoveDescriptionHeader("Description.", "\nHeader of PHPDoc comment.\n\nDescription.");
    }

    public void testRemoveDescriptionHeader_15() {
        /**
         * Header of PHPDoc comment.
         *
         * Description. Multiple Line
         * Description.
         * Something.
         * @param string $parameter description.
         */
        checkRemoveDescriptionHeader(
                "Description. Multiple Line\nDescription.\nSomething.",
                "\nHeader of PHPDoc comment.\n\nDescription. Multiple Line\nDescription.\nSomething."
        );
    }

    public void testReplaceInlineInheritdoc_01() {
        checkReplaceInlineInheritdoc(
                "Header.\nParent Description. Child Description.",
                "Header.\n{@inheritdoc} Child Description.",
                "Parent Description."
        );
    }

    public void testReplaceInlineInheritdoc_02() {
        checkReplaceInlineInheritdoc(
                "Header.\nParent Description. Child Description. Parent Description.",
                "Header.\n{@inheritdoc} Child Description. {@inheritDoc}",
                "Parent Description."
        );
    }

    public void testReplaceInlineInheritdoc_03() {
        checkReplaceInlineInheritdoc(
                "Header.\n{@inheritdoc} Child Description.",
                "Header.\n{@inheritdoc} Child Description.",
                null
        );
    }

    public void testReplaceInlineInheritdoc_04() {
        checkReplaceInlineInheritdoc(
                "Parent Description",
                null,
                "Parent Description"
        );
    }

    public void testReplaceInlineInheritdoc_05() {
        checkReplaceInlineInheritdoc(
                null,
                null,
                null
        );
    }

    public void testReplaceInlineInheritdoc_06() {
        checkReplaceInlineInheritdoc(
                "Header.\n{@inheritdoc} Child Description.",
                "Header.\n{@inheritdoc} Child Description.",
                ""
        );
    }

    public void testReplaceInlineInheritdoc_07() {
        checkReplaceInlineInheritdoc(
                "Header.\n{@inheritdoc} Child Description.",
                "Header.\n{@inheritdoc} Child Description.",
                "   \r\n   	"
        );
    }

    public void testReplaceInlineInheritdoc_08() {
        checkReplaceInlineInheritdoc(
                "Header.\n@inheritdoc Child Description.",
                "Header.\n@inheritdoc Child Description.",
                "Parent Description."
        );
    }

    // #270415
    public void testReplaceInlineInheritdoc_09() {
        checkReplaceInlineInheritdoc(
                "Header.\n$test Child Description.",
                "Header.\n{@inheritdoc} Child Description.",
                "$test"
        );
    }

    public void testReplaceInlineInheritdoc_10() {
        checkReplaceInlineInheritdoc(
                "Header.\n\\ Child Description.",
                "Header.\n{@inheritdoc} Child Description.",
                "\\"
        );
    }

    public void testReplaceInlineInheritdoc_11() {
        checkReplaceInlineInheritdoc(
                "Header.\n\\Foo\\Bar Child Description.",
                "Header.\n{@inheritdoc} Child Description.",
                "\\Foo\\Bar"
        );
    }

    private void checkRemoveDescriptionHeader(String expected, String description) {
        assertEquals(expected, PHPDocExtractor.removeDescriptionHeader(description));
    }

    private void checkReplaceInlineInheritdoc(String expected, String description, String inheritdoc) {
        assertEquals(expected, PHPDocExtractor.replaceInlineInheritdoc(description, inheritdoc));
    }

}
