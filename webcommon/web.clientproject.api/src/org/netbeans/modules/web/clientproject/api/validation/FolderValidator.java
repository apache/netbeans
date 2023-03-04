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
package org.netbeans.modules.web.clientproject.api.validation;

import java.io.File;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.util.NbBundle;

/**
 * Validator for folder.
 * @since 1.20
 */
public final class FolderValidator {

    /**
     * {@link ValidationResult.Message#getSource() Source} of the validation message.
     */
    public static final String FOLDER = "folder"; // NOI18N

    private final ValidationResult result = new ValidationResult();


    /**
     * Get validation result.
     * @return validation result
     */
    public ValidationResult getResult() {
        return result;
    }

    /**
     * Validate the given folder. It means that the given folder:
     * <ul>
     *   <li>must be non-null</li>
     *   <li>must exist and must be a directory</li>
     * </ul>
     * @param folder folder to be validated, can be {@code null}
     * @return validator itself
     * @see #validateFolder(File, String)
     */
    @NbBundle.Messages("FolderValidator.error.folder.prefix=Folder")
    public FolderValidator validateFolder(@NullAllowed File folder) {
        return validateFolder(folder, Bundle.FolderValidator_error_folder_prefix());
    }

    /**
     * Validate the given folder. It means that the given folder:
     * <ul>
     *   <li>must be non-null</li>
     *   <li>must exist and must be a directory</li>
     * </ul>
     * @param folder folder to be validated, can be {@code null}
     * @param directoryName custom directory name to be used in the validation messages, e.g. "Unit Tests"
     * @return validator itself
     * @since 1.65
     * @see #validateFolder(File)
     */
    @NbBundle.Messages({
        "# {0} - directory name, e.g. Unit Tests directory",
        "FolderValidator.error.folder.invalid={0} must be a valid directory.",
    })
    public FolderValidator validateFolder(@NullAllowed File folder, String directoryName) {
        if (folder == null || !folder.isDirectory()) {
            result.addError(new ValidationResult.Message(FOLDER, Bundle.FolderValidator_error_folder_invalid(directoryName)));
        }
        return this;
    }

}
