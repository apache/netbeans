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

import java.util.Iterator;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.impl.GlobalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.LocalComplexTypeImpl;
import org.netbeans.modules.xml.schema.model.impl.LocalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaImpl;
import org.netbeans.modules.xml.schema.model.impl.SequenceImpl;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.w3c.dom.Node;

/**
 *
 * @author Ayub Khan
 */
public class CutPasteTest extends TestCase {
    
    public CutPasteTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
	
    /**
     * Test of cut/paste operation
     */
    public void testCutPasteLocalElement() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/CutPasteTest_before.xsd");
        Document doc = AbstractDocumentModel.class.cast(model).getBaseDocument();
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        Node schemaNode = schema.getPeer();
        GlobalElementImpl gei = (GlobalElementImpl) schema.getElements().iterator().next();
        LocalComplexTypeImpl lcti = (LocalComplexTypeImpl) gei.getInlineType();
        SequenceImpl seq = (SequenceImpl) lcti.getDefinition();
        Node seqNode = seq.getPeer();
		LocalElementImpl leti = (LocalElementImpl) seq.getContent().get(1);
		
        assertEquals("testCutPasteByDocSync.schema.node", 2, seq.getChildren().size());
		assertEquals("testCutPasteByDocSync.schema.node", 5, seqNode.getChildNodes().getLength());		
		
		model.startTransaction();
		LocalElementImpl clonedLeti = (LocalElementImpl) leti.copy(seq);
		assertTrue("testCutPasteByDocSync.localElement", clonedLeti!=null);
		seq.removeContent(leti);
		model.endTransaction();	
		
        SchemaImpl changedSchema = (SchemaImpl) model.getSchema();
        Node changedSchemaNode = changedSchema.getPeer();
        GlobalElementImpl changedGei = (GlobalElementImpl) changedSchema.getElements().iterator().next();
        LocalComplexTypeImpl changedLcti = (LocalComplexTypeImpl) changedGei.getInlineType();
        SequenceImpl changedSeq = (SequenceImpl) changedLcti.getDefinition();
        Node changedSeqNode = changedSeq.getPeer();				
		
        //make sure elements and nodes on the path before sequence is same 
        assertTrue("testCutPasteByRemove.schema", schema == changedSchema);
        //NOTE: we are now actively update nodes on path to root component.
        //assertTrue("testCutPasteByRemove.schema.node", schemaNode == changedSchemaNode);
		assertTrue("testCutPasteByRemove.schema.node", seqNode != changedSeqNode);		
		
        assertEquals("testCutPasteByDocSync.schema.node", 1, seq.getChildren().size());
		assertEquals("testCutPasteByDocSync.schema.node", 3, seq.getPeer().getChildNodes().getLength());		
		assertEquals("testCutPasteByDocSync.schema.node", 1, changedSeq.getChildren().size());		
		assertEquals("testCutPasteByDocSync.schema.node", 3, changedSeqNode.getChildNodes().getLength());
		
		model.startTransaction();
		seq.addContent(clonedLeti, 1);
		model.endTransaction();	
		
        SchemaImpl changedSchema1 = (SchemaImpl) model.getSchema();
        Node changedSchemaNode1 = changedSchema1.getPeer();
        GlobalElementImpl changedGei1 = (GlobalElementImpl) changedSchema1.getElements().iterator().next();
        LocalComplexTypeImpl changedLcti1 = (LocalComplexTypeImpl) changedGei1.getInlineType();
        SequenceImpl changedSeq1 = (SequenceImpl) changedLcti1.getDefinition();
        Node changedSeqNode1 = changedSeq1.getPeer();		
		
        assertEquals("testCutPasteByDocSync.schema.node", 2, seq.getChildren().size());
		assertEquals("testCutPasteByDocSync.schema.node", 5, seq.getPeer().getChildNodes().getLength());		
        assertEquals("testCutPasteByDocSync.schema.node", 2, changedSeq1.getChildren().size());
		assertEquals("testCutPasteByDocSync.schema.node", 5, changedSeqNode1.getChildNodes().getLength());		
		assertTrue("testCutPasteByDocSync.localElement.isSame", leti.getName().equals(clonedLeti.getName()));
		assertTrue("testCutPasteByDocSync.localElement.isIdNotSame", 
			((NodeImpl)leti.getPeer()).getId()!=((NodeImpl)clonedLeti.getPeer()).getId());
		Iterator it=changedSeq1.getChildren().iterator();
		it.next();
		LocalElementImpl le2=(LocalElementImpl) it.next();
		assertTrue("testCutPasteByDocSync.localElement.isSame", leti.getName().equals(le2.getName()));
		assertTrue("testCutPasteByDocSync.localElement.isIdNotSame", 
			((NodeImpl)leti.getPeer()).getId()!=((NodeImpl)le2.getPeer()).getId());
    }
	
    /**
     * testMultipleDnDAndUndo
     */
    public void testMultipleDnDAndUndo() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/PO_copypasteundoSequence.xsd");
        UndoManager um = new UndoManager();
        model.addUndoableEditListener(um);
        
        Sequence seq = (Sequence)Util.findComponent(
                model.getSchema(), 
				"/schema/complexType[@name='PurchaseOrderType']/sequence");		
		assert(seq != null);
        LocalElement shipTo = (LocalElement)Util.findComponent(
                model.getSchema(), 
				"/schema/complexType[@name='PurchaseOrderType']/" +
				"sequence/element[@name='shipTo']");
		assert(shipTo != null);
        ElementReference comment = (ElementReference)Util.findComponent(
                model.getSchema(), 
				"/schema/complexType[@name='PurchaseOrderType']/" +
				"sequence/element[@ref='comment']");
		assert(comment != null);
        
		assertEquals(4, seq.getChildren().size());
        LocalElement copy_shipTo = (LocalElement) shipTo.copy(seq);
        model.startTransaction();
		seq.removeContent(shipTo);
		seq.addContent(copy_shipTo, 3);
        model.endTransaction();
        assertEquals(4, seq.getChildren().size());
        
        um.undo();
        assertEquals(4, seq.getChildren().size());
		
        seq = (Sequence)Util.findComponent(
                model.getSchema(), 
				"/schema/complexType[@name='PurchaseOrderType']/sequence");		
		assert(seq != null);
        shipTo = (LocalElement)Util.findComponent(
                model.getSchema(), 
				"/schema/complexType[@name='PurchaseOrderType']/" +
				"sequence/element[@name='shipTo']");
		assert(shipTo != null);
        comment = (ElementReference)Util.findComponent(
                model.getSchema(), 
				"/schema/complexType[@name='PurchaseOrderType']/" +
				"sequence/element[@ref='comment']");
		assert(comment != null);
		
        ElementReference copy_comment = (ElementReference) comment.copy(seq);
        model.startTransaction();
		seq.removeContent(comment);
		seq.addContent(copy_comment, 1);
        model.endTransaction();
        assertEquals(4, seq.getChildren().size());
        
        um.undo();
        assertEquals(4, seq.getChildren().size());		
    }
		
    private Document sd;
    private SchemaModel model;
    
}
