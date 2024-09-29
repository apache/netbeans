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
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.analysis.commands.CodingStandardsFixer;
import org.netbeans.modules.php.analysis.commands.MessDetector;
import org.netbeans.modules.php.analysis.commands.PHPStan;
import org.netbeans.modules.php.analysis.commands.Psalm;
import org.netbeans.modules.php.analysis.util.AnalysisUtils;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.util.NbPreferences;

public final class AnalysisOptions {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "analysis"; // NOI18N

    private static final AnalysisOptions INSTANCE = new AnalysisOptions();

    // code sniffer
    private static final String CODE_SNIFFER_PATH = "codeSniffer.path"; // NOI18N
    private static final String CODE_SNIFFER_STANDARD = "codeSniffer.standard"; // NOI18N
    // mess detector
    private static final String MESS_DETECTOR_PATH = "messDetector.path"; // NOI18N
    private static final String MESS_DETECTOR_RULE_SETS = "messDetector.ruleSets"; // NOI18N
    private static final String MESS_DETECTOR_RULE_SET_FILE = "messDetector.ruleSetFile"; // NOI18N
    private static final String MESS_DETECTOR_OPTIONS = "messDetector.options"; // NOI18N
    // coding standards fixer
    private static final String CODING_STANDARDS_FIXER_VERSION = "codingStandardsFixer.version"; // NOI18N
    private static final String CODING_STANDARDS_FIXER_PATH = "codingStandardsFixer.path"; // NOI18N
    private static final String CODING_STANDARDS_FIXER_LEVEL = "codingStandardsFixer.level"; // NOI18N
    private static final String CODING_STANDARDS_FIXER_CONFIG = "codingStandardsFixer.config"; // NOI18N
    private static final String CODING_STANDARDS_FIXER_OPTIONS = "codingStandardsFixer.options"; // NOI18N
    // PHPStan - PHP Static Analysis Tool
    private static final String PHPSTAN_PATH = "phpstan.path"; // NOI18N
    private static final String PHPSTAN_LEVEL = "phpstan.level"; // NOI18N
    private static final String PHPSTAN_CONFIGURATION = "phpstan.configuration"; // NOI18N
    private static final String PHPSTAN_MEMORY_LIMIT = "phpstan.memory.limit"; // NOI18N
    public static final int PHPSTAN_MIN_LEVEL = Integer.getInteger("nb.phpstan.min.level", 0); // NOI18N
    public static final int PHPSTAN_MAX_LEVEL = Integer.getInteger("nb.phpstan.max.level", 9); // NOI18N
    // Psalm - PHP Static Analysis Tool
    private static final String PSALM_PATH = "psalm.path"; // NOI18N
    private static final String PSALM_LEVEL = "psalm.level"; // NOI18N
    private static final String PSALM_CONFIGURATION = "psalm.configuration"; // NOI18N
    private static final String PSALM_MEMORY_LIMIT = "psalm.memory.limit"; // NOI18N
    public static final int PSALM_MIN_LEVEL = Integer.getInteger("nb.psalm.min.level", 1); // NOI18N
    public static final int PSALM_MAX_LEVEL = Integer.getInteger("nb.psalm.max.level", 8); // NOI18N

    private volatile boolean codeSnifferSearched = false;
    private volatile boolean messDetectorSearched = false;
    private volatile boolean codingStandardsFixerSearched = false;
    private volatile boolean phpstanSearched = false;
    private volatile boolean psalmSearched = false;

    private AnalysisOptions() {
    }

    public static AnalysisOptions getInstance() {
        return INSTANCE;
    }

    // code sniffer
    @CheckForNull
    public String getCodeSnifferPath() {
        String codeSnifferPath = getPreferences().get(CODE_SNIFFER_PATH, null);
        if (codeSnifferPath == null && !codeSnifferSearched) {
            codeSnifferSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(CodeSniffer.NAME, CodeSniffer.LONG_NAME);
            if (!scripts.isEmpty()) {
                codeSnifferPath = scripts.get(0);
                setCodeSnifferPath(codeSnifferPath);
            }
        }
        return codeSnifferPath;
    }

    public void setCodeSnifferPath(String path) {
        getPreferences().put(CODE_SNIFFER_PATH, path);
    }

