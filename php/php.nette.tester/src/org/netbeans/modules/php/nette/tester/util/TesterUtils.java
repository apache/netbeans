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

package org.netbeans.modules.php.nette.tester.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.util.NbBundle;

public final class TesterUtils {

    public static final List<String> BINARY_EXECUTABLES = Collections.unmodifiableList(Arrays.asList(null, "php-cgi", "php")); // NOI18N


    private TesterUtils() {
    }

    @NbBundle.Messages("TesterUtils.tester.label=Tester file")
    @CheckForNull
    public static String validateTesterPath(String testerPath) {
        return PhpExecutableValidator.validateCommand(testerPath, Bundle.TesterUtils_tester_label());
    }

    @NbBundle.Messages("TesterUtils.php.ini.error=Absolute path to file or directory must be set for php.ini.")
    @CheckForNull
    public static String validatePhpIniPath(@NullAllowed String phpIniPath) {
        if (FileUtils.validateDirectory(phpIniPath, false) != null
                && FileUtils.validateFile(phpIniPath, false) != null) {
            return Bundle.TesterUtils_php_ini_error();
        }
        return null;
    }

    @NbBundle.Messages("TesterUtils.coverage.source.path.error=Absolute path to directory must be set for coverage source path.")
    @CheckForNull
    public static String validateCoverageSourcePath(@NullAllowed String sourcePath) {
        if (FileUtils.validateDirectory(sourcePath, false) != null) {
            return Bundle.TesterUtils_coverage_source_path_error();
        }
        return null;
    }

}
