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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Checks behaviour of a bridge that listens on a provided lookup
 * and initializes activated nodes according to the nodes in the
 * lookup.
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
public final class TopComponentLookupToNodesBridge extends NbTestCase {
    /** own action map */
    protected ActionMap map;
    /** top component we work on */
    protected TopComponent top;
    /** instance in the lookup */
    protected InstanceContent ic;
    /** its lookup */
    protected Lookup lookup;
    
    public TopComponentLookupToNodesBridge(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    /** Setup component with lookup.
     */
    protected void setUp() {
        System.setProperty("org.openide.util.Lookup", "-");
        
        map = new ActionMap();
        ic = new InstanceContent();
        ic.add(map);
        
        lookup = new AbstractLookup(ic);
        top = new TopComponent(lookup);
    }
    
    
    public void testTheLookupIsReturned() {
        assertEquals("Lookup provided to TC in constructor is returned", lookup, top.getLookup());
    }
    
    public void testActionMapIsTakenFromTheLookupIfProvided() {
        Action a1 = new Action();
        map.put("key", a1);
        
        assertEquals("Action map is set", a1, top.getActionMap().get("key"));
        
        ActionMap another = new ActionMap();
        
        ic.set(Collections.singleton(another), null);
        assertEquals("And is not changed (right now) if modified in list", a1, top.getActionMap().get("key"));
    }
    
    public void testEmptyLookupGeneratesZeroLengthArray() {
        assertNotNull("Array is there", top.getActivatedNodes());
        assertEquals("No nodes", 0, top.getActivatedNodes().length);
    }
    
    public void testNodeIsThereIfInLookup() {
        class Listener implements PropertyChangeListener {
            public int cnt;
            public String name;
            
            public void propertyChange(PropertyChangeEvent ev) {
                cnt++;
                name = ev.getPropertyName();
            }
        }
        
        Listener l = new Listener();
        top.addPropertyChangeListener(l);
        
        ic.add(Node.EMPTY);
        
        assertNotNull("Array exists", top.getActivatedNodes());
        assertEquals("One node", 1, top.getActivatedNodes().length);
        assertEquals("The node", Node.EMPTY, top.getActivatedNodes()[0]);
        assertEquals("One PCE", 1, l.cnt);
        assertEquals("Name of property", "activatedNodes", l.name);
        
        
        ic.set(Collections.nCopies(2, Node.EMPTY), null);
        
        assertEquals("Two nodes", 2, top.getActivatedNodes().length);
        assertEquals("The same", Node.EMPTY, top.getActivatedNodes()[0]);
        assertEquals("The same", Node.EMPTY, top.getActivatedNodes()[1]);
        assertEquals("second PCE change", 2, l.cnt);
        assertEquals("Name of property", "activatedNodes", l.name);
        
    }
    
    private static final class Action extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
        }
    }
}
