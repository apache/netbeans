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

package org.netbeans.modules.java.jarloader;

import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;


/**
 * A node to represent a JAR file.
 * @author Jesse Glick
 */
final class JarDataNode extends DataNode {
    private static final RequestProcessor RP = new RequestProcessor(JarDataNode.class.getName(), 1, false, false);

    public JarDataNode(JarDataObject obj) {
        this(obj, new DummyChildren());
    }
    
    private JarDataNode(JarDataObject obj, DummyChildren c) {
        super(obj, c);
        c.attachJarNode(this);
        setIconBaseWithExtension("org/netbeans/modules/java/jarloader/jar.gif"); // NOI18N
    }
    
    public Action getPreferredAction() {
        return null;
    }
    
    private static Children childrenFor(FileObject jar) {
        if (!FileUtil.isArchiveFile(jar)) {
            // Maybe corrupt, etc.
            return Children.LEAF;
        }
        final FileObject root = FileUtil.getArchiveRoot(jar);
        if (root != null) {
            return new FilterNode.Children(PackageView.createPackageView(new SourceGroup() {
                @Override public FileObject getRootFolder() {return root;}
                @Override public String getName() {return null;}
                @Override public String getDisplayName() {return null;}
                @Override public Icon getIcon(boolean opened) {return null;}
                @Override public boolean contains(FileObject file) {return true;}
                @Override public void addPropertyChangeListener(PropertyChangeListener listener) {}
                @Override public void removePropertyChangeListener(PropertyChangeListener listener) {}
            }));
        } else {
            return Children.LEAF;
        }
    }
    
    /**
     * There is no nice way to lazy create delegating node's children.
     * So, in order to fix #83595, here is a little hack that schedules
     * replacement of this dummy children on addNotify call.
     */
    static final class DummyChildren extends Children implements Runnable {

        private JarDataNode node;

        protected void addNotify() {
            super.addNotify();
            assert node != null;
            RP.post(this);
        }

        private void attachJarNode(JarDataNode jarDataNode) {
            this.node = jarDataNode;
        }

        public void run() {
            node.setChildren(childrenFor(node.getDataObject().getPrimaryFile()));
        }
        
        public boolean add(final Node[] nodes) {
            // no-op
            return false;
        }

        public boolean remove(final Node[] nodes) {
            // no-op
            return false;
        }
        
    }
    
}
