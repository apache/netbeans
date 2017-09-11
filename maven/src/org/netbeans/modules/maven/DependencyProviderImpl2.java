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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.DependencyProjectProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.Utilities;

/**
 * finds subprojects (projects this one depends on) that are locally available
 * and can be build as one unit. Uses maven multiproject infrastructure. (maven.multiproject.includes)
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service=DependencyProjectProvider.class, projectType="org-netbeans-modules-maven")
public class DependencyProviderImpl2 implements DependencyProjectProvider {

    private final Project project;
    private final ChangeSupport chs = new ChangeSupport(this);

    public DependencyProviderImpl2(Project proj) {
        project = proj;
    }

    private void addKnownOwners(Set<Project> resultset) {
        Set<Artifact> artifacts = project.getLookup().lookup(NbMavenProject.class).getMavenProject().getArtifacts();
        for (Artifact ar : artifacts) {
            File f = ar.getFile();
            if (f != null) {
                Project p = MavenFileOwnerQueryImpl.getInstance().getOwner(Utilities.toURI(f));
                if (p == project) {
                    continue;
                }
                if (p != null) {
                    resultset.add(p);
                }
            }
        }
    }    

    //TODO 1. cache result
    //     2. listen to changes and fire change. Project + MFOQI
    @Override
    public Result getDependencyProjects() {
        Set<Project> projects = new HashSet<Project>();
        addKnownOwners(projects);
        return new Result(projects, true);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        if (!chs.hasListeners()) {
            //attach listeners to changes.
        }
        chs.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        chs.removeChangeListener(listener);
        if (!chs.hasListeners()) {
            //detach listeners to changes.
        }
    }
}
