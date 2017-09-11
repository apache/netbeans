/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            super.addPropertyChangeListener (listener);
            listeners.add (listener);
        }        
        
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
