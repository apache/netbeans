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

import java.util.ArrayList;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.Component;

/**
 *
 * @author Administrator
 */
public class SyncUpdateVisitorTest extends TestCase {
    
    public static final String TEST_XSD     = "resources/PurchaseOrder.xsd";
    private Schema schema;
    private SchemaModel model;

    public SyncUpdateVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }
    
    protected void setUp1() throws Exception {
        model = (SchemaModel)Util.loadSchemaModel(TEST_XSD);
        schema = model.getSchema();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SyncUpdateVisitorTest.class);
        
        return suite;
    }

    public void testVisitDelete() throws Exception {
        setUp1();
        SyncUpdateVisitor instance = new SyncUpdateVisitor();
        
        int size = schema.getComplexTypes().size();
        GlobalComplexType gct = schema.getComplexTypes().iterator().next();
        model.startTransaction();
        instance.update(schema, gct ,SyncUpdateVisitor.Operation.REMOVE);
        model.endTransaction();
        assertEquals(size-1,schema.getComplexTypes().size());

        size = schema.getSimpleTypes().size();
        GlobalSimpleType gst = schema.getSimpleTypes().iterator().next();
        model.startTransaction();
        instance.update(schema, gst ,SyncUpdateVisitor.Operation.REMOVE);
        model.endTransaction();
        assertEquals(size-1,schema.getSimpleTypes().size());

        size = schema.getElements().size();
        GlobalElement ge = schema.getElements().iterator().next();
        model.startTransaction();
        instance.update(schema, ge ,SyncUpdateVisitor.Operation.REMOVE);
        model.endTransaction();
        assertEquals(size-1,schema.getElements().size());
    }
    
    public void testVisitAdd() throws Exception{
        setUp1();
        SyncUpdateVisitor instance = new SyncUpdateVisitor();
        
        int size = schema.getComplexTypes().size();
        schema.getModel().getFactory().createGlobalComplexType();
        GlobalComplexType gct = schema.getModel().getFactory().createGlobalComplexType();
        model.startTransaction();
        instance.update(schema, gct ,SyncUpdateVisitor.Operation.ADD);
        model.endTransaction();
        assertEquals(size+1,schema.getComplexTypes().size());

        size = schema.getSchemaReferences().size();
        SchemaComponent sc = schema.getModel().getFactory().createImport();
        model.startTransaction();
        instance.update(schema, sc ,SyncUpdateVisitor.Operation.ADD);
        model.endTransaction();
        assertEquals(size+1,schema.getSchemaReferences().size());
    }
    
    public void testRemoveAllPurchaseOrder() throws Exception {
        setUp1();
        model.startTransaction();
        recursiveRemoveChildren(schema);
        assertEquals("children removed", 0, schema.getChildren().size());
        model.endTransaction();
    }
    
    //TODO: debug mysterious StackOverflowError on UnmodifiableCollection.iterator
    /*public void testRemoveAllOTA() throws Exception {
        model = TestResolver.getDefault().getModel(NamespaceLocation.OTA);
        schema = model.getSchema();
        model.startTransaction();
        recursiveRemoveChildren(schema);
        assertEquals("children removed", 0, schema.getChildren().size());
        model.endTransaction();
    }*/
    
    public void testRemoveAllLoanApp() throws Exception {
        model = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.LOANAPP);
        schema = model.getSchema();
        model.startTransaction();
        recursiveRemoveChildren(schema);
        assertEquals("children removed", 0, schema.getChildren().size());
        model.endTransaction();
    }
    
    public static void recursiveRemoveChildren(SchemaComponent target) {
        SchemaModel model = target.getModel();
        ArrayList<SchemaComponent> children = new ArrayList<SchemaComponent>(target.getChildren());
        for (SchemaComponent child : children) {
            recursiveRemoveChildren(child);
        }
        if (target.getParent() != null) {
            model.removeChildComponent(target);
        }
    }

    public void testCanPasteAllLoanApp() throws Exception {
        model = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.LOANAPP);
        schema = model.getSchema();
        recursiveCanPasteChildren(schema);
        recursiveCannotPasteChildren(schema);
    }
    
    public static void recursiveCanPasteChildren(SchemaComponent target) {
        SchemaModel model = target.getModel();
        ArrayList<SchemaComponent> children = new ArrayList<SchemaComponent>(target.getChildren());
        for (SchemaComponent child : children) {
            recursiveCanPasteChildren(child);
        }
        if (target.getParent() != null) {
            target.getParent().canPaste(target);  //FIXME no assertion because can return false now
        }
    }

    public static void recursiveCannotPasteChildren(SchemaComponent target) {
        SchemaModel model = target.getModel();
        ArrayList<SchemaComponent> children = new ArrayList<SchemaComponent>(target.getChildren());
        for (SchemaComponent child : children) {
            recursiveCannotPasteChildren(child);
        }
        if (target.getParent() != null) {
            if (! (target instanceof SimpleTypeRestriction && target.getParent() instanceof LocalSimpleType)) {
            String msg = target.getClass().getName() + " canPaste " + target.getParent().getClass().getName();
            assertFalse(msg, target.canPaste(target.getParent()));
            }
        }
    }
}

