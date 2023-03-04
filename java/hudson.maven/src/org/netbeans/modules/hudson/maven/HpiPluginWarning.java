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

package org.netbeans.modules.hudson.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.modules.ModuleInfo;
import static org.netbeans.modules.hudson.maven.Bundle.*;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Warns you about Hudson plugins that now need an external plugin.
 */
@ProjectServiceProvider(service=ProjectProblemsProvider.class, projectType={"org-netbeans-modules-maven/hpi", "org-netbeans-modules-maven/jenkins-module"})
public class HpiPluginWarning implements ProjectProblemsProvider, PropertyChangeListener, LookupListener {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Lookup.Result<ModuleInfo> modules = Lookup.getDefault().lookupResult(ModuleInfo.class);
    {
        modules.addLookupListener(WeakListeners.create(LookupListener.class, this, modules));
    }

    @Messages({
        "HpiPluginWarning_problem_displayName=Missing Hudson/Jenkins plugin support",
        "HpiPluginWarning_problem_description=Hudson/Jenkins plugin development support was removed from NetBeans 7.3. Install https://github.com/stapler/netbeans-stapler-plugin (available from Plugin Portal).",
        "HpiPluginWarning_unresolved=Automated installation from Plugin Portal not yet implemented; install the “Jenkins Plugin Support” and “Stapler Support” plugins."
    })
    @Override public Collection<? extends ProjectProblem> getProblems() {
        for (ModuleInfo mi : modules.allInstances()) {
            if (mi.getCodeNameBase().equals("org.kohsuke.stapler.netbeans.jenkinsdev")) {
                if (mi.isEnabled()) {
                    return Collections.emptySet();
                } else {
                    mi.addPropertyChangeListener(WeakListeners.propertyChange(this, mi));
                    // XXX better to display a specialized warning
                    continue;
                }
            }
        }
        return Collections.singleton(ProjectProblem.createWarning(HpiPluginWarning_problem_displayName(), HpiPluginWarning_problem_description(), new ProjectProblemResolverImpl()));
    }
    
    @Override public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        pcs.firePropertyChange(PROP_PROBLEMS, null, null);
    }

    @Override public void resultChanged(LookupEvent ev) {
        pcs.firePropertyChange(PROP_PROBLEMS, null, null);
    }

    private static class ProjectProblemResolverImpl implements ProjectProblemResolver {

        
        public ProjectProblemResolverImpl() {
        }

        @Override public Future<Result> resolve() {
            return RequestProcessor.getDefault().submit(new Callable<Result>() {
                @Override public Result call() throws Exception {
                    return Result.create(Status.UNRESOLVED, HpiPluginWarning_unresolved());
                }
            });
        }
        

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + 1234567;
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
            return true;
        }
        
    }

}
