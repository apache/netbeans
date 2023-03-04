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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JPanel;
import org.netbeans.junit.NbTestCase;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/** Test of behaviour of manager listening for ActionMap in a lookup.
 *
 * @author Jaroslav Tulach
 */
public class GlobalManagerTest extends NbTestCase {
    
    public GlobalManagerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testFindManager() {
        doFindManager(true);
    }
    public void testFindManagerNoSurvive() {
        doFindManager(false);
    }
    
    private void doFindManager(boolean survive) {
        Lookup context = new AbstractLookup(new InstanceContent());
        
        GlobalManager r1 = GlobalManager.findManager(context, survive);
        assertNotNull("Need an instace", r1);
        GlobalManager r2 = GlobalManager.findManager(context, survive);
        assertEquals("Caches", r1, r2);

        Lookup c3 = new AbstractLookup(new InstanceContent());
        GlobalManager r3 = GlobalManager.findManager(c3, survive);
        if (r3 == r2) {
            fail("Need next manager for new lookup: " + r2 + " e: " + r3);
        }
        
        r1 = null;
        WeakReference<?> ref = new WeakReference<GlobalManager>(r2);
        r2 = null;
        assertGC("Disappers", ref);
        
        WeakReference<?> lookupRef = new WeakReference<Lookup>(c3);
        c3 = null;
        r3 = null;
        
        assertGC("Lookup can also disappear", lookupRef);
    }

    public void testActionsCanHoldOnLookup() {
        class TopComponent extends JPanel implements Lookup.Provider {
            Lookup l;

            void associateLookup(Lookup f) {
                l = f;
            }

            public Lookup getLookup() {
                return l;
            }
        }
        TopComponent tc = new TopComponent();
        class CAA extends AbstractAction implements
                ContextAwareAction {
            public void actionPerformed(ActionEvent arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Action createContextAwareInstance(Lookup actionContext) {
                return this;
            }

        }
        tc.associateLookup(Lookups.fixed(tc.getActionMap(), tc));
        ContextAwareAction del = new CAA();
        ContextAwareAction context = Actions.context(Integer.class, true, true, del, null, "DisplayName", null, true);
        Action a = context.createContextAwareInstance(tc.getLookup());
        tc.getActionMap().put("key", a);

        WeakReference<Object> ref = new WeakReference<Object>(tc);
        tc = null;
        a = null;
        del = null;
        context = null;
        assertGC("Can the component GC?", ref);

    }

    public void testActionsUpdatedWhenActionMapChanges() throws Exception {
        ContextAwareAction a = Actions.callback("ahoj", null, true, "Ahoj!", "no/icon.png", true);
        final InstanceContent ic = new InstanceContent();
        Lookup lkp = new AbstractLookup(ic);

        ActionMap a1 = new ActionMap();
        ActionMap a2 = new ActionMap();
        a2.put("ahoj", new Enabled());

        ic.add(a1);
        Action clone = a.createContextAwareInstance(lkp);
        class L implements PropertyChangeListener {
            int cnt;
            public void propertyChange(PropertyChangeEvent evt) {
                cnt++;
            }
        }
        L listener = new L();
        clone.addPropertyChangeListener(listener);
        assertFalse("Not enabled", isEnabled(clone));

        ic.set(Collections.singleton(a2), null);

        assertTrue("Enabled now", isEnabled(clone));
        assertEquals("one change", 1, listener.cnt);
    }

    private static boolean isEnabled(final Action a) throws Exception {
        final boolean[] ret = new boolean[1];
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                ret[0] = a.isEnabled();
            }
        });
        return ret[0];
    }

    private static final class Enabled extends AbstractAction {
        public Enabled() {
            setEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
        }
    } // end of Enabled
}
