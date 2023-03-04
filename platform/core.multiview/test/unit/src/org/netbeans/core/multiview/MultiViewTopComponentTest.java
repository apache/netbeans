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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;

import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.openide.util.io.NbMarshalledObject;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public class MultiViewTopComponentTest extends AbstractMultiViewTopComponentTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewTopComponentTest.class);
    }

    public MultiViewTopComponentTest(String name) {
        super (name);
    }
    
    protected TopComponent callFactory(MultiViewDescription[] desc, MultiViewDescription def) {
        return MultiViewFactory.createMultiView(desc, def);
    }    
    
    protected TopComponent callFactory(MultiViewDescription[] desc, MultiViewDescription def, CloseOperationHandler close) {
        return MultiViewFactory.createMultiView(desc, def, close);
    }
    
    protected Class getTopComponentClass() {
        return MultiViewTopComponent.class;
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
        tc = (MultiViewTopComponent)obj;
        
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        MultiViewPerspective[] descsAfter = handler.getPerspectives();
        assertNotNull(descsAfter);
        assertEquals(2, descsAfter.length);
        MultiViewPerspective selDesc = handler.getSelectedPerspective();
        assertNotNull(selDesc);
        assertEquals("desc2", selDesc.getDisplayName());
        tc.open();
        tc.requestActive();
        MultiViewTopComponent mvtc = (MultiViewTopComponent)tc;
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

    /** Tests that multiple instances of the same class propagate well
     * into array of actions of enclosing multiview TopComponent
     */
    public void testFix_132948_MoreActionsOfSameClass () {
        Action[] acts = new Action[] {
                new MultiViewActionMapTest.TestAction("First"),
                new MultiViewActionMapTest.TestAction("Second"),
                new MultiViewActionMapTest.TestAction("Third")
        };
        Action[] actsCopy = new Action[acts.length];
        System.arraycopy(acts, 0, actsCopy, 0, acts.length);
        
        MVElem elem = new MVElem(actsCopy);
        MultiViewDescription desc = new MVDesc("desc1", null, 0, elem);
        TopComponent tc = MultiViewFactory.createMultiView(new MultiViewDescription[] { desc }, desc);

        MultiViewTopComponent mvtc = (MultiViewTopComponent)tc;
        mvtc.setSuperActions(actsCopy);
        
        List<Action> tcActs = Arrays.asList(tc.getActions());
        
        for (int i = 0; i < acts.length; i++) {
            Action action = acts[i];
            assertTrue("Action " + action.getValue(Action.NAME) + 
                    " not propagated into multiview TC actions", 
                    tcActs.contains(action));
        }
    }
    
    
    /** Tests that multiple instances of the same class propagate well
     * into array of actions of enclosing multiview TopComponent
     */
    public void testFix_204072_MissingIcon() {
        final Image img = new BufferedImage( 10, 10, BufferedImage.TYPE_INT_RGB );
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", img, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        TopComponent tc = MultiViewFactory.createMultiView(new MultiViewDescription[] { desc1, desc2 }, desc1);

        tc.open();
        tc.requestActive();
        
        assertEquals( img, tc.getIcon() );
        
        MultiViewTopComponent mvtc = ( MultiViewTopComponent ) tc;
        mvtc.getModel().setActiveDescription( desc2 );
        
        assertEquals( img, tc.getIcon() );
    }
}

