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

package org.netbeans.spi.project.support.ant;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.MockChangeListener;

/**
 * @author Jesse Glick
 */
public class FilterPropertyProviderTest extends NbTestCase {

    public FilterPropertyProviderTest(String name) {
        super(name);
    }

    public void testDelegatingPropertyProvider() throws Exception {
        AntBasedTestUtil.TestMutablePropertyProvider mpp = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        DPP dpp = new DPP(mpp);
        MockChangeListener l = new MockChangeListener();
        dpp.addChangeListener(l);
        assertEquals("initially empty", Collections.emptyMap(), dpp.getProperties());
        mpp.defs.put("foo", "bar");
        mpp.mutated();
        l.assertEvent();
        assertEquals("now right contents", Collections.singletonMap("foo", "bar"), dpp.getProperties());
        AntBasedTestUtil.TestMutablePropertyProvider mpp2 = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        mpp2.defs.put("foo", "bar2");
        dpp.setDelegate_(mpp2);
        l.msg("got a change from new delegate").assertEvent();
        assertEquals("right contents from new delegate", Collections.singletonMap("foo", "bar2"), dpp.getProperties());
        mpp2.defs.put("foo", "bar3");
        mpp2.mutated();
        l.msg("got a change in new delegate").assertEvent();
        assertEquals("right contents", Collections.singletonMap("foo", "bar3"), dpp.getProperties());
        Reference<?> r = new WeakReference<Object>(mpp);
        mpp = null;
        assertGC("old delegates can be collected", r);
        r = new WeakReference<Object>(dpp);
        dpp = null; // but not mpp2
        assertGC("delegating PP can be collected when delegate is not", r); // #50572
    }

    private static final class DPP extends FilterPropertyProvider {
        public DPP(PropertyProvider pp) {
            super(pp);
        }
        public void setDelegate_(PropertyProvider pp) {
            setDelegate(pp);
        }
    }

}
