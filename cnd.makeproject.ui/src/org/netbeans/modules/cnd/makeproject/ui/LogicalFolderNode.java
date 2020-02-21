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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.ui.actions.AddExistingFolderItemsAction;
import org.netbeans.modules.cnd.makeproject.ui.actions.DebugTestAction;
import org.netbeans.modules.cnd.makeproject.ui.actions.NewTestActionFactory;
import org.netbeans.modules.cnd.makeproject.ui.actions.RunTestAction;
import org.netbeans.modules.cnd.makeproject.ui.actions.StepIntoTestAction;
import org.netbeans.modules.cnd.makeproject.api.ui.actions.AddExistingItemAction;
import org.netbeans.modules.cnd.makeproject.api.ui.actions.NewFolderAction;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.NodeActionFactory.RenameNodeAction;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.FileSystemAction;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 */
final class LogicalFolderNode extends AnnotatedNode implements ChangeListener {

    private static final MessageFormat FOLDER_VIEW_FLAVOR = new MessageFormat("application/x-org-netbeans-modules-cnd-makeproject-uidnd-folder; class=org.netbeans.modules.cnd.makeproject.ui.LogicalFolderNode; mask={0}"); // NOI18N
    private final Folder folder;
    private final MakeLogicalViewProvider provider;
    private final String pathPostfix;
    private RequestProcessor.Task updateTask;

    public LogicalFolderNode(Node folderNode, Folder folder, MakeLogicalViewProvider provider) {
        super(new LogicalViewChildren(folder, provider), createLFNLookup(folderNode, folder, provider), provider.getAnnotationRP());
        this.folder = folder;
        this.provider = provider;
        String postfix = "";
        if (folder != null && folder.getRoot() != null) {
            String absPath = folder.getAbsolutePath();
//            String AbsRootPath = CndPathUtilities.toAbsolutePath(provider.getMakeConfigurationDescriptor().getBaseDir(), folder.getRoot());
//            AbsRootPath = RemoteFileUtil.normalizeAbsolutePath(AbsRootPath, provider.getProject());
//            FileObject folderFile = RemoteFileUtil.getFileObject(AbsRootPath, provider.getProject());
            if (absPath != null) {
                postfix = " - " + absPath; // NOI18N
            }
        }
        pathPostfix = postfix;
        setForceAnnotation(true);
        updateAnnotationFiles();
    }

    private static Lookup createLFNLookup(Node folderNode, Folder folder, MakeLogicalViewProvider provider) {
        List<Object> elems = new ArrayList<>(3);
        elems.add(folder);
        elems.add(new FolderSearchInfo(folder));

        //No need to have project in lookup for physical folders, see bug 229005
        if (!folder.isDiskFolder()) {
            elems.add(provider.getProject());
        } else {
            MakeConfigurationDescriptor conf = folder.getConfigurationDescriptor();
            if (conf != null) {
                String rootPath = folder.getRootPath();
                FileObject fo = RemoteFileUtil.getFileObject(conf.getBaseDirFileObject(), rootPath);
                if (fo != null /*paranoia*/ && fo.isValid() && fo.isFolder()) {
                    try {
                        DataFolder dataFolder = DataFolder.findFolder(fo);
                        if (dataFolder != null) {
                            elems.add(dataFolder);
                        }
                    } catch (IllegalArgumentException ex) {
                        // do nothing
                    }
                    File file = FileUtil.toFile(fo);
                    if (file != null) {
                        elems.add(file);
                    }
                }
            }
        }
        return Lookups.fixed(elems.toArray());
    }

    private void updateAnnotationFiles() {
        if (updateTask == null) {
            updateTask = provider.getAnnotationRP().create(new FileAnnotationUpdater(this));
        }
        updateTask.schedule(BaseMakeViewChildren.WAIT_DELAY); // batch by 50 ms
    }

    private final class FileAnnotationUpdater implements Runnable {

        private final LogicalFolderNode logicalFolderNode;

        FileAnnotationUpdater(LogicalFolderNode logicalFolderNode) {
            this.logicalFolderNode = logicalFolderNode;
        }

