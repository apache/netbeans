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
package org.netbeans.modules.javascript.grunt.options;

import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutableValidator;
import org.openide.util.NbBundle;

public final class GruntOptionsValidator {

    public static final String GRUNT_PATH = "grunt.path"; // NOI18N

    private final ValidationResult result = new ValidationResult();


    public GruntOptionsValidator validate() {
        return GruntOptionsValidator.this.validateGrunt();
    }

    public GruntOptionsValidator validateGrunt() {
        return validateGrunt(GruntOptions.getInstance().getGrunt());
    }

    @NbBundle.Messages("GruntOptionsValidator.grunt.name=Grunt")
    public GruntOptionsValidator validateGrunt(String grunt) {
        String warning = ExternalExecutableValidator.validateCommand(grunt, Bundle.GruntOptionsValidator_grunt_name());
        if (warning != null) {
            result.addWarning(new ValidationResult.Message(GRUNT_PATH, warning));
        }
        return this;
    }

    public ValidationResult getResult() {
        return result;
    }

}
