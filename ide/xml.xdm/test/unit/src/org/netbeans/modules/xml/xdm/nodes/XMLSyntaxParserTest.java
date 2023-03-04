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

/*
 * XMLSyntaxParserTest.java
 * JUnit based test
 *
 * Created on September 26, 2005, 12:38 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import java.util.List;
import junit.framework.*;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xdm.visitor.FlushVisitor;
import org.netbeans.modules.xml.xdm.Util;
import org.w3c.dom.NodeList;

/**
 *
 * @author Administrator
 */
public class XMLSyntaxParserTest extends TestCase {
    
    public XMLSyntaxParserTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLSyntaxParserTest("testParse"));
        suite.addTest(new XMLSyntaxParserTest("testParseDoctype"));
        suite.addTest(new XMLSyntaxParserTest("testParseInvalid"));
        suite.addTest(new XMLSyntaxParserTest("testParseInvalidTag"));
        suite.addTest(new XMLSyntaxParserTest("testParseInvalidTag2"));
        suite.addTest(new XMLSyntaxParserTest("testParseInvalidTag3"));
        suite.addTest(new XMLSyntaxParserTest("testParseInvalidTag4"));
        suite.addTest(new XMLSyntaxParserTest("testParsePI"));
        suite.addTest(new XMLSyntaxParserTest("testParseValidTag"));
        suite.addTest(new XMLSyntaxParserTest("testParseTestXML"));
        suite.addTest(new XMLSyntaxParserTest("testMultiRootXML"));
        suite.addTest(new XMLSyntaxParserTest("testParseWSDL"));
//        Disabled as referenced files were partly not donated by oracle to apache
//        suite.addTest(new XMLSyntaxParserTest("testParsePerformace"));
        return suite;
    }
    
    private BaseDocument getDocument(String path) throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument(path);
        //must set the language for XML lexer to work.
        basedoc.putProperty(Language.class, XMLTokenId.language());
        return basedoc;
    }
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParse() throws Exception {
        BaseDocument basedoc = getDocument("nodes/test.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        Document doc = parser.parse(basedoc);
        assertNotNull("Document can not be null", doc);
        FlushVisitor fv = new FlushVisitor();
        String docBuf = fv.flushModel(doc);
        assertEquals("The document should be unaltered",basedoc.getText(0,basedoc.getLength()),docBuf);
    }
	    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalid() throws Exception {
        BaseDocument basedoc = getDocument("nodes/invalid.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token") && ex.getMessage().contains("sss"));
        }
    }	
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag() throws Exception {
        BaseDocument basedoc = getDocument("nodes/invalidtag.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token") && ex.getMessage().contains("sss"));
        }
    }
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag2() throws Exception {
        BaseDocument basedoc = getDocument("nodes/invalidtag2.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token '</a' does not end with '>'"));
        }
    }   
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag3() throws Exception {
        BaseDocument basedoc = getDocument("nodes/invalidtag3.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token found in document"));
        }
    }
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag4() throws Exception {
        BaseDocument basedoc = getDocument("nodes/invalidtag4.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token '</b' does not end with '>'"));
        }
    }    
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseValidTag() throws Exception {
        BaseDocument basedoc = getDocument("nodes/validtag.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);            
        } catch(Exception ex) {
            assertTrue("Should not come here", false);
        }
    }    

    public void testParsePI() throws Exception {
        BaseDocument basedoc = getDocument("resources/PI_after_prolog.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();

        Document doc = parser.parse(basedoc);            
        List<Token> tokens = doc.getTokens();
        assertEquals(12, tokens.size());
        assertEquals(TokenType.TOKEN_PI_START_TAG, tokens.get(0).getType());
        assertEquals(TokenType.TOKEN_PI_END_TAG, tokens.get(4).getType());
        assertEquals(TokenType.TOKEN_PI_START_TAG, tokens.get(6).getType());
        assertEquals(TokenType.TOKEN_PI_NAME, tokens.get(7).getType());
        assertEquals("Siebel-Property-Set", tokens.get(7).getValue());
        assertEquals(TokenType.TOKEN_PI_VAL, tokens.get(9).getType());
        NodeList nl = doc.getChildNodes();
        assertEquals(2, nl.getLength());    
    }    

    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     * Test the parsing of doctype
     */
    public void testParseDoctype() throws Exception {
        BaseDocument basedoc = getDocument("nodes/testDoctype.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        Document doc = parser.parse(basedoc);
        assertNotNull("Document can not be null", doc);
        FlushVisitor fv = new FlushVisitor();
        String docBuf = fv.flushModel(doc);
        assertEquals("The document should be unaltered",basedoc.getText(0,basedoc.getLength()),docBuf);
    }
    
    /**
     * Parses a xml document, which is really a wsdl.
     */
    public void testParseWSDL() throws Exception {
        BaseDocument basedoc = getDocument("nodes/wsdl.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);            
        } catch(Exception ex) {
            assertTrue("Should not come here", false);
        }
    }    
	
    /**
     * Parses a xml document, which is really a wsdl.
     */
    public void testParseTestXML() throws Exception {
        BaseDocument basedoc = getDocument("resources/test1_2.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);            
        } catch(Exception ex) {
            assertTrue("Should not come here", false);
        }
    }    

    public void testMultiRootXML() throws Exception {
        BaseDocument basedoc = getDocument("nodes/multiRoot.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue(ex.getMessage(), true);
        }
    }
    
    public void testParsePerformace() throws Exception {
        long start = System.currentTimeMillis();
        BaseDocument basedoc = getDocument("nodes/fields.xsd");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        Document doc = parser.parse(basedoc);
        long end = System.currentTimeMillis();
        System.out.println("Time taken to parse healthcare schema: " + (end-start) + "ms.");
        assertNotNull("Document can not be null", doc);
        //FlushVisitor fv = new FlushVisitor();
        //String docBuf = fv.flushModel(doc);
        //assertEquals("The document should be unaltered",basedoc.getText(0,basedoc.getLength()),docBuf);
    }    
}
