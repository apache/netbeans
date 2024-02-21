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
import java.awt.EventQueue;
import java.awt.MenuShortcut;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultEditorKit;
import org.openide.awt.Actions;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.UserCancelException;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.TopComponent;

/** Paste from clipboard. This is a callback system action,
* with enhanced behaviour. Others can plug in by adding
* <PRE>
* topcomponent.getActionMap ().put (javax.swing.text.DefaultEditorKit.pasteAction, theActualAction);
* </PRE>
* or by using the now deprecated <code>setPasteTypes</code> and <code>setActionPerformer</code>
* methods.
* <P>
* There is a special support for more than one type of paste to be enabled at once.
* If the <code>theActualAction</code> returns array of actions from
* <code>getValue ("delegates")</code> than those actions are offered as
* subelements by the paste action presenter.
*/
public final class PasteAction extends CallbackSystemAction {
    /** Imlementation of ActSubMenuInt */
    private static ActSubMenuModel globalModel;

    /** All currently possible paste types. */
    private static PasteType[] types;

    /** Lazy initializtion of the global model */
    private static synchronized ActSubMenuModel model() {
        if (globalModel == null) {
            globalModel = new ActSubMenuModel(null);
        }

        return globalModel;
    }

    protected @Override void initialize() {
        super.initialize();

        setEnabled(false);
    }

