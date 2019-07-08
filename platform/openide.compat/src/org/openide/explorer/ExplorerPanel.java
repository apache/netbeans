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
package org.openide.explorer;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.io.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.*;

import java.text.MessageFormat;

import javax.swing.Timer;


/** Simple top component capable of displaying an Explorer.
* Holds one instance of {@link ExplorerManager} and
* implements {@link ExplorerManager.Provider} to allow child components to share
* the same explorer manager.
* <p>Uses {@link java.awt.BorderLayout} by default.
* Pays attention to the selected nodes and explored context as indicated by the manager.
* Cut/copy/paste actions are sensitive to the activation state of the component.
* <p>It is up to you to add a view and other UI apparatus to the panel.
*
* @deprecated Use {@link ExplorerUtils#actionCopy}, etc, see {@link ExplorerUtils} javadoc
*   for details
* @author Jaroslav Tulach
*/
@Deprecated
public class ExplorerPanel extends TopComponent implements ExplorerManager.Provider {
    /** serial version UID */
    static final long serialVersionUID = 5522528786650751459L;

    /** The message formatter for Explorer title */
    private static MessageFormat formatExplorerTitle;

    /** Init delay for second change of the activated nodes. */
    private static final int INIT_DELAY = 70;

    /** Maximum delay for repeated change of the activated nodes. */
    private static final int MAX_DELAY = 350;
    private static Boolean scheduleAcivatedNodes;

    /** mapping from ExplorerManagers to the ExplorerPanels they are associated
     * with. ExplorerManager -> Reference (ExplorerPanel)
     */
    private static java.util.WeakHashMap panels = new java.util.WeakHashMap();

    /** the instance of the explorer manager*/
    private ExplorerManager manager;

    /** listens on the selected nodes in the ExporerManager */
    transient private final PropertyChangeListener managerListener = new PropL();

    /** action handler for cut/copy/paste/delete for this panel */
    private ExplorerActions actions;
    private transient DelayedSetter delayedSetter;

    /** Initialize the explorer panel with the provided manager.
    * @param manager the explorer manager to use
    */
    public ExplorerPanel(ExplorerManager manager) {
        this(manager, null);
    }

    /** Default constructor. Uses newly created manager.
    */
    public ExplorerPanel() {
        this(null, null);
    }

    /** Allows to create an explorer with disabled delete confirmation.
     * @param confirm false if delete action should not be confirmed
     */
    ExplorerPanel(ExplorerManager manager, boolean confirm) {
        this(manager, Boolean.valueOf(confirm));
    }

    private ExplorerPanel(ExplorerManager manager, Boolean confirm) {
        if (manager == null) {
            manager = new ExplorerManager();
        }

        this.manager = manager;
        panels.put(manager, new java.lang.ref.WeakReference(this));

        setLayout(new java.awt.BorderLayout());
        initActionMap(confirm);
        initListening();
    }

    // bugfix #36509, added 3-state (true/false/unset) parameter for confirm delete
    // unset means read parameter from the global ConfirmDelete option

    /** Initializes actions map. */
    private void initActionMap(Boolean confirm) {
        ExplorerActions a = new ExplorerActions(false);

        if (confirm != null) {
            a.setConfirmDelete(confirm.booleanValue());
        }

        a.attach(getExplorerManager());
    }

    /** Called from a ExplorerActions.attach, to notify that these
     * ExplorerActions will now be responsible for given explorer
     * manager.
     *
     * @param actions the actions
     * @param em the manager
     */
    static void associateActions(ExplorerActions actions, ExplorerManager em) {
        java.lang.ref.Reference ref = (java.lang.ref.Reference) panels.get(em);
        ExplorerPanel p = (ref == null) ? null : (ExplorerPanel) ref.get();

        if (p != null) {
            p.getActionMap().put(javax.swing.text.DefaultEditorKit.copyAction, actions.copyAction());
            p.getActionMap().put(javax.swing.text.DefaultEditorKit.cutAction, actions.cutAction());
            p.getActionMap().put(javax.swing.text.DefaultEditorKit.pasteAction, actions.pasteAction());
            p.getActionMap().put("delete", actions.deleteAction() // NOI18N
            );

            // and remember the actions
            p.actions = actions;
        }
    }