        @Override
        public void run() {
            Set<FileObject> newSet = Collections.<FileObject>emptySet(); /*Collections.EMPTY_SET*/ /*folder.getAllItemsAsFileObjectSet(true)*/ // See IZ 100394 for details
            if (folder.isDiskFolder()) {
                MakeConfigurationDescriptor conf = folder.getConfigurationDescriptor();
                if (conf != null) {
                    String rootPath = folder.getRootPath();
                    FileObject fo = RemoteFileUtil.getFileObject(conf.getBaseDirFileObject(), rootPath);
                    if (fo != null /*paranoia*/ && fo.isValid() && fo.isFolder()) {
                        newSet = Collections.<FileObject>singleton(fo);
                    }
                }
            }
            setFiles(newSet);
            List<Folder> allFolders = new ArrayList<>();
            allFolders.add(folder);
            allFolders.addAll(folder.getAllFolders(true));
            Iterator<Folder> iter = allFolders.iterator();
            while (iter.hasNext()) {
                iter.next().addChangeListener(logicalFolderNode);
            }
            EventQueue.invokeLater(new VisualUpdater()); // IZ 151257
        }
    }

    private final class VisualUpdater implements Runnable {

        @Override
        public void run() {
            fireIconChange();
            fireOpenedIconChange();
        }
    }
    /*
     * Something in the folder has changed
     **/

    @Override
    public void stateChanged(ChangeEvent e) {
        updateAnnotationFiles();
    }

    public Folder getFolder() {
        return folder;
    }

    @Override
    public Object getValue(String valstring) {
        if (valstring == null) {
            return super.getValue(null);
        }
        if (valstring.equals("Folder")) // NOI18N
        {
            return folder;
        } else if (valstring.equals("Project")) // NOI18N
        {
            return provider.getProject();
        } else if (valstring.equals("This")) // NOI18N
        {
            return this;
        } else if (valstring.equals("slowRename")) // NOI18N
        {
            return null;
        }
        return super.getValue(valstring);
    }

    @Override
    public Image getIcon(int type) {
        Image image;
        if (folder.isTest()) {
            image = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testContainer.gif"); // NOI18N
        } else if (folder.isTestRootFolder()) {
            image = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testFolder.gif"); // NOI18N
        } else if (folder.isDiskFolder() && folder.isTestLogicalFolder()) {
            image = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testFolder.gif"); // NOI18N
        } else if (folder.isDiskFolder()) {
            image = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/tree_folder.gif"); // NOI18N
        } else {
            image = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/logicalFilesFolder.gif"); // NOI18N
        }
        if (folder.isProjectFiles() && folder.isRemoved()) {
            image = ImageUtilities.mergeImages(image, MakeLogicalViewProvider.brokenFolderBadge, 11, 0);
        }
        image = annotateIcon(image, type);
        return image;
    }

    @Override
    public Image getOpenedIcon(int type) {
        Image image;
        if (folder.isTest()) {
            image = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testContainer.gif"); // NOI18N
        } else if (folder.isTestRootFolder()) {
            image = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testFolderOpened.gif"); // NOI18N
        } else if (folder.isDiskFolder() && folder.isTestLogicalFolder()) {
            image = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testFolder.gif"); // NOI18N
        } else if (folder.isDiskFolder()) {
            image = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/tree_folder.gif"); // NOI18N
        } else {
            image = ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/logicalFilesFolderOpened.gif"); // NOI18N
        }
        if (folder.isProjectFiles() && folder.isRemoved()) {
            image = ImageUtilities.mergeImages(image, MakeLogicalViewProvider.brokenFolderBadge, 11, 0);
        }
        image = annotateIcon(image, type);
        return image;
    }

    @Override
    public String getName() {
        return folder.getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return annotateName(folder.getDisplayName() + pathPostfix);
    }

    @Override
    public void setName(final String newName) {
        provider.getAnnotationRP().post(() -> {
            setNameImpl(newName);
        });
    }

    public void setNameImpl(String newName) {
        String oldName = folder.getDisplayName();
        if (folder.isDiskFolder()) {
            String rootPath = folder.getRootPath();
            FileObject fo;
//            if (CndFileUtils.isLocalFileSystem(folder.getConfigurationDescriptor().getBaseDirFileSystem())) {
//                String AbsRootPath = CndPathUtilities.toAbsolutePath(folder.getConfigurationDescriptor().getBaseDir(), rootPath);
//                fo = CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(AbsRootPath));
//            } else {
                // looks like line below is OK for all cases
                fo = RemoteFileUtil.getFileObject(folder.getConfigurationDescriptor().getBaseDirFileObject(), rootPath);
//            }
            if (fo == null /*paranoia*/ || !fo.isValid() || !fo.isFolder()) {
                return;
            }
            FileLock lock = null;
            try {
                lock = fo.lock();
                fo.rename(lock, newName, null);
            } catch (IOException ioe) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ioe.getMessage()));
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
            return;
        }
        if (folder.getParent() != null && folder.getParent().findFolderByDisplayName(newName) != null) {
            String msg = NbBundle.getMessage(MakeLogicalViewProvider.class, "CANNOT_RENAME", oldName, newName); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            return;
        }
        folder.setDisplayName(newName);
        fireDisplayNameChange(oldName, newName);
    }

