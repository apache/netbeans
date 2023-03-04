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

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.analysis.ui.analyzer.PHPStanCustomizerPanel;
import org.netbeans.modules.php.analysis.ui.options.PHPStanOptionsPanel;
import org.netbeans.modules.php.api.util.StringUtils;

public final class ValidatorPHPStanParameter {

    @NullAllowed
    private final String phpStanPath;
    @NullAllowed
    private final String configuration;
    @NullAllowed
    private final String memoryLimit;

    public static ValidatorPHPStanParameter create(PHPStanOptionsPanel panel) {
        return new ValidatorPHPStanParameter(panel);
    }

    public static ValidatorPHPStanParameter create(PHPStanCustomizerPanel panel) {
        return new ValidatorPHPStanParameter(panel);
    }

    private ValidatorPHPStanParameter() {
        phpStanPath = null;
        configuration = null;
        memoryLimit = null;
    }

    private ValidatorPHPStanParameter(PHPStanOptionsPanel panel) {
        phpStanPath = panel.getPHPStanPath();
        configuration = panel.getPHPStanConfigurationPath();
        memoryLimit = panel.getPHPStanMemoryLimit();
    }

    private ValidatorPHPStanParameter(PHPStanCustomizerPanel panel) {
        if (StringUtils.hasText(panel.getPHPStanPath())) {
            phpStanPath = panel.getPHPStanPath();
        } else {
            phpStanPath = AnalysisOptions.getInstance().getPHPStanPath();
        }
        configuration = panel.getConfiguration();
        memoryLimit = panel.getMemoryLimit();
    }

    @CheckForNull
    public String getPHPStanPath() {
        return phpStanPath;
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
