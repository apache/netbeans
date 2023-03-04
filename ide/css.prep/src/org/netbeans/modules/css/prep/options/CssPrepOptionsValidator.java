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
package org.netbeans.modules.css.prep.options;

import org.netbeans.modules.css.prep.less.LessExecutable;
import org.netbeans.modules.css.prep.sass.SassCli;
import org.netbeans.modules.css.prep.util.StringUtils;
import org.netbeans.modules.web.common.api.ValidationResult;

public final class CssPrepOptionsValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }

    public CssPrepOptionsValidator validateSassPath(String sassPath, boolean allowEmpty) {
        if (allowEmpty
                && !StringUtils.hasText(sassPath)) {
            // no warning in dialog, project problems will catch it
            return this;
        }
        String warning = SassCli.validate(sassPath);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("sass.path", warning)); // NOI18N
        }
        return this;
    }

    public CssPrepOptionsValidator validateLessPath(String lessPath, boolean allowEmpty) {
        if (allowEmpty
                && !StringUtils.hasText(lessPath)) {
            // no warning in dialog, project problems will catch it
            return this;
        }
        String warning = LessExecutable.validate(lessPath);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("less.path", warning)); // NOI18N
        }
        return this;
    }

}
