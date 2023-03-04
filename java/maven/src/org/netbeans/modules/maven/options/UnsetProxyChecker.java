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

package org.netbeans.modules.maven.options;

import java.io.IOException;
import java.net.URI;
import org.apache.maven.repository.RepositorySystem;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import static org.netbeans.modules.maven.options.Bundle.*;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NetworkSettings;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

@ProjectServiceProvider(service=ExecutionResultChecker.class, projectType="org-netbeans-modules-maven")
public class UnsetProxyChecker implements ExecutionResultChecker { // #194916

    private static final String USE_SYSTEM_PROXIES = "-Djava.net.useSystemProxies=true";

    @Override public void executionResult(RunConfig config, ExecutionContext res, int resultCode) {
        if (resultCode == 0) {
            return; // build seemed to work, do not bother user
        }
        final String proxyHost = NetworkSettings.getProxyHost(URI.create(RepositorySystem.DEFAULT_REMOTE_REPO_URL));
        if (proxyHost == null) {
            return; // seem to be using a direct connection from the IDE
        }
        if (!EmbedderFactory.getProjectEmbedder().getSettings().getProxies().isEmpty()) {
            return; // user has somehow configured proxies manually, so trust them
        }
        if (MavenSettings.getDefault().getDefaultOptions().contains(USE_SYSTEM_PROXIES)) {
            return; // our suggestion was already accepted
        }
        try {
            res.getInputOutput().getOut().println(/* NOI18N - part of Maven output */"Check Maven network proxy...", new OutputListener() {
                @Override public void outputLineAction(OutputEvent ev) {
                    prompt(proxyHost);
                }
                @Override public void outputLineSelected(OutputEvent ev) {}
                @Override public void outputLineCleared(OutputEvent ev) {}
            });
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
        }
    }

    @Messages({
        "TITLE_unset_proxy=Configure Automatic Proxies",
        "# {0} - proxy host", "MSG_unset_proxy=<html>"
            + "The IDE appears to be using a network proxy (<code>{0}</code>) but Maven is not configured to use any.<br>"
            + "This can result in build failures in case remote artifacts need to be downloaded.<br>"
            + "In general you may need to edit the <code>&lt;proxies&gt;</code> section in <code>settings.xml</code>:<br>"
            + "http://wiki.netbeans.org/FaqMavenProxySettings<br>"
            + "As a shortcut that works <em>on some systems</em>, <code>" + USE_SYSTEM_PROXIES + "</code> can be passed to Maven.<br>"
            + "Try this now? (Remove it from Global Execution Options if it does not help.)"
    })
    private static void prompt(String proxyHost) {
        String opts = MavenSettings.getDefault().getDefaultOptions();
        if (opts.contains(USE_SYSTEM_PROXIES)) {
            return; // do nothing if clicked again after accepting
        }
        if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(MSG_unset_proxy(proxyHost), TITLE_unset_proxy(), NotifyDescriptor.OK_CANCEL_OPTION)) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        MavenSettings.getDefault().setDefaultOptions(opts.isEmpty() ? USE_SYSTEM_PROXIES : opts + ' ' + USE_SYSTEM_PROXIES);
    }

}
