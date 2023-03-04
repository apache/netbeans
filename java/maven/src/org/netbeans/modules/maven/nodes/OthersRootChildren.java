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
package org.netbeans.modules.maven.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import org.apache.maven.model.Resource;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.VisibilityQueryDataFilter;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.classpath.MavenSourcesImpl;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author  Milos Kleint
 */
class OthersRootChildren extends Children.Keys<SourceGroup> {
    private static final @StaticResource String FILTERED_RESOURCE = "org/netbeans/modules/maven/nodes/filteredResourceWarningBadge.png";
    
    private NbMavenProjectImpl project;
    private PropertyChangeListener changeListener;
    private boolean test;
    public OthersRootChildren(NbMavenProjectImpl prj, boolean testResource) {
        this.project = prj;
        test = testResource;
        changeListener  = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName()) ||
                    NbMavenProject.PROP_RESOURCE.equals(evt.getPropertyName())) 
                {
                    regenerateKeys();
                    refresh();
                }
            }
        };
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        NbMavenProject.addPropertyChangeListener(project, changeListener);
        regenerateKeys();
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.<SourceGroup>emptySet());
        NbMavenProject.removePropertyChangeListener(project, changeListener);
        super.removeNotify();
        
    }
    
    private void regenerateKeys() {
        List<SourceGroup> list = new ArrayList<SourceGroup>();
        SourceGroup[] resgroup = getSourceGroups();
        Set<FileObject> files = new HashSet<FileObject>();
        for (int i = 0; i < resgroup.length; i++) {
            list.add(resgroup[i]);
            files.add(resgroup[i].getRootFolder());
            //TODO all direct subfolders that are contained in the SG?
        }
        setKeys(list);
        ((OthersRootNode)getNode()).setFiles(files);
    }
    
    private SourceGroup[] getSourceGroups() {
        Sources srcs = project.getLookup().lookup(Sources.class);
        if (srcs == null) {
            throw new IllegalStateException("need Sources instance in lookup"); //NOI18N
        }
        return  srcs.getSourceGroups(test ? MavenSourcesImpl.TYPE_TEST_OTHER  
                                                           : MavenSourcesImpl.TYPE_OTHER);
    }

    void doRefresh() {
        for (SourceGroup sg : getSourceGroups()) {
            super.refreshKey(sg);
        }
    }
    
    
    @Override
    protected Node[] createNodes(SourceGroup grp) {
        Node[] toReturn = new Node[1];
        if (grp instanceof MavenSourcesImpl.OtherGroup) {
            DataFolder dobj = DataFolder.findFolder(grp.getRootFolder());
            MavenSourcesImpl.OtherGroup resgrp = (MavenSourcesImpl.OtherGroup)grp;
            if (resgrp.getResource() != null && OthersRootNode.showAsPackages()) {
                //#159560 PackageView.cPV operates with owners of files. If file not
                // owned by the project, then it's skipped. Resulting in empty view.
                // marking the resources as owned by the current project could result
                // in clashes when resources are pointed to from various projects..
                // -> we just silently replace the PackageView approach with a simple flder view
                // that's a ui inconsistency but less severe than having an empty view..
                Project owner = FileOwnerQuery.getOwner(resgrp.getRootFolder());
                if (owner != null && owner.getProjectDirectory().equals(project.getProjectDirectory())) {
                    toReturn[0] = new OGFilterNode(PackageView.createPackageView(grp), resgrp);
                } else {
                    Children childs = dobj.createNodeChildren(VisibilityQueryDataFilter.VISIBILITY_QUERY_FILTER);
                    toReturn[0] = new OGFilterNode(dobj.getNodeDelegate().cloneNode(), childs, resgrp);
                }
            } else {
                Children childs = dobj.createNodeChildren(VisibilityQueryDataFilter.VISIBILITY_QUERY_FILTER);
                toReturn[0] = new OGFilterNode(dobj.getNodeDelegate().cloneNode(), childs, resgrp);
            }
        } else {
            assert false : "Group is not a MavenSourcesImpl.OtherGroup instance"; //NOI18N
        }
        return toReturn;
    }
    

    private static class OGFilterNode extends FilterNode {
        private final MavenSourcesImpl.OtherGroup group;

        OGFilterNode(Node orig, MavenSourcesImpl.OtherGroup grp) {
            super(orig);
            group = grp;
        }

        OGFilterNode(Node orig, org.openide.nodes.Children childs, MavenSourcesImpl.OtherGroup grp) {
            super(orig, childs);
            group = grp;
        }

        @Override
        @Messages({
            "# {0} - directory path",
            "TIP_Resource1=<html>Resource directory defined in POM.<br><i>Directory: </i><b>{0}</b><br>", 
            "# {0} - maven resource target path",
            "TIP_Resource2=<i>Target Path: </i><b>{0}</b><br>", 
            "# {0} - boolean value",
            "TIP_Resource6=<b><i>Filtering: </i>{0}. Please note that some IDE features rely on non-filtered content only.</b><br>", 
            "# {0} - includes string value",
            "TIP_Resource3=<i>Includes: </i><b>{0}</b><br>", 
            "# {0} - excludes string value",
            "TIP_Resource4=<i>Excludes: </i><b>{0}</b><br>", 
            "# {0} - directory path",
            "TIP_Resource5=<html>Configuration Directory<br><i>Directory: </i><b>{0}</b><br>"})
        public String getShortDescription() {
            if (group.getResource() != null) {
                Resource rs = group.getResource();
                String str = TIP_Resource1(rs.getDirectory());
                if (rs.getTargetPath() != null) {
                    str = str + TIP_Resource2(rs.getTargetPath());
                }
                if (rs.isFiltering()) {
                    str = str + TIP_Resource6(rs.isFiltering());
                }
                if (rs.getIncludes() != null && rs.getIncludes().size() > 0) {
                    str = str + TIP_Resource3(Arrays.toString(rs.getIncludes().toArray()));
                }
                if (rs.getExcludes() != null && rs.getExcludes().size() > 0) {
                    str = str + TIP_Resource4(Arrays.toString(rs.getExcludes().toArray()));
                }
                return str;
            } else {
                return  TIP_Resource5(FileUtil.getFileDisplayName(group.getRootFolder()));
             }
        }

        @Override
        public String getDisplayName() {
            if (group.getResource() != null) {
                return group.getDisplayName();
            } else {
                return super.getDisplayName();
            }
        }

        public @Override Image getIcon(int type) {
            return computeIcon( false, type );
        }
        
        public @Override Image getOpenedIcon(int type) {
            return computeIcon( true, type );
        }
        private Image computeIcon( boolean opened, int type ) {
            if (group.getResource() != null) {
                Icon icon = group.getIcon( opened );
                Image img = ImageUtilities.icon2Image(icon);
                if (group.getResource().isFiltering()) {
                    Image warn = ImageUtilities.loadImage(FILTERED_RESOURCE);
                    img = ImageUtilities.mergeImages(img, warn , 0, 8);
                }
                return img;
            } else {
                return super.getIcon(type);
            }
        }

    }
   
}