    @CheckForNull
    public String getCodeSnifferStandard() {
        return getPreferences().get(CODE_SNIFFER_STANDARD, null);
    }

    public void setCodeSnifferStandard(String standard) {
        // avoid NPE, can happen if invalid code sniffer is selected
        if (standard == null) {
            standard = ""; // NOI18N
        }
        getPreferences().put(CODE_SNIFFER_STANDARD, standard);
    }

    // mess detector
    @CheckForNull
    public String getMessDetectorPath() {
        String messDetectorPath = getPreferences().get(MESS_DETECTOR_PATH, null);
        if (messDetectorPath == null && !messDetectorSearched) {
            messDetectorSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(MessDetector.NAME, MessDetector.LONG_NAME);
            if (!scripts.isEmpty()) {
                messDetectorPath = scripts.get(0);
                setMessDetectorPath(messDetectorPath);
            }
        }
        return messDetectorPath;
    }

    public void setMessDetectorPath(String path) {
        getPreferences().put(MESS_DETECTOR_PATH, path);
    }

    public List<String> getMessDetectorRuleSets() {
        String rulesets = getPreferences().get(MESS_DETECTOR_RULE_SETS, null);
        if (rulesets == null) {
            return MessDetector.RULE_SETS;
        }
        return AnalysisUtils.deserialize(rulesets);
    }

    public void setMessDetectorRuleSets(List<String> ruleSets) {
        getPreferences().put(MESS_DETECTOR_RULE_SETS, AnalysisUtils.serialize(ruleSets));
    }

    @CheckForNull
    public String getMessDetectorRuleSetFilePath() {
        return getPreferences().get(MESS_DETECTOR_RULE_SET_FILE, null);
    }

    public void setMessDetectorRuleSetFilePath(String ruleSetFilePath) {
        getPreferences().put(MESS_DETECTOR_RULE_SET_FILE, ruleSetFilePath);
    }

    @CheckForNull
    public String getMessDetectorOptions() {
        return getPreferences().get(MESS_DETECTOR_OPTIONS, null);
    }

    public void setMessDetectorOptions(String options) {
        getPreferences().put(MESS_DETECTOR_OPTIONS, options);
    }

    // coding standards fixer
    @CheckForNull
    public String getCodingStandardsFixerVersion() {
        return getPreferences().get(CODING_STANDARDS_FIXER_VERSION, CodingStandardsFixer.VERSIONS.get(CodingStandardsFixer.VERSIONS.size() - 1));
    }

    public void setCodingStandardsFixerVersion(String version) {
        getPreferences().put(CODING_STANDARDS_FIXER_VERSION, version);
    }

    @CheckForNull
    public String getCodingStandardsFixerPath() {
        String codingStandardsFixerPath = getPreferences().get(CODING_STANDARDS_FIXER_PATH, null);
        if (codingStandardsFixerPath == null && !codingStandardsFixerSearched) {
            codingStandardsFixerSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(CodingStandardsFixer.NAME, CodingStandardsFixer.LONG_NAME);
            if (!scripts.isEmpty()) {
                codingStandardsFixerPath = scripts.get(0);
                setCodingStandardsFixerPath(codingStandardsFixerPath);
            }
        }
        return codingStandardsFixerPath;
    }

    public void setCodingStandardsFixerPath(String path) {
        getPreferences().put(CODING_STANDARDS_FIXER_PATH, path);
    }

    @CheckForNull
    public String getCodingStandardsFixerLevel() {
        return getPreferences().get(CODING_STANDARDS_FIXER_LEVEL, null);
    }

    public void setCodingStandardsFixerLevel(String level) {
        if (level == null) {
            level = ""; // NOI18N
        }
        getPreferences().put(CODING_STANDARDS_FIXER_LEVEL, level);
    }

    @CheckForNull
    public String getCodingStandardsFixerConfig() {
        return getPreferences().get(CODING_STANDARDS_FIXER_CONFIG, null);
    }

    public void setCodingStandardsFixerConfig(String config) {
        if (config == null) {
            config = ""; // NOI18N
        }
        getPreferences().put(CODING_STANDARDS_FIXER_CONFIG, config);
    }

