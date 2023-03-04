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

package org.netbeans.modules.php.spi.framework.commands;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.api.framework.ui.commands.FrameworkCommandChooser;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Support for running framework commands.
 */
public abstract class FrameworkCommandSupport {

    private static final Logger LOGGER = Logger.getLogger(FrameworkCommandSupport.class.getName());
    // @GuardedBy(COMMANDS_CACHE)
    private static final Map<PhpModule, Map<String, List<FrameworkCommand>>> COMMANDS_CACHE = new WeakHashMap<>();

    private static final RequestProcessor RP = new RequestProcessor(FrameworkCommandSupport.class);

    protected final PhpModule phpModule;

    // @GuardedBy(this)
    private PluginListener pluginListener;


    protected FrameworkCommandSupport(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    /**
     * Get the name of the framework; it's used in the UI for selecting a command.
     * @return the name of the framework.
     */
    public abstract String getFrameworkName();

    /**
     * Run command for the given command descriptor.
     * @param commandDescriptor descriptor for the selected framework command
     * @see CommandDescriptor
     */
    public abstract void runCommand(CommandDescriptor commandDescriptor, Runnable postExecution);

    /**
     * Get options path for {@link ExecutionDescriptor execution descriptor}, can be <code>null</code>.
     * @return options path, can be <code>null</code> if not needed.
     */
    protected abstract String getOptionsPath();

    /**
     * Get the plugin directory to which a {@link FileChangeListener} is added
     * (commands are refreshed if any change in this directory happens).
     * @return the plugin directory or <code>null</code> if the framework does not have such directory
     */
    protected abstract File getPluginsDirectory();

    /**
     * Get the framework commands. Typically in this method script is called and its output is parsed,
     * so the list of {@link FrameworkCommand commands} can be returned.
     * @return list of {@link FrameworkCommand commands}, can be <code>null</code> (typically if any error occurs).
     */
    protected abstract List<FrameworkCommand> getFrameworkCommandsInternal();

    /**
     * Get {@link PhpModule PHP module} for which this framework command support is created.
     * @return {@link PhpModule PHP module} for which this framework command support is created, never <code>null</code>.
     */
    public final PhpModule getPhpModule() {
        return phpModule;
    }

    /**
     * Get framework commands, can be empty or <code>null</code> if not known already.
     * @return list of {@link FrameworkCommand framework commands} or <code>null</code> if not known already.
     */
    public final List<FrameworkCommand> getFrameworkCommands() {
        List<FrameworkCommand> frameworkCommands = null;
        synchronized (COMMANDS_CACHE) {
            Map<String, List<FrameworkCommand>> moduleCommands = COMMANDS_CACHE.get(phpModule);
            if (moduleCommands != null) {
                frameworkCommands = moduleCommands.get(getFrameworkName());
            }
        }
        return frameworkCommands;
    }

    /**
     * Show the panel with framework commands with possibility to run any.
     * @see #runCommand(CommandDescriptor)
     */
    public final void openPanel() {
        FrameworkCommandChooser.open(this);
    }

    final void refreshFrameworkCommands() {
        List<FrameworkCommand> freshCommands = getFrameworkCommandsInternal();

        File plugins = getPluginsDirectory();
        if (plugins != null) {
            // intentionally used isFile() because directory does not need to exist
            assert !plugins.isFile() : "Plugins is expected to be a directory: " + plugins;
            synchronized (this) {
                if (pluginListener == null) {
                    pluginListener = new PluginListener();
                    FileUtil.addFileChangeListener(pluginListener, plugins);
                }
            }
        }
        synchronized (COMMANDS_CACHE) {
            Map<String, List<FrameworkCommand>> moduleCommands = COMMANDS_CACHE.get(phpModule);
            if (moduleCommands == null) {
                moduleCommands = new HashMap<>();
            }
            moduleCommands.put(getFrameworkName(), freshCommands);
            COMMANDS_CACHE.put(phpModule, moduleCommands);
        }
    }

    /**
     * Refresh framework commands in background.
     * @param post {@link Runnable} that is run afterwards, can be <code>null</code>.
     */
    public final void refreshFrameworkCommandsLater(final Runnable post) {
        RP.execute(new Runnable() {
            @Override
            public void run() {
                refreshFrameworkCommands();
                if (post != null) {
                    post.run();
                }
            }
        });
    }

    //~ Inner classes

    private class PluginListener implements FileChangeListener {

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            changed();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            changed();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            changed();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            changed();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            changed();
        }

        private void changed() {
            synchronized (COMMANDS_CACHE) {
                COMMANDS_CACHE.remove(getPhpModule());
            }
        }
    }

    /**
     * Descriptor for the selected framework command.
     * @see org.netbeans.modules.php.api.framework.ui.commands.FrameworkCommandChooser#open(FrameworkCommandSupport)
     */
    public static final class CommandDescriptor {

        private final FrameworkCommand task;
        private final String[] params;
        private final boolean debug;

        public CommandDescriptor(FrameworkCommand task, String params, boolean debug) {
            Parameters.notNull("task", task);
            Parameters.notNull("params", params);

            this.task = task;
            this.params = Utilities.parseParameters(params.trim());
            this.debug = debug;
        }

        public FrameworkCommand getFrameworkCommand() {
            return task;
        }

        public String[] getCommandParams() {
            return params.clone();
        }

        public boolean isDebug() {
            return debug;
        }
    }

}
