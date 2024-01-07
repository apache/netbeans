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
package org.netbeans.modules.java.lsp.server.explorer;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.SemanticTokensCapabilities;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.lsp.server.explorer.api.CreateExplorerParams;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangedParams;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeOperationParams;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.DecorationRenderOptions;
import org.netbeans.modules.java.lsp.server.protocol.HtmlPageParams;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeClientCapabilities;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.NbLanguageServer;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.netbeans.modules.java.lsp.server.protocol.SaveDocumentRequestParams;
import org.netbeans.modules.java.lsp.server.protocol.SetTextEditorDecorationParams;
import org.netbeans.modules.java.lsp.server.protocol.ShowStatusMessageParams;
import org.netbeans.modules.java.lsp.server.protocol.TestProgressParams;
import org.netbeans.modules.java.lsp.server.protocol.UpdateConfigParams;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 *
 * @author sdedic
 */
public class ProjectViewTest extends NbTestCase {
    private final Gson gson = new Gson();
    private Socket clientSocket;
    private Thread serverThread;
    
    static {
        // TODO remove ASAP from MicronautGradleArtifactsImplTest and ProjectViewTest
        // investigate "javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure"
        // during gradle download "at org.netbeans.modules.gradle.spi.newproject.TemplateOperation$InitStep.execute(TemplateOperation.java:317)"
        // this looks like a misconfigured webserver to me
        System.setProperty("https.protocols", "TLSv1.2");
    }

    public ProjectViewTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances(new ServerLookupExtractionCommand());
        System.setProperty("java.awt.headless", Boolean.TRUE.toString());
        super.setUp();
        clearWorkDir();
        ServerSocket srv = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        serverThread = new Thread(() -> {
            try {
                Socket server = srv.accept();
                // start the language server
                CommandLine.getDefault().process(new String[] {"--start-java-language-server"}, server.getInputStream(), server.getOutputStream(), System.err, getWorkDir());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });
        serverThread.start();
        clientSocket = new Socket(srv.getInetAddress(), srv.getLocalPort());
    }

    @Override
    protected void tearDown() throws Exception {
        OpenProjects.getDefault().close(OpenProjects.getDefault().openProjects().get());
        // wait until all projects are closed.
        OpenProjects.getDefault().openProjects().get();
        super.tearDown();
    }
    
    
    
    class LspClient implements NbCodeLanguageClient {
        AtomicInteger watchNodeId = new AtomicInteger(-1);
        List<NodeChangedParams> nodeChangeList = new ArrayList<>();
        Semaphore nodeChanges = new Semaphore(0);
        NbCodeClientCapabilities caps = new NbCodeClientCapabilities();
        List<MessageParams> loggedMessages = new ArrayList<>();

        @Override
        public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void notifyProgress(ProgressParams params) {
        }
        
        
        @Override
        public void telemetryEvent(Object arg0) {
        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams params) {
        }

