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
package org.netbeans.modules.refactoring.java.ui.scope;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.java.plugins.JavaWhereUsedQueryPlugin;
import org.netbeans.modules.refactoring.spi.ui.ScopeProvider;
import org.netbeans.modules.refactoring.spi.ui.ScopeReference;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.refactoring.java.ui.scope.Bundle.*;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
@ScopeProvider.Registration(id = "current-project-dependencies", displayName = "#LBL_CurrentProjectDependencies", position = 150)
@ScopeReference(path = "org-netbeans-modules-refactoring-java-ui-WhereUsedPanel")
@Messages({"LBL_CurrentProjectDependencies=Current Project & Dependencies",
           "WRN_COMPILED=<html>Disclaimer: Searching for usages in compiled dependencies has \n" +
"certain limitations. <a href=\"http://wiki.netbeans.org/Find_Usages_in_Compiled_Dependencies\">Find out more.</a>"})
public final class CurrentJavaProjectDependenciesScopeProvider extends ScopeProvider {
    
    private String detail;
    private Scope scope;
    private Problem problem;
    private Icon icon;

    public CurrentJavaProjectDependenciesScopeProvider() {
        problem = new Problem(false, WRN_COMPILED());
    }
    
    @Override
    public boolean initialize(Lookup context, AtomicBoolean cancel) {
        if(!JavaWhereUsedQueryPlugin.DEPENDENCIES) {
            return false;
        }
        FileObject file = context.lookup(FileObject.class);
        Project selected = null;
        if (file != null) {
            selected = FileOwnerQuery.getOwner(file);
        }
        if (selected == null) {
            selected = context.lookup(Project.class);
            if (selected == null) {
                SourceGroup sg = context.lookup(SourceGroup.class);
                if (sg != null) {
                    selected = FileOwnerQuery.getOwner(sg.getRootFolder());
                }
            }
            if (selected == null) {
                DataFolder df = context.lookup(DataFolder.class);
                if (df != null) {
                    selected = FileOwnerQuery.getOwner(df.getPrimaryFile());
                }
            }
        }
        if (selected == null || !OpenProjects.getDefault().isProjectOpen(selected)) {
            return false;
        }

        ProjectInformation pi = ProjectUtils.getInformation(selected);
        final SourceGroup[] sourceGroups = ProjectUtils.getSources(pi.getProject()).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject[] projectSources = new FileObject[sourceGroups.length];
        for (int i = 0; i < sourceGroups.length; i++) {
            projectSources[i] = sourceGroups[i].getRootFolder();
        }
        scope = Scope.create(Arrays.asList(projectSources), null, null, true);
        detail = pi.getDisplayName();
        icon = new ImageIcon(ImageUtilities.mergeImages(
                ImageUtilities.icon2Image(pi.getIcon()),
                ImageUtilities.loadImage("org/netbeans/modules/refactoring/java/resources/binary_badge.gif"),
                10, 10));
        return true;
    }

    @Override
    public String getDetail() {
        return detail;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public Problem getProblem() {
        return problem;
    }
}
