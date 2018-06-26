/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.codeception.preferences;

import java.io.File;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.codeception.util.CodeceptionUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class CodeceptionPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    public CodeceptionPreferencesValidator validate(PhpModule phpModule) {
        validateCodecept(CodeceptionPreferences.isCustomCodeceptEnabled(phpModule), CodeceptionPreferences.getCustomCodeceptPath(phpModule));
        return this;
    }

    public CodeceptionPreferencesValidator validateCodecept(boolean codeceptEnabled, @NullAllowed String codeceptPath) {
        if (codeceptEnabled) {
            String warning = CodeceptionUtils.validateCodeceptPath(codeceptPath);
            if (warning != null) {
                result.addWarning(new ValidationResult.Message("codeceptPath", warning)); // NOI18N
            }
        }
        return this;
    }

    @NbBundle.Messages({
        "CodeceptionPreferencesValidator.incorrect.codeception.yml.fileName=codeception.yml or codeception.dist.yml must be set.",
        "CodeceptionPreferencesValidator.valid.codeception.yml.fileNames=codeception.yml or codeception.dist.yml"
    })
    public CodeceptionPreferencesValidator validateCodeceptionYml(boolean codeceptionYmlEnabled, @NullAllowed String codeceptionYmlPath) {
        if (codeceptionYmlEnabled) {
            validatePath(codeceptionYmlEnabled, codeceptionYmlPath, Bundle.CodeceptionPreferencesValidator_valid_codeception_yml_fileNames(), "codeceptionYmlPath"); // NOI18N
            if (codeceptionYmlPath != null) {
                File file = new File(codeceptionYmlPath);
                if (file.exists()) {
                    String fileName = FileUtil.toFileObject(file).getNameExt();
                    if (!Codecept.CODECEPTION_CONFIG_FILE_NAME.equals(fileName)
                            && !Codecept.CODECEPTION_DIST_CONFIG_FILE_NAME.equals(fileName)) {
                        result.addWarning(new ValidationResult.Message("codeceptionYmlPath", // NOI18N
                                Bundle.CodeceptionPreferencesValidator_incorrect_codeception_yml_fileName()));
                    }
                }
            }
        }
        return this;
    }

    private void validatePath(boolean pathEnabled, String path, String label, String source) {
        if (!pathEnabled) {
            return;
        }
        String warning = FileUtils.validateFile(label, path, false);
        if (warning != null) {
            result.addError(new ValidationResult.Message(source, warning));
        }
    }

    public ValidationResult getResult() {
        return result;
    }

}
