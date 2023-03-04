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
package org.netbeans.modules.php.phing.options;

import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.util.NbBundle;

public final class PhingOptionsValidator {

    public static final String PHING_PATH = "phing.path"; // NOI18N

    private final ValidationResult result = new ValidationResult();


    public PhingOptionsValidator validate() {
        return validatePhing();
    }

    public PhingOptionsValidator validatePhing() {
        return validatePhing(PhingOptions.getInstance().getPhing());
    }

    @NbBundle.Messages("PhingOptionsValidator.phing.name=Phing")
    public PhingOptionsValidator validatePhing(String phing) {
        String warning = PhpExecutableValidator.validateCommand(phing, Bundle.PhingOptionsValidator_phing_name());
        if (warning != null) {
            result.addWarning(new ValidationResult.Message(PHING_PATH, warning));
        }
        return this;
    }

    public ValidationResult getResult() {
        return result;
    }

}