    public String getCodingStandardsFixerOptions() {
        return getPreferences().get(CODING_STANDARDS_FIXER_OPTIONS, ""); // NOI18N
    }

    public void setCodingStandardsFixerOptions(String options) {
        getPreferences().put(CODING_STANDARDS_FIXER_OPTIONS, options);
    }

    // phpstan
    @CheckForNull
    public String getPHPStanPath() {
        String phpstanPath = getPreferences().get(PHPSTAN_PATH, null);
        if (phpstanPath == null && !phpstanSearched) {
            phpstanSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(PHPStan.NAME, PHPStan.LONG_NAME);
            if (!scripts.isEmpty()) {
                phpstanPath = scripts.get(0);
                setMessDetectorPath(phpstanPath);
            }
        }
        return phpstanPath;
    }

    public void setPHPStanPath(String path) {
        getPreferences().put(PHPSTAN_PATH, path);
    }

    public String getPHPStanLevel() {
        String level = getPreferences().get(PHPSTAN_LEVEL, String.valueOf(PHPSTAN_MIN_LEVEL));
        return getValidPHPStanLevel(level);
    }

    public void setPHPStanLevel(String level) {
        getPreferences().put(PHPSTAN_LEVEL, getValidPHPStanLevel(level));
    }

    public static String getValidPHPStanLevel(String level) {
        if (PHPStan.MAX_LEVEL.equals(level)) {
            return level;
        }
        String phpstanLevel;
        try {
            phpstanLevel = String.valueOf(AnalysisUtils.getValidInt(PHPSTAN_MIN_LEVEL, PHPSTAN_MAX_LEVEL, Integer.valueOf(level)));
        } catch (NumberFormatException e) {
            phpstanLevel = level;
        }
        return phpstanLevel;
    }

    @CheckForNull
    public String getPHPStanConfigurationPath() {
        return getPreferences().get(PHPSTAN_CONFIGURATION, null);
    }

    public void setPHPStanConfigurationPath(String configuration) {
        getPreferences().put(PHPSTAN_CONFIGURATION, configuration);
    }

    public String getPHPStanMemoryLimit() {
        return getPreferences().get(PHPSTAN_MEMORY_LIMIT, ""); // NOI18N
    }

    public void setPHPStanMemoryLimit(String memoryLimit) {
        getPreferences().put(PHPSTAN_MEMORY_LIMIT, memoryLimit);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(AnalysisOptions.class).node(PREFERENCES_PATH);
    }

    // psalm
    @CheckForNull
    public String getPsalmPath() {
        String psalmPath = getPreferences().get(PSALM_PATH, null);
        if (psalmPath == null && !psalmSearched) {
            psalmSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(Psalm.NAME, Psalm.LONG_NAME);
            if (!scripts.isEmpty()) {
                psalmPath = scripts.get(0);
                setMessDetectorPath(psalmPath);
            }
        }
        return psalmPath;
    }

    public void setPsalmPath(String path) {
        getPreferences().put(PSALM_PATH, path);
    }

    public String getPsalmLevel() {
        String level = getPreferences().get(PSALM_LEVEL, String.valueOf(PSALM_MIN_LEVEL));
        return getValidPsalmLevel(level);
    }

    public void setPsalmLevel(String level) {
        getPreferences().put(PSALM_LEVEL, getValidPsalmLevel(level));
    }

    public static String getValidPsalmLevel(String level) {
        String psalmLevel;
        try {
            psalmLevel = String.valueOf(AnalysisUtils.getValidInt(PSALM_MIN_LEVEL, PSALM_MAX_LEVEL, Integer.valueOf(level)));
        } catch (NumberFormatException e) {
            psalmLevel = level;
        }
        return psalmLevel;
    }

    @CheckForNull
    public String getPsalmConfigurationPath() {
        return getPreferences().get(PSALM_CONFIGURATION, null);
    }

    public void setPsalmConfigurationPath(String configuration) {
        getPreferences().put(PSALM_CONFIGURATION, configuration);
    }

    public String getPsalmMemoryLimit() {
        return getPreferences().get(PSALM_MEMORY_LIMIT, ""); // NOI18N
    }

    public void setPsalmMemoryLimit(String memoryLimit) {
        getPreferences().put(PSALM_MEMORY_LIMIT, memoryLimit);
    }

}
