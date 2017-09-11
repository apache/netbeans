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
package org.netbeans.api.project.ui;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.BrokenReferencesImplementation;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Support for managing project problems. Project freshly checkout from
 * VCS can has broken references of several types: reference to other project,
 * reference to a foreign file, reference to an external source root, reference
 * to a library, etc. This class has helper methods for detection of these problems
 * and for fixing them.
 *
 * @see ProjectProblemsProvider
 * 
 * @since 1.60
 * @author Tomas Zezula
 */
public class ProjectProblems {

    private ProjectProblems() {
        throw new IllegalStateException();
    }


    /**
     * Checks whether the project has some project problem.
     * @param project the project to test for existence of project problems like broken references, missing server, etc.
     * @return true if some problem was found and it is necessary to give
     *  user a chance to fix them.
     */
    public static boolean isBroken(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        final ProjectProblemsProvider provider = project.getLookup().lookup(ProjectProblemsProvider.class);
        return provider !=null && !provider.getProblems().isEmpty();
    }


    /**
     * Show alert message box informing user that a project has broken
     * references. This method can be safely called from any thread, e.g. during
     * the project opening, and it will take care about showing message box only
     * once for several subsequent calls during a timeout.
     * The alert box has also "show this warning again" check box and provides resolve
     * broken references option
     * @param project to show the alert for.
     */
    public static void showAlert(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        final BrokenReferencesImplementation impl = Lookup.getDefault().lookup(BrokenReferencesImplementation.class);
        if (impl != null) {
            impl.showAlert(project);
        }
    }

    /**
     * Shows an UI customizer which gives users chance to fix encountered problems.
     * @param project the project for which the customizer should be shown.
     */
    public static void showCustomizer(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        final BrokenReferencesImplementation impl = Lookup.getDefault().lookup(BrokenReferencesImplementation.class);
        if (impl != null) {
            impl.showCustomizer(project);
        }
    }

}
