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
package org.netbeans.modules.web.jsf.navigation;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.openide.DialogDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@MultiViewElement.Registration(displayName = "#LBL_PageFlow", //NOI18N
        iconBase = "org/netbeans/modules/web/jsf/navigation/resources/JSFConfigIcon.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "jsf.page.flow",
        mimeType = "text/x-jsf+xml",
        position = 300)
public class PageFlowElement extends CloneableEditor implements MultiViewElement, Serializable {

    private static final Logger LOG = Logger.getLogger(PageFlowElement.class.getName());
    //        private transient JScrollPane panel;
    private transient PageFlowView tc;
    private transient JComponent toolbar;
    private static final long serialVersionUID = 5454879177214643L;
    private JSFConfigEditorContext context;
    private DataObject dataObject;

    public PageFlowElement(Lookup lookup) {
        super(lookup.lookup(DataEditorSupport.class));
        this.context = lookup.lookup(JSFConfigEditorContext.class);
        this.dataObject = lookup.lookup(DataObject.class);
        assert context != null;
        init();
    }

    private void init() {
        getTopComponent().setName(context.getFacesConfigFile().getName());
    }

    @Override
    public JComponent getVisualRepresentation() {
        return getTopComponent();
    }

    @Override
    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = getTopComponent().getToolbarRepresentation();
        }
        return toolbar;
    }

    private synchronized PageFlowView getTopComponent() {
        if (tc == null) {
            tc = new PageFlowView(this, context);
        }
        return tc;
    }

    @Override
    public Action[] getActions() {
        Action[] a = getTopComponent().getActions();

        try {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

            if (l == null) {
                l = getClass().getClassLoader();
            }

            Class<? extends SystemAction> c = Class.forName("org.openide.actions.FileSystemAction", true, l).asSubclass(SystemAction.class); // NOI18N
            SystemAction ra = SystemAction.findObject(c, true);

            Action[] a2 = new Action[a.length + 1];
            System.arraycopy(a, 0, a2, 0, a.length);
            a2[a.length] = ra;
            return a2;
        } catch (Exception ex) {
            // ok, we no action like this I guess
        }
        return new Action[]{};
    }

    @Override
    public Lookup getLookup() {
        return getTopComponent().getLookup();
    }

    @Override
    public void componentOpened() {
        getTopComponent().registerListeners();
//            tc.startBackgroundPinAddingProcess();
        LOG.finest("PageFlowEditor componentOpened");
    }

    @NbBundle.Messages("PageFlowElement.lbl.saving.file=Saving file...")
    @Override
    public void componentClosed() {
        final FileObject storageFile = PageFlowView.getStorageFile(context.getFacesConfigFile());

        if (storageFile != null && storageFile.isValid()) {
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    long time = System.currentTimeMillis();
                    getTopComponent().serializeNodeLocations(storageFile);
                    cleanUpScene();
                    LOG.log(Level.FINEST, "PageFlowEditor componentClosed save took: {0} ms", (System.currentTimeMillis() - time));
                }
            }, Bundle.PageFlowElement_lbl_saving_file(), new AtomicBoolean(false), true, 500, 2000);
        } else {
            DialogDescriptor dialog;
            if (storageFile != null) {
                dialog = new DialogDescriptor(
                        NbBundle.getMessage(PageFlowElement.class, "MSG_NoFileToSave", storageFile), //NOI18N
                        NbBundle.getMessage(PageFlowElement.class, "TLE_NoFileToSave")); //NOI18N
            } else {
                dialog = new DialogDescriptor(
                        NbBundle.getMessage(PageFlowElement.class, "MSG_NoProjectToSave"), //NOI18N
                        NbBundle.getMessage(PageFlowElement.class, "TLE_NoFileToSave")); //NOI18N
            }
            dialog.setOptions(new Object[]{DialogDescriptor.OK_OPTION});
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            cleanUpScene();
        }
    }

    private void cleanUpScene() {
        getTopComponent().unregstierListeners();
        PageFlowToolbarUtilities.removePageFlowView(getTopComponent());
        // tc.clearGraph();
        getTopComponent().destroyScene();
        toolbar = null;
        tc = null;
    }

    @Override
    public void componentShowing() {
        // page flow not initialized yet
        if (getTopComponent().getPageFlowController() == null) {
            return;
        }
        getTopComponent().getPageFlowController().flushGraphIfDirty();
    }

    @Override
    public void componentHidden() {
        LOG.finest("PageFlowEditor componentHidden");
    }

    @Override
    public void componentActivated() {
        //tc.requestFocusInWindow();
        LOG.finest("PageFlowView componentActivated");
        getTopComponent().requestActive();
    }

    @Override
    public void componentDeactivated() {
        LOG.finest("PageFlowView Deactivated");
    }
    private MultiViewElementCallback callback;

    public MultiViewElementCallback getMultiViewCallback() {
        return callback;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        context.setMultiViewTopComponent(callback.getTopComponent());
    }

    @Override
    public CloseOperationState canCloseElement() {
        if (!getEditorSupport().isModified()) {
            return CloseOperationState.STATE_OK;
        }

        AbstractAction save = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                //save changes
                try {
                    getEditorSupport().saveDocument();
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "File {0} couldn''t be saved.", context.getFacesConfigFile().getName());
                }
            }
        };
        save.putValue(Action.LONG_DESCRIPTION, NbBundle.getMessage(DataObject.class,
                "MSG_SaveFile", // NOI18N
                getEditorSupport().getDataObject().getPrimaryFile().getNameExt()));

        return MultiViewFactory.createUnsafeCloseState(
                "ID_FACES_CONFIG_CLOSING", //NOI18N
                save,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        getTopComponent().serializeNodeLocations(PageFlowView.getStorageFile(context.getFacesConfigFile()));
        out.writeObject(context);
        LOG.finest("writeObject");
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object object = in.readObject();
        if (!(object instanceof JSFConfigEditorContext)) {
            throw new ClassNotFoundException("JSFConfigEditorContext expected but not found");
        }
        context = (JSFConfigEditorContext) object;
        /* deserialization of node locations is completed in the PageFlowView constructor (in init() ) */
        init();
        LOG.finest("readObject");
    }

    private DataEditorSupport getEditorSupport() {
        return (DataEditorSupport) cloneableEditorSupport();
    }

    @Override
    public UndoRedo getUndoRedo() {
        return context.getUndoRedo();
    }
}
