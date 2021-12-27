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
import org.netbeans.modules.php.analysis.ui.analyzer.CodeSnifferCustomizerPanel;
import org.netbeans.modules.php.analysis.ui.options.CodeSnifferOptionsPanel;

public final class ValidatorCodeSnifferParameter {

    @NullAllowed
    private final String codeSnifferPath;
    @NullAllowed
    private final String codeSnifferStandard;

    public static ValidatorCodeSnifferParameter create(CodeSnifferOptionsPanel panel) {
        return new ValidatorCodeSnifferParameter(panel);
    }

    public static ValidatorCodeSnifferParameter create(CodeSnifferCustomizerPanel panel) {
        return new ValidatorCodeSnifferParameter(panel);
    }

    private ValidatorCodeSnifferParameter() {
        this.codeSnifferPath = null;
        this.codeSnifferStandard = null;
    }

    private ValidatorCodeSnifferParameter(CodeSnifferOptionsPanel panel) {
        this.codeSnifferPath = panel.getCodeSnifferPath();
        this.codeSnifferStandard = panel.getCodeSnifferStandard();
    }

    private ValidatorCodeSnifferParameter(CodeSnifferCustomizerPanel panel) {
        this.codeSnifferPath = panel.getValidCodeSnifferPath();
        this.codeSnifferStandard = panel.getCodeSnifferStandard();
    }

    @CheckForNull
    public String getCodeSnifferPath() {
        return codeSnifferPath;
    }

    @CheckForNull
    public String getCodeSnifferStandard() {
        return codeSnifferStandard;
    }

}
