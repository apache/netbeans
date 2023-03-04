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

package org.netbeans.modules.openide.explorer;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static org.netbeans.modules.openide.explorer.Bundle.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExtendedDelete;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * This class contains the default implementation of reactions to the standard
 * explorer actions. It can be attached to any {@link ExplorerManager}. Then
 * this class will listen to changes of selected nodes or the explored context
 * of that manager, and update the state of cut/copy/paste/delete actions.  <P>
 * An instance of this class can only be attached to one manager at a time. Use
 * {@link #attach} and {@link #detach} to make the connection.
 *
 * @author Jan Jancura, Petr Hamernik, Ian Formanek, Jaroslav Tulach
 */
public final class ExplorerActionsImpl {
    /** background updater of actions */
    private static final RequestProcessor RP = new RequestProcessor("Explorer Actions"); // NOI18N

    private static final Logger LOG = Logger.getLogger(ExplorerActionsImpl.class.getName());
    
    /** copy action performer */
    private final CopyCutActionPerformer copyActionPerformer = new CopyCutActionPerformer(true);

    /** cut action performer */
    private final CopyCutActionPerformer cutActionPerformer = new CopyCutActionPerformer(false);

    /** delete action performer */
    private final DeleteActionPerformer deleteActionPerformerConfirm = new DeleteActionPerformer(true);

    /** delete action performer no confirm */
    private final DeleteActionPerformer deleteActionPerformerNoConfirm = new DeleteActionPerformer(false);

    /** own paste action */
    private final OwnPaste pasteActionPerformer = new OwnPaste();
    private ActionStateUpdater actionStateUpdater;

    /** the manager we are listening on */
    private ExplorerManager manager;

    /** Creates new instance with a decision whether the action should update
     * performers (the old behaviour) or only set the state of cut,copy,delete,
     * and paste actions.
     */
    public ExplorerActionsImpl() {
    }

    //
    // Implementation
    //

    /** Getter for the copy action.
     */
    public Action copyAction() {
        return copyActionPerformer;
    }

    /** The cut action */
    public Action cutAction() {
        return cutActionPerformer;
    }

    /** The delete action
     */
    public Action deleteAction(boolean confirm) {
        return confirm ? deleteActionPerformerConfirm : deleteActionPerformerNoConfirm;
    }

    /** Own paste action
     */
    public Action pasteAction() {
        return pasteActionPerformer;
    }

    /** Attach to new manager.
     * @param m the manager to listen on
     */
    public synchronized void attach(ExplorerManager m) {
        if (manager != null) {
            // first of all detach
            detach();
        }

        manager = m;

        // Sets action state updater and registers listening on manager and
        // exclipboard.
        actionStateUpdater = new ActionStateUpdater(manager);
        actionStateUpdater.schedule();
    }

    /** Detach from manager currently being listened on. */
    public synchronized void detach() {
        if (manager == null || actionStateUpdater == null) {
            return;
        }

        // Unregisters (weak) listening on manager and exclipboard (see attach).
        actionStateUpdater.unlisten(manager);
        actionStateUpdater = null;

        stopActions();

        manager = null;
    }

    /** Stops listening on all actions */
    final void stopActions() {
        assert EventQueue.isDispatchThread();
        if (copyActionPerformer != null) {
            copyActionPerformer.setEnabled(false);
            cutActionPerformer.setEnabled(false);
            deleteActionPerformerConfirm.setEnabled(false);
            deleteActionPerformerNoConfirm.setEnabled(false);
            pasteActionPerformer.setEnabled(false);
        }
    }

    /** Updates the state of all actions.
     * @param path list of selected nodes
     */
    final void updateActions(boolean updatePasteAction) {
        assert !EventQueue.isDispatchThread();
        ExplorerManager m;
        synchronized (this) {
            m = manager;
        }
        if (m == null) {
            return;
        }

        Node[] path = m.getSelectedNodes();

        int i;
        int k = (path != null) ? path.length : 0;

        if (k > 0) {
            boolean incest = false;

            if (k > 1) {
                // Do a special check for parenthood. Affects delete (for a long time),
                // copy (#13418), cut (#13426). If one node is a parent of another,
                // assume that the situation is sketchy and prevent it.
                // For k==1 it is impossible so do not waste time on it.
                HashMap<Node, Object> allNodes = new HashMap<Node, Object>(101);

                for (i = 0; i < k; i++) {
                    if (!checkParents(path[i], allNodes)) {
                        incest = true;

                        break;
                    }
                }
            }

            for (i = 0; i < k; i++) {
                if (incest || !path[i].canCopy()) {
                    copyActionPerformer.toEnabled(false);

                    break;
                }
            }

            if (i == k) {
                copyActionPerformer.toEnabled(true);
            }

            for (i = 0; i < k; i++) {
                if (incest || !path[i].canCut()) {
                    cutActionPerformer.toEnabled(false);

                    break;
                }
            }

            if (i == k) {
                cutActionPerformer.toEnabled(true);
            }

            for (i = 0; i < k; i++) {
                if (incest || !path[i].canDestroy()) {
                    deleteActionPerformerConfirm.toEnabled(false);
                    deleteActionPerformerNoConfirm.toEnabled(false);

                    break;
                }
            }

            if (i == k) {
                deleteActionPerformerConfirm.toEnabled(true);
                deleteActionPerformerNoConfirm.toEnabled(true);
            }
        } else { // k==0, i.e. no nodes selected
            copyActionPerformer.toEnabled(false);
            cutActionPerformer.toEnabled(false);
            deleteActionPerformerConfirm.toEnabled(false);
            deleteActionPerformerNoConfirm.toEnabled(false);
        }

        if (updatePasteAction) {
            updatePasteAction(path);
        }
    }

    /** Adds all parent nodes into the set.
     * @param set set of all nodes
     * @param node the node to check
     * @return false if one of the nodes is parent of another
     */
    private boolean checkParents(Node node, HashMap<Node, Object> set) {
        if (set.get(node) != null) {
            return false;
        }

        // this signals that this node is the original one
        set.put(node, this);

        for (;;) {
            node = node.getParentNode();

            if (node == null) {
                return true;
            }

            if (set.put(node, node) == this) {
                // our parent is a node that is also in the set
                return false;
            }
        }
    }

    /** Updates paste action.
    * @param path selected nodes
    */
    private void updatePasteAction(Node[] path) {
        ExplorerManager man = manager;

        if (man == null) {
            pasteActionPerformer.setPasteTypes(null);

            return;
        }

        if ((path != null) && (path.length > 1)) {
            pasteActionPerformer.setPasteTypes(null);

            return;
        } else {
            Node node = man.getExploredContext();
            Node[] selectedNodes = man.getSelectedNodes();

            if ((selectedNodes != null) && (selectedNodes.length == 1)) {
                node = selectedNodes[0];
            }

            if (node != null) {
                if (actionStateUpdater != null) {
                    Transferable trans = actionStateUpdater.getTransferable();
                    if (trans != null) {
                        updatePasteTypes(wrapTransferable(trans, node), node);
                    }
                } else {
                    LOG.fine("#126145: caused by http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6322854");
                }
            }
        }
    }

    /**
     * Wrap transferable to an instance that can hold information about node on
     * which the operation was invoked. (And thus its parent node and context
     * are known). See comment at o.o.loaders.DataNode.getPasteTypesFromParent()
     * and bug 250134.
     *
     * @param trans The Transferable object.
     * @param node The node on which the paste operation would be invoked.
     */
    private Transferable wrapTransferable(Transferable trans, Node node) {
        class ExplorerTransferable implements Transferable, Lookup.Provider {
            private final Transferable delegate;
            private final Lookup lookup;

            public ExplorerTransferable(Transferable delegate, Node node) {
                this.delegate = delegate;
                this.lookup = Lookups.singleton(node);
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return delegate.getTransferDataFlavors();
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return delegate.isDataFlavorSupported(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException, IOException {
                return delegate.getTransferData(flavor);
            }

            @Override
            public Lookup getLookup() {
                return lookup;
            }
        }
        return new ExplorerTransferable(trans, node);
    }

    /** Actually updates paste types. */
    private void updatePasteTypes(Transferable trans, Node pan) {
        if (trans != null) {
            // First, just ask the node if it likes this transferable, whatever it may be.
            // If it does, then fine.
            PasteType[] pasteTypes = (pan == null) ? new PasteType[] {  } : pan.getPasteTypes(trans);

            if (pasteTypes.length != 0) {
                pasteActionPerformer.setPasteTypes(pasteTypes);

                return;
            }

            if (trans.isDataFlavorSupported(ExTransferable.multiFlavor)) {
                // The node did not accept this multitransfer as is--try to break it into
                // individual transfers and paste them in sequence instead.
                try {
                    MultiTransferObject obj = (MultiTransferObject) trans.getTransferData(ExTransferable.multiFlavor);
                    int count = obj.getCount();
                    boolean ok = true;
                    Transferable[] t = new Transferable[count];
                    PasteType[] p = new PasteType[count];

                    for (int i = 0; i < count; i++) {
                        t[i] = obj.getTransferableAt(i);
                        pasteTypes = (pan == null) ? new PasteType[] {  } : pan.getPasteTypes(t[i]);

                        if (pasteTypes.length == 0) {
                            ok = false;

                            break;
                        }

                        // [PENDING] this is ugly! ideally should be some way of comparing PasteType's for similarity?
                        p[i] = pasteTypes[0];
                    }

                    if (ok) {
                        PasteType[] arrOfPaste = new PasteType[] { new MultiPasteType(t, p) };
                        pasteActionPerformer.setPasteTypes(arrOfPaste);

                        return;
                    }
                } catch (UnsupportedFlavorException e) {
                    // [PENDING] notify?!
                } catch (IOException e) {
                    // [PENDING] notify?!
                }
            }
        }

        pasteActionPerformer.setPasteTypes(null);
    }
    
    private static Transferable getTransferableOwner(Node node, boolean copyCut) {
        try {
            return copyCut ? node.clipboardCopy() : node.clipboardCut();
        } catch (IOException e) {
            LOG.log(Level.WARNING, null, e);
            return null;
        }
    }
    
    /**
     * Get a transferable of a selection of node(s).
     * @param sel An array with selected nodes.
     * @param copyCut <code>true</code> for copy, <code>false</code> for cut.
     * @return The transferable or <code>null</code>
     */
    public static Transferable getTransferableOwner(Node[] sel, boolean copyCut) {
        Transferable trans;
        if (sel.length != 1) {
            Transferable[] arrayTrans = new Transferable[sel.length];

            for (int i = 0; i < sel.length; i++) {
                if ((arrayTrans[i] = getTransferableOwner(sel[i], copyCut)) == null) {
                    return null;
                }
            }

            trans = ExternalDragAndDrop.maybeAddExternalFileDnd( new ExTransferable.Multi(arrayTrans) );
        } else {
            trans = getTransferableOwner(sel[0], copyCut);
        }

        return trans;
    }

    /** If our clipboard is not found return the default system clipboard. */
    public static Clipboard getClipboard() {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        Clipboard c = Lookup.getDefault().lookup(Clipboard.class);

        if (c == null) {
            c = Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        return c;
    }

    final void syncActions() {
        copyActionPerformer.syncEnable();
        cutActionPerformer.syncEnable();
        deleteActionPerformerConfirm.syncEnable();
        deleteActionPerformerNoConfirm.syncEnable();
        pasteActionPerformer.syncEnable();
    }

    private boolean actionsUpdateScheduled() {
        ActionStateUpdater asu = actionStateUpdater;
        return asu != null ? asu.updateScheduled() : false;
    }

    /** Paste type used when in clipbopard is MultiTransferable */
    private static class MultiPasteType extends PasteType {
        /** Array of transferables */
        Transferable[] t;

        /** Array of paste types */
        PasteType[] p;

        /** Constructs new MultiPasteType for the given content of the clipboard */
        MultiPasteType(Transferable[] t, PasteType[] p) {
            this.t = t;
            this.p = p;
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the clipboard after
        *         paste action. It can be null, which means that clipboard content
        *         should be cleared.
        */
        @Override
        public Transferable paste() throws IOException {
            int size = p.length;
            Transferable[] arr = new Transferable[size];

            for (int i = 0; i < size; i++) {
                Transferable newTransferable = p[i].paste();

                if (newTransferable != null) {
                    arr[i] = newTransferable;
                } else {
                    // keep the orginal
                    arr[i] = t[i];
                }
            }

            return new ExTransferable.Multi(arr);
        }
    }

    /** Own implementation of paste action
     */
    private class OwnPaste extends BaseAction {
        private PasteType[] pasteTypes;

        OwnPaste() {
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled();
        }

        public void setPasteTypes(PasteType[] arr) {
            synchronized (this) {
                this.pasteTypes = arr;
            }
            LOG.log(Level.FINER, "setPasteTypes for {0}", Arrays.toString(arr));
            toEnabled(arr != null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            PasteType[] arr;
            synchronized (this) {
                arr = this.pasteTypes;
            }
            if (arr != null && arr.length > 0) {
                try {
                    arr[0].paste();
                    return;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            Utilities.disabledActionBeep();
        }

        @Override
        public Object getValue(String s) {
            if ("delegates".equals(s)) { // NOI18N
                String prev = "";
                if (LOG.isLoggable(Level.FINE)) {
                    synchronized (this) {
                        prev = Arrays.toString(pasteTypes);
                    }
                }
                ActionStateUpdater asu = actionStateUpdater;
                if (asu != null) {
                    asu.update();
                }
                if (LOG.isLoggable(Level.FINE)) {
                    String now;
                    synchronized (this) {
                        now = Arrays.toString(pasteTypes);
                    }
                    if (now == null) {
                        now = "";
                    }
                    if (prev.equals(now)) {
                        LOG.log(Level.FINER, "getDelegates {0}", now);
                    } else {
                        LOG.log(Level.FINE, "Delegates updated. Before: {0}", prev);
                        LOG.log(Level.FINE, "Delegates updated. After : {0}", now);
                    }
                }
                synchronized (this) {
                    return pasteTypes;
                }
            }

            return super.getValue(s);
        }
    }
    
    private abstract static class BaseAction extends AbstractAction {
        private static final int NO_CHANGE = 0;
        private static final int ENABLED = 1;
        private static final int DISABLED = 2;

        private final AtomicInteger toEnable = new AtomicInteger(NO_CHANGE);
        
        public void toEnabled(boolean e) {
            toEnable.set(e ? ENABLED : DISABLED);
        }
        
        public void syncEnable() {
            assert EventQueue.isDispatchThread();
            int toEnableValue = toEnable.getAndSet(NO_CHANGE);
            if (toEnableValue != NO_CHANGE) {
                setEnabled(toEnableValue == ENABLED);
            }
        }
    }

    /** Class which performs copy and cut actions */
    private class CopyCutActionPerformer extends BaseAction {
        /** determine if adapter is used for copy or cut action. */
        private boolean copyCut;

        /** Create new adapter */
        public CopyCutActionPerformer(boolean b) {
            copyCut = b;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled();
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            ExplorerManager em = manager;
            if (em == null) {
                return;
            }
            Node[] sel = em.getSelectedNodes();
            Transferable trans = getTransferableOwner(sel, copyCut);

            if (trans != null) {
                Clipboard clipboard = getClipboard();
                if (clipboard != null) {
                    clipboard.setContents(trans, new StringSelection("")); // NOI18N
                }
            }
        }

    }

    /** Class which performs delete action */
    private class DeleteActionPerformer extends BaseAction implements Runnable {
        private boolean confirmDelete;

        DeleteActionPerformer(boolean confirmDelete) {
            this.confirmDelete = confirmDelete;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled();
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            ExplorerManager em = manager;
            if (em == null) {
                return;
            }

            final Node[] sel = em.getSelectedNodes();
            if ((sel == null) || (sel.length == 0)) {
                return;
            }

            for (ExtendedDelete del : Lookup.getDefault().lookupAll(ExtendedDelete.class)) {
                try {
                    if (del.delete(sel)) return;
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                    return;
                }
            }
            
            // perform action if confirmed
            if (!confirmDelete || doConfirm(sel)) {
                // clear selected nodes
                try {
                    em.setSelectedNodes(new Node[]{});
                } catch (PropertyVetoException e) {
                // never thrown, setting empty selected nodes cannot be vetoed
                }

                doDestroy(sel);

                // disables the action in AWT thread
                Mutex.EVENT.readAccess(this);
            }
        }

        /** Disables the action.
         */
        @Override
        public void run() {
            assert EventQueue.isDispatchThread();
            setEnabled(false);
        }

        // ExplorerActionsImpl and openide.compat/src/org/openide/explorer/ExplorerActions.java
        @Messages({
            "# {0} - name", "MSG_ConfirmDeleteObject=Are you sure you want to delete {0}?",
            "MSG_ConfirmDeleteObjectTitle=Confirm Object Deletion",
            "# {0} - number of objects", "MSG_ConfirmDeleteObjects=Are you sure you want to delete these {0} items?",
            "MSG_ConfirmDeleteObjectsTitle=Confirm Multiple Object Deletion"
        })
        private boolean doConfirm(Node[] sel) {
            String message;
            String title;
            boolean customDelete = true;

            for (int i = 0; i < sel.length; i++) {
                if (!Boolean.TRUE.equals(sel[i].getValue("customDelete"))) { // NOI18N
                    customDelete = false;

                    break;
                }
            }

            if (customDelete) {
                return true;
            }

            if (sel.length == 1) {
                message = MSG_ConfirmDeleteObject(
                        sel[0].getDisplayName()
                    );
                title = MSG_ConfirmDeleteObjectTitle();
            } else {
                message = MSG_ConfirmDeleteObjects(
                        Integer.valueOf(sel.length)
                    );
                title = MSG_ConfirmDeleteObjectsTitle();
            }

            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);

            return NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc));
        }

        private void doDestroy(final Node[] sel) {
            for (int i = 0; i < sel.length; i++) {
                try {
                    sel[i].destroy();
                }
                catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }

    }
    
    public final void waitFinished() {
        ActionStateUpdater u = actionStateUpdater;
        synchronized (this) {
            u = actionStateUpdater;
        }
        if (u == null) {
            return;
        }
        u.waitFinished();
        if (EventQueue.isDispatchThread()) {
            u.run();
        } else {
            try {
                EventQueue.invokeAndWait(u);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /** Class which register changes in manager, and clipboard, coalesces
     * them if they are frequent and performs the update of actions state. */
    private class ActionStateUpdater implements PropertyChangeListener, FlavorListener, Runnable {
        private final RequestProcessor.Task timer;
        private final PropertyChangeListener weakL;
        private FlavorListener flavL;
        private Transferable trans;

        ActionStateUpdater(ExplorerManager m) {
            timer = RP.create(this);
            weakL = WeakListeners.propertyChange(this, m);
            m.addPropertyChangeListener(weakL);
        }

        void unlisten(ExplorerManager m) {
            m.removePropertyChangeListener(weakL);
        }

        boolean updateScheduled() {
            return timer.getDelay() > 0;
        }

        @Override
        public synchronized void propertyChange(PropertyChangeEvent e) {
            schedule();
        }

        @Override
        public void flavorsChanged(FlavorEvent ev) {
            schedule();
        }

        @Override
        public void run() {
            if (EventQueue.isDispatchThread()) {
                syncActions();
            } else {
                updateActions(false);
                EventQueue.invokeLater(this);
                registerListener();
                updateTrans();
                updateActions(true);
                EventQueue.invokeLater(this);
            }
        }
        
        private void registerListener() {
            if (flavL == null) {
                Clipboard c = getClipboard();
                if (c != null) {
                    flavL = WeakListeners.create(FlavorListener.class, this, c);
                    c.addFlavorListener(flavL);
                }
            }
        }

        private void updateTrans() {
            Clipboard clipboard = getClipboard();
            if (clipboard == null) {
                return;
            }
            Transferable t = clipboard.getContents(ExplorerActionsImpl.this);
            synchronized (this) {
                trans = t;
            }
        }
        
        final Transferable getTransferable() {
            return trans;
        }

        /** Updates actions states now if there is pending event. */
        public void update() {
            if (EventQueue.isDispatchThread()) {
                try {
                    timer.waitFinished(100);
                } catch (InterruptedException ex) {
                    LOG.log(Level.FINE, null, ex);
                }
            } else {
                timer.waitFinished();
            }
        }

        private void schedule() {
            copyActionPerformer.toEnabled(false);
            cutActionPerformer.toEnabled(false);
            deleteActionPerformerConfirm.toEnabled(false);
            deleteActionPerformerNoConfirm.toEnabled(false);
            pasteActionPerformer.toEnabled(false);
            EventQueue.invokeLater(this);
            timer.schedule(0);
        }

        final void waitFinished() {
            timer.waitFinished();
        }
    }
}
