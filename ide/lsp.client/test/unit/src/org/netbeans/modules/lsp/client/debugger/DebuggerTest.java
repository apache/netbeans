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
package org.netbeans.modules.lsp.client.debugger;

import java.io.ByteArrayOutputStream;
import org.netbeans.modules.lsp.client.debugger.api.DAPConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.ProcessBuilder.Redirect;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.text.Document;
import junit.framework.Test;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.Assert;
import org.netbeans.Main;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.lsp.client.debugger.breakpoints.DAPLineBreakpoint;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

public class DebuggerTest extends NbTestCase {

    private static final String javaLauncher = new File(new File(System.getProperty("java.home"), "bin"), "java").getAbsolutePath();
    private FileObject project;
    private FileObject srcDir;
    private String srcDirURL;
    private FileObject testFile;
    private String testFileURL;

    public DebuggerTest(String name) {
        super(name);
    }

    public void testStartDebugger() throws Exception {
        writeTestFile("""
                      package test;                                             // 1
                      public class Test {                                       // 2
                          public static void main(String... args) {             // 3
                              System.err.println(1);                            // 4
                              nestedPrint("2");                                 // 5
                              nestedPrint("3");                                 // 6
                              nestedPrint("4");                                 // 7
                              nestedPrint("5");                                 // 8
                          }                                                     // 9
                          private static void nestedPrint(String toPrint) {     //10
                              System.err.println(toPrint);                      //11
                          }                                                     //12
                      }                                                         //13
                      """);

        DebuggerManager manager = DebuggerManager.getDebuggerManager();

        manager.addBreakpoint(DAPLineBreakpoint.create(testFileURL, 4));
        DAPLineBreakpoint line6Breakpoint = DAPLineBreakpoint.create(testFileURL, 6);
        manager.addBreakpoint(line6Breakpoint);
        int backendPort = startBackend();
        Socket socket = new Socket("localhost", backendPort);
        DAPConfiguration.create(socket.getInputStream(), socket.getOutputStream())
                        .addConfiguration(Map.of("type", "java+",
                                                 "request", "launch",
                                                 "file", FileUtil.toFile(testFile).getAbsolutePath(),
                                                 "classPaths", List.of("any")))
                        .launch();
        waitFor(true, () -> DebuggerManager.getDebuggerManager().getSessions().length > 0);
        assertEquals(1, DebuggerManager.getDebuggerManager().getSessions().length);
        Session session = DebuggerManager.getDebuggerManager().getSessions()[0];
        assertNotNull(session);
        ActionsManager am = session.getCurrentEngine().getActionsManager();
        //wait until it stops at breakpoint:
        waitFor(START_TIMEOUT, List.of("4: CurrentPC"), () -> readAnnotations());

        //step over a statement:
        waitFor(true, () -> am.isEnabled(ActionsManager.ACTION_STEP_OVER));
        am.postAction(ActionsManager.ACTION_STEP_OVER);

        //wait until it stops after the step:
        waitFor(List.of("5: CurrentPC"), () -> readAnnotations());

        //step into the method
        waitFor(true, () -> am.isEnabled(ActionsManager.ACTION_STEP_INTO));
        am.postAction(ActionsManager.ACTION_STEP_INTO);

        //wait until it stops:
        waitFor(List.of("5: CallSite", "11: CurrentPC"), () -> readAnnotations());
        //and verify Variables view contain an expected variable, with an expected value:
        waitFor("Local/toPrint:String:\"2\"", () -> getVariableNameTypeValue(session, "Local/toPrint"));

        //tweak breakpoints:
        manager.removeBreakpoint(line6Breakpoint);
        manager.addBreakpoint(DAPLineBreakpoint.create(testFileURL, 7));
        //continue to debugging - should finish at line 7, not 6:
        waitFor(true, () -> am.isEnabled(ActionsManager.ACTION_CONTINUE));
        am.postAction(ActionsManager.ACTION_CONTINUE);

        //wait until it stops after the step:
        waitFor(List.of("7: CurrentPC"), () -> readAnnotations());

        //continue to finish debugging:
        waitFor(true, () -> am.isEnabled(ActionsManager.ACTION_CONTINUE));
        am.postAction(ActionsManager.ACTION_CONTINUE);

        //verify things are cleaned up:
        waitFor(0, () -> DebuggerManager.getDebuggerManager().getSessions().length);
        waitFor(List.of(), () -> readAnnotations());
    }

