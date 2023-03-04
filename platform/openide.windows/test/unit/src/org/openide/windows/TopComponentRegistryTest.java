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

package org.openide.windows;

import java.awt.GraphicsEnvironment;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

public class TopComponentRegistryTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TopComponentRegistryTest.class);
    }

    public TopComponentRegistryTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testGetOpenedIsSafeToIterate() {
        TopComponent tc1 = new TopComponent();
        TopComponent tc2 = new TopComponent();
        TopComponent tc3 = new TopComponent();

        Set<TopComponent> all = TopComponent.getRegistry().getOpened();

        tc1.open();
        tc2.open();
        tc3.open();

        assertEquals("Contains 3 elements: " + all, 3, all.size());

        assertTrue("tc1 in set: ", all.contains(tc1));
        assertTrue("tc2 in set: ", all.contains(tc2));
        assertTrue("tc3 in set: ", all.contains(tc3));

        int cnt = 3;
        for (TopComponent c : all) {
            assertTrue("Can be closed", c.close());
            assertEquals("Now there are ", --cnt, all.size());
        }

        assertTrue("All components are closed", all.isEmpty());
    }
}
