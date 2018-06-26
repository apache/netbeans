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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
                null,
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
