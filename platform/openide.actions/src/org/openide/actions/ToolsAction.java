/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.actions;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;


/** A "meta-action" that displays (in a submenu) a list of enabled actions provided by modules.
* Such registered actions are called "service actions":
* they are provided externally but seem to provide additional services on existing components.
* Often they will be {@link NodeAction}s or {@link CookieAction}s so that they will
* be enabled based on the node selection, i.e. the node containing this popup.
* It is desirable for most nodes to include this action somewhere in their popup menu.
*
* <p><em>Note:</em> you do not need to touch this class to add a service action!
* Just register your action into <code>UI/ToolActions</code> layer folder
* (read <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">more about layers</a>)
 * since version 6.15.
*
* @author Jaroslav Tulach
*/
public class ToolsAction extends SystemAction implements ContextAwareAction, Presenter.Menu, Presenter.Popup {
    static final long serialVersionUID = 4906417339959070129L;

    private static ScheduledFuture<G> taskGl;
    // Global ActionManager listener monitoring all available actions
    // and their state
    static final G gl() {
        return gl(Long.MAX_VALUE);
    }
    static final G gl(long timeOut) {
        initGl();
        for (;;) {
            try {
                return taskGl.get(timeOut, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ex) {
                return null;
            } catch (InterruptedException ex) {
                continue;
            } catch (Exception ex) {
                taskGl = null;
                throw new IllegalStateException(ex);
            }
        }
    }
    
    private static synchronized void initGl() {
        if (taskGl == null) {
            taskGl = RequestProcessor.getDefault().schedule(new G(), 0, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        
    }

    /* @return name
    */
    @Override
    public String getName() {
        return getActionName();
    }

    /* @return help for this action
    */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ToolsAction.class);
    }

    /* @return menu presenter for the action
    */
    @Override
    public JMenuItem getMenuPresenter() {
        return new Inline(this);
    }

    /* @return menu presenter for the action
    */
    @Override
    public JMenuItem getPopupPresenter() {
        return new Popup(this);
    }

    /* Does nothing.
    */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DelegateAction(this, actionContext);
    }

    /* @return name
    */
    private static String getActionName() {
        return NbBundle.getMessage(ToolsAction.class, "CTL_Tools");
    }

    static List<Action> getToolActions() {
        ActionManager am = ActionManager.getDefault();
        List<Action> arr = new ArrayList<Action>();
        arr.addAll(Arrays.<Action>asList(am.getContextActions()));

        String pref = arr.isEmpty() ? null : "";
        for (Lookup.Item<Action> item : gl().result.allItems()) {
            final Action action = item.getInstance();
            if (action == null) {
                continue;
            }
            String where = item.getId().replaceFirst("[^/]*$", ""); // NOI18N
            if (pref != null && !pref.equals(where)) {
                arr.add(null);
            }
            pref = where;
            arr.add(action);
        }
        return arr;
    }


    /** Implementation method that regenerates the items in the menu or
    * in the array.
    *
    * @param forMenu true if will be presented in menu or false if presented in popup
    * @param list (can be null)
    */
    private static List<JMenuItem> generate(Action toolsAction, boolean forMenu) {
        List<Action> actions = getToolActions();
        List<JMenuItem> list = new ArrayList<JMenuItem>(actions.size());

        boolean separator = false;
        boolean firstItemAdded = false; // flag to prevent adding separator before actual menu items

        // Get action context.
        Lookup lookup;

        if (toolsAction instanceof Lookup.Provider) {
            lookup = ((Lookup.Provider) toolsAction).getLookup();
        } else {
            lookup = null;
        }

        for (Action a : actions) {

            // Retrieve context sensitive action instance if possible.
            if (lookup != null && a instanceof ContextAwareAction) {
                a = ((ContextAwareAction) a).createContextAwareInstance(lookup);
            }

            if (a == null) {
                if (firstItemAdded) {
                    separator = true;
                }
            } else {
                boolean isPopup = (a instanceof Presenter.Popup);
                boolean isMenu = (a instanceof Presenter.Menu);

                if (!((forMenu && isMenu) || (!forMenu && isPopup)) && (isMenu || isPopup)) {
                    continue; // do not call isEnabled on action that is only popup presenter when building menu (i18nPopupAction)
                }

                if (a.isEnabled()) {
                    JMenuItem mi;

                    if (forMenu && isMenu) {
                        mi = ((Presenter.Menu) a).getMenuPresenter();
                    } else if (!forMenu && isPopup) {
                        mi = ((Presenter.Popup) a).getPopupPresenter();
                    } else if (!isMenu && !isPopup) {
                        // Generic Swing action.
                        mi = new JMenuItem();
                        Actions.connect(mi, a, !forMenu);
                    } else {
                        // Should not be here.
                        continue;
                    }

                    if (separator) {
                        list.add(null);
                        separator = false;
                    }

                    list.add(mi);
                    firstItemAdded = true;
                }
            }
        }

        return list;
    }

