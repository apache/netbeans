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

package org.netbeans.modules.remote.ui;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class FileSystemNode extends AbstractNode {

    private final ExecutionEnvironment env;
    private final FileObject fileObject;

    public FileSystemNode(ExecutionEnvironment env, FileObject fileObject) {
        super(createChildren(env, fileObject), Lookups.fixed(env, fileObject));
        this.env = env;
        this.fileObject = fileObject;
    }

    private static Children createChildren(ExecutionEnvironment env, FileObject rootFileObject) {
        return Children.create(new FileSystemChildren(env, rootFileObject), true);
    }

    @Override
    public String getDisplayName() {
        String path = fileObject.getPath();
        if (path == null || path.length() == 0) {
            return "/"; //NOI18N
        } else {
            return path;
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
       return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/fs_open.gif"); // NOI18N
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/fs.gif"); // NOI18N
    }

    private static class FileSystemChildren extends ChildFactory<FileObject> {

        private final ExecutionEnvironment env;
        private final FileObject rootFileObject;

        public FileSystemChildren(ExecutionEnvironment env, FileObject rootFileObject) {
            this.env = env;
            this.rootFileObject = rootFileObject;
        }

        @Override
        protected boolean createKeys(List<FileObject> toPopulate) {
            // TODO: add "Connect menu item and refresh"
            if (ConnectionManager.getInstance().isConnectedTo(env)) {
                FileObject[] children = rootFileObject.getChildren();
                Collections.addAll(toPopulate, children);
                return true;
            } else {
                toPopulate.add(rootFileObject);
                return true;
            }
        }

        @Override
        protected Node createNodeForKey(FileObject key) {
            try {
                DataObject dao = DataObject.find(key);
                Node node = dao.getNodeDelegate();
                return node;
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
                return null; //TODO: error processing
            }
        }
    }
}
