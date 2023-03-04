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

package org.netbeans.modules.maven.profiler;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher.Launcher;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher.Session;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.windows.InputOutput;

/**
 *
 * @author mkleint
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=LateBoundPrerequisitesChecker.class, projectType="org-netbeans-modules-maven")
public class RunCheckerImpl implements LateBoundPrerequisitesChecker {

    private static final Logger LOG = Logger.getLogger(RunCheckerImpl.class.getName());
    
    private static final String VM_ARGS = "vm.args"; // NOI18N
    private static final String PROFILER_ARGS = "${profiler.args}"; // NOI18N
//    private static final String PROFILER_ARGS_PREFIXED = "${profiler.args.prefixed}"; // NOI18N
//    private static final String EXEC_EXECUTABLE = "exec.executable"; // NOI18N
//    private static final String PROFILER_JAVA = "${profiler.java}"; // NOI18N
//    private static final String PROFILER_JDKHOME_OPT = "${profiler.jdkhome.opt}"; // NOI18N
    
    @ProjectServiceProvider(service=ProfilerLauncher.LauncherFactory.class, projectType="org-netbeans-modules-maven")
    public static final class MavenLauncherFactory implements ProfilerLauncher.LauncherFactory {
        @Override
        public Launcher createLauncher(final Session session) {
            return new Launcher() {

                @Override
                public void launch(boolean rerun) {
                    if (rerun) {
                        RunConfig config = (RunConfig)session.getAttribute("mvn-run-checker.config");
                        if (config != null) {
                            RunUtils.executeMaven(config);
                        }
                    } else {
                        Project p = session.getProject();
                        if (p == null) {
                            FileObject f = session.getFile();
                            if (f != null) {
                                p = FileOwnerQuery.getOwner(f);
                            }
                        }
                        if (p != null) {
                            ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
                            if (ap != null) {
                                ap.invokeAction(session.getCommand(), session.getContext());
                            }
                        }
                    }
                }
            };
        }
        
    }
    
    @Override
    public boolean checkRunConfig(final RunConfig config, ExecutionContext context) {
        Map<? extends String,? extends String> configProperties = config.getProperties();
        final String actionName = config.getActionName();
        
        if (ActionProvider.COMMAND_PROFILE.equals(actionName) ||
               ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(actionName) ||
              (actionName != null && actionName.startsWith(ActionProvider.COMMAND_PROFILE_SINGLE))) {
            
            ProfilerLauncher.Session session = ProfilerLauncher.getLastSession();
            
            if (session == null) {
                closeInputOuptut(context);
                return false;
            }
                       
            Map<String, String> sProps = session.getProperties();
            if (sProps == null) {
                closeInputOuptut(context);
                return false;
            }
            
            session.setAttribute("mvn-run-checker.config", config);
             
            final String agentArg = sProps.get("agent.jvmargs"); // NOI18N
            final String internalProfilerArgs = sProps.get("profiler.info.jvmargs") // NOI18N
                                + " " + agentArg; // NOI18N 

            final String prefixPublicArgs = "profiler.jvmargs"; // NOI18N
            config.setProperty(prefixPublicArgs+".all",internalProfilerArgs); // NOI18N
            for (Map.Entry<? extends String, ? extends String> entry : config.getProperties().entrySet()) {
                if (entry.getKey().equals(VM_ARGS)) {
                    String value = entry.getValue();
                    int index = value.indexOf(PROFILER_ARGS);
                    if(index > -1) {
                        value = value.replace(PROFILER_ARGS, internalProfilerArgs);
                        config.setProperty(entry.getKey(), value);
                    }
                }
            }
            // Make the profiler jvm args available as individual arguments.
            // Note: this assumes ordering doesn't matter.
            int idxArg = 0;
            for(Map.Entry<String, String> entry : sProps.entrySet()) {
                if(entry.getKey().startsWith("profiler.netbeansBindings.jvmarg.")) { // NOI18N
                    ++idxArg;
                    config.setProperty(prefixPublicArgs+".arg"+idxArg, entry.getValue()); // NOI18N
                }
            }
            final ProfilerLauncher.Session s = session;
            // Attach profiler engine (in separate thread) to profiled process
            if (!NetBeansProfiler.getDefaultNB().startEx(s.getProfilingSettings(), s.getSessionSettings(), new AtomicBoolean())) {
                return false;
            }
        }
        
        return true;
    }

    private void closeInputOuptut(ExecutionContext context) {
        InputOutput ioput = context.getInputOutput();
        if (ioput != null) {
            ioput.closeInputOutput();
        }
    }
}
