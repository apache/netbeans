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

package org.netbeans.modules.j2ee.jboss4.ide;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.InputReader;
import org.netbeans.api.extexecution.input.InputReaderTask;
import org.netbeans.api.extexecution.input.InputReaders;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public final class JBOutputSupport {

    private static final Logger LOGGER = Logger.getLogger(JBOutputSupport.class.getName());

    private static final ExecutionDescriptor DESCRIPTOR = new ExecutionDescriptor().frontWindow(true).inputVisible(true);

    // TODO what will happen on server remove actually
    private static final Map<InstanceProperties, JBOutputSupport> INSTANCE_CACHE
            = new HashMap<InstanceProperties, JBOutputSupport>();

    private static final ExecutorService PROFILER_SERVICE = Executors.newCachedThreadPool();

    private static final ExecutorService LOG_FILE_SERVICE = Executors.newCachedThreadPool();

    private static final Pattern JBOSS_7_STARTED_ML = Pattern.compile(".*JBoss AS 7(\\..*)* \\d+ms .*");

    private final InstanceProperties props;

    /** GuardedBy("this") */
    private boolean started;

    /** GuardedBy("this") */
    private boolean failed;

    /** GuardedBy("this") */
    private Future<Integer> processTask;

    /** GuardedBy("this") */
    private Future<?> profileCheckTask;

    /** GuardedBy("this") */
    private InputReaderTask fileTask;

    private JBOutputSupport(InstanceProperties props) {
        this.props = props;
    }

    public static synchronized JBOutputSupport getInstance(InstanceProperties props, boolean create) {
        JBOutputSupport instance = INSTANCE_CACHE.get(props);
        if (instance == null && create) {
            instance = new JBOutputSupport(props);
            INSTANCE_CACHE.put(props, instance);
        }
        return instance;
    }

    public void start(InputOutput io, final Process serverProcess, final boolean profiler) {
        reset();

        ExecutionDescriptor descriptor = DESCRIPTOR.inputOutput(io);
        descriptor = descriptor.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(new StartLineProcessor(profiler)));
            }
        });
        descriptor = descriptor.errProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(new StartLineProcessor(profiler)));
            }
        });

        descriptor = descriptor.postExecution(new Runnable() {

            @Override
            public void run() {
                synchronized (JBOutputSupport.class) {
                    INSTANCE_CACHE.remove(JBOutputSupport.this.props);
                }

            }
        });

        ExecutionService service = ExecutionService.newService(new Callable<Process>() {

            @Override
            public Process call() throws Exception {
                return serverProcess;
            }
        }, descriptor, props.getProperty(InstanceProperties.DISPLAY_NAME_ATTR));
        Future<Integer> localProcessTask = service.run();

        synchronized (this) {
            if (profiler) {
                profileCheckTask = PROFILER_SERVICE.submit(new ProfilerCheckTask());
            }

            processTask = localProcessTask;
        }
    }

    public void start(InputOutput io, final File file) {
        reset();

        InputReader reader = InputReaders.forFile(file, Charset.defaultCharset());
        InputReaderTask localFileTask = InputReaderTask.newTask(reader, InputProcessors.printing(io.getOut(), false));
        LOG_FILE_SERVICE.submit(localFileTask);
        synchronized (this) {
            fileTask = localFileTask;
        }

    }

    public void stop() {
        try {
            synchronized (this) {
                if (processTask != null) {
                    processTask.cancel(true);
                } else if (fileTask != null) {
                    fileTask.cancel();
                }
                if (profileCheckTask != null) {
                    profileCheckTask.cancel(true);
                }

                started = false;
                failed = false;
                processTask = null;
                profileCheckTask = null;
                fileTask = null;
            }
        } finally {
            synchronized (JBOutputSupport.class) {
                INSTANCE_CACHE.remove(JBOutputSupport.this.props);
            }
        }
    }

    public boolean waitForStart(long timeout) throws TimeoutException, InterruptedException {
        synchronized (this) {
            if (processTask == null) {
                // just defensive
                if (fileTask != null) {
                    return true;
                }
                return false;
            }

            while (!started && !failed) {
                wait(timeout);
            }
            if (started) {
                return true;
            } else if (failed) {
                return false;
            }

            // timeouted block
            if (profileCheckTask != null) {
                profileCheckTask.cancel(true);
            }
            throw new TimeoutException("Expired timeout " + timeout + " ms"); // NOI18N
        }
    }

    public void waitForStop(long timeout) throws TimeoutException, InterruptedException,
            ExecutionException {

        Future<Integer> localProcessTask;
        synchronized (this) {
            localProcessTask = processTask;
        }
        if (localProcessTask == null) {
            return;
        }
        localProcessTask.get(timeout, TimeUnit.MILLISECONDS);
    }

    private void reset() {
        synchronized (this) {
            if (fileTask != null) {
                fileTask.cancel();
            }

            if (started) {
                // XXX perhaps this may happen when profiler terminated ?
                LOGGER.log(Level.INFO, "Instance {0} started again without proper stop",
                        props.getProperty(InstanceProperties.DISPLAY_NAME_ATTR));
            }

            started = false;
            failed = false;
            processTask = null;
            profileCheckTask = null;
            fileTask = null;
        }
    }

    private static boolean isProfilerReady() {
        int state = ProfilerSupport.getState();
        return state == ProfilerSupport.STATE_BLOCKING || state == ProfilerSupport.STATE_RUNNING
                || state == ProfilerSupport.STATE_PROFILING;
    }

    private static boolean isProfilerInactive() {
        return ProfilerSupport.getState() == ProfilerSupport.STATE_INACTIVE;
    }

    private class StartLineProcessor implements LineProcessor {

        private final boolean profiler;

        private boolean check = true;

        public StartLineProcessor(boolean profiler) {
            this.profiler = profiler;
        }

        @Override
        public void processLine(String line) {
            if (!check) {
                return;
            }
            synchronized (JBOutputSupport.this) {
                if (started) {
                   check = false;
                   return;
                }
            }

            if (profiler) {
                if (isProfilerReady()) {
                    synchronized (JBOutputSupport.this) {
                        started = true;
                        JBOutputSupport.this.notifyAll();
                    }
                    check = false;
                } else if (isProfilerInactive()) {
                    synchronized (JBOutputSupport.this) {
                        failed = true;
                        JBOutputSupport.this.notifyAll();
                    }
                    check = false;
                }
            }

            if (line.indexOf("Starting JBoss (MX MicroKernel)") > -1 // JBoss 4.x message // NOI18N
                    || line.indexOf("Starting JBoss (Microcontainer)") > -1 // JBoss 5.0 message // NOI18N
                    || line.indexOf("Starting JBossAS") > -1) { // JBoss 6.0 message // NOI18N
                LOGGER.log(Level.FINER, "STARTING message fired"); // NOI18N
                //fireStartProgressEvent(StateType.RUNNING, createProgressMessage("MSG_START_SERVER_IN_PROGRESS")); // NOI18N
            } else if ( ((line.indexOf("JBoss (MX MicroKernel)") > -1 // JBoss 4.x message // NOI18N
                    || line.indexOf("JBoss (Microcontainer)") > -1 // JBoss 5.0 message // NOI18N
                    || line.indexOf("JBossAS") > -1 // JBoss 6.0 message // NOI18N
                    || line.indexOf("JBoss AS") > -1)// JBoss 7.0 message // NOI18N
                    && (line.indexOf("Started in") > -1) // NOI18N
                        || line.indexOf("started in") > -1 // NOI18N
                        || line.indexOf("started (with errors) in") > -1) // JBoss 7 with some errors (include wrong deployments) // NOI18N
                        || JBOSS_7_STARTED_ML.matcher(line).matches()) {
                LOGGER.log(Level.FINER, "STARTED message fired"); // NOI18N

                synchronized (JBOutputSupport.this) {
                    started = true;
                    JBOutputSupport.this.notifyAll();
                }
                check = false;
            } else if (line.indexOf("Shutdown complete") > -1) { // NOI18N
                synchronized (JBOutputSupport.this) {
                    failed = true;
                    JBOutputSupport.this.notifyAll();
                }
                check = false;
            }
        }

        @Override
        public void reset() {
            // noop
        }

        @Override
        public void close() {
            // noop
        }
    }

    private class ProfilerCheckTask implements Runnable {

        @Override
        public void run() {
            for (;;) {
                if (isProfilerReady()) {
                    synchronized (JBOutputSupport.this) {
                        started = true;
                        JBOutputSupport.this.notifyAll();
                    }
                    break;
                } else if (isProfilerInactive()) {
                    synchronized (JBOutputSupport.this) {
                        failed = true;
                        JBOutputSupport.this.notifyAll();
                    }
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    break;
                }
            }
        }

    }
}
