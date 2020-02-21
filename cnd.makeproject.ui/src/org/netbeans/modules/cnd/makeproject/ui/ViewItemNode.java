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
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.CodeAssistance;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.ui.actions.CreateProjectAction;
import org.netbeans.modules.cnd.makeproject.ui.actions.DebugDialogAction;
import org.netbeans.modules.cnd.makeproject.ui.actions.NewTestActionFactory;
import org.netbeans.modules.cnd.makeproject.ui.actions.RunDialogAction;
import org.netbeans.modules.cnd.makeproject.ui.actions.RunDialogAction.SimpleRunActionProxy;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.openide.actions.PasteAction;
import org.openide.actions.RenameAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 */
final class ViewItemNode extends FilterNode implements ChangeListener {

    private static final RequestProcessor RP = new RequestProcessor("ViewItemNode", 1); //NOI18N
    private static CodeAssistance cachedCA;

    private static final MessageFormat ITEM_VIEW_FLAVOR = new MessageFormat("application/x-org-netbeans-modules-cnd-makeproject-uidnd; class=org.netbeans.modules.cnd.makeproject.ui.ViewItemNode; mask={0}"); // NOI18N

    private RefreshableItemsContainer childrenKeys;
    private Folder folder;
    private Item item;
    private volatile boolean itemIsExcludedCache = false;
    private final Project project;
    private final ProjectNodesRefreshSupport.ProjectNodeRefreshListener refreshListener;
    private final boolean simpleRunDebug;
    private Action runAction;
    private final VisualUpdater visualUpdater = new VisualUpdater();

    public ViewItemNode(RefreshableItemsContainer childrenKeys, Folder folder, Item item, DataObject dataObject, Project project, boolean simpleRunDebug) {
        super(dataObject.getNodeDelegate());//, null, Lookups.fixed(item));
        this.childrenKeys = childrenKeys;
        this.folder = folder;
        this.item = item;
        setShortDescription(item.getNormalizedPath());
        this.project = project;
        this.refreshListener = (Project project1) -> {
            if (getParentNode() == null) {
                return;
            }
            if (project1 == ViewItemNode.this.project) {
                visualUpdater.postIfNeed();
            }
        };
        ProjectNodesRefreshSupport.addProjectNodeRefreshListener(WeakListeners.create(
                ProjectNodesRefreshSupport.ProjectNodeRefreshListener.class, refreshListener, ProjectNodesRefreshSupport.class));

        this.simpleRunDebug = simpleRunDebug;
        CodeAssistance CAProvider = getCodeAssistance();
        if (CAProvider != null) {
            CAProvider.addChangeListener(this);
        }
        itemIsExcludedCache = isExcluded();
    }

    public ViewItemNode(RefreshableItemsContainer childrenKeys, Folder folder, Item item, DataObject dataObject, Project project) {
        this(childrenKeys, folder, item, dataObject, project, false);
    }

    @Override
    public void setName(final String s) {
        RP.post(() -> {
            ViewItemNode.super.setName(s.trim()); // IZ #152560
        });
    }

    static RequestProcessor getRP(){
        return RP;
    }

