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
