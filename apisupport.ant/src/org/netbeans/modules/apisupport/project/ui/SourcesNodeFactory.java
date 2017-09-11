/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
