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

import javax.swing.ActionMap;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Jaroslav Tulach
 */
public class PreventNeedlessChangesOfActionMapTest extends NbTestCase
implements LookupListener {
    private TopComponent tc;
    private Lookup.Result res;
    private int cnt;
    
    /** Creates a new instance of PreventNeedlessChangesOfActionMapTest */
    public PreventNeedlessChangesOfActionMapTest(String s) {
        super(s);
    }
    
    protected void setUp() throws Exception {
        tc = new TopComponent();
        res = tc.getLookup().lookup(new Lookup.Template<ActionMap> (ActionMap.class));
        assertEquals("One instance", 1, res.allItems().size());
        
        res.addLookupListener(this);
    }
    
    public void testChangeOfNodeDoesNotFireChangeInActionMap() {
        ActionMap am = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        assertNotNull(am);
        
        Node m1 = new AbstractNode(Children.LEAF);
        m1.setName("old m1");
        Node m2 = new AbstractNode(Children.LEAF);
        m2.setName("new m2");
        
        tc.setActivatedNodes(new Node[] { m1 });
        assertEquals("No change in ActionMap 1", 0, cnt);
        tc.setActivatedNodes(new Node[] { m2 });
        assertEquals("No change in ActionMap 2", 0, cnt);
        tc.setActivatedNodes(new Node[0]);
        assertEquals("No change in ActionMap 3", 0, cnt);
        tc.setActivatedNodes(null);
        assertEquals("No change in ActionMap 4", 0, cnt);
        
        ActionMap am2 = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        assertEquals("Still the same action map", am, am2);
    }

    public void resultChanged(LookupEvent ev) {
        cnt++;
    }
    
}
