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
package org.netbeans.modules.web.clientproject.browser;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowserFeatures;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.api.WebServer;
import org.netbeans.modules.web.clientproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.clientproject.validation.RunProjectValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Pair;

public class BrowserActionProvider implements ActionProvider {

    final private ClientSideProject project;
    private final BrowserSupport support;
    private ClientProjectEnhancedBrowserImpl cfg;

    public BrowserActionProvider(ClientSideProject project, BrowserSupport support, ClientProjectEnhancedBrowserImpl cfg) {
        this.project = project;
        this.support = support;
        this.cfg = cfg;
    }

    @Override
    public String[] getSupportedActions() {
        return new String[] {COMMAND_RUN, COMMAND_RUN_SINGLE};
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (project.isUsingEmbeddedServer()) {
            WebServer.getWebserver().start(project, project.getSiteRootFolder(), project.getWebContextRoot());
        } else {
            WebServer.getWebserver().stop(project);
        }
        if (COMMAND_RUN.equals(command)) {
            if (!validateRun(true)) {
                return;
            }
            Pair<FileObject, String> startFileWithFragment = getStartFileWithFragment();
            if (startFileWithFragment == null) {
                return;
            }
            FileObject fo = startFileWithFragment.first();
            String fragment = startFileWithFragment.second();
            if (fo == null) {
                DialogDisplayer.getDefault().notify(
                    new DialogDescriptor.Message(
                        org.openide.util.NbBundle.getMessage(BrowserActionProvider.class, "MAIN_FILE", project.getStartFile())));
                CustomizerProviderImpl cust = project.getLookup().lookup(CustomizerProviderImpl.class);
                cust.showCustomizer(CompositePanelProviderImpl.RUN);
                // try again:
                startFileWithFragment = getStartFileWithFragment();
                if (startFileWithFragment == null) {
                    return;
                }
                fo = startFileWithFragment.first();
                fragment = startFileWithFragment.second();
                if (fo == null) {
                    return;
                }
            }
            browseFile(support, fo, fragment);
        } else if (COMMAND_RUN_SINGLE.equals(command)) {
            if (!validateRun(false)) {
                return;
            }
            FileObject fo = getFile(context);
            if (fo != null) {
                browseFile(support, fo);
            }
        }
    }

    @CheckForNull
    private Pair<FileObject, String> getStartFileWithFragment() {
        FileObject siteRoot = project.getSiteRootFolder();
        if (siteRoot == null) {
            ProjectProblems.showAlert(project);
            return null;
        }
        String[] parts = ClientSideProjectUtilities.splitPathAndFragment(project.getStartFile());
        return Pair.of(siteRoot.getFileObject(parts[0]), parts[1]);
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return true;
    }

    private FileObject getFile(Lookup context) {
        return context.lookup(FileObject.class);
    }

    private void browseFile(BrowserSupport bs, FileObject fo) {
        browseFile(bs, fo, "");
    }

    private void browseFile(BrowserSupport bs, FileObject fo, String fragment) {
        URL url;
        if (ClientSideProjectUtilities.isParentOrItself(project.getSiteRootFolder(), fo)) {
            url = ServerURLMapping.toServer(project, fo);
            if (fragment.length() > 0) {
                url = WebUtils.stringToUrl(WebUtils.urlToString(url)+fragment);
            }
            bs.load(url, fo);
        } else {
            url = fo.toURL();
            WebBrowser wb = project.getProjectWebBrowser();
            WebBrowserFeatures features = new WebBrowserFeatures(false, false, false, false, false, false);
            wb.createNewBrowserPane(features).showURL(url);
        }
    }

    private boolean validateRun(boolean validateStartFile) {
        ValidationResult result = new RunProjectValidator()
                .validate(project, validateStartFile)
                .getResult();
        boolean errors = result.hasErrors()
                || result.hasWarnings();
        if (errors) {
            CustomizerProviderImpl cust = project.getLookup().lookup(CustomizerProviderImpl.class);
            cust.showCustomizer(CompositePanelProviderImpl.RUN);
        }
        return !errors;
    }

}
