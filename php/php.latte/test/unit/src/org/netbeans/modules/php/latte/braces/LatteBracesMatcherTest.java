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

package org.netbeans.modules.php.latte.braces;

import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.modules.php.latte.LatteTestBase;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteBracesMatcherTest extends LatteTestBase {

    public LatteBracesMatcherTest(String testName) {
        super(testName);
    }

    public void testDefine() throws Exception {
        testMatches("<html>\n"
                + "{def^ine}\n"
                + "text\n"
                + "{/define}\n"
                + "</html>");
    }

    public void testEndDefine() throws Exception {
        testMatches("<html>\n"
                + "{define}\n"
                + "text\n"
                + "{/^define}\n"
                + "</html>");
    }

    public void testIfCurrent() throws Exception {
        testMatches("<html>\n"
                + "{if^Current}\n"
                + "text\n"
                + "{/ifCurrent}\n"
                + "</html>");
    }

    public void testEndIfCurrent() throws Exception {
        testMatches("<html>\n"
                + "{ifCurrent}\n"
                + "text\n"
                + "{/^ifCurrent}\n"
                + "</html>");
    }

    public void testFor() throws Exception {
        testMatches("<html>\n"
                + "{f^or}\n"
                + "text\n"
                + "{/for}\n"
                + "</html>");
    }

    public void testEndFor() throws Exception {
        testMatches("<html>\n"
                + "{for}\n"
                + "text\n"
                + "{/^for}\n"
                + "</html>");
    }

    public void testForeach() throws Exception {
        testMatches("<html>\n"
                + "{f^oreach}\n"
                + "text\n"
                + "{/foreach}\n"
                + "</html>");
    }

    public void testEndForeach() throws Exception {
        testMatches("<html>\n"
                + "{foreach}\n"
                + "text\n"
                + "{/^foreach}\n"
                + "</html>");
    }

    public void testWhile() throws Exception {
        testMatches("<html>\n"
                + "{whi^le}\n"
                + "text\n"
                + "{/while}\n"
                + "</html>");
    }

    public void testEndWhile() throws Exception {
        testMatches("<html>\n"
                + "{while}\n"
                + "text\n"
                + "{/^while}\n"
                + "</html>");
    }

    public void testFirst() throws Exception {
        testMatches("<html>\n"
                + "{fir^st}\n"
                + "text\n"
                + "{/first}\n"
                + "</html>");
    }

    public void testEndFirst() throws Exception {
        testMatches("<html>\n"
                + "{first}\n"
                + "text\n"
                + "{/^first}\n"
                + "</html>");
    }

    public void testLast() throws Exception {
        testMatches("<html>\n"
                + "{la^st}\n"
                + "text\n"
                + "{/last}\n"
                + "</html>");
    }

    public void testEndLast() throws Exception {
        testMatches("<html>\n"
                + "{last}\n"
                + "text\n"
                + "{/^last}\n"
                + "</html>");
    }

    public void testSep() throws Exception {
        testMatches("<html>\n"
                + "{s^ep}\n"
                + "text\n"
                + "{/sep}\n"
                + "</html>");
    }

    public void testEndSep() throws Exception {
        testMatches("<html>\n"
                + "{sep}\n"
                + "text\n"
                + "{/^sep}\n"
                + "</html>");
    }

    public void testCapture() throws Exception {
        testMatches("<html>\n"
                + "{cap^ture}\n"
                + "text\n"
                + "{/capture}\n"
                + "</html>");
    }

    public void testEndCapture() throws Exception {
        testMatches("<html>\n"
                + "{capture}\n"
                + "text\n"
                + "{/^capture}\n"
                + "</html>");
    }

    public void testCache() throws Exception {
        testMatches("<html>\n"
                + "{ca^che}\n"
                + "text\n"
                + "{/cache}\n"
                + "</html>");
    }

    public void testEndCache() throws Exception {
        testMatches("<html>\n"
                + "{cache}\n"
                + "text\n"
                + "{/^cache}\n"
                + "</html>");
    }

    public void testSyntax() throws Exception {
        testMatches("<html>\n"
                + "{syn^tax}\n"
                + "text\n"
                + "{/syntax}\n"
                + "</html>");
    }

    public void testEndSyntax() throws Exception {
        testMatches("<html>\n"
                + "{syntax}\n"
                + "text\n"
                + "{/^syntax}\n"
                + "</html>");
    }

    public void testUnderscope() throws Exception {
        testMatches("<html>\n"
                + "{^_}\n"
                + "text\n"
                + "{/_}\n"
                + "</html>");
    }

    public void testEndUnderscope() throws Exception {
        testMatches("<html>\n"
                + "{_}\n"
                + "text\n"
                + "{/^_}\n"
                + "</html>");
    }

    public void testBlock() throws Exception {
        testMatches("<html>\n"
                + "{bl^ock}\n"
                + "text\n"
                + "{/block}\n"
                + "</html>");
    }

    public void testEndBlock() throws Exception {
        testMatches("<html>\n"
                + "{block}\n"
                + "text\n"
                + "{/^block}\n"
                + "</html>");
    }

    public void testForm() throws Exception {
        testMatches("<html>\n"
                + "{fo^rm}\n"
                + "text\n"
                + "{/form}\n"
                + "</html>");
    }

    public void testEndForm() throws Exception {
        testMatches("<html>\n"
                + "{form}\n"
                + "text\n"
                + "{/^form}\n"
                + "</html>");
    }

    public void testLabel() throws Exception {
        testMatches("<html>\n"
                + "{la^bel}\n"
                + "text\n"
                + "{/label}\n"
                + "</html>");
    }

    public void testEndLabel() throws Exception {
        testMatches("<html>\n"
                + "{label}\n"
                + "text\n"
                + "{/^label}\n"
                + "</html>");
    }

    public void testSnippet() throws Exception {
        testMatches("<html>\n"
                + "{snip^pet}\n"
                + "text\n"
                + "{/snippet}\n"
                + "</html>");
    }

    public void testEndSnippet() throws Exception {
        testMatches("<html>\n"
                + "{snippet}\n"
                + "text\n"
                + "{/^snippet}\n"
                + "</html>");
    }

    public void testIf_01() throws Exception {
        testMatches("<html>\n"
                + "{i^f true }\n"
                + "text\n"
                + "{elseif true}\n"
                + "text\n"
                + "{else}\n"
                + "text\n"
                + "{/if}\n"
                + "</html>");
    }

    public void testIf_02() throws Exception {
        testMatches("<html>\n"
                + "{if true }\n"
                + "text\n"
                + "{else^if true}\n"
                + "text\n"
                + "{else}\n"
                + "text\n"
                + "{/if}\n"
                + "</html>");
    }

    public void testIf_03() throws Exception {
        testMatches("<html>\n"
                + "{if true }\n"
                + "text\n"
                + "{elseif true}\n"
                + "text\n"
                + "{el^se}\n"
                + "text\n"
                + "{/if}\n"
                + "</html>");
    }

    public void testIf_04() throws Exception {
        testMatches("<html>\n"
                + "{if true }\n"
                + "text\n"
                + "{elseif true}\n"
                + "text\n"
                + "{else}\n"
                + "text\n"
                + "{/i^f}\n"
                + "</html>");
    }

    public void testIfset_01() throws Exception {
        testMatches("<html>\n"
                + "{i^fset true }\n"
                + "text\n"
                + "{elseifset true}\n"
                + "text\n"
                + "{else}\n"
                + "text\n"
                + "{/ifset}\n"
                + "</html>");
    }

    public void testIfset_02() throws Exception {
        testMatches("<html>\n"
                + "{ifset true }\n"
                + "text\n"
                + "{else^ifset true}\n"
                + "text\n"
                + "{else}\n"
                + "text\n"
                + "{/ifset}\n"
                + "</html>");
    }

    public void testIfset_03() throws Exception {
        testMatches("<html>\n"
                + "{ifset true }\n"
                + "text\n"
                + "{elseifset true}\n"
                + "text\n"
                + "{el^se}\n"
                + "text\n"
                + "{/ifset}\n"
                + "</html>");
    }

    public void testIfset_04() throws Exception {
        testMatches("<html>\n"
                + "{ifset true }\n"
                + "text\n"
                + "{elseifset true}\n"
                + "text\n"
                + "{else}\n"
                + "text\n"
                + "{/i^fset}\n"
                + "</html>");
    }

    public void testNested_01() throws Exception {
        testMatches("<html>\n"
                + "{def^ine}\n"
                + "{define}\n"
                + "{define}\n"
                + "asd\n"
                + "{/define}\n"
                + "{/define}\n"
                + "{/define}\n"
                + "</html>");
    }

    public void testNested_02() throws Exception {
        testMatches("<html>\n"
                + "{define}\n"
                + "{def^ine}\n"
                + "{define}\n"
                + "asd\n"
                + "{/define}\n"
                + "{/define}\n"
                + "{/define}\n"
                + "</html>");
    }

    public void testNested_03() throws Exception {
        testMatches("<html>\n"
                + "{define}\n"
                + "{define}\n"
                + "{def^ine}\n"
                + "asd\n"
                + "{/define}\n"
                + "{/define}\n"
                + "{/define}\n"
                + "</html>");
    }

    public void testNested_04() throws Exception {
        testMatches("<html>\n"
                + "{define}\n"
                + "{define}\n"
                + "{define}\n"
                + "asd\n"
                + "{/def^ine}\n"
                + "{/define}\n"
                + "{/define}\n"
                + "</html>");
    }

    public void testNested_05() throws Exception {
        testMatches("<html>\n"
                + "{define}\n"
                + "{define}\n"
                + "{define}\n"
                + "asd\n"
                + "{/define}\n"
                + "{/def^ine}\n"
                + "{/define}\n"
                + "</html>");
    }

    public void testNested_06() throws Exception {
        testMatches("<html>\n"
                + "{define}\n"
                + "{define}\n"
                + "{define}\n"
                + "asd\n"
                + "{/define}\n"
                + "{/define}\n"
                + "{/def^ine}\n"
                + "</html>");
    }

    public void testNested_07() throws Exception {
        testMatches("<html>\n"
                + "{def^ine}\n"
                + "{/define}\n"
                + "text\n"
                + "{define}\n"
                + "{/define}\n"
                + "</html>");
    }

    public void testNested_08() throws Exception {
        testMatches("<html>\n"
                + "{define}\n"
                + "{/def^ine}\n"
                + "text\n"
                + "{define}\n"
                + "{/define}\n"
                + "</html>");
    }

    public void testNested_09() throws Exception {
        testMatches("<html>\n"
                + "{define}\n"
                + "{/define}\n"
                + "text\n"
                + "{def^ine}\n"
                + "{/define}\n"
                + "</html>");
    }

    public void testNested_10() throws Exception {
        testMatches("<html>\n"
                + "{define}\n"
                + "{/define}\n"
                + "text\n"
                + "{define}\n"
                + "{/def^ine}\n"
                + "</html>");
    }

    public void testIssueGH5862_01() throws Exception {
        // no golden file
        testMatches("<html>\n"
                + "<p>{^_'test'}</p>\n"
                + "</html>",
                true
        );
    }

    public void testIssueGH5862_02() throws Exception {
        testMatches("<html>\n"
                + "<p>{tr^anslate}Test{/translate}</p>\n"
                + "</html>");
    }

    public void testIssueGH5862_03() throws Exception {
        testMatches("<html>\n"
                + "<p>{translate}Test{/transl^ate}</p>\n"
                + "</html>");
    }

    public void testIssueGH5862_04() throws Exception {
        testMatches("<html>\n"
                + "<p>{transl^ate domain: order}Test{/translate}</p>\n"
                + "</html>");
    }

    public void testIssueGH5862_05() throws Exception {
        testMatches("<html>\n"
                + "<p>{translate domain: order}Test{/transla^te}</p>\n"
                + "</html>");
    }

    public void testIssueGH5862_06() throws Exception {
        // no goleden file
        testMatches("<html>\n"
                + "<p>{translate dom^ain: order}Test{/translate}</p>\n"
                + "</html>",
                true
        );
    }

    private void testMatches(String originalWithCaret) throws Exception {
        testMatches(originalWithCaret, false);
    }

    private void testMatches(String originalWithCaret, boolean notFoundOrigin) throws Exception {
        BracesMatcherFactory factory = MimeLookup.getLookup(getPreferredMimeType()).lookup(BracesMatcherFactory.class);
        int caretPosition = originalWithCaret.indexOf('^');
        assert caretPosition != -1;
        String original = originalWithCaret.substring(0, caretPosition) + originalWithCaret.substring(caretPosition + 1);

        BaseDocument doc = getDocument(original);

        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(doc, caretPosition, false, 1);
        BracesMatcher matcher = factory.createMatcher(context);
        int[] origin = null, matches = null;
        try {
            origin = matcher.findOrigin();
            matches = matcher.findMatches();
        } catch (InterruptedException ex) {
        }
        if (notFoundOrigin && origin == null) {
            return;
        }

        assertNotNull("Did not find origin", origin);
        assertEquals("Wrong origin length", 2, origin.length);

        int matchesLength = 0;
        if (matches != null) {
            matchesLength = matches.length;
        }
        int[] boundaries = new int[origin.length + matchesLength];
        System.arraycopy(origin, 0, boundaries, 0, origin.length);
        if (matchesLength != 0) {
            System.arraycopy(matches, 0, boundaries, origin.length, matchesLength);
        }

        Integer[] boundariesIntegers = new Integer[boundaries.length];
        for (int i = 0; i < boundaries.length; i++) {
            boundariesIntegers[i] = boundaries[i];
        }
        Arrays.sort(boundariesIntegers, Collections.reverseOrder());
        String expected = original;
        boolean caretInserted = false;
        for (int i : boundariesIntegers) {
            if (i <= caretPosition && !caretInserted) {
                expected = expected.substring(0, caretPosition) + "^" + expected.substring(caretPosition);
                caretInserted = true;
            }
            expected = expected.substring(0, i) + "*" + expected.substring(i);
        }
        assertDescriptionMatches("testfiles/braces/" + getName(), expected, true, ".braces", false);
    }

}
