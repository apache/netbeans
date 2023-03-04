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
import org.netbeans.modules.php.analysis.ui.analyzer.CodingStandardsFixerCustomizerPanel;
import org.netbeans.modules.php.analysis.ui.options.CodingStandardsFixerOptionsPanel;
import org.netbeans.modules.php.api.util.StringUtils;

public final class ValidatorCodingStandardsFixerParameter {

    @NullAllowed
    private final String codingStandardsFixerPath;

    public static ValidatorCodingStandardsFixerParameter create(CodingStandardsFixerOptionsPanel panel) {
        return new ValidatorCodingStandardsFixerParameter(panel);
    }

    public static ValidatorCodingStandardsFixerParameter create(CodingStandardsFixerCustomizerPanel panel) {
        return new ValidatorCodingStandardsFixerParameter(panel);
    }

    private ValidatorCodingStandardsFixerParameter() {
        this.codingStandardsFixerPath = null;
    }

    private ValidatorCodingStandardsFixerParameter(CodingStandardsFixerOptionsPanel panel) {
        this.codingStandardsFixerPath = panel.getCodingStandardsFixerPath();
    }

    private ValidatorCodingStandardsFixerParameter(CodingStandardsFixerCustomizerPanel panel) {
        if (StringUtils.hasText(panel.getCodingStandardsFixerPath())) {
            this.codingStandardsFixerPath = panel.getCodingStandardsFixerPath();
        } else {
            this.codingStandardsFixerPath = AnalysisOptions.getInstance().getCodingStandardsFixerPath();
        }
    }

    @CheckForNull
    public String getCodingStandardsFixerPath() {
        return codingStandardsFixerPath;
    }

}
