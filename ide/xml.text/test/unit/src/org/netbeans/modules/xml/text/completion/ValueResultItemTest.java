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
package org.netbeans.modules.xml.text.completion;

import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.text.Document;
import org.junit.Test;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Finder;
import org.netbeans.editor.FinderFactory;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.spi.dom.AbstractNode;
import org.netbeans.modules.xml.text.AbstractTestCase;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class ValueResultItemTest extends AbstractTestCase {

    public ValueResultItemTest(String testName) {
        super(testName);
    }

    @Test
    public void testValueReplacement() throws Exception {
        JTextField textField = new JTextField();
        BaseDocument doc = getDocument("/org/netbeans/modules/xml/text/completion/res/ValueReplacement.xml");
        BaseDocument referenceDoc = getDocument("/org/netbeans/modules/xml/text/completion/res/ValueReplacement.golden.xml");
        Finder ip1Finder = new FinderFactory.StringFwdFinder("##IP1##", true);
        int insertPos1 = doc.find(ip1Finder, doc.getStartPosition().getOffset(), doc.getEndPosition().getOffset());
        doc.replace(insertPos1, 7, "", null);
        textField.setDocument(doc);
        MockGrammarResult mgr = new MockGrammarResult("Middle", "Mid");
        ValueResultItem vri1 = new ValueResultItem(insertPos1, mgr, 0, null);
        textField.setCaretPosition(insertPos1);
        vri1.defaultAction(textField);

        Finder ip2Finder = new FinderFactory.StringFwdFinder("##IP2##", true);
        int insertPos2 = doc.find(ip2Finder, doc.getStartPosition().getOffset(), doc.getEndPosition().getOffset());
        doc.replace(insertPos2, 7, "", null);
        ValueResultItem vri2 = new ValueResultItem(insertPos2, mgr, 7, null);
        textField.setCaretPosition(insertPos2);
        vri2.defaultAction(textField);

        MockGrammarResult mgr2 = new MockGrammarResult("Middle", "");
        Finder ip3Finder = new FinderFactory.StringFwdFinder("##IP3##", true);
        int insertPos3 = doc.find(ip3Finder, doc.getStartPosition().getOffset(), doc.getEndPosition().getOffset());
        doc.replace(insertPos3, 7, "", null);
        ValueResultItem vri3 = new ValueResultItem(insertPos3, mgr2, 7, null);
        textField.setCaretPosition(insertPos3);
        vri3.defaultAction(textField);

        assertTrue("Result did not match reference", compare(referenceDoc, doc));
    }

    private static class MockGrammarResult extends AbstractNode implements GrammarResult, Text {
        private final String name;
        private final String prefix;
        private final String description = "";

        public MockGrammarResult(String name, String prefix) {
            this.name = name;
            this.prefix = prefix;
        }

        @Override
        public short getNodeType() {
            return Node.TEXT_NODE;
        }

        @Override
        public String getNodeName() {
            return name;
        }

        @Override
        public String getTagName() {
            return name;
        }

        @Override
        public String getNodeValue() {
            return name.substring(prefix.length());
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public Icon getIcon(int kind) {
            return null;
        }

        @Override
        public boolean isEmptyElement() {
            return false;
        }

    }
}
