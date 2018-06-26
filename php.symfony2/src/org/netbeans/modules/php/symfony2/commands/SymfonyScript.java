/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.symfony2.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.symfony2.SymfonyVersion;
import org.netbeans.modules.php.symfony2.ui.options.SymfonyOptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.InputOutput;

/**
 * Represents Symfony 2/3 command line tool.
 */
public final class SymfonyScript {

    private static final Logger LOGGER = Logger.getLogger(SymfonyScript.class.getName());

    private static final String XML_CHARSET_NAME = "UTF-8"; // NOI18N

    // #239750
    private static final String SHELL_INTERACTIVE = "SHELL_INTERACTIVE"; // NOI18N

    private static final List<String> CACHE_CLEAR_COMMAND = Arrays.asList("cache:clear", "--verbose"); // NOI18N
    private static final List<String> CACHE_WARMUP_COMMAND = Collections.singletonList("cache:warmup"); // NOI18N
    private static final List<String> LIST_COMMANDS_COMMAND = Arrays.asList("list", "--format", "xml"); // NOI18N

    private static final List<String> DEFAULT_PARAMS = Collections.singletonList("--ansi"); // NOI18N

    public static final String SCRIPT_NAME = "console"; // NOI18N

    private final String symfony2Path;


    private SymfonyScript(String symfony2Path) {
        this.symfony2Path = symfony2Path;
    }

    /**
     * Get the project specific, <b>valid only</b> Symfony2 script. If not found, {@code null} is returned.
     * @param phpModule PHP module for which Symfony2 script is taken
     * @param warn <code>true</code> if user is warned when the Symfony2 script is not valid
     * @return Symfony2 console script or {@code null} if the script is not valid
     */
    @Messages({
        "# {0} - error message",
        "MSG_InvalidSymfony2Script=<html>Project''s Symfony console script is not valid.<br>({0})"
    })
    @CheckForNull
    public static SymfonyScript forPhpModule(PhpModule phpModule, boolean warn) {
        String console = null;
        SymfonyVersion symfonyVersion = SymfonyVersion.forPhpModule(phpModule);
        if (symfonyVersion != null) {
            FileObject script = symfonyVersion.getConsole();
            if (script != null) {
                console = FileUtil.toFile(script).getAbsolutePath();
            }
        }
        String error = validate(console);
        if (error == null) {
            return new SymfonyScript(console);
        }
        if (warn) {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                    Bundle.MSG_InvalidSymfony2Script(error),
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(message);
        }
        return null;
    }

    @Messages("SymfonyScript.script.label=Symfony console")
    public static String validate(String command) {
        return PhpExecutableValidator.validateCommand(command, Bundle.SymfonyScript_script_label());
    }

    public void clearCache(PhpModule phpModule) {
        runCommand(phpModule, CACHE_CLEAR_COMMAND, null);
    }

    public void cacheWarmUp(PhpModule phpModule) {
        runCommand(phpModule, CACHE_WARMUP_COMMAND, null);
    }

    public void runCommand(PhpModule phpModule, List<String> parameters, Runnable postExecution) {
        createExecutable(phpModule)
                .displayName(getDisplayName(phpModule))
                .additionalParameters(getAllParameters(parameters))
                .run(getDescriptor(postExecution));
    }

    public List<SymfonyCommandVO> getCommands(PhpModule phpModule) {
        File tmpFile;
        try {
            tmpFile = File.createTempFile("nb-symfony23-commands-", ".xml"); // NOI18N
            tmpFile.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }
        Future<Integer> result = createExecutable(phpModule)
                .fileOutput(tmpFile, XML_CHARSET_NAME, true)
                .additionalParameters(LIST_COMMANDS_COMMAND)
                .run(getSilentDescriptor());
        try {
            if (result == null || result.get() != 0) {
                // error => rerun with output window
                runCommand(phpModule, LIST_COMMANDS_COMMAND, null);
                return null;
            }
        } catch (CancellationException ex) {
            // canceled
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, SymfonyOptionsPanelController.OPTIONS_SUBPATH);
            return null;
        }
        List<SymfonyCommandVO> commandsVO = new ArrayList<>();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile), XML_CHARSET_NAME));
            SymfonyCommandsXmlParser.parse(reader, commandsVO);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            if (!tmpFile.delete()) {
                LOGGER.info("Cannot delete temporary file");
            }
        }
        if (commandsVO.isEmpty()) {
            // error => rerun with output window
            runCommand(phpModule, LIST_COMMANDS_COMMAND, null);
            return null;
        }
        return commandsVO;
    }

    private PhpExecutable createExecutable(PhpModule phpModule) {
        return new PhpExecutable(symfony2Path)
                .environmentVariables(Collections.singletonMap(SHELL_INTERACTIVE, "true")) // NOI18N
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()));
    }

    @NbBundle.Messages({
        "# {0} - symfony version",
        "# {1} - project name",
        "SymfonyScript.command.title={0} ({1})"
    })
    private String getDisplayName(PhpModule phpModule) {
        SymfonyVersion symfonyVersion = SymfonyVersion.forPhpModule(phpModule);
        assert symfonyVersion != null : phpModule;
        return Bundle.SymfonyScript_command_title(symfonyVersion.getFrameworkName(true), phpModule.getDisplayName());
    }

    private List<String> getAllParameters(List<String> params) {
        List<String> allParams = new ArrayList<>(DEFAULT_PARAMS.size() + params.size());
        allParams.addAll(DEFAULT_PARAMS);
        allParams.addAll(params);
        return allParams;
    }

    private ExecutionDescriptor getDescriptor(Runnable postExecution) {
        ExecutionDescriptor executionDescriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(SymfonyOptionsPanelController.getOptionsPath())
                .inputVisible(true);
        if (postExecution != null) {
            executionDescriptor = executionDescriptor.postExecution(postExecution);
        }
        return executionDescriptor;
    }

    private ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL);
    }

}
