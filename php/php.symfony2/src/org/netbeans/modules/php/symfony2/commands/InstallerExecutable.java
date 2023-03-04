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
package org.netbeans.modules.php.symfony2.commands;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.symfony2.options.SymfonyOptions;
import org.netbeans.modules.php.symfony2.options.SymfonyOptionsValidator;
import org.netbeans.modules.php.symfony2.ui.options.SymfonyOptionsPanelController;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class InstallerExecutable {

    private static final Logger LOGGER = Logger.getLogger(InstallerExecutable.class.getName());

    public static final String NAME = "symfony"; // NOI18N

    private static final String ANSI_PARAM = "--ansi"; // NOI18N
    private static final String NO_INTERACTION_PARAM = "--no-interaction"; // NOI18N
    private static final String NEW_PARAM = "new"; // NOI18N
    private static final String LTS_PARAM = "lts"; // NOI18N

    private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "nb-symfony23"); // NOI18N

    private final PhpModule phpModule;
    private final String installerPath;

    // @GuardedBy("this")
    private File workDir = null;


    InstallerExecutable(String installerPath, @NullAllowed PhpModule phpModule) {
        assert installerPath != null;
        assert phpModule != null;
        this.installerPath = installerPath;
        this.phpModule = phpModule;
    }

    @CheckForNull
    public static InstallerExecutable getDefault(@NullAllowed PhpModule phpModule, boolean showOptions) {
        ValidationResult result = new SymfonyOptionsValidator()
                .validate()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptions) {
                UiUtils.showOptions(SymfonyOptionsPanelController.OPTIONS_SUBPATH);
            }
            return null;
        }
        return new InstallerExecutable(SymfonyOptions.getInstance().getInstaller(), phpModule);
    }

    public Future<Integer> run(boolean lts) {
        assert !EventQueue.isDispatchThread();
        resetWorkDir();
        Future<Integer> task = getExecutable()
                .additionalParameters(getRunParams(lts))
                .run(getDescriptor());
        assert task != null : installerPath;
        return task;
    }

    public File getSymfony2Dir() {
        assert assertWorkDirExists();
        return new File(getWorkDir(), getNewDirectoryName());
    }

    private List<String> getRunParams(boolean lts) {
        List<String> params = new ArrayList<>(5);
        params.add(ANSI_PARAM);
        params.add(NO_INTERACTION_PARAM);
        params.add(NEW_PARAM);
        params.add(getNewDirectoryName());
        if (lts) {
            params.add(LTS_PARAM);
        }
        return params;
    }

    private String getNewDirectoryName() {
        return phpModule.getName();
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "InstallerExecutable.run.title=Symfony 2/3 ({0})",
    })
    private PhpExecutable getExecutable() {
        return new PhpExecutable(installerPath)
                .workDir(getWorkDir())
                .displayName(Bundle.InstallerExecutable_run_title(phpModule.getDisplayName()))
                .optionsSubcategory(SymfonyOptionsPanelController.OPTIONS_SUBPATH);
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(SymfonyOptionsPanelController.getOptionsPath())
                .rerunCondition(new ExecutionDescriptor.RerunCondition() {
                    @Override
                    public void addChangeListener(ChangeListener listener) {
                        // noop
                    }
                    @Override
                    public void removeChangeListener(ChangeListener listener) {
                        // noop
                    }
                    @Override
                    public boolean isRerunPossible() {
                        return false;
                    }
                });
    }

    private synchronized File getWorkDir() {
        if (workDir == null) {
            try {
                workDir = Files.createTempDirectory("nb-symfony23-").toFile(); // NOI18N
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                workDir = new File(TMP_DIR, new BigInteger(130, new SecureRandom()).toString(32));
            }
            if (!workDir.isDirectory()) {
                if (!workDir.mkdirs()) {
                    LOGGER.log(Level.INFO, "Cannot create TMP dir {0}", workDir);
                }
            }
            FileUtil.refreshFor(workDir);
        }
        return workDir;
    }

    private synchronized void resetWorkDir() {
        workDir = null;
    }

    private synchronized boolean assertWorkDirExists() {
        assert workDir != null;
        assert workDir.isDirectory() : workDir;
        return true;
    }

    @CheckForNull
    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getErrors().get(0).getMessage();
        }
        return result.getWarnings().get(0).getMessage();
    }

}
