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
package org.netbeans.modules.php.blade.editor;

import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * 
 */
public class BladeBracesMatcherTest extends BladeTestBase {

    public BladeBracesMatcherTest(String testName) {
        super(testName);
    }

    public void testClosedIfDirective_01() throws Exception {
        testMatches("<html>\n"
                + "@if($x)\n"
                + "text\n"
                + "@end^if\n"
                + "</html>");
    }
    
    public void testClosedSectionDirective_01() throws Exception {
        testMatches("<html>\n"
                + "@sec^tion($x)\n"
                + "text\n"
                + "@endsection\n"
                + "</html>");
    }
    
    public void testUnclosedSectionDirective_01() throws Exception {
        testMatches("<html>\n"
                + "@sec^tion($x)\n"
                + "text\n"
                + "@endsec\n"
                + "</html>");
    }

    private void testMatches(String original) throws Exception {
        BracesMatcherFactory factory = MimeLookup.getLookup(getPreferredMimeType()).lookup(BracesMatcherFactory.class);
        int caretPosition = original.indexOf('^');
        assert caretPosition != -1;
        original = original.substring(0, caretPosition) + original.substring(caretPosition + 1);

        BaseDocument doc = getDocument(original);

        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(doc, caretPosition, false, 1);
        BracesMatcher matcher = factory.createMatcher(context);
        int[] origin = null, matches = null;
        try {
            origin = matcher.findOrigin();
            matches = matcher.findMatches();
        } catch (InterruptedException ex) {
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
            System.arraycopy(matches, 0, boundaries, origin.length, matches.length);
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
