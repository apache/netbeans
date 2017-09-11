/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
