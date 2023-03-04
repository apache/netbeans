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

package org.netbeans.core.execution;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.TopSecurityManager;
import org.netbeans.core.ModuleActions;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.execution.ExecutorTask;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Registers security manager for execution.
 * Also shows Pending Tasks dialog at shutdown time.
 * Also adds/removes specific beaninfo and property editor search paths.
 * @author Jesse Glick
 */
@OnStart
public class Install implements Runnable {
    
    private static final Logger LOG = Logger.getLogger(Install.class.getName());
    
    public @Override void run() {
        TopSecurityManager.register(SecMan.DEFAULT);
    }
    
    @OnStop
    public static final class Down implements Runnable {
        
        public @Override void run() {
            showPendingTasks();

            TopSecurityManager.unregister(SecMan.DEFAULT);
        }
    }

    @OnStop
    public static final class Closing implements Callable {
        @Override
        public Boolean call() throws Exception {
            return showPendingTasks();
        }
    }
    
    /** A class that server as a pending dialog manager.
     * It closes the dialog if there are no more pending tasks
     * and also servers as the action listener for killing the tasks.
     */
    private static class PendingDialogCloser extends WindowAdapter implements Runnable,
            PropertyChangeListener, ActionListener, NodeListener {
        private Dialog[] dialogHolder;
        private Object exitOption;
        PendingDialogCloser(Dialog[] holder, Object exit) {
            dialogHolder = holder;
            exitOption = exit;
        }
        
        public void run() {
            dialogHolder[0].setVisible(false);
        }
        
        // Beware: this may be called also from rootNode's prop changes
        // Once all pending tasks are gone, close the dialog.
        public void propertyChange(PropertyChangeEvent evt) {
            if(ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                checkClose();
            }
        }
        
        // kill pending tasks and close the dialog
        public void actionPerformed(ActionEvent evt) {
            if(evt.getSource() == exitOption) {
                killPendingTasks();
                Mutex.EVENT.readAccess(this); // close in AWT
            }
        }
        
        public void childrenRemoved(NodeMemberEvent evt) {
            checkClose();
        }
        
        // Dialog was opened but pending tasks could disappear inbetween.
        public @Override void windowOpened(WindowEvent evt) {
            checkClose();
        }
        
        /** Checks if there are pending tasks and closes (in AWT)
         * the dialog if not. */
        private void checkClose() {
            if(dialogHolder[0] != null && getPendingTasks().isEmpty()) {
                Mutex.EVENT.readAccess(this);
            }
        }
        
        // noop - rest of node listener
        public void childrenAdded(NodeMemberEvent ev) {}
        public void childrenReordered(NodeReorderEvent ev) {}
        public void nodeDestroyed(NodeEvent ev) {}
        
    }
    
    // Remainder moved from ExitDialog:
    
    /** Shows dialog which waits for finishing of pending tasks,
     * (currently actions only) and offers to user to leave IDE
     * immediatelly interrupting those tasks.
     * @return <code>true</code> if to continue with the action
     * <code>false</code> if the action to cancel
     */
    private static boolean showPendingTasks() {
        // Avoid showing the tasks in the dialog when either running internal tests
        if (Boolean.getBoolean("netbeans.full.hack") // NOI18N
                // or there are no pending tasks.
                || getPendingTasks().isEmpty()) {
            return true;
        }
        
        EM panel = new EM();
        
        Dialog[] dialog = new Dialog[1];
        Node root = new AbstractNode(new PendingChildren());
        
        
        JButton exitOption = new JButton();
        Mnemonics.setLocalizedText(exitOption, NbBundle.getMessage(Install.class, "LAB_EndTasks"));
        // No default button.
        // exitOption.setDefaultCapable(false);
        exitOption.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(Install.class, "ACSD_EndTasks"));
        
        PendingDialogCloser closer = new PendingDialogCloser(dialog, exitOption);
        
        panel.getExplorerManager().setRootContext(root);
        // closer will autoclose the dialog if all pending tasks finish
        panel.getExplorerManager().addPropertyChangeListener(closer);
        
        DialogDescriptor dd = new DialogDescriptor(
                panel,
                NbBundle.getMessage(Install.class, "CTL_PendingTitle"),
                true, // modal
                new Object[] {
            exitOption,
            DialogDescriptor.CANCEL_OPTION
        },
                exitOption,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                closer
                );
        // #33135 - no Help button for this dialog
        dd.setHelpCtx(null);
        
