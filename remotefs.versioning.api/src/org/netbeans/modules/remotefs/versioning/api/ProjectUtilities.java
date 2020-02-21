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
package org.netbeans.modules.remotefs.versioning.api;

import org.netbeans.api.project.Project;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.remotefs.versioning.util.projects.ProjectOpener;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * Simpliied nb_all/projects/projectui/src/org/netbeans/modules/project/ui/ProjectUtilities.java,
 * nb_all/projects/projectui/src/org/netbeans/modules/project/ui/ProjectTab.java and
 * nb_all/ide/welcome/src/org/netbeans/modules/welcome/ui/TitlePanel.java copy.
 *
 */
public final class ProjectUtilities {

    /**
     * Guides user through a project opening process.
     * @param checkedOutProjects list of scanned checkout projects
     * @param workingFolder implicit folder to create a new project in if user selects <em>Create New Project</em> in the following dialog
     */
    public static void openExportedProjects(Map<Project, Set<Project>> checkedOutProjects, VCSFileProxy workingFolder) {
        ProjectOpener opener = new ProjectOpener(ProjectOpener.ProjectOpenerType.EXPORT, checkedOutProjects, workingFolder);
        opener.openProjects();
    }

    /**
     * Guides user through a project opening process.
     * @param checkedOutProjects list of scanned checkout projects
     * @param workingFolder implicit folder to create a new project in if user selects <em>Create New Project</em> in the following dialog
     */
    public static void openCheckedOutProjects(Map<Project, Set<Project>> checkedOutProjects, VCSFileProxy workingFolder) {
        ProjectOpener opener = new ProjectOpener(ProjectOpener.ProjectOpenerType.CHECKOUT, checkedOutProjects, workingFolder);
        opener.openProjects();
    }

    /**
     * Guides user through a project opening process.
     * @param checkedOutProjects list of scanned cloned projects
     * @param workingFolder implicit folder to create a new project in if user selects <em>Create New Project</em> in the following dialog
     */
    public static void openClonedOutProjects(Map<Project, Set<Project>> checkedOutProjects, VCSFileProxy workingFolder) {
        ProjectOpener opener = new ProjectOpener(ProjectOpener.ProjectOpenerType.CLONE, checkedOutProjects, workingFolder);
        opener.openProjects();
    }
}
