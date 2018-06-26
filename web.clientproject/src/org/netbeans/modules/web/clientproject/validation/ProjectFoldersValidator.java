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
