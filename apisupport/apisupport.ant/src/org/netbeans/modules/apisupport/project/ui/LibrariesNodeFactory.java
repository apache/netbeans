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

package org.netbeans.modules.apisupport.project.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-apisupport-project", position=300)
public class LibrariesNodeFactory implements NodeFactory {
    
    public @Override NodeList<?> createNodes(Project p) {
        NbModuleProject proj =  p.getLookup().lookup(NbModuleProject.class);
        assert proj != null;
        return new LibraryNL(proj);
    }
    
    private static class LibraryNL implements NodeList<String>, AntProjectListener {
        
        private NbModuleProject project;
        private final ChangeSupport cs = new ChangeSupport(this);
        
        LibraryNL(NbModuleProject prj) {
            project = prj;
        }
    
        public @Override List<String> keys() {
            List<String> toRet = new ArrayList<String>();
            toRet.add(LibrariesNode.LIBRARIES_NAME);
            for (String testType : project.supportedTestTypes()) {
                toRet.add(testType);
            }
            return toRet;
        }

        public @Override void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public @Override void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        public @Override Node node(String key) {
            if (key.equals(LibrariesNode.LIBRARIES_NAME)) {
                return  new LibrariesNode(project);
            } else {
                return new UnitTestLibrariesNode(key, project);
            }
        }

        public @Override void addNotify() {
            project.getHelper().addAntProjectListener(this);
        }

        public @Override void removeNotify() {
            project.getHelper().removeAntProjectListener(this);
        }

        public @Override void configurationXmlChanged(AntProjectEvent ev) {
            cs.fireChange();
        }
       
        public @Override void propertiesChanged(AntProjectEvent ev) {}
    }

}
