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
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class FileSystemRootNode extends AbstractNode {

    private final ExecutionEnvironment env;
    private static enum Kind {
        HOME,
        MIRROR,
        ROOT,
        DISCONNECTED
    }

    public FileSystemRootNode(ExecutionEnvironment env) {
        super(createChildren(env), Lookups.fixed(env));
        this.env = env;
    }

    @Override
    public Image getOpenedIcon(int type) {
       return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/fs_open.gif"); // NOI18N
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/fs.gif"); // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "LBL_FileSystemRootNode");
    }
    
    private static Children createChildren(ExecutionEnvironment env) {
        return Children.create(new FileSystemRootChildren(env), true);
    }

    private static FileObject getRootFileObject(ExecutionEnvironment env) {
        FileSystem fs = FileSystemProvider.getFileSystem(env);
        FileObject fo = fs.getRoot();
        return fo;
    }

    /*package*/ void refresh() {
        setChildren(createChildren(env));
    }

    private static class FileSystemRootChildren extends ChildFactory<Kind> {

        private final ExecutionEnvironment env;
        private final FileObject rootFileObject;

        public FileSystemRootChildren(ExecutionEnvironment env) {
            this.env = env;
            rootFileObject = getRootFileObject(env);
        }

        @Override
        protected boolean createKeys(List<Kind> toPopulate) {
            if (ConnectionManager.getInstance().isConnectedTo(env)) {
                toPopulate.add(Kind.ROOT);
                toPopulate.add(Kind.HOME);
                toPopulate.add(Kind.MIRROR);
            } else {
                toPopulate.add(Kind.DISCONNECTED);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(Kind key) {
            FileObject fo = null;
            switch (key) {
                case DISCONNECTED:
                    return new NotConnectedNode(env);
                case HOME:
                    try {
                        String homeDir = HostInfoUtils.getHostInfo(env).getUserDir();
                        fo = rootFileObject.getFileObject(homeDir);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (CancellationException ex) {
                        // don't report CancellationException
                    }
                    break;
                case MIRROR:
                    String mirror = RemotePathMap.getRemoteSyncRoot(env);
                    if (mirror!= null) {
                        fo = rootFileObject.getFileObject(mirror);
                    }
                    break;
                case ROOT:
                    fo = rootFileObject;
                    break;
                default:
                    fo = rootFileObject;
                    break;
            }
            if (fo != null) {
                return new FileSystemNode(env, fo);
            }
        return null; // TODO: error processing
        }
    }
}
