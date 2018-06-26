/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
