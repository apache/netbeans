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
package org.netbeans.modules.php.api.executable;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.util.FileUtils;

/**
 * Validator for {@link PhpExecutable}.
 */
public final class PhpExecutableValidator {

    private PhpExecutableValidator() {
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
            executable = PhpExecutable.parseCommand(command).first();
        }
        if (executableName == null) {
            return FileUtils.validateFile(executable, false);
        }
        return FileUtils.validateFile(executableName, executable, false);
    }

    /**
     * Validate the given command and return error if it is not valid, {@code null} otherwise.
     * @param command command to be validated
     * @param validationHandler handler to be used for validation
     * @return error if it is not valid, {@code null} otherwise
     */
    public static String validateCommand(@NonNull String command, @NonNull ValidationHandler validationHandler) {
        return validationHandler.validate(PhpExecutable.parseCommand(command).first());
    }

    //~ Inner classes

    /**
     * Handler for {@link PhpExecutableValidator#validateCommand(String, PhpExecutableValidator.ValidationHandler) custom validation}.
     */
    public interface ValidationHandler {

        /**
         * Validate the given executable.
         * @param executable executable to be validated (typically the full path of a file)
         * @return error if it is not valid, {@code null} otherwise
         */
        String validate(String executable);

    }

}
