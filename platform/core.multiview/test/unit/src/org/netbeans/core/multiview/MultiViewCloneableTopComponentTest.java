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


package org.netbeans.core.multiview;

import java.awt.GraphicsEnvironment;
import javax.swing.JComponent;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;

import java.awt.Image;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JEditorPane;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.SourceViewMarker;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.io.NbMarshalledObject;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public class MultiViewCloneableTopComponentTest extends AbstractMultiViewTopComponentTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewCloneableTopComponentTest.class);
    }

    public MultiViewCloneableTopComponentTest(String name) {
        super (name);
    }
    
    protected TopComponent callFactory(MultiViewDescription[] desc, MultiViewDescription def) {
        return MultiViewFactory.createCloneableMultiView(desc, def);
    }    
    
    protected TopComponent callFactory(MultiViewDescription[] desc, MultiViewDescription def, CloseOperationHandler close) {
        return MultiViewFactory.createCloneableMultiView(desc, def, close);
    }
    
    protected Class getTopComponentClass() {
        return MultiViewCloneableTopComponent.class;
    }
    
    public void testPersistence() throws Exception {
        MVElem elem1 = new MVElem(new Action[] {new Act1("act1")} );
        SerMVElem elem2 = new SerMVElem();
        SerMVElem elem3 = new SerMVElem();
        elem2.deserializeTest = "testtesttest - 2";
        elem3.deserializeTest = "testtesttest - 3";
        
        MultiViewDescription desc1 = new SerMVDesc("desc1", null, TopComponent.PERSISTENCE_NEVER, elem1);
        MultiViewDescription desc2 = new SerMVDesc("desc2", null, TopComponent.PERSISTENCE_ONLY_OPENED, elem2);
        MultiViewDescription desc3 = new SerMVDesc("desc3", null, TopComponent.PERSISTENCE_ALWAYS, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2 };
        SerCloseHandler close = new SerCloseHandler("serializedvalue");
        
        TopComponent tc = callFactory(descs, desc2, close);
        tc.open();
        tc.requestActive();
        // testing closehandler here..
        tc.close();
        
        NbMarshalledObject mars = new NbMarshalledObject(tc);
        Object obj = mars.get();
        assertNotNull(obj);
        assertEquals(getTopComponentClass(), obj.getClass());
        tc = (MultiViewCloneableTopComponent)obj;
        
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        MultiViewPerspective[] descsAfter = handler.getPerspectives();
        assertNotNull(descsAfter);
        assertEquals(2, descsAfter.length);
        MultiViewPerspective selDesc = handler.getSelectedPerspective();
        assertNotNull(selDesc);
        assertEquals("desc2", selDesc.getDisplayName());
        tc.open();
        tc.requestActive();
        MultiViewCloneableTopComponent mvtc = (MultiViewCloneableTopComponent)tc;
        Collection cold = mvtc.getModel().getCreatedElements();
        // expected number of elements is one, because the elem3 was not initialized at all..
        assertEquals(1, cold.size());
        
        // test if the deserialized instance is there..
        SerMVElem elSelecto = (SerMVElem)mvtc.getModel().getActiveElement();
        assertEquals("testtesttest - 2", elSelecto.deserializeTest);
        assertEquals("componentOpened-componentShowing-componentActivated-", elSelecto.getLog());
        
        //testing if closehandler was correctly deserialized..
        tc.close();
        
        
    }    
    
    
    public void testSourceViewMarker() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new SourceMVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new SourceMVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        CloneableTopComponent tc = MultiViewFactory.createCloneableMultiView(descs, desc1);
        
        tc.open();
        tc.requestActive();
        
        CloneableEditorSupport.Pane pane = (CloneableEditorSupport.Pane)tc;
        JEditorPane editor = pane.getEditorPane();
        assertNotNull(editor);
        
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        assertFalse(desc3.equals(Accessor.DEFAULT.extractDescription(hand.getSelectedPerspective())));
        
    }
    public void testUpdateNameTellsAll() throws Exception {
        class P extends CloneableEditor {
            int cnt;
            boolean used;
            
            @Override
            public void updateName() {
                cnt++;
            }
        }
        final P edit1 = new P();
        final P edit2 = new P();
        final P edit3 = new P();
        
        
        MVElem elem1 = new MVElem() {
            @Override
            public JComponent getVisualRepresentation() {
                edit1.used = true;
                return edit1;
            }
        };
        MVElem elem2 = new MVElem() {
            @Override
            public JComponent getVisualRepresentation() {
                edit2.used = true;
                return edit2;
            }
        };
        MVElem elem3 = new SourceMVElem() {
            @Override
            public JComponent getVisualRepresentation() {
                edit3.used = true;
                return edit3;
            }
        };
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new SourceMVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        CloneableTopComponent tc = MultiViewFactory.createCloneableMultiView(descs, desc1);
        
        tc.open();
        tc.requestActive();
        CloneableEditorSupport.Pane pane = (CloneableEditorSupport.Pane)tc;
        
        assertTrue("First one is used", edit1.used);
        assertFalse("Second one is not used", edit2.used);
        assertFalse("Third one is not used", edit3.used);
        
        edit1.cnt = 0;
        edit2.cnt = 0;
        edit3.cnt = 0;
        pane.updateName();
        
        assertTrue("First one is used (obviously)", edit1.used);
        assertFalse("Second one is still not used", edit2.used);
        assertTrue("Third one is now used", edit3.used);
        
        assertEquals("Update name called on first as it is used", 1, edit1.cnt);
        assertEquals("Update name called on third as it marked", 1, edit3.cnt);
        assertEquals("No call to 2nd one", 0, edit2.cnt);
        
        MultiViewHandler h = MultiViews.findMultiViewHandler(tc);
        h.requestActive(h.getPerspectives()[1]);
        h.requestVisible(h.getPerspectives()[1]);
        
        edit1.cnt = 0;
        edit2.cnt = 0;
        edit3.cnt = 0;
        pane.updateName();
        
        assertTrue("1st is used", edit1.used);
        assertTrue("2nd is used", edit2.used);
        assertTrue("3rd is now used", edit3.used);
        
        assertEquals("All updateName called: 1st", 1, edit1.cnt);
        assertEquals("All updateName called: 2nd", 1, edit2.cnt);
        assertEquals("All updateName called: 3rd", 1, edit3.cnt);
    }
    
    private class SourceMVDesc extends MVDesc implements SourceViewMarker {
        public SourceMVDesc(String name, Image img, int persType, MultiViewElement element) {
            super(name, img, persType, element);
        }
        
    }
    
    private class SourceMVElem extends MVElem {

        @Override
        public JComponent getVisualRepresentation() {
            return new Pane();
        }
        
    }
    
    private class Pane extends JEditorPane implements CloneableEditorSupport.Pane {

        public JEditorPane getEditorPane() {
            return this;
        }

        public CloneableTopComponent getComponent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void updateName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void ensureVisible() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}

