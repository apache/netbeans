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
package org.netbeans.modules.weblogic.common.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.base.BaseExecutionDescriptor;
import org.netbeans.api.extexecution.base.Environment;
import org.netbeans.api.extexecution.base.Processes;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.InputReaderTask;
import org.netbeans.api.extexecution.base.input.InputReaders;
import org.netbeans.api.extexecution.base.input.LineProcessor;
import org.netbeans.modules.weblogic.common.RemoteLogInputReader;
import org.netbeans.modules.weblogic.common.spi.WebLogicTrustHandler;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public final class WebLogicRuntime {

    private static final Logger LOGGER = Logger.getLogger(WebLogicRuntime.class.getName());

    private static final RequestProcessor RUNTIME_RP = new RequestProcessor(WebLogicRuntime.class.getName(), 2);

    private static final Pattern LOG_PARSING_PATTERN = Pattern.compile(
            "^####(<.*>)\\s+(<.*>)\\s+(<.*>)\\s+(<.*>)\\s+(<.*>)\\s+(<.*>)\\s+" // NOI18N
                    + "(<.*>)\\s+(<.*>)\\s+(<.*>)\\s+(<.*>)\\s+(<.*>)\\s+(<.*>?)(\\s+|$)"); // NOI18N

    private static final String STARTUP_SH = "startWebLogic.sh";   // NOI18N

    private static final String STARTUP_BAT = "startWebLogic.cmd"; // NOI18N

    private static final String SHUTDOWN_SH = "stopWebLogic.sh"; // NOI18N

    private static final String SHUTDOWN_BAT = "stopWebLogic.cmd"; // NOI18N

    private static final String START_KEY_UUID = "NB_EXEC_WL_START_PROCESS_UUID"; //NOI18N

    private static final String STOP_KEY_UUID = "NB_EXEC_WL_STOP_PROCESS_UUID"; //NOI18N

    private static final int TIMEOUT = 300000;

    private static final int DELAY = 1000;

    private static final int CHECK_TIMEOUT = 5000;

    //@GuardedBy(PROCESSES)
    private static final WeakHashMap<WebLogicConfiguration, Process> PROCESSES = new WeakHashMap<WebLogicConfiguration, Process>();

    private final WebLogicConfiguration config;

    private WebLogicRuntime(WebLogicConfiguration config) {
        this.config = config;
    }

    @NonNull
    public static WebLogicRuntime getInstance(@NonNull WebLogicConfiguration config) {
        return new WebLogicRuntime(config);
    }

//    public static void clear(WebLogicConfiguration config) {
//        synchronized (PROCESSES) {
//            PROCESSES.remove(config);
//        }
//    }

    public void startAndWait(@NullAllowed final BaseExecutionDescriptor.InputProcessorFactory outFactory,
            @NullAllowed final BaseExecutionDescriptor.InputProcessorFactory errFactory,
            @NullAllowed final Map<String, String> environment) throws InterruptedException, ExecutionException {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> reference = new AtomicReference<>();

        start(outFactory, errFactory, new BlockingListener(latch, reference, false), environment, null);
        latch.await();

        Exception exception = reference.get();
        if (exception != null) {
            throw new ExecutionException(exception);
        }
    }

    public void start(@NullAllowed final BaseExecutionDescriptor.InputProcessorFactory outFactory,
            @NullAllowed final BaseExecutionDescriptor.InputProcessorFactory errFactory,
            @NullAllowed final RuntimeListener listener,
            @NullAllowed final Map<String, String> environment,
            @NullAllowed final RunningCondition condition) {

        if (listener != null) {
            listener.onStart();
        }

        if (config.isRemote()) {
            if (listener != null) {
                listener.onRunning();
                listener.onExit();
            }
            return;
        }

        RUNTIME_RP.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    File domainHome = config.getDomainHome();
                    if (!domainHome.exists() || !domainHome.isDirectory()) {
                        if (listener != null) {
                            listener.onFail();
                        }
                        return;
                    }

                    if (isRunning()) {
                        if (listener != null) {
                            listener.onRunning();
                        }
                        return;
                    }

                    long start = System.currentTimeMillis();

                    File startup;
                    if (BaseUtilities.isWindows()) {
                        startup = new File(domainHome, STARTUP_BAT);
                        if (!startup.exists()) {
                            startup = new File(new File(domainHome, "bin"), STARTUP_BAT); // NOI18N
                        }
                    } else {
                        startup = new File(domainHome, STARTUP_SH);
                        if (!startup.exists()) {
                            startup = new File(new File(domainHome, "bin"), STARTUP_SH); // NOI18N
                        }
                    }

                    org.netbeans.api.extexecution.base.ProcessBuilder builder =
                            org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
                    builder.setExecutable(startup.getAbsolutePath());
                    builder.setWorkingDirectory(domainHome.getAbsolutePath());
                    builder.getEnvironment().setVariable(START_KEY_UUID, config.getId());

                    File mwHome = config.getLayout().getMiddlewareHome();
                    if (mwHome != null) {
                        builder.getEnvironment().setVariable("MW_HOME", mwHome.getAbsolutePath()); // NOI18N
                    }

                    configureEnvironment(builder.getEnvironment(), environment);

                    Process process;
                    try {
                        process = builder.call();
                    } catch (IOException ex) {
                        if (listener != null) {
                            listener.onException(ex);
                        }
                        return;
                    }
                    synchronized (PROCESSES) {
                        PROCESSES.put(config, process);
                    }

                    if (listener != null) {
                        listener.onProcessStart();
                    }

                    ExecutorService service = Executors.newFixedThreadPool(2);
                    startService(service, process, outFactory, errFactory);

                    while ((System.currentTimeMillis() - start) < TIMEOUT) {
                        if ((condition != null && condition.isRunning()) || isRunning()) {
                            if (listener != null) {
                                listener.onRunning();
                            }

                            // FIXME we should wait for the process and kill service
                            boolean interrupted = false;
                            try {
                                process.waitFor();
                            } catch (InterruptedException ex) {
                                interrupted = true;
                            }
                            if (interrupted) {
                                // this is interruption just in wait for process
                                Thread.currentThread().interrupt();
                            } else {
                                stopService(service);
                                if (listener != null) {
                                    listener.onProcessFinish();
                                }
                            }
                            if (listener != null) {
                                listener.onFinish();
                            }
                            return;
                        }
                        try {
                            Thread.sleep(DELAY);
                        } catch (InterruptedException e) {
                            if (listener != null) {
                                listener.onInterrupted();
                            }
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    // timeouted
                    if (listener != null) {
                        listener.onTimeout();
                    }
                } finally {
                    if (listener != null) {
                        listener.onExit();
                    }
                }
            }
        });
    }

    public void stopAndWait(@NullAllowed final BaseExecutionDescriptor.InputProcessorFactory outFactory,
            @NullAllowed final BaseExecutionDescriptor.InputProcessorFactory errFactory) throws InterruptedException, ExecutionException {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> reference = new AtomicReference<>();

        stop(outFactory, errFactory, new BlockingListener(latch, reference, true));
        latch.await();

        Exception exception = reference.get();
        if (exception != null) {
            throw new ExecutionException(exception);
        }
    }

    public void stop(@NullAllowed final BaseExecutionDescriptor.InputProcessorFactory outFactory,
            @NullAllowed final BaseExecutionDescriptor.InputProcessorFactory errFactory,
            @NullAllowed final RuntimeListener listener) {

        if (listener != null) {
            listener.onStart();
        }

        if (config.isRemote()) {
            if (listener != null) {
                listener.onRunning();
                listener.onExit();
            }
            return;
        }

        RUNTIME_RP.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    File domainHome = config.getDomainHome();
                    if (!domainHome.isDirectory()) {
                        if (listener != null) {
                            listener.onFail();
                        }
                        return;
                    }
                    File shutdown;
                    if (BaseUtilities.isWindows()) {
                        shutdown = new File(new File(domainHome, "bin"), SHUTDOWN_BAT); // NOI18N
                    } else {
                        shutdown = new File(new File(domainHome, "bin"), SHUTDOWN_SH); // NOI18N
                    }

                    ExecutorService stopService = null;
                    Process stopProcess = null;
                    String uuid = UUID.randomUUID().toString();

                    try {
                        long start = System.currentTimeMillis();

                        if (shutdown.exists()) {
                            org.netbeans.api.extexecution.base.ProcessBuilder builder
                                    = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
                            builder.setExecutable(shutdown.getAbsolutePath());
                            builder.setWorkingDirectory(domainHome.getAbsolutePath());
                            builder.getEnvironment().setVariable(STOP_KEY_UUID, uuid);

                            List<String> arguments = new ArrayList<String>();
                            arguments.add(config.getUsername());
                            arguments.add(config.getPassword());
                            arguments.add(config.getAdminURL());
                            builder.setArguments(arguments);

                            File mwHome = config.getLayout().getMiddlewareHome();
                            if (mwHome != null) {
                                builder.getEnvironment().setVariable("MW_HOME", mwHome.getAbsolutePath()); // NOI18N
                            }

                            try {
                                stopProcess = builder.call();
                            } catch (IOException ex) {
                                if (listener != null) {
                                    listener.onException(ex);
                                }
                                return;
                            }

                            if (listener != null) {
                                listener.onProcessStart();
                            }

                            stopService = Executors.newFixedThreadPool(2);
                            startService(stopService, stopProcess, outFactory, errFactory);
                        } else {
                            Process process;
                            synchronized (PROCESSES) {
                                process = PROCESSES.get(config);
                            }
                            if (process == null) {
                                // FIXME what to do here
                                if (listener != null) {
                                    listener.onFail();
                                }
                                return;
                            }
                            Map<String, String> mark = new HashMap<String, String>();
                            mark.put(START_KEY_UUID, config.getId());
                            Processes.killTree(process, mark);
                        }

                        while ((System.currentTimeMillis() - start) < TIMEOUT) {
                            if (isRunning() && isRunning(stopProcess)) {
                                if (listener != null) {
                                    listener.onRunning();
                                }

                                try {
                                    Thread.sleep(DELAY);
                                } catch (InterruptedException e) {
                                    if (listener != null) {
                                        listener.onInterrupted();
                                    }
                                    Thread.currentThread().interrupt();
                                    return;
                                }
                            } else {
                                if (stopProcess != null) {
                                    try {
                                        stopProcess.waitFor();
                                    } catch (InterruptedException ex) {
                                        if (listener != null) {
                                            listener.onInterrupted();
                                        }
                                        Thread.currentThread().interrupt();
                                        return;
                                    }
                                }

                                if (isRunning()) {
                                    if (listener != null) {
                                        listener.onFail();
                                    }
                                } else {
                                    if (listener != null) {
                                        listener.onFinish();
                                    }
                                }
                                return;
                            }
                        }

                        // timeouted
                        if (listener != null) {
                            listener.onTimeout();
                        }
                    } finally {
                        // do the cleanup
                        if (stopProcess != null) {
                            Map<String, String> mark = new HashMap<String, String>();
                            mark.put(STOP_KEY_UUID, uuid);
                            Processes.killTree(stopProcess, mark);
                            stopService(stopService);
                            if (listener != null) {
                                listener.onProcessFinish();
                            }
                        }
                    }
                } finally {
                    if (listener != null) {
                        listener.onExit();
                    }
                }
            }
        });
    }

    public void kill() {
        Process process;
        synchronized (PROCESSES) {
            process = PROCESSES.get(config);
        }
        if (process != null) {
            Map<String, String> mark = new HashMap<String, String>();
            mark.put(START_KEY_UUID, config.getId());
            Processes.killTree(process, mark);
        }
    }

    public boolean isRunning() {
//        Process proc;
//        synchronized (PROCESSES) {
//            proc = PROCESSES.get(config);
//        }
//
//        if (!isRunning(proc)) {
//            return false;
//        }

        String host = config.getHost();
        int port = config.getPort();
        return ping(host, port, CHECK_TIMEOUT, config.isRemote(), config.isSecured()); // is server responding?
    }

    public boolean isProcessRunning() {
        if (config.isRemote()) {
            return false;
        }
        Process proc;
        synchronized (PROCESSES) {
            proc = PROCESSES.get(config);
        }

        return isRunning(proc);
    }

    @CheckForNull
    public InputReaderTask createLogReaderTask(@NonNull final LineProcessor processor,
            @NullAllowed Callable<String> nonProxy) {

        if (config.isRemote()) {
            return InputReaderTask.newTask(new RemoteLogInputReader(config, nonProxy), InputProcessors.bridge(processor));
        }

        final StringBuilder sb = new StringBuilder();
        return InputReaderTask.newTask(InputReaders.forFileInputProvider(new LogFileProvider(config)),
                InputProcessors.bridge(new LineProcessor() {

                    @Override
                    public void processLine(String line) {
                        Matcher m = LOG_PARSING_PATTERN.matcher(line);
                        if (m.matches()) {
                            sb.append(m.group(1)).append(" "); // NOI18N
                            sb.append(m.group(2)).append(" "); // NOI18N
                            sb.append(m.group(3)).append(" "); // NOI18N
                            sb.append(m.group(11)).append(" "); // NOI18N
                            sb.append(m.group(12)).append(" "); // NOI18N
                            processor.processLine(sb.toString());
                            sb.setLength(0);
                        } else {
                            processor.processLine(line);
                        }
                    }

                    @Override
                    public void reset() {
                        processor.reset();
                    }

                    @Override
                    public void close() {
                        processor.close();
                    }
                }));
    }

    private static void configureEnvironment(Environment environment, Map<String, String> variables) {
        if (variables == null) {
            return;
        }

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            environment.setVariable(entry.getKey(), entry.getValue());
        }
    }

    private static void startService(final ExecutorService service, Process process,
            BaseExecutionDescriptor.InputProcessorFactory outFactory,
            BaseExecutionDescriptor.InputProcessorFactory errFactory) {

        InputProcessor output = null;
        if (outFactory != null) {
            output = outFactory.newInputProcessor();
        }
        InputProcessor error = null;
        if (errFactory != null) {
            error = errFactory.newInputProcessor();
        }

        service.submit(InputReaderTask.newTask(InputReaders.forStream(
                process.getInputStream(), Charset.defaultCharset()), output));
        service.submit(InputReaderTask.newTask(InputReaders.forStream(
                process.getErrorStream(), Charset.defaultCharset()), error));
    }

    private static void stopService(final ExecutorService service) {
        if (service != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                @Override
                public Void run() {
                    service.shutdownNow();
                    return null;
                }
            });
        }
    }

    private static boolean isRunning(Process process) {
        if (process == null) {
            return false;
        }
        try {
            process.exitValue();
            // process is stopped
            return false;
        } catch (IllegalThreadStateException e) {
            // process is running
            return true;
        }
    }

    private boolean ping(String host, int port, int timeout, boolean remote, boolean secured) {
        if (ping(host, port, timeout, "/console/login/LoginForm.jsp", remote, secured)) {
            return true;
        }
        return ping(host, port, timeout, "/console", remote, secured);
    }

    private boolean ping(String host, int port, int timeout, String path, boolean remote, boolean secured) {
        // checking whether a socket can be created is not reliable enough, see #47048
        try {
            Socket socket = createSocket(host, port, path, remote, secured);
            try {
                socket.connect(new InetSocketAddress(host, port), timeout); // NOI18N
                socket.setSoTimeout(timeout);
                try (PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                    out.println("GET " + path + " HTTP/1.1\nHost:\n"); // NOI18N
                    String line = in.readLine();
                    return "HTTP/1.1 200 OK".equals(line) // NOI18N
                            || "HTTP/1.1 302 Moved Temporarily".equals(line); // NOI18N
                }
            } finally {
                socket.close();
            }
        } catch (IOException ioe) {
            if (secured) {
                LOGGER.log(Level.INFO, null, ioe);
            } else {
                LOGGER.log(Level.FINE, null, ioe);
            }
            return false;
        }
    }

    private Socket createSocket(String host, int port, String path, boolean remote, boolean secured) throws IOException {
        if (secured) {
            WebLogicTrustHandler provider = Lookup.getDefault().lookup(WebLogicTrustHandler.class);
            if (provider != null) {
                try {
                    SSLContext context = SSLContext.getInstance("TLS"); // NOI18N
                    context.init(null, new TrustManager[] {provider.getTrustManager(config)}, new SecureRandom());
                    return context.getSocketFactory().createSocket();
                } catch (GeneralSecurityException ex) {
                    throw new IOException(ex);
                }
            }
            return SSLSocketFactory.getDefault().createSocket();
        }

        Proxy proxy = Proxy.NO_PROXY;
        if (remote) {
            try {
                List<Proxy> proxies = ProxySelector.getDefault().select(new URI("http://" + host + ":" + port + path)); // NOI18N
                if (!proxies.isEmpty()) {
                    proxy = proxies.get(0);
                }
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return new Socket(proxy);
    }
    public static interface RunningCondition {

        boolean isRunning();

    }

    private static class LogFileProvider implements InputReaders.FileInput.Provider {

        private final WebLogicConfiguration config;

        private InputReaders.FileInput currentInput;

        public LogFileProvider(WebLogicConfiguration config) {
            this.config = config;
        }

        @Override
        public InputReaders.FileInput getFileInput() {
            File fresh = config.getLogFile();
            if (currentInput == null) {
                currentInput = new InputReaders.FileInput(fresh, StandardCharsets.UTF_8);
            } else {
                File current = currentInput.getFile();
                if (!current.equals(fresh) && fresh.lastModified() > current.lastModified()) {
                    currentInput = new InputReaders.FileInput(fresh, StandardCharsets.UTF_8);
                }
            }
            return currentInput;
        }

    }

    private static class BlockingListener implements RuntimeListener {

        private final CountDownLatch latch;

        private final AtomicReference<Exception> exception;

        private final boolean waitOnRunning;

        public BlockingListener(CountDownLatch latch, AtomicReference<Exception> exception, boolean waitOnRunning) {
            this.latch = latch;
            this.exception = exception;
            this.waitOnRunning = waitOnRunning;
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        public AtomicReference<Exception> getException() {
            return exception;
        }

        @Override
        public void onStart() {
            // noop
        }

        @Override
        public void onFinish() {
            // noop
        }

        @Override
        public void onFail() {
            // noop
        }

        @Override
        public void onProcessStart() {
            // noop
        }

        @Override
        public void onProcessFinish() {
            // noop
        }

        @Override
        public void onRunning() {
            if (!waitOnRunning) {
                latch.countDown();
            }
        }

        @Override
        public void onTimeout() {
            exception.set(new TimeoutException(TIMEOUT + " ms"));
            latch.countDown();
        }

        @Override
        public void onInterrupted() {
            // XXX is this ok ?
            exception.set(new InterruptedException());
            latch.countDown();
        }

        @Override
        public void onException(Exception ex) {
            exception.set(ex);
            latch.countDown();
        }

        @Override
        public void onExit() {
            latch.countDown();
        }
    }
}
