/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.symfony;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import org.netbeans.modules.php.symfony.commands.SymfonyCommand;
import org.netbeans.modules.php.symfony.commands.SymfonyCommandVO;
import org.netbeans.modules.php.symfony.commands.SymfonyCommandsXmlParser;
import org.netbeans.modules.php.symfony.ui.options.SymfonyOptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public class SymfonyScript {

    private static final Logger LOGGER = Logger.getLogger(SymfonyScript.class.getName());

    public static final String SCRIPT_NAME = "symfony"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);

    private static final String XML_CHARSET_NAME = "UTF-8"; // NOI18N

    public static final String OPTIONS_ID = "Symfony"; // NOI18N
    public static final String OPTIONS_SUB_PATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH+"/"+OPTIONS_ID; // NOI18N

    private static final String DEFAULT_PARAM = "--color"; // NOI18N

    private static final String INIT_PROJECT_COMMAND = "generate:project"; // NOI18N
    private static final String CLEAR_CACHE_COMMAND = "cache:clear"; // NOI18N
    private static final String INIT_APP_COMMAND = "generate:app"; // NOI18N
    private static final String HELP_COMMAND = "help"; // NOI18N
    private static final String LIST_COMMAND = "list"; // NOI18N
    private static final List<String> LIST_XML_COMMAND = Arrays.asList("list", "--xml"); // NOI18N

    private final String symfonyPath;


    private SymfonyScript(String symfonyPath) {
        this.symfonyPath = symfonyPath;
    }

    /**
     * Get the default, <b>valid only</b> Symfony script.
     * @return the default, <b>valid only</b> Symfony script.
     * @throws InvalidPhpExecutableException if Symfony script is not valid.
     */
    public static SymfonyScript getDefault() throws InvalidPhpExecutableException {
        String symfony = SymfonyOptions.getInstance().getSymfony();
        String error = validate(symfony);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new SymfonyScript(symfony);
    }

    /**
     * Get the project specific, <b>valid only</b> Symfony script. If not found, the {@link #getDefault() default} Symfony script is returned.
     * @param phpModule PHP module for which Symfony script is taken
     * @param warn <code>true</code> if user is warned when the {@link #getDefault() default} Symfony script is returned.
     * @return the project specific, <b>valid only</b> Symfony script.
     * @throws InvalidPhpExecutableException if Symfony script is not valid. If not found, the {@link #getDefault() default} Symfony script is returned.
     * @see #getDefault()
     */
    public static SymfonyScript forPhpModule(PhpModule phpModule, boolean warn) throws InvalidPhpExecutableException {
        String symfony = new File(FileUtil.toFile(phpModule.getSourceDirectory()), SCRIPT_NAME).getAbsolutePath();
        String error = validate(symfony);
        if (error != null) {
            if (warn) {
                Message message = new NotifyDescriptor.Message(
                        NbBundle.getMessage(SymfonyScript.class, "MSG_InvalidProjectSymfonyScript", error),
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(message);
            }
            return getDefault();
        }
        return new SymfonyScript(symfony);
    }

    @NbBundle.Messages("SymfonyScript.script.label=Symfony script")
    public static String validate(String command) {
        return PhpExecutableValidator.validateCommand(command, Bundle.SymfonyScript_script_label());
    }

    /**
     * @return full IDE options Symfony path
     */
    public static String getOptionsPath() {
        return UiUtils.FRAMEWORKS_AND_TOOLS_OPTIONS_PATH + "/" + getOptionsSubPath(); // NOI18N
    }

    /**
     * @return IDE options Symfony subpath
     */
    public static String getOptionsSubPath() {
        return OPTIONS_SUB_PATH;
    }

    public void runCommand(PhpModule phpModule, List<String> parameters, Runnable postExecution) {
        createPhpExecutable(phpModule)
                .displayName(getDisplayName(phpModule))
                .additionalParameters(getAllParams(parameters))
                .run(getDescriptor(postExecution));
    }

    public void clearCache(PhpModule phpModule) {
        runCommand(phpModule, Collections.singletonList(CLEAR_CACHE_COMMAND), null);
    }

    public boolean initProject(PhpModule phpModule, String[] params) {
        String projectName = phpModule.getDisplayName();
        List<String> allParams = new ArrayList<>();
        allParams.add(INIT_PROJECT_COMMAND);
        allParams.add(projectName);
        allParams.addAll(Arrays.asList(params));

        try {
            Future<Integer> result = createPhpExecutable(phpModule)
                    .displayName(getDisplayName(phpModule))
                    .additionalParameters(getAllParams(allParams))
                    .warnUser(false)
                    .run(getDescriptor(null));
            if (result != null) {
                result.get();
            }
        } catch (CancellationException | ExecutionException ex) {
            // cancelled | wizard handles it
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return SymfonyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule);
    }

    public void initApp(PhpModule phpModule, String app, String[] params) {
        assert StringUtils.hasText(app);
        assert params != null;

        List<String> allParams = new ArrayList<>();
        allParams.add(INIT_APP_COMMAND);
        allParams.add(app);
        allParams.addAll(Arrays.asList(params));

        createPhpExecutable(phpModule)
                .displayName(getDisplayName(phpModule))
                .additionalParameters(getAllParams(allParams))
                .warnUser(false)
                .run(getDescriptor(null));
    }

    public String getHelp(PhpModule phpModule, String[] params) {
        assert phpModule != null;

        List<String> allParams = new ArrayList<>();
        allParams.add(HELP_COMMAND);
        allParams.addAll(Arrays.asList(params));

        HelpLineProcessor lineProcessor = new HelpLineProcessor();
        Future<Integer> result = createPhpExecutable(phpModule)
                .displayName(getDisplayName(phpModule))
                .additionalParameters(getAllParams(allParams))
                .run(getSilentDescriptor(), getOutProcessorFactory(lineProcessor));
        try {
            if (result != null) {
                result.get();
            }
        } catch (CancellationException ex) {
            // canceled
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, getOptionsSubPath());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return lineProcessor.getHelp();
    }

    public List<FrameworkCommand> getCommands(PhpModule phpModule) {
        List<FrameworkCommand> freshCommands = getFrameworkCommandsInternalXml(phpModule);
        if (freshCommands != null) {
            return freshCommands;
        }
        freshCommands = getFrameworkCommandsInternalConsole(phpModule);
        if (freshCommands != null) {
            return freshCommands;
        }
        // some error => rerun command with console
        runCommand(phpModule, Collections.singletonList(LIST_COMMAND), null);
        return null;
    }

    private PhpExecutable createPhpExecutable(PhpModule phpModule) {
        return new PhpExecutable(symfonyPath)
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()));
    }

    private List<String> getAllParams(List<String> params) {
        List<String> allParams = new ArrayList<>();
        allParams.add(DEFAULT_PARAM);
        allParams.addAll(params);
        return allParams;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "SymfonyScript.command.title=Symfony ({0})"
    })
    private String getDisplayName(PhpModule phpModule) {
        return Bundle.SymfonyScript_command_title(phpModule.getDisplayName());
    }

    private ExecutionDescriptor getDescriptor(Runnable postExecution) {
        ExecutionDescriptor executionDescriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(OPTIONS_SUB_PATH);
        if (postExecution != null) {
            executionDescriptor = executionDescriptor.postExecution(postExecution);
        }
        return executionDescriptor;
    }

    private ExecutionDescriptor.InputProcessorFactory2 getOutProcessorFactory(final LineProcessor lineProcessor) {
        return new ExecutionDescriptor.InputProcessorFactory2() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.ansiStripping(InputProcessors.bridge(lineProcessor));
            }
        };
    }

    private ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL);
    }

    private List<FrameworkCommand> getFrameworkCommandsInternalXml(PhpModule phpModule) {
        File tmpFile;
        try {
            tmpFile = File.createTempFile("nb-symfony-commands-", ".xml"); // NOI18N
            tmpFile.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }
        Future<Integer> result = createPhpExecutable(phpModule)
                .fileOutput(tmpFile, XML_CHARSET_NAME, true)
                .warnUser(false)
                .additionalParameters(LIST_XML_COMMAND)
                .run(getSilentDescriptor());
        try {
            if (result == null || result.get() != 0) {
                // error
                return null;
            }
        } catch (CancellationException | ExecutionException ex) {
            // cancelled | ignored
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
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
            // error
            return null;
        }
        List<FrameworkCommand> commands = new ArrayList<>(commandsVO.size());
        for (SymfonyCommandVO command : commandsVO) {
            commands.add(new SymfonyCommand(phpModule, command.getCommand(), command.getDescription(), command.getCommand()));
        }
        return commands;
    }

    private List<FrameworkCommand> getFrameworkCommandsInternalConsole(PhpModule phpModule) {
        CommandsLineProcessor lineProcessor = new CommandsLineProcessor(phpModule);
        List<FrameworkCommand> freshCommands;
        Future<Integer> task = createPhpExecutable(phpModule)
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()))
                .additionalParameters(Collections.singletonList(LIST_COMMAND))
                .run(getSilentDescriptor(), getOutProcessorFactory(lineProcessor));
        try {
            if (task != null && task.get().intValue() == 0) {
                freshCommands = lineProcessor.getCommands();
                if (!freshCommands.isEmpty()) {
                    return freshCommands;
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return null;
    }

    //~ Inner classes

    static final class HelpLineProcessor implements LineProcessor {
        private final StringBuilder buffer = new StringBuilder(500);

        @Override
        public void processLine(String line) {
            buffer.append(line);
            buffer.append("\n"); // NOI18N
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }

        public String getHelp() {
            return buffer.toString().trim() + "\n"; // NOI18N
        }
    }

    //~ Inner classes

    static final class CommandsLineProcessor implements LineProcessor {

        private static final Pattern COMMAND_PATTERN = Pattern.compile("^\\:(\\S+)\\s+(.+)$"); // NOI18N
        private static final Pattern PREFIX_PATTERN = Pattern.compile("^(\\w+)$"); // NOI18N

        // @GuardedBy(commands)
        private final List<FrameworkCommand> commands = new ArrayList<>();
        private final PhpModule phpModule;
        private String prefix;


        public CommandsLineProcessor(PhpModule phpModule) {
            this.phpModule = phpModule;
        }

        @Override
        public void processLine(String line) {
            if (!StringUtils.hasText(line)) {
                prefix = null;
                return;
            }

            String trimmed = line.trim();
            Matcher prefixMatcher = PREFIX_PATTERN.matcher(trimmed);
            if (prefixMatcher.matches()) {
                prefix = prefixMatcher.group(1);
            }
            Matcher commandMatcher = COMMAND_PATTERN.matcher(trimmed);
            if (commandMatcher.matches()) {
                String command = commandMatcher.group(1);
                if (prefix != null) {
                    command = prefix + ":" + command; // NOI18N
                }
                String description = commandMatcher.group(2);
                synchronized (commands) {
                    commands.add(new SymfonyCommand(phpModule, command, description, command));
                }
            }
        }

        public List<FrameworkCommand> getCommands() {
            List<FrameworkCommand> copy;
            synchronized (commands) {
                copy = new ArrayList<>(commands);
            }
            return copy;
        }

        @Override
        public void close() {
        }

        @Override
        public void reset() {
        }

    }

}
