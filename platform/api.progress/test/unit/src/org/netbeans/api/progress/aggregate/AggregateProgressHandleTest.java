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

package org.netbeans.api.progress.aggregate;

import junit.framework.TestCase;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.ProgressUIWorker;
import org.netbeans.modules.progress.spi.ProgressEvent;

/**
 *
 * @author mkleint
 */
public class AggregateProgressHandleTest extends TestCase {

    public AggregateProgressHandleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Controller.defaultInstance = new Controller(new ProgressUIWorker() {
            public void processProgressEvent(ProgressEvent event) { }
            public void processSelectedProgressEvent(ProgressEvent event) { }
        }) {
        };
    }

    public void testContributorShare() throws Exception {
        ProgressContributor contrib1 = AggregateProgressFactory.createProgressContributor("1");
        ProgressContributor contrib2 = AggregateProgressFactory.createProgressContributor("2");
        AggregateProgressHandle handle = AggregateProgressFactory.createHandle("fact1", new ProgressContributor[] { contrib1, contrib2}, null, null);
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib2.getRemainingParentWorkUnits());
        
        ProgressContributor contrib3 = AggregateProgressFactory.createProgressContributor("3");
        handle.addContributor(contrib3);
        assertEquals(AggregateProgressHandle.WORKUNITS /3, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /3, contrib2.getRemainingParentWorkUnits());
        // the +1 deal is there because of the rounding, the last one gest the remainder
        assertEquals(AggregateProgressHandle.WORKUNITS /3 + 1, contrib3.getRemainingParentWorkUnits());
    }
    
    public void testDynamicContributorShare() throws Exception {
        ProgressContributor contrib1 = AggregateProgressFactory.createProgressContributor("1");
        AggregateProgressHandle handle = AggregateProgressFactory.createHandle("fact1", new ProgressContributor[] { contrib1}, null, null);
        assertEquals(AggregateProgressHandle.WORKUNITS, contrib1.getRemainingParentWorkUnits());
    
        handle.start();
        contrib1.start(100);
        contrib1.progress(50);
        assertEquals(AggregateProgressHandle.WORKUNITS /2, contrib1.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /2, handle.getCurrentProgress());
        
        ProgressContributor contrib2 = AggregateProgressFactory.createProgressContributor("2");
        handle.addContributor(contrib2);
        assertEquals(AggregateProgressHandle.WORKUNITS /4, contrib2.getRemainingParentWorkUnits());
        contrib1.finish();
        assertEquals(AggregateProgressHandle.WORKUNITS /4 * 3, handle.getCurrentProgress());
        
        ProgressContributor contrib3 = AggregateProgressFactory.createProgressContributor("3");
        handle.addContributor(contrib3);
        assertEquals(AggregateProgressHandle.WORKUNITS /8, contrib2.getRemainingParentWorkUnits());
        assertEquals(AggregateProgressHandle.WORKUNITS /8, contrib3.getRemainingParentWorkUnits());
        contrib3.start(100);
        contrib3.finish();
        assertEquals((AggregateProgressHandle.WORKUNITS /4 * 3) + (AggregateProgressHandle.WORKUNITS /8), 
                     handle.getCurrentProgress());
        
        
    }
    
}
