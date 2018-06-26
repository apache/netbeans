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
package org.netbeans.modules.php.composer.api;

import java.io.File;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;

/**
 * Composer executable.
 * @since 0.8
 */
public final class Composer {

    private final org.netbeans.modules.php.composer.commands.Composer composer;


    private Composer(org.netbeans.modules.php.composer.commands.Composer composer) {
        this.composer = composer;
    }

    /**
     * Get new instance of the default, <b>valid only</b> Composer.
     * @return new instance of the default, <b>valid only</b> Composer.
     * @throws InvalidPhpExecutableException if Composer is not valid.
     */
    public static Composer getDefault() throws InvalidPhpExecutableException {
        return new Composer(org.netbeans.modules.php.composer.commands.Composer.getDefault());
    }

    /**
     * Get the current working directory.
     * <p>
     * If the directory is {@code null}, it means that source directory of the given PHP module will be used, if possible.
     * @return current working directory, can be {@code null}
     * @since 1.12
     */
    @CheckForNull
    public File getWorkDir() {
        return composer.getWorkDir();
    }

    /**
     * Set current working directory. If it is not {@code null}, existing directory must be given.
     * @param workDir existing working directory, can be {@code null}
     * @return self instance
     * @throws IllegalArgumentException if the given file is not {@code null} and is not existing directory
     * @since 1.12
     */
    public Composer setWorkDir(@NullAllowed File workDir) {
        if (workDir != null
                && !workDir.isDirectory()) {
            throw new IllegalArgumentException("Existing directory must be provided: " + workDir);
        }
        composer.setWorkDir(workDir);
        return this;
    }

    /**
     * Invokes <code>composer init</code> command.
     * @param phpModule PHP module to run the command for
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     *         or {@code null} if the executable cannot be run
     */
    public Future<Integer> init(PhpModule phpModule) {
        return composer.init(phpModule);
    }

    /**
     * Invokes <code>composer install</code> command.
     * @param phpModule PHP module to run the command for
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     *         or {@code null} if the executable cannot be run
     */
    public Future<Integer> install(PhpModule phpModule) {
        return composer.install(phpModule);
    }

    /**
     * Invokes <code>composer install --dev</code> command.
     * @param phpModule PHP module to run the command for
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     *         or {@code null} if the executable cannot be run
     */
    public Future<Integer> installDev(PhpModule phpModule) {
        return composer.installDev(phpModule);
    }

    /**
     * Invokes <code>composer install --no-dev</code> command.
     * @param phpModule PHP module to run the command for
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     *         or {@code null} if the executable cannot be run
     * @since 0.16
     */
    public Future<Integer> installNoDev(PhpModule phpModule) {
        return composer.installNoDev(phpModule);
    }

    /**
     * Invokes <code>composer update</code> command.
     * @param phpModule PHP module to run the command for
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     *         or {@code null} if the executable cannot be run
     */
    public Future<Integer> update(PhpModule phpModule) {
        return composer.update(phpModule);
    }

    /**
     * Invokes <code>composer update --dev</code> command.
     * @param phpModule PHP module to run the command for
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     *         or {@code null} if the executable cannot be run
     */
    public Future<Integer> updateDev(PhpModule phpModule) {
        return composer.updateDev(phpModule);
    }

    /**
     * Invokes <code>composer update --no-dev</code> command.
     * @param phpModule PHP module to run the command for
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     *         or {@code null} if the executable cannot be run
     * @since 0.16
     */
    public Future<Integer> updateNoDev(PhpModule phpModule) {
        return composer.updateNoDev(phpModule);
    }

    /**
     * Invokes <code>composer validate</code> command.
     * @param phpModule PHP module to run the command for
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     *         or {@code null} if the executable cannot be run
     */
    public Future<Integer> validate(PhpModule phpModule) {
        return composer.validate(phpModule);
    }

    /**
     * Invokes <code>composer self-update</code> command.
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     *         or {@code null} if the executable cannot be run
     */
    public Future<Integer> selfUpdate() {
        return composer.selfUpdate();
    }

}
