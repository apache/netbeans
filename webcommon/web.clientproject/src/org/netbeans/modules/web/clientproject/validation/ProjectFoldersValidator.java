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
import org.netbeans.modules.web.clientproject.api.validation.FolderValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.util.NbBundle;

/**
 * Validator for project sources.
 */
public final class ProjectFoldersValidator {

    public static final String SOURCE_FOLDER = "source.folder"; // NOI18N
    public static final String SITE_ROOT_FOLDER = "site.root.folder"; // NOI18N
    public static final String SOURCE_OR_SITE_ROOT_FOLDER = "source.or.site.root.folder"; // NOI18N
    public static final String TEST_FOLDER = "test.folder"; // NOI18N
    public static final String TEST_SELENIUM_FOLDER = "test.selenium.folder"; // NOI18N

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }

    public ProjectFoldersValidator validate(File sourceFolder, File siteRootFolder, File testFolder, File testSeleniumFolder) {
        validateSourceFolder(sourceFolder);
        validateSiteRootFolder(siteRootFolder);
        validateSourceAndSiteRootFolders(sourceFolder, siteRootFolder);
        validateTestFolder(testFolder);
        validateTestSeleniumFolder(testSeleniumFolder);
        return this;
    }

    @NbBundle.Messages("ProjectFoldersValidator.sources=Sources")
    public ProjectFoldersValidator validateSourceFolder(File sourceFolder) {
        return validateProjectFolder(sourceFolder, SOURCE_FOLDER, Bundle.ProjectFoldersValidator_sources());
    }

    @NbBundle.Messages("ProjectFoldersValidator.siteRoot=Site Root")
    public ProjectFoldersValidator validateSiteRootFolder(File siteRootFolder) {
        return validateProjectFolder(siteRootFolder, SITE_ROOT_FOLDER, Bundle.ProjectFoldersValidator_siteRoot());
    }

    @NbBundle.Messages({
        "ProjectFoldersValidator.error.noSourcesOrSiteRoot=Source or Site Root directory must be specified.",
        "ProjectFoldersValidator.error.sourcesEqualsSiteRoot=Source directory and Site Root directory are the same.",
        "ProjectFoldersValidator.error.sourcesUnderneathSiteRoot=Source directory is underneath Site Root directory.",
    })
    public ProjectFoldersValidator validateSourceAndSiteRootFolders(File sourceFolder, File siteRootFolder) {
        if (sourceFolder == null
                && siteRootFolder == null) {
            result.addError(new ValidationResult.Message(SOURCE_OR_SITE_ROOT_FOLDER, Bundle.ProjectFoldersValidator_error_noSourcesOrSiteRoot()));
        } else if (sourceFolder != null
                && siteRootFolder != null) {
            if (siteRootFolder.equals(sourceFolder)) {
                result.addWarning(new ValidationResult.Message(SOURCE_FOLDER, Bundle.ProjectFoldersValidator_error_sourcesEqualsSiteRoot()));
            } else {
                File parent = sourceFolder.getParentFile();
                while (parent != null) {
                    if (parent.equals(siteRootFolder)) {
                        result.addWarning(new ValidationResult.Message(SOURCE_FOLDER, Bundle.ProjectFoldersValidator_error_sourcesUnderneathSiteRoot()));
                        break;
                    }
                    parent = parent.getParentFile();
                }
            }
        }
        return this;
    }

    @NbBundle.Messages("ProjectFoldersValidator.tests=Unit Tests")
    ProjectFoldersValidator validateTestFolder(File testFolder) {
        return validateProjectFolder(testFolder, TEST_FOLDER, Bundle.ProjectFoldersValidator_tests());
    }

    @NbBundle.Messages("ProjectFoldersValidator.tests.selenium=Selenium Tests")
    ProjectFoldersValidator validateTestSeleniumFolder(File testSeleniumFolder) {
        return validateProjectFolder(testSeleniumFolder, TEST_SELENIUM_FOLDER, Bundle.ProjectFoldersValidator_tests_selenium());
    }

    private ProjectFoldersValidator validateProjectFolder(File folder, String source, String dirName) {
        if (folder == null) {
            // can be empty
            return this;
        }
        ValidationResult folderValidationResult = new FolderValidator()
                .validateFolder(folder, dirName)
                .getResult();
        for (ValidationResult.Message error : folderValidationResult.getErrors()) {
            result.addError(new ValidationResult.Message(source, error.getMessage()));
        }
        for (ValidationResult.Message warning : folderValidationResult.getWarnings()) {
            result.addWarning(new ValidationResult.Message(source, warning.getMessage()));
        }
        return this;
    }

}
