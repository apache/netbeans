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
package org.netbeans.modules.gradle.problems;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider.Result;
import org.openide.util.EditableProperties;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Adds, modifies or removes proxy properties from gradle's settings file. If found in some of the existing files, the
 * Resolver modifies that property file. If the properties are nowehere to be found (and are necessary), the Resolver
 * adds them to the user properties file ({@code ~/.gradle/gradle.properties}).
 * 
 * @author sdedic
 */
public class ChangeOrRemovePropertyResolver implements ProjectProblemResolver {
    private static final Logger LOG = Logger.getLogger(ChangeOrRemovePropertyResolver.class.getName());
    private static final RequestProcessor EDIT_RP = new RequestProcessor(ChangeOrRemovePropertyResolver.class);
    
    private final Project project;
    private final String proxyHost;
    private final PropertiesEditor editor;
    private final int proxyPort;
    private final CompletableFuture<ProjectProblemsProvider.Result> future = new CompletableFuture<>();
    private final NbGradleProject gp;
    
    public ChangeOrRemovePropertyResolver(Project project, PropertiesEditor editor, String proxyHost, int proxyPort) {
        this.project = project;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.editor = editor;
        gp = NbGradleProject.get(project);
    }

    @Override
    public Future<ProjectProblemsProvider.Result> resolve() {
        EDIT_RP.post(() -> run());
        return future;
    }
    
    @NbBundle.Messages({
        "# {0} - properties file name",
        "# {1} - reported error message",
        "Error_UpdatePropertiesError=Error updating properties file {0}: {1}",
        "Error_ProxySettingNotFound=Proxy settings not found.",
        "# {0} - properties file name",
        "Error_LoadingUserProperties=Could not load user properties file ({0}).",
        "ReasonProxyChanged=Gradle proxies have changed"
    })
    public void run() {
        boolean reload = doRun();
        if (reload) {
            gp.toQuality(Bundle.ReasonProxyChanged(), NbGradleProject.Quality.FULL, true);
        }
    }
    
    boolean doRun() {
        if (editor == null) {
            // should not happen
            return false;
        }
        EditableProperties p;
        try {
             p = editor.open();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Could not load properties: {0}", editor.getFilePath());
            LOG.log(Level.WARNING, "Error reported", ex);

            future.complete(Result.create(ProjectProblemsProvider.Status.UNRESOLVED, Bundle.Error_UpdatePropertiesError(editor.getFilePath(), ex.getLocalizedMessage())));
            return false;
        }

        boolean updated = updateProperties(p);
        if (!updated) {
            if (proxyHost == null) {
                // there were no properties, still the error happened ??
                future.complete(Result.create(ProjectProblemsProvider.Status.UNRESOLVED, Bundle.Error_ProxySettingNotFound()));
                return false;
            }

            p.setProperty("systemProp.http.proxyHost", proxyHost);
            p.setProperty("systemProp.http.proxyPort", Integer.toString(proxyPort));
            p.setProperty("systemProp.https.proxyHost", proxyHost);
            p.setProperty("systemProp.https.proxyPort", Integer.toString(proxyPort));
        }
        try {
            editor.save();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Could not save updated properties: {0}", editor.getFilePath());
            LOG.log(Level.WARNING, "Error reported", ex);
            future.complete(Result.create(ProjectProblemsProvider.Status.UNRESOLVED, Bundle.Error_UpdatePropertiesError(editor.getFilePath(), ex.getLocalizedMessage())));
            return false;
        }
        future.complete(Result.create(ProjectProblemsProvider.Status.RESOLVED));
        return true;
    }
    
    private boolean updateProperties(EditableProperties props) {
        boolean b = changeProperty(props, "systemProp.http.proxyHost"); // NOI18N
        b |= changeProperty(props, "systemProp.https.proxyHost"); // NOI18N
        b |= changeProperty(props, "systemProp.socks.proxyHost"); // NOI18N
        return b;
    }
    
    private boolean changeProperty(EditableProperties props, String k) {
        String s = props.getProperty(k);
        if (s == null) {
            return false;
        }
        if (proxyHost != null) {
            props.setProperty(k, proxyHost);
            props.setProperty(k.replace("Host", "Port"), Integer.toString(proxyPort));
        } else {
            props.remove(k);
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.project);
        hash = 83 * hash + Objects.hashCode(this.proxyHost);
        hash = 83 * hash + this.proxyPort;
        hash = 83 * hash + this.editor.getFilePath().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChangeOrRemovePropertyResolver other = (ChangeOrRemovePropertyResolver) obj;
        if (this.proxyPort != other.proxyPort) {
            return false;
        }
        if (!Objects.equals(this.proxyHost, other.proxyHost)) {
            return false;
        }
        return Objects.equals(this.project, other.project);
    }
}