//        @Override
//        public void setDisplayName(String newName) {
//            setDisplayName(newName);
//        }
    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return true;//getFolder().isDiskFolder();
    }

    @Override
    public boolean canCut() {
        return true;//getFolder().isDiskFolder();
    }

    @Override
    public boolean canCopy() {
        return true;//getFolder().isDiskFolder();
    }
    @Override
    public Transferable clipboardCopy() throws IOException {
        return addViewFolderTransferable(super.clipboardCopy(), DnDConstants.ACTION_COPY);
    }

    @Override
    public Transferable clipboardCut() throws IOException {
        return addViewFolderTransferable(super.clipboardCut(), DnDConstants.ACTION_MOVE);
    }

    @Override
    public Transferable drag() throws IOException {
        return addViewFolderTransferable(super.drag(), DnDConstants.ACTION_NONE);
    }

    @Override
    public void destroy() throws IOException {
        provider.getAnnotationRP().post(() -> {
            try {
                destroyImpl();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    public void destroyImpl() throws IOException {
        final Folder aFolder = getFolder();
        if (!aFolder.isDiskFolder()) {
            Folder parent = aFolder.getParent();
            if (parent != null && provider != null) { // provider != null is probably a paranoia
                Project project = provider.getProject();
                if (project != null) {                    
                    ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                    if (pdp != null) {
                        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
                        if (makeConfigurationDescriptor.okToChange()) {
                            parent.removeFolderAction(folder);
                            makeConfigurationDescriptor.save();
                        }
                    }                    
                }
            }            
            return;
        }
        String absPath = CndPathUtilities.toAbsolutePath(aFolder.getConfigurationDescriptor().getBaseDirFileObject(), aFolder.getRootPath());
        FileObject folderFileObject = CndFileUtils.toFileObject(aFolder.getConfigurationDescriptor().getBaseDirFileSystem(), absPath);
        if (folderFileObject == null /*paranoia*/ || !folderFileObject.isValid() || !folderFileObject.isFolder()) {
            return;
        }
        folderFileObject.delete();
        Folder parent = aFolder.getParent();
        if (parent != null) {
            parent.removeFolderAction(aFolder);
        }
        super.destroy();
    }

    @Override
    public PasteType getDropType(Transferable transferable, int action, int index) {
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].getSubType().equals(MakeLogicalViewProvider.SUBTYPE)) {
                return super.getDropType(transferable, action, index);
            } else if (flavors[i].getSubType().equals(MakeLogicalViewProvider.SUBTYPE_FOLDER)) {
                return super.getDropType(transferable, action, index);
            }
        }
        return null;
    }

    @Override
    protected void createPasteTypes(Transferable transferable, List<PasteType> list) {
        if (folder.isTestLogicalFolder()) {
            // Don't drop items into a regular test folder (IZ 185173)
            return;
        }
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].getSubType().equals(MakeLogicalViewProvider.SUBTYPE)) {
                try {
                    ViewItemNode viewItemNode = (ViewItemNode) transferable.getTransferData(flavors[i]);
                    int type = Integer.parseInt(flavors[i].getParameter(MakeLogicalViewProvider.MASK));
                    list.add(new ViewItemPasteType(this.getFolder(), viewItemNode, type, provider));
                } catch (Exception e) {
                }
            } else if (flavors[i].getSubType().equals(MakeLogicalViewProvider.SUBTYPE_FOLDER)) {
                try {
                    LogicalFolderNode viewFolderNode = (LogicalFolderNode) transferable.getTransferData(flavors[i]);
                    if (viewFolderNode != this) {
                        int type = Integer.parseInt(flavors[i].getParameter(MakeLogicalViewProvider.MASK));
                        list.add(new ViewFolderPasteType(folder, viewFolderNode, type, provider));
                    }
                } catch (Exception e) {
                }
            }
        }
        super.createPasteTypes(transferable, list);
    }

    private ExTransferable addViewFolderTransferable(Transferable t, int operation) {
        try {
            ExTransferable extT = ExTransferable.create(t);
            ViewFolderTransferable viewItem = new ViewFolderTransferable(this, operation);
            extT.put(viewItem);
            return extT;
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public void newLogicalFolder() {
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] result;
        ResourceBundle bundle = NbBundle.getBundle(MakeLogicalViewProvider.class);
        if (folder.isTestRootFolder()) {
            result = new Action[]{ //
                        null,
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null),
                        null,
                        NewTestActionFactory.emptyTestFolderAction(),
                        SystemAction.get(NewFolderAction.class),
                        SystemAction.get(org.openide.actions.FindAction.class),
                        null,
                        SystemAction.get(PropertiesFolderAction.class),};
            result = NodeActionFactory.insertAfter(NewTestActionFactory.getTestCreationActions(folder.getProject()), result);
        } else if (folder.isTestLogicalFolder() && !folder.isDiskFolder()) {
            result = new Action[]{ //
                        null,
                        NewTestActionFactory.emptyTestFolderAction(),
                        SystemAction.get(NewFolderAction.class),
                        SystemAction.get(org.openide.actions.FindAction.class),
                        null,
                        SystemAction.get(RemoveFolderAction.class),
                        NodeActionFactory.createRenameAction(),
                        null,
                        SystemAction.get(PropertiesFolderAction.class),};
            result = NodeActionFactory.insertAfter(NewTestActionFactory.getTestCreationActions(folder.getProject()), result);
        } else if (folder.isTest()) {
            result = new Action[]{ //
                        CommonProjectActions.newFileAction(), //
                        SystemAction.get(AddExistingItemAction.class),
                        SystemAction.get(org.openide.actions.FindAction.class), //
                        null,
                        SystemAction.get(RunTestAction.class),
                        SystemAction.get(DebugTestAction.class),
                        SystemAction.get(StepIntoTestAction.class),
                        null,
                        SystemAction.get(RemoveFolderAction.class),
                        NodeActionFactory.createRenameAction(),
                        null,
                        SystemAction.get(PropertiesFolderAction.class),};
        } else if (folder.isDiskFolder()) {
            if (folder.isRemoved()) {
                result = new Action[]{
                            CommonProjectActions.newFileAction(),
                            null,
                            SystemAction.get(RemoveFolderAction.class),
                            null,
                            SystemAction.get(FileSystemAction.class),
                            null,
                            SystemAction.get(PropertiesFolderAction.class),};
            } else {
                result = new Action[]{
                            CommonProjectActions.newFileAction(),
                            SystemAction.get(org.openide.actions.FindAction.class),
                            null,
                            SystemAction.get(CutAction.class),
                            SystemAction.get(CopyAction.class),
                            SystemAction.get(PasteAction.class),
                            null,
                            //                        new RefreshItemAction((LogicalViewChildren) getChildren(), folder, null),
                            //                        null,
                            SystemAction.get(DeleteAction.class),
                            NodeActionFactory.createRenameAction(),
                            null,
                            SystemAction.get(FileSystemAction.class),
                            null,
                            SystemAction.get(PropertiesFolderAction.class),};
            }
        } else {
            result = new Action[]{
                        CommonProjectActions.newFileAction(),
                        SystemAction.get(NewFolderAction.class),
                        SystemAction.get(AddExistingItemAction.class),
                        SystemAction.get(AddExistingFolderItemsAction.class),
                        SystemAction.get(org.openide.actions.FindAction.class),
                        null,
                        //                        new RefreshItemAction((LogicalViewChildren) getChildren(), folder, null),
                        //                        null,
                        SystemAction.get(CutAction.class),
                        SystemAction.get(CopyAction.class),
                        SystemAction.get(PasteAction.class),
                        null,
                        SystemAction.get(RemoveFolderAction.class),
                        //                SystemAction.get(RenameAction.class),
                        NodeActionFactory.createRenameAction(),
                        null,
                        SystemAction.get(PropertiesFolderAction.class),};
        }
        // makeproject sensitive actions
        final MakeProjectType projectKind = provider.getProject().getLookup().lookup(MakeProjectType.class);
        final List<? extends Action> actionsForMakeProject = Utilities.actionsForPath(projectKind.folderActionsPath());
        result = NodeActionFactory.insertAfter(result, actionsForMakeProject.toArray(new Action[actionsForMakeProject.size()]), RenameNodeAction.class);
        result = NodeActionFactory.insertSyncActions(result, RenameNodeAction.class);
        return result;
    }

    private static final class ViewFolderTransferable extends ExTransferable.Single {

        private final LogicalFolderNode node;

        public ViewFolderTransferable(LogicalFolderNode node, int operation) throws ClassNotFoundException {
            super(new DataFlavor(FOLDER_VIEW_FLAVOR.format(new Object[]{operation}), null, MakeLogicalViewProvider.class.getClassLoader()));
            this.node = node;
        }

        @Override
        protected Object getData() throws IOException, UnsupportedFlavorException {
            return this.node;
        }
    }
}
