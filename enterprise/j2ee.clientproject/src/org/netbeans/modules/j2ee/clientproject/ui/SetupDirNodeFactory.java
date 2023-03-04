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

package org.netbeans.modules.j2ee.clientproject.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-j2ee-clientproject",position=400)
public final class SetupDirNodeFactory implements NodeFactory {
    
    /** Creates a new instance of LibrariesNodeFactory */
    public SetupDirNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        AppClientProject project = p.getLookup().lookup(AppClientProject.class);
        assert project != null;
        return new SetupDirNodeList(project);
    }

    private static class SetupDirNodeList implements NodeList<String>, PropertyChangeListener {
        private static final String SETUP_DIR = "setupDir"; //NOI18N

        private final AppClientProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        SetupDirNodeList(AppClientProject proj) {
            project = proj;
            AppClientLogicalViewProvider logView = project.getLookup().lookup(AppClientLogicalViewProvider.class);
            assert logView != null;
        }
        
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            result.add(SETUP_DIR);
            return result;
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(String key) {
            if (key == SETUP_DIR) {
                return J2eeProjectView.createServerResourcesNode(project);
            }
            assert false: "No node for key: " + key;
            return null;
        }

        public void addNotify() {
        }

        public void removeNotify() {
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
        
    }
    
}
