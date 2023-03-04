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
package org.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ContextManagerIntegrationTest extends NbTestCase 
implements Lookup.Provider {
    private TopComponent currentTC;
    
    public ContextManagerIntegrationTest(String name) {
        super(name);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testSurviveFocusChange() throws Exception {
        InstanceContent ic = new InstanceContent();
        final Lookup lkp = new AbstractLookup(ic);
        final TopComponent tc = new TopComponent() {
            {
                associateLookup(lkp);
            }
        };
        currentTC = tc;

        Lookup revolving = Lookups.proxy(this);
        
        Action clone = ((ContextAwareAction) Actions.forID("cat33", "survive1")).createContextAwareInstance(revolving);
        L listener = new L();
        clone.addPropertyChangeListener(listener);
        
        assertFalse("Disabled", clone.isEnabled());
        Object val = Integer.valueOf(1);
        ic.add(val);
        assertTrue("Enabled now", clone.isEnabled());
        assertEquals("One change", 1, listener.cnt);
        
        currentTC = new TopComponent();
        currentTC.setActivatedNodes(null);
        revolving.lookup(Object.class);
        
        assertTrue("Still Enabled", clone.isEnabled());
        final Node[] arr = new Node[] { Node.EMPTY.cloneNode() };
        currentTC.setActivatedNodes(arr);
        revolving.lookup(Object.class);
        
        assertFalse("Disabled now", clone.isEnabled());
        
        Survival.value = 0;
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("No change", 0, Survival.value);
        
        currentTC = tc;
        revolving.lookup(Object.class);
        assertTrue("Enabled again", clone.isEnabled());
        
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("Incremented", 1, Survival.value);
        
        currentTC = new TopComponent();
        currentTC.setActivatedNodes(null);
        revolving.lookup(Object.class);
        
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("Incremented again", 2, Survival.value);
    }

    @Override
    public Lookup getLookup() {
        return currentTC.getLookup();
    }
    
    private static class L implements PropertyChangeListener {
        int cnt;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            cnt++;
        }
    }
    
    @ActionID(category="cat33", id="survive1")
    @ActionRegistration(displayName="Survive", surviveFocusChange=true)
    public static final class Survival implements ActionListener {
        static int value;
        
        private Integer context;

        public Survival(Integer context) {
            this.context = context;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            value += context;
        }
    }
}
