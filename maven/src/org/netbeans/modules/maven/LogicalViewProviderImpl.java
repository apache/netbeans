/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */


package org.netbeans.modules.maven;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.maven.execute.navigator.GoalsNavigatorHint;
import org.netbeans.modules.maven.nodes.MavenProjectNode;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * provider of logical view, meaning the top node in the projects tab.
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service=LogicalViewProvider.class, projectType="org-netbeans-modules-maven")
public class LogicalViewProviderImpl implements LogicalViewProvider {

    private final Project proj;

    public LogicalViewProviderImpl(Project proj) {
        this.proj = proj;
    }
    
    /**
     * create the root node for maven projects..
     */
    @Override
    public Node createLogicalView() {
        NbMavenProjectImpl project = proj.getLookup().lookup(NbMavenProjectImpl.class);
        return new MavenProjectNode(createLookup(project), project);
    }
    
    private static Lookup createLookup( NbMavenProjectImpl project ) {
        //#173465
        if (!project.getProjectDirectory().isValid()) {
            return Lookups.fixed(project);
        }
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        return Lookups.fixed(project, rootFolder, project.getProjectDirectory(), new GoalsNavigatorHint());
    }
    
    /**
     * TODO this is probably good for the Select in Project view action..
     */
    @Override
    public Node findPath(Node node, Object target) {
        NbMavenProjectImpl prj = node.getLookup().lookup(NbMavenProjectImpl.class );
        if ( prj == null ) {
            return null;
        }
        
        if ( target instanceof FileObject ) {
            FileObject fo = (FileObject)target;
            
            if (isOtherProjectSource(fo, prj) ) {
                return null; // Don't waste time if project does not own the fo
            }
            Node[] nodes = node.getChildren().getNodes(true);
            for (int i = 0; i < nodes.length; i++) {
                Node result = PackageView.findPath(nodes[i], target);
                if (result != null) {
                    return result;
                }
                
            }
            // fallback if not found by PackageView.
            for (int i = 0; i < nodes.length; i++) {
                FindDelegate deleg = nodes[i].getLookup().lookup(FindDelegate.class);
                if (deleg != null) {
                    for (Node n : deleg.getDelegates(nodes[i])) {
                        Node result = PackageView.findPath(n, fo);
                        if (result != null) {
                            return result;
                        }
                        Node found = findNodeByFileDataObject(n, fo);
                        if (found != null) {
                            return found;
                        }
                    }
                    continue;
                }
                Node found = findNodeByFileDataObject(nodes[i], fo);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }

    private static boolean isOtherProjectSource(
            @NonNull final FileObject fo,
            @NonNull final Project me) {
        final Project owner = FileOwnerQuery.getOwner(fo);
        if (owner == null) {
            return false;
        }
        if (me.equals(owner)) {
            return false;
        }
        for (SourceGroup sg : ProjectUtils.getSources(owner).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (FileUtil.isParentOf(sg.getRootFolder(), fo)) {
                return true;
            }
        }
        return false;
    }

    private Node findNodeByFileDataObject(Node node, FileObject fo) {
        FileObject xfo = node.getLookup().lookup(FileObject.class);
        if (xfo == null) {
            DataObject dobj = node.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                xfo = dobj.getPrimaryFile();
            }
        }
        if (xfo != null) {
            if ((xfo.equals(fo))) {
                return node;
            } else if (FileUtil.isParentOf(xfo, fo)) {
                FileObject folder = fo.isFolder() ? fo : fo.getParent();
                String relPath = FileUtil.getRelativePath(xfo, folder);
                List<String> path = new ArrayList<String>();
                StringTokenizer strtok = new StringTokenizer(relPath, "/"); // NOI18N
                while (strtok.hasMoreTokens()) {
                    String token = strtok.nextToken();
                    path.add(token);
                }
                try {
                    Node folderNode = folder.equals(xfo) ? node : NodeOp.findPath(node, Collections.enumeration(path));
                    if (fo.isFolder()) {
                        return folderNode;
                    } else {
                        Node[] childs = folderNode.getChildren().getNodes(true);
                        for (int j = 0; j < childs.length; j++) {
                            DataObject dobj = childs[j].getLookup().lookup(DataObject.class);
                            if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt())) {
                                return childs[j];
                            }
                        }
                    }
                } catch (NodeNotFoundException e) {
                    // OK, never mind
                }
            }
        }
        return null;
    }

    public static interface FindDelegate {
         Node[] getDelegates(Node current);
    }
    
}
