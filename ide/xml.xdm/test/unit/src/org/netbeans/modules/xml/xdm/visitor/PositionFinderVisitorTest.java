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
 * PositionFinderVisitorTest.java
 *
 * Created on October 26, 2005, 8:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.w3c.dom.NodeList;

/**
 *
 * @author rico
 */
public class PositionFinderVisitorTest extends TestCase{
    
    /** Creates a new instance of PositionFinderVisitorTest */
    public PositionFinderVisitorTest() {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PositionFinderVisitorTest.class);
        
        return suite;
    }
 
    public void testFindPosition(){
        FindVisitor instance = new FindVisitor();
        
        Document root = xmlModel.getDocument();
        
        PositionFinderVisitor pfVisitor = new PositionFinderVisitor();
        
        //element company
        Element company = (Element)root.getChildNodes().item(0);
        Node result = instance.find(root, company.getId());
        assertEquals(company, result);
        
        int position = pfVisitor.findPosition(root, company);
        this.assertEquals("Position of root element",25, position);
        
        //newline char
        Text t = (Text)company.getChildNodes().item(0);
        position = pfVisitor.findPosition(root, t);
        this.assertEquals("Position of newline ", 83, position);
        
        //employee element
        Element employee = (Element)company.getChildNodes().item(1);
        position = pfVisitor.findPosition(root, employee);
        assertEquals("Position of employee element",89, position);
        
        //ssn attribute
        Attribute attr = (Attribute)employee.getAttributes().item(0);
        position = pfVisitor.findPosition(root, attr);
        assertEquals("Position of ssn attribute",99, position);
        
        //id attribute
        attr = (Attribute)employee.getAttributes().item(1);
        position = pfVisitor.findPosition(root, attr);
        assertEquals("Position of id attribute",119, position);
        
        //address attribute
        attr = (Attribute)employee.getAttributes().item(2);
        position = pfVisitor.findPosition(root, attr);
        assertEquals("Position of address attribute",136, position);
        
        //phone attribute
        attr = (Attribute)employee.getAttributes().item(3);
        position = pfVisitor.findPosition(root, attr);
        assertEquals("Position of phone attribute with embedded whitespaces",172, position);
        
        //text value child node of employee
        Text txt = (Text)employee.getChildNodes().item(0);
        position = pfVisitor.findPosition(root, txt);
        assertEquals("Position of text child node",195, position);
        
        //comment
        txt = (Text)company.getChildNodes().item(3);
        position = pfVisitor.findPosition(root, txt);
        assertEquals("Position of comment (after newline)",238, position);
        
        //second employee element
        Element employee2 = (Element)company.getChildNodes().item(5);
        position = pfVisitor.findPosition(root, employee2);
        assertEquals("Position of second employee element",259, position);
    }
	
    public void testFindPosition2() throws IOException, Exception {
		
        xmlModel = Util.loadXDMModel("visitor/testPosition.xsd");
        xmlModel.sync();	
		
        FindVisitor instance = new FindVisitor();
        
        Document root = xmlModel.getDocument();
        
        PositionFinderVisitor pfVisitor = new PositionFinderVisitor();
        
        //element schema
        Element schema = (Element)root.getChildNodes().item(2);
        Node result = instance.find(root, schema.getId());
        assertEquals(schema, result);
        
        int position = pfVisitor.findPosition(root, schema);
        this.assertEquals("Position of schema element",215, position);
        
        //global element
        Element ge = (Element)schema.getChildNodes().item(4);
        position = pfVisitor.findPosition(root, ge);
        assertEquals("Position of employee element",588, position);
    }	
    
    public void testFindPosition3() throws IOException, Exception {
        PositionFinderVisitor pfVisitor = new PositionFinderVisitor();
        xmlModel = Util.loadXDMModel("visitor/testPosition3.wsdl");
        Document root = xmlModel.getDocument();
        Element definitions = (Element) root.getDocumentElement();
        
        int position = pfVisitor.findPosition(root, definitions);
        assertEquals("Position of root element", 83, position);
        
        Element types = (Element) definitions.getChildNodes().item(1);
        assertEquals("types", types.getTagName());
        position = pfVisitor.findPosition(root, types);
        assertEquals("Position of types", 464, position);
        
        Element schema = (Element) types.getChildNodes().item(1);
        assertEquals("xsd:schema", schema.getTagName());
        position = pfVisitor.findPosition(root, schema);
        assertEquals("Position of types", 480, position);
        
    }	

    public void testFindPosition4() throws IOException, Exception {
        PositionFinderVisitor pfVisitor = new PositionFinderVisitor();
        xmlModel = Util.loadXDMModel("visitor/testPosition4.wsdl");
        Document root = xmlModel.getDocument();
        Element definitions = (Element) root.getDocumentElement();
        Element types = (Element) definitions.getChildNodes().item(1);
        Element foobar = (Element) types.getChildNodes().item(3);
        assertEquals("foobar", foobar.getTagName());
        int position = pfVisitor.findPosition(root, foobar);
        assertEquals("Position of types", 899, position);
    }	
    
    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("visitor/testPosition.xml");
        xmlModel.sync();
    }
    
    private void dumpTextNodes(Node node){
        NodeList nodes = node.getChildNodes();
        int counter = 0;
        for(int i = 0; i < nodes.getLength(); i++){
            Node n = (Node)nodes.item(i);
            if(n instanceof Text){
                Text t = ((Text)n);
                
            }
        }
    }
    
    private XDMModel xmlModel;
    
}
