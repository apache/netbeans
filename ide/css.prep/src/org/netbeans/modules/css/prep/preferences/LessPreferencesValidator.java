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
import org.netbeans.modules.css.prep.less.LessExecutable;
import org.netbeans.modules.css.prep.util.CssPreprocessorUtils;
import org.netbeans.modules.css.prep.util.InvalidExternalExecutableException;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public class LessPreferencesValidator implements CssPreprocessorPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    @Override
    public ValidationResult getResult() {
        return result;
    }

    @Override
    public LessPreferencesValidator validate(Project project) {
        LessPreferences lessPreferences = LessPreferences.getInstance();
        return validateMappings(CssPreprocessorUtils.getWebRoot(project), lessPreferences.isEnabled(project), lessPreferences.getMappings(project));
    }

    @Override
    public LessPreferencesValidator validateMappings(@NullAllowed FileObject root, boolean enabled, List<Pair<String, String>> mappings) {
        if (enabled) {
            result.merge(new CssPreprocessorUtils.MappingsValidator("less") // NOI18N
                    .validate(root, mappings)
                    .getResult());
        }
        return this;
    }

    @NbBundle.Messages({
        "# {0} - error",
        "LessPreferencesValidator.error.executable={0} Use Configure Executables button to fix it.",
    })
    @Override
    public LessPreferencesValidator validateExecutable(boolean enabled) {
        if (enabled) {
            try {
                LessExecutable.getDefault();
            } catch (InvalidExternalExecutableException ex) {
                result.addError(new ValidationResult.Message("less.path", Bundle.LessPreferencesValidator_error_executable(ex.getLocalizedMessage()))); // NOI18N
            }
        }
        return this;
    }

}