    public void testStopDebuggerAndBreakpointConditions() throws Exception {
        writeTestFile("""
                      package test;                                             // 1
                      public class Test {                                       // 2
                          public static void main(String... args) {             // 3
                              System.err.println(1);                            // 4
                              nestedPrint("2");                                 // 5
                              nestedPrint("3");                                 // 6
                              nestedPrint("4");                                 // 7
                              nestedPrint("5");                                 // 8
                          }                                                     // 9
                          private static void nestedPrint(String toPrint) {     //10
                              System.err.println(toPrint);                      //11
                          }                                                     //12
                      }                                                         //13
                      """);

        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        DAPLineBreakpoint breakpoint = DAPLineBreakpoint.create(testFileURL, 11);

        breakpoint.setCondition("\"4\".equals(toPrint)");
        manager.addBreakpoint(breakpoint);
        int backendPort = startBackend();
        Socket socket = new Socket("localhost", backendPort);
        DAPConfiguration.create(socket.getInputStream(), socket.getOutputStream())
                        .addConfiguration(Map.of("type", "java+",
                                                 "request", "launch",
                                                 "file", FileUtil.toFile(testFile).getAbsolutePath(),
                                                 "classPaths", List.of("any")))
                        .launch();
        waitFor(true, () -> DebuggerManager.getDebuggerManager().getSessions().length > 0);
        assertEquals(1, DebuggerManager.getDebuggerManager().getSessions().length);
        Session session = DebuggerManager.getDebuggerManager().getSessions()[0];
        assertNotNull(session);
        ActionsManager am = session.getCurrentEngine().getActionsManager();
        //wait until it stops at breakpoint:
        waitFor(START_TIMEOUT, List.of("7: CallSite", "11: CurrentPC"), () -> readAnnotations());

        //step over a statement:
        waitFor(true, () -> am.isEnabled(ActionsManager.ACTION_KILL));
        am.postAction(ActionsManager.ACTION_KILL);

        //verify things are cleaned up:
        waitFor(0, () -> DebuggerManager.getDebuggerManager().getSessions().length);
        waitFor(List.of(), () -> readAnnotations());
    }

    private void writeTestFile(String code) throws IOException {
        project = FileUtil.createFolder(new File(getWorkDir(), "prj"));
        srcDir = FileUtil.createFolder(project, "src/main/java");
        srcDirURL = srcDir.toURL().toString();
        testFile = FileUtil.createData(srcDir, "test/Test.java");
        try (OutputStream out = testFile.getOutputStream();
             Writer w = new OutputStreamWriter(out)) {
            w.write(code);
        }
        testFileURL = testFile.toURL().toString();
        try (OutputStream out = FileUtil.createData(project, "pom.xml").getOutputStream();
             Writer w = new OutputStreamWriter(out)) {
            w.write("""
                    <?xml version="1.0" encoding="UTF-8"?>
                    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                        <modelVersion>4.0.0</modelVersion>
                        <groupId>test</groupId>
                        <artifactId>test</artifactId>
                        <version>1.0-SNAPSHOT</version>
                        <packaging>jar</packaging>
                        <properties>
                            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                            <maven.compiler.release>17</maven.compiler.release>
                        </properties>
                    </project>
                    """);
        }
    }

    private static final int DEFAULT_TIMEOUT = 10_000;
//    private static final int DEFAULT_TIMEOUT = 1_000_000; //for debugging
    private static final int START_TIMEOUT = Math.max(60_000, DEFAULT_TIMEOUT);
    private static final int DELAY = 100;

    private <T> void waitFor(T expectedValue, Supplier<T> actualValue) {
        waitFor(DEFAULT_TIMEOUT, expectedValue, actualValue);
    }

