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
package org.netbeans.modules.web.clientproject.validation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Validator for project running.
 */
public final class RunProjectValidator {

    public static final String SITE_ROOT = "site.root"; // NOI18N
    public static final String START_FILE = "start.file"; // NOI18N
    public static final String PROJECT_URL = "project.url"; // NOI18N

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }

    public RunProjectValidator validate(ClientSideProject project, boolean validateStartFile) {
        if (validateStartFile) {
            String startFile = ClientSideProjectUtilities.splitPathAndFragment(project.getStartFile())[0];
            File siteRoot = getSiteRoot(project);
            validateStartFile(getSiteRoot(project), resolveFile(siteRoot, startFile));
        }
        if (!project.isUsingEmbeddedServer()) {
            validateProjectUrl(project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_PROJECT_URL));
        }
        return this;
    }

    @NbBundle.Messages({
        "RunProjectValidator.error.siteRoot.none=No Site Root set in Sources category",
        "RunProjectValidator.error.startFile.invalid=Start File must be a valid file.",
        "RunProjectValidator.error.startFile.notUnderSiteRoot=Start File must be underneath Site Root directory."
    })
    public RunProjectValidator validateStartFile(File siteRootFolder, File startFile) {
        if (siteRootFolder == null) {
            result.addWarning(new ValidationResult.Message(SITE_ROOT, Bundle.RunProjectValidator_error_siteRoot_none()));
            return this;
        }
        ValidationResult foldersResult = new ProjectFoldersValidator()
                .validateSiteRootFolder(siteRootFolder)
                .getResult();
        result.merge(foldersResult);
        if (result.hasErrors()) {
            return this;
        }
        if (startFile == null || !startFile.exists()) {
            result.addWarning(new ValidationResult.Message(START_FILE, Bundle.RunProjectValidator_error_startFile_invalid()));
            return this;
        }
        if (!ClientSideProjectUtilities.isParentOrItself(FileUtil.toFileObject(siteRootFolder), FileUtil.toFileObject(startFile))) {
            result.addWarning(new ValidationResult.Message(START_FILE, Bundle.RunProjectValidator_error_startFile_notUnderSiteRoot()));
        }
        return this;
    }

    @NbBundle.Messages({
        "RunProjectValidator.error.projectUrl.missing=Project URL is missing.",
        "RunProjectValidator.error.projectUrl.invalidProtocol=Project URL must start with http(s):// or file://.",
        "RunProjectValidator.error.projectUrl.invalid=Project URL is invalid."
    })
    public RunProjectValidator validateProjectUrl(String projectUrl) {
        if (projectUrl == null || projectUrl.isEmpty()) {
            result.addWarning(new ValidationResult.Message(PROJECT_URL, Bundle.RunProjectValidator_error_projectUrl_missing()));
            return this;
        }
        if (!projectUrl.startsWith("http://") // NOI18N
                && !projectUrl.startsWith("https://") // NOI18N
                && !projectUrl.startsWith("file://")) { // NOI18N
            result.addWarning(new ValidationResult.Message(PROJECT_URL, Bundle.RunProjectValidator_error_projectUrl_invalidProtocol()));
            return this;
        }
        try {
            URL url = new URL(projectUrl);
            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                result.addWarning(new ValidationResult.Message(PROJECT_URL, Bundle.RunProjectValidator_error_projectUrl_invalid()));
            }
        } catch (MalformedURLException ex) {
            result.addWarning(new ValidationResult.Message(PROJECT_URL, Bundle.RunProjectValidator_error_projectUrl_invalid()));
        }
        return this;
    }

    @CheckForNull
    private File getSiteRoot(ClientSideProject project) {
        FileObject siteRootFolder = project.getSiteRootFolder();
        if (siteRootFolder == null) {
            return null;
        }
        return FileUtil.toFile(siteRootFolder);
    }

    @CheckForNull
    private File resolveFile(@NullAllowed File root, String child) {
        if (root == null) {
            return null;
        }
        if (child.isEmpty()) {
            return root;
        }
        return new File(root, child);
    }

}