        @Override
        public void showMessage(MessageParams arg0) {
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void logMessage(MessageParams arg0) {
            loggedMessages.add(arg0);
        }

        @Override
        public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void showStatusBarMessage(ShowStatusMessageParams params) {
        }

        @Override
        public CompletableFuture<String> showHtmlPage(HtmlPageParams params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<String> execInHtmlPage(HtmlPageParams params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Map<String, Either<List<QuickPickItem>, String>>> showMultiStepInput(ShowMutliStepInputParams params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void notifyTestProgress(TestProgressParams params) {
        }

        @Override
        public CompletableFuture<String> createTextEditorDecoration(DecorationRenderOptions params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void setTextEditorDecoration(SetTextEditorDecorationParams params) {
        }

        @Override
        public void disposeTextEditorDecoration(String params) {
        }

        @Override
        public void notifyNodeChange(NodeChangedParams params) {
            int watchId = watchNodeId.get();
            if (watchId != -1) {
                if (params.getNodeId() != null) {
                    if (params.getNodeId() != watchId) {
                        return;
                    }
                } else if (params.getRootId() != watchId) {
                    return;
                }
            }
            synchronized (this) {
                nodeChangeList.add(params);
                nodeChanges.release();
            }
        }

        @Override
        public NbCodeClientCapabilities getNbCodeCapabilities() {
            return caps;
        }
        
        public List<NodeChangedParams> waitChanges(long t, TimeUnit u) {
            try {
                assertTrue("Timeout while expecting node change", nodeChanges.tryAcquire(t, u));
            } catch (InterruptedException ex) {
                fail("Interrupted while waiting for node change");
            }
            synchronized (this) {
                List<NodeChangedParams> changes = new ArrayList<>(nodeChangeList);
                nodeChangeList.clear();
                return changes;
            }
        }
        
        public void clearChanges() {
            synchronized (client) {
                client.nodeChangeList.clear();
                client.nodeChanges.drainPermits();
            }
        }

        @Override
        public CompletableFuture<Void> configurationUpdate(UpdateConfigParams params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Boolean> requestDocumentSave(SaveDocumentRequestParams documentUris) {
            return CompletableFuture.completedFuture(false);
        }
    }

    private static Launcher<NbLanguageServer> createLauncher(NbCodeLanguageClient client, InputStream in, OutputStream out,
            Function<MessageConsumer, MessageConsumer> processor) {
        return new LSPLauncher.Builder<NbLanguageServer>()
            .setLocalService(client)
            .setRemoteInterface(NbLanguageServer.class)
            .setInput(in)
            .setOutput(out)
            .wrapMessages(processor)
            .configureGson(gb -> {
                gb.registerTypeAdapter(SemanticTokensCapabilities.class, new InstanceCreator<SemanticTokensCapabilities>() {
                    @Override public SemanticTokensCapabilities createInstance(Type type) {
                        return new SemanticTokensCapabilities(null);
                    }
                });
                gb.registerTypeAdapter(SemanticTokensParams.class, new InstanceCreator<SemanticTokensParams>() {
                    @Override public SemanticTokensParams createInstance(Type type) {
                        return new SemanticTokensParams(new TextDocumentIdentifier(""));
                    }
                });
            })
            .create();
    }

    private String toURI(File f) {
        return Utilities.toURI(f).toString();
    }
    
    FileObject projectDir;
    Project project;
    
    @org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class, position = 1000)
    public static class InstalledFileLocator extends DummyInstalledFileLocator {
        public InstalledFileLocator() {
            registerDestDir(new File(System.getProperty("test.netbeans.dest.dir")));
        }
    }

    private void createSimpleProject() throws IOException {
        FileObject template = FileUtil.getConfigFile("Templates/Project/Gradle/org-netbeans-modules-gradle-java-newproject-SimpleApplicationProjectWizard");
        FileObject from = FileUtil.toFileObject(getDataDir()).getFileObject("gradle-hello/app");
        FileObject dest = FileUtil.toFileObject(getWorkDir());
        FileBuilder b = new FileBuilder(template, dest).name("app").param("packageBase", "gradle.hello");
        List<FileObject> projectFiles = b.build();
        // the template will create a parent project with 'app' application subproject.
        projectDir = projectFiles.get(0).getFileObject("app");
        assertNotNull(projectDir);

        deepCopy(from, projectDir.getParent());
        project = FileOwnerQuery.getOwner(projectDir);
        OpenProjects.getDefault().open(new Project[] { project } , true);
        try {
            OpenProjects.getDefault().openProjects().get();
        } catch (InterruptedException | ExecutionException ex) {
            fail("Unexpected error: " + ex.getMessage());
        }
    }
    
    private FileObject deepCopy(FileObject from, FileObject to) throws IOException {
        if (from.isData()) {
            return FileUtil.copyFile(from, to, from.getName());
        } 
        
        FileObject d = to.getFileObject(from.getNameExt());
        if (d == null) {
            d = to.createFolder(from.getNameExt());
        } else if (!d.isFolder()) {
            throw new IllegalStateException();
        }
        for (FileObject f : from.getChildren()) {
            deepCopy(f, d);
        }
        return d;
    }
    
    private NbLanguageServer server;
    private LspClient client;
    
    private TreeItem createAndFindProjectNode() throws Exception {
        createSimpleProject();
        return findFirstProjectNode();
    }
    
    volatile Lookup serverLookup;

    public class ServerLookupExtractionCommand extends CodeActionsProvider {

        @Override
        public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
            return Collections.emptyList();
        }

        @Override
        public Set<String> getCommands() {
            // this is called during server's initialization.
            serverLookup = Lookup.getDefault();
            return Collections.emptySet();
        }
    }
    
    private TreeItem findFirstProjectNode() throws Exception {
        if (client == null) {
            client = new LspClient();
        }
        Launcher<NbLanguageServer> serverLauncher = createLauncher(client, clientSocket.getInputStream(), clientSocket.getOutputStream(), null);
        serverLauncher.startListening();
        server = serverLauncher.getRemoteProxy();

        InitializeResult result = server.initialize(new InitializeParams()).get();
                
        // by default the ProjectsRootNode initializes its contents asynchronously; a proper reaction to that will be tested in another testcase. Save the complexity here.
        System.setProperty("test.projectnode.sync", "true");

        CompletableFuture<TreeItem> explorer = server.getTreeViewService().explorerManager(new CreateExplorerParams("foundProjects"));
        TreeItem root = explorer.get();
        assertNotNull(root);
        
        int[] childIds = server.getTreeViewService().getChildren(new NodeOperationParams(root.id)).get();
        assertNotNull(childIds);
        assertEquals(1, childIds.length);
        
        TreeItem projectNode = server.getTreeViewService().info(new NodeOperationParams(childIds[0])).get();
        return projectNode;
    }
    
    private TreeItem findChild(TreeItem parent, String... labelPath) throws Exception {
        TreeNodeRegistry reg = serverLookup.lookup(TreeNodeRegistry.class);
        TreeItem item = parent;
        for (String l : labelPath) {
            TreeItem next = findChild(item, l);
            if (next == null) {
                Node n = reg.findNode(item.id);
                Node[] ns = n.getChildren().getNodes(true);
                StringBuilder sb = new StringBuilder();
                for (Node a : ns) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(a.getDisplayName());
                }
                System.out.println("*** Error - node " + l + " does not exist in " + item.label + ", node list: " + sb.toString());
                sb = new StringBuilder();
                for (TreeItem candidate : getChildren(parent)) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(candidate.label);
                }
                System.out.println("TreeItem children: " + sb.toString());
            }
            assertNotNull("There's no " + l + " in " + item.label, next);
            item = next;
        }
        return item;
    }
    
    private TreeItem findChild(TreeItem parent, String childLabel) throws Exception {
        TreeNodeRegistry reg = serverLookup.lookup(TreeNodeRegistry.class);
        reg.findNode(parent.id).getChildren().getNodes(true);
        for (TreeItem candidate : getChildren(parent)) {
            if (childLabel.equals(candidate.label)) {
                return candidate;
            }
        }
        return null;
    }
    
    private List<TreeItem> getChildren(TreeItem parent) throws Exception {
        int[] childIds = server.getTreeViewService().getChildren(new NodeOperationParams(parent.id)).get();
        List<TreeItem> l = new ArrayList<>(childIds.length);
        for (int i : childIds) {
            l.add(server.getTreeViewService().info(new NodeOperationParams(i)).get());
        }
        return l;
    }

    public void testProjectExplorerExists() throws Exception {
        TreeItem projectNode = createAndFindProjectNode();
        assertEquals("app:app", projectNode.label);
    }
    
    public void testProjectSourcePackages() throws Exception {
        TreeItem mainJava = findChild(createAndFindProjectNode(), "Source Packages [java]", "gradle.hello", "App.java");
        assertNotNull(mainJava);
    }
    
    /**
     * A TreeItem should appear after a file is created. An event on the parent TreeItem must be fired
     * before the new TreeItem appears as a result of the file creation.
     * @throws Exception 
     */
    public void testCreatedPeerFileAppears() throws Exception {
        TreeItem root = createAndFindProjectNode();
        TreeItem helloPackage = findChild(root, "Source Packages [java]", "gradle.hello");
        // "expand" to the level of files
        TreeItem mainJava = findChild(root, "Source Packages [java]", "gradle.hello", "App.java");
        
        assertNotNull(helloPackage);
        assertNotNull(mainJava);
        
        client.watchNodeId.set(helloPackage.id);
        
        FileObject dir = projectDir.getFileObject("src/main/java/gradle/hello");
        
        // clear event queue
        client.clearChanges();
        FileObject newFile = dir.createData("Bubu.txt"); 
        TreeItem found = null;
        
        while (found == null) {
            List<NodeChangedParams> copy = client.waitChanges(10, TimeUnit.SECONDS);
            for (NodeChangedParams ncp : copy) {
                found = findChild(helloPackage, "Bubu.txt");
                if (found != null) {
                    break;
                }
            }
        }
        
        assertEquals(URLMapper.findURL(newFile, URLMapper.EXTERNAL).toString(), found.resourceUri);
    }
    
    /**
     * A TreeItem should be reported for a new package, if a file is added to a parent folder with only
     * folders (= packages) present at the moment. The project view flattens packages, so the new node
     * will appear inside the project's Source Root node.
     * @throws Exception 
     */
    public void testNewPackageaAppears() throws Exception {
        TreeItem root = createAndFindProjectNode();
        TreeItem sourceRoot = findChild(root, "Source Packages [java]");
        // "expand" to the level of files
        TreeItem mainJava = findChild(root, "Source Packages [java]", "gradle.hello", "App.java");
        client.watchNodeId.set(sourceRoot.id);
        
        FileObject dir = projectDir.getFileObject("src/main/java/gradle");
        
        // clear event queue
        client.clearChanges();
        FileObject newFile = dir.createData("Bubu.txt"); 
        TreeItem found = null;
        
        while (found == null) {
            List<NodeChangedParams> copy = client.waitChanges(10, TimeUnit.SECONDS);
            for (NodeChangedParams ncp : copy) {
                found = findChild(sourceRoot, "gradle");
                if (found != null) {
                    break;
                }
            }
        }
    }
    
    /**
     * Checks that a query to a node that does not exist at all will not fail with an exception.
     * @throws Exception 
     */
    public void testQueryNonExistentNode() throws Exception {
        TreeItem root = createAndFindProjectNode();
        TreeItem item = server.getTreeViewService().info(new NodeOperationParams(111)).get();
        assertNotNull(item);
        assertEquals("Items that do not exist must report -1", -1, item.id);
    }
    
    /**
     * TreeItem that represents a file that is deleted should disappear + necessary events for
     * its parent.
     * @throws Exception 
     */
    public void testDeletedFileDisappears() throws Exception {
        TreeItem root = createAndFindProjectNode();
        TreeItem helloPackage = findChild(root, "Source Packages [java]", "gradle.hello");
        // "expand" to the level of files
        client.watchNodeId.set(helloPackage.id);
        
        TreeItem found = findChild(root, "Source Packages [java]", "gradle.hello", "Whatever.txt");
        assertNotNull("Marker file must exist", found);
        
        FileObject toDelete = projectDir.getFileObject("src/main/java/gradle/hello/Whatever.txt");

        client.clearChanges();
        toDelete.delete();
        
        while (found != null) {
            List<NodeChangedParams> copy = client.waitChanges(10, TimeUnit.SECONDS);
            for (NodeChangedParams ncp : copy) {
                found = findChild(helloPackage, "Whatever.txt");
            }
        }
    }
    
    /**
     * An empty package should be removed - after the last file in it is deleted.
     * @throws Exception 
     */
    public void testEmptyInterimPackageRemoved() throws Exception {
        TreeItem root = createAndFindProjectNode();
        FileObject createIn = projectDir.getFileObject("src/test/java/gradle");
        // create a file in an interim package -> a new package node will be returned.
        // note the subtree is not yet loaded.
        FileObject createdFile = createIn.createData("Interim.java");
        TreeItem testRoot = findChild(root, "Test Packages [java]");
        // "expand" to the level of files
        client.watchNodeId.set(testRoot.id);
        
        TreeItem found = findChild(testRoot, "gradle");
        assertNotNull("Test package exists", found);
        TreeNodeRegistry reg = serverLookup.lookup(TreeNodeRegistry.class);
        reg.findNode(found.id).getChildren().getNodes(true);
        int[] childIds = server.getTreeViewService().getChildren(new NodeOperationParams(found.id)).get();
        assertEquals("Test package node is not empty", 1, childIds.length);
        
        TreeItem createdItem = findChild(testRoot, "gradle", "Interim.java");
        assertNotNull(createdItem);
        
        createdFile.delete();
        
        while (found != null) {
            List<NodeChangedParams> copy = client.waitChanges(10, TimeUnit.SECONDS);
            for (NodeChangedParams ncp : copy) {
                found = findChild(testRoot, "gradle");
            }
        }
    }

    
    public void testEmptyPackageRemains() throws Exception {
        TreeItem root = createAndFindProjectNode();
        TreeItem helloPackage = findChild(root, "Test Packages [java]", "gradle.hello");
        TreeItem found = findChild(root, "Test Packages [java]", "gradle.hello", "AppTest.java");
        
        client.watchNodeId.set(helloPackage.id);
        assertNotNull(found);
        
        FileObject created = projectDir.getFileObject("src/test/java/gradle/hello/AppTest.java");
        client.clearChanges();
        created.delete();
        
        while (found != null) {
            List<NodeChangedParams> copy = client.waitChanges(10, TimeUnit.SECONDS);
            for (NodeChangedParams ncp : copy) {
                found = findChild(helloPackage, "AppTest.java");
            }
        }
        
        TreeItem hello2 = findChild(root, "Test Packages [java]", "gradle.hello");
        assertNotNull("Empty existing package must remain", hello2);
    }
    
    /**
     * Checks that it is OK (not exception) to query a node that really existed, but its
     * file was deleted and the node has vanished.
     * 
     * @throws Exception 
     */
    public void testOnceExistedDeletedItemInfo() throws Exception {
        TreeItem root = createAndFindProjectNode();
        TreeItem helloPackage = findChild(root, "Test Packages [java]", "gradle.hello");
        TreeItem found = findChild(root, "Test Packages [java]", "gradle.hello", "AppTest.java");
        TreeItem original = found;
        
        client.watchNodeId.set(helloPackage.id);
        assertNotNull(found);
        
        FileObject created = projectDir.getFileObject("src/test/java/gradle/hello/AppTest.java");
        client.clearChanges();
        created.delete();
        
        while (found != null) {
            List<NodeChangedParams> copy = client.waitChanges(10, TimeUnit.SECONDS);
            for (NodeChangedParams ncp : copy) {
                found = findChild(helloPackage, "AppTest.java");
            }
        }
        
        server.getTreeViewService().info(new NodeOperationParams(original.id));
    }
}
