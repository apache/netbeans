/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
