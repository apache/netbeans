/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.api.xml.parsers;

import java.io.*;
import java.net.URL;
import junit.framework.*;
import org.netbeans.junit.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.ext.*;

/**
 * Tests SAXEntityParser as DTDParser.
 * Tests wrapping logic for relative references.
 *
 * @author Petr Kuzel
 */
public class SAXEntityParserTest extends NbTestCase {

    public SAXEntityParserTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(SAXEntityParserTest.class);
        
        return suite;
    }
    
    /** Test of parse method, of class org.netbeans.api.xml.parsers.SAXEntityParser. */
    public void testParse() throws Exception {
        System.out.println("testParse");
        
        // DTD parser test        
        
        InputSource input = new InputSource(new StringReader("<!ELEMENT x ANY>"));
        input.setSystemId("StringReader");
                
        XMLReader peer = XMLReaderFactory.createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
        TestDeclHandler dtdHandler = new TestDeclHandler();
        peer.setProperty("http://xml.org/sax/properties/declaration-handler", dtdHandler);
        SAXEntityParser parser = new SAXEntityParser(peer, false);
        parser.parse(input);

        // Add your test code below by replacing the default call to fail.
        assertTrue("DTD entity parser did not detected 'x' decl!", dtdHandler.pass);

        // Reentrance test
        
        boolean exceptionThrown = false;
        try {
            parser.parse(new InputSource(new StringReader("")));
        } 
        catch (IllegalStateException ex) {
            exceptionThrown = true;
        } 
        finally {
            assertTrue("Parser may not be reused!", exceptionThrown);
        }
        
        relativeReferenceTest();
    }        
    
    /**
     * Wrapping used to broke relative references.
     */
    private void relativeReferenceTest() throws Exception {

        final boolean pass[] = {false};

        try {
            URL url = getClass().getResource("data/RelativeTest.dtd");
            System.out.println("URL:" + url);
            InputSource input = new InputSource(url.toExternalForm());
            XMLReader peer = XMLReaderFactory.createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
            peer.setDTDHandler(new DefaultHandler() {
                public void notationDecl(String name, String publicId, String systemId) {
                    if ("notation".equals(name)) pass[0] = true;
                }
            });
            SAXEntityParser parser = new SAXEntityParser(peer, false);
            parser.parse(input);
        }
        finally {
            assertTrue("External entity not reached!", pass[0]);
        }
    }
    
    
    class TestDeclHandler implements DeclHandler {
        
        boolean pass;
        
        public void attributeDecl(String str, String str1, String str2, String str3, String str4) throws org.xml.sax.SAXException {
        }
        
        public void elementDecl(String str, String str1) throws org.xml.sax.SAXException {
            if ("x".equals(str)) pass = true;
        }
        
        public void externalEntityDecl(String str, String str1, String str2) throws org.xml.sax.SAXException {
        }
        
        public void internalEntityDecl(String str, String str1) throws org.xml.sax.SAXException {
        }
        
    }
    
}
