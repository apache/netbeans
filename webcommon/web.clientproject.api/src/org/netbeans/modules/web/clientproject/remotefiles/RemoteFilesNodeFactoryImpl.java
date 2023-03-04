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
package org.netbeans.modules.web.clientproject.remotefiles;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

public class RemoteFilesNodeFactoryImpl implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project p) {
        return new RemoteFilesNodeListImpl(p);
    }

    private static class RemoteFilesNodeListImpl implements NodeList<String>, ChangeListener {

        private final Project project;
        private final RemoteFiles remoteFiles;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public RemoteFilesNodeListImpl(Project project) {
            this.project = project;
            this.remoteFiles = new RemoteFiles(project);
        }

        @Override
        public void addNotify() {
            // #230378 - use weak listeners otherwise project is not garbage collected
            remoteFiles.addChangeListener(WeakListeners.change(this, remoteFiles));
        }

        @Override
        public void removeNotify() {
            // #230378 - weak listeners are used so no need to call "removeListener"
        }

        @Override
        public List<String> keys() {
            ArrayList<String> keys = new ArrayList<>();
            if (!remoteFiles.getRemoteFiles().isEmpty()) {
                keys.add("key"); // NOI18N
            }
            return keys;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public Node node(String key) {
            return new RemoteFilesNode(project, remoteFiles);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }

    }

}
