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

package org.netbeans.modules.cnd.makeproject.ui;

import org.netbeans.modules.cnd.makeproject.api.TempEnv;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.CodeAssistanceConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.api.ConnectionNotifier;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;

/**
 *
 */
public final class EnvProjectProblemsProvider {

    private final MakeProject project;
    private final AtomicReference<UnsetEnvVar> lastUnsetVar = new AtomicReference<>();
    private ExecutionEnvironment listeningEnv = null;
    private final Object listeningEnvLock = new Object();

    public EnvProjectProblemsProvider(MakeProject project) {
        Parameters.notNull("project", project); //NOI18N
        this.project = project;
    }

    public Set<? extends ProjectProblemsProvider.ProjectProblem> getEnvProblems() {
        final UnsetEnvVar unset = getUndefinedEnvVars();
        lastUnsetVar.set(unset);
        if (unset != null && !unset.getUndefinedEnvVars().isEmpty()) {
            final ProjectProblemsProvider.ProjectProblem error
                    = ProjectProblemsProvider.ProjectProblem.createError(
                            NbBundle.getMessage(ResolveReferencePanel.class, "env_var_resolve_name"), //NOI18N
                            NbBundle.getMessage(ResolveReferencePanel.class, "env_var_resolve_description"), //NOI18N
                            new EnvResolverImpl(project, unset));
            ExecutionEnvironment env = unset.getExecutionEnvironment();
            if (env.isRemote()) {
                synchronized (listeningEnvLock) {
                    if (!env.equals(listeningEnv)) {
                        listeningEnv = env;
                        ConnectionNotifier.addTask(env, new ConnListener(project, lastUnsetVar));
                    }
                }
            }
            return Collections.singleton(error);
        }
        return Collections.emptySet();
    }

    private UnsetEnvVar getUndefinedEnvVars() {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (cdp.gotDescriptor()) {
            MakeConfigurationDescriptor configurationDescriptor = cdp.getConfigurationDescriptor();
            MakeConfiguration activeConfiguration = configurationDescriptor.getActiveConfiguration();
            if (activeConfiguration != null) {
                CodeAssistanceConfiguration codeAssistanceConfiguration = activeConfiguration.getCodeAssistanceConfiguration();
                if (codeAssistanceConfiguration != null) {
                    VectorConfiguration<String> codeAssisyancVars = codeAssistanceConfiguration.getEnvironmentVariables();
                    if (codeAssisyancVars != null && !codeAssisyancVars.getValue().isEmpty()) {
                        ExecutionEnvironment ee = activeConfiguration.getDevelopmentHost().getExecutionEnvironment();
                        HostInfo hostInfo = null;                        
                        if (HostInfoUtils.isHostInfoAvailable(ee)) {
                            try {
                                hostInfo = HostInfoUtils.getHostInfo(ee);
                            } catch (IOException | ConnectionManager.CancellationException ex) {
                                Exceptions.printStackTrace(ex); // should never occur after isHostInfoAvailable check => report
                            }
                        }
                        List<String> res = new ArrayList<>();
                        for (String var : codeAssisyancVars.getValue()) {
                            String value = (hostInfo == null) ? null : hostInfo.getEnvironment().get(var);
                            if (value == null) {
                                if (!TempEnv.getInstance(ee).isTemporaryEnvSet(var)) {
                                    res.add(var);
                                }
                            }
                        }
                        return res.isEmpty() ? null : new UnsetEnvVar(res, ee);
                    }
                }
            }
        }
        return null;
    }

    public static final class UnsetEnvVar {

        private final Collection<String> undefinedEnvVars;
        private final Map<String, String> edit = new HashMap<>();
        private final ExecutionEnvironment ee;
        private UnsetEnvVar(Collection<String> undefinedEnvVars, ExecutionEnvironment ee) {
            this.undefinedEnvVars = undefinedEnvVars;
            this.ee = ee;
        }

        public Collection<String> getUndefinedEnvVars() {
            return undefinedEnvVars;
        }

        public ExecutionEnvironment getExecutionEnvironment() {
            return ee;
        }

        public void editValue(String key, String val) {
            edit.put(key, val);
        }
    }
    
    
    private static class EnvResolverImpl extends BrokenReferencesSupport.BaseProjectProblemResolver {

        private final UnsetEnvVar unset;

        private EnvResolverImpl(MakeProject project, UnsetEnvVar unset) {
            super(project);
            this.unset = unset;
        }

