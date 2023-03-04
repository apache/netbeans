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

package org.netbeans.lib.editor.util.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.random.TestDocument;

public class PriorityDocumentListenerListTest extends NbTestCase {

    public PriorityDocumentListenerListTest(String testName) {
        super(testName);
    }

    public void testAddListenerDuringFire() throws Exception {
        TestDocument doc = new TestDocument();
        DocL docL = new DocL(doc);
        docL.run();
    }

    private static final class DocL implements DocumentListener {
        
        private TestDocument doc;
        
        private boolean shouldNotFire;

        DocL(TestDocument doc) {
            this.doc = doc;
        }
        
        void run() throws Exception {
            DocumentUtilities.addPriorityDocumentListener(doc, this, DocumentListenerPriority.CARET_UPDATE);
            doc.insertString(0, "ahoj", null);
        }

        public void insertUpdate(DocumentEvent e) {
            if (shouldNotFire) {
                fail("Not expected to be fired.");
            }
            DocL docL2 = new DocL(doc);
            docL2.shouldNotFire = true;
            DocumentUtilities.addPriorityDocumentListener(doc, docL2, DocumentListenerPriority.LEXER);
            DocumentUtilities.addPriorityDocumentListener(doc, docL2, DocumentListenerPriority.AFTER_CARET_UPDATE);
        }

        public void removeUpdate(DocumentEvent e) {
        }

        public void changedUpdate(DocumentEvent e) {
        }

    }

}
