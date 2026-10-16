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
package org.netbeans.modules.payara.server.maven.project;

import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Lookup-registered wrapper that carries a {@link ServerMavenApplication}.
 * <p>
 * Detection is deferred to the first call of {@link #getApplication()} that
 * occurs after the Maven project model has fully loaded, so it is safe to call
 * from the Event Dispatch Thread without causing I/O hangs.
 * <p>
 * When a server-maven project is detected, a {@link ServerMavenIcon} is added
 * to {@link #getExtraLookup()} so the icon is only present in server-maven
 * project lookups — not in plain WAR or Micro project lookups.
 * <p>
 * If the Maven model has not yet loaded at construction time, a
 * {@link NbMavenProject#PROP_PROJECT} listener is registered to trigger
 * detection as soon as Maven finishes loading.
 *
 * @author Gaurav Gupta <gaurav.gupta@azul.com>
 */
public class ServerMavenApplicationContent {

    private final Project project;
    private volatile boolean initialized;
    private volatile ServerMavenApplication application;

    private final InstanceContent extraServices = new InstanceContent();
    private final Lookup extraLookup = new AbstractLookup(extraServices);

    private PropertyChangeListener mavenLoadListener;

    public ServerMavenApplicationContent(Project project) {
        this.project = project;
        NbMavenProject nbmp = project.getLookup().lookup(NbMavenProject.class);
        if (nbmp != null) {
            if (nbmp.isMavenProjectLoaded()) {
                getApplication();
            } else {
                mavenLoadListener = evt -> {
                    if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                        getApplication();
                        if (initialized) {
                            nbmp.removePropertyChangeListener(mavenLoadListener);
                            mavenLoadListener = null;
                        }
                    }
                };
                nbmp.addPropertyChangeListener(mavenLoadListener);
            }
        }
    }

    public Lookup getExtraLookup() {
        return extraLookup;
    }

    public ServerMavenApplication getApplication() {
        if (!initialized) {
            NbMavenProject nbmp = project.getLookup().lookup(NbMavenProject.class);
            if (nbmp != null && nbmp.isMavenProjectLoaded()) {
                synchronized (this) {
                    if (!initialized) {
                        if (ServerMavenApplication.isPayaraServerMavenProject(project)) {
                            application = new ServerMavenApplication(project);
                            ServerMavenIcon icon = new ServerMavenIcon();
                            icon.setProject(project);
                            extraServices.add(icon);
                            SwingUtilities.invokeLater(
                                    () -> NbMavenProject.fireMavenProjectReload(project));
                        }
                        initialized = true;
                    }
                }
            }
        }
        return application;
    }

    synchronized void setApplication(ServerMavenApplication application) {
        this.application = application;
        this.initialized = true;
    }
}
