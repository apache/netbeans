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

package org.netbeans.modules.j2ee.earproject.ui;

import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
/**
 *
 * @author Lukas Jungmann
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-j2ee-earproject", position=50)
public class ModuleNodeFactory implements NodeFactory {

    public ModuleNodeFactory() {
    }

    public NodeList<String> createNodes(Project p) {
        EarProject project = p.getLookup().lookup(EarProject.class);
        assert project != null;
        return new ModuleNodeList(project);
    }

    private static final class ModuleNodeList implements NodeList<String> {

        private static final String JAVAEE_MODULES = "javaeeModules"; //NOI18N

        private final EarProject project;

        ModuleNodeList(EarProject proj) {
            project = proj;
        }

        public List<String> keys() {
            return Collections.singletonList(JAVAEE_MODULES);
        }

        public void addChangeListener(ChangeListener l) {
            // Ignore, not generating change event.
        }

        public void removeChangeListener(ChangeListener l) {
            // Ignore, not generating change event.
        }

        public Node node(String key) {
            if (JAVAEE_MODULES.equals(key)) {
                return new LogicalViewNode(project.getAntProjectHelper(), project, 
                        project.getUpdateHelper(), project.getClassPathSupport());
            }
            assert false : "No node for key: " + key; // NOI18N
            return null;
        }

        public void addNotify() {
        }

        public void removeNotify() {
        }
    }
}

