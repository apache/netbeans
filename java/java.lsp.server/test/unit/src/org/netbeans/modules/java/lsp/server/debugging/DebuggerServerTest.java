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
package org.netbeans.modules.java.lsp.server.debugging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.eclipse.lsp4j.debug.BreakpointEventArguments;
import org.eclipse.lsp4j.debug.BreakpointLocation;
import org.eclipse.lsp4j.debug.BreakpointLocationsArguments;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.StackFrameFormat;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminateArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.ThreadEventArguments;
import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.file.launcher.queries.MultiSourceRootProvider;
import org.netbeans.modules.java.lsp.server.LspArgsProcessor;
import org.netbeans.modules.java.source.parsing.ParameterNameProviderImpl;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.test.MockLookup;

public class DebuggerServerTest extends NbTestCase {

    private static final long TIMEOUT = 10_000;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private Socket clientSocket;
    private Thread serverThread;

    public DebuggerServerTest(String name) {
        super(name);
    }

    public void testBreakpoints() throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code =
                """
                public class Test {
                    public static void main(String... args) {
                        System.err.println("start");

                        for (int i = 0; i < 5; i++) {
                            System.err.println(i);
                        }
                    }
                }
                """;
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        IDebugProtocolServer[] server = new IDebugProtocolServer[1];
        List<Pair<String, Object>> called = new ArrayList<>();
        IDebugProtocolClient client = new TestDebugProtocolClient(called);
        Launcher<IDebugProtocolServer> serverLauncher =
                DSPLauncher.createClientLauncher(client, clientSocket.getInputStream(), clientSocket.getOutputStream(), false, new PrintWriter(System.err));
        serverLauncher.startListening();
        server[0] = serverLauncher.getRemoteProxy();
        InitializeRequestArguments init = new InitializeRequestArguments();
        init.setAdapterID("test");
        init.setColumnsStartAt1(true);
        init.setLinesStartAt1(true);
        Capabilities capa = server[0].initialize(init).get();
        server[0].launch(Map.of("file", src.toString(),
                                "classPaths", List.of("whatever"))).get();
        awaitCallBack(called, "initialized");
        SetBreakpointsArguments setBreakpointsArguments = new SetBreakpointsArguments();
        Source source = new Source();

        source.setPath(src.getAbsolutePath());
        setBreakpointsArguments.setSource(source);
        setBreakpointsArguments.setBreakpoints(new SourceBreakpoint[] {
            createSourceBreakpoint(6, null, null)
        });
        server[0].setBreakpoints(setBreakpointsArguments).get();
        server[0].configurationDone(new ConfigurationDoneArguments()).get();
        for (int i = 0; i < 5; i++) {
            assertEquals("breakpoint", awaitCallBack(called, "stopped"));
            server[0].continue_(new ContinueArguments()).get();
        }

