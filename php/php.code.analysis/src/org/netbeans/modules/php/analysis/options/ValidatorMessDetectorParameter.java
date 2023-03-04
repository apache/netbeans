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
package org.netbeans.modules.php.analysis.options;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.analysis.ui.analyzer.MessDetectorCustomizerPanel;
import org.netbeans.modules.php.analysis.ui.options.MessDetectorOptionsPanel;

public final class ValidatorMessDetectorParameter {

    @NullAllowed
    private final String messDetectorPath;
    private final List<String> ruleSets;
    @NullAllowed
    private final String ruleSetFilePath;

    public static ValidatorMessDetectorParameter create(MessDetectorOptionsPanel panel) {
        return new ValidatorMessDetectorParameter(panel);
    }

    public static ValidatorMessDetectorParameter create(MessDetectorCustomizerPanel panel) {
        return new ValidatorMessDetectorParameter(panel);
    }

    private ValidatorMessDetectorParameter() {
        messDetectorPath = null;
        ruleSets = Collections.emptyList();
        ruleSetFilePath = null;
    }

    private ValidatorMessDetectorParameter(MessDetectorOptionsPanel panel) {
        messDetectorPath = panel.getMessDetectorPath();
        ruleSets = panel.getMessDetectorRuleSets();
        ruleSetFilePath = panel.getMessDetectorRuleSetFilePath();
    }

    private ValidatorMessDetectorParameter(MessDetectorCustomizerPanel panel) {
        messDetectorPath = panel.getValidMessDetectorPath();
        ruleSets = panel.getSelectedRuleSets();
        ruleSetFilePath = panel.getRuleSetFile();
    }

    @CheckForNull
    public String getMessDetectorPath() {
        return messDetectorPath;
    }

    public List<String> getRuleSets() {
        return Collections.unmodifiableList(ruleSets);
    }

    @CheckForNull
    public String getRuleSetFilePath() {
        return ruleSetFilePath;
    }

}
