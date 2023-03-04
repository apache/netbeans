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

package org.netbeans.modules.j2ee.ejbjarproject.ui;

import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;

/**
 * NodeFactory to create Setup/Server resource nodes.
 * 
 * @author gpatil
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-j2ee-ejbjarproject", position=300)
public final class SetupDirNodeFactory implements NodeFactory {
    
    public SetupDirNodeFactory() {
    }

    @Override
    public NodeList createNodes(Project p) {
        EjbJarProject project = p.getLookup().lookup(EjbJarProject.class);
        assert project != null;
        return new SetupDirNodeList(project);
    }

    private static class SetupDirNodeList implements NodeList<String> {
        private static final String SETUP_DIR = "setupDir"; //NOI18N

        private final EjbJarProject project;

        SetupDirNodeList(EjbJarProject proj) {
            project = proj;
        }
        
        @Override
        public List<String> keys() {
            return Collections.singletonList(SETUP_DIR);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            // Ignore, not generating change event.
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            // Ignore, not generating change event.
        }

        @Override
        public Node node(String key) {
            if (SETUP_DIR.equals(key)) {
                return J2eeProjectView.createServerResourcesNode(project);
            }
            assert false: "No node for key: " + key; // NOI18N
            return null;
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }
    }    
}
