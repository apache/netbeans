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
package org.netbeans.modules.web.common.ui.api;

import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.NbBundle;

/**
 *
 * @since 1.74
 */
public final class ExternalExecutableValidator {

    private ExternalExecutableValidator() {
    }

    /**
     * Return {@code true} if the given command is {@link #validateCommand(String, String) valid}.
     * @param command command to be validated, can be {@code null}
     * @return {@code true} if the given command is {@link #validateCommand(String, String) valid}, {@code false} otherwise
     */
    public static boolean isValidCommand(@NullAllowed String command) {
        return validateCommand(command, (String) null) == null;
    }

    /**
     * Validate the given command and return error if it is not valid, {@code null} otherwise.
     * @param command command to be validated, can be {@code null}
     * @param executableName the name of the executable (e.g. "Doctrine script"), can be {@code null} (in such case, "File" is used)
     * @return error if it is not valid, {@code null} otherwise
     */
    public static String validateCommand(@NullAllowed String command, @NullAllowed String executableName) {
        String executable = null;
        if (command != null) {
            executable = ExternalExecutable.parseCommand(command).first();
        }
        if (executableName == null) {
            return validateFile(executable, false);
        }
        return validateFile(executableName, executable, false);
    }

    @NbBundle.Messages("ExternalExecutableValidator.validateFile.file=File")
    @CheckForNull
    private static String validateFile(String filePath, boolean writable) {
        return validateFile(Bundle.ExternalExecutableValidator_validateFile_file(), filePath, writable);
    }

    @NbBundle.Messages({
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.missing={0} must be selected.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notAbsolute={0} must be an absolute path.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notFile={0} must be a valid file.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notReadable={0} is not readable.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notWritable={0} is not writable."
    })
    @CheckForNull
    private static String validateFile(String source, String filePath, boolean writable) {
        if (filePath == null
                || filePath.trim().isEmpty()) {
            return Bundle.ExternalExecutableValidator_validateFile_missing(source);
        }

        File file = new File(filePath);
        if (!file.isAbsolute()) {
            return Bundle.ExternalExecutableValidator_validateFile_notAbsolute(source);
        } else if (!file.isFile()) {
            return Bundle.ExternalExecutableValidator_validateFile_notFile(source);
        } else if (!file.canRead()) {
            return Bundle.ExternalExecutableValidator_validateFile_notReadable(source);
        } else if (writable && !file.canWrite()) {
            return Bundle.ExternalExecutableValidator_validateFile_notWritable(source);
        }
        return null;
    }

}
