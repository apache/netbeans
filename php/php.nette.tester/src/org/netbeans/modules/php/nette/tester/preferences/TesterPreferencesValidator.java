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

package org.netbeans.modules.php.nette.tester.preferences;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.nette.tester.util.TesterUtils;

public final class TesterPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }

    public TesterPreferencesValidator validate(PhpModule phpModule) {
        validatePhpIni(TesterPreferences.isPhpIniEnabled(phpModule), TesterPreferences.getPhpIniPath(phpModule));
        validateTester(TesterPreferences.isTesterEnabled(phpModule), TesterPreferences.getTesterPath(phpModule));
        validateCoverageSourcePath(TesterPreferences.isCoverageSourcePathEnabled(phpModule), TesterPreferences.getCoverageSourcePath(phpModule));
        return this;
    }

    public TesterPreferencesValidator validatePhpIni(boolean phpIniEnabled, String phpIniPath) {
        if (phpIniEnabled) {
            String warning = TesterUtils.validatePhpIniPath(phpIniPath);
            if (warning != null) {
                result.addWarning(new ValidationResult.Message("php.ini.path", warning)); // NOI18N
            }
        }
        return this;
    }

    public TesterPreferencesValidator validateTester(boolean testerEnabled, String testerPath) {
        if (testerEnabled) {
            String warning = TesterUtils.validateTesterPath(testerPath);
            if (warning != null) {
                result.addWarning(new ValidationResult.Message("tester.path", warning)); // NOI18N
            }
        }
        return this;
    }

    public TesterPreferencesValidator validateCoverageSourcePath(boolean sourcePathEnabled, String sourcePath) {
        if (sourcePathEnabled) {
            String warning = TesterUtils.validateCoverageSourcePath(sourcePath);
            if (warning != null) {
                result.addWarning(new ValidationResult.Message("coverage.source.path", warning)); // NOI18N
            }
        }
        return this;
    }

}
