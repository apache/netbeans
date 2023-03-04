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

import java.io.Serializable;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.*;
import org.openide.util.Lookup;

import org.openide.windows.*;


/**
 *
 * @author Milos Kleint
 */
public class MultiViewsTest extends NbTestCase {
    
    public MultiViewsTest(String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    
    
    public void testcreateMultiViewHandler () throws Exception {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, new MVElem());
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
        assertNotNull(hand);
        
        tc = new TopComponent();
        hand = MultiViews.findMultiViewHandler(tc);
        assertNull(hand);
        
        hand = MultiViews.findMultiViewHandler(null);
        assertNull(hand);

    }

    public void testNonExistingMimeType() throws Exception {
        final Lookup lkp = Lookup.EMPTY;
        
        class L implements Lookup.Provider, Serializable {
            @Override
            public Lookup getLookup() {
                return lkp;
            }
        }
        TopComponent tc = MultiViews.createMultiView("text/x-does-not-exist", new L());
        tc.open();
    }
    
}

