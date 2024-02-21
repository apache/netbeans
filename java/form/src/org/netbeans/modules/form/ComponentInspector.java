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

package org.netbeans.modules.form;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.beans.*;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit;

import org.openide.*;
import org.openide.actions.PasteAction;
import org.openide.nodes.*;
import org.openide.explorer.*;
import org.openide.awt.UndoRedo;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.datatransfer.*;

import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.util.lookup.ProxyLookup;

/**
 * The ComponentInspector - special explorer for form editor.
 *
 * @author Tomas Pavek
 */

public class ComponentInspector extends JPanel
                                implements NavigatorPanelWithUndo, ExplorerManager.Provider
{
    private FormDesigner formDesigner;

    private ExplorerManager explorerManager;
    private ExplorerManagerLookup lookup;

    private PropertyChangeListener nodeSelectionListener;

    private PasteAction pasteAction = SystemAction.findObject(PasteAction.class, true);

    private CopyCutActionPerformer copyActionPerformer = new CopyCutActionPerformer(true);
    private CopyCutActionPerformer cutActionPerformer = new CopyCutActionPerformer(false);
    private DeleteActionPerformer deleteActionPerformer = new DeleteActionPerformer();

    private ClipboardListener clipboardListener;

    private BeanTreeView treeView;

    private static ComponentInspector instance;

    // ------------
    // construction (ComponentInspector is a singleton)

    /** Finds default instance. Use in client code instead of {@link #getDefault()}.
     * 
     * @return ComponentInspector singleton.
     */
    public static synchronized ComponentInspector getInstance() {
        if (instance == null) {
            instance = new ComponentInspector();
        }
        return instance;
    }

    static boolean exists() {
        return instance != null;
    }

    private ComponentInspector() {
        lookup = new ExplorerManagerLookup();

        setLayout(new java.awt.BorderLayout());
        createComponents();
        setupActionMap(getActionMap());

        HelpCtx.setHelpIDString(this, "gui.component-inspector"); // NOI18N
    }

    final javax.swing.ActionMap setupActionMap(javax.swing.ActionMap map) {
        map.put(DefaultEditorKit.copyAction, copyActionPerformer);
        map.put(DefaultEditorKit.cutAction, cutActionPerformer);
        //map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(explorerManager));
        map.put("delete", deleteActionPerformer); // NOI18N

        return map;
    }

    private void createComponents() {
        treeView = new BeanTreeView();
        treeView.setDragSource(true);
        treeView.setDropTarget(true);
        treeView.getAccessibleContext().setAccessibleName(
            FormUtils.getBundleString("ACS_ComponentTree")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_ComponentTree")); // NOI18N
        add(treeView, BorderLayout.CENTER);
    }

    // --------------
    // overriding superclasses, implementing interfaces

    @Override
    public ExplorerManager getExplorerManager() {
        if (explorerManager == null) {
            // placeholder ExplorerManager until we get one from FormDesigner
            explorerManager = new ExplorerManager();
        }
        return explorerManager;
    }

    @Override
    public UndoRedo getUndoRedo() {
        if (formDesigner != null) {
            FormModel formModel = formDesigner.getFormModel();
            if (formModel != null) {
                UndoRedo ur = formModel.getUndoRedoManager();
                if (ur != null) {
                    return ur;
                }
            }
        }
        return UndoRedo.NONE;
    }

    @Override
    public String getDisplayName() {
        return FormUtils.getBundleString("CTL_InspectorNavigatorCaption"); // NOI18N
    }

    @Override
    public String getDisplayHint() {
        return FormUtils.getBundleString("HINT_ComponentInspector"); // NOI18N
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void panelActivated(Lookup context) {
        attachActions();
        // actual context is set via setFormDesigner
    }

    @Override
    public void panelDeactivated() {
        detachActions();
        // actual context is set via setFormDesigner
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    // ------------
    // activating and focusing

    FormDesigner getFormDesigner() {
        return formDesigner;
    }

    void setFormDesigner(final FormDesigner designer) {
        if (designer != formDesigner) {
            if (java.awt.EventQueue.isDispatchThread()) {
                setFormDesignerImpl(designer);
            } else {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setFormDesignerImpl(designer);
                    }
                });
            }
        }
    }

    private void setFormDesignerImpl(FormDesigner designer) {
        if (explorerManager != null) {
            ExplorerUtils.activateActions(explorerManager, false);
            if (nodeSelectionListener != null) {
                explorerManager.removePropertyChangeListener(nodeSelectionListener);
            }
        }

        formDesigner = designer;

        if (designer == null) {
            lookup.setLookupFromExplorerManager(null, null);
            explorerManager = null;
            removeAll(); // swing memory leak workaround (old)
            createComponents();
            revalidate();
        } else {
            explorerManager = designer.getExplorerManager();
            remove(treeView);
            add(treeView, BorderLayout.CENTER);
            lookup.setLookupFromExplorerManager(explorerManager, getActionMap());
            if (nodeSelectionListener == null) {
                nodeSelectionListener = new NodeSelectionListener();
            }
            explorerManager.addPropertyChangeListener(nodeSelectionListener);
        }
    }

    void attachActions() {
        if (explorerManager == null) {
            return;
        }

        ExplorerUtils.activateActions(explorerManager, true);
        updatePasteAction();

        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            if (clipboardListener == null) {
                clipboardListener = new ClipboardChangesListener();
            } else {
                clip.removeClipboardListener(clipboardListener);
            }
            clip.addClipboardListener(clipboardListener);
        }
    }

    void detachActions() {
        if (explorerManager != null) {
            ExplorerUtils.activateActions(explorerManager, false);
        }

        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            clip.removeClipboardListener(clipboardListener);
        }
    }

    private Node[] getSelectedRootNodes() {
        // exclude nodes that are under other selected nodes
        Node[] selected = getExplorerManager().getSelectedNodes();
        if (selected != null && selected.length > 1) {
            List<Node> list = new ArrayList<Node>(selected.length);
            for (int i=0; i < selected.length; i++) {
                Node node = selected[i];
                boolean subcontained = false;
                for (int j=0; j < selected.length; j++) {
                    if (j != i && isSubcontainedNode(node, selected[j])) {
                        subcontained = true;
                        break;
                    }
                }
                if (!subcontained) {
                    list.add(node);
                }
            }
            if (list.size() < selected.length) {
                selected = list.toArray(new Node[0]);
            }
        }
        return selected;
    }

    private static boolean isSubcontainedNode(Node node, Node maybeParent) {
        RADComponentCookie cookie = node.getCookie(RADComponentCookie.class);
        RADComponent comp = (cookie != null) ? cookie.getRADComponent() : null;
        if (comp != null) {
            cookie = maybeParent.getCookie(RADComponentCookie.class);
            RADComponent parentComp = (cookie != null) ? cookie.getRADComponent() : null;
            if (parentComp != null && parentComp.isParentComponent(comp)) {
                return true;
            }
        }
        return false;
    }

    // ---------------
    // actions
    
    // fix of issue 42082
    private void updatePasteAction() {
        if(java.awt.EventQueue.isDispatchThread()) {
            updatePasteActionInAwtThread();
        } else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updatePasteActionInAwtThread();
                }
            });
        }
    }

    private void updatePasteActionInAwtThread() {
        Node[] selected = getExplorerManager().getSelectedNodes();
        if (selected != null && selected.length >= 1) {
            // pasting considered only on the first selected node
            Clipboard clipboard = getClipboard();
            Transferable trans = clipboard.getContents(this); // [this??]
            if (trans != null) {
                Node node = selected[0];
                PasteType[] pasteTypes = node.getPasteTypes(trans);
                if (pasteTypes.length != 0) {
                    // transfer accepted by the node, we are done
                    pasteAction.setPasteTypes(pasteTypes);
                    return;
                }

                boolean multiFlavor = false;
                try {
                    multiFlavor = trans.isDataFlavorSupported(
                                    ExTransferable.multiFlavor);
                }
                catch (Exception e) {} // ignore, should not happen

                if (multiFlavor) {
                    // The node did not accept whole multitransfer as is - try
                    // to break it into individual transfers and paste them in
                    // sequence instead.
                    try {
                        MultiTransferObject mto = (MultiTransferObject)
                            trans.getTransferData(ExTransferable.multiFlavor);

                        int n = mto.getCount(), i;
                        Transferable[] t = new Transferable[n];
                        PasteType[] p = new PasteType[n];

                        for (i=0; i < n; i++) {
                            t[i] = mto.getTransferableAt(i);
                            pasteTypes = node.getPasteTypes(t[i]);
                            if (pasteTypes.length == 0)
                                break;

                            p[i] = pasteTypes[0]; // ??
                        }

                        if (i == n) { // all individual transfers accepted
                            pasteAction.setPasteTypes(
                                new PasteType[] { new MultiPasteType(t, p) });
                            return;
                        }
                    }
                    catch (Exception ex) {
                        org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        }

        pasteAction.setPasteTypes(null);
    }

    private Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(java.awt.datatransfer.Clipboard.class);
        if (c == null)
            c = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        return c;
    }

    @Override
    public void requestFocus() {
        treeView.requestFocus();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow();
    }

    // ---------------
    // innerclasses

    private static class ExplorerManagerLookup extends ProxyLookup {
        void setLookupFromExplorerManager(ExplorerManager manager, ActionMap actionMap) {
            setLookups(manager != null ? ExplorerUtils.createLookup(manager, actionMap) : Lookup.EMPTY);
        }
    }

    // listener on nodes selection (ExplorerManager)
    private class NodeSelectionListener implements PropertyChangeListener,
                                                   ActionListener, Runnable
    {
        private javax.swing.Timer timer;

        NodeSelectionListener() {
            timer = new javax.swing.Timer(150, this);
            timer.setCoalesce(true);
            timer.setRepeats(false);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())
                    && formDesigner != null && formDesigner.getFormModel() != null) {
                // refresh nodes' lookup with current set of cookies
                for (Node node : getExplorerManager().getSelectedNodes()) {
                    ((FormNode)node).updateCookies();
                }
                // restart waiting for expensive part of the update
                timer.restart();
            }
        }

        @Override
        public void actionPerformed(ActionEvent evt) { // invoked by Timer
            java.awt.EventQueue.invokeLater(this); // replan to EventQueue thread
        }

        /** Updates activated nodes and actions. It is executed via timer 
         * restarted each time a new selection change appears - if they come
         * quickly e.g. due to the user is holding a cursor key, this
         * (relatively time expensive update) is done only at the end.
         */
        @Override
        public void run() {
            updatePasteAction();
            timer.stop();
        }
    }

    // listener on clipboard changes
    private class ClipboardChangesListener implements ClipboardListener {
        @Override
        public void clipboardChanged(ClipboardEvent ev) {
            if (!ev.isConsumed())
                updatePasteAction();
        }
    }

    // performer for DeleteAction
    private class DeleteActionPerformer extends javax.swing.AbstractAction
                                        implements ActionPerformer, Mutex.Action<Object>
    {
        private Node[] nodesToDestroy;

        @Override
        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        @Override
        public void performAction(SystemAction action) {
            Node[] selected = getSelectedRootNodes();

            if (selected == null || selected.length == 0)
                return;

            for (int i=0; i < selected.length; i++)
                if (!selected[i].canDestroy())
                    return;

            try { // clear nodes selection first
                getExplorerManager().setSelectedNodes(new Node[0]);
            }
            catch (PropertyVetoException e) {} // cannot be vetoed

            nodesToDestroy = selected;
            if (java.awt.EventQueue.isDispatchThread())
                doDelete();
            else // reinvoke synchronously in AWT thread
                Mutex.EVENT.readAccess(this);
        }

        @Override
        public Object run() {
            doDelete();
            return null;
        }

        private void doDelete() {
            if (nodesToDestroy != null) {
                for (int i=0; i < nodesToDestroy.length; i++) {
                    try {
                        nodesToDestroy[i].destroy();
                    }
                    catch (java.io.IOException ex) { // should not happen
                        ex.printStackTrace();
                    }
                }
                nodesToDestroy = null;
            }
        }
    }
    
    // performer for CopyAction and CutAction
    private class CopyCutActionPerformer extends javax.swing.AbstractAction
                                         implements ActionPerformer
    {
        private boolean copy;

        public CopyCutActionPerformer(boolean copy) {
            this.copy = copy;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        @Override
        public void performAction(SystemAction action) {
            Transferable trans;
            Node[] selected = getSelectedRootNodes();

            if (selected == null || selected.length == 0)
                trans = null;
            else if (selected.length == 1)
                trans = getTransferableOwner(selected[0]);
            else {
                Transferable[] transArray = new Transferable[selected.length];
                for (int i=0; i < selected.length; i++)
                    if ((transArray[i] = getTransferableOwner(selected[i]))
                                                                     == null)
                        return;

                trans = new ExTransferable.Multi(transArray);
            }

            if (trans != null) {
                Clipboard clipboard = getClipboard();
                clipboard.setContents(trans, new StringSelection("")); // NOI18N
            }
        }

        private Transferable getTransferableOwner(Node node) {
            try {
                return copy ? node.clipboardCopy() : node.clipboardCut();
            }
            catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return null;
            }
        }
    }

    // paste type used for ExTransferable.Multi
    private static class MultiPasteType extends PasteType
                                 implements Mutex.ExceptionAction<Transferable>
    {
        private Transferable[] transIn;
        private PasteType[] pasteTypes;

        MultiPasteType(Transferable[] t, PasteType[] p) {
            transIn = t;
            pasteTypes = p;
        }

        // performs the paste action
        @Override
        public Transferable paste() throws java.io.IOException {
            if (java.awt.EventQueue.isDispatchThread())
                return doPaste();
            else { // reinvoke synchronously in AWT thread
                try {
                    return Mutex.EVENT.readAccess(this);
                }
                catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof java.io.IOException)
                        throw (java.io.IOException) e;
                    else { // should not happen, ignore
                        e.printStackTrace();
                        return ExTransferable.EMPTY;
                    }
                }
            }
        }

        @Override
        public Transferable run() throws Exception {
            return doPaste();
        }

        private Transferable doPaste() throws java.io.IOException {
            Transferable[] transOut = new Transferable[transIn.length];
            for (int i=0; i < pasteTypes.length; i++) {
                Transferable newTrans = pasteTypes[i].paste();
                transOut[i] = newTrans != null ? newTrans : transIn[i];
            }
            return new ExTransferable.Multi(transOut);
        }
    }

    // -----------

//    // node for empty ComponentInspector
//    private static class EmptyInspectorNode extends AbstractNode {
//        public EmptyInspectorNode() {
//            super(Children.LEAF);
//            setIconBaseWithExtension(EMPTY_INSPECTOR_ICON_BASE);
//        }
//        @Override
//        public boolean canRename() {
//            return false;
//        }
//    }

    public static final class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 7424646018839457544L;
        public Object readResolve() {
            return new TopComponent() {
                @Override
                protected void componentOpened() {
                    close();
                }
                @Override
                public int getPersistenceType() {
                    return TopComponent.PERSISTENCE_NEVER;
                }
            };
        }
    }
}
