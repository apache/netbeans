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
