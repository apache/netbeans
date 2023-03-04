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
package org.netbeans.modules.mercurial.ui.annotate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AnnotateActionTest {

    private static class AnnotateTestCase {
        private final String input;
        private final String expectedUser;
        private final String exectedRevision;
        private final String expectedFilename;
        private final int expectedLineNumber;
        private final String expectedContent;

        public AnnotateTestCase(String input, String expectedUser, String exectedRevision, String expectedFilename, int expectedLineNumber, String expectedContent) {
            this.input = input;
            this.expectedUser = expectedUser;
            this.exectedRevision = exectedRevision;
            this.expectedFilename = expectedFilename;
            this.expectedLineNumber = expectedLineNumber;
            this.expectedContent = expectedContent;
        }

        public void runTest(int idx) {
            AnnotateLine[] resultLines = AnnotateAction.toAnnotateLines(Collections.singletonList(input));
            assertNotNull(resultLines);
            assertEquals(1, resultLines.length);
            AnnotateLine result = resultLines[0];
            assertNotNull(result);

            assertFalse("Testcase " + idx + " resulted in FakeAnnotationLine", result.getClass().getName().contains("FakeAnnotationLine"));
            assertEquals("Testcase " + idx, expectedUser, result.getUsername());
            assertEquals("Testcase " + idx, expectedFilename, result.getFileName());
            assertEquals("Testcase " + idx, exectedRevision, result.getRevision());
            assertEquals("Testcase " + idx, expectedLineNumber, result.getPreviousLineNumber());
            assertEquals("Testcase " + idx, expectedContent, result.getContent());
        }
    }

    @Test
    public void testToAnnotateLines() {
        List<AnnotateTestCase> testInput = new ArrayList<>();
        /*

        Testcases were generated on Ubuntu 18.04 and debian Jessie (oldstable
        by the time of creation). Outputs are marked with a leading ">"

        ------------------------------------------------------------------------

        mkdir sampledir
        cd sampledir

        hg --version
        
        > Mercurial Distributed SCM (version 4.5.3)
        > (see https://mercurial-scm.org for more information)

        > Mercurial Distributed SCM (version 3.1.2)
        > (siehe http://mercurial.selenic.com f?r mehr Information)

        hg init
        echo "Simplefile" > simplefile
        echo "File with space" > file\ with\ space
        echo "File with colon" > file\ \:\ with\ colon
        echo "File with leading dot" > .fileWithLeadingDot
        echo "File with leading and trailing space" > " leading and trailing space "
        hg add simplefile file\ with\ space file\ \:\ with\ colon .fileWithLeadingDot " leading and trailing space "
        hg commit -m "Simple user" -u "simpleuser"
        hg annotate --repository . --number --user --line-number --follow simplefile file\ with\ space file\ \:\ with\ colon .fileWithLeadingDot " leading and trailing space "

        > simpleuser 0  leading and trailing space :1: File with leading and trailing space
        > simpleuser 0 .fileWithLeadingDot:1: File with leading dot
        > simpleuser 0 file : with colon:1: File with colon
        > simpleuser 0 file with space:1: File with space
        > simpleuser 0 simplefile:1: Simplefile

        echo "User with colon" > simplefile
        echo "User with colon" > file\ with\ space
        echo "User with colon" > file\ \:\ with\ colon
        echo "User with colon" > .fileWithLeadingDot
        echo "User with colon" > " leading and trailing space "
        hg commit -m "User with colon" -u "simple:user"
        hg annotate --repository . --number --user --line-number --follow simplefile file\ with\ space file\ \:\ with\ colon .fileWithLeadingDot " leading and trailing space "

        > simple:user 1  leading and trailing space :1: User with colon
        > simple:user 1 .fileWithLeadingDot:1: User with colon
        > simple:user 1 file : with colon:1: User with colon
        > simple:user 1 file with space:1: User with colon
        > simple:user 1 simplefile:1: User with colon

        echo "User with colon and space" > simplefile
        echo "User with colon and space" > file\ with\ space
        echo "User with colon and space" > file\ \:\ with\ colon
        echo "User with colon and space" > .fileWithLeadingDot
        echo "User with colon and space" > " leading and trailing space "
        hg commit -m "User with colon and space" -u "simple : user"
        hg annotate --repository . --number --user --line-number --follow simplefile file\ with\ space file\ \:\ with\ colon .fileWithLeadingDot " leading and trailing space "

        > simple 2  leading and trailing space :1: User with colon and space
        > simple 2 .fileWithLeadingDot:1: User with colon and space
        > simple 2 file : with colon:1: User with colon and space
        > simple 2 file with space:1: User with colon and space
        > simple 2 simplefile:1: User with colon and space

        echo "User with colon and space 2" > simplefile
        echo "User with colon and space 2" > file\ with\ space
        echo "User with colon and space 2" > file\ \:\ with\ colon
        echo "User with colon and space 2" > .fileWithLeadingDot
        echo "User with colon and space 2" > " leading and trailing space "
        hg commit -m "User with colon and space 2" -u "simple\ :\ user"
        hg annotate --repository . --number --user --line-number --follow simplefile file\ with\ space file\ \:\ with\ colon .fileWithLeadingDot " leading and trailing space "

        > simple\ 3  leading and trailing space :1: User with colon and space 2
        > simple\ 3 .fileWithLeadingDot:1: User with colon and space 2
        > simple\ 3 file : with colon:1: User with colon and space 2
        > simple\ 3 file with space:1: User with colon and space 2
        > simple\ 3 simplefile:1: User with colon and space 2

        echo "Complex user" > simplefile
        echo "Complex user" > file\ with\ space
        echo "Complex user" > file\ \:\ with\ colon
        echo "Complex user" > .fileWithLeadingDot
        echo "Complex user" > " leading and trailing space "
        hg commit -m "Complex User" -u "Complex User <complex:email@testdomain.invalid>"
        hg annotate --repository . --number --user --line-number --follow simplefile file\ with\ space file\ \:\ with\ colon .fileWithLeadingDot " leading and trailing space "

        > complex:email 4  leading and trailing space :1: Complex user
        > complex:email 4 .fileWithLeadingDot:1: Complex user
        > complex:email 4 file : with colon:1: Complex user
        > complex:email 4 file with space:1: Complex user
        > complex:email 4 simplefile:1: Complex user

        ------------------------------------------------------------------------

         */
        testInput.add(new AnnotateTestCase("simpleuser 0 simplefile:1: Simplefile", "simpleuser", "0", "simplefile", 1, "Simplefile"));
        testInput.add(new AnnotateTestCase("simpleuser 0 file with space:1: File with space", "simpleuser", "0", "file with space", 1, "File with space"));
        testInput.add(new AnnotateTestCase("simpleuser 0 file : with colon:1: File with colon", "simpleuser", "0", "file : with colon", 1, "File with colon"));
        testInput.add(new AnnotateTestCase("simpleuser 0 .fileWithLeadingDot:1: File with leading dot", "simpleuser", "0", ".fileWithLeadingDot", 1, "File with leading dot"));
        testInput.add(new AnnotateTestCase("simpleuser 0  leading and trailing space :1: File with leading and trailing space", "simpleuser", "0", " leading and trailing space ", 1, "File with leading and trailing space"));
        testInput.add(new AnnotateTestCase("simple:user 1 simplefile:1: User with colon", "simple:user", "1", "simplefile", 1, "User with colon"));
        testInput.add(new AnnotateTestCase("simple:user 1 file with space:1: User with colon", "simple:user", "1", "file with space", 1, "User with colon"));
        testInput.add(new AnnotateTestCase("simple:user 1 file : with colon:1: User with colon", "simple:user", "1", "file : with colon", 1, "User with colon"));
        testInput.add(new AnnotateTestCase("simple:user 1 .fileWithLeadingDot:1: User with colon", "simple:user", "1", ".fileWithLeadingDot", 1, "User with colon"));
        testInput.add(new AnnotateTestCase("simple:user 1  leading and trailing space :1: User with colon", "simple:user", "1", " leading and trailing space ", 1, "User with colon"));
        testInput.add(new AnnotateTestCase("simple 2 simplefile:1: User with colon and space", "simple", "2", "simplefile", 1, "User with colon and space"));
        testInput.add(new AnnotateTestCase("simple 2 file with space:1: User with colon and space", "simple", "2", "file with space", 1, "User with colon and space"));
        testInput.add(new AnnotateTestCase("simple 2 file : with colon:1: User with colon and space", "simple", "2", "file : with colon", 1, "User with colon and space"));
        testInput.add(new AnnotateTestCase("simple 2 .fileWithLeadingDot:1: User with colon and space", "simple", "2", ".fileWithLeadingDot", 1, "User with colon and space"));
        testInput.add(new AnnotateTestCase("simple 2  leading and trailing space :1: User with colon and space", "simple", "2", " leading and trailing space ", 1, "User with colon and space"));
        testInput.add(new AnnotateTestCase("simple\\ 3 simplefile:1: User with colon and space 2", "simple\\", "3", "simplefile", 1, "User with colon and space 2"));
        testInput.add(new AnnotateTestCase("simple\\ 3 file with space:1: User with colon and space 2", "simple\\", "3", "file with space", 1, "User with colon and space 2"));
        testInput.add(new AnnotateTestCase("simple\\ 3 file : with colon:1: User with colon and space 2", "simple\\", "3", "file : with colon", 1, "User with colon and space 2"));
        testInput.add(new AnnotateTestCase("simple\\ 3 .fileWithLeadingDot:1: User with colon and space 2", "simple\\", "3", ".fileWithLeadingDot", 1, "User with colon and space 2"));
        testInput.add(new AnnotateTestCase("simple\\ 3  leading and trailing space :1: User with colon and space 2", "simple\\", "3", " leading and trailing space ", 1, "User with colon and space 2"));
        testInput.add(new AnnotateTestCase("complex:email 4 simplefile:1: Complex user", "complex:email", "4", "simplefile", 1, "Complex user"));
        testInput.add(new AnnotateTestCase("complex:email 4 file with space:1: Complex user", "complex:email", "4", "file with space", 1, "Complex user"));
        testInput.add(new AnnotateTestCase("complex:email 4 file : with colon:1: Complex user", "complex:email", "4", "file : with colon", 1, "Complex user"));
        testInput.add(new AnnotateTestCase("complex:email 4 .fileWithLeadingDot:1: Complex user", "complex:email", "4", ".fileWithLeadingDot", 1, "Complex user"));
        testInput.add(new AnnotateTestCase("complex:email 4  leading and trailing space :1: Complex user", "complex:email", "4", " leading and trailing space ", 1, "Complex user"));
        // One verification from the netbeans old codebase
        testInput.add(new AnnotateTestCase("      jglick   2218 nbbuild/build.xml:   2: <!--", "jglick", "2218", "nbbuild/build.xml", 2, "<!--"));
        // NETBEANS-643
        testInput.add(new AnnotateTestCase("foo 8731 foo/ba r.baz: 98: Stuff", "foo", "8731", "foo/ba r.baz", 98, "Stuff"));
        testInput.add(new AnnotateTestCase("foo 8731 .foo/bar.baz: 98: Stuff", "foo", "8731", ".foo/bar.baz", 98, "Stuff"));

        for(int i = 0; i < testInput.size(); i++) {
            testInput.get(i).runTest(i);
        }
    }

}
