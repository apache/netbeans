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
package org.netbeans.modules.xml.text.typinghooks;

import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.text.ComplexValueSettingsFactory;

public class XmlCamelCaseInterceptorTest extends NbTestCase {

    public XmlCamelCaseInterceptorTest(String name) {
        super(name);
    }

    public void testIdentifierAcceptor() {
        Acceptor acceptor = ComplexValueSettingsFactory.getXMLIdentifierAcceptor();
        assertFalse(acceptor.accept('.'));
        assertFalse(acceptor.accept('-'));
        assertFalse(acceptor.accept(':'));
        assertFalse(acceptor.accept(','));
        assertFalse(acceptor.accept('='));
        assertFalse(acceptor.accept('<'));
        assertFalse(acceptor.accept('>'));
        assertFalse(acceptor.accept('"'));
        assertFalse(acceptor.accept('\''));
        assertFalse(acceptor.accept(' '));
        assertTrue(acceptor.accept('a'));
        assertTrue(acceptor.accept('Z'));
        assertTrue(acceptor.accept('5'));
        assertTrue(acceptor.accept('_'));
    }

    public void testForwardCamelCaseInTagAndText() throws Exception {
        String text = "<artifactId>fooBarBaz</artifactId>";
        assertWordOffset(text, text.indexOf("artifactId"), false, text.indexOf("Id"));
        assertWordOffset(text, text.indexOf("fooBarBaz"), false, text.indexOf("Bar"));
        assertWordOffset(text, text.indexOf("Baz"), false, -1);
    }

    public void testBackwardCamelCase() throws Exception {
        String text = "<t>fooBar</t>";
        int word = text.indexOf("fooBar");
        assertWordOffset(text, word + "fooBar".length(), true, text.indexOf("Bar"));
        assertWordOffset(text, word, true, -1);
    }

    public void testPunctuationWinsOverLaterHump() throws Exception {
        String text = "<g>org.apache.maven maven-compiler-plugin my-coolArtifact</g>";
        assertWordOffset(text, text.indexOf("org.apache.maven"), false, -1);
        assertWordOffset(text, text.indexOf("maven-compiler-plugin"), false, -1);
        assertWordOffset(text, text.indexOf("my-coolArtifact"), false, -1);
        assertWordOffset(text, text.indexOf("coolArtifact"), false, text.indexOf("Artifact"));
    }

    public void testMarkupWhitespaceAndOtherTokens() throws Exception {
        String text = "<t> fooBarBaz</t><!-- fooBarBaz -->";
        assertWordOffset(text, text.indexOf(" foo"), false, -1);
        assertWordOffset(text, text.indexOf('<'), false, -1);
        assertWordOffset(text, text.indexOf('>'), false, -1);
        assertWordOffset(text, text.lastIndexOf("fooBarBaz"), false, -1);
    }

    private static void assertWordOffset(String text, int caret, boolean backward, int expected) throws BadLocationException {
        BaseDocument doc = new BaseDocument(true, "text/xml"); //NOI18N
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, XMLTokenId.language());
        doc.readLock();
        try {
            assertEquals(expected, XmlCamelCaseInterceptor.getWordOffset(doc, caret, backward));
        } finally {
            doc.readUnlock();
        }
    }
}
