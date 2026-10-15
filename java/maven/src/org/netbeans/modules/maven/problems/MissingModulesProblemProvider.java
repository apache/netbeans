/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.autoupdate.PluginInstaller;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public class MissingModulesProblemProvider implements ProjectProblemsProvider {
    private static final RequestProcessor RP  = new RequestProcessor(MissingModulesProblemProvider.class);
    
    @NbBundle.Messages({
        "ERR_MissingJ2eeModule=Maven Java EE support missing",
        "MSG_MissingJ2eeModule=You are missing the Maven Java EE support module in your installation. "
        + "This means that all EE-related functionality (for example, Deployment, File templates) is missing. "
        + "The most probable cause is that part of the general Java EE support is missing as well. "
        + "Please go to Tools/Plugins and install the plugins related to Java EE."
    })
    @ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = "org-netbeans-modules-maven")
    public static ProjectProblemsProvider j2ee(Project project) {
        Set<String> packs = new HashSet<String>();
        packs.add(NbMavenProject.TYPE_WAR);
        packs.add(NbMavenProject.TYPE_EAR);
        packs.add(NbMavenProject.TYPE_EJB);
        return new MissingModulesProblemProvider(project, packs, "org.netbeans.modules.maven.j2ee", "org.netbeans.modules.j2ee.kit", ERR_MissingJ2eeModule(), MSG_MissingJ2eeModule());
    }
    @NbBundle.Messages({
        "ERR_MissingApisupportModule=Maven NetBeans Module Projects support missing",
        "MSG_MissingApisupportModule=You are missing the Maven NetBeans Module Projects module in your installation. "
        + "This means that all NetBeans Platform functionality (for example, API wizards, running Platform applications) is missing. "
        + "The most probable cause is that part of the general Platform development support is missing as well. "
        + "Please go to Tools/Plugins and install the plugins related to NetBeans development."
    })
    @ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = "org-netbeans-modules-maven")
    public static ProjectProblemsProvider apisupport(Project project) {
        Set<String> packs = new HashSet<String>();
        packs.add(NbMavenProject.TYPE_NBM);
        packs.add(NbMavenProject.TYPE_NBM_APPLICATION);
        return new MissingModulesProblemProvider(project, packs, "org.netbeans.modules.maven.apisupport", "org.netbeans.modules.apisupport.kit", ERR_MissingApisupportModule(), MSG_MissingApisupportModule());
    }
    
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final Project project;
    private EnablementListener listener;
    private String lastPackaging;
    private final AtomicBoolean projectListenerSet = new AtomicBoolean(false);
    private final PropertyChangeListener projectListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                if (lastPackaging != null) {
                    if (!lastPackaging.equals(project.getLookup().lookup(NbMavenProject.class).getPackagingType())) {
                        //reset only if packaging changed, that's when maybe the missing modules do or don't matter anymore
                        firePropertyChange();
                    }
                }
            }
        }
    };
    private final Set<String> packagings;
    private final String moduleCodenameBase;
    private final String kitCodeNameBase;
    private final String problemDescription;
    private final String problemName;

    private MissingModulesProblemProvider(Project project, Set<String> packagings, String moduleCodenameBase, String kitCodeNameBase, String errorMessage, String errorDescription) {
        this.project = project;
        this.packagings = packagings;
        this.moduleCodenameBase = moduleCodenameBase;
        this.kitCodeNameBase = kitCodeNameBase;
        this.problemName = errorMessage;
        this.problemDescription = errorDescription;
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
    public Collection<? extends ProjectProblem> getProblems() {
        //lazy adding listener only when someone asks for the problems the first time
        if (projectListenerSet.compareAndSet(false, true)) {
            //TODO do we check only when the project is opened?
            project.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(projectListener);
        }
        return doIDEConfigChecks();
    }

    public synchronized Collection<ProjectProblem> doIDEConfigChecks() {
        Collection<ProjectProblem> toRet = new ArrayList<ProjectProblem>();
        NbMavenProject nbproject = project.getLookup().lookup(NbMavenProject.class);
        String packaging = nbproject.getPackagingType();
        
        if (packagings.contains(packaging)) {
            //TODO check on lastpackaging to prevent re-calculation
            ModuleInfo moduleInfo = listener != null ? listener.info : findModule(moduleCodenameBase);
            boolean foundModule = moduleInfo != null && moduleInfo.isEnabled();
            if (!foundModule) {
                if (listener == null) {
                    ProjectProblem problem = ProjectProblem.createWarning(problemName, problemDescription, new InstallModulesResolver(kitCodeNameBase));
                    listener = new EnablementListener(moduleInfo, problem);
                    listener.startListening();
                }
                toRet.add(listener.problem);
            } else {
                if (listener != null) {
                    listener.stopListening();
                    listener = null;
                }
            }
        }
        lastPackaging = packaging;
        
        return toRet;
    }

    private void firePropertyChange() {
        support.firePropertyChange(ProjectProblemsProvider.PROP_PROBLEMS, null, null);
    }

    private ModuleInfo findModule(@NonNull String codenamebase) {
        Collection<? extends ModuleInfo> infos = Lookup.getDefault().lookupAll(ModuleInfo.class);
        for (ModuleInfo info : infos) {
            if (codenamebase.equals(info.getCodeNameBase())) {
                return info;
            }
        }
        return null;
    }

    private class EnablementListener implements PropertyChangeListener {

        private final @NullAllowed ModuleInfo info;
        private final @NonNull ProjectProblem problem;

        public EnablementListener(@NullAllowed ModuleInfo info, @NonNull ProjectProblem problem) {
            this.info = info;
            this.problem = problem;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
                if (info != null && info.isEnabled()) {
                    info.removePropertyChangeListener(this);
                    firePropertyChange();
                }
            }
        }
        
        public void stopListening() {
            if (info != null) {
                info.removePropertyChangeListener(this);
            }
        }

        private void startListening() {
            if (info != null) {
                info.addPropertyChangeListener(this);
            }
        }
    }

    private static class InstallModulesResolver implements ProjectProblemResolver {
        private final String codenamebase;

        public InstallModulesResolver(String codenamebase) {
            this.codenamebase = codenamebase;
        }

        @Override
        public Future<Result> resolve() {
            FutureTask<Result> task = new FutureTask<Result>(new Callable<Result>() {
                @Override
                public Result call() throws Exception {
                    final Result[] res = new Result[1];
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                Object retval = PluginInstaller.getDefault().install(codenamebase);
                                res[0] = retval == null ? Result.create(Status.RESOLVED) : Result.create(Status.UNRESOLVED);
                            }
                        });
                    } catch (InterruptedException ex) {
                        res[0] = Result.create(Status.UNRESOLVED);
                    } catch (InvocationTargetException ex) {
                        res[0] = Result.create(Status.UNRESOLVED);
                    }
                    return res[0];
                }
            });
            RP.execute(task);
            return task;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + (this.codenamebase != null ? this.codenamebase.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InstallModulesResolver other = (InstallModulesResolver) obj;
            if ((this.codenamebase == null) ? (other.codenamebase != null) : !this.codenamebase.equals(other.codenamebase)) {
                return false;
            }
            return true;
        }
        
        
    }

}
