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
package org.netbeans.modules.php.project.runconfigs.validation;

import org.netbeans.modules.php.project.ui.Utils;
import org.openide.util.NbBundle;

/**
 * Validator for {@link org.netbeans.modules.php.project.runconfigs.RunConfigWeb}.
 */
final class RunConfigWebValidator {

    private RunConfigWebValidator() {
    }

    @NbBundle.Messages("RunConfigWebValidator.error.url.invalid=Project URL is not valid.")
    static String validateUrl(String url) {
        if (!Utils.isValidUrl(url)) {
            return Bundle.RunConfigWebValidator_error_url_invalid();
        }
        return null;
    }

}
