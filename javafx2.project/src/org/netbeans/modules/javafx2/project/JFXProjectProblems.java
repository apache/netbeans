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
package org.netbeans.modules.javafx2.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectPlatform;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;

/**
 * Problem resolver specific to JavaFX Application project type
 *
 * @author Petr Somol, Tomas Zezula
 */
@ProjectServiceProvider(
        service = ProjectProblemsProvider.class,
        projectType = "org-netbeans-modules-java-j2seproject")
public class JFXProjectProblems implements ProjectProblemsProvider, PropertyChangeListener  {

    private static final String PLATFORM_ACTIVE = JFXProjectProperties.PLATFORM_ACTIVE;
    private final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(JFXProjectProblems.class);
    private AtomicReference<Task> updateClassPathExtensionTask = new AtomicReference<Task>();
    private AtomicBoolean testedCorrectClassPathExtension = new AtomicBoolean(false);
    private AtomicBoolean updatedClassPathExtension = new AtomicBoolean(false);
    private final Project prj;
    private final J2SEPropertyEvaluator eval;
    private final J2SEProjectPlatform platformSetter;
    private JFXPlatformUpdater updater;

    public JFXProjectProblems(final Lookup lkp) {
        this.updater = null;
        Parameters.notNull("lkp", lkp); //NOI18N
        this.prj = lkp.lookup(Project.class);
        Parameters.notNull("prj", prj); //NOI18N
        this.eval = lkp.lookup(J2SEPropertyEvaluator.class);
        Parameters.notNull("eval", eval);   //NOI18N
        this.platformSetter = lkp.lookup(J2SEProjectPlatform.class);
        Parameters.notNull("platformSetter", platformSetter);   //NOI18N
        eval.evaluator().addPropertyChangeListener(JFXProjectProblems.this);
        platformSetter.addPropertyChangeListener(JFXProjectProblems.this);
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        problemsProviderSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        problemsProviderSupport.removePropertyChangeListener(listener);
    }

    @Override
    @NbBundle.Messages({
        "LBL_FX_Not_Supported_By_JDK=Java Platform Does Not Support FX",
        "HINT_FX_Not_Supported_By_JDK=The active project platform is not JavaFX-enabled."
    })
    public Collection<? extends ProjectProblem> getProblems() {
        if(updater == null) {
            updater = prj.getLookup().lookup(JFXPlatformUpdater.class);
            if(updater != null) {
                updater.addListener(this);
            }
        }
        if(updater != null) {
            if(!updater.hasUpdated()) {
                return Collections.<ProjectProblem>emptySet();
            }
        } else {
            return Collections.<ProjectProblem>emptySet();
        }
        // initiate or evaluate classpathextension correctness test
        if((updater == null || updater.hasUpdated()) && !testedCorrectClassPathExtension.get()) {
            // if async evaluation does not run yet, launch it, otherwise cancel this problem check
            if (updateClassPathExtensionTask.get() == null || updateClassPathExtensionTask.get().isFinished()) {
                updateClassPathExtensionTask.set(RP.create(new Runnable() { // NOI18N
                    @Override
                    public void run() {
                        updatedClassPathExtension.set(false);
                        try {
                            if(!JFXProjectUtils.hasCorrectClassPathExtension(prj)) {
                                JFXProjectUtils.updateClassPathExtension(prj);
                                updatedClassPathExtension.set(true);
                            }
                            testedCorrectClassPathExtension.set(true);
                            
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "Can't access project properties: {0}", ex); // NOI18N
                        }
                    }
                }));
                updateClassPathExtensionTask.get().addTaskListener(new TaskListener() {
                    @Override
                    public void taskFinished(org.openide.util.Task task) {
                        if (updatedClassPathExtension.get()) {
                            problemsProviderSupport.fireProblemsChange();
                        }
                    }
                });
                updateClassPathExtensionTask.get().schedule(0);
            }
            return Collections.<ProjectProblem>emptySet();
        }
        testedCorrectClassPathExtension.set(false);

        if(!isFXProject(eval)) {
            return Collections.<ProjectProblem>emptySet();
        }

