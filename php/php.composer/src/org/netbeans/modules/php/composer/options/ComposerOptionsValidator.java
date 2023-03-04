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
package org.netbeans.modules.php.composer.options;

import java.util.regex.Pattern;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.composer.commands.Composer;
import org.openide.util.NbBundle;

/**
 * Validator for Composer options.
 */
public final class ComposerOptionsValidator {

    private static final Pattern VENDOR_REGEX = Pattern.compile("^[a-z0-9-]+$"); // NOI18N
    private static final Pattern EMAIL_REGEX = Pattern.compile("^\\w+[\\.\\w\\-]*@\\w+[\\.\\w\\-]*\\.[a-z]{2,}$", Pattern.CASE_INSENSITIVE); // NOI18N
    private static final Pattern AUTHOR_NAME_REGEX = Pattern.compile("^[^\\d]+$"); // NOI18N


    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }

    public ComposerOptionsValidator validate(ComposerOptions composerOptions) {
        return validate(composerOptions.getComposerPath(), composerOptions.getVendor(),
                composerOptions.getAuthorName(), composerOptions.getAuthorEmail());
    }

    public ComposerOptionsValidator validate(String composerPath, String vendor, String authorName, String authorEmail) {
        validateComposerPath(composerPath);
        validateVendor(vendor);
        validateAuthorName(authorName);
        validateAuthorEmail(authorEmail);
        return this;
    }

    private ComposerOptionsValidator validateComposerPath(String composerPath) {
        String warning = Composer.validate(composerPath);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("composerPath", warning)); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages("ComposerOptionsValidator.warning.invalidVendor=Vendor is not valid (only lower-cased letters and \"-\" allowed).")
    ComposerOptionsValidator validateVendor(String vendor) {
        if (!VENDOR_REGEX.matcher(vendor).matches()) {
            result.addWarning(new ValidationResult.Message("vendor", Bundle.ComposerOptionsValidator_warning_invalidVendor())); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages({
        "ComposerOptionsValidator.warning.noAuthorName=Author name cannot be empty.",
        "ComposerOptionsValidator.warning.authorNameNumber=Author name cannot contain number.",
    })
    ComposerOptionsValidator validateAuthorName(String authorName) {
        if (!StringUtils.hasText(authorName)) {
            result.addWarning(new ValidationResult.Message("authorName", Bundle.ComposerOptionsValidator_warning_noAuthorName())); // NOI18N
        } else if (!AUTHOR_NAME_REGEX.matcher(authorName).matches()) {
            result.addWarning(new ValidationResult.Message("authorName", Bundle.ComposerOptionsValidator_warning_authorNameNumber())); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages("ComposerOptionsValidator.warning.invalidAuthorEmail=Author e-mail is not valid.")
    ComposerOptionsValidator validateAuthorEmail(String authorEmail) {
        if (!StringUtils.hasText(authorEmail)
                || !EMAIL_REGEX.matcher(authorEmail).matches()) {
            result.addWarning(new ValidationResult.Message("authorEmail", Bundle.ComposerOptionsValidator_warning_invalidAuthorEmail())); // NOI18N
        }
        return this;
    }

}