    private <T> void waitFor(int timeout, T expectedValue, Supplier<T> actualValue) {
        long s = System.currentTimeMillis();
        T lastActualvalue = null;

        while (true) {
            if (Objects.equals(lastActualvalue = actualValue.get(), expectedValue)) {
                return ;
            }
            if ((System.currentTimeMillis() - s) > timeout) {
                break;
            }
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        fail("Didn't finish in time, last actual value: " + lastActualvalue);
    }

    private List<String> readAnnotations() {
        List<String> result = new ArrayList<>();

        assertNotNull(testFile);
        EditorCookie ec = testFile.getLookup().lookup(EditorCookie.class);
        Document doc = ec.getDocument();

        if (doc == null) {
            return result;
        }

        Annotations annos = ((BaseDocument) doc).getAnnotations();
        int currentLine = -1;

        while (true) {
            int prevLine = currentLine;

            currentLine = annos.getNextLineWithAnnotation(currentLine + 1);

            if (currentLine == prevLine + 1) {
                break;
            }

            List<AnnotationDesc> annotations = new ArrayList<>();
            AnnotationDesc active = annos.getActiveAnnotation(currentLine);

            if (active != null) {
                annotations.add(active);
            }

            AnnotationDesc[] passive = annos.getPassiveAnnotationsForLine(currentLine);

            if (passive != null) {
                annotations.addAll(Arrays.asList(passive));
            }

            if (annotations.isEmpty()) {
                break;
            }

            result.add("" + (currentLine + 1) + ": " + annotations.stream().map(desc -> desc.getAnnotationType()).collect(Collectors.joining(", ")));
        }

        return result;
    }

    private String getVariableNameTypeValue(Session session, String variablePath) {
        try {
            TreeModel variablesTree = session.lookupFirst("LocalsView", TreeModel.class);
            Element found = findTreeNode(variablesTree, variablePath);
            TableModel variablesTable = (TableModel) variablesTree;

            if (found == null) {
                return "<not found>";
            }

            return found.path + ":" +
                    variablesTable.getValueAt(found.key, "LocalsType") + ":" +
                    variablesTable.getValueAt(found.key, "LocalsValue");
        } catch (UnknownTypeException ex) {
            throw new AssertionError(ex);
        }
    }


    private Element findTreeNode(TreeModel treeModel, String findPath) {
        try {
            NodeModel nodeModel = (NodeModel) treeModel;
            List<Element> todo = new ArrayList<>();
            todo.add(new Element("", treeModel.getRoot()));
            while (!todo.isEmpty()) {
                Element current = todo.remove(0);
                if (findPath.equals(current.path)) {
                    return current;
                }
                int childrenCount = treeModel.getChildrenCount(current.key);
                Object[] children = treeModel.getChildren(current.key, 0, childrenCount);
                for (Object child : children) {
                    String displayName = nodeModel.getDisplayName(child);
                    String path = current.path.isEmpty() ? displayName
                                                         : current.path + "/" + displayName;

                    todo.add(new Element(path, child));
                }
            }
        } catch (UnknownTypeException ex) {
            throw new AssertionError(ex);
        }
        return null;
    }

    record Element(String path, Object key) {}

    private static File toFile(URI uri) {
        return Paths.get(uri).toFile();
    }

    private static final Pattern PORT = Pattern.compile("Listening for transport dt_socket at address: ([0-9]+)\n");
    private int startDebugee() throws Exception {
        //XXX: should not use a hard-coded port
        Process p = new ProcessBuilder(javaLauncher, "-agentlib:jdwp=transport=dt_socket,suspend=y,server=y,address=0", FileUtil.toFile(testFile).getAbsolutePath())
                .inheritIO()
                .redirectOutput(Redirect.PIPE)
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            p.destroyForcibly();
        }));
        CountDownLatch portFound = new CountDownLatch(1);
        AtomicInteger port = new AtomicInteger();
        new Thread(() -> {
            InputStream in = p.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int r;
            try {
                while ((r = in.read()) != (-1)) {
                    output.write(r);
                    System.out.write(r);
                    Matcher m = PORT.matcher(new String(output.toByteArray()));
                    if (m.find()) {
                        port.set(Integer.parseInt(m.group(1)));
                        portFound.countDown();
                        break;
                    }
                }
                while ((r = in.read()) != (-1)) {
                    System.out.write(r);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
        if (!portFound.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("Didn't detect port before timeout.");
        }
        return port.get();
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(DebuggerTest.class)
                .enableModules(".*").clusters("platform|ide").gui(false));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        getWorkDir();
    }

//<editor-fold defaultstate="collapsed" desc="startBackend">
    private static final Pattern DAP_PORT = Pattern.compile("Java Debug Server Adapter listening at port ([0-9]+)\n");
    private static final Pattern LSP_PORT = Pattern.compile("Java Language Server listening at port ([0-9]+)\n");

    private static int backendPort = -1;

    private int startBackend() throws Exception {
        if (backendPort == (-1)) {
            backendPort = doStartBackend();
        }

        return backendPort;
    }

    private static int doStartBackend() throws Exception {
        List<String> options = new ArrayList<>();
        options.add(javaLauncher);
        options.add("--add-opens=java.base/java.net=ALL-UNNAMED");

        File platform = findPlatform();
        List<File> bootCP = new ArrayList<>();
        List<File> dirs = new ArrayList<>();
        dirs.add(new File(platform, "lib"));

        File jdkHome = new File(System.getProperty("java.home"));
        if (new File(jdkHome.getParentFile(), "lib").exists()) {
            jdkHome = jdkHome.getParentFile();
        }
        dirs.add(new File(jdkHome, "lib"));

        //in case we're running code coverage, load the coverage libraries
        if (System.getProperty("code.coverage.classpath") != null) {
            dirs.add(new File(System.getProperty("code.coverage.classpath")));
        }

        for (File dir: dirs) {
            File[] jars = dir.listFiles();
            if (jars != null) {
                for (File jar : jars) {
                    if (jar.getName().endsWith(".jar")) {
                        bootCP.add(jar);
                    }
                }
            }
        }

        options.add("-cp"); options.add(bootCP.stream().map(jar -> jar.getAbsolutePath()).collect(Collectors.joining(System.getProperty("path.separator"))));

        options.add("-Djava.util.logging.config=-");
        options.add("-Dnetbeans.logger.console=true");
        options.add("-Dnetbeans.logger.noSystem=true");
        options.add("-Dnetbeans.home=" + platform.getPath());
        options.add("-Dnetbeans.full.hack=true");
        options.add("-DTopSecurityManager.disable=true");

        String branding = System.getProperty("branding.token"); // NOI18N
        if (branding != null) {
            options.add("-Dbranding.token=" + branding);
        }

        File ud = new File(new File(Manager.getWorkDirPath()), "userdir");

        deleteRecursivelly(ud);

        ud.mkdirs();

        options.add("-Dnetbeans.user=" + ud.getPath());

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (File f : findClusters()) {
            if (f.getPath().endsWith("ergonomics")) {
                continue;
            }
            sb.append(sep);
            sb.append(f.getPath());
            sep = File.pathSeparator;
        }
        options.add("-Dnetbeans.dirs=" + sb.toString());

        options.add("-Dnetbeans.security.nocheck=true");

//        options.add("-agentlib:jdwp=transport=dt_socket,suspend=y,server=y,address=8000");

        options.add(Main.class.getName());
        options.add("--nosplash");
        options.add("--nogui");

        options.add("--start-java-language-server=listen:0");
        options.add("--start-java-debug-adapter-server=listen:0");

        Process p = new ProcessBuilder(options).redirectError(Redirect.INHERIT).start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                p.destroyForcibly();
            }
        });

        ByteArrayOutputStream outData = new ByteArrayOutputStream();
        new RequestProcessor(DebuggerTest.class.getName(), 1, false, false).post(() -> {
            try {
                InputStream in = p.getInputStream();
                int r;
                while ((r = in.read()) != (-1)) {
                    synchronized (outData) {
                        outData.write(r);
                        outData.notifyAll();
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            synchronized (outData) {
                outData.notifyAll();
            }
        });

        synchronized (outData) {
            int backendPort = (-1);
            boolean lspServerConnected = false;

            while (p.isAlive()) {
                Matcher dapMatcher = DAP_PORT.matcher(new String(outData.toByteArray()));
                if (dapMatcher.find()) {
                    backendPort = Integer.parseInt(dapMatcher.group(1));
                }
                Matcher lspMatcher = LSP_PORT.matcher(new String(outData.toByteArray()));
                if (lspMatcher.find()) {
                    //must connect a (dummy) LSP client, so that the Java debugger's "launch" works:
                    Socket lspSocket = new Socket("localhost", Integer.parseInt(lspMatcher.group(1)));
                    Launcher<LanguageServer> serverLauncher = LSPLauncher.createClientLauncher(new LanguageClient() {
                        @Override
                        public void telemetryEvent(Object object) {}
                        @Override
                        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {}
                        @Override
                        public void showMessage(MessageParams messageParams) {}
                        @Override
                        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public void logMessage(MessageParams message) {}

                        @Override
                        public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
                            CompletableFuture<List<Object>> result = new CompletableFuture<>();
                            result.complete(List.of());
                            return result;
                        }

                    }, lspSocket.getInputStream(), lspSocket.getOutputStream());
                    serverLauncher.startListening();
                    serverLauncher.getRemoteProxy().initialize(new InitializeParams()).get();
                    serverLauncher.getRemoteProxy().initialized(new InitializedParams());
                    lspServerConnected = true;
                }
                if (lspServerConnected && backendPort != (-1)) {
                    return backendPort;
                }
                outData.wait();
            }
        }

        throw new AssertionError("Cannot start backend");
    }

    static File findPlatform() {
        String clusterPath = System.getProperty("cluster.path.final"); // NOI18N
        if (clusterPath != null) {
            for (String piece : tokenizePath(clusterPath)) {
                File d = new File(piece);
                if (d.getName().matches("platform\\d*")) {
                    return d;
                }
            }
        }
        String allClusters = System.getProperty("all.clusters"); // #194794
        if (allClusters != null) {
            File d = new File(allClusters, "platform"); // do not bother with old numbered variants
            if (d.isDirectory()) {
                return d;
            }
        }
        try {
            Class<?> lookup = Class.forName("org.openide.util.Lookup"); // NOI18N
            File util = toFile(lookup.getProtectionDomain().getCodeSource().getLocation().toURI());
            Assert.assertTrue("Util exists: " + util, util.exists());

            return util.getParentFile().getParentFile();
        } catch (Exception ex) {
            try {
                File nbjunit = toFile(NbModuleSuite.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                File harness = nbjunit.getParentFile().getParentFile();
                Assert.assertEquals(nbjunit + " is in a folder named 'harness'", "harness", harness.getName());
                TreeSet<File> sorted = new TreeSet<File>();
                for (File p : harness.getParentFile().listFiles()) {
                    if (p.getName().startsWith("platform")) {
                        sorted.add(p);
                    }
                }
                Assert.assertFalse("Platform shall be found in " + harness.getParent(), sorted.isEmpty());
                return sorted.last();
            } catch (Exception ex2) {
                Assert.fail("Cannot find utilities JAR: " + ex + " and: " + ex2);
            }
            return null;
        }
    }

    private static File[] findClusters() throws IOException {
        Collection<File> clusters = new LinkedHashSet<File>();

        //not apisupport, so that the apisupport project do not recognize the test workdirs, so that the multi-source support can work on it:
        findClusters(clusters, List.of("platform|ide|extide|java"));

        return clusters.toArray(new File[0]);
    }

    private static String[] tokenizePath(String path) {
        List<String> l = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(path, ":;", true); // NOI18N
        char dosHack = '\0';
        char lastDelim = '\0';
        int delimCount = 0;
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            if (s.length() == 0) {
                // Strip empty components.
                continue;
            }
            if (s.length() == 1) {
                char c = s.charAt(0);
                if (c == ':' || c == ';') {
                    // Just a delimiter.
                    lastDelim = c;
                    delimCount++;
                    continue;
                }
            }
            if (dosHack != '\0') {
                // #50679 - "C:/something" is also accepted as DOS path
                if (lastDelim == ':' && delimCount == 1 && (s.charAt(0) == '\\' || s.charAt(0) == '/')) {
                    // We had a single letter followed by ':' now followed by \something or /something
                    s = "" + dosHack + ':' + s;
                    // and use the new token with the drive prefix...
                } else {
                    // Something else, leave alone.
                    l.add(Character.toString(dosHack));
                    // and continue with this token too...
                }
                dosHack = '\0';
            }
            // Reset count of # of delimiters in a row.
            delimCount = 0;
            if (s.length() == 1) {
                char c = s.charAt(0);
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    // Probably a DOS drive letter. Leave it with the next component.
                    dosHack = c;
                    continue;
                }
            }
            l.add(s);
        }
        if (dosHack != '\0') {
            //the dosHack was the last letter in the input string (not followed by the ':')
            //so obviously not a drive letter.
            //Fix for issue #57304
            l.add(Character.toString(dosHack));
        }
        return l.toArray(new String[0]);
    }

    static void findClusters(Collection<File> clusters, List<String> regExps) throws IOException {
        File plat = findPlatform().getCanonicalFile();
        String selectiveClusters = System.getProperty("cluster.path.final"); // NOI18N
        Set<File> path;
        if (selectiveClusters != null) {
            path = new TreeSet<File>();
            for (String p : tokenizePath(selectiveClusters)) {
                File f = new File(p);
                path.add(f.getCanonicalFile());
            }
        } else {
            File parent;
            String allClusters = System.getProperty("all.clusters"); // #194794
            if (allClusters != null) {
                parent = new File(allClusters);
            } else {
                parent = plat.getParentFile();
            }
            path = new TreeSet<File>(Arrays.asList(parent.listFiles()));
        }
        for (String c : regExps) {
            for (File f : path) {
                if (f.equals(plat)) {
                    continue;
                }
                if (!f.getName().matches(c)) {
                    continue;
                }
                File m = new File(new File(f, "config"), "Modules");
                if (m.exists()) {
                    clusters.add(f);
                }
            }
        }
    }

    private static void deleteRecursivelly(File ud) {
        if (ud.isDirectory()) {
            File[] list = ud.listFiles();

            if (list != null) {
                for (File c : list) {
                    deleteRecursivelly(c);
                }
            }
        }

        ud.delete();
    }
//</editor-fold>
}
