/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.classpath.BootClassPathImpl;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import static org.netbeans.modules.maven.problems.Bundle.*;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=ProjectProblemsProvider.class, projectType="org-netbeans-modules-maven")
public class JavaPlatformProblemProvider implements ProjectProblemsProvider {
    private final ProjectProblemsProviderSupport support;
    private final Project project;
    private final PropertyChangeListener pchListener;
    private final PropertyChangeListener pchJavaPlatformListener;
    private JavaPlatformManager platformManager;

    public JavaPlatformProblemProvider(Project project) {
        support = new ProjectProblemsProviderSupport(this);
        this.project = project;
        this.pchListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    support.fireProblemsChange(); //always fire or first calculate and then fire?
                }
            }
        };
        this.pchJavaPlatformListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                support.fireProblemsChange();
            }
        };
    }

    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
       support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    @NbBundle.Messages({
        "# {0} - jdk platform id",
        "MGS_No_such_JDK=No such Java Platform: {0}",
        "# {0} - jdk platform id",
        "MGS_Broken_JDK=Invalid Java Platform: {0}",
        "# {0} - jdk platform id",
        "DESC_No_such_JDK=There is no Java Platform with value \"{0}\" defined in the current IDE. The default platform is used instead. To fix, introduce a Java Platform with the given name or change the project's used Java Platform.",
        "# {0} - jdk platform id",
        "DESC_Broken_JDK=There is no Java Platform with value \"{0}\" defined in the current IDE. The default platform is used instead. To fix, introduce a Java Platform with the given name or change the project's used Java Platform."})
    public Collection<? extends ProjectProblem> getProblems() {
        if (platformManager == null) {
                platformManager = JavaPlatformManager.getDefault();
                platformManager.addPropertyChangeListener(WeakListeners.propertyChange(pchJavaPlatformListener, platformManager));
                NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
                watch.addPropertyChangeListener(pchListener);  
        }
        return support.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<? extends ProjectProblem> collectProblems() {
                List<ProjectProblem> toRet = new ArrayList<ProjectProblem>();
                String val = project.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_JDK_PLATFORM, true);
                if (val != null) {
                    JavaPlatform plat = BootClassPathImpl.getActivePlatform(val);
                    final String[] desc;
                    if (plat == null) {
                        desc = new String[] {
                            MGS_No_such_JDK(val),
                            DESC_No_such_JDK(val)
                        };
                    } else if (!plat.isValid()) {
                        desc = new String[] {
                            MGS_Broken_JDK(val),
                            DESC_Broken_JDK(val)
                        };
                    } else {
                        desc = null;
                    }
                    if (desc != null) {
                        //we have a problem.
                        toRet.add(ProjectProblemsProvider.ProjectProblem.createWarning(desc[0], desc[1],
                                new ProjectProblemResolver() {
                                    @Override
                                    public Future<Result> resolve() {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                PlatformsCustomizer.showCustomizer(null);
                                            }
                                        });                                       
                                        return new Done(ProjectProblemsProvider.Result.create(Status.UNRESOLVED));
                                    }
                        }));
                    }
                }
                return toRet;
            }
        });
    }
   
    
        private static final class Done implements Future<ProjectProblemsProvider.Result> {

        private final ProjectProblemsProvider.Result result;

        Done(@NonNull final ProjectProblemsProvider.Result result) {
            Parameters.notNull("result", result);   //NOI18N
            this.result = result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public ProjectProblemsProvider.Result get() throws InterruptedException, ExecutionException {
            return result;
        }

        @Override
        public ProjectProblemsProvider.Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }

    }
}