    public String getName() {
        return NbBundle.getMessage(PasteAction.class, "Paste");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PasteAction.class);
    }

    protected @Override String iconResource() {
        return "org/openide/resources/actions/paste.gif"; // NOI18N
    }

    public @Override JMenuItem getMenuPresenter() {
        return new Actions.SubMenu(this, model(), false);
    }

    public @Override JMenuItem getPopupPresenter() {
        return new Actions.SubMenu(this, model(), true);
    }

    public @Override Action createContextAwareInstance(Lookup actionContext) {
        return new DelegateAction(this, actionContext);
    }

    public @Override Object getActionMapKey() {
        return DefaultEditorKit.pasteAction;
    }

    public @Override void actionPerformed(ActionEvent ev) {
        PasteType t;

        if (ev.getSource() instanceof PasteType) {
            t = (PasteType) ev.getSource();
        } else {
            PasteType[] arr = getPasteTypes();

            if ((arr != null) && (arr.length > 0)) {
                t = arr[0];
            } else {
                t = null;
            }
        }

        if (t == null) {
            // Try to find paste action 'performer' from activated TopComponent.
            Action ac = findActionFromActivatedTopComponentMap();

            if (ac != null) {
                // XXX Hack to get paste types from action 'performer',
                // which in fact doesn't perform the paste.
                // Look at ExplorerActions.OwnPaste#getValue method.
                Object obj = ac.getValue("delegates"); // NOI18N
                
                if (obj instanceof PasteType []) {
                    PasteType [] arr = (PasteType []) obj;
                    if (arr.length > 0) {
                        t = arr [0];
                    }
                } else if (obj instanceof Action []) {
                    Action [] arr = (Action []) obj;
                    if (arr.length > 0) {
                        arr [0].actionPerformed (ev);
                        return;
                    }
                } else {
                    ac.actionPerformed(ev);

                    return;
                }
            }
        }

        if (t != null) {
            // posts the action in RP thread
            new ActionPT(t, ev.getActionCommand());
        } else {
            Toolkit.getDefaultToolkit().beep();
            Logger.getLogger(PasteAction.class.getName()).log(Level.INFO, "No paste types available when performing paste action. ActionEvent: {0}", ev); // NOI18N
        }
    }

    protected @Override boolean asynchronous() {
        return false;
    }

    /** Set possible paste types.
    * Automatically enables or disables the paste action according to whether there are any.
    * @deprecated Use <code>TopComponent.getActionMap ().put (javax.swing.text.DefaultEditorKit.pasteAction, yourPasteAction);</code>
    *  If you want register more paste types then use an action which delegates to
    *  an array of <code>PasteAction</code> or also can delegate to an array of
     * <code>org.openide.util.datatransfer.PasteType</code>.
    * @param types the new types to allow, or <code>null</code>
    */
    @Deprecated
    public void setPasteTypes(PasteType[] types) {
        PasteAction.types = types;

        if ((types == null) || (types.length == 0)) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }

        model().checkStateChanged(true);
    }

    /** Get all paste types.
     * @return all possible paste types, or <code>null</code> */
    public PasteType[] getPasteTypes() {
        return types;
    }

    /** Finds paste action from currently activated TopComponent's action map. */
    private static Action findActionFromActivatedTopComponentMap() {
        TopComponent tc = TopComponent.getRegistry().getActivated();

        if (tc != null) {
            ActionMap map = tc.getActionMap();

            return findActionFromMap(map);
        }

        return null;
    }

    /** Finds paste action from provided map. */
    private static Action findActionFromMap(ActionMap map) {
        if (map != null) {
            return map.get(DefaultEditorKit.pasteAction);
        }

        return null;
    }

    /** If our clipboard is not found return the default system clipboard. */
    private static Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(Clipboard.class);

        if (c == null) {
            c = Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        return c;
    }

    /** Utility method for finding the currently selected explorer manager.
     * it uses reflection because it should work without
     * the rest of the IDE classes.
     *
     * @return current explorer manager or null
     */
    static ExplorerManager findExplorerManager() {
        Throwable t = null;

        try {
            Class c = Class.forName("org.openide.windows.TopComponent"); // NOI18N

            // use reflection now
            Method m = c.getMethod("getRegistry"); // NOI18N
            Object o = m.invoke(null);

            c = Class.forName("org.openide.windows.TopComponent$Registry"); // NOI18N

            // use reflection now
            m = c.getMethod("getActivated"); // NOI18N
            o = m.invoke(o);

            if (o instanceof ExplorerManager.Provider) {
                return ((ExplorerManager.Provider) o).getExplorerManager();
            }
        }
        // exceptions from forName:
         catch (ClassNotFoundException x) {
        } catch (ExceptionInInitializerError x) {
        } catch (LinkageError x) {
        }
        // exceptions from getMethod:
         catch (SecurityException x) {
            t = x;
        } catch (NoSuchMethodException x) {
            t = x;
        }
        // exceptions from invoke
         catch (IllegalAccessException x) {
            t = x;
        } catch (IllegalArgumentException x) {
            t = x;
        } catch (InvocationTargetException x) {
            t = x;
        }

        if (t != null) {
            Logger.getLogger(PasteAction.class.getName()).log(Level.WARNING, null, t);
        }

        return null;
    }

    /** General implementation of Actions.SubMenuModel that works
     * with provided lookup or without it. With lookup it attaches
     * to changes in the lookup and updates its state according to
     * it. Without it listens on TopComponent.getActivated() and
     * works with it.
     */
    private static class ActSubMenuModel implements Actions.SubMenuModel, LookupListener, PropertyChangeListener {

        private final ChangeSupport cs = new ChangeSupport(this);

        /** lookup we are attached to or null we we should work globally */
        private Lookup.Result<ActionMap> result;

        /** previous enabled state */
        private boolean enabled;

        /** weak listener for action */
        private PropertyChangeListener actionWeakL;

        /** weak listener for paste type */
        private PropertyChangeListener pasteTypeWeakL;

        /** weak lookup listener */
        private LookupListener weakLookup;

        /** @param lookup can be null */
        public ActSubMenuModel(Lookup lookup) {
            attachListenerToChangesInMap(lookup);
        }

        /** Finds appropriate map to work with.
         * @return map from lookup or from activated TopComponent, null no available
         */
        private ActionMap map() {
            if (result == null) {
                TopComponent tc = TopComponent.getRegistry().getActivated();

                if (tc != null) {
                    return tc.getActionMap();
                }
            } else {
                for (ActionMap am : result.allInstances()) {
                    return am;
                }
            }

            return null;
        }

        /** Adds itself as a listener for changes in current ActionMap.
         * If the lookup is null then it means to listen on TopComponent
         * otherwise to listen on the lookup itself.
         *
         * @param lookup lookup to listen on or null
         */
        private void attachListenerToChangesInMap(Lookup lookup) {
            if (lookup == null) {
                TopComponent.getRegistry().addPropertyChangeListener(WeakListeners.propertyChange(this, TopComponent.getRegistry()));
            } else {
                result = lookup.lookupResult(ActionMap.class);
                weakLookup = WeakListeners.create(LookupListener.class, this, result);
                result.addLookupListener(weakLookup);
            }

            checkStateChanged(false);
        }

        /** Finds the currently active items this method should delegate to.
         * For historical reasons one can use PasteType by PasteAction.setPasteTypes
         * in the new implementation it is expected that such paste types
         * will be replaced by Actions (obtained from getValue("delegates")).
         *
         *
         * @param actionToWorkWith array of size 1 or null. Will be filled
         *   with action that we actually delegate to (either the global or local
         *   found in action map)
         * @return array of either PasteTypes or Actions
         */
        private Object[] getPasteTypesOrActions(Action[] actionToWorkWith) {
            Action x = findActionFromMap(map());

            if (x == null) {
                // No context action use the global one.
                PasteAction a = findObject(PasteAction.class);

                if (actionToWorkWith != null) {
                    actionToWorkWith[0] = a;
                }

                Object[] arr = a == null ? null : a.getPasteTypes();

                if (arr != null) {
                    return arr;
                } else {
                    return new Object[0];
                }
            }

            if (actionToWorkWith != null) {
                actionToWorkWith[0] = x;
            }

            Object obj = x.getValue("delegates"); // NOI18N

            if (obj instanceof Object[]) {
                return (Object[]) obj;
            } else {
                return new Object[] { x };
            }
        }

        private boolean isEnabledImpl(Object[] pasteTypesOrActions) {
            if (pasteTypesOrActions == null) {
                pasteTypesOrActions = getPasteTypesOrActions(null);
            }

            if ((pasteTypesOrActions.length == 1) && pasteTypesOrActions[0] instanceof Action) {
                return ((Action) pasteTypesOrActions[0]).isEnabled();
            } else {
                return pasteTypesOrActions.length > 0;
            }
        }

        public boolean isEnabled() {
            return isEnabledImpl(null);
        }

        public int getCount() {
            return getPasteTypesOrActions(null).length;
        }

        public String getLabel(int index) {
            Object[] arr = getPasteTypesOrActions(null);

            if (arr.length <= index) {
                return null;
            }

            if (arr[index] instanceof PasteType) {
                return ((PasteType) arr[index]).getName();
            } else {
                // is Action
                return (String) ((Action) arr[index]).getValue(Action.NAME);
            }
        }

        public HelpCtx getHelpCtx(int index) {
            Object[] arr = getPasteTypesOrActions(null);

            if (arr.length <= index) {
                return null;
            }

            if (arr[index] instanceof PasteType) {
                return ((PasteType) arr[index]).getHelpCtx();
            } else {
                // is action
                Object helpID = ((Action) arr[index]).getValue("helpID"); // NOI18N

                if (helpID instanceof String) {
                    return new HelpCtx((String) helpID);
                } else {
                    return null;
                }
            }
        }

        public MenuShortcut getMenuShortcut(int index) {
            return null;
        }

        public void performActionAt(int index) {
            performActionAt(index, null);
        }

        public void performActionAt(int index, ActionEvent ev) {
            Action[] action = new Action[1];

            Object[] arr = getPasteTypesOrActions(action);

            if (arr.length <= index) {
                return;
            }

            if (arr[index] instanceof PasteType) {
                PasteType t = (PasteType) arr[index];

                // posts the action is RP thread
                new ActionPT(t, (ev == null) ? null : ev.getActionCommand());

                return;
            } else {
                // is action
                Action a = (Action) arr[index];
                a.actionPerformed(new ActionEvent(a, ActionEvent.ACTION_PERFORMED, Action.NAME));

                return;
            }
        }

        /** Registers .ChangeListener to receive events.
         *@param listener The listener to register.
         */
        public synchronized void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        /** Removes .ChangeListener from the list of listeners.
         *@param listener The listener to remove.
         */
        public synchronized void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        /** Notifies all registered listeners about the event.
         *
         *@param param1 Parameter #1 of the <CODE>.ChangeEvent<CODE> constructor.
         */
        protected void checkStateChanged(boolean fire) {
            Action[] listen = new Action[1];
            Object[] arr = getPasteTypesOrActions(listen);

            Action a = null;

            if ((arr.length == 1) && arr[0] instanceof Action) {
                a = (Action) arr[0];
                a.removePropertyChangeListener(pasteTypeWeakL);
                pasteTypeWeakL = WeakListeners.propertyChange(this, a);
                a.addPropertyChangeListener(pasteTypeWeakL);
            }

            // plus always make sure we are listening on the actions
            if (listen[0] != a) {
                listen[0].removePropertyChangeListener(actionWeakL);
                actionWeakL = WeakListeners.propertyChange(this, listen[0]);
                listen[0].addPropertyChangeListener(actionWeakL);
            }

            boolean en = isEnabledImpl(arr);

            if (en == enabled) {
                return;
            }

            enabled = en;

            // and fire if requested....
            if (!fire) {
                return;
            }

            cs.fireChange();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            checkStateChanged(true);
        }

        public void resultChanged(LookupEvent ev) {
            checkStateChanged(true);
        }
    }

    /** Class that listens on a given node and when invoked listen on changes
     * and after that tries to select the desired node.
     */
    static final class NodeSelector implements NodeListener, Runnable {
        /** All added children */
        private List<Node> added;

        /** node we are listening to */
        private Node node;

        /** manager to work with */
        private ExplorerManager em;

        /** children */
        private Node[] children;

        /** @param em explorer manager to work with
         * @param n nodes to attach to or null if em's nodes should be used
         */
        public NodeSelector(ExplorerManager em, Node[] n) {
            this.em = em;

            if ((n != null) && (n.length > 0)) {
                this.node = n[0];
            } else {
                Node[] arr = em.getSelectedNodes();

                if (arr.length != 0) {
                    this.node = arr[0];
                } else {
                    // do not initialize
                    return;
                }
            }

            // XXX [FindBugs] is this field good for something (never read)? needed to hold hard ref, perhaps?
            this.children = node.getChildren().getNodes(true);

            this.added = new ArrayList<Node>();
            this.node.addNodeListener(this);
        }

        /** Selects the added nodes */
        public void select() {
            if (added != null) {
                // if initialized => wait till finished update
                node.getChildren().getNodes(true);

                // and select the right nodes
                Children.MUTEX.readAccess(this);
            }
        }

        public void run() {
            node.removeNodeListener(this);

            if (added.isEmpty()) {
                return;
            }

// bugfix #22698, don't select the added nodes
// when the nodes not under managed explorer's root node
bigloop:
            for (Node n : added) {
                while (n != null) {
                    if (n.equals(em.getRootContext())) {
                        continue bigloop;
                    }

                    n = n.getParentNode();
                }

                return;
            }

            try {
                em.setSelectedNodes(added.toArray(new Node[0]));
            } catch (PropertyVetoException ex) {
                Logger.getLogger(PasteAction.class.getName()).log(Level.WARNING, null, ex);
            } catch (IllegalStateException ex) {
                Logger.getLogger(PasteAction.class.getName()).log(Level.WARNING, null, ex);
            }
        }

        /** Fired when a set of new children is added.
         * @param ev event describing the action
         */
        public void childrenAdded(NodeMemberEvent ev) {
            added.addAll(Arrays.asList(ev.getDelta()));
        }

        /** Fired when a set of children is removed.
         * @param ev event describing the action
         */
        public void childrenRemoved(NodeMemberEvent ev) {
        }

        /** Fired when the order of children is changed.
         * @param ev event describing the change
         */
        public void childrenReordered(NodeReorderEvent ev) {
        }

        /** Fired when the node is deleted.
         * @param ev event describing the node
         */
        public void nodeDestroyed(NodeEvent ev) {
        }

        /** This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *           and the property that has changed.
         */
        public void propertyChange(PropertyChangeEvent evt) {
        }
    }
     // end of NodeSelector

    /** A delegate action that is usually associated with a specific lookup and
     * extract the nodes it operates on from it. Otherwise it delegates to the
     * regular NodeAction.
     */
    private static final class DelegateAction extends AbstractAction implements Presenter.Menu,
            Presenter.Popup, Presenter.Toolbar, ChangeListener {
        /** action to delegate too */
        private PasteAction delegate;

        /** model to work with */
        private ActSubMenuModel model;

        public DelegateAction(PasteAction a, Lookup actionContext) {
            this.delegate = a;
            this.model = new ActSubMenuModel(actionContext);
            this.model.addChangeListener(this);
        }

        /** Overrides superclass method, adds delegate description. */
        public @Override String toString() {
            return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
        }

        public @Override void putValue(String key, Object value) {
        }

        /** Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            if (model != null) {
                model.performActionAt(0, e);
            }
        }

        public @Override boolean isEnabled() {
            return (model != null) && model.isEnabled();
        }

        public @Override Object getValue(String key) {
            return delegate.getValue(key);
        }

        public @Override void setEnabled(boolean b) {
        }

        public JMenuItem getMenuPresenter() {
            return new Actions.SubMenu(this, model, false);
        }

        public JMenuItem getPopupPresenter() {
            return new Actions.SubMenu(this, model, true);
        }

        public Component getToolbarPresenter() {
            return new Actions.ToolbarButton(this);
        }

        public void stateChanged(ChangeEvent evt) {
            firePropertyChange("enabled", null, null);
        }
    }
     // end of DelegateAction    

    /** Action that wraps paste type.
     */
    private static final class ActionPT extends AbstractAction implements Runnable {
        private static final RequestProcessor RP = new RequestProcessor("Pasting"); // NOI18N
        private PasteType t;
        private NodeSelector sel;
        private boolean secondInvocation;

        public ActionPT(PasteType t, String command) {
            this.t = t;

            ExplorerManager em = findExplorerManager();

            if (em != null) {
                this.sel = new NodeSelector(em, null);
            }

            if ("waitFinished".equals(command)) { // NOI18N
                run();
            } else {
                RP.post(this);
            }
        }

        public void actionPerformed(ActionEvent ev) {
            try {
                Transferable trans = t.paste();
                Clipboard clipboard = getClipboard();

                if (trans != null) {
                    ClipboardOwner owner = (trans instanceof ClipboardOwner) ? (ClipboardOwner) trans
                                                                             : new StringSelection(""); // NOI18N
                    clipboard.setContents(trans, owner);
                }
            } catch (UserCancelException exc) {
                // ignore - user just pressed cancel in some dialog....
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            } finally {
                EventQueue.invokeLater(this);
            }
        }

        public void run() {
            if (secondInvocation) {
                if (sel != null) {
                    sel.select();
                }
            } else {
                secondInvocation = true;
                ActionManager.getDefault().invokeAction(
                    this, new ActionEvent(t, ActionEvent.ACTION_PERFORMED, Action.NAME)
                );
            }
        }

        public @Override boolean isEnabled() {
            return SystemAction.get(PasteAction.class).isEnabled();
        }

        public @Override Object getValue(String key) {
            return SystemAction.get(PasteAction.class).getValue(key);
        }
    }
}