        @Override
        public Future<ProjectProblemsProvider.Result> resolve() {
            unset.edit.clear();
            ResolveEnvVarPanel panel = new ResolveEnvVarPanel(unset);
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ResolveEnvVarPanel.class, "env_var_fix_title"));
            dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
            if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd))) {
                boolean success = true;
                boolean changed = false; // they might have restored from prev sessions values
                for(String var : unset.getUndefinedEnvVars()) {
                    String val = unset.edit.get(var);
                    if (val != null && !val.trim().isEmpty()) {
                        ExecutionEnvironment env = unset.getExecutionEnvironment();
                        String oldVal = TempEnv.getInstance(env).getTemporaryEnv(var);
                        String newVal = val.trim();                        
                        // TODO: HostInfo().getEnvironment() is not modifieble. There is no way to set additional env directly in the HostInfo.
                        // It would be nice if native execution provides such service
                        //unset.getHostInfo().getEnvironment().put(var, val.trim());
                        // As work around keep env in temporary map.
                        TempEnv.getInstance(env).setTemporaryEnv(var, newVal);
                        // we need to call setTemporaryEnv even if the value stays the same,
                        // in order to set "explicit" flag;
                        // but fire property change only if it really changed
                        if (oldVal == null || !oldVal.trim().equals(newVal)) {
                            changed = true;
                        }
                    } else {
                        success = false;
                    }
                }
                
                if (success) {
                    updateProblems();
                    if (changed) {
                        NativeProject nativeProject = getProject().getLookup().lookup(NativeProject.class);
                        if (nativeProject != null) {
                            nativeProject.fireFilesPropertiesChanged();
                        }
                    }
                    return new BrokenReferencesSupport.Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
                }
            }
            return new BrokenReferencesSupport.Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
        }
        
        @Override
        public int hashCode() {
            return this.getProject().hashCode() + EnvResolverImpl.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EnvResolverImpl other = (EnvResolverImpl) obj;
            return this.getProject().equals(other.getProject());
        }
    }
    
    private static class ConnListener extends ConnectionNotifier.NamedRunnable {

        private final WeakReference<MakeProject> projectRef;
        private final AtomicReference<UnsetEnvVar> unsetRef;

        public ConnListener(MakeProject project, AtomicReference<UnsetEnvVar> unset) {
            super(NbBundle.getMessage(ConnListener.class, "EnvResolver_connection_text", getExecEnv(unset)));
            this.unsetRef = unset;
            this.projectRef = new WeakReference<>(project);
        }
        
        private static ExecutionEnvironment getExecEnv(AtomicReference<UnsetEnvVar> unsetRef) {
            UnsetEnvVar unset = unsetRef.get();
            return (unset == null) ? ExecutionEnvironmentFactory.getLocal() : unset.getExecutionEnvironment();
        }
        
        @Override
        protected void runImpl() {
            MakeProject project = projectRef.get();
            if (project == null) {
                return;
            }
            if (!OpenProjects.getDefault().isProjectOpen(project)) {
                return;
            }
//            UnsetEnvVar unset = getUndefinedEnvVars(project);
//            if (unset == null || unset.getUndefinedEnvVars().isEmpty()) {
//                return;
//            }
            UnsetEnvVar unset = unsetRef.get();
            if (unset == null) {
                return;
            }
            ExecutionEnvironment execEnv = unset.getExecutionEnvironment();
            if (!HostInfoUtils.isHostInfoAvailable(execEnv)) {
                CndUtils.assertTrueInConsole(false, "Host unavailable?!"); //NOI18N
                return;
            }
            HostInfo hostInfo = null;
            try {
                hostInfo = HostInfoUtils.getHostInfo(execEnv);
            } catch (IOException | ConnectionManager.CancellationException ex) {
                Exceptions.printStackTrace(ex); // should be available! => report
            }
            if (hostInfo == null) {
                return;
            }
            Map<String, String> envMap = hostInfo.getEnvironment();
            boolean changed = false;
            for (String var : unset.getUndefinedEnvVars()) {
                String value = envMap.get(var);
                if (value != null) {
                    String tempEnv = TempEnv.getInstance(execEnv).getTemporaryEnv(var);
                    if (!value.equals(tempEnv)) {
                        changed = true;
                        TempEnv.getInstance(execEnv).setTemporaryEnv(var, value);
                    }
                }
            }
            if (changed) {
                NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
                if (nativeProject != null) {
                    nativeProject.fireFilesPropertiesChanged();
                }                
            }
            BrokenReferencesSupport.updateProblems(project);
        }
    }    
}
