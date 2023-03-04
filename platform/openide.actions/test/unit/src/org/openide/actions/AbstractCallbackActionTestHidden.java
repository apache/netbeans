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

package org.openide.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/** Test behaviour of regular callback actions.
 */
public abstract class AbstractCallbackActionTestHidden extends NbTestCase {
    public AbstractCallbackActionTestHidden(String name) {
        super(name);
    }

    /** global action */
    protected CallbackSystemAction global;
    
    /** our action that is being added into the map */
    protected OurAction action = new OurAction ();
    
    /** map that we lookup action in */
    protected ActionMap map;
    /** the clonned action */
    protected Action clone;
    
    /** listener that is attached to the clone action and allows counting of prop events.*/
    protected CntListener listener;
    
    /** that is the action being clonned to */
    private Lookup lookup;
    
    /** Which action to test.
     */
    protected abstract Class<? extends CallbackSystemAction> actionClass();
    
    /** The key that is used in the action map
     */
    protected abstract String actionKey ();

    protected boolean runInEQ () {
        return true;
    }
    
    protected void setUp() throws Exception {
        global = SystemAction.get(actionClass());
        map = new ActionMap ();
        map.put (actionKey (), action);
        lookup = Lookups.singleton(map);
        // Retrieve context sensitive action instance if possible.
        clone = global.createContextAwareInstance(lookup);
        
        listener = new CntListener ();
        clone.addPropertyChangeListener(listener);
    }
    
    public void testThatDefaultEditorKitPasteActionIsTheCorrectKeyOfPasteAction () {
        clone.actionPerformed (new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        action.assertCnt ("Clone correctly delegates to OurAction", 1);
    }
    
    public void testChangesAreCorrectlyPropagatedToTheDelegate () {
        action.setEnabled (true);
        
        assertTrue ("Clone is correctly enabled", clone.isEnabled ());
        
        action.setEnabled (false);
        assertTrue ("Clone is disabled", !clone.isEnabled());
        listener.assertCnt ("Change notified", 1);
        
        action.setEnabled (true);
        listener.assertCnt ("Change notified again", 1);
        assertTrue ("Clone is correctly enabled", clone.isEnabled ());
    }
    
    protected static final class OurAction extends AbstractAction {
        private int cnt;
        private Set<PropertyChangeListener> listeners = new HashSet<PropertyChangeListener>();
        
        public void actionPerformed(ActionEvent e) {
            cnt++;
        }
        
        public void assertCnt (String msg, int count) {
            assertEquals (msg, count, this.cnt);
            this.cnt = 0;
        }
        
        public void assertListeners (String msg, int count) throws Exception {
            if (count == 0) {
                synchronized (this) {
                    int c = 5;
                    while (this.listeners.size () != 0 && c-- > 0) {
                        System.gc ();
                        wait (500);
                    }
                }
            }
            
            if (count != this.listeners.size ()) {
                fail (msg + " listeners expected: " + count + " but are " + this.listeners);
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener (listener);
            listeners.add (listener);
        }

        @Override
        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            super.removePropertyChangeListener (listener);
            listeners.remove (listener);
            notifyAll ();
        }
    } // end of OurAction
    
    protected static final class CntListener implements PropertyChangeListener {
        private int cnt;
        
        public void propertyChange(PropertyChangeEvent evt) {
            cnt++;
        }
        
        public void assertCnt (String msg, int count) {
            assertEquals (msg, count, this.cnt);
            this.cnt = 0;
        }
    } // end of CntListener
}
