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
import org.netbeans.core.api.multiview.MultiViews;
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
public class CloseOperationHandlerTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(CloseOperationHandlerTest.class);
    }

    public CloseOperationHandlerTest(String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testCreateMultiView2 () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new NonClosableElem());
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, new NonClosableElem());
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        MyCloseHandler close = new MyCloseHandler();
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1, close);
        assertNotNull(tc);
        
         tc.open();
        // just one element as shown..
        close.supposed = 1;
        tc.close();
        assertTrue(close.wasUsed);
        
        tc.open();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        handler.requestActive(handler.getPerspectives()[2]);
        // 1 shall be checked - elem3 can be closed.
        close.supposed = 1;
        // do not allow closing..
        close.canClose = false;
        
        tc.close();  
        assertTrue(tc.isOpened());

        
        handler.requestActive(handler.getPerspectives()[1]);
        // 2 shall be checked.
        close.supposed = 2;
        // allow closing..
        close.canClose = true;
        
        tc.close();  
        assertTrue(!tc.isOpened());
        
    }

    
    private class MyCloseHandler implements CloseOperationHandler {
        
        public boolean wasUsed = false;
        public int supposed = 0;
        public boolean canClose = true;
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            wasUsed = true;
            if (supposed != elements.length) {
                throw new IllegalStateException("A different number of elements returned. Expected=" + supposed + " but was:" + elements.length);
            }
            return canClose;
        }
        
        
    }
    
    private class NonClosableElem extends MVElem {
        
        public CloseOperationState canCloseElement() {
            return MultiViewFactory.createUnsafeCloseState("ID", MultiViewFactory.NOOP_CLOSE_ACTION, MultiViewFactory.NOOP_CLOSE_ACTION);
        }
        
    }
    
}

