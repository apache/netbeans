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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.api.utils.CndVisibilityQuery;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item.ItemFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.ui.LogicalViewNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.ui.LogicalViewNodeProviders;
import org.netbeans.modules.cnd.makeproject.api.ui.ItemEx;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

/**
 *
 */
class LogicalViewChildren extends BaseMakeViewChildren implements PropertyChangeListener {

    public LogicalViewChildren(Folder folder, MakeLogicalViewProvider provider) {
        super(folder, provider);
    }

    @Override
    protected void onFolderChange(Folder folder) {
        if (folder != null && folder.isDiskFolder()) {
            MakeProjectOptions.removePropertyChangeListener(this);
            MakeProjectOptions.addPropertyChangeListener(this);
        }
    }
    
    @Override
    protected void removeNotify() {
        super.removeNotify();
        final Folder folder = getFolder();
        if (folder != null && folder.isDiskFolder()) {
            MakeProjectOptions.removePropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (property.equals(MakeProjectOptions.VIEW_BINARY_FILES_EVENT_NAME)) {
            stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    protected Node[] createNodes(Object key) {
        Node node = null;
        if (key instanceof LoadingNode) {
            //System.err.println("LogicalViewChildren: return wait node");
            node = (Node) key;
        } else if (key instanceof Node) {
            node = (Node) key;
        } else if (key instanceof Folder) {
            Folder folder = (Folder) key;
            if (folder.isProjectFiles() || folder.isTestLogicalFolder() || folder.isTest()) {
                //FileObject srcFileObject = project.getProjectDirectory().getFileObject("src");
                FileObject srcFileObject = getProject().getProjectDirectory();
                DataObject srcDataObject = null;
                try {
                    if (srcFileObject.isValid()) {
                        srcDataObject = DataObject.find(srcFileObject);
                    }
                } catch (DataObjectNotFoundException e) {
                    // Do not throw Exception.
                    // It is normal use case when folder can be deleted at build time.
                    //throw new AssertionError(e);
                }
                if (srcDataObject != null) {
                    node = new LogicalFolderNode(((DataFolder) srcDataObject).getNodeDelegate(), folder, provider);
                } else {
                    // Fix me. Create Broken Folder
                    //node = new BrokenViewFolderNode(this, getFolder(), folder);
                }
            } else {
                node = new ExternalFilesNode(folder, provider);
            }
        } else if (key instanceof ItemEx) {
            ItemEx item = (ItemEx) key;
            DataObject fileDO = item.getDataObject();
            if (fileDO != null && fileDO.isValid()) {
                node = new ViewItemNode(this, getFolder(), item, fileDO, provider.getProject());
            } else {
                node = new BrokenViewItemNode(this, getFolder(), item, provider.getProject());
            }
        } else if (key instanceof AbstractNode) {
            node = (AbstractNode) key;
        }
        if (node == null) {
            return new Node[]{};
        }
        return new Node[]{node};
    }

    @Override
    protected Collection<Object> getKeys(AtomicBoolean canceled) {
        Collection<Object> collection;
        final MakeConfigurationDescriptor configurationDescriptor = getFolder().getConfigurationDescriptor();
        if (getFolder().isDiskFolder()) {
            final ArrayList<Object> collection2 = new ArrayList<>(getFolder().getElements());
            while(true) {
                if (canceled != null && canceled.get()) {
                    break;
                }
                // Search disk folder for C/C++ files and add them to the view (not the project!).
                final CndVisibilityQuery folderVisibilityQuery = configurationDescriptor.getFolderVisibilityQuery();
                final FileObject baseDirFileObject = configurationDescriptor.getBaseDirFileObject();
                final FileSystem baseDirFileSystem = configurationDescriptor.getBaseDirFileSystem();
                final FileObject fileObject = RemoteFileUtil.getFileObject(baseDirFileObject, getFolder().getRootPath());
                if (fileObject != null && fileObject.isValid() && fileObject.isFolder()) {
                    FileObject[] children = fileObject.getChildren();
                    if (children != null) {
                        for (FileObject child : children) {
                            if (canceled != null && canceled.get()) {
                                break;
                            }
                            if (child == null || !child.isValid() || child.isFolder()) {
                                // it's a folder
                                continue;
                            }
                            if (getFolder().findItemByName(child.getNameExt()) != null) {
                                // Already there
                                continue;
                            }
                            if (!VisibilityQuery.getDefault().isVisible(child)) {
                                // not visible
                                continue;
                            }

                            if (!getFolder().isTestLogicalFolder()) {
                                if (!MakeProjectOptions.getViewBinaryFiles() && CndFileVisibilityQuery.getDefault().isIgnored(child.getNameExt())) {
                                    continue;
                                }
                            }
                            // Add file to the view
                            Item item = ItemFactory.getDefault().createDetachedViewItem(baseDirFileSystem, child.getPath());
                            Folder.insertItemElementInList(collection2, item);
                        }
                    }
                }
                if (folderVisibilityQuery != null) {
                    for (Iterator<Object> it = collection2.iterator(); it.hasNext();) {
                        if (canceled != null && canceled.get()) {
                            break;
                        }
                        Object object = it.next();
                        if (object instanceof Folder) {
                            Folder fldr = (Folder) object;
                            // check if we need to show folders marked as removed
                            if (fldr.isRemoved()) {
                                FileObject toCheck = RemoteFileUtil.getFileObject(baseDirFileObject, fldr.getRootPath());
                                // hide folders from ignore pattern
                                if (toCheck != null && folderVisibilityQuery.isIgnored(toCheck)) {
                                    it.remove();
                                }
                            }
                        }
                    }
                }
                break;
            }
            collection = collection2;
        } else {
            collection = getFolder().getElements();
        }

        switch (configurationDescriptor.getState()) {
            case READING:
                if (collection.isEmpty()) {
                    collection = Collections.singletonList((Object) new LoadingNode());
                }
                break;
            case BROKEN:
            // TODO show broken node
            }
        if ("root".equals(getFolder().getName())) { // NOI18N
            LogicalViewNodeProvider[] providers = LogicalViewNodeProviders.getInstance().getProvidersAsArray();
            if (providers.length > 0) {
                for (LogicalViewNodeProvider aProvider : providers) {
                    AbstractNode node = aProvider.getLogicalViewNode(provider.getProject());
                    if (node != null) {
                        collection.add(node);
                    }
                }
            }
        }

        return collection;
    }
}
