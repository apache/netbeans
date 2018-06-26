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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.groovy.grailsproject.ui;

import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Martin Adamek, Martin Janicek
 */
@NodeFactory.Registration(projectType = "org-netbeans-modules-groovy-grailsproject",
                          position=100)
public class SourceNodeFactory implements NodeFactory {

    public SourceNodeFactory() {
    }

    @Override
    public NodeList<?> createNodes(Project p) {
        return new SourcesNodeList(p.getLookup().lookup(GrailsProject.class));
    }

    private static class SourcesNodeList implements NodeList<SourceGroupKey>, ChangeListener {

        // Contains a set of String for which tree view node will be created
        // Every node not contained in this bag will use package view node
        private static final Set<String> treeViewBag = new HashSet<String>();
        static {
            treeViewBag.add("conf"); // NOI18N
            treeViewBag.add("i18n"); // NOI18N
            treeViewBag.add("views"); // NOI18N
            treeViewBag.add("web-app"); // NOI18N
            treeViewBag.add("lib"); // NOI18N
            treeViewBag.add("templates"); // NOI18N
        }

        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private GrailsProject project;


        public SourcesNodeList(GrailsProject project) {
            assert project != null;
            this.project = project;
        }

        @Override
        public List<SourceGroupKey> keys() {
            FileObject projectDir = project.getProjectDirectory();
            if (projectDir == null || !projectDir.isValid()) {
                return Collections.<SourceGroupKey>emptyList();
            }
            Sources sources = getSources();
            List<SourceGroup> sourceGroups = GroovySources.getGroovySourceGroups(sources);
            List<SourceGroupKey> result =  new ArrayList<SourceGroupKey>();

            for (SourceGroup sourceGroup : sourceGroups) {
                if (sourceGroup.getRootFolder() != null) {
                    result.add(new SourceGroupKey(sourceGroup, projectDir));
                }
            }

            Collections.sort(result);
            return result;
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
        public Node node(SourceGroupKey key) {
            String groupPath = key.group.getName();
            String groupName = groupPath.substring(groupPath.lastIndexOf("/") + 1); // NOI18N

            if (treeViewBag.contains(groupName)) {
                try {
                    DataFolder folder = DataFolder.findFolder(key.fileObject);

                    if ("lib".equals(groupName)) {
                        return new TreeRootNode(folder, key.group, project, TreeRootNode.Type.LIBRARY);
                    } else {
                        return new TreeRootNode(folder, key.group, project, TreeRootNode.Type.FOLDER);
                    }
                } catch (IllegalArgumentException ex) {
                    return null; // It might happened sometimes - see issue 208426
                }
            }

            // The rest should have package view Look & Feel
            return PackageView.createPackageView(key.group);
        }

        @Override
        public void addNotify() {
            getSources().addChangeListener(this);
        }

        @Override
        public void removeNotify() {
            getSources().removeChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }

        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }
    }

    private static class SourceGroupKey implements Comparable<SourceGroupKey> {

        public final SourceGroup group;
        public final FileObject fileObject;
        public final FileObject projectDir;


        SourceGroupKey(SourceGroup group, FileObject projectDir) {
            this.group = group;
            this.fileObject = group.getRootFolder();
            this.projectDir = projectDir;
        }

        @Override
        public int hashCode() {
            return fileObject.hashCode();
        }

        @Override
        public int compareTo(SourceGroupKey o) {
            String relativePath1 = FileUtil.getRelativePath(projectDir, fileObject);
            String relativePath2 = FileUtil.getRelativePath(projectDir, o.fileObject);
            return relativePath1.compareTo(relativePath2);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceGroupKey)) {
                return false;
            } else {
                SourceGroupKey otherKey = (SourceGroupKey) obj;
                String thisDisplayName = this.group.getDisplayName();
                String otherDisplayName = otherKey.group.getDisplayName();
                // XXX what is the operator binding order supposed to be here??
                return fileObject.equals(otherKey.fileObject) &&
                        thisDisplayName == null ? otherDisplayName == null : thisDisplayName.equals(otherDisplayName);
            }
        }

        @Override
        public String toString() {
            return group.toString();
        }
    }
}