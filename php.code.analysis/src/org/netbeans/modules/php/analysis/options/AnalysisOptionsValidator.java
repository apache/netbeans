/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
