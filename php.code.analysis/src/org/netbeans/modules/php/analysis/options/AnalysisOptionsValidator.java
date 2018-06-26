/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.analysis.options;

import java.util.List;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.analysis.commands.CodingStandardsFixer;
import org.netbeans.modules.php.analysis.commands.MessDetector;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.util.NbBundle;

public final class AnalysisOptionsValidator {

    private final ValidationResult result = new ValidationResult();


    public AnalysisOptionsValidator validateCodeSniffer(String codeSnifferPath, String codeSnifferStandard) {
        validateCodeSnifferPath(codeSnifferPath);
        validateCodeSnifferStandard(codeSnifferStandard);
        return this;
    }

    public AnalysisOptionsValidator validateMessDetector(String messDetectorPath, List<String> messDetectorRuleSets) {
        validateMessDetectorPath(messDetectorPath);
        validateMessDetectorRuleSets(messDetectorRuleSets);
        return this;
    }

    public AnalysisOptionsValidator validateCodingStandardsFixer(String codingStandardsFixerPath) {
        validateCodingStandardsFixerPath(codingStandardsFixerPath);
        return this;
    }

    public ValidationResult getResult() {
        return result;
    }

    public AnalysisOptionsValidator validateCodeSnifferPath(String codeSnifferPath) {
        String warning = CodeSniffer.validate(codeSnifferPath);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("codeSniffer.path", warning)); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages("AnalysisOptionsValidator.codeSniffer.standard.empty=Valid code sniffer standard must be set.")
    public AnalysisOptionsValidator validateCodeSnifferStandard(String codeSnifferStandard) {
        if (!StringUtils.hasText(codeSnifferStandard)) {
            result.addWarning(new ValidationResult.Message("codeSniffer.standard", Bundle.AnalysisOptionsValidator_codeSniffer_standard_empty())); // NOI18N
        }
        return this;
    }

    private AnalysisOptionsValidator validateMessDetectorPath(String messDetectorPath) {
        String warning = MessDetector.validate(messDetectorPath);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("messDetector.path", warning)); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages("AnalysisOptionsValidator.messDetector.ruleSets.empty=At least one rule set must be set.")
    public AnalysisOptionsValidator validateMessDetectorRuleSets(List<String> messDetectorRuleSets) {
        if (messDetectorRuleSets.isEmpty()) {
            result.addWarning(new ValidationResult.Message("messDetector.ruleSets", Bundle.AnalysisOptionsValidator_messDetector_ruleSets_empty())); // NOI18N
        }
        return this;
    }

    private AnalysisOptionsValidator validateCodingStandardsFixerPath(String codingStandardsFixerPath) {
        String warning = CodingStandardsFixer.validate(codingStandardsFixerPath);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("codingStandardsFixer.path", warning)); // NOI18N
        }
        return this;
    }

}
