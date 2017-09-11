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