    //------------------------------------------

    /** @deprecated Useless, see {@link ActionManager}. */
    @Deprecated
    public static void setModel(Model m) {
        throw new SecurityException();
    }

    /** @deprecated Useless, see {@link ActionManager}. */
    @Deprecated
    public static interface Model {
        public SystemAction[] getActions();

        public void addChangeListener(javax.swing.event.ChangeListener l);

        public void removeChangeListener(javax.swing.event.ChangeListener l);
    }

    /** Inline menu that watches model changes only when really needed.
     */
    private static final class Inline extends JMenuItem implements DynamicMenuContent {
        static final long serialVersionUID = 2269006599727576059L;

        /** timestamp of the beginning of the last regeneration */
        private int timestamp = 0;

        /** Associated tools action. */
        private Action toolsAction;

        Inline(Action toolsAction) {
            this.toolsAction = toolsAction;
        }


        
        @NbBundle.Messages({
            "LAB_ToolsActionInitializing=Initializing..."
        })
        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            G g = gl(50);
            if (g == null) {
                JMenuItem init = new JMenuItem();
                init.setText(Bundle.LAB_ToolsActionInitializing());
                init.setEnabled(false);
                return new JMenuItem[] { init };
            }
            if (timestamp == g.getTimestamp()) {
                return items;
            }
            // generate directly list of menu items
            List<JMenuItem> l = generate(toolsAction, true);
            timestamp = gl().getTimestamp();
            return l.toArray(new JMenuItem[0]);
        }
        
        
        @Override
        public JComponent[] getMenuPresenters() {
            return synchMenuPresenters(new JComponent[0]);
        }        
    }

    //--------------------------------------------------

    /** Inline menu that is either empty or contains one submenu.*/
    private static final class Popup extends JMenuItem implements DynamicMenuContent {
        static final long serialVersionUID = 2269006599727576059L;

        /** sub menu */
        private JMenu menu = new MyMenu();

        /** Associated tools action. */
        private Action toolsAction;

        public Popup(Action toolsAction) {
            super();
            this.toolsAction = toolsAction;
            HelpCtx.setHelpIDString(menu, ToolsAction.class.getName());

        }

        
        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return gl().isPopupEnabled(toolsAction) ? new JMenuItem[] { menu } : new JMenuItem[0];
        }
        
        
        @Override
        public JComponent[] getMenuPresenters() {
            return synchMenuPresenters(new JComponent[0]);
        }                


        /** A special menu that will properly update its submenu before posting */
        private class MyMenu extends org.openide.awt.JMenuPlus implements PopupMenuListener {
            /* A popup menu we've attached our listener to.
             * If null, the content is not up-to-date */
            private JPopupMenu lastPopup = null;

            MyMenu() {
                super(getActionName());
            }

            @Override
            public JPopupMenu getPopupMenu() {
                JPopupMenu popup = super.getPopupMenu();
                fillSubmenu(popup);

                return popup;
            }

            private void fillSubmenu(JPopupMenu pop) {
                if (lastPopup == null) {
                    pop.addPopupMenuListener(this);
                    lastPopup = pop;

                    removeAll();

                    Iterator<JMenuItem> it = generate(toolsAction, false).iterator();

                    while (it.hasNext()) {
                        java.awt.Component item = (java.awt.Component) it.next();

                        if (item == null) {
                            addSeparator();
                        } else {
                            add(item);
                        }
                    }

                    // also work with empty element
                    if (getMenuComponentCount() == 0) {
                        JMenuItem empty = new JMenuItem(NbBundle.getMessage(ToolsAction.class, "CTL_EmptySubMenu"));
                        empty.setEnabled(false);
                        add(empty);
                    }
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (lastPopup != null) {
                    lastPopup.removePopupMenuListener(this);
                    lastPopup = null; // clear the status and stop listening
                }
            }
        }
    }

    //------------------------------------------------
    //----------------------------------------------------------
    private static class G implements PropertyChangeListener, LookupListener, Callable<G> {
        public static final String PROP_STATE = "actionsState"; // NOI18N
        private int timestamp = 1;
        private Action[] actions = null;
        private PropertyChangeSupport supp = new PropertyChangeSupport(this);
        Lookup.Result<Action> result;

        public G() {
            // init is done in run
        }
        
        @Override
        public G call() {
            ActionManager am = ActionManager.getDefault();
            am.addPropertyChangeListener(this);
            result = Lookups.forPath("UI/ToolActions").lookupResult(Action.class); // NOI18N
            result.addLookupListener(this);
            actionsListChanged();
            return this;
        }

        public final void addPropertyChangeListener(PropertyChangeListener listener) {
            supp.addPropertyChangeListener(listener);
        }

        public final void removePropertyChangeListener(PropertyChangeListener listener) {
            supp.removePropertyChangeListener(listener);
        }

        protected final void firePropertyChange(String name, Object o, Object n) {
            supp.firePropertyChange(name, o, n);
        }

        private void actionsListChanged() {
            timestamp++;

            // deregister all actions listeners
            Action[] copy = actions;

            if (copy != null) {
                for (int i = 0; i < copy.length; i++) {
                    Action act = copy[i];

                    if (act != null) {
                        act.removePropertyChangeListener(this);
                    }
                }
            }

            ActionManager am = ActionManager.getDefault();
            List<Action> all = new ArrayList<Action>();
            all.addAll(Arrays.asList(am.getContextActions()));
            all.addAll(result.allInstances());
            copy = all.toArray(new Action[0]);

            for (int i = 0; i < copy.length; i++) {
                Action act = copy[i];

                if (act != null) {
                    act.addPropertyChangeListener(this);
                }
            }

            actions = copy;

            firePropertyChange(PROP_STATE, null, null); // tell the world
        }

        private void actionStateChanged() {
            timestamp++;
            firePropertyChange(PROP_STATE, null, null); // tell the world
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();

            if ((prop == null) || prop.equals(ActionManager.PROP_CONTEXT_ACTIONS)) {
                actionsListChanged();
            } else if (prop.equals("enabled")) {
                actionStateChanged();
            }
        }

        /** Tells if there is any action that is willing to provide
         * Presenter.Popup
         */
        private boolean isPopupEnabled(Action toolsAction) {
            boolean en = false;
            Action[] copy = actions;

            // Get action conext.
            Lookup lookup;

            if (toolsAction instanceof Lookup.Provider) {
                lookup = ((Lookup.Provider) toolsAction).getLookup();
            } else {
                lookup = null;
            }

            for (int i = 0; i < copy.length; i++) {
                if (copy[i] == null) {
                    continue;
                }
                // Get context aware action instance if needed.
                Action act;

                // Retrieve context aware action instance if possible.
                if ((lookup != null) && copy[i] instanceof ContextAwareAction) {
                    act = ((ContextAwareAction) copy[i]).createContextAwareInstance(lookup);
                    if (act == null) {
                        throw new IllegalStateException("createContextAwareInstance for " + copy[i] + " returned null!");
                    }
                } else {
                    act = copy[i];
                }

                if (act.isEnabled()) {
                    en = true;

                    break;
                }
            }

            return en;
        }

        private int getTimestamp() {
            return timestamp;
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            actionsListChanged();
        }
    }

    /** Delegate tools action. Which act accordingly to current context
     * (represented by lookup). */
    private static final class DelegateAction extends Object implements Action, Presenter.Menu, Presenter.Popup,
        Lookup.Provider {
        private ToolsAction delegate;
        private Lookup lookup;

        /** support for listeners */
        private PropertyChangeSupport support = new PropertyChangeSupport(this);

        public DelegateAction(ToolsAction delegate, Lookup actionContext) {
            this.delegate = delegate;
            this.lookup = actionContext;
        }

        /** Overrides superclass method, adds delegate description. */
        @Override
        public String toString() {
            return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
        }

        /** Implements <code>Lookup.Provider</code>. */
        @Override
        public Lookup getLookup() {
            return lookup;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }

        @Override
        public void putValue(String key, Object o) {
        }

        @Override
        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        @Override
        public boolean isEnabled() {
            // Irrelevant see G#isPopupEnabled(..).
            return delegate.isEnabled();
        }

        @Override
        public void setEnabled(boolean b) {
            // Irrelevant see G#isPopupEnabled(..).
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        /** Implements <code>Presenter.Menu</code>. */
        @Override
        public javax.swing.JMenuItem getMenuPresenter() {
            return new Inline(this);
        }

        /** Implements <code>Presenter.Popup</code>. */
        @Override
        public javax.swing.JMenuItem getPopupPresenter() {
            return new ToolsAction.Popup(this);
        }
    }
     // End of DelegateAction.
}
