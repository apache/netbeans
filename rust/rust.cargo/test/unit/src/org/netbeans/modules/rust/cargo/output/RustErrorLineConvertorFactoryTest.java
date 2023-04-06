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
package org.netbeans.modules.rust.cargo.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author antonio
 */
public class RustErrorLineConvertorFactoryTest extends NbTestCase {

    public RustErrorLineConvertorFactoryTest() {
        super("RustErrorLineConvertorFactoryTest");
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testShouldDetectHyperlinks() throws Exception {
        System.out.println("testShouldDetectHyperlinks");

        // Given some error lines with a proper source position
        String[] lines = {
            " --> src/main.rs:8:17",
            "  --> src/main.rs:17:12",
            "  --> src/main.rs:29:8"
        };

        for (String line : lines) {
            // When we try to find the position
            Matcher matcher = RustErrorHyperlinkConvertorFactory.matchesSourcePosition(line);

            // Then the position is found
            assertTrue("This line could not be detected: " + line, matcher.matches());
            assertEquals(
                    "src/main.rs", matcher.group(1));
            int nLine = Integer.parseInt(matcher.group(2));
            assertEquals("" + nLine, matcher.group(2));
            int nCol = Integer.parseInt(matcher.group(3));
            assertEquals("" + nCol, matcher.group(3));
        }
    }

    @Test
    public void testShouldDetectHyperlinksInTestFile() throws Exception {
        System.out.println("testShouldDetectHyperlinksInTestFile");
        File test = new File(getDataDir(), "rust-output-with-errors-1.txt");

        int hyperlinkCount = 0;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(test), StandardCharsets.UTF_8))) {
            do {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                lineNumber ++;
                if (RustErrorHyperlinkConvertorFactory.matchesSourcePosition(line).matches()) {
                    hyperlinkCount++;
                    // DEBUG:
                    // System.out.format("LINE %3d:'%s'%n", lineNumber, line);
                }
            } while (true);
        }
        assertEquals(13, hyperlinkCount);

    }

}
