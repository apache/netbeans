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
