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
