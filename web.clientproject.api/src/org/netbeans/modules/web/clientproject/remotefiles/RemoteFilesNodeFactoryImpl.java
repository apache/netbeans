/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
