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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.*;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public class MultiViewHandlerTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewHandlerTest.class);
    }

    public MultiViewHandlerTest(String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testRequestVisible() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        assertNotNull(hand);
        assertEquals(hand.getPerspectives().length, 3);
        MultiViewPerspective pers = hand.getSelectedPerspective();

        assertEquals(Accessor.DEFAULT.extractDescription(pers), desc1);
        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        assertEquals("componentOpened-componentShowing-", elem1.getLog());
        assertEquals("",elem2.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        
        assertEquals(Accessor.DEFAULT.extractDescription(hand.getSelectedPerspective()), desc2);
        assertEquals("componentOpened-componentShowing-componentHidden-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-", elem2.getLog());
        assertEquals("", elem3.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        assertEquals("componentOpened-componentShowing-componentHidden-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentHidden-", elem2.getLog());
        assertEquals("componentOpened-componentShowing-", elem3.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        assertEquals("componentOpened-componentShowing-componentHidden-", elem3.getLog());
        assertEquals("componentOpened-componentShowing-componentHidden-componentShowing-", elem1.getLog());
        
    }

    
    public void testRequestActive() throws Exception {
        final MVElem elem1 = new MVElem();
        final MVElem elem2 = new MVElem();
        final MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc2);
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        assertNotNull(hand);
        assertEquals(hand.getPerspectives().length, 3);
        
        MultiViewPerspective pers = hand.getSelectedPerspective();

        assertEquals(Accessor.DEFAULT.extractDescription(pers), desc2);
        // NOT OPENED YET.
        assertEquals("",elem1.getLog());
        assertEquals("",elem2.getLog());
        
        tc.open();
        tc.requestActive();
        assertEquals("",elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
        // test related hack, easy establishing a  connection from Desc->perspective
//        System.err.println("start caring..........................");
        hand.requestActive(Accessor.DEFAULT.createPerspective(desc1));
//        System.err.println("elem1=" + elem1.getLog());
//        System.err.println("elem2=" + elem2.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());

        // do request active the same element, nothing should happen.
        // test related hack, easy establishing a  connection from Desc->perspective
        hand.requestActive(Accessor.DEFAULT.createPerspective(desc1));
        assertEquals("componentOpened-componentShowing-componentActivated-", elem1.getLog());
        assertEquals("componentOpened-componentShowing-componentActivated-componentDeactivated-componentHidden-", elem2.getLog());
        assertEquals("",elem3.getLog());
        
    }
    
    
}

