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

package org.netbeans.modules.spring.beans.refactoring;

import javax.swing.text.BadLocationException;
import junit.framework.TestCase;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spring.beans.TestUtils;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;

/**
 *
 * @author Rohan Ranade
 */
public class AttributeFinderTest extends TestCase {

    public AttributeFinderTest(String testName) {
        super(testName);
    }

    public void testFind() throws Exception {
        final String contents = TestUtils.createXMLConfigText("<bean id='foo' class='org.example.Foo'><ref></ref></bean>");
        BaseDocument doc = TestUtils.createSpringXMLConfigDocument(contents);
        final XMLSyntaxSupport syntaxSupport = XMLSyntaxSupport.getSyntaxSupport(doc);
        doc.render(new Runnable() {
            public void run() {
                int beanOffset = contents.indexOf("<bean ");
                int classOffset = contents.indexOf("class");
                AttributeFinder finder = new AttributeFinder(syntaxSupport, beanOffset);
                try {
                    assertTrue(finder.find("class"));
                    assertEquals(classOffset, finder.getFoundOffset());
                } catch (BadLocationException e) {
                    fail(e.toString());
                }
            }
        });
    }
}
