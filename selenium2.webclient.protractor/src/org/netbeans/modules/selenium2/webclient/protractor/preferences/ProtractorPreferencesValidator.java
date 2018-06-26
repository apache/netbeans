/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.protractor.preferences;

import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutableValidator;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
public class ProtractorPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }
    
    public ProtractorPreferencesValidator validate(Project project) {
        validateProtractor(ProtractorPreferences.getProtractor(project));
        validateUserConfigurationFile(project, ProtractorPreferences.getUserConfigurationFile(project));
        return this;
    }

    @NbBundle.Messages("ProtractorPreferencesValidator.protractor.name=Protractor")
    public ProtractorPreferencesValidator validateProtractor(String protractor) {
        String warning = ExternalExecutableValidator.validateCommand(protractor, Bundle.ProtractorPreferencesValidator_protractor_name());
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("path", warning)); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages("ProtractorPreferencesValidator.userConfigurationFile.name=Configuration File")
    public ProtractorPreferencesValidator validateUserConfigurationFile(Project project, String userConfigurationFile) {
        String warning = validateFile(project, Bundle.ProtractorPreferencesValidator_userConfigurationFile_name(), userConfigurationFile, false);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("configuration file", warning)); // NOI18N
        }
        return this;
    }
    
    /**
     * Validate a file path and return {@code null} if it is valid, otherwise an error.
     * <p>
     * A valid file means that the <tt>filePath</tt> represents a valid, readable file
     * with absolute file path.
     * @param source source used in error message (e.g. "Script", "Config file")
     * @param filePath a file path to validate
     * @param writable {@code true} if the file must be writable, {@code false} otherwise
     * @return {@code null} if it is valid, otherwise an error
     */
    @NbBundle.Messages({
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.missing={0} must be selected.",
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.notAbsolute={0} must be an absolute path.",
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.notFile={0} must be a valid file.",
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.notReadable={0} is not readable.",
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.notWritable={0} is not writable.",
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.notUnderProject={0} is not under project's directory."
    })
    @CheckForNull
    private static String validateFile(Project project, String source, String filePath, boolean writable) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return Bundle.ProtractorPreferencesValidator_validateFile_missing(source);
        }

        File file = new File(filePath);
        if (!file.isAbsolute()) {
            return Bundle.ProtractorPreferencesValidator_validateFile_notAbsolute(source);
        } else if (!file.isFile()) {
            return Bundle.ProtractorPreferencesValidator_validateFile_notFile(source);
        } else if (!file.canRead()) {
            return Bundle.ProtractorPreferencesValidator_validateFile_notReadable(source);
        } else if (writable && !file.canWrite()) {
            return Bundle.ProtractorPreferencesValidator_validateFile_notWritable(source);
        }
        if(project != null && !project.equals(FileOwnerQuery.getOwner(FileUtil.toFileObject(file)))) {
            return Bundle.ProtractorPreferencesValidator_validateFile_notUnderProject(source);
        }
        return null;
    }
    
}
