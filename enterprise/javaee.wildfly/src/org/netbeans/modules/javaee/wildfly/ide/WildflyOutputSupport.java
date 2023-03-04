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
package org.netbeans.modules.javaee.wildfly.ide;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.InputReader;
import org.netbeans.api.extexecution.base.input.InputReaderTask;
import org.netbeans.api.extexecution.base.input.InputReaders;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public final class WildflyOutputSupport {

    private static final Logger LOGGER = Logger.getLogger(WildflyOutputSupport.class.getName());

    private static final ExecutionDescriptor DESCRIPTOR = new ExecutionDescriptor().frontWindow(true).inputVisible(true);

    // TODO what will happen on server remove actually
    private static final Map<InstanceProperties, WildflyOutputSupport> INSTANCE_CACHE
            = new HashMap<InstanceProperties, WildflyOutputSupport>();

    private static final ExecutorService PROFILER_SERVICE = Executors.newCachedThreadPool();

    private static final ExecutorService LOG_FILE_SERVICE = Executors.newCachedThreadPool();

    private final InstanceProperties props;

    /**
     * GuardedBy("this")
     */
    private boolean started;

    /**
     * GuardedBy("this")
     */
    private boolean failed;

    /**
     * GuardedBy("this")
     */
    private Future<Integer> processTask;

    /**
     * GuardedBy("this")
     */
    private Process process;

    /**
     * GuardedBy("this")
     */
    private Future<?> profileCheckTask;

    /**
     * GuardedBy("this")
     */
    private InputReaderTask fileTask;

    private WildflyOutputSupport(InstanceProperties props) {
        this.props = props;
    }

    public static synchronized WildflyOutputSupport getInstance(InstanceProperties props, boolean create) {
        WildflyOutputSupport instance = INSTANCE_CACHE.get(props);
        if (instance == null && create) {
            instance = new WildflyOutputSupport(props);
            INSTANCE_CACHE.put(props, instance);
        }
        return instance;
    }

    public void start(InputOutput io, final Process serverProcess, final boolean profiler) {
        reset();

        ExecutionDescriptor descriptor = DESCRIPTOR.inputOutput(io);
        descriptor = descriptor.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory2() {

            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(new StartLineProcessor(profiler)));
            }
        });
        descriptor = descriptor.errProcessorFactory(new ExecutionDescriptor.InputProcessorFactory2() {

            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(new StartLineProcessor(profiler)));
            }
        });

        descriptor = descriptor.postExecution(new Runnable() {

            @Override
            public void run() {
                synchronized (WildflyOutputSupport.this) {
                    if (profileCheckTask != null) {
                        profileCheckTask.cancel(true);
                    }
                    WildflyOutputSupport.this.notifyAll();
                }
                synchronized (WildflyOutputSupport.class) {
                    INSTANCE_CACHE.remove(WildflyOutputSupport.this.props);
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
            process = serverProcess;
        }
        failed = !isAlive(serverProcess);
    }

    private boolean isAlive(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public void start(InputOutput io, final File file) {
        reset();
        InputReader reader;
        try {
            InputStream in = Files.newInputStream(file.toPath());
            in.skip(file.length());
            reader = InputReaders.forStream(in, Charset.defaultCharset());
        } catch (IOException ex) {
            reader = InputReaders.forFile(file, Charset.defaultCharset());
        }
        InputReaderTask localFileTask = InputReaderTask.newTask(reader, InputProcessors.printing(io.getOut()));
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
                process = null;
                profileCheckTask = null;
                fileTask = null;
            }
        } finally {
            synchronized (WildflyOutputSupport.class) {
                INSTANCE_CACHE.remove(WildflyOutputSupport.this.props);
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
                if (process != null && !isAlive(process)) {
                    failed = true;
                    return false;
                }
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

    public Process getProcess() {
        synchronized (this) {
            return process;
        }
    }

    private void reset() {
        synchronized (this) {
            if (fileTask != null) {
                fileTask.cancel();
            }

            if (started) {
                LOGGER.log(Level.INFO, "Instance {0} started again without proper stop",
                        props.getProperty(InstanceProperties.DISPLAY_NAME_ATTR));
            }
            started = false;
            failed = false;
            processTask = null;
            process = null;
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
            synchronized (WildflyOutputSupport.this) {
                if (started) {
                    check = false;
                    return;
                }
            }

            if (profiler) {
                if (isProfilerReady()) {
                    synchronized (WildflyOutputSupport.this) {
                        started = true;
                        WildflyOutputSupport.this.notifyAll();
                    }
                    check = false;
                } else if (isProfilerInactive()) {
                    synchronized (WildflyOutputSupport.this) {
                        failed = true;
                        WildflyOutputSupport.this.notifyAll();
                    }
                    check = false;
                }
            }

            if (isStarting(line)) {
                LOGGER.log(Level.FINER, "STARTING message fired"); // NOI18N
                //fireStartProgressEvent(StateType.RUNNING, createProgressMessage("MSG_START_SERVER_IN_PROGRESS")); // NOI18N
            } else if (isStarted(line)) {
                LOGGER.log(Level.FINER, "STARTED message fired"); // NOI18N
                synchronized (WildflyOutputSupport.this) {
                    started = true;
                    WildflyOutputSupport.this.notifyAll();
                }
                check = false;
            } else if (line.contains("Shutdown complete")) { // NOI18N
                synchronized (WildflyOutputSupport.this) {
                    failed = true;
                    WildflyOutputSupport.this.notifyAll();
                }
                check = false;
            }
        }

        private boolean isStarting(String line) {
            return WildflyStartLineParser.isStarting(line);
        }

        private boolean isStarted(String line) {
            return WildflyStartLineParser.isStarted(line);
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
                    synchronized (WildflyOutputSupport.this) {
                        started = true;
                        WildflyOutputSupport.this.notifyAll();
                    }
                    break;
                } else if (isProfilerInactive()) {
                    synchronized (WildflyOutputSupport.this) {
                        failed = true;
                        WildflyOutputSupport.this.notifyAll();
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