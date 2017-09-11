/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.sax;

import org.netbeans.modules.javafx2.editor.sax.XmlLexerParser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.netbeans.modules.javafx2.editor.GoldenFileTestBase;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 *
 * @author sdedic
 */
public class XMLLexerParserTest extends GoldenFileTestBase {

    public XMLLexerParserTest(String name) {
        super(name);
    }
    
    private CH handler;
    
    private void defaultTestContents() throws Exception {
        final XmlLexerParser parser = new XmlLexerParser(hierarchy);
        handler = new CH();
        
        parser.setContentHandler(handler);
        parser.setLexicalHandler(handler);
        
        final Exception[] exc = new Exception[1];
        document.render(new Runnable() {
            public void run() {
                try {
                    parser.parse();
                } catch (Exception ex) {
                    exc[0] = ex;
                }
            }
        });
        if (exc[0] != null) {
            throw exc[0];
        }
        assertContents(handler.out);
    }
    
//    public static TestSuite suite() {
//        TestSuite s = new TestSuite();
//        s.addTest(new XMLLexerParserTest("testIncompletePi"));
//        return s;
//    }
    
    public void testParser() throws Exception {
        defaultTestContents();
    }
    
    /**
     * Tests recovery in half-written processing instructions
     * @throws Exception 
     */
    public void testIncompletePi() throws Exception {
        defaultTestContents();
    }
    
    /**com
     * Tests recovery when writing attributes.
     * 
     * @throws Exception Test
     */
    public void testBrokenAttributes() throws Exception {
        defaultTestContents();
    }

    public void testBrokenElements() throws Exception {
        defaultTestContents();
    }
    
    public void testBrokenHierarchy() throws Exception {
        defaultTestContents();
    }
    
    public void testXmlEntities() throws Exception {
        defaultTestContents();
    }

    private void assertContents(StringBuilder sb) throws IOException {
        File out = new File(getWorkDir(), fname + ".parsed");
        FileWriter wr = new FileWriter(out);
        wr.append(sb);
        wr.close();
        
        assertFile(out, getGoldenFile(fname + ".pass"), new File(getWorkDir(), fname + ".diff"));
    }
    
    private class CH implements ContentHandler, LexicalHandler {
        private Locator docLocator;
        private StringBuilder out = new StringBuilder();
        private int indent;
        
        private StringBuilder indent(StringBuilder sb) {
            for (int i = 0; i < indent; i++) {
                sb.append(' ');
            }
            return sb;
        }
        
        @Override
        public void setDocumentLocator(Locator locator) {
            docLocator = locator;
        }

        @Override
        public void startDocument() throws SAXException {
            indent(out).append("[doc]\n");
        }

        @Override
        public void endDocument() throws SAXException {
            indent(out).append("[end-doc]\n");
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            indent(out).append("[prefix ").append(prefix).append("=").append(uri).append("]\n");
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            indent(out).append("[end-prefix ").append(prefix).append("\n");
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            indent(out).append("[elem] ").append(qName).append("\n");
            if (atts.getLength() > 0) {
                indent(out).append("    ");
                for (int i = 0; i < atts.getLength(); i++) {
                    String qn = atts.getQName(i);
                    if (i > 0) {
                        out.append(", ");
                    }
                    out.append(qn).append("=").append(atts.getValue(i));
                }
                out.append("\n");
            }
            indent++;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            --indent;
            indent(out).append("[end-elem] ").append(qName).append("]\n");
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            indent(out).append("[chars] ").append(ch).append("\n");
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            //indent(out).append("[ignorable-ws] ").append(length).append("\n");
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            indent(out).append("[instruction] target=").append(target).append(", data=").append(data).append("\n");
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
        }

        @Override
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
        }

        @Override
        public void endDTD() throws SAXException {
        }

        @Override
        public void startEntity(String name) throws SAXException {
        }

        @Override
        public void endEntity(String name) throws SAXException {
        }

        @Override
        public void startCDATA() throws SAXException {
        }

        @Override
        public void endCDATA() throws SAXException {
        }

        @Override
        public void comment(char[] ch, int start, int length) throws SAXException {
        }
        
    }
}
