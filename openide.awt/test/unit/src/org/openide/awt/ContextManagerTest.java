/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ContextManagerTest extends NbTestCase {
    private AbstractLookup lkp;
    private ContextManager cm;
    
    public ContextManagerTest(String name) {
        super(name);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testSurviveFocusChange() throws Exception {
        InstanceContent ic = new InstanceContent();
        Lookup lkp = new AbstractLookup(ic);

        Action clone = ((ContextAwareAction) Actions.forID("cat", "survive")).createContextAwareInstance(lkp);
        L listener = new L();
        clone.addPropertyChangeListener(listener);

        assertFalse("Disabled", clone.isEnabled());
        Object val = Integer.valueOf(1);
        ic.add(val);
        assertTrue("Enabled now", clone.isEnabled());
        assertEquals("One change", 1, listener.cnt);
        ic.remove(val);
        assertTrue("Still Enabled", clone.isEnabled());

        Survival.value = 0;
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("Added one", 1, Survival.value);
    }

    public void testListenerGCed () throws Exception {
        InstanceContent ic = new InstanceContent();
        lkp = new AbstractLookup(ic);
        Lookup.Result<Integer> lookupResult = lkp.lookupResult(Integer.class);

        Action action = ((ContextAwareAction) Actions.forID("cat", "survive")).createContextAwareInstance(lkp);
        Action fallbackAction = ((GeneralAction.DelegateAction) action).fallback;
        WeakReference<Action> fallbackActionRef = new WeakReference<Action>(fallbackAction);
        WeakReference<Action> clone = new WeakReference<Action>(action);
        cm = ContextManager.findManager(lkp, true);
        WeakReference lsetRef = new WeakReference<Object>(cm.findLSet(Integer.class));

        action = null;

        assertGC("Action should be GCed", clone);

        fallbackAction = null;

        assertGC("Fallback action should be GCed", fallbackActionRef);
        assertGC("Action LSet Should be GCed", lsetRef);
        lookupResult.allInstances();
    }

    public void testAllResultListenersRemoved () throws Exception {
        InstanceContent ic = new InstanceContent();
        lkp = new AbstractLookup(ic);
        Lookup.Result<Integer> lookupResult = lkp.lookupResult(Integer.class);

        Action action = ((ContextAwareAction) Actions.forID("cat", "survive")).createContextAwareInstance(lkp);
        Action fallbackAction = ((GeneralAction.DelegateAction) action).fallback;
        WeakReference<Action> fallbackActionRef = new WeakReference<Action>(fallbackAction);
        WeakReference<Action> clone = new WeakReference<Action>(action);
        cm = ContextManager.findManager(lkp, true);
        WeakReference<ContextManager.LSet> lsetRef = new WeakReference<ContextManager.LSet>(cm.findLSet(Integer.class));
        WeakReference<Lookup.Result> lookupResultRef = new WeakReference<Lookup.Result>(lsetRef.get().result);

        action = null;

        assertGC("Action should be GCed", clone);

        fallbackAction = null;

        assertGC("Fallback action should be GCed", fallbackActionRef);
        assertGC("Action LSet Should be GCed", lsetRef);
        if (lookupResultRef.get() == lookupResult) {
            // LSet holds ref to the actual real lookup result, nothing to test
        } else {
            // LSet holds ref to a wrapper class NeverEmptyResult, which should have been GCed
            assertGC("NeverEmptyResult should be GCed", lookupResultRef);
        }
    }

    @RandomlyFails
    public void testListenerGCedAfterActionGCed () throws Exception {
        InstanceContent ic = new InstanceContent();
        lkp = new AbstractLookup(ic);
        Lookup.Result<Integer> lookupResult = lkp.lookupResult(Integer.class);

        Action action = ((ContextAwareAction) Actions.forID("cat", "survive")).createContextAwareInstance(lkp);
        Action fallbackAction = ((GeneralAction.DelegateAction) action).fallback;
        WeakReference<Action> fallbackActionRef = new WeakReference<Action>(fallbackAction);
        WeakReference<Action> clone = new WeakReference<Action>(action);
        cm = ContextManager.findManager(lkp, true);
        WeakReference lsetRef = new WeakReference<Object>(cm.findLSet(Integer.class));

        // both delegate and delegating actions are GCed before WeakListenerSupport is triggered in ActiveRefQueue:
        // fallbackAction.removePropertyChangeListener(delegating.weakL);
        fallbackAction = null;
        action = null;
        assertGC("Action should be GCed", clone);

        assertGC("Fallback action should be GCed", fallbackActionRef);
        assertGC("Action LSet Should be GCed", lsetRef);
        lookupResult.allInstances();
    }
    
    private static class L implements PropertyChangeListener {
        int cnt;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            cnt++;
        }
    }
    
    @ActionID(category="cat", id="survive")
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
