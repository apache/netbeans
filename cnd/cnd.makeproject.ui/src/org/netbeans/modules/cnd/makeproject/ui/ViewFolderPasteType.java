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

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;

/**
 *
 */
final class ViewFolderPasteType  extends PasteType {

    private static final RequestProcessor RP = new RequestProcessor("ViewFolderPasteType", 1); //NOI18N
    private final Folder toFolder;
    //private final LogicalFolderNode viewFolderNode;
    private final Folder fromFolder;
    private final int type;
    private final MakeLogicalViewProvider provider;

    public ViewFolderPasteType(Folder toFolder, LogicalFolderNode viewFolderNode, int type, MakeLogicalViewProvider provider) {
        this.toFolder = toFolder;
        //this.viewFolderNode = viewFolderNode;
        fromFolder = viewFolderNode.getFolder();
        this.type = type;
        this.provider = provider;
    }

    private void copyFolderConfigurations(FolderConfiguration[] newConfigurations, FolderConfiguration[] oldConfigurations) {
        // Only allowing copying configurations within same project
        if (newConfigurations == null || oldConfigurations == null) {
            return;
        }
        assert newConfigurations.length == oldConfigurations.length;
        for (int i = 0; i < newConfigurations.length; i++) {
            newConfigurations[i].assignValues(oldConfigurations[i]);
        }
    }

    private void copyItemConfigurations(ItemConfiguration[] newConfigurations, ItemConfiguration[] oldConfigurations) {
        // Only allowing copying configurations within same project
        if (newConfigurations == null || oldConfigurations == null) {
            return;
        }        
        assert newConfigurations.length == oldConfigurations.length;
        if (newConfigurations.length == 0 || oldConfigurations.length == 0) {
            return;
        }
        for (int i = 0; i < newConfigurations.length; i++) {
            if (oldConfigurations[i] != null && newConfigurations[i] != null) {
                newConfigurations[i].assignValues(oldConfigurations[i]);
            }
        }
    }
    
    private FileObject getFolderFileObject(Folder folder) {
        String rootPath = folder.getRootPath();
        return RemoteFileUtil.getFileObject(folder.getConfigurationDescriptor().getBaseDirFileObject(), rootPath);
    }

    @Override
    public Transferable paste() throws IOException {
        RP.post(() -> {
            try {
                pasteImpl();
                provider.getMakeConfigurationDescriptor().save();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        return null;
    }
    
    private void pasteImpl() throws IOException {
        if (!provider.gotMakeConfigurationDescriptor() || !(provider.getMakeConfigurationDescriptor().okToChange())) {
            return;
        }
        FileObject itemFO = getFolderFileObject(fromFolder);
        if (type == DnDConstants.ACTION_MOVE) {
            // Drag&Drop, Cut&Paste
            // Move within same project
            if (toFolder.isDiskFolder() && fromFolder.isDiskFolder()) {
                if (itemFO.isValid()) {
                    String toFolderPath = CndPathUtilities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDirFileObject(), toFolder.getRootPath());
                    FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileObject().getFileSystem(), toFolderPath); // should it be normalized?
                    if (toFolderFO == null || !toFolderFO.isValid()) {
                        return;
                    }
                    String newName = CndPathUtilities.createUniqueFileName(toFolderFO, itemFO.getNameExt(), ""); // NOI18N
                    final FileLock lock = itemFO.lock();
                    try {
                        FileObject movedFileFO = itemFO.move(lock, toFolderFO, newName, ""); // NOI18N
                        Folder movedFolder = toFolder.findFolderByName(movedFileFO.getNameExt());
                        if (toFolder.getProject() == fromFolder.getProject()) {
                            if (!fromFolder.getAllFolders(true).contains(toFolder)) {
                                if (movedFolder != null) {
                                    recussiveMoveConfigurations(fromFolder, toFolder, movedFolder.getName(), true);
                                }
                            }
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            } else if (!toFolder.isDiskFolder() && !fromFolder.isDiskFolder()) {
                if (!fromFolder.getAllFolders(true).contains(toFolder)) {
                    recussiveMove(fromFolder, toFolder, true);
                }
            }
        } else if (type == DnDConstants.ACTION_COPY || type == DnDConstants.ACTION_NONE) {
            // Copy&Paste
            if (toFolder.isDiskFolder() && fromFolder.isDiskFolder()) {
                if (itemFO.isValid()) {
                    String toFolderPath = CndPathUtilities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDirFileObject(), toFolder.getRootPath());
                    FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileSystem(), toFolderPath); // should it be normalized?
                    String newName = CndPathUtilities.createUniqueFileName(toFolderFO, itemFO.getNameExt(), "");
                    FileObject copiedFileObject = itemFO.copy(toFolderFO, newName, "");

                    Folder copiedFolder = toFolder.findFolderByName(copiedFileObject.getNameExt());
                    if (toFolder.getProject() == fromFolder.getProject()) {
                        if (!fromFolder.getAllFolders(true).contains(toFolder)) {
                            if (copiedFolder != null) {
                                recussiveMoveConfigurations(fromFolder, toFolder, copiedFolder.getName(), false);
                            }
                        }
                    }
                }
            } else if (!toFolder.isDiskFolder() && !fromFolder.isDiskFolder()) {
                if (!fromFolder.getAllFolders(true).contains(toFolder)) {
                    recussiveMove(fromFolder, toFolder, false);
                }
            }
        }
    }

    private void recussiveMoveConfigurations(Folder folder, Folder toFolder, String newName, boolean move) throws IOException {
        Folder target = toFolder.findFolderByName(newName);
        if (target != null) {
            if (target.getProject() == folder.getProject()) {
                copyFolderConfigurations(target.getFolderConfigurations(), folder.getFolderConfigurations());
            }
            for (Folder sub : folder.getFolders()) {
                recussiveMoveConfigurations(sub, target, sub.getName(), move);
            }
            for (Item item : folder.getItemsAsArray()) {
                Item toItem = target.findItemByName(item.getName());
                if (toItem != null) {
                    copyItemConfigurations(toItem.getItemConfigurations(), item.getItemConfigurations());
                }
            }
        }
        if (move) {
            Folder parent = folder.getParent();
            parent.removeFolderAction(folder);
        }
    }

    private void recussiveMove(Folder folder, Folder toFolder, boolean move) throws IOException {
        Folder parent = folder.getParent();
        Folder target = toFolder.findFolderByDisplayName(folder.getDisplayName());
        if (target == null) {
            target = new Folder(toFolder.getConfigurationDescriptor(), toFolder, folder.getName(), folder.getDisplayName(), true, Folder.Kind.SOURCE_LOGICAL_FOLDER);
            target = toFolder.addFolder(target, true);
            if (target.getProject() == folder.getProject()) {
                copyFolderConfigurations(target.getFolderConfigurations(), folder.getFolderConfigurations());
            }
        }
        for (Folder sub : folder.getFolders()) {
            recussiveMove(sub, target, move);
        }
        for (Item item : folder.getItemsAsArray()) {
            ViewItemPasteType viewItemPasteType = new ViewItemPasteType(target ,folder,  item, type, provider);
            viewItemPasteType.pasteImpl();
        }
        if (move) {
            parent.removeFolderAction(folder);
        }
    }
}
