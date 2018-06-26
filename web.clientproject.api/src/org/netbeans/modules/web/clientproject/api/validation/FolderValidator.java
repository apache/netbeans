/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
