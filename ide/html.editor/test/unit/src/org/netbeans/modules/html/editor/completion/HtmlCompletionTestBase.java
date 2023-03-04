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
package org.netbeans.modules.html.editor.completion;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.completion.HtmlCompletionTestSupport.Match;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public abstract class HtmlCompletionTestBase extends TestBase {

    private static final String DATA_DIR_BASE = "testfiles/completion/";

    public HtmlCompletionTestBase(String name) {
        super(name);
    }

    protected abstract HtmlVersion getExpectedVersion();

    protected String getPublicID() {
        return null;
    }
    
    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type) throws BadLocationException, ParseException {
        assertItems(documentText, expectedItemsNames, type, -1);
    }

    protected void assertItems(String documentText, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws BadLocationException, ParseException {
        HtmlCompletionTestSupport.assertItems(getDocument(documentText), expectedItemsNames, type, expectedAnchor);
    }

    protected void assertItems(Document doc, final String[] expectedItemsNames, final Match type, int expectedAnchor) throws BadLocationException, ParseException {
        HtmlCompletionTestSupport.assertItems(doc, expectedItemsNames, type, expectedAnchor);
    }
    
    protected void assertCompletedText(String documentText, String itemToCompleteName, String expectedText) throws BadLocationException, ParseException {
        HtmlCompletionTestSupport.assertCompletedText(getDocument(""), documentText, itemToCompleteName, expectedText);
    }

    protected String[] arr(String... args) {
        return args;
    }

    protected TestSource getTestSource(String testFilePath, boolean removePipe) throws BadLocationException {
        FileObject source = getTestFile(DATA_DIR_BASE + testFilePath);
        BaseDocument doc = getDocument(source);
        StringBuilder sb = new StringBuilder(doc.getText(0, doc.getLength()));
        int pipeIndex = sb.indexOf("|");
        assertTrue(String.format("Errorneous test file %s, there is no pipe char specifying the completion offset!", testFilePath),
                pipeIndex != -1);

        if(removePipe) {
            sb.deleteCharAt(pipeIndex);
        }

        return new TestSource(sb.toString(), pipeIndex);
    }

    protected static class TestSource {
        private String code;
        private int position;

        private TestSource(String code, int position) {
            this.code = code;
            this.position = position;
        }

        public String getCode() {
            return code;
        }

        public int getPosition() {
            return position;
        }
        
    }

}
