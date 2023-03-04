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
package org.netbeans.modules.php.symfony2.options;

import java.io.File;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.util.NbBundle;

public final class SymfonyOptionsValidator {

    private final ValidationResult result = new ValidationResult();


    public SymfonyOptionsValidator validate() {
        if (SymfonyOptions.getInstance().isUseInstaller()) {
            validateInstaller();
        } else {
            validateSandbox();
        }
        return this;
    }

    public SymfonyOptionsValidator validate(boolean useInstaller, String installer, String sandbox) {
        if (useInstaller) {
            return validateInstaller(installer);
        }
        return validateSandbox(sandbox);
    }

    public SymfonyOptionsValidator validateInstaller() {
        return validateInstaller(SymfonyOptions.getInstance().getInstaller());
    }

    @NbBundle.Messages("SymfonyOptionsValidator.installer=Installer")
    public SymfonyOptionsValidator validateInstaller(String installer) {
        String warning = PhpExecutableValidator.validateCommand(installer, Bundle.SymfonyOptionsValidator_installer());
        if (warning != null) {
            result.addWarning(new ValidationResult.Message(SymfonyOptions.INSTALLER, warning));
        }
        return this;
    }

    public SymfonyOptionsValidator validateSandbox() {
        return validateSandbox(SymfonyOptions.getInstance().getSandbox());
    }

    @NbBundle.Messages({
        "SymfonyOptionsValidator.sandbox=Sandbox",
        "SymfonyOptionsValidator.sandbox.notZip=Sandbox is not ZIP file.",
    })
    public SymfonyOptionsValidator validateSandbox(@NullAllowed String sandbox) {
        String warning = FileUtils.validateFile(Bundle.SymfonyOptionsValidator_sandbox(), sandbox, false);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message(SymfonyOptions.SANDBOX, warning));
        } else if (!new File(sandbox).getName().toLowerCase().endsWith(".zip")) { // NOI18N
            result.addWarning(new ValidationResult.Message(SymfonyOptions.SANDBOX, Bundle.SymfonyOptionsValidator_sandbox_notZip()));
        }
        return this;
    }

    public ValidationResult getResult() {
        return result;
    }

}
