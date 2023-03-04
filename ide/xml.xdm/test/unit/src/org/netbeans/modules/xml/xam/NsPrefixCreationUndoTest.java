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

package org.netbeans.modules.xml.xam;

import javax.swing.text.Document;
import junit.framework.*;
import javax.swing.undo.UndoManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.xam.TestComponent3.A;
import org.netbeans.modules.xml.xdm.Util;

/**
 *
 * @author Nikita Krjukov
 */
public class NsPrefixCreationUndoTest extends NbTestCase {
    AbstractModelTest.PropertyListener plistener;
    AbstractModelTest.TestComponentListener listener;
    TestModel3 model;
    Document doc;

    public NsPrefixCreationUndoTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        listener = new AbstractModelTest.TestComponentListener();
        plistener = new AbstractModelTest.PropertyListener();
    }
    
    private void defaultSetup() throws Exception {
        doc = Util.getResourceAsDocument("resources/test1.xml");
        model = Util.loadModel(doc);
        model.addComponentListener(listener);
        model.addPropertyChangeListener(plistener);
    }

    @Override
    protected void tearDown() throws Exception {
        if (model != null) {
            model.removePropertyChangeListener(plistener);
            model.removeComponentListener(listener);
        }
    }

    public static Test suite() {
        return new TestSuite(NsPrefixCreationUndoTest.class);
    }

    /**
     * Tests Issue #166177
     * @throws Exception
     */
    public void testUndoNsPrefixCreation() throws Exception {
        defaultSetup();
        final UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);
        A a = model.getRootComponent().getChild(A.class);
        //
        String xdmModelTextInitial = Util.getXdmBasedModelText(model);
        String xamModelTextInitial = Util.getXamBasedModelText(model);
        //
        // Add child component to A element
        model.startTransaction();
        try {
            TestComponent3.Aa newChildAa = new TestComponent3.Aa(model, 1);
            a.appendChild("setup", newChildAa);
        } finally {
            model.endTransaction();
        }
        //
        TestComponent3.Aa newAa = a.getChild(TestComponent3.Aa.class);
        assertEquals(TestComponent3.NS2_URI, newAa.getNamespaceURI());
        //
        String xdmModelTextAfterAdd = Util.getXdmBasedModelText(model);
        String xamModelTextAfterAdd = Util.getXamBasedModelText(model);
        //
        ur.undo();
        //
        // Check the XDM and XAM models' structure return back to initial state after undo.
        String xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextInitial);
        //
        String xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextInitial);
        //
        ur.redo();
        //
        xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextAfterAdd);
        //
        xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextAfterAdd);
        //
        ur.undo();
        //
        xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextInitial);
        //
        xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextInitial);
        //
    }

    /**
     * Tests procesing of an interruption inside of ComponentUpdater with
     * a RuntimeException. According to TestComponentUpdater class,
     * a RuntimeException should be thrown when an Err element is added to the model. 
     * 
     * This test shows that regardless of the exception, the XAM model remains 
     * valid due to AbstractDocumentModel.refresh() method is called. 
     *
     * @throws Exception
     */
    public void testInterruptedComponentUpdater() throws Exception {
        defaultSetup();
        final UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);
        A a = model.getRootComponent().getChild(A.class);
        //
        String xdmModelTextInitial = Util.getXdmBasedModelText(model);
        String xamModelTextInitial = Util.getXamBasedModelText(model);
        //
        // Add child component to A element
        model.startTransaction();
        try {
            TestComponent3.Err newChildErr = new TestComponent3.Err(model, 1);
            a.appendChild(TestComponent3.Err.LNAME, newChildErr);
        } finally {
            model.endTransaction();
        }
        //
        TestComponent3.Err newErr = a.getChild(TestComponent3.Err.class);
        assertEquals(TestComponent3.NS_URI, newErr.getNamespaceURI());
        //
        String xdmModelTextAfterAdd = Util.getXdmBasedModelText(model);
        String xamModelTextAfterAdd = Util.getXamBasedModelText(model);
        //
        ur.undo();
        //
        // Check the XDM and XAM models' structure return back to initial state after undo.
        String xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextInitial);
        //
        String xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextInitial);
        //
        try {
            ur.redo();
        } catch (Exception ex) {
            String exMsg = ex.getMessage();
            assertEquals(exMsg, "Test synch crashed.");
        }
        //
        xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextAfterAdd);
        //
        xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextAfterAdd);
        //
        ur.undo();
        //
        xdmModelText = Util.getXdmBasedModelText(model);
        assertEquals(xdmModelText, xdmModelTextInitial);
        //
        xamModelText = Util.getXamBasedModelText(model);
        assertEquals(xamModelText, xamModelTextInitial);
        //
    }

}
