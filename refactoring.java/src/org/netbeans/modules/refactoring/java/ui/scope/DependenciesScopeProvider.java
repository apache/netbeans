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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.project.JavaProjectConstants;
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
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import static org.netbeans.modules.refactoring.java.ui.scope.Bundle.*;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
@NbBundle.Messages("LBL_Dependencies=Open Projects & Dependencies")
@ScopeProvider.Registration(id = "all-projects-dependencies", displayName = "#LBL_Dependencies", position = 50, iconBase = "org/netbeans/modules/refactoring/java/resources/found_item_binary.gif")
@ScopeReference(path = "org-netbeans-modules-refactoring-java-ui-WhereUsedPanel")
public class DependenciesScopeProvider extends ScopeProvider {

    private Scope scope;
    private Problem problem;

    public DependenciesScopeProvider() {
        problem = new Problem(false, WRN_COMPILED());
    }

    @Override
    public boolean initialize(Lookup context, AtomicBoolean cancel) {
        if (!JavaWhereUsedQueryPlugin.DEPENDENCIES) {
            return false;
        }
        Future<Project[]> openProjects = OpenProjects.getDefault().openProjects();

        Project[] projects;
        try {
            projects = openProjects.get();
        } catch (InterruptedException | ExecutionException ex) {
            return false;
        }

        if (projects == null || projects.length == 0) {
            return false;
        }

        Set<FileObject> srcRoots = new HashSet<>();

        for (Project project : projects) {
            ProjectInformation pi = ProjectUtils.getInformation(project);
            final SourceGroup[] sourceGroups = ProjectUtils.getSources(pi.getProject()).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i = 0; i < sourceGroups.length; i++) {
                srcRoots.add(sourceGroups[i].getRootFolder());
            }
        }
        if (srcRoots.isEmpty()) {
            return false;
        }
        scope = Scope.create(srcRoots, null, null, true);
        return true;
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
