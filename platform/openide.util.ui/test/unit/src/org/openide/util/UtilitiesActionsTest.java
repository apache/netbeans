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

package org.openide.util;

import java.lang.ref.*;
import javax.swing.Action;
import javax.swing.JMenuItem;

import junit.framework.*;

import org.netbeans.junit.*;

/** Tests of actions related methods in Utilities class.
 */
public class UtilitiesActionsTest extends NbTestCase {

    public UtilitiesActionsTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(UtilitiesActionsTest.class));
    }

    public void testActionClone () throws Exception {
        CloneAction original = new CloneAction ();
        
        javax.swing.Action clone = original.createContextAwareInstance(Lookup.EMPTY);

        assertTrue ("Clone is instance of desired class", clone instanceof CloneAction);
        assertNull ("Original has empty lookup", original.lookup);
        assertEquals ("Clone has lookup assigned", Lookup.EMPTY, ((CloneAction)clone).lookup);
    }
    
    
    public void testActionToPopupForLookup () throws Exception {
        class MyAction extends CloneAction 
        implements org.openide.util.actions.Presenter.Popup {
            public JMenuItem item = new JMenuItem ("Ahoj");
            public int called;

            @Override
            protected CloneAction cloneAction () {
                return new MyAction ();
            }

            
            public JMenuItem getPopupPresenter () {
                called++;
                return item;
            }
        }
        
        final CloneAction[] arr = { 
            new CloneAction (), new MyAction ()
        };
        
        final javax.swing.JPopupMenu menu = org.openide.util.Utilities.actionsToPopup (arr, Lookup.EMPTY);
        
        assertEquals ("Presenter called", 1, ((MyAction)arr[1].clone).called);
        assertEquals ("Lookup is fine", ((CloneAction)arr[0].clone).lookup, Lookup.EMPTY);
        assertEquals ("The other as well", ((CloneAction)arr[1].clone).lookup, Lookup.EMPTY);
        
        // We need to do this stuff in AWT; cf. Actions.Bridge.propertyChange.
        final AssertionFailedError[] err = new AssertionFailedError[1];
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                 try {
        
        javax.swing.MenuElement[] elem = menu.getSubElements ();
        assertEquals ("Two subitems", 2, elem.length);

        // Otherwise there may be no listener:
        elem[0].getComponent().addNotify();
        
        arr[0].clone.setEnabled(false);
        assertTrue ("Menu item is disabled", !((JMenuItem)elem[0].getComponent()).isEnabled ());
        arr[0].clone.setEnabled(true);
        assertTrue ("Menu item is enabled", ((JMenuItem)elem[0].getComponent()).isEnabled ());
        
        assertEquals ("The presenter is really used", ((MyAction)arr[1].clone).item, elem[1].getComponent ());
        
                 } catch (AssertionFailedError e) {
                     err[0] = e;
                 }
            }
        });
        if (err[0] != null) throw err[0];
    }
    
    public void testActionsToPopupFromComponent () throws Exception {
        Lookup lookup = org.openide.util.lookup.Lookups.singleton (this);
        final Lookup[] returnedLookup = {lookup};
        
        class Provider extends javax.swing.JComponent implements Lookup.Provider {
            public Lookup getLookup () {
                return returnedLookup[0];
            }
        }
        
        CloneAction sample = new CloneAction ();
        
        
        javax.swing.JComponent outerMost = new javax.swing.JPanel ();
        outerMost.getActionMap().put ("1", sample);
        
        javax.swing.JComponent provider = new Provider ();
        outerMost.add (provider);
        provider.getActionMap().put ("2", sample);
        
        javax.swing.JComponent child = new javax.swing.JPanel ();
        provider.add (child);
        child.getActionMap().put ("3", sample);
        javax.swing.JComponent menuOwner = new javax.swing.JPanel ();  
        child.add (menuOwner);
        menuOwner.getActionMap().put ("4", sample);
        
        javax.swing.JComponent sibling = new javax.swing.JPanel ();
        provider.add (child);        
        sibling.getActionMap().put ("5", sample);

        
        CloneAction[] arr = { new CloneAction () };
        
        javax.swing.JPopupMenu menu = org.openide.util.Utilities.actionsToPopup (arr, menuOwner);
        
        Lookup actionLookup = arr[0].clone.lookup;
        assertNotNull("Clone lookup is not null", actionLookup);
        assertEquals ("The lookup returned by 'provider' is assigned to the clonned actions", 
            this, actionLookup.lookup (this.getClass ())
        );
        
        // Now try it without a (valid) Lookup.Provider in the component hierarchy.
        returnedLookup[0] = null;
        arr[0].clone = null;
        menu = org.openide.util.Utilities.actionsToPopup (arr, menuOwner);
        
        actionLookup = arr[0].clone.lookup;
        assertNotNull("Clone lookup is not null", actionLookup);
        
        javax.swing.ActionMap map = (javax.swing.ActionMap)actionLookup.lookup (javax.swing.ActionMap.class);
        assertNotNull ("Action map is in the lookup", map);
        assertNull ("ActionMap of parent of Lookup.Provider is not included", map.get ("1"));
        assertNotNull ("ActionMap of Lookup.Provider is included", map.get ("2"));
        assertNotNull ("So is the child's one", map.get ("3"));
        assertNotNull ("And also of the menuOwner", map.get ("4"));
        assertNull ("But of course the sibling's one is not", map.get ("5"));
    }
    
    class CloneAction extends javax.swing.AbstractAction implements ContextAwareAction {
        public Lookup lookup;
        public CloneAction clone;

        public void actionPerformed (java.awt.event.ActionEvent ev) {
        }
        
        protected CloneAction cloneAction () {
            return new CloneAction ();
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            assertNull ("We support just one clone right now", clone);
            clone = cloneAction ();
            clone.lookup = actionContext;
            return clone;
        }
        
    } // end of CloneAction
    
}
