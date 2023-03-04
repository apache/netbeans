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
package org.netbeans.modules.javascript.nodejs.editor;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsPlatformProvider;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.netbeans.modules.javascript2.nodejs.spi.NodeJsSupport;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

@ProjectServiceProvider(service = NodeJsSupport.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
public final class NodeJsSupportImpl implements NodeJsSupport {

    private static final String NODEJS_DOC_URL = "https://nodejs.org/docs/v%s/api/"; // NOI18N
    private static final String IOJS_DOC_URL = "https://iojs.org/dist/v%s/doc/api/"; // NOI18N

    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final PropertyChangeListener nodeJsChangeListener = new NodeJsChangeListener();

    // @GuardedBy("this")
    private org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport nodeJsSupport = null;


    public NodeJsSupportImpl(Project project) {
        this.project = project;
    }

    @Override
    public boolean isSupportEnabled() {
        return getNodeJsSupport().getPreferences().isEnabled();
    }

    @Override
    public Version getVersion() {
        assert !EventQueue.isDispatchThread() : "Should not be called in the UI thread";
        NodeExecutable node = NodeExecutable.forProject(project, false);
        if (node == null) {
            return null;
        }
        return node.getVersion();
    }

    @Override
    public String getDocumentationUrl() {
        assert !EventQueue.isDispatchThread() : "Should not be called in the UI thread";
        NodeExecutable node = NodeExecutable.forProject(project, false);
        if (node == null) {
            return null;
        }
        Version version = node.getVersion();
        if (version == null) {
            return null;
        }
        return String.format(node.isIojs() ? IOJS_DOC_URL : NODEJS_DOC_URL, version.toString());
    }

    @Override
    public FileObject getDocumentationFolder() {
        assert !EventQueue.isDispatchThread() : "Should not be called in the UI thread";
        File nodeSources = NodeJsUtils.getNodeSources(project);
        if (nodeSources == null) {
            return null;
        }
        FileObject sources = FileUtil.toFileObject(nodeSources);
        if (sources == null) {
            return null;
        }
        return sources.getFileObject("doc"); // NOI18N
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private synchronized org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport getNodeJsSupport() {
        if (nodeJsSupport == null) {
            nodeJsSupport = org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport.forProject(project);
            nodeJsSupport.addPropertyChangeListener(nodeJsChangeListener);
        }
        return nodeJsSupport;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    //~ Inner classes

    private final class NodeJsChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (NodeJsPlatformProvider.PROP_ENABLED.equals(propertyName)
                    || NodeJsPlatformProvider.PROP_SOURCE_ROOTS.equals(propertyName)) {
                fireChange();
            }
        }

    }

}
