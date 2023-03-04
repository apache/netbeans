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
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.*;

import org.openide.windows.*;


/** 
 *
 * @author Milos Kleint
 */
public class MultiViewFactoryTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewFactoryTest.class);
    }

    public MultiViewFactoryTest(String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testcreateMultiView () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createMultiView(descs, null);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createMultiView(null, null);
        assertNull(tc);
    }

    
    public void testCreateMultiView2 () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        MyClose close = new MyClose();
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1, close);
        assertNotNull(tc);
        
        tc.open();
        // just one element as shown..
        tc.close();
        // the close handler is not used, becasue all the elements are in consistent state
        assertFalse(close.wasUsed);
        
    }
    
   public void testCreateCloneableMultiView () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        CloneableTopComponent tc = MultiViewFactory.createCloneableMultiView(descs, desc1);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createCloneableMultiView(descs, null);
        assertNotNull(tc);
        
        tc = MultiViewFactory.createCloneableMultiView(null, null);
        assertNull(tc);
    }

    
    public void testCreateCloneableMultiView2 () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new MVElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        MyClose close = new MyClose();
        TopComponent tc = MultiViewFactory.createCloneableMultiView(descs, desc1, close);
        assertNotNull(tc);
        
        tc.open();
        // just one element as shown..
        tc.close();
        // the close handler is not used, becasue all the elements are in consistent state
        assertFalse(close.wasUsed);
        
    }    


//    public void testCreateSafeCloseState () throws Exception {
//        CloseOperationState state = MultiViewFactory.createSafeCloseState();
//        assertNotNull(state);
//        assertTrue(state.canClose());
//        assertNotNull(state.getDiscardAction());
//        assertNotNull(state.getProceedAction());
//        assertNotNull(state.getCloseWarningID());
//        
//    }

    
    public void testCreateUnsafeCloseState () throws Exception {
        CloseOperationState state = MultiViewFactory.createUnsafeCloseState("ID_UNSAFE", 
                                            MultiViewFactory.NOOP_CLOSE_ACTION, MultiViewFactory.NOOP_CLOSE_ACTION);
        assertNotNull(state);
        assertFalse(state.canClose());
        assertNotNull(state.getDiscardAction());
        assertNotNull(state.getProceedAction());
        assertEquals("ID_UNSAFE", state.getCloseWarningID());
        
        state = MultiViewFactory.createUnsafeCloseState( null, null, null);
        assertNotNull(state);
        assertFalse(state.canClose());
        assertNotNull(state.getDiscardAction());
        assertNotNull(state.getProceedAction());
        assertNotNull(state.getCloseWarningID());
        
    }
    
    
    private class MyClose implements CloseOperationHandler {
        
        public boolean wasUsed = false;
        public int supposed = 0;
        public boolean canClose = true;
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            wasUsed = true;
            return canClose;
        }
        
        
    }
    
}

