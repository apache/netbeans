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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.php.analysis.MessDetectorParams;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.analysis.commands.CodingStandardsFixer;
import org.netbeans.modules.php.analysis.commands.MessDetector;
import org.netbeans.modules.php.analysis.commands.PHPStan;
import org.netbeans.modules.php.analysis.commands.Psalm;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class AnalysisOptionsValidator {

    private static final Pattern PHPSTAN_MEMORY_LIMIT_PATTERN = Pattern.compile("^\\-?\\d+[kmg]?$", Pattern.CASE_INSENSITIVE); // NOI18N
    private static final Pattern PSALM_MEMORY_LIMIT_PATTERN = Pattern.compile("^\\-?\\d+[kmg]?$", Pattern.CASE_INSENSITIVE); // NOI18N
    private final ValidationResult result = new ValidationResult();

    public AnalysisOptionsValidator validateCodeSniffer(ValidatorCodeSnifferParameter param) {
        validateCodeSnifferPath(param.getCodeSnifferPath());
        validateCodeSnifferStandard(param.getCodeSnifferStandard());
        return this;
    }

    public AnalysisOptionsValidator validateMessDetector(ValidatorMessDetectorParameter param) {
        validateMessDetectorPath(param.getMessDetectorPath());
        validateMessDetectorRuleSets(param.getRuleSets(), param.getRuleSetFilePath());
        return this;
    }

    public AnalysisOptionsValidator validateMessDetector(MessDetectorParams param) {
        FileObject ruleSetFile = param.getRuleSetFile();
        String ruleSetFilePath = ruleSetFile == null ? null : FileUtil.toFile(ruleSetFile).getAbsolutePath();
        validateMessDetectorRuleSets(param.getRuleSets(), ruleSetFilePath);
        return this;
    }

    public AnalysisOptionsValidator validateCodingStandardsFixer(ValidatorCodingStandardsFixerParameter param) {
        validateCodingStandardsFixerPath(param.getCodingStandardsFixerPath());
        return this;
    }

    public AnalysisOptionsValidator validatePHPStan(ValidatorPHPStanParameter param) {
        validatePHPStanPath(param.getPHPStanPath());
        validatePHPStanConfiguration(param.getConfiguration());
        validatePHPStanMemoryLimit(param.getMemoryLimit());
        return this;
    }

    public AnalysisOptionsValidator validatePsalm(ValidatorPsalmParameter param) {
        validatePsalmPath(param.getPsalmPath());
        validatePsalmConfiguration(param.getConfiguration());
        validatePsalmMemoryLimit(param.getMemoryLimit());
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
        if (codeSnifferStandard == null) {
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
    private AnalysisOptionsValidator validateMessDetectorRuleSets(List<String> messDetectorRuleSets, String ruleSetFile) {
        if ((messDetectorRuleSets == null || messDetectorRuleSets.size() == 1 && messDetectorRuleSets.contains(MessDetector.EMPTY_RULE_SET)) && StringUtils.isEmpty(ruleSetFile)) {
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

    private AnalysisOptionsValidator validatePHPStanPath(String phpStanPath) {
        String warning = PHPStan.validate(phpStanPath);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("phpStan.path", warning)); // NOI18N
        }
        return this;
    }

    private AnalysisOptionsValidator validatePHPStanConfiguration(String configuration) {
        if (!StringUtils.isEmpty(configuration)) {
            String warning = FileUtils.validateFile("Configuration file", configuration, false); // NOI18N
            if (warning != null) {
                result.addWarning(new ValidationResult.Message("phpStan.configuration", warning)); // NOI18N
            }
        }
        return this;
    }

    @NbBundle.Messages("AnalysisOptionsValidator.phpStan.memory.limit.invalid=Valid memory limit value must be set.")
    private AnalysisOptionsValidator validatePHPStanMemoryLimit(String memoryLimit) {
        if (!StringUtils.isEmpty(memoryLimit)) {
            Matcher matcher = PHPSTAN_MEMORY_LIMIT_PATTERN.matcher(memoryLimit);
            if (!matcher.matches()) {
                result.addWarning(new ValidationResult.Message("phpStan.memory.limit", Bundle.AnalysisOptionsValidator_phpStan_memory_limit_invalid())); // NOI18N
            }
        }
        return this;
    }

    private AnalysisOptionsValidator validatePsalmPath(String psalmPath) {
        String warning = Psalm.validate(psalmPath);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("psalm.path", warning)); // NOI18N
        }
        return this;
    }

    private AnalysisOptionsValidator validatePsalmConfiguration(String configuration) {
        if (!StringUtils.isEmpty(configuration)) {
            String warning = FileUtils.validateFile("Configuration file", configuration, false); // NOI18N
            if (warning != null) {
                result.addWarning(new ValidationResult.Message("psalm.configuration", warning)); // NOI18N
            }
        }
        return this;
    }

    @NbBundle.Messages("AnalysisOptionsValidator.psalm.memory.limit.invalid=Valid memory limit value must be set.")
    private AnalysisOptionsValidator validatePsalmMemoryLimit(String memoryLimit) {
        if (!StringUtils.isEmpty(memoryLimit)) {
            Matcher matcher = PSALM_MEMORY_LIMIT_PATTERN.matcher(memoryLimit);
            if (!matcher.matches()) {
                result.addWarning(new ValidationResult.Message("psalm.memory.limit", Bundle.AnalysisOptionsValidator_psalm_memory_limit_invalid())); // NOI18N
            }
        }
        return this;
    }

}
