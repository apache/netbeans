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
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.analysis.commands.CodingStandardsFixer;
import org.netbeans.modules.php.analysis.commands.MessDetector;
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
    // coding standards fixer
    private static final String CODING_STANDARDS_FIXER_PATH = "codingStandardsFixer.path"; // NOI18N
    private static final String CODING_STANDARDS_FIXER_LEVEL = "codingStandardsFixer.level"; // NOI18N
    private static final String CODING_STANDARDS_FIXER_CONFIG = "codingStandardsFixer.config"; // NOI18N
    private static final String CODING_STANDARDS_FIXER_OPTIONS = "codingStandardsFixer.options"; // NOI18N

    private volatile boolean codeSnifferSearched = false;
    private volatile boolean messDetectorSearched = false;
    private volatile boolean codingStandardsFixerSearched = false;


    public static AnalysisOptions getInstance() {
        return INSTANCE;
    }

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

    private Preferences getPreferences() {
        return NbPreferences.forModule(AnalysisOptions.class).node(PREFERENCES_PATH);
    }

}
