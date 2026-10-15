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

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.analysis.ui.analyzer.PsalmCustomizerPanel;
import org.netbeans.modules.php.analysis.ui.options.PsalmOptionsPanel;
import org.netbeans.modules.php.api.util.StringUtils;

public final class ValidatorPsalmParameter {

    @NullAllowed
    private final String psalmPath;
    @NullAllowed
    private final String configuration;
    @NullAllowed
    private final String memoryLimit;

    public static ValidatorPsalmParameter create(PsalmOptionsPanel panel) {
        return new ValidatorPsalmParameter(panel);
    }

    public static ValidatorPsalmParameter create(PsalmCustomizerPanel panel) {
        return new ValidatorPsalmParameter(panel);
    }

    private ValidatorPsalmParameter() {
        psalmPath = null;
        configuration = null;
        memoryLimit = null;
    }

    private ValidatorPsalmParameter(PsalmOptionsPanel panel) {
        psalmPath = panel.getPsalmPath();
        configuration = panel.getPsalmConfigurationPath();
        memoryLimit = panel.getPsalmMemoryLimit();
    }

    private ValidatorPsalmParameter(PsalmCustomizerPanel panel) {
        if (StringUtils.hasText(panel.getPsalmPath())) {
            psalmPath = panel.getPsalmPath();
        } else {
            psalmPath = AnalysisOptions.getInstance().getPsalmPath();
        }
        configuration = panel.getConfiguration();
        memoryLimit = panel.getMemoryLimit();
    }

    @CheckForNull
    public String getPsalmPath() {
        return psalmPath;
    }

    @CheckForNull
    public String getConfiguration() {
        return configuration;
    }

    @CheckForNull
    public String getMemoryLimit() {
        return memoryLimit;
    }
}