    public Folder getFolder() {
        return folder;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public boolean canCut() {
        return true;
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        return addViewItemTransferable(super.clipboardCopy(), DnDConstants.ACTION_COPY);
    }

    @Override
    public Transferable clipboardCut() throws IOException {
        return addViewItemTransferable(super.clipboardCut(), DnDConstants.ACTION_MOVE);
    }

    @Override
    public Transferable drag() throws IOException {
        return addViewItemTransferable(super.drag(), DnDConstants.ACTION_NONE);
    }

    private ExTransferable addViewItemTransferable(Transferable t, int operation) {
        try {
            ExTransferable extT = ExTransferable.create(t);
            ViewItemTransferable viewItem = new ViewItemTransferable(this, operation);
            extT.put(viewItem);
            return extT;
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
    // The node will be removed when the Item gets notification that the file has been destroyed.
    // No need to do it here.

    @Override
    public void destroy() throws IOException {
        RP.post(() -> {
            try {
                ViewItemNode.super.destroy();
                folder.removeItemAction(item);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    @Override
    public Object getValue(String valstring) {
        if (valstring == null) {
            return super.getValue(null);
        }
        if (valstring.equals("Folder")) // NOI18N
        {
            return getFolder();
        } else if (valstring.equals("Project")) // NOI18N
        {
            return project;
        } else if (valstring.equals("Item")) // NOI18N
        {
            return getItem();
        } else if (valstring.equals("This")) // NOI18N
        {
            return this;
        } else if (valstring.equals("slowRename")) // NOI18N
        {
            return null;
        }
        return super.getValue(valstring);
    }

    private Action getRunAction(Action action) {
        if (action instanceof RunDialogAction && runAction == null) {
            runAction = ((RunDialogAction) action).new SimpleRunActionProxy(project, item.getAbsolutePath());
        }
        return runAction;
    }

    @Override
    public Action getPreferredAction() {
        if (simpleRunDebug && !getItem().getFolder().isDiskFolder() && MIMENames.isBinary(MIMESupport.getBinaryFileMIMEType(getItem().getFileObject()))) {
            return getRunAction(super.getPreferredAction());
        }
        return super.getPreferredAction();
    }

    @Override
    public Action[] getActions(boolean context) {
        // Replace DeleteAction with Remove Action
        // Replace PropertyAction with customizeProjectAction
        Action[] oldActions = super.getActions(false);
        List<Action> newActions = new ArrayList<>();
        if (getItem().getFolder() == null) {
            return oldActions;
        } else if (getItem().getFolder().isDiskFolder()) {
            for (int i = 0; i < oldActions.length; i++) {
                String key = null; // Some actions are now openide.awt.GenericAction. Use key instead
                if (oldActions[i] != null) {
                    key = (String) oldActions[i].getValue("key"); // NOI18N
                }
                if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.OpenAction) {
                    newActions.add(oldActions[i]);
                    newActions.add(null);
//                        newActions.add(new RefreshItemAction(childrenKeys, null, getItem()));
//                        newActions.add(null);
                } else if (oldActions[i] != null && oldActions[i] instanceof PasteAction) {
                    newActions.add(oldActions[i]);
                    newActions.add(FileSensitiveActions.fileCommandAction(ActionProvider.COMMAND_COMPILE_SINGLE, NbBundle.getMessage(getClass(), "CTL_CompileSingleAction"), null));
                } else if (oldActions[i] != null && oldActions[i] instanceof RenameAction) {
                    newActions.add(NodeActionFactory.createRenameAction());
                    NodeActionFactory.addSyncActions(newActions);
                } else if (key != null && key.equals("delete")) { // NOI18N
                    newActions.add(NodeActionFactory.createDeleteAction());
                } else if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.PropertiesAction && getFolder().isProjectFiles()) {
                    newActions.add(SystemAction.get(PropertiesItemAction.class));
                } else if (key != null && ("CndCompileAction".equals(key)||"CndCompileRunAction".equals(key)||"CndCompileDebugAction".equals(key))) { // NOI18N
                    // skip
                } else {
                    newActions.add(oldActions[i]);
                }
            }
            return newActions.toArray(new Action[newActions.size()]);
        } else {
            for (int i = 0; i < oldActions.length; i++) {
                String key = null; // Some actions are now openide.awt.GenericAction. Use key instead
                if (oldActions[i] != null) {
                    key = (String) oldActions[i].getValue("key"); // NOI18N
                }
                if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.OpenAction) {
                    newActions.add(oldActions[i]);
                    newActions.add(null);
//                        newActions.add(new RefreshItemAction(childrenKeys, null, getItem()));
//                        newActions.add(null);
                } else if (oldActions[i] != null && oldActions[i] instanceof PasteAction) {
                    newActions.add(oldActions[i]);
                    newActions.add(FileSensitiveActions.fileCommandAction(ActionProvider.COMMAND_COMPILE_SINGLE, NbBundle.getMessage(getClass(), "CTL_CompileSingleAction"), null));
                    if (!getItem().getFolder().isTest()) {
                        newActions.add(NewTestActionFactory.createNewTestsSubmenu());
                    }
                } else if (oldActions[i] != null && oldActions[i] instanceof RenameAction) {
                    newActions.add(NodeActionFactory.createRenameAction());
                    NodeActionFactory.addSyncActions(newActions);
                } else if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.PropertiesAction && getFolder().isProjectFiles()) {
                    newActions.add(SystemAction.get(PropertiesItemAction.class));
                } else if (key != null && key.equals("delete")) { // NOI18N
                    newActions.add(SystemAction.get(RemoveItemAction.class));
                    newActions.add(NodeActionFactory.createDeleteAction());
                } else if (simpleRunDebug && key != null && key.equals("CndDebugCorefileNodeAction")) { //NOI18N
                    // not need this for binaries added to logical folders
                } else if (simpleRunDebug && oldActions[i] != null && oldActions[i] instanceof CreateProjectAction) {
                    // not need this for binaries added to logical folders
                } else if (simpleRunDebug && oldActions[i] != null && oldActions[i] instanceof RunDialogAction) {
                   newActions.add(getRunAction(oldActions[i]));
                } else if (simpleRunDebug && oldActions[i] != null && oldActions[i] instanceof DebugDialogAction) {
                   newActions.add(((DebugDialogAction) oldActions[i]).new SimpleDebugActionProxy(project, item.getAbsolutePath()));
                } else if (key != null && ("CndCompileAction".equals(key)||"CndCompileRunAction".equals(key)||"CndCompileDebugAction".equals(key))) { // NOI18N
                    // skip
                } else {
                    newActions.add(oldActions[i]);
                }
            }
            return newActions.toArray(new Action[newActions.size()]);
        }
    }

    @Override
    public Image getIcon(int type) {
        Image image = super.getIcon(type);
        if (itemIsExcludedCache && (image instanceof BufferedImage)) {
            image = getGrayImage((BufferedImage)image);
        }
        return image;
    }

    private static final Map<BufferedImage,Image> grayImageCache = new WeakHashMap<>();
    private static Image getGrayImage(BufferedImage image) {
        Image gray = grayImageCache.get(image);
        if (gray == null) {
            ColorSpace gray_space = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            ColorConvertOp convert_to_gray_op = new ColorConvertOp(gray_space, null);
            gray = convert_to_gray_op.filter(image, null);
            grayImageCache.put(image, gray);
        }
        return gray;
    }

    @Override
    public String getHtmlDisplayName() {
        if (itemIsExcludedCache) {
            String baseName = super.getHtmlDisplayName();
            if (baseName != null && baseName.toLowerCase(Locale.getDefault()).contains("color=")) { // NOI18N
                // decorating node already has color, leave it
                return baseName;
            } else {
                // add own "disabled" color
                baseName = baseName != null ? baseName : getDisplayName();
                return "<font color='!textInactiveText'>" + baseName; // NOI18N
            }
        }
        return super.getHtmlDisplayName();
    }

    private boolean isExcluded() {
        CndUtils.assertNonUiThread();
        if (item == null || item.getFolder() == null || item.getFolder().getConfigurationDescriptor() == null || item.getFolder().getConfigurationDescriptor().getConfs() == null) {
            return false;
        }
        MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return false;
        }
        CodeAssistance CAProvider = Lookup.getDefault().lookup(CodeAssistance.class);
        if (CAProvider != null) {
            if (CAProvider.hasCodeAssistance(item)) {
                return false;
            }
        }
        BooleanConfiguration excl = itemConfiguration.getExcluded();
        return excl.getValue();
    }

    private CodeAssistance getCodeAssistance(){
        CodeAssistance res = cachedCA;
        if (res == null) {
            res = Lookup.getDefault().lookup(CodeAssistance.class);
            cachedCA = res;
        }
        return res;
    }

    private class VisualUpdater implements Runnable {
        private final AtomicBoolean finished = new AtomicBoolean(true);

        private VisualUpdater() {
        }

        private void postIfNeed() {
            ViewItemNode.this.itemIsExcludedCache = ViewItemNode.this.isExcluded();
            if (finished.getAndSet(false)) {
                EventQueue.invokeLater(this);
            }
        }

        @Override
        public void run() {
            finished.set(true);
            fireIconChange();
            fireOpenedIconChange();
            String displayName = getDisplayName();
            fireDisplayNameChange(displayName, "");
            fireDisplayNameChange("", displayName);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (source instanceof FileObject) {
            // See CsmCodeAssistanceProvider.fireChanges(CsmFile file)
            if (source.equals(item.getFileObject())) {
                visualUpdater.postIfNeed();
            }
        } else if (source instanceof NativeProject) {
            // See CsmCodeAssistanceProvider.fireChanges(CsmProject project)
            if (source.equals(item.getNativeProject())) {
                visualUpdater.postIfNeed();
            }
        } else {
            visualUpdater.postIfNeed();
        }
    }

    private static final class ViewItemTransferable extends ExTransferable.Single {

        private final ViewItemNode node;

        public ViewItemTransferable(ViewItemNode node, int operation) throws ClassNotFoundException {
            super(new DataFlavor(ITEM_VIEW_FLAVOR.format(new Object[]{operation}), null, MakeLogicalViewProvider.class.getClassLoader()));
            this.node = node;
        }

        @Override
        protected Object getData() throws IOException, UnsupportedFlavorException {
            return this.node;
        }
    }

}
