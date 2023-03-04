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

package org.netbeans.modules.xml.schema.model.impl.xdm;

import java.util.Iterator;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.impl.GlobalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaImpl;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Node;

/**
 *
 * @author Ayub Khan
 */
public class RenameTest extends TestCase {
    
    public RenameTest(String testName) {
        super(testName);
    }
    
    
    
    /**
     * Test of rename operation
     */
    public void testRenameGlobalElement() throws Exception {
	// add this code to make sure getDocument returns the same document
        SchemaModel model = Util.loadSchemaModel("resources/CutPasteTest_before.xsd");
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        Node schemaNode = schema.getPeer();
        GlobalElementImpl gei = (GlobalElementImpl) schema.getElements().iterator().next();
		
        assertEquals("testRenameGlobalElement.schema", 1, schema.getChildren().size());
		assertEquals("testRenameGlobalElement.schema.node", 3, schemaNode.getChildNodes().getLength());
		
		//Debug.log(Debug.LEVEL.ERROR, "Initial Document: ");
		//Debug.logDocument(Debug.LEVEL.ERROR, schemaNode.getOwnerDocument());		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, schemaNode);		
		
		model.startTransaction();
		gei.setName("NewName");
		model.endTransaction();	
		
		model.sync();
		
        SchemaImpl changedSchema = (SchemaImpl) model.getSchema();
        GlobalElementImpl changedGei = (GlobalElementImpl) changedSchema.getElements().iterator().next();
		Node changedGeiNode = changedGei.getPeer();
				
		assertEquals("testRenameGlobalElement.firstRename.Gei", "NewName", changedGei.getName());
		assertEquals("testRenameGlobalElement.firstRename.GeiNode", "NewName", 
				changedGeiNode.getAttributes().item(0).getNodeValue());		
		
		//TODO - fix, prints children from old document
		//child nodes: Text@2, Element@3(xsd:element, OrgChart), Text@28		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, changedSchemaNode);	
		
		model.startTransaction();
		changedGei.setName("NewName2");
		model.endTransaction();	
		
		model.sync();		
		
        SchemaImpl changedSchema2 = (SchemaImpl) model.getSchema();
        GlobalElementImpl changedGei2 = (GlobalElementImpl) changedSchema2.getElements().iterator().next();
		Node changedGeiNode2 = changedGei2.getPeer();
		
		assertEquals("testRenameGlobalElement.secondRename.Gei2", "NewName2", changedGei2.getName());
		assertEquals("testRenameGlobalElement.secondRename.Geinode2", "NewName2", 
				changedGeiNode2.getAttributes().item(0).getNodeValue());
		
		//TODO - fix, prints children from old document
		//child nodes: Text@2, Element@3(xsd:element, OrgChart), Text@28
		//XDMModel.printChildren(Debug.LEVEL.ERROR, changedSchemaNode2);		
      }

    public void testRenameGlobalElementAfterCopy() throws Exception {
		// add this code to make sure getDocument returns the same document
		
		/* This simulates
		 *- create a new schema;
		 *- create a new element;
		 *- switch to Source view;
		 *- copy created element;
		 *- go to Schema and back to Source view;
		 */	
        SchemaModel model = Util.loadSchemaModel("resources/RenameTestRename_before.xsd");
        
		SchemaImpl schema = (SchemaImpl) model.getSchema();
        Document doc = AbstractDocumentModel.class.cast(model).getBaseDocument();
		
		Iterator it = schema.getElements().iterator();
        GlobalElementImpl ge1 = (GlobalElementImpl) it.next();
		GlobalElementImpl ge2 = (GlobalElementImpl) it.next();
		assertEquals("testRenameGlobalElementAfterCopy.secondRename.ge1", 
				"OrgChart", ge1.getName());
		assertEquals("testRenameGlobalElementAfterCopy.secondRename.ge2", 
				"OrgChart", ge2.getName());
		
		/* This simulates
		 *- rename both elements;
		 *- switch to Schema view.
		 */		
        Util.setDocumentContentTo(doc, "resources/RenameTestRename_after.xsd");
        model.sync();
		
		Iterator it1 = schema.getElements().iterator();
        ge1 = (GlobalElementImpl) it1.next();
		ge2 = (GlobalElementImpl) it1.next();
		assertEquals("testRenameGlobalElementAfterCopy.secondRename.ge1", 
				"OrgChart1", ge1.getName());
		assertEquals("testRenameGlobalElementAfterCopy.secondRename.ge2", 
				"OrgChart2", ge2.getName());	
    }
    	
	protected void tearDown() throws Exception {
	    super.tearDown();
        TestCatalogModel.getDefault().clearDocumentPool();
	}
	
}
