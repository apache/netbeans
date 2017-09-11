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

package org.netbeans.core;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;

import org.openide.actions.ActionManager;
import org.openide.util.actions.SystemAction;
import org.openide.util.Lookup;

import org.netbeans.core.startup.ManifestSection;


/**
 * Holds list of all actions added by modules.
 * @author Jaroslav Tulach, Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.actions.ActionManager.class)
public class ModuleActions extends ActionManager
/*implements PropertyChangeListener*/ {

    /** array of all actions added by modules */
    private static SystemAction[] array;
    /** of (ModuleItem, List (ManifestSection.ActionSection)) */
    private static Map<Object,List<ManifestSection.ActionSection>> map = new HashMap<Object,List<ManifestSection.ActionSection>> (8);
    /** current module */
    private static Object module = null;
    /** Map of currently running actions */
    private Map<ActionEvent,Action> runningActions = new HashMap<ActionEvent,Action>();

    public static ModuleActions getDefaultInstance() {
        ActionManager mgr = ActionManager.getDefault();
        assert mgr instanceof ModuleActions : "Got wrong ActionManager instance: " + mgr + " from " + Lookup.getDefault();
        return (ModuleActions)mgr;
    }

    /** Array with all activated actions.
    * Can contain null that will be replaced by separators.
    */
    public SystemAction[] getContextActions () {
        SystemAction[] a = array;
        if (a != null) {
            return a;
        }
        array = a = createActions ();
        return a;
    }
    
    /** Invokes action in a RequestPrecessor dedicated to performing
     * actions.
     */
    @SuppressWarnings("deprecation")
    public void invokeAction(final Action a, final ActionEvent e) {
        try {
            org.openide.util.Mutex.EVENT.readAccess (new Runnable() {
                public void run() {
                    showWaitCursor(e);
                }
            });
            addRunningAction(a, e);
            
            a.actionPerformed (e);
        } finally {
            removeRunningAction(e);
            org.openide.util.Mutex.EVENT.readAccess (new Runnable() {
                public void run() {
                    hideWaitCursor(e);
                }
            });
        }
    }
    
    /** Listens on change of modules and if changed,
    * fires change to all listeners.
    */
    private void fireChange () {
        firePropertyChange(PROP_CONTEXT_ACTIONS, null, null);
    }
    
    /** Adds action to <code>runningAction</code> map using event as a key.
     * @param rp <code>RequestProcessor</code> which runs the actio task
     * @param action action to put in map 
     * @param evt action event used as key in the map */
    private void addRunningAction(Action action, ActionEvent evt) {
        synchronized(runningActions) {
            runningActions.put(evt, action);
        }
    }
    
    /** Removes action from <code>runningAction</code> map for key.
     * @param evt action event used as a key in map */
    private void removeRunningAction(ActionEvent evt) {
        synchronized(runningActions) {
            runningActions.remove(evt);
        }
    }

    /** Gets collection of currently running actions. */
    public Collection<Action> getRunningActions() {
        synchronized(runningActions) {
            return new ArrayList<Action>(runningActions.values());
        }
    }
     
    /** Change enabled property of an action
    *
    public void propertyChange (PropertyChangeEvent ev) {
        if (SystemAction.PROP_ENABLED.equals (ev.getPropertyName ())) {
            fireChange ();
        }
    }
    */

    /** Attaches to processing of a module.
     * The actual object passed is arbitrary, so long as
     * it is different for every installed modules (as this
     * controls the grouping of actions with separators).
     * Passing null means stop processing a given module.
     */
    public static synchronized void attachTo (Object m) {
        module = m;
    }

    /** Adds new action to the list.
    */
    public synchronized static void add (ManifestSection.ActionSection a) {
        List<ManifestSection.ActionSection> list = map.get (module);
        if (list == null) {
            list = new ArrayList<ManifestSection.ActionSection> ();
            map.put (module, list);
        }
        list.add (a);
        //a.addPropertyChangeListener (INSTANCE);

        array = null;
        getDefaultInstance().fireChange (); // PENDING this is too often
    }

    /** Removes new action from the list.
    */
    public synchronized static void remove (ManifestSection.ActionSection a) {
        List<ManifestSection.ActionSection> list = map.get (module);
        if (list == null) {
            return;
        }
        list.remove (a);
        //a.removePropertyChangeListener (INSTANCE);

        if (list.isEmpty ()) {
            map.remove (module);
        }

        array = null;
        getDefaultInstance().fireChange (); // PENDING this is too often
    }

    /** Creates the actions.
    */
    private synchronized static SystemAction[] createActions () {
        Iterator<List<ManifestSection.ActionSection>> it = map.values ().iterator ();

        ArrayList<Object> arr = new ArrayList<Object> (map.size () * 5);

        while (it.hasNext ()) {
            List<ManifestSection.ActionSection> l = it.next ();

            Iterator<ManifestSection.ActionSection> actions = l.iterator ();
            while (actions.hasNext()) {
                ManifestSection.ActionSection s = actions.next();
                
                try {
                    arr.add (s.getInstance ());
                } catch (Exception ex) {
                    Logger.getLogger(ModuleActions.class.getName()).log(Level.WARNING, null, ex);
                }
            }
            
            
            if (it.hasNext ()) {
                // add separator between modules
                arr.add (null);
            }

        }

        return (SystemAction[])arr.toArray (new SystemAction[arr.size ()]);
    }

    
    private static final Logger err = Logger.getLogger("org.openide.util.actions.MouseCursorUtils"); // NOI18N
    
    /**
     * Running show/hide count for glass panes in use.
     * Maps arbitrary keys to glass panes.
     * Several keys may map to the same glass pane - the wait cursor is shown
     * so long as there are any.
     */
    private static final Map<Object,java.awt.Component> glassPaneUses = new HashMap<Object,java.awt.Component>();
    
    /**
     * Try to find the active window's glass pane.
     * @return a glass pane, or null
     */
    private static java.awt.Component activeGlassPane() {
        java.awt.Window w = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (w instanceof javax.swing.RootPaneContainer) {
            return ((javax.swing.RootPaneContainer)w).getGlassPane();
        } else {
            return null;
        }
    }
    
    /**
     * Sets wait cursor visible on the window associated with an event, if any.
     * @param key something to pass to {@link #hideWaitCursor} to turn it off
     */
    public static void showWaitCursor(Object key) {
        assert java.awt.EventQueue.isDispatchThread();
        assert !glassPaneUses.containsKey(key);
        java.awt.Component c = activeGlassPane();
        if (c == null) {
            err.warning("showWaitCursor could not find a suitable glass pane; key=" + key);
            return;
        }
        if (glassPaneUses.values().contains(c)) {
            err.fine("wait cursor already displayed on " + c);
        } else {
            err.fine("wait cursor will be displayed on " + c);
            c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            c.setVisible(true);
        }
        glassPaneUses.put(key, c);
    }
    
    /**
     * Resets cursor to default.
     * @param key the same key passed to {@link #showWaitCursor}
     */
    public static void hideWaitCursor(Object key) {
        assert java.awt.EventQueue.isDispatchThread();
        java.awt.Component c = glassPaneUses.get(key);
        if (c == null) {
            return;
        }
        glassPaneUses.remove(key);
        if (glassPaneUses.values().contains(c)) {
            err.fine("wait cursor still displayed on " + c);
        } else {
            err.fine("wait cursor will be hidden on " + c);
            c.setVisible(false);
            c.setCursor(null);
        }
    }
    
    
}

