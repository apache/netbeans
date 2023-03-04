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
package org.netbeans.modules.html.editor.codegen;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author daniel
 */
public class LoremIpsumGeneratorTest extends TestBase {

    private BaseDocument doc;
    private List<String> paragraphs = new ArrayList<>();

    public LoremIpsumGeneratorTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() {
        doc = createDocument();
        paragraphs.add("one");
        paragraphs.add("two");
    }

    public void testInsertAtNonEmptyLine() throws Exception {
        String originalText = "<html><head></head><body>text</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text");
        LoremIpsumGenerator.insertLoremIpsumText(doc, paragraphs, "<p>", insertPosition);
        assertEquals("<html><head></head><body>\n"
                + "        <p>\n"
                + "            one\n"
                + "        </p>\n"
                + "        <p>\n"
                + "            two\n"
                + "        </p>\n"
                + "        text</body></html>", new String(doc.getChars(0, doc.getLength())));
    }
    
    public void testInsertAtEmptyLine() throws Exception {
        String originalText = "<html><head></head><body>\ntext\n</body></html>";
        doc.insertString(0, originalText, null);
        int insertPosition = originalText.indexOf("text");
        LoremIpsumGenerator.insertLoremIpsumText(doc, paragraphs, "<p>", insertPosition);
        assertEquals("<html><head></head><body>\n"
                + "\n"
                + "        <p>\n"
                + "            one\n"
                + "        </p>\n"
                + "        <p>\n"
                + "            two\n"
                + "        </p>\n"
                + "        text\n"
                + "</body></html>", new String(doc.getChars(0, doc.getLength())));
    }

    
}
