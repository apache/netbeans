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
package org.netbeans.modules.css.prep.preferences;

import java.util.List;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.prep.sass.SassCli;
import org.netbeans.modules.css.prep.util.CssPreprocessorUtils;
import org.netbeans.modules.css.prep.util.InvalidExternalExecutableException;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class SassPreferencesValidator implements CssPreprocessorPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    @Override
    public ValidationResult getResult() {
        return result;
    }

    @Override
    public SassPreferencesValidator validate(Project project) {
        SassPreferences sassPreferences = SassPreferences.getInstance();
        return validateMappings(CssPreprocessorUtils.getWebRoot(project), sassPreferences.isEnabled(project), sassPreferences.getMappings(project));
    }

    @Override
    public SassPreferencesValidator validateMappings(@NullAllowed FileObject root, boolean enabled, List<Pair<String, String>> mappings) {
        if (enabled) {
            result.merge(new CssPreprocessorUtils.MappingsValidator("scss") // NOI18N
                    .validate(root, mappings)
                    .getResult());
        }
        return this;
    }

    @NbBundle.Messages({
        "# {0} - error",
        "SassPreferencesValidator.error.executable={0} Use Configure Executables button to fix it.",
    })
    @Override
    public SassPreferencesValidator validateExecutable(boolean enabled) {
        if (enabled) {
            try {
                SassCli.getDefault();
            } catch (InvalidExternalExecutableException ex) {
                result.addError(new ValidationResult.Message("sass.path", Bundle.SassPreferencesValidator_error_executable(ex.getLocalizedMessage()))); // NOI18N
            }
        }
        return this;
    }

}