        return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<? extends ProjectProblemsProvider.ProjectProblem> collectProblems() {
                Collection<? extends ProjectProblemsProvider.ProjectProblem> currentProblems = ProjectManager.mutex().readAccess(
                        new Mutex.Action<Collection<? extends ProjectProblem>>() {
                            @Override
                            public Collection<? extends ProjectProblem> run() {
                                final JavaPlatform activePlatform = platformSetter.getProjectPlatform();
                                boolean hasFXProblem = activePlatform != null && !JavaFXPlatformUtils.isJavaFXEnabled(activePlatform);
                                return !hasFXProblem  ? Collections.<ProjectProblem>emptySet() :
                                    Collections.singleton(ProjectProblem.createError(
                                        Bundle.LBL_FX_Not_Supported_By_JDK(),
                                        Bundle.HINT_FX_Not_Supported_By_JDK(),
                                        new JFXProjectProblems.NonFXPlatformResolver(
                                            prj,
                                            platformSetter,
                                            null,
                                            JavaPlatform.getDefault().getSpecification().getName()))) ;
                            }
                    });
                return currentProblems;
            }
        });
    }

    private static boolean isFXProject(@NonNull final J2SEPropertyEvaluator eval) {
        if (eval == null) {
            return false;
        }
        //Don't use JFXProjectProperties.isTrue to prevent JFXProjectProperties from being loaded
        //JFXProjectProperties.JAVAFX_ENABLED is inlined by compliler
        return isTrue(eval.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED));
    }

    private static boolean isTrue(@NullAllowed final String value) {
        return  value != null && (
           "true".equalsIgnoreCase(value) ||    //NOI18N
           "yes".equalsIgnoreCase(value) ||     //NOI18N
           "on".equalsIgnoreCase(value));       //NOI18N
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (propName == null || PLATFORM_ACTIVE.equals(propName)) { // || versionProps.contains(propName)) {
            problemsProviderSupport.fireProblemsChange();
        }
    }

    private static class NonFXPlatformResolver implements ProjectProblemResolver {

        private final Project project;
        private final String type;
        private final BrokenReferencesSupport.PlatformUpdatedCallBack hook;
        private final J2SEProjectPlatform platformSetter;

        NonFXPlatformResolver(
            @NonNull final Project project,
            @NonNull final J2SEProjectPlatform platformSetter,
            @NullAllowed final BrokenReferencesSupport.PlatformUpdatedCallBack hook,
            @NonNull final String type) {
            Parameters.notNull("project", project);   //NOI18N
            Parameters.notNull("type", type);   //NOI18N
            Parameters.notNull("platformSetter", platformSetter);   //NOI18N
            this.project = project;
            this.platformSetter = platformSetter;
            this.hook = hook;
            this.type = type;
        }


        @NbBundle.Messages({"LBL_ResolveFXJDK=Choose FX-enabled Java Platform - \"{0}\" Project"})
        @Override
        public Future<Result> resolve() {
            final ChooseOtherPlatformPanel choosePlatform = new ChooseOtherPlatformPanel(type);
            final DialogDescriptor dd = new DialogDescriptor(choosePlatform, Bundle.LBL_ResolveFXJDK(ProjectUtils.getInformation(project).getDisplayName()));
            if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
                final Callable<ProjectProblemsProvider.Result> resultFnc =
                        new Callable<Result>() {
                    @Override
                    public Result call() throws Exception {
                        final JavaPlatform jp = choosePlatform.getSelectedPlatform();
                        if(jp != null) {
                            try {
                                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                                    @Override
                                    public Void run() throws IOException {
                                        platformSetter.setProjectPlatform(jp);
                                        JFXProjectUtils.updateClassPathExtension(project);
                                        return null;
                                    }
                                });
                            } catch (MutexException e) {
                                throw (IOException) e.getCause();
                            }
                            LOGGER.info("Set " + PLATFORM_ACTIVE + " to platform " + jp);
                            return ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED);
                        }
                        return ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED);
                    }
                };
                final RunnableFuture<Result> result = new FutureTask<Result>(resultFnc);
                RP.post(result);
                return result;
            }
            return new JFXProjectProblems.Done(
                    Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof JFXProjectProblems.NonFXPlatformResolver)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 29;

        }
        
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
