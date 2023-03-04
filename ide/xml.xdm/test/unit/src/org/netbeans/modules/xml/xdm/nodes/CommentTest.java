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
 * TextTest.java
 * JUnit based test
 *
 * Created on October 21, 2005, 2:21 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import java.io.IOException;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.visitor.PrintVisitor;
import org.w3c.dom.NodeList;

/**
 *
 * @author ajit
 */
public class CommentTest extends TestCase {
    
    public CommentTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        baseDocument = Util.getResourceAsDocument("nodes/cdata.xml");
        xmlModel = Util.loadXDMModel(baseDocument);
        text = getCommentNode();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(CommentTest.class);
        
        return suite;
    }

    /**
     * Test of getNodeValue method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeValue() {
        String expResult = " I am a comment ";
        String result = text.getNodeValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNodeType method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeType() {
        short expResult = org.w3c.dom.Node.COMMENT_NODE;
        short result = text.getNodeType();
        assertEquals("getNodeType must return COMMENT_NODE",expResult, result);
    }

    /**
     * Test of getNodeName method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeName() {
        String expResult = "#comment";
        String result = text.getNodeName();
        assertEquals("getNodeName must return #comment",expResult, result);
    }

    /**
     * Test of getNamespaceURI method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNamespaceURI() {
        String result = text.getNamespaceURI();
        assertNull(result);
    }

    
    public void testGetData() {
	testGetNodeValue();
    }
    
    public void testMultiLineComment() {
	Comment c = getMultiLineComment();
	final String expectedValue = " line 1\nline 2\nline 3\n";
	assertEquals(c.getData(),expectedValue);
    }
    
    public void testSetData() {
	String tValue = "CBW #1";
	try {
	    text.setData(tValue);
	    fail("node not cloned");
	} catch (Exception e) {
	    
	}
	Comment clone = (Comment) text.cloneNode(true);
	clone.setData(tValue);
	assertEquals(3,clone.getTokens().size());
	assertEquals(tValue, clone.getData());
	
	xmlModel.modify(text,clone);
	xmlModel.flush();
	try {
	    xmlModel.sync();
	} catch (IOException ex) {
	    fail("sync failed");
	}
	assertEquals(tValue, getCommentNode().getNodeValue());
	assertEquals(3,getCommentNode().getTokens().size());
    }
    
    private Comment getMultiLineComment() {
	Element root = (Element) xmlModel.getDocument().getChildNodes().item(0);
	org.w3c.dom.Node multiLineComment = root.getElementsByTagName("multi-line-comment").item(0);
	NodeList nl = multiLineComment.getChildNodes();
	Comment comment = null;
	for (int i = 0; i < nl.getLength(); i++) {
	    org.w3c.dom.Node n = nl.item(i);
	    if (n instanceof Comment) {
		comment = (Comment) n;
		break;
	    }
	}
	return comment;
    }
    
    private Comment getCommentNode() {
	Element root = (Element) xmlModel.getDocument().getChildNodes().item(0);
	NodeList nl = root.getChildNodes();
	Comment comment = null;
	for (int i = 0; i < nl.getLength(); i++) {
	    org.w3c.dom.Node n = nl.item(i);
	    if (n instanceof Comment) {
		comment = (Comment) n;
		break;
	    }
	}
	return comment;
    }
    
    private XDMModel xmlModel;
    private Comment text;
    private javax.swing.text.Document baseDocument;
}
