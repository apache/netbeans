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

import java.io.File;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Validator for {@link org.netbeans.modules.php.project.runconfigs.BaseRunConfig}.
 */
public final class BaseRunConfigValidator {

    private BaseRunConfigValidator() {
    }

    public static String validateIndexFile(File rootDirectory, String indexFile) {
        return validateIndexFile(rootDirectory, indexFile, true);
    }

    @NbBundle.Messages("BaseRunConfigValidator.error.index.label=Index File")
    public static String validateIndexFile(File rootDirectory, String indexFile, boolean normalizeFile) {
        return validateRelativeFile(rootDirectory, indexFile, Bundle.BaseRunConfigValidator_error_index_label(), normalizeFile);
    }

    static String validateRelativeFile(File rootDirectory, String relativeFile, String errSource) {
        return validateRelativeFile(rootDirectory, relativeFile, errSource, true);
    }

    @NbBundle.Messages({
        "# {0} - source of error",
        "BaseRunConfigValidator.error.relativeFile.missing={0} must be specified in order to run or debug project in command line.",
        "# {0} - source of error",
        "BaseRunConfigValidator.error.relativeFile.invalid={0} must be a valid relative URL."
    })
    static String validateRelativeFile(File rootDirectory, String relativeFile, String errSource, boolean normalizeFile) {
        assert rootDirectory != null;
        if (!StringUtils.hasText(relativeFile)) {
            return Bundle.BaseRunConfigValidator_error_relativeFile_missing(errSource);
        }
        boolean error = false;
        if (relativeFile.startsWith("/") // NOI18N
                || relativeFile.startsWith("\\")) { // NOI18N
            error = true;
        } else if (Utilities.isWindows() && relativeFile.contains(File.separator)) {
            error = true;
        } else {
            File index = PhpProjectUtils.resolveFile(rootDirectory, relativeFile);
            if (!index.isFile()) {
                error = true;
            } else if (normalizeFile
                    && !index.equals(FileUtil.normalizeFile(index))) {
                error = true;
            }
        }
        if (error) {
            return Bundle.BaseRunConfigValidator_error_relativeFile_invalid(errSource);
        }
        return null;
    }

}
