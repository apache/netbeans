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
package org.netbeans.api.project.ui;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.ProjectProblemsImplementation;
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
        final ProjectProblemsImplementation impl = Lookup.getDefault().lookup(ProjectProblemsImplementation.class);
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
        final ProjectProblemsImplementation impl = Lookup.getDefault().lookup(ProjectProblemsImplementation.class);
        if (impl != null) {
            try {
                // compatibility: wait for the process to complete.
                impl.showCustomizer(project).get();
            } catch (InterruptedException ex) {
                throw new CompletionException(ex);
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof Error) {
                    throw (Error)ex.getCause();
                }
                if (ex.getCause() instanceof RuntimeException) {
                    throw (RuntimeException)ex.getCause();
                }
                throw new CompletionException(ex.getCause());
            }
        }
    }

}
