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

package org.netbeans.modules.xml.schema.model.impl.xdm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.impl.GlobalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.LocalComplexTypeImpl;
import org.netbeans.modules.xml.schema.model.impl.LocalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaImpl;
import org.netbeans.modules.xml.schema.model.impl.SequenceImpl;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Ayub Khan
 */
public class CopyPasteTest extends TestCase {
    
    public static final String TEST_XSD     = "resources/PurchaseOrder.xsd";
    public static final String TEST_XSD_OP     = "resources/PurchaseOrderSyncTest.xsd";
    
    public CopyPasteTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    private String readFile(String filename) throws IOException {
        URL url = getClass().getResource(filename);
        BufferedReader br =  new BufferedReader(new InputStreamReader(url.openStream(),"UTF-8"));
        StringBuffer sbuf = new StringBuffer();
        try {
            int c = 0;
            while((c = br.read()) != -1) {
                sbuf.append((char)c);
            }
        } finally {
            br.close();
        }
        return sbuf.toString();
    }
	
    /**
     * Test of copy/paste operation
     */
    public void testCopyPasteLocalElement() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/CutPasteTest_before.xsd");	
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        Node schemaNode = schema.getPeer();
        GlobalElementImpl gei = (GlobalElementImpl) schema.getElements().iterator().next();
        LocalComplexTypeImpl lcti = (LocalComplexTypeImpl) gei.getInlineType();
        SequenceImpl seq = (SequenceImpl) lcti.getDefinition();
        Node seqNode = seq.getPeer();
		LocalElementImpl leti = (LocalElementImpl) seq.getContent().get(1);
		
        assertEquals("testCutPasteByDocSync.localElement.size", 2, seq.getChildren().size());
		assertEquals("testCutPasteByDocSync.localElement.nodes.size", 5, seqNode.getChildNodes().getLength());
		
		//Debug.log(Debug.LEVEL.ERROR, "Initial Document: ");
		//Debug.logDocument(Debug.LEVEL.ERROR, schemaNode.getOwnerDocument());
		//XDMModel.printChildren(Debug.LEVEL.ERROR, seqNode);
		
		model.startTransaction();	
		LocalElementImpl clonedLeti = (LocalElementImpl) leti.copy(seq);
		assertTrue("testCutPasteByDocSync.localElement", clonedLeti!=null);
		seq.addContent(clonedLeti, 2);
		model.endTransaction();	
		
        SchemaImpl changedSchema1 = (SchemaImpl) model.getSchema();
        Node changedSchemaNode1 = changedSchema1.getPeer();		
        GlobalElementImpl changedGei1 = (GlobalElementImpl) changedSchema1.getElements().iterator().next();
        LocalComplexTypeImpl changedLcti1 = (LocalComplexTypeImpl) changedGei1.getInlineType();
        SequenceImpl changedSeq1 = (SequenceImpl) changedLcti1.getDefinition();
        Node changedSeqNode1 = changedSeq1.getPeer();
		
        assertEquals("testCutPasteByDocSync.localElement.nodes.size", 3, changedSeq1.getChildren().size());
		Iterator it=changedSeq1.getChildren().iterator();
		it.next();
		LocalElementImpl le2=(LocalElementImpl) it.next();
		LocalElementImpl le3=(LocalElementImpl) it.next();
		assertTrue("testCutPasteByDocSync.localElement.isSame", le2.getName().equals(le3.getName()));
		assertTrue("testCutPasteByDocSync.localElement.isIdNotSame", 
			((NodeImpl)le2.getPeer()).getId()!=((NodeImpl)le3.getPeer()).getId());
		assertEquals("testCutPasteByDocSync.localElement.nodes.size", 7, changedSeqNode1.getChildNodes().getLength());		
		
		//Debug.log(Debug.LEVEL.ERROR, "After add: ");
		//Debug.logDocument(Debug.LEVEL.ERROR, changedSchemaNode1.getOwnerDocument());		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, changedSeqNode1);
    }
	
    /**
     * Test of copy/paste operation
     */
    public void testCopyPasteGlobalElement() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/CutPasteTest_before.xsd");	
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        Node schemaNode = schema.getPeer();
        GlobalElement ge = (GlobalElement) schema.getElements().iterator().next();
				
        assertEquals("testCutPasteByDocSync.localElement.size", 1, schema.getChildren().size());
		assertEquals("testCutPasteByDocSync.localElement.nodes.size", 3, schemaNode.getChildNodes().getLength());
		
		//Debug.log(Debug.LEVEL.ERROR, "Initial Document: ");
		//Debug.logDocument(Debug.LEVEL.ERROR, schemaNode.getOwnerDocument());
		//XDMModel.printChildren(Debug.LEVEL.ERROR, schemaNode);
		//Debug.log(Debug.LEVEL.ERROR, "\nchild of GlobalElement: ");		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, ((GlobalElementImpl)ge).getPeer());		
		
		model.startTransaction();	
		GlobalElement clonedGe = (GlobalElement) ge.copy(schema);
		assertTrue("testCutPasteByDocSync.globalElement", clonedGe!=null);
		schema.addElement(clonedGe);
		model.endTransaction();	
		
        SchemaImpl changedSchema1 = (SchemaImpl) model.getSchema();
        Node changedSchemaNode1 = changedSchema1.getPeer();		
 		
        assertEquals("testCutPasteByDocSync.localElement.nodes.size", 2, changedSchema1.getChildren().size());
		Iterator it=changedSchema1.getChildren().iterator();		
		GlobalElement ge1=(GlobalElement) it.next();		
		GlobalElement ge2=(GlobalElement) it.next();	
		
		assertTrue("testCutPasteByDocSync.localElement.isSame", ge1.getName().equals(ge2.getName()));
		assertTrue("testCutPasteByDocSync.localElement.isIdNotSame", 
			((NodeImpl)((GlobalElementImpl)ge1).getPeer()).getId()!=((NodeImpl)((GlobalElementImpl)ge2).getPeer()).getId());
		assertEquals("testCutPasteByDocSync.localElement.nodes.size", 5, changedSchemaNode1.getChildNodes().getLength());		
		
		//Debug.log(Debug.LEVEL.ERROR, "After add: ");
		//Debug.logDocument(Debug.LEVEL.ERROR, changedSchemaNode1.getOwnerDocument());		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, changedSchemaNode1);
		
		LocalComplexType ge1Child = (LocalComplexType) ge1.getChildren().iterator().next();
		Element ge1ChildNode = ((LocalComplexTypeImpl)ge1Child).getPeer();

		LocalComplexType ge2Child = (LocalComplexType) ge2.getChildren().iterator().next();
		Element ge2ChildNode = ((LocalComplexTypeImpl)ge2Child).getPeer();
		
		assertTrue("testCutPasteByDocSync.localElement.isIdNotSame",
				((NodeImpl)ge1ChildNode).getId()!=((NodeImpl)ge2ChildNode).getId());		
		//Debug.log(Debug.LEVEL.ERROR, "\nchild of GlobalElement: ");		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, ((GlobalElementImpl)ge1).getPeer());		
		//Debug.log(Debug.LEVEL.ERROR, "\nchild of new GlobalElement: ");		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, ((GlobalElementImpl)ge2).getPeer());
    }	
	
    private Document sd;
    private SchemaModel model;
    
}
