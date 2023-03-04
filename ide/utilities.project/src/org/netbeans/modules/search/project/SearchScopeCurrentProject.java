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
package org.netbeans.modules.search.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Search info for current project - project of a file opened in the active top
 * component.
 *
 * @author jhavlin
 */
public class SearchScopeCurrentProject extends SearchScopeDefinition {

    private static final int NAME_LENGTH_LIMIT = 30;
    private PropertyChangeListener pcl;
    private boolean applicable = true;
    private String name = null;
    private Icon icon;
    private Project project = null;

    public SearchScopeCurrentProject() {
        pcl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (TopComponent.Registry.PROP_ACTIVATED.equals(
                        evt.getPropertyName())) {
                    update();
                }
            }
        };
        TopComponent.getRegistry().addPropertyChangeListener(pcl);
        update();
    }

    private void update() {
        project = null;
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc != null) {
            FileObject fo = tc.getLookup().lookup(FileObject.class);
            if (fo == null) {
                DataObject dob = tc.getLookup().lookup(DataObject.class);
                if (dob != null) {
                    fo = dob.getPrimaryFile();
                }
            }
            if (fo != null) {
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {
                    project = p;
                }
            }
        }
        if (project != null) {
            applicable = true;
            name = NbBundle.getMessage(SearchScopeCurrentProject.class,
                    "SearchScopeCurrentProject", shortName(project));   //NOI18N
            icon = ProjectUtils.getInformation(project).getIcon();
        } else {
            applicable = false;
            name = "";                                                  //NOI18N
            icon = null;
        }
        notifyListeners();
    }

    private String shortName(Project p) {
        assert p != null;
        ProjectInformation pi = ProjectUtils.getInformation(p);
        if (pi == null) {
            return "";                                                  //NOI18N
        } else {
            String name = pi.getDisplayName();
            if (name == null) {
                return "";                                              //NOI18N
            } else if (name.length() > NAME_LENGTH_LIMIT) {
                return name.substring(0, NAME_LENGTH_LIMIT - 3)
                        + "...";                                        //NOI18N
            } else {
                return name;
            }
        }
    }

    @Override
    public String getTypeId() {
        return "current project";                                       //NOI18N
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public boolean isApplicable() {
        assert !applicable || (applicable && project != null && name != null);
        return applicable;
    }

    @Override
    public SearchInfo getSearchInfo() {
        return AbstractProjectSearchScope.createSingleProjectSearchInfo(
                project);
    }

    @Override
    public int getPriority() {
        return 99; // above main project
    }

    @Override
    public void clean() {
        TopComponent.getRegistry().removePropertyChangeListener(pcl);
        pcl = null;
        applicable = false;
        name = null;
        project = null;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }
}