    /** Initializes listening on ExplorerManager property changes. */
    private void initListening() {
        // Attaches listener if there is not one already.
        ExplorerManager man = getExplorerManager();
        man.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(managerListener, man));
        setActivatedNodes(manager.getSelectedNodes());
    }

    /* Add a listener to the explorer panel in addition to the normal
    * open behaviour.
    */
    public void open() {
        open(WindowManager.getDefault().getCurrentWorkspace());
    }

    /* Add a listener to the explorer panel in addition to the normal
    * open behaviour.
    */
    public void open(Workspace workspace) {
        super.open(workspace);
        setActivatedNodes(getExplorerManager().getSelectedNodes());
        updateTitle();
    }

    /* Provides the explorer manager to all who are interested.
    * @return the manager
    */
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    /** Activates copy/cut/paste actions.
     */
    protected void componentActivated() {
        if (actions != null) {
            actions.attach(getExplorerManager());
        }
    }

    /* Deactivates copy/cut/paste actions.
     */
    protected void componentDeactivated() {
        if (actions != null) {
            actions.detach();
        }
    }

    /** Called when the explored context changes.
    * The default implementation updates the title of the window.
    */
    protected void updateTitle() {
        String name = ""; // NOI18N

        ExplorerManager em = getExplorerManager();

        if (em != null) {
            Node n = em.getExploredContext();

            if (n != null) {
                String nm = n.getDisplayName();

                if (nm != null) {
                    name = nm;
                }
            }
        }

        if (formatExplorerTitle == null) {
            formatExplorerTitle = new MessageFormat(NbBundle.getMessage(ExplorerPanel.class, "explorerTitle"));
        }

        setName(formatExplorerTitle.format(new Object[] { name }));
    }

    /** Get context help for an explorer window.
    * Looks at the manager's node selection.
    * @return the help context
    * @see #getHelpCtx(Node[],HelpCtx)
    */
    public HelpCtx getHelpCtx() {
        return getHelpCtx(getExplorerManager().getSelectedNodes(), new HelpCtx(ExplorerPanel.class));
    }

    /** Utility method to get context help from a node selection.
    * Tries to find context helps for selected nodes.
    * If there are some, and they all agree, uses that.
    * In all other cases, uses the supplied generic help.
    * @param sel a list of nodes to search for help in
    * @param def the default help to use if they have none or do not agree
    * @return a help context
    */
    public static HelpCtx getHelpCtx(Node[] sel, HelpCtx def) {
        return ExplorerUtils.getHelpCtx(sel, def);
    }

    /** Set whether deletions should have to be confirmed on all Explorer panels.
    * @param confirmDelete <code>true</code> to confirm, <code>false</code> to delete at once
    */
    public static void setConfirmDelete(boolean confirmDelete) {
        NbPreferences.root().node("/org/netbeans/core").putBoolean("confirmDelete",confirmDelete);//NOI18N
    }

    /** Are deletions confirmed on all Explorer panels?
    * @return <code>true</code> if they must be confirmed
    */
    public static boolean isConfirmDelete() {
        return NbPreferences.root().node("/org/netbeans/core").getBoolean("confirmDelete",true);//NOI18N
    }

    /** Stores the manager */
    public void writeExternal(ObjectOutput oo) throws IOException {
        super.writeExternal(oo);
        oo.writeObject(new NbMarshalledObject(manager));
    }

    /** Reads the manager.
    * Deserialization may throw {@link SafeException} in case
    * the manager cannot be loaded correctly but the stream is still uncorrupted.
    */
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal(oi);

        Object anObj = oi.readObject();

        if (anObj instanceof ExplorerManager) {
            manager = (ExplorerManager) anObj;
            panels.put(manager, new java.lang.ref.WeakReference(this));
            initActionMap(null);
            initListening();

            return;
        }

        NbMarshalledObject obj = (NbMarshalledObject) anObj;

        // --- read all data from main stream, it is OK now ---
        try {
            manager = (ExplorerManager) obj.get();
            panels.put(manager, new java.lang.ref.WeakReference(this));
            initActionMap(null);
            initListening();
        } catch (SafeException se) {
            throw se;
        } catch (IOException ioe) {
            throw new SafeException(ioe);
        }
    }

    // temporary workaround the issue #31244
    private boolean delayActivatedNodes() {
        if (scheduleAcivatedNodes == null) {
            if (System.getProperty("netbeans.delay.tc") != null) { // NOI18N
                scheduleAcivatedNodes = Boolean.getBoolean("netbeans.delay.tc") ? Boolean.TRUE : Boolean.FALSE; // NOI18N
            } else {
                scheduleAcivatedNodes = Boolean.FALSE;
            }
        }

        return scheduleAcivatedNodes.booleanValue();
    }

    // schudule activation the nodes
    private final void scheduleActivatedNodes(Node[] nodes) {
        synchronized (this) {
            if (delayedSetter == null) {
                delayedSetter = new DelayedSetter();
            }
        }

        delayedSetter.scheduleActivatedNodes(nodes);
    }

    /** Listener on the explorer manager properties.
    * Changes selected nodes of this frame.
    */
    private final class PropL extends Object implements PropertyChangeListener {
        PropL() {
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() != manager) {
                return;
            }

            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                if (delayActivatedNodes()) {
                    scheduleActivatedNodes(manager.getSelectedNodes());
                } else {
                    setActivatedNodes(manager.getSelectedNodes());
                }

                return;
            }

            if (ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                updateTitle();

                return;
            }
        }
    }

    private class DelayedSetter implements ActionListener {
        private Node[] nodes;
        private Timer timer;
        private boolean firstChange = true;

        DelayedSetter() {
        }

        public void scheduleActivatedNodes(Node[] nodes) {
            synchronized (this) {
                this.nodes = nodes;

                if (timer == null) {
                    // start timer with INIT_DELAY
                    timer = new Timer(INIT_DELAY, this);
                    timer.setCoalesce(true);
                    timer.setRepeats(false);
                }

                if (timer.isRunning()) {
                    // if timer is running then double init delay
                    if (timer.getInitialDelay() < MAX_DELAY) {
                        timer.setInitialDelay(timer.getInitialDelay() * 2);
                    }

                    firstChange = false;
                } else {
                    // the first change is set immediatelly
                    setActivatedNodes(nodes);
                    firstChange = true;
                }

                // make sure timer is running
                timer.restart();
            }
        }

        public void actionPerformed(ActionEvent evt) {
            synchronized (this) {
                synchronized (this) {
                    timer.stop();
                }
            }

            // set activated nodes for 2nd and next changes
            if (!firstChange) {
                setActivatedNodes(nodes);
            }
        }
    }
}
