/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grails.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Petr Hejl
 */
public final class GrailsInstanceProvider implements ServerInstanceProvider {

    private static final ExecutorService PROCESS_EXECUTOR = Executors.newCachedThreadPool();

    private static GrailsInstanceProvider instance;

    private final Map<Process, Project> running = new HashMap<Process, Project>();

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // instance for single runtime - when more runtimes allowed this need to be changed
    private GrailsInstance grailsInstance;

    private GrailsInstanceProvider() {
        super();
    }

    public static synchronized GrailsInstanceProvider getInstance() {
        if (instance == null) {
            instance = new GrailsInstanceProvider();
            instance.grailsInstance = GrailsInstance.forProvider(instance);
        }
        return instance;
    }

    public List<ServerInstance> getInstances() {
        if (!GrailsPlatform.getDefault().isConfigured()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(ServerInstanceFactory.createServerInstance(grailsInstance));
    }

    public Map<Process, Project> getRunningProjects() {
        synchronized (this) {
            return new HashMap<Process, Project>(running);
        }
    }

    public void serverStarted(Project project, Process process) {
        assert process != null;

        synchronized (this) {
            running.put(process, project);
            PROCESS_EXECUTOR.submit(new ProcessHandler(this, process));
        }
        grailsInstance.refreshChildren();
    }

    public void serverStopped(Process process) {
        synchronized (this) {
            running.remove(process);
        }
        grailsInstance.refreshChildren();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void runtimeChanged() {
        synchronized (this) {
            // FIXME do we really want this
            // TODO we should go through open projects and check the grails
            // version and server state maybe
            running.clear();
        }
        grailsInstance.refreshNode();
        grailsInstance.refreshChildren();
        changeSupport.fireChange();
    }

    private static class ProcessHandler implements Runnable {

        private final GrailsInstanceProvider provider;

        private final Process serverProcess;

        public ProcessHandler(GrailsInstanceProvider provider, Process serverProcess) {
            this.provider = provider;
            this.serverProcess = serverProcess;
        }

        public void run() {
            try {
                serverProcess.waitFor();
            } catch (InterruptedException ex) {
                serverProcess.destroy();
            } finally {
                provider.serverStopped(serverProcess);
            }
        }

    }
}
