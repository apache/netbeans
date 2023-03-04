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
package org.netbeans.modules.php.zend2.validation;

import java.io.File;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.util.NbBundle;

/**
 * Validator for IDE Options.
 */
public class OptionsValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return new ValidationResult(result);
    }

    public OptionsValidator validate(String skeleton) {
        String err = validateSkeleton(skeleton);
        if (err != null) {
            result.addError(new ValidationResult.Message("skeleton", err)); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages({
        "OptionsValidator.skeleton.title=Skeleton",
        "OptionsValidator.skeleton.notZip=Skeleton is not Zip file."
    })
    private String validateSkeleton(String skeleton) {
        String error = FileUtils.validateFile(Bundle.OptionsValidator_skeleton_title(), skeleton, false);
        if (error == null) {
            if (!new File(skeleton).getName().toLowerCase().endsWith(".zip")) { // NOI18N
                error = Bundle.OptionsValidator_skeleton_notZip();
            }
        }
        return error;
    }

}
