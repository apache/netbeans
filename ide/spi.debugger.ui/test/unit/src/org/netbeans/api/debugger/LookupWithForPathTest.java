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

package org.netbeans.api.debugger;

import java.util.List;
import org.netbeans.api.debugger.test.TestLookupServiceFirst;
import org.netbeans.api.debugger.test.TestNodeModelContext;
import org.netbeans.spi.viewmodel.NodeModel;

/**
 *
 * @author Martin Entlicher
 */
public class LookupWithForPathTest  extends DebuggerApiTestBase {

    static {
        String[] layers = new String[] {"org/netbeans/api/debugger/test/mf-layer.xml"};//NOI18N
        Object[] instances = new Object[] { };
        IDEInitializer.setup(layers,instances);
    }

    public LookupWithForPathTest(String s) {
        super(s);
    }

    public void testForPath() throws Exception {
        Lookup.MetaInf l = new Lookup.MetaInf("unittest");
        List list = l.lookup(null, TestLookupServiceFirst.class);
        assertEquals("Wrong looked up object", 2, list.size());
        assertInstanceOf("Wrong looked up object", list.get(0), TestLookupServiceFirst.class);
        assertInstanceOf("Wrong looked up object", list.get(1), TestLookupServiceFirst.class);

        Lookup testContext = new Lookup.Instance(new Object[] {});
        l.setContext(testContext);
        List<? extends NodeModel> nodeModelList = l.lookup(null, NodeModel.class);
        assertEquals("Wrong looked up object", 2, nodeModelList.size());
        assertInstanceOf("Wrong looked up object", nodeModelList.get(0), NodeModel.class);
        assertInstanceOf("Wrong looked up object", nodeModelList.get(1), NodeModel.class);
        assertInstanceOf("Wrong looked up object", nodeModelList.get(1), TestNodeModelContext.class);
        TestNodeModelContext nmc = (TestNodeModelContext) nodeModelList.get(1);
        assertNotNull(nmc.getContext());
        assertEquals(testContext, nmc.getContext());
    }
}
