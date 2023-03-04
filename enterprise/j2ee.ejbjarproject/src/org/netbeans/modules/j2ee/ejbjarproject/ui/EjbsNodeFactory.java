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
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;

/**
 * NodeFactory to create EJB nodes.
 * 
 * @author gpatil
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-j2ee-ejbjarproject", position=150)
public class EjbsNodeFactory implements NodeFactory {

    public EjbsNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        EjbJarProject project = p.getLookup().lookup(EjbJarProject.class);
        assert project != null;
        return new EjbNodeList(project);
    }
    
    private static class EjbNodeList implements NodeList<String> {
        private static final String KEY_EJBS = "ejbKey"; //NOI18N

        private final EjbJarProject project;

        EjbNodeList(EjbJarProject proj) {
            this.project = proj;
        }
        
        public List<String> keys() {
            return Collections.singletonList(KEY_EJBS);
        }

        public void addChangeListener(ChangeListener l) {
            // Ignore, will not generate any change event.
        }

        public void removeChangeListener(ChangeListener l) {
            // Ignore, will not generate any change event.
        }

        public Node node(String key) {
            if (KEY_EJBS.equals(key)) {
                EjbJar ejbModule = project.getAPIEjbJar(); 
                return J2eeProjectView.createEjbsView(ejbModule, project);
            }
            assert false: "No node for key: " + key; //NO18N
            return null;
        }

        public void addNotify() {
        }

        public void removeNotify() {
        }
    }           
}
