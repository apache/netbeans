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

import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.junit.Test;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Finder;
import org.netbeans.editor.FinderFactory;
import org.netbeans.modules.xml.text.AbstractTestCase;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;

public class XMLCompletionQueryTest extends AbstractTestCase {

    public XMLCompletionQueryTest(String testName) {
        super(testName);
    }

    @Test
    public void testShouldCloseTag() throws IOException, BadLocationException, Exception {
        BaseDocument doc = getDocument("/org/netbeans/modules/xml/text/completion/res/docResourceUnclosed.html");
        XMLSyntaxSupport xss = XMLSyntaxSupport.getSyntaxSupport((BaseDocument)doc);
        // ##IP1## and ##IP2## mark the positions in the document
        // which will be checked fif at that point the tag should be closed
        Finder ip1Finder = new FinderFactory.StringFwdFinder("##IP1##", true);
        Finder ip2Finder = new FinderFactory.StringFwdFinder("##IP2##", true);
        int insertPos1 = doc.find(ip1Finder, doc.getStartPosition().getOffset(), doc.getEndPosition().getOffset());
        int insertPos2 = doc.find(ip2Finder, doc.getStartPosition().getOffset(), doc.getEndPosition().getOffset());
        // The first position is a caret position behind a <a> Element, that
        // has a closing tag => shouldCloseTag needs to return NULL here to
        // indicate, that inserted content should be inserted without the
        // closing tag.
        SyntaxQueryHelper sqh = new SyntaxQueryHelper(xss, insertPos1);
        assertNull("XMLCompletionQuery#shouldCloseTag should return NULL if closing tag is present",
                XMLCompletionQuery.shouldCloseTag(sqh, doc, xss));
        // The second position is a caret position behind a <a> Element without
        // a matching end tag.
        SyntaxQueryHelper sqh2 = new SyntaxQueryHelper(xss, insertPos2);
        assertEquals("XMLCompletionQuery#shouldCloseTag should return tagname if no closing tag is present",
                "a", XMLCompletionQuery.shouldCloseTag(sqh2, doc, xss));
    }
}
