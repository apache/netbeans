/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.doctrine2.commands;

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
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.doctrine2.options.Doctrine2Options;
import org.netbeans.modules.php.doctrine2.ui.options.Doctrine2OptionsPanelController;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Represents <a href="http://doctrine-project.org/">doctrine</a> command line tool.
 */
public final class Doctrine2Script {

    private static final Logger LOGGER = Logger.getLogger(Doctrine2Script.class.getName());

    public static final String SCRIPT_NAME = "doctrine"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);

    private static final String XML_CHARSET_NAME = "UTF-8"; // NOI18N

    private static final List<String> DEFAULT_PARAMS = Collections.singletonList("--ansi"); // NOI18N
    private static final List<String> LIST_PARAMS = Arrays.asList(
            "list", // NOI18N
            "--format", // NOI18N
            "xml"); // NOI18N

    private final String doctrine2Path;


    private Doctrine2Script(String doctrine2Path) {
        this.doctrine2Path = doctrine2Path;
    }

    /**
     * Get the default, <b>valid only</b> Doctrine2 script.
     * @return the default, <b>valid only</b> Doctrine2 script.
     * @throws InvalidPhpExecutableException if Doctrine2 script is not valid.
     */
    public static Doctrine2Script getDefault() throws InvalidPhpExecutableException {
        String doctrine2Path = Doctrine2Options.getInstance().getScript();
        String error = validate(doctrine2Path);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new Doctrine2Script(doctrine2Path);
    }

    @NbBundle.Messages("Doctrine2Script.script.label=Doctrine2 script")
    public static String validate(String command) {
        return PhpExecutableValidator.validateCommand(command, Bundle.Doctrine2Script_script_label());
    }

    public void runCommand(PhpModule phpModule, List<String> parameters, Runnable postExecution) {
        new PhpExecutable(doctrine2Path)
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()))
                .displayName(getDisplayName(phpModule))
                .additionalParameters(getAllParameters(parameters))
                .run(getDescriptor(postExecution));
    }

    public List<Doctrine2CommandVO> getCommands(PhpModule phpModule) {
        File tmpFile;
        try {
            tmpFile = File.createTempFile("nb-doctrine2-commands-", ".xml"); // NOI18N
            tmpFile.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }
        Future<Integer> result = new PhpExecutable(doctrine2Path)
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()))
                .fileOutput(tmpFile, XML_CHARSET_NAME, true)
                .additionalParameters(LIST_PARAMS)
                .run(getSilentDescriptor());
        try {
            if (result == null || result.get() != 0) {
                // error => rerun with output window
                runCommand(phpModule, LIST_PARAMS);
                return null;
            }
        } catch (CancellationException ex) {
            // canceled
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, Doctrine2OptionsPanelController.OPTIONS_SUBPATH);
            return null;
        }
        List<Doctrine2CommandVO> commandsVO = new ArrayList<>();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile), XML_CHARSET_NAME));
            Doctrine2CommandsXmlParser.parse(reader, commandsVO);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            if (!tmpFile.delete()) {
                LOGGER.info("Cannot delete temporary file");
            }
        }
        if (commandsVO.isEmpty()) {
            // error => rerun with output window
            runCommand(phpModule, LIST_PARAMS);
            return null;
        }
        return commandsVO;
    }

    private void runCommand(PhpModule phpModule, List<String> parameters) {
        runCommand(phpModule, parameters, null);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Doctrine2Script.command.title=Doctrine2 ({0})"
    })
    private String getDisplayName(PhpModule phpModule) {
        return Bundle.Doctrine2Script_command_title(phpModule.getDisplayName());
    }

    private List<String> getAllParameters(List<String> params) {
        List<String> allParams = new ArrayList<>(DEFAULT_PARAMS.size() + params.size());
        allParams.addAll(DEFAULT_PARAMS);
        allParams.addAll(params);
        return allParams;
    }

    private ExecutionDescriptor getDescriptor(Runnable postExecution) {
        ExecutionDescriptor executionDescriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(Doctrine2OptionsPanelController.getOptionsPath())
                .inputVisible(false);
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