        if(!getPendingTasks().isEmpty()) {
            root.addNodeListener(closer);
            
            dialog[0] = DialogDisplayer.getDefault().createDialog(dd);
            
            dialog[0].addWindowListener(closer);
            
            dialog[0].setVisible(true);
            dialog[0].dispose();
            
            if(dd.getValue() == DialogDescriptor.CANCEL_OPTION
                    || dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
                return false;
            }
            
        }
        
        return true;
    }
    
    private static class EM extends JPanel implements ExplorerManager.Provider {
        private ExplorerManager manager = new ExplorerManager();
        private org.openide.util.Lookup lookup;
        
        public EM() {
            manager = new ExplorerManager();
            ActionMap map = getActionMap();
            map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
            map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
            map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
            map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false
            
            lookup = ExplorerUtils.createLookup(manager, map);
            
            initComponent();
        }
        
        private void initComponent() {
            setLayout(new GridBagLayout());
            
            GridBagConstraints cons = new GridBagConstraints();
            cons.gridx = 0;
            cons.gridy = 0;
            cons.weightx = 1.0D;
            cons.fill = GridBagConstraints.HORIZONTAL;
            cons.insets = new Insets(11, 11, 0, 12);
            
            JLabel label = new JLabel();
            Mnemonics.setLocalizedText(label, NbBundle.getMessage(Install.class, "LAB_PendingTasks"));
            
            add(label, cons);
            
            cons.gridy = 1;
            cons.weighty = 1.0D;
            cons.fill = GridBagConstraints.BOTH;
            cons.insets = new Insets(2, 11, 0, 12);
            
            ListView view = new ListView();
            //#66881
            view.setBorder(UIManager.getBorder("Nb.ScrollPane.border"));
            label.setLabelFor(view);
            
            add(view, cons);
            
            view.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(Install.class, "ACSD_PendingTasks"));
            getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(Install.class, "ACSD_PendingTitle"));
            
            // set size requested by HIE guys
            Dimension origSize = getPreferredSize();
            setPreferredSize(new Dimension(origSize.width * 5 / 4, origSize.height));
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        public Lookup getLookup() {
            return lookup;
        }
        public @Override void addNotify() {
            super.addNotify();
            ExplorerUtils.activateActions(manager, true);
        }
        public @Override void removeNotify() {
            ExplorerUtils.activateActions(manager, false);
            super.removeNotify();
        }
    }
    
    /** Gets pending (running) tasks. Used as keys
     * for pending dialog root node children. Currently it gets pending
     * actions only. */
    static Collection<?> getPendingTasks() {

        ArrayList<Object> pendingTasks = new ArrayList<Object>( 10 ); // Action | ExecutorTask | InternalHandle
        pendingTasks.addAll(ModuleActions.getDefaultInstance().getRunningActions());
        
        ExecutionEngine ee = ExecutionEngine.getExecutionEngine();
        if (ee != null) {
            pendingTasks.addAll(ee.getRunningTasks());
        }
        
        pendingTasks.addAll(Arrays.asList(Controller.getDefault().getModel().getHandles()));
        
        // [PENDING] When it'll be added another types of tasks (locks etc.)
        // add them here to the list. Then you need to create also a nodes
        // for them in PendingChildren.createNodes.

        return pendingTasks;
    }
    
    /** Ends penidng tasks. */
    private static void killPendingTasks() {
        // no way to kill actions
        final LogRecord r = new LogRecord(Level.INFO, "KILL_PENDING_TASKS"); //NOI18N
        r.setLoggerName(LOG.getName());
        LOG.log(r);
        for (InternalHandle h : Controller.getDefault().getModel().getHandles()) {
            h.requestCancel();
        }
        
        killRunningExecutors();
        
        // [PENDING] When it'll be added another types of tasks (locks etc.)
        // kill them here.
    }
    
    /** Tries to kill running executions */
    private static void killRunningExecutors() {
        ExecutionEngine ee = ExecutionEngine.getExecutionEngine();
        if (ee == null) {
            return;
        }
        for (ExecutorTask et : new ArrayList<ExecutorTask>(ee.getRunningTasks())) {
            if ( !et.isFinished() ) {
                et.stop();
            }
        }
        
    }
    
    /** Children showing pending tasks. */
    /* non private because of tests - was private before */
    static class PendingChildren extends Children.Keys<Object /* Action|ExecutorTask|InternalHandle*/> 
            implements ExecutionListener, ListDataListener {
        
        /** Listens on changes of sources from getting the tasks from.
         * Currently on module actions only. */
        //        private PropertyChangeListener propertyListener;
        
        
        /** Constructs new children. */
        public PendingChildren() {
            /* XXX no equiv yet in CallableSystemAction
            propertyListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ModuleActions.PROP_RUNNING_ACTIONS.equals(evt.getPropertyName())) {
                        setKeys(getPendingTasks());
                    }
                }
            };
             
            ModuleActions.getDefault().addPropertyChangeListener(
                org.openide.util.WeakListeners.propertyChange (propertyListener, ModuleActions.getDefault())
            );
             */
            
            ExecutionEngine ee = ExecutionEngine.getExecutionEngine();
            if (ee != null) {
                ee.addExecutionListener(this);
            }
            Controller.getDefault().getModel().addListDataListener(this);
        }
        
        /** Implements superclass abstract method. Creates nodes from key.
         * @return <code>PendingActionNode</code> if key is of
         * <code>Action</code> type otherwise <code>null</code> */
        protected Node[] createNodes(Object key) {
            Node n = null;
            if(key instanceof Action) {
                Action action = (Action)key;
                Icon icon = (action instanceof SystemAction) ?
                    ((SystemAction)action).getIcon() : null;
                
                String actionName = (String)action.getValue(Action.NAME);
                if (actionName == null) actionName = ""; // NOI18N
                actionName = org.openide.awt.Actions.cutAmpersand(actionName);
                n = new NoActionNode(icon, actionName, NbBundle.getMessage(
                        Install.class, "CTL_ActionInProgress", actionName));
            } else if (key instanceof ExecutorTask) {
                n = new NoActionNode(null, key.toString(),
                        NbBundle.getMessage(Install.class, "CTL_PendingExternalProcess2",
                        // getExecutionEngine() had better be non-null, since getPendingTasks gave an ExecutorTask:
                        ExecutionEngine.getExecutionEngine().getRunningTaskName((ExecutorTask) key))
                        );
            } else if (key instanceof InternalHandle) {
                n = new NoActionNode(null, ((InternalHandle)key).getDisplayName(), null);
            }
            return n == null ? null : new Node[] { n };
        }
        
        protected @Override void addNotify() {
            setKeys(getPendingTasks());
            super.addNotify();
        }
        
        protected @Override void removeNotify() {
            setKeys(Collections.emptySet());
            super.removeNotify();
            ExecutionEngine ee = ExecutionEngine.getExecutionEngine();
            if (ee != null) {
                ee.removeExecutionListener(this);
            }
            Controller.getDefault().getModel().removeListDataListener(this);
        }
        
        // ExecutionListener implementation ------------------------------------
        
        public void startedExecution( ExecutionEvent ev ) {
            setKeys(getPendingTasks());
        }
        
        public void finishedExecution( ExecutionEvent ev ) {
            setKeys(getPendingTasks());
        }
        
        public void intervalAdded(ListDataEvent e) {
            setKeys(getPendingTasks());
        }
        
        public void intervalRemoved(ListDataEvent e) {
            setKeys(getPendingTasks());
        }
        
        
        public void contentsChanged(ListDataEvent e) {
            setKeys(getPendingTasks());
        }
        
    } //  End of class PendingChildren.
    
    /** Node without any actions. */
    private static class NoActionNode extends AbstractNode {
        private Image img;
        
        /** Creates node for action. */
        public NoActionNode(Icon icon, String name, String display) {
            super(Children.LEAF);
            if (icon != null) img = ImageUtilities.icon2Image(icon);
            
            setName(name);
            if (display != null) setDisplayName(display);
        }
        
        /** @return empty array of actions */
        public @Override Action[] getActions(boolean context) {
            return new Action[0];
        }
        
        /** @return provided icon or delegate to superclass if no icon provided.
         */
        public @Override Image getIcon(int type) {
            return img == null ? super.getIcon(type) : img;
        }
    }
}
