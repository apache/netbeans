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
package org.netbeans.modules.javascript.bower.options;

import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutableValidator;
import org.openide.util.NbBundle;

public final class BowerOptionsValidator {

    public static final String BOWER_PATH = "bower.path"; // NOI18N

    private final ValidationResult result = new ValidationResult();


    public BowerOptionsValidator validate() {
        return validateBower();
    }

    public BowerOptionsValidator validateBower() {
        return validateBower(BowerOptions.getInstance().getBower());
    }

    @NbBundle.Messages("BowerOptionsValidator.bower.name=Bower")
    public BowerOptionsValidator validateBower(String bower) {
        String warning = ExternalExecutableValidator.validateCommand(bower, Bundle.BowerOptionsValidator_bower_name());
        if (warning != null) {
            result.addWarning(new ValidationResult.Message(BOWER_PATH, warning));
        }
        return this;
    }

    public ValidationResult getResult() {
        return result;
    }

}
