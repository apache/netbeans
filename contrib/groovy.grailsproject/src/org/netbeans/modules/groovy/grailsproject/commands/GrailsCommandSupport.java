/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.groovy.grailsproject.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.extexecution.input.LineProcessors;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.GrailsServerState;
import org.netbeans.modules.groovy.grailsproject.actions.RefreshProjectRunnable;
import org.netbeans.modules.groovy.grailsproject.config.BuildConfig;
import org.netbeans.modules.groovy.grailsproject.debug.GrailsDebugger;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public final class GrailsCommandSupport {

    private static final ExecutionDescriptor GRAILS_DESCRIPTOR = new ExecutionDescriptor()
            .controllable(true).frontWindow(true).inputVisible(true)
                .showProgress(true).optionsPath(GroovySettings.GROOVY_OPTIONS_CATEGORY);

    private static final ExecutionDescriptor RUN_DESCRIPTOR = GRAILS_DESCRIPTOR.showSuspended(true);

    private static final InputProcessorFactory ANSI_STRIPPING = new AnsiStrippingInputProcessorFactory();

    private static final Logger LOGGER = Logger.getLogger(GrailsCommandSupport.class.getName());

    private static final String WEB_APP_DIR = "web-app"; // NOI18N

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final GrailsProject project;

    private List<GrailsCommand> commands;

    private BuildConfigListener buildConfigListener;

    private ProjectConfigListener projectConfigListener;

    public GrailsCommandSupport(GrailsProject project) {
        this.project = project;
    }

    public synchronized List<GrailsCommand> getGrailsCommands() {
        return commands;
    }

    public ExecutionDescriptor getRunDescriptor(boolean debug) {
        return getDescriptor(GrailsPlatform.IDE_RUN_COMMAND, debug);
    }

    public ExecutionDescriptor getDescriptor(String command) {
        return getDescriptor(command, false);
    }

    public ExecutionDescriptor getDescriptor(String command, boolean debug) {
        return getDescriptor(command, null, debug);
    }

    public ExecutionDescriptor getDescriptor(String command, InputProcessorFactory outFactory) {
        return getDescriptor(command, outFactory, false);
    }

    public ExecutionDescriptor getDescriptor(String command, InputProcessorFactory outFactory, final boolean debug) {
        if (GrailsPlatform.IDE_RUN_COMMAND.equals(command)) {

            ExecutionDescriptor descriptor = RUN_DESCRIPTOR;
            InputProcessorFactory urlFactory = new InputProcessorFactory() {
                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    LineProcessor lineProcessor = null;
                    if (debug) {
                        lineProcessor = LineProcessors.proxy(
                                new ServerOutputProcessor(project, debug),
                                new DebugOutputProcessor(project));
                    } else {
                        lineProcessor = new ServerOutputProcessor(project, debug);
                    }

                    return InputProcessors.proxy(defaultProcessor,
                            InputProcessors.bridge(lineProcessor));
                }
            };

            descriptor = descriptor.outProcessorFactory(
                    createInputProcessorFactory(urlFactory, outFactory));
            return descriptor;
        }

        InputProcessorFactory debugFactory = null;
        if (debug) {
            debugFactory = new InputProcessorFactory() {

                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    return InputProcessors.proxy(defaultProcessor,
                            InputProcessors.bridge(new DebugOutputProcessor(project)));
                }
            };
        }

        if ("shell".equals(command)) { // NOI18N
            ExecutionDescriptor descriptor = RUN_DESCRIPTOR.postExecution(new RefreshProjectRunnable(project))
                    .errProcessorFactory(ANSI_STRIPPING);

            descriptor = descriptor.outProcessorFactory(
                    createInputProcessorFactory(ANSI_STRIPPING, outFactory, debugFactory));
            return descriptor;
        } else {
            ExecutionDescriptor descriptor = GRAILS_DESCRIPTOR.postExecution(new RefreshProjectRunnable(project))
                    .errProcessorFactory(ANSI_STRIPPING);

            descriptor = descriptor.outProcessorFactory(
                    createInputProcessorFactory(ANSI_STRIPPING, outFactory, debugFactory));
            return descriptor;
        }
    }

    private InputProcessorFactory createInputProcessorFactory(InputProcessorFactory... factories) {
        List<InputProcessorFactory> real = new ArrayList<InputProcessorFactory>(3);
        for (InputProcessorFactory factory : factories) {
            if (factory != null) {
                real.add(factory);
            }
        }
        if (real.isEmpty()) {
            return null;
        }
        if (real.size() == 1) {
            return real.get(0);
        }
        return new ProxyInputProcessorFactory(real.toArray(new InputProcessorFactory[0]));
    }

    public void refreshGrailsCommands() {
        Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand("help", // NOI18N
                GrailsProjectConfig.forProject(project));
        final HelpLineProcessor lineProcessor = new HelpLineProcessor();

        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL)
                .outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                // we are sure this will be invoked at most once
                return InputProcessors.bridge(lineProcessor);
            }
        });

        List<GrailsCommand> freshCommands = Collections.emptyList();
        ExecutionService service = ExecutionService.newService(callable, descriptor, "help"); // NOI18N
        Future<Integer> task = service.run();
        try {
            if (task.get().intValue() == 0) {
                freshCommands = new ArrayList<GrailsCommand>();
                for (String command : lineProcessor.getCommands()) {
                    freshCommands.add(new GrailsCommand(command, null, command)); // NOI18N
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        synchronized (this) {
            if (buildConfigListener == null) {
                BuildConfig buildConfig = project.getBuildConfig();
                buildConfigListener = new BuildConfigListener();
                buildConfigListener.attachListeners(buildConfig);
                buildConfig.addPropertyChangeListener(WeakListeners.propertyChange(buildConfigListener, buildConfig));
            }

            if (projectConfigListener == null) {
                GrailsProjectConfig projectConfig = project.getLookup().lookup(GrailsProjectConfig.class);
                if (projectConfig != null) {
                    projectConfigListener = new ProjectConfigListener();
                    projectConfig.addPropertyChangeListener(WeakListeners.propertyChange(projectConfigListener, projectConfig));
                }
            }
            this.commands = freshCommands;
        }
    }

    public void refreshGrailsCommandsLater(final Runnable post) {
        EXECUTOR.submit(new Runnable() {

            public void run() {
                refreshGrailsCommands();
                if (post != null) {
                    post.run();
                }
            }
        });
    }

    public static final void showURL(URL url, boolean debug, GrailsProject project) {
        if (!debug) {
            if (GrailsProjectConfig.forProject(project).getDisplayBrowser()) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            }
        } else {
            // there is no other debugger than JavaScript is there?
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }
    }

    private static class HelpLineProcessor implements LineProcessor {

        private static final Pattern COMMAND_PATTERN = Pattern.compile("grails\\s(.*)"); // NOI18N

        private static final Pattern EXCLUDE_PATTERN = Pattern.compile("Usage.*|Examples.*"); // NOI18N

        private List<String> commands = Collections.synchronizedList(new ArrayList<String>());

        private boolean excluded;

        public void processLine(String line) {
            Matcher matcher = COMMAND_PATTERN.matcher(line);
            if (matcher.matches()) {
                if (!excluded) {
                    commands.add(matcher.group(1));
                }
            } else {
                excluded = EXCLUDE_PATTERN.matcher(line).matches();
            }
        }

        public List<String> getCommands() {
            return commands;
        }

        public void close() {
        }

        public void reset() {
        }
    }

    private static class AnsiStrippingInputProcessorFactory implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.ansiStripping(defaultProcessor);
        }
    }

    private static class DebugOutputProcessor implements LineProcessor {

        private static final Pattern DEBUGGER_PATTERN =
                Pattern.compile("Listening\\s+for\\s+transport\\s+(\\w+)\\s+at\\s+address:\\s+([0-9]+).*");

        private final GrailsProject project;

        private boolean debugging;

        public DebugOutputProcessor(GrailsProject project) {
            this.project = project;
        }

        public void processLine(String line) {
            Matcher matcher = DEBUGGER_PATTERN.matcher(line);
            if (!debugging && matcher.matches()) {
                debugging = true;

                try {
                    String name = project.getLookup().lookup(ProjectInformation.class).getDisplayName();
                    GrailsDebugger debuger = project.getLookup().lookup(GrailsDebugger.class);
                    if (debuger != null) {
                        debuger.attachDebugger(name, matcher.group(1), "localhost", matcher.group(2)); // NOI18N
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        public void reset() {
            // noop
        }

        public void close() {
            // noop
        }
    }

    private static class ServerOutputProcessor implements LineProcessor {

        private final GrailsProject project;
        private final boolean debug;

        private boolean running;

        public ServerOutputProcessor(GrailsProject project, boolean debug) {
            this.project = project;
            this.debug = debug;
        }

        public void processLine(String line) {
            if (!running && isReady(line)) {
                running = true;

                String urlString = line.substring(line.indexOf("http://"));
                // grails 3 includes a few more words after the url, fetch url:
                urlString = urlString.split("\\s+")[0];

                URL url;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException ex) {
                    LOGGER.log(Level.WARNING, "Could not start browser", ex);
                    return;
                }

                GrailsServerState state = project.getLookup().lookup(GrailsServerState.class);
                if (state != null) {
                    state.setRunningUrl(url);
                }

                GrailsCommandSupport.showURL(url, debug, project);
            }
        }

        public void reset() {
            // noop
        }

        public void close() {
            // noop
        }
 	
        private boolean isReady(String line) {            
            if ( line.contains("Grails application running at http://") ) {
                //grails 3
                return true;
            } else if ( line.contains("Browse to http://") ) {
                //grails 2
                return true;
            }
            return false;  
        }
    }

    private static class ProxyInputProcessorFactory implements InputProcessorFactory {

        private final List<InputProcessorFactory> factories = new ArrayList<InputProcessorFactory>();

        public ProxyInputProcessorFactory(InputProcessorFactory... proxied) {
            for (InputProcessorFactory factory : proxied) {
                if (factory != null) {
                    factories.add(factory);
                }
            }
        }

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            InputProcessor[] processors = new InputProcessor[factories.size()];
            for (int i = 0; i < processors.length; i++) {
                processors[i] = factories.get(i).newInputProcessor(defaultProcessor);
            }
            return InputProcessors.proxy(processors);
        }
    }

    private class ProjectConfigListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (GrailsProjectConfig.GRAILS_PLATFORM_PROPERTY.equals(evt.getPropertyName())) {
                synchronized (GrailsCommandSupport.this) {
                    commands = null;
                }
            }
        }
    }

    private class BuildConfigListener implements PropertyChangeListener {

        private final PluginListener pluginListener = new PluginListener();

        private File globalPluginsDir;

        private File projectPluginsDir;

        public void propertyChange(PropertyChangeEvent evt) {
            if (BuildConfig.BUILD_CONFIG_PLUGINS.equals(evt.getPropertyName())) {
                synchronized (GrailsCommandSupport.this) {
                    attachListeners((BuildConfig) evt.getSource());
                    commands = null;
                }
            }
        }

        public void attachListeners(BuildConfig config) {
            synchronized (GrailsCommandSupport.this) {
                // attach listener for global plugins
                File currentGlobalPluginsDir = config.getGlobalPluginsDir();
                updateListener(pluginListener, globalPluginsDir, currentGlobalPluginsDir);
                globalPluginsDir = currentGlobalPluginsDir;

                // if the directories are same we can't attach same listener twice
                File currentProjectPluginsDir = config.getProjectPluginsDir();
                if ((currentGlobalPluginsDir == null && currentGlobalPluginsDir == currentProjectPluginsDir)
                        || currentGlobalPluginsDir.equals(currentProjectPluginsDir)) {

                    if (projectPluginsDir != null) {
                        FileUtil.removeFileChangeListener(pluginListener, projectPluginsDir);
                    }
                    projectPluginsDir = null;
                    return;
                }

                // attach listener for project plugins
                updateListener(pluginListener, projectPluginsDir, currentProjectPluginsDir);
                projectPluginsDir = currentProjectPluginsDir;
            }
        }

        private void updateListener(FileChangeListener listener, File oldDir, File newDir) {
            if (oldDir == null || !oldDir.equals(newDir)) {
                if (oldDir != null) {
                    FileUtil.removeFileChangeListener(listener, oldDir);
                }
                if (newDir != null) {
                    FileUtil.addFileChangeListener(listener, newDir);
                }
            }
        }
    }

    private class PluginListener implements FileChangeListener {

        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

        public void fileChanged(FileEvent fe) {
            changed();
        }

        public void fileDataCreated(FileEvent fe) {
            changed();
        }

        public void fileDeleted(FileEvent fe) {
            changed();
        }

        public void fileFolderCreated(FileEvent fe) {
            changed();
        }

        public void fileRenamed(FileRenameEvent fe) {
            changed();
        }

        private void changed() {
            synchronized (GrailsCommandSupport.this) {
                commands = null;
            }
        }
    }
}
