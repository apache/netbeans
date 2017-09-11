/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
