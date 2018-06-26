/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