        assertEquals("exited:true", awaitCallBack(called, "thread"));
        server[0].disconnect(new DisconnectArguments()).get();
    }

    public void testLambdaBreakpoints1() throws Exception {
        doTestLambdaBreakpoints(
                new LambdaBreakpointTestCase(List.of(
                     createSourceBreakpoint(9, null, null)
                 ),
                 List.of(
                     List.of("Static:", "input:0"),
                     List.of("Static:", "v:\"a\""),
                     List.of("Static:", "v:\"b\"")
                 )));
    }

    public void testLambdaBreakpoints2() throws Exception {
        doTestLambdaBreakpoints(
                new LambdaBreakpointTestCase(List.of(
                     createSourceBreakpoint(9, 18, null)
                 ),
                 List.of(
                     List.of("Static:", "v:\"a\""),
                     List.of("Static:", "v:\"b\"")
                 )));
    }

    public void testLambdaBreakpoints3() throws Exception {
        doTestLambdaBreakpoints(
            new LambdaBreakpointTestCase(List.of(
                     createSourceBreakpoint(9, null, null),
                     createSourceBreakpoint(9, 18, null)
                 ),
                 List.of(
                     List.of("Static:", "v:\"a\""),
                     List.of("Static:", "v:\"b\"")
                 )));
    }

    public void testLambdaBreakpoints4() throws Exception {
        doTestLambdaBreakpoints(
            new LambdaBreakpointTestCase(List.of(
                     createSourceBreakpoint(9, null, null),
                     createSourceBreakpoint(9, 1, null),
                     createSourceBreakpoint(9, 18, null)
                 ),
                 List.of(
                     List.of("Static:", "input:0"),
                     List.of("Static:", "v:\"a\""),
                     List.of("Static:", "v:\"b\"")
                 )));
    }

    public void testLambdaBreakpointsZeroBased1() throws Exception {
        doTestLambdaBreakpoints(
                new LambdaBreakpointTestCase(List.of(
                     createSourceBreakpoint(8, 17, null)
                 ),
                 List.of(
                     List.of("Static:", "v:\"a\""),
                     List.of("Static:", "v:\"b\"")
                 )),
                false,
                false);
    }

    public void testLambdaBreakpointsZeroBased2() throws Exception {
        doTestLambdaBreakpoints(
                new LambdaBreakpointTestCase(List.of(
                     createSourceBreakpoint(9, 17, null)
                 ),
                 List.of(
                     List.of("Static:", "v:\"a\""),
                     List.of("Static:", "v:\"b\"")
                 )),
                true,
                false);
    }

    public void testLambdaBreakpointsZeroBased3() throws Exception {
        doTestLambdaBreakpoints(
                new LambdaBreakpointTestCase(List.of(
                     createSourceBreakpoint(8, 18, null)
                 ),
                 List.of(
                     List.of("Static:", "v:\"a\""),
                     List.of("Static:", "v:\"b\"")
                 )),
                false,
                true);
    }

    private record LambdaBreakpointTestCase(List<SourceBreakpoint> breakpoints, List<List<String>> variables) {}

    private void doTestLambdaBreakpoints(LambdaBreakpointTestCase testCase) throws Exception {
        doTestLambdaBreakpoints(testCase, true, true);
    }

    private void doTestLambdaBreakpoints(LambdaBreakpointTestCase testCase, boolean linesStartAt1, boolean columnsStartAt1) throws Exception {
        File src = new File(getWorkDir(), "Test.java");
        src.getParentFile().mkdirs();
        String code =
                """
                import java.util.List;
                public class Test {
                    public static void main(String... args) {
                        helper(args.length);
                    }
                    private static void helper(int input) {
                        List.of("a", "b", "c")
                            .stream()
                            .map(v -> v.length())
                            .max((v1, v2) -> v1 - v2);
                    }
                }
                """;
        try (Writer w = new FileWriter(src)) {
            w.write(code);
        }
        IDebugProtocolServer[] server = new IDebugProtocolServer[1];
        List<Pair<String, Object>> called = new ArrayList<>();
        TestDebugProtocolClient client = new TestDebugProtocolClient(called);
        Launcher<IDebugProtocolServer> serverLauncher =
                DSPLauncher.createClientLauncher(client, clientSocket.getInputStream(), clientSocket.getOutputStream(), false, new PrintWriter(System.err));
        serverLauncher.startListening();
        server[0] = serverLauncher.getRemoteProxy();
        InitializeRequestArguments init = new InitializeRequestArguments();
        init.setAdapterID("test");
        init.setColumnsStartAt1(columnsStartAt1);
        init.setLinesStartAt1(linesStartAt1);
        Capabilities capa = server[0].initialize(init).get();
        server[0].launch(Map.of("file", src.toString(),
                                "classPaths", List.of("whatever"))).get();
        awaitCallBack(called, "initialized");
        SetBreakpointsArguments setBreakpointsArguments = new SetBreakpointsArguments();
        Source source = new Source();

        source.setPath(src.getAbsolutePath());
        setBreakpointsArguments.setSource(source);
        setBreakpointsArguments.setBreakpoints(new SourceBreakpoint[] {
            createSourceBreakpoint(3 + (linesStartAt1 ? 1 : 0), null, null)
        });
        setBreakpointsArguments.setSourceModified(true);
        server[0].setBreakpoints(setBreakpointsArguments).get();
        server[0].configurationDone(new ConfigurationDoneArguments()).get();
        assertEquals("breakpoint", awaitCallBack(called, "stopped"));
        BreakpointLocationsArguments locationArgs = new BreakpointLocationsArguments();
        locationArgs.setSource(source);
        locationArgs.setLine(4);
        locationArgs.setColumn(0);
        locationArgs.setEndLine(7);
        locationArgs.setEndColumn(38);
        BreakpointLocation[] locations = server[0].breakpointLocations(locationArgs).get().getBreakpoints();
        String lambdaBreakpointLocations = """
                                           [
                                             {
                                               "line": 9
                                             },
                                             {
                                               "line": 9,
                                               "column": 18
                                             },
                                             {
                                               "line": 10
                                             },
                                             {
                                               "line": 10,
                                               "column": 18
                                             }
                                           ]""";
        if (!linesStartAt1) {
            lambdaBreakpointLocations =
                    lambdaBreakpointLocations.replace("9", "8")
                                             .replace("10", "9");
        }
        if (!columnsStartAt1) {
            lambdaBreakpointLocations = lambdaBreakpointLocations.replace("18", "17");
        }
        assertEquals(lambdaBreakpointLocations,
                     GSON.toJson(locations));

        setBreakpointsArguments.setBreakpoints(testCase.breakpoints.toArray(SourceBreakpoint[]::new));
        server[0].setBreakpoints(setBreakpointsArguments).get();
        for (List<String> expectedVariables : testCase.variables()) {
            server[0].continue_(new ContinueArguments());
            assertEquals("breakpoint", awaitCallBack(called, "stopped"));
            List<String> actualVariables = getVariables(server[0], client.mainThreadID);
            assertEquals(expectedVariables, actualVariables);
        }
        server[0].disconnect(new DisconnectArguments()).get();
    }

    private static SourceBreakpoint createSourceBreakpoint(int line, Integer column, String condition) {
        SourceBreakpoint result = new SourceBreakpoint();

        result.setLine(line);
        result.setColumn(column);
        result.setCondition(condition);

        return result;
    }

    private static List<String> getVariables(IDebugProtocolServer server, int threadId) throws Exception {
        StackTraceArguments stackTraceRequest = new StackTraceArguments();
        stackTraceRequest.setThreadId(threadId);
        StackTraceResponse stackTrace = server.stackTrace(stackTraceRequest).get();
        ScopesArguments scopesRequest = new ScopesArguments();
        scopesRequest.setFrameId(stackTrace.getStackFrames()[0].getId());
        ScopesResponse scopes = server.scopes(scopesRequest).get();
        Scope localScope = Arrays.stream(scopes.getScopes())
                                 .filter(scope -> "locals".equals(scope.getPresentationHint()))
                                 .findAny()
                                 .orElseThrow();
        VariablesArguments variablesRequest = new VariablesArguments();
        variablesRequest.setVariablesReference(localScope.getVariablesReference());
        VariablesResponse variables = server.variables(variablesRequest).get();
        List<String> result = new ArrayList<>();
        for (Variable v : variables.getVariables()) {
            result.add(v.getName() + ":" + v.getValue());
        }
        return result;
    }

    private Object awaitCallBack(List<Pair<String, Object>> called, String callback) {
        long start = System.currentTimeMillis();
        synchronized (called) {
            while ((System.currentTimeMillis() - start) < TIMEOUT) {
                for (int i = 0; i < called.size(); i++) {
                    if (callback.equals(called.get(i).first())) {
                        Object res = called.get(i).second();
                        while (i >= 0) {
                            called.remove(0);
                            i--;
                        }
                        return res;
                    }
                }
                try {
                    called.wait(TIMEOUT - (System.currentTimeMillis() - start));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        throw new AssertionError(String.valueOf(called));
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("java.awt.headless", Boolean.TRUE.toString());
        ParameterNameProviderImpl.DISABLE_PARAMETER_NAMES_LOADING = true;
        super.setUp();
        clearWorkDir();
        ServerSocket srv = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        serverThread = new Thread(() -> {
            try {
                Socket server = srv.accept();

                Path tempDir = Files.createTempDirectory("lsp-server");
                File userdir = tempDir.resolve("scratch-user").toFile();
                File cachedir = tempDir.resolve("scratch-cache").toFile();
                System.setProperty("netbeans.user", userdir.getAbsolutePath());
                File varLog = new File(new File(userdir, "var"), "log");
                varLog.mkdirs();
                System.setProperty("jdk.home", System.getProperty("java.home")); //for j2seplatform
                Class<?> main = Class.forName("org.netbeans.core.startup.Main");
                main.getDeclaredMethod("initializeURLFactory").invoke(null);
                new File(cachedir, "index").mkdirs();
                Class jsClass = JavaSource.class;
                File javaCluster = Utilities.toFile(jsClass.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile();
                System.setProperty("netbeans.dirs", javaCluster.getAbsolutePath());
                CacheFolderProvider.getCacheFolderForRoot(Utilities.toURI(Places.getUserDirectory()).toURL(), EnumSet.noneOf(CacheFolderProvider.Kind.class), CacheFolderProvider.Mode.EXISTENT);

                Lookup.getDefault().lookup(ModuleInfo.class); //start the module system

                CommandLine.getDefault().process(new String[] {"--start-java-debug-adapter-server"}, server.getInputStream(), server.getOutputStream(), System.err, getWorkDir());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });
        serverThread.start();
        clientSocket = new Socket(srv.getInetAddress(), srv.getLocalPort());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            serverThread.stop();
        } catch (UnsupportedOperationException ex) {
        }
        OpenProjects.getDefault().close(OpenProjects.getDefault().getOpenProjects());
    }

    private class TestDebugProtocolClient implements IDebugProtocolClient {

        private final List<Pair<String, Object>> called;

        public TestDebugProtocolClient(List<Pair<String, Object>> called) {
            this.called = called;
        }
        private int mainThreadID = -1;

        @Override
        public void initialized() {
            recordState("initialized", null);
        }

        @Override
        public void stopped(StoppedEventArguments args) {
            if (mainThreadID == (-1)) {
                mainThreadID = args.getThreadId();
            }
            recordState("stopped", args.getReason());
        }

        @Override
        public void thread(ThreadEventArguments args) {
            recordState("thread", args.getReason() + ":" + (mainThreadID == args.getThreadId()));
        }

        @Override
        public void exited(ExitedEventArguments args) {
            recordState("exited", null);
        }

        @Override
        public void terminated(TerminatedEventArguments args) {
            recordState("terminated", null);
        }

        private void recordState(String event, Object data) {
            synchronized (called) {
                called.add(Pair.of(event, data));
                called.notifyAll();
            }
        }
    }
}
