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

package org.netbeans.modules.php.zend;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.netbeans.modules.php.zend.commands.ZendCommand;
import org.netbeans.modules.php.zend.ui.options.ZendOptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public final class ZendScript {

    private static final Logger LOGGER = Logger.getLogger(ZendScript.class.getName());

    public static final String SCRIPT_NAME = "zf"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);

    public static final String OPTIONS_ID = "Zend"; // NOI18N
    public static final String OPTIONS_SUB_PATH = UiUtils.FRAMEWORKS_AND_TOOLS_SUB_PATH+"/"+OPTIONS_ID; // NOI18N

    static final String COMMANDS_SEPARATOR = ":NB:"; // NOI18N

    // environment variables
    private static final String ENV_INCLUDE_PATH_PREPEND = "ZEND_TOOL_INCLUDE_PATH_PREPEND"; // NOI18N
    private static final Map<String, String> ENVIRONMENT_VARIABLES;

    // commands
    private static final String HELP_COMMAND = "?"; // NOI18N
    private static final List<String> INIT_PROJECT_COMMAND = Arrays.asList("create", "project", "."); // NOI18N
    private static final List<String> CREATE_CONFIG_COMMAND = Arrays.asList("create", "config"); // NOI18N
    private static final List<String> ENABLE_CONFIG_COMMAND = Arrays.asList("enable", "config.provider", "NetBeansCommandsProvider"); // NOI18N
    private static final List<String> SHOW_COMMANDS_COMMAND = Arrays.asList("show", "nb-commands", COMMANDS_SEPARATOR); // NOI18N

    // commands provider
    private static final String COMMANDS_PROVIDER_REL_PATH = "zend/NetBeansCommandsProvider.php"; // NOI18N
    private static final File COMMANDS_PROVIDER;

    static {
        File commandsProvider = null;
        try {
            commandsProvider = FileUtil.normalizeFile(
                    InstalledFileLocator.getDefault().locate(COMMANDS_PROVIDER_REL_PATH, "org.netbeans.modules.php.zend", false).getCanonicalFile()); // NOI18N
            if (commandsProvider == null || !commandsProvider.isFile()) {
                throw new IllegalStateException("Could not locate file " + COMMANDS_PROVIDER_REL_PATH);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not locate file " + COMMANDS_PROVIDER_REL_PATH, ex);
        }
        COMMANDS_PROVIDER = commandsProvider;
        ENVIRONMENT_VARIABLES = Collections.singletonMap(ZendScript.ENV_INCLUDE_PATH_PREPEND, COMMANDS_PROVIDER.getParentFile().getAbsolutePath());
    }

    private final String zendPath;


    private ZendScript(String zendPath) {
        this.zendPath = zendPath;
    }

    /**
     * Get the default, <b>valid only</b> Zend script.
     * @return the default, <b>valid only</b> Zend script.
     * @throws InvalidPhpExecutableException if Zend script is not valid.
     */
    public static ZendScript getDefault() throws InvalidPhpExecutableException {
        return getCustom(ZendOptions.getInstance().getZend());
    }

    /**
     * Get custom, <b>valid only</b> Zend script.
     * @return the custom, <b>valid only</b> Zend script.
     * @throws InvalidPhpExecutableException if Zend script is not valid.
     */
    public static ZendScript getCustom(String zendPath) throws InvalidPhpExecutableException {
        String error = validate(zendPath);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new ZendScript(zendPath);
    }

    @NbBundle.Messages("ZendScript.script.label=Zend script")
    public static String validate(String zendPath) {
        return PhpExecutableValidator.validateCommand(zendPath, Bundle.ZendScript_script_label());
    }

    /**
     * @return full IDE options Zend path
     */
    public static String getOptionsPath() {
        return UiUtils.FRAMEWORKS_AND_TOOLS_OPTIONS_PATH + "/" + getOptionsSubPath(); // NOI18N
    }

    /**
     * @return IDE options Zend subpath
     */
    public static String getOptionsSubPath() {
        return OPTIONS_SUB_PATH;
    }

    public String getHelp(PhpModule phpModule, List<String> parameters) {
        assert !EventQueue.isDispatchThread();
        assert phpModule != null;

        List<String> allParameters = new ArrayList<>(parameters);
        allParameters.add(HELP_COMMAND);
        HelpLineProcessor lineProcessor = new HelpLineProcessor();
        Future<Integer> result = createPhpExecutable()
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()))
                .displayName(getDisplayName(phpModule))
                .additionalParameters(allParameters)
                .run(getSilentDescriptor(), getOutProcessorFactory(lineProcessor));
        if (result == null) {
            // some error
            return ""; // NOI18N
        }
        try {
            result.get();
        } catch (CancellationException ex) {
            // canceled
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, getOptionsSubPath());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return lineProcessor.getHelp();
    }

    public void registerNetBeansProvider() {
        ExecutionDescriptor descriptor = getDescriptor(null);
        try {
            // create config
            Future<Integer> result = createPhpExecutable()
                    .displayName(getDisplayName())
                    .additionalParameters(CREATE_CONFIG_COMMAND)
                    .run(descriptor);
            if (result != null) {
                result.get();
            }

            descriptor = descriptor.noReset(true);
            // enable config
            result = createPhpExecutable()
                    .displayName(getDisplayName())
                    .additionalParameters(ENABLE_CONFIG_COMMAND)
                    .run(descriptor);
            if (result != null) {
                result.get();
            }

            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                NbBundle.getMessage(ZendScript.class, "MSG_ProviderRegistrationInfo"),
                NotifyDescriptor.INFORMATION_MESSAGE));
        } catch (CancellationException ex) {
            // canceled
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, getOptionsSubPath());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean initProject(PhpModule phpModule) {
        try {
            File sources = FileUtil.toFile(phpModule.getSourceDirectory());
            Future<Integer> result = createPhpExecutable()
                    .workDir(sources)
                    .displayName(getDisplayName(phpModule))
                    .additionalParameters(INIT_PROJECT_COMMAND)
                    .warnUser(false)
                    .run(getDescriptor(null));
            if (result != null) {
                result.get();
                // #217987
                FileUtil.refreshFor(sources);
            }
        } catch (CancellationException ex) {
            // canceled
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, getOptionsSubPath());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return ZendPhpFrameworkProvider.getInstance().isInPhpModule(phpModule);
    }

    @NbBundle.Messages("ZendScript.noCommands.registerProvider=No commands found. Do you want to register NetBeans provider (for ZF 1.10 or newer)?")
    public List<FrameworkCommand> getCommands(PhpModule phpModule) {
        CommandsLineProcessor lineProcessor = new CommandsLineProcessor(phpModule);
        List<FrameworkCommand> freshCommands;
        Future<Integer> task = createPhpExecutable()
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()))
                .additionalParameters(SHOW_COMMANDS_COMMAND)
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
        // error => rerun with output window
        runCommand(phpModule, SHOW_COMMANDS_COMMAND, null);
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                Bundle.ZendScript_noCommands_registerProvider(),
                NotifyDescriptor.YES_NO_OPTION);
        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
            registerNetBeansProvider();
        }
        return null;
    }

    public void runCommand(PhpModule phpModule, List<String> parameters, Runnable postExecution) {
        createPhpExecutable()
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()))
                .displayName(getDisplayName(phpModule))
                .additionalParameters(parameters)
                .run(getDescriptor(postExecution));
    }

    private PhpExecutable createPhpExecutable() {
        return new PhpExecutable(zendPath)
                .viaPhpInterpreter(false)
                .viaAutodetection(false)
                .environmentVariables(ENVIRONMENT_VARIABLES);
    }

    private ExecutionDescriptor getDescriptor(Runnable postExecution) {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(getOptionsPath())
                .inputVisible(false)
                .postExecution(postExecution);
    }

    private ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL);
    }

    private ExecutionDescriptor.InputProcessorFactory2 getOutProcessorFactory(final LineProcessor lineProcessor) {
        return new ExecutionDescriptor.InputProcessorFactory2() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.ansiStripping(InputProcessors.bridge(lineProcessor));
            }
        };
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "ZendScript.command.title=Zend ({0})",
    })
    private String getDisplayName(PhpModule phpModule) {
        return Bundle.ZendScript_command_title(phpModule.getDisplayName());
    }

    @NbBundle.Messages({
        "ZendScript.command.title.pure=Zend",
    })
    private String getDisplayName() {
        return Bundle.ZendScript_command_title_pure();
    }

    //~ Inner classes

    static class HelpLineProcessor implements LineProcessor {
        private final StringBuilder buffer = new StringBuilder(2000);

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

    static final class CommandsLineProcessor implements LineProcessor {

        private final PhpModule phpModule;

        // @GuardedBy(commands)
        private final List<FrameworkCommand> commands = new LinkedList<>();


        public CommandsLineProcessor(PhpModule phpModule) {
            this.phpModule = phpModule;
        }

        @Override
        public void processLine(String line) {
            if (!StringUtils.hasText(line)) {
                return;
            }
            // # 179255
            if (!line.contains(COMMANDS_SEPARATOR)) {
                // error occured
                return;
            }
            String trimmed = line.trim();
            List<String> exploded = StringUtils.explode(trimmed, COMMANDS_SEPARATOR);
            assert exploded.size() > 0;
            String command = exploded.get(0);
            String description = ""; // NOI18N
            if (exploded.size() > 1) {
                description = exploded.get(1);
            }
            synchronized (commands) {
                commands.add(new ZendCommand(phpModule, StringUtils.explode(command, " ").toArray(new String[0]), description, command)); // NOI18N
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
