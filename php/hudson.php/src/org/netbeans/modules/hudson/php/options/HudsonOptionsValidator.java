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
package org.netbeans.modules.hudson.php.options;

import java.io.File;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.util.NbBundle;

/**
 * Validator for {@link HudsonOptions Hudson options}.
 */
public final class HudsonOptionsValidator {

    public static final String BUILD_XML_NAME = "build.xml"; // NOI18N
    public static final String JOB_CONFIG_NAME = "config.xml"; // NOI18N
    public static final String PHP_UNIT_CONFIG_NAME = "phpunit.xml"; // NOI18N
    public static final String PHP_UNIT_CONFIG_DIST_NAME = "phpunit.xml.dist"; // NOI18N


    private HudsonOptionsValidator() {
    }

    public static String validate(String buildXml, String jobConfig, String phpUnitConfig) {
        String error = validateBuildXml(buildXml);
        if (error != null) {
            return error;
        }
        error = validateJobConfig(jobConfig);
        if (error != null) {
            return error;
        }
        error = validatePhpUnitConfig(phpUnitConfig);
        if (error != null) {
            return error;
        }
        return null;
    }

    @NbBundle.Messages({
        "HudsonOptionsValidator.error.buildXml.file=Build script",
        "HudsonOptionsValidator.error.buildXml.name=File 'build.xml' must be selected."
    })
    public static String validateBuildXml(String buildXml) {
        return validateFile(Bundle.HudsonOptionsValidator_error_buildXml_file(), buildXml,
                Bundle.HudsonOptionsValidator_error_buildXml_name(), BUILD_XML_NAME);
    }

    @NbBundle.Messages({
        "HudsonOptionsValidator.error.jobConfig.file=Job config",
        "HudsonOptionsValidator.error.jobConfig.name=File 'config.xml' must be selected."
    })
    public static String validateJobConfig(String jobConfig) {
        return validateFile(Bundle.HudsonOptionsValidator_error_jobConfig_file(), jobConfig,
                Bundle.HudsonOptionsValidator_error_jobConfig_name(), JOB_CONFIG_NAME);
    }

    @NbBundle.Messages({
        "HudsonOptionsValidator.error.phpUnitConfig.file=PHPUnit config",
        "HudsonOptionsValidator.error.phpUnitConfig.name=File 'phpunit.xml' or 'phpunit.xml.dist' must be selected."
    })
    public static String validatePhpUnitConfig(String phpUnitConfig) {
        return validateFile(Bundle.HudsonOptionsValidator_error_phpUnitConfig_file(), phpUnitConfig,
                Bundle.HudsonOptionsValidator_error_phpUnitConfig_name(), PHP_UNIT_CONFIG_NAME, PHP_UNIT_CONFIG_DIST_NAME);
    }

    private static String validateFile(String fileLabel, String filePath, String fileNameError, String... fileNames) {
        String error = FileUtils.validateFile(fileLabel, filePath, false);
        if (error != null) {
            return error;
        }
        // check file name
        boolean found = false;
        for (String fileName : fileNames) {
            if (fileName.equals(new File(filePath).getName())) {
                found = true;
                break;
            }
        }
        if (!found) {
            return fileNameError;
        }
        return null;
    }

}
