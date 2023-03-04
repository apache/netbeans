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
import java.util.Arrays;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-apisupport-project", position=100)
public class SourcesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of SourcesNodeFactory */
    public SourcesNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        NbModuleProject prj = p.getLookup().lookup(NbModuleProject.class);
        return new SourceNL(prj);
    }

    
    private static class SourceNL implements NodeList<SourceGroup>, ChangeListener {
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private static final String[] SOURCE_GROUP_TYPES = {
            JavaProjectConstants.SOURCES_TYPE_JAVA,
            NbModuleProject.SOURCES_TYPE_JAVAHELP,
        };
        
        private final NbModuleProject project;
        
        SourceNL(NbModuleProject prj) {
            project = prj;
        }
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public void addNotify() {
            ProjectUtils.getSources(project).addChangeListener(this);
        }

        public void removeNotify() {
            ProjectUtils.getSources(project).removeChangeListener(this);
        }

        public Node node(SourceGroup key) {
            return PackageView.createPackageView(key);
        }

        public List<SourceGroup> keys() {
            List<SourceGroup> l = new ArrayList<SourceGroup>();
            Sources s = ProjectUtils.getSources(project);
            for (int i = 0; i < SOURCE_GROUP_TYPES.length; i++) {
                SourceGroup[] groups = s.getSourceGroups(SOURCE_GROUP_TYPES[i]);
                l.addAll(Arrays.asList(groups));
            }
            SourceGroup javadocDocfiles = makeJavadocDocfilesSourceGroup();
            if (javadocDocfiles != null) {
                l.add(javadocDocfiles);
            }
            return l;
        }
        
        private SourceGroup makeJavadocDocfilesSourceGroup() {
            String propname = "javadoc.docfiles"; // NOI18N
            FileObject root = resolveFileObjectFromProperty(propname);
            if(root == null) {
                return null;
            }
            return GenericSources.group(project, root, propname, NbBundle.getMessage(ModuleLogicalView.class, "LBL_extra_javadoc_files"), null, null);
        }
        
        private FileObject resolveFileObjectFromProperty(String property){
            String filename = project.evaluator().getProperty(property);
            if (filename == null) {
                return null;
            }
            return project.getHelper().resolveFileObject(filename);
        }

        public void stateChanged(ChangeEvent arg0) {
            changeSupport.fireChange();
        }
}
}
