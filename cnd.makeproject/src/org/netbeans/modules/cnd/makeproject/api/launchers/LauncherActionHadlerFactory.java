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
package org.netbeans.modules.cnd.makeproject.api.launchers;

import java.util.Collection;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandlerFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.InputOutput;

/**
 *
 */

@ServiceProvider(service = ProjectActionHandlerFactory.class, position = 0)
public final class LauncherActionHadlerFactory implements ProjectActionHandlerFactory {
    private ExecutionListener listener;

    @Override
    public boolean canHandle(ProjectActionEvent.Type type, Lookup context, Configuration configuration) {
        return false;
    }

    @Override
    public boolean canHandle(ProjectActionEvent pae) {
        Lookup context = pae.getContext();
        ExecutionListener el = context.lookup(ExecutionListener.class);
        if (el != null) {
            this.listener = el;
            return true;
        }
        return false;
    }

    @Override
    public ProjectActionHandler createHandler() {
        ProjectActionHandler handler = new LauncherActionHandler(listener);
        return handler;
    }
    
    /*package*/static ProjectActionHandler createDelegateHandler(ProjectActionEvent pae) {
        boolean selfFound = false;
        for (ProjectActionHandlerFactory factory : Lookup.getDefault().lookupAll(ProjectActionHandlerFactory.class)) {
            if (factory instanceof LauncherActionHadlerFactory) {
                selfFound = true;
            } else if (selfFound) {
                if (factory.canHandle(pae)) {
                    return factory.createHandler();
                }
            }
        }
        return null;
    }

    private static class LauncherActionHandler implements ProjectActionHandler {
        private ProjectActionHandler delegate;
        private final ExecutionListener listener;

        public LauncherActionHandler(ExecutionListener listener) {
            this.listener = listener;
        }

        @Override
        public void init(ProjectActionEvent pae, ProjectActionEvent[] paes, Collection<BuildActionsProvider.OutputStreamHandler> outputHandlers) {
            this.delegate = LauncherActionHadlerFactory.createDelegateHandler(pae);
            this.delegate.init(pae, paes, outputHandlers);
            this.delegate.addExecutionListener(listener);
        }

        @Override
        public void execute(InputOutput io) {
            delegate.execute(io);
        }

        @Override
        public boolean canCancel() {
            return delegate.canCancel();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public void addExecutionListener(ExecutionListener l) {
            delegate.addExecutionListener(l);
        }

        @Override
        public void removeExecutionListener(ExecutionListener l) {
            delegate.removeExecutionListener(l);
        }
    }
}