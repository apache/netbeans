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
package org.netbeans.modules.java.lsp.server.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ShowDocumentParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolLocation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodFinder;
import org.netbeans.modules.java.hints.spi.preview.PreviewEnabler;
import org.netbeans.modules.java.hints.spi.preview.PreviewEnabler.Factory;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.debugging.attach.AttachConfigurations;
import org.netbeans.modules.java.lsp.server.debugging.attach.AttachNativeConfigurations;
import org.netbeans.modules.java.lsp.server.project.LspProjectInfo;
import org.netbeans.modules.java.lsp.server.singlesourcefile.SingleFileOptionsQueryImpl;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.ui.JavaSymbolProvider;
import org.netbeans.modules.java.source.ui.JavaTypeProvider;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.lsp.ErrorProvider;
import org.netbeans.spi.lsp.StructureProvider;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public final class WorkspaceServiceImpl implements WorkspaceService, LanguageClientAware {
    
    private static final Logger LOG = Logger.getLogger(WorkspaceServiceImpl.class.getName());

    private static final RequestProcessor WORKER = new RequestProcessor(WorkspaceServiceImpl.class.getName(), 1, false, false);
    private static final RequestProcessor PROJECT_WORKER = new RequestProcessor(WorkspaceServiceImpl.class.getName(), 5, false, false);
    private static final String NETBEANS_JAVA_HINTS = "hints";

    private final Gson gson = new Gson();
    private final LspServerState server;
    private NbCodeLanguageClient client;

    /**
     * List of workspace folders as reported by the client. Initialized in `initialize` request,
     * and then updated by didChangeWorkspaceFolder notifications.
     */
    private volatile List<FileObject> clientWorkspaceFolders = Collections.emptyList();
    
    WorkspaceServiceImpl(LspServerState server) {
        this.server = server;
    }

    /**
     * Returns the set of workspace folders reported by the client. If a folder from the list is recognized
     * as a project, it will be also present in {@link #openedProjects()} including all its subprojects.
     * The list of client workspace folders contains just toplevel items in client's workspace, as defined in
     * LSP protocol.
     * @return list of workspace folders
     */
    public List<FileObject> getClientWorkspaceFolders() {
        return new ArrayList<>(clientWorkspaceFolders);
    }

    public void setClientWorkspaceFolders(List<WorkspaceFolder> clientWorkspaceFolders) {
        if (clientWorkspaceFolders == null) {
            return;
        }
        List<FileObject> newWorkspaceFolders = new ArrayList<>(this.clientWorkspaceFolders);
        try {
            for (WorkspaceFolder clientWorkspaceFolder : clientWorkspaceFolders) {
                newWorkspaceFolders.add(Utils.fromUri(clientWorkspaceFolder.getUri()));
            }
            this.clientWorkspaceFolders = newWorkspaceFolders;
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        String command = Utils.decodeCommand(params.getCommand(), client.getNbCodeCapabilities());
        switch (command) {
            case Server.NBLS_GRAALVM_PAUSE_SCRIPT:
                ActionsManager am = DebuggerManager.getDebuggerManager().getCurrentEngine().getActionsManager();
                am.doAction("pauseInGraalScript");
                return CompletableFuture.completedFuture(true);
            case Server.NBLS_NEW_FROM_TEMPLATE:
                return LspTemplateUI.createFromTemplate("Templates", client, params);
            case Server.NBLS_NEW_PROJECT:
                return LspTemplateUI.createProject("Templates/Project", client, params);
            case Server.NBLS_BUILD_WORKSPACE: {
                final CommandProgress progressOfCompilation = new CommandProgress();
                final Lookup ctx = Lookups.singleton(progressOfCompilation);
                for (Project prj : server.openedProjects().getNow(OpenProjects.getDefault().getOpenProjects())) {
                    ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
                    if (ap != null && ap.isActionEnabled(ActionProvider.COMMAND_BUILD, Lookup.EMPTY)) {
                        ap.invokeAction(ActionProvider.COMMAND_REBUILD, ctx);
                    }
                }
                progressOfCompilation.checkStatus();
                return progressOfCompilation.getFinishFuture();
            }
            case Server.NBLS_RUN_PROJECT_ACTION: {
                // TODO: maybe a structure would be better for future compatibility / extensions, i.e. what to place in the action's context Lookup.
                List<FileObject> targets = new ArrayList<>();
                ProjectActionParams actionParams = gson.fromJson(gson.toJson(params.getArguments().get(0)), ProjectActionParams.class);
                String actionName = actionParams.getAction();
                String configName = actionParams.getConfiguration();
                boolean acceptDefault = actionParams.getFallbackDefault() == Boolean.TRUE;
                
                for (int i = 1; i < params.getArguments().size(); i++) {
                    JsonElement item = gson.fromJson(gson.toJson(params.getArguments().get(i)), JsonElement.class);                    
                    if (item.isJsonPrimitive()) {
                        String uri = item.getAsString();
                        FileObject file;
                        try {
                            file = URLMapper.findFileObject(new URL(uri));
                        } catch (MalformedURLException ex) {
                            // TODO: report an invalid parameter or ignore ?
                            continue;
                        }
                        targets.add(file);
                    }
                }
                // also forms invokeAction off the main LSP thread.
                return server.asyncOpenSelectedProjects(targets, false).thenCompose((Project[] owners) -> {
                    Map<Project, List<FileObject>> items = new LinkedHashMap<>();
                    for (int i = 0; i < owners.length; i++) {
                        if (owners[i] == null) {
                            continue;
                        }
                        items.computeIfAbsent(owners[i], (p) -> new ArrayList<>()).add(targets.get(i));
                    }
                    final CommandProgress progressOfCompilation = new CommandProgress();
                    boolean someStarted = false;
                    boolean configNotFound = false;
                    
                    for (Project prj : items.keySet()) {
                        List<Object> ctxObjects = new ArrayList<>();
                        ctxObjects.add(progressOfCompilation);
                        ctxObjects.addAll(items.get(prj));
                        if (!StringUtils.isBlank(configName)) {
                            ProjectConfigurationProvider<ProjectConfiguration> pcp = prj.getLookup().lookup(ProjectConfigurationProvider.class);
                            if (pcp != null) {
                                Optional<ProjectConfiguration> cfg = pcp.getConfigurations().stream().filter(c -> c.getDisplayName().equals(configName)).findAny();
                                if (cfg.isPresent()) {
                                    ctxObjects.add(cfg);
                                } else if (!acceptDefault) {
                                    // TODO: report ? Fail the action ? Fallback to default config ?
                                    configNotFound = true;
                                    continue;
                                }
                            }
                        }
                        // TBD: possibly include project configuration ?
                        final Lookup ctx = Lookups.fixed(ctxObjects.toArray(new Object[0]));
                        ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
                        if (ap != null && ap.isActionEnabled(actionName, ctx)) {
                            ap.invokeAction(actionName, ctx);
                            someStarted = true;
                        }
                    }
                    if (!configNotFound || !someStarted) {
                        // TODO: print a message like 'nothing to do' in the status bar ?
                        return CompletableFuture.completedFuture(false);
                    }
                    final boolean cfgNotFound = configNotFound;
                    progressOfCompilation.checkStatus();
                    return progressOfCompilation.getFinishFuture().thenApply(b -> (b == Boolean.TRUE) && cfgNotFound);
                });
            }
            case Server.NBLS_CLEAN_WORKSPACE: {
                final CommandProgress progressOfCompilation = new CommandProgress();
                final Lookup ctx = Lookups.singleton(progressOfCompilation);
                for (Project prj : server.openedProjects().getNow(OpenProjects.getDefault().getOpenProjects())) {
                    ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
                    if (ap != null && ap.isActionEnabled(ActionProvider.COMMAND_CLEAN, Lookup.EMPTY)) {
                        ap.invokeAction(ActionProvider.COMMAND_CLEAN, ctx);
                    }
                }
                progressOfCompilation.checkStatus();
                return progressOfCompilation.getFinishFuture();
            }
            case Server.NBLS_GET_ARCHIVE_FILE_CONTENT: {
                CompletableFuture<Object> future = new CompletableFuture<>();
                try {
                    String uri = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                    FileObject file = Utils.fromUri(uri);
                    if (file != null && file.isData() && file.canRead()) {
                        future.complete(file.asText("UTF-8"));
                    }
                    future.complete(null);
                } catch (IOException ioe) {
                    future.completeExceptionally(ioe);
                }
                return future;
            }
            case Server.JAVA_GET_PROJECT_SOURCE_ROOTS: {
                String uri = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                String type = params.getArguments().size() > 1 ? ((JsonPrimitive) params.getArguments().get(1)).getAsString() : JavaProjectConstants.SOURCES_TYPE_JAVA;
                return getSourceRoots(uri, type).thenApply(roots -> {
                    return roots.stream().map(root -> Utils.toUri(root)).collect(Collectors.toList());
                });
            }
            case Server.JAVA_GET_PROJECT_CLASSPATH: {
                String uri = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                ClasspathInfo.PathKind kind = params.getArguments().size() > 1 ? ClasspathInfo.PathKind.valueOf(((JsonPrimitive) params.getArguments().get(1)).getAsString()) : ClasspathInfo.PathKind.COMPILE;
                boolean preferSources = params.getArguments().size() > 2 ? ((JsonPrimitive) params.getArguments().get(2)).getAsBoolean() : false;
                return getSourceRoots(uri, JavaProjectConstants.SOURCES_TYPE_JAVA).thenApply(roots -> {
                    HashSet<FileObject> cpRoots = new HashSet<>();
                    for(FileObject root : roots) {
                        for (FileObject cpRoot : ClasspathInfo.create(root).getClassPath(kind).getRoots()) {
                            FileObject[] srcRoots = preferSources ? SourceForBinaryQuery.findSourceRoots(cpRoot.toURL()).getRoots() : null;
                            if (srcRoots != null && srcRoots.length > 0) {
                                for (FileObject srcRoot : srcRoots) {
                                    cpRoots.add(srcRoot);
                                }
                            } else {
                                cpRoots.add(cpRoot);
                            }
                        }
                    }
                    return cpRoots.stream().map(fo -> Utils.toUri(fo)).collect(Collectors.toList());
                });
            }
            case Server.JAVA_GET_PROJECT_PACKAGES: {
                String uri = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                boolean srcOnly = params.getArguments().size() > 1 ? ((JsonPrimitive) params.getArguments().get(1)).getAsBoolean() : false;
                return getSourceRoots(uri, JavaProjectConstants.SOURCES_TYPE_JAVA).thenCompose(roots -> {
                    CompletableFuture<Object> future = new CompletableFuture<>();
                    JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY));
                    try {
                        js.runWhenScanFinished(controller -> {
                            HashSet<String> packages = new HashSet<>();
                            EnumSet<ClassIndex.SearchScope> scope = srcOnly ? EnumSet.of(ClassIndex.SearchScope.SOURCE) : EnumSet.allOf(ClassIndex.SearchScope.class);
                            for(FileObject root : roots) {
                                packages.addAll(ClasspathInfo.create(root).getClassIndex().getPackageNames("", false, scope));
                            }
                            ArrayList<String> ret = new ArrayList<>(packages);
                            Collections.sort(ret);
                            future.complete(ret);
                        }, true);
                    } catch (IOException ex) {
                        future.completeExceptionally(ex);
                    }
                    return future;
                });
            }
            case Server.NBLS_LOAD_WORKSPACE_TESTS: {
                String uri = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                FileObject file;
                try {
                    file = URLMapper.findFileObject(new URL(uri));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }
                if (file == null) {
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }
                long t = System.currentTimeMillis();
                return server.asyncOpenFileOwner(file).thenCompose(this::getTestRoots).thenCompose(testRoots -> {
                    LOG.log(Level.INFO, "Project {2}: {0} test roots opened in {1}ms", new Object[] { testRoots.size(), (System.currentTimeMillis() - t), file});
                    BiFunction<FileObject, Collection<TestMethodController.TestMethod>, Collection<TestSuiteInfo>> f = (fo, methods) -> {
                        String url = Utils.toUri(fo);
                        Map<String, TestSuiteInfo> suite2infos = new LinkedHashMap<>();
                        for (TestMethodController.TestMethod testMethod : methods) {
                            TestSuiteInfo suite = suite2infos.computeIfAbsent(testMethod.getTestClassName(), name -> {
                                Position pos = testMethod.getTestClassPosition() != null ? Utils.createPosition(fo, testMethod.getTestClassPosition().getOffset()) : null;
                                return new TestSuiteInfo(name, url, pos != null ? new Range(pos, pos) : null, TestSuiteInfo.State.Loaded, new ArrayList<>());
                            });
                            String id = testMethod.getTestClassName() + ':' + testMethod.method().getMethodName();
                            Position startPos = testMethod.start() != null ? Utils.createPosition(fo, testMethod.start().getOffset()) : null;
                            Position endPos = testMethod.end() != null ? Utils.createPosition(fo, testMethod.end().getOffset()) : startPos;
                            Range range = startPos != null ? new Range(startPos, endPos) : null;
                            suite.getTests().add(new TestSuiteInfo.TestCaseInfo(id, testMethod.method().getMethodName(), url, range, TestSuiteInfo.State.Loaded, null));
                        }
                        return suite2infos.values();
                    };
                    testMethodsListener.compareAndSet(null, (fo, methods) -> {
                        try {
                            for (TestSuiteInfo testSuiteInfo : f.apply(fo, methods)) {
                                client.notifyTestProgress(new TestProgressParams(Utils.toUri(fo), testSuiteInfo));
                            }
                        } catch (Exception e) {
                            Logger.getLogger(WorkspaceServiceImpl.class.getName()).severe(e.getMessage());
                            Exceptions.printStackTrace(e);
                            testMethodsListener.set(null);
                        }
                    });
                    if (openProjectsListener.compareAndSet(null, (evt) -> {
                        if ("openProjects".equals(evt.getPropertyName())) {
                            JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY));
                            try {
                                js.runWhenScanFinished(controller -> {
                                    List<Project> old = Arrays.asList((Project[]) evt.getOldValue());
                                    for (Project p : (Project[])evt.getNewValue()) {
                                        if (!old.contains(p)) {
                                            getTestRoots(p).thenAccept(tr -> {
                                                for (Entry<FileObject, Collection<TestMethodController.TestMethod>> entry : TestMethodFinder.findTestMethods(tr, testMethodsListener.get()).entrySet()) {
                                                    for (TestSuiteInfo tsi : f.apply(entry.getKey(), entry.getValue())) {
                                                        client.notifyTestProgress(new TestProgressParams(Utils.toUri(entry.getKey()), tsi));
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }, true);
                            } catch (IOException ex) {
                            }
                        }
                    })) {
                        OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(openProjectsListener.get(), OpenProjects.getDefault()));
                    }
                    CompletableFuture<Object> future = new CompletableFuture<>();
                    JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY));
                    try {
                        js.runWhenScanFinished(controller -> {
                            Map<FileObject, Collection<TestMethodController.TestMethod>> testMethods = TestMethodFinder.findTestMethods(testRoots, testMethodsListener.get());
                            Collection<TestSuiteInfo> suites = new ArrayList<>(testMethods.size());
                            for (Entry<FileObject, Collection<TestMethodController.TestMethod>> entry : testMethods.entrySet()) {
                                suites.addAll(f.apply(entry.getKey(), entry.getValue()));
                            }
                            future.complete(suites);
                        }, true);
                    } catch (IOException ex) {
                        future.completeExceptionally(ex);
                    }
                    return future;
                });
            }
            case Server.NBLS_RESOLVE_STACKTRACE_LOCATION: {
                CompletableFuture<Object> future = new CompletableFuture<>();
                try {
                    if (params.getArguments().size() >= 3) {
                        String uri = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                        String methodName = ((JsonPrimitive) params.getArguments().get(1)).getAsString();
                        String fileName = ((JsonPrimitive) params.getArguments().get(2)).getAsString();
                        FileObject fo = Utils.fromUri(uri);
                        if (fo != null) {
                            ClassPath classPath = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                                ClassPath.getClassPath(fo, ClassPath.EXECUTE),
                                ClassPath.getClassPath(fo, ClassPath.BOOT)
                            });
                            String name = fileName.substring(0, fileName.lastIndexOf('.'));
                            String packageName = methodName.substring(0, methodName.indexOf(name)).replace('.', '/');
                            String resourceName = packageName + name + ".class";
                            List<FileObject> resources = classPath.findAllResources(resourceName);
                            if (resources != null) {
                                for (FileObject resource : resources) {
                                    FileObject root = classPath.findOwnerRoot(resource);
                                    if (root != null) {
                                        URL url = URLMapper.findURL(root, URLMapper.INTERNAL);
                                        SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(url);
                                        FileObject[] rootz = res.getRoots();
                                        for (int i = 0; i < rootz.length; i++) {
                                            String path = packageName + fileName;
                                            FileObject sourceFo = rootz[i].getFileObject(path);
                                            if (sourceFo != null) {
                                                future.complete(Utils.toUri(sourceFo));
                                                return future;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    future.complete(null);
                } catch (IOException ex) {
                    future.completeExceptionally(ex);
                }
                return future;
            }
            case Server.JAVA_SUPER_IMPLEMENTATION:
                String uri = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                Position pos = gson.fromJson(gson.toJson(params.getArguments().get(1)), Position.class);
                return (CompletableFuture)((TextDocumentServiceImpl)server.getTextDocumentService()).superImplementations(uri, pos);
            case Server.NBLS_FIND_PROJECT_CONFIGURATIONS: {
                String fileUri = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                
                FileObject file;
                try {
                    file = URLMapper.findFileObject(new URL(fileUri));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }

                return findProjectConfigurations(file);
            }
            case Server.JAVA_FIND_DEBUG_ATTACH_CONFIGURATIONS: {
                return AttachConfigurations.findConnectors(client.getNbCodeCapabilities());
            }
            case Server.JAVA_FIND_DEBUG_PROCESS_TO_ATTACH: {
                return AttachConfigurations.findProcessAttachTo(client);
            }
            case Server.NATIVE_IMAGE_FIND_DEBUG_PROCESS_TO_ATTACH: {
                return AttachNativeConfigurations.findProcessAttachTo(client);
            }
            case Server.NBLS_PROJECT_CONFIGURATION_COMPLETION: {
                // We expect one, two or three arguments.
                // The first argument is always the URI of the launch.json file.
                // When not more arguments are provided, all available configurations ought to be provided.
                // When only a second argument is present, it's a map of the current attributes in a configuration,
                // and additional attributes valid in that particular configuration ought to be provided.
                // When a third argument is present, it's an attribute name whose possible values ought to be provided.
                List<Object> arguments = params.getArguments();
                Collection<? extends LaunchConfigurationCompletion> configurations = Lookup.getDefault()
                                                                                           .lookupAll(LaunchConfigurationCompletion.Factory.class)
                                                                                           .stream()
                                                                                           .map(f -> f.createLaunchConfigurationCompletion(client.getNbCodeCapabilities()))
                                                                                           .collect(Collectors.toList());
                List<CompletableFuture<List<CompletionItem>>> completionFutures;
                String configUri = ((JsonPrimitive) arguments.get(0)).getAsString();
                Supplier<CompletableFuture<Project>> projectSupplier = () -> {
                    FileObject file;
                    try {
                        file = URLMapper.findFileObject(new URL(configUri));
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                        return CompletableFuture.completedFuture(null);
                    }
                    return server.asyncOpenFileOwner(file);
                };
                switch (arguments.size()) {
                    case 1:
                        completionFutures = configurations.stream().map(c -> c.configurations(projectSupplier)).collect(Collectors.toList());
                        break;
                    case 2:
                        Map<String, Object> attributes = attributesMap((JsonObject) arguments.get(1));
                        completionFutures = configurations.stream().map(c -> c.attributes(projectSupplier, attributes)).collect(Collectors.toList());
                        break;
                    case 3:
                        attributes = attributesMap((JsonObject) arguments.get(1));
                        String attribute = ((JsonPrimitive) arguments.get(2)).getAsString();
                        completionFutures = configurations.stream().map(c -> c.attributeValues(projectSupplier, attributes, attribute)).collect(Collectors.toList());
                        break;
                    default:
                        StringBuilder classes = new StringBuilder();
                        for (int i = 0; i < arguments.size(); i++) {
                            classes.append(arguments.get(i).getClass().toString());
                        }
                        throw new IllegalStateException("Wrong arguments("+arguments.size()+"): " + arguments + ", classes = " + classes);  // NOI18N
                }
                CompletableFuture<List<CompletionItem>> joinedFuture = CompletableFuture.allOf(completionFutures.toArray(new CompletableFuture[0]))
                        .thenApply(avoid -> completionFutures.stream().flatMap(c -> c.join().stream()).collect(Collectors.toList()));
                return (CompletableFuture<Object>) (CompletableFuture<?>) joinedFuture;
            }
            case Server.NBLS_PROJECT_RESOLVE_PROJECT_PROBLEMS: {
                final CompletableFuture<Object> result = new CompletableFuture<>();
                List<Object> arguments = params.getArguments();
                if (!arguments.isEmpty()) {
                    String fileStr = ((JsonPrimitive) arguments.get(0)).getAsString();
                    FileObject file;
                    try {
                        file = URLMapper.findFileObject(URI.create(fileStr).toURL());
                    } catch (MalformedURLException ex) {
                        result.completeExceptionally(ex);
                        return result;
                    }
                    Project project = FileOwnerQuery.getOwner(file);
                    if (project != null) {
                        ProjectProblemsProvider ppp = project.getLookup().lookup(ProjectProblemsProvider.class);
                        if (ppp != null) {
                            Collection<? extends ProjectProblemsProvider.ProjectProblem> problems = ppp.getProblems();
                            if (!problems.isEmpty()) {
                                WORKER.post(() -> {
                                    List<Pair<ProjectProblemsProvider.ProjectProblem, Future<ProjectProblemsProvider.Result>>> resolvers = new LinkedList<>();
                                    for (ProjectProblemsProvider.ProjectProblem problem : ppp.getProblems()) {
                                        if (problem.isResolvable()) {
                                            resolvers.add(Pair.of(problem, problem.resolve()));
                                        } else {
                                            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(problem.getDescription(), NotifyDescriptor.Message.ERROR_MESSAGE));
                                        }
                                    }
                                    if (!resolvers.isEmpty()) {
                                        for (Pair<ProjectProblemsProvider.ProjectProblem, Future<ProjectProblemsProvider.Result>> resolver : resolvers) {
                                            try {
                                                if (!resolver.second().get().isResolved()) {
                                                    String message = resolver.second().get().getMessage();
                                                    if (message != null) {
                                                        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.Message.ERROR_MESSAGE));
                                                    }
                                                }
                                            } catch (ExecutionException ex) {
                                                result.completeExceptionally(ex.getCause());
                                            } catch (InterruptedException ex) {
                                                result.complete(false);
                                                break;
                                            }
                                        }
                                    }
                                    if (!result.isDone()) {
                                        result.complete(true);
                                    }
                                });
                            }
                        }
                    }
                } else {
                    result.completeExceptionally(new IllegalStateException("Expecting file URL as an argument to " + command));
                }
                return result;
            }
            case Server.NBLS_CLEAR_PROJECT_CACHES: {
                // politely clear project manager's cache of "no project" answers
                ProjectManager.getDefault().clearNonProjectCache();
                // impolitely clean the project-based traversal's cache, so any affiliation of intermediate folders will disappear
                ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                CompletableFuture<Boolean> result = new CompletableFuture<>();
                try {
                    Class queryImpl = Class.forName("org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation", true, loader); // NOI18N
                    Method resetMethod = queryImpl.getMethod("reset"); // NOI18N
                    resetMethod.invoke(null);
                    result.complete(true);
                } catch (ReflectiveOperationException ex) {
                    result.completeExceptionally(ex);
                }
                // and finally, let's refresh everything we had opened:
                for (FileObject f : server.getAcceptedWorkspaceFolders()) {
                    f.refresh();
                }
                for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                    p.getProjectDirectory().refresh();
                }
                return (CompletableFuture<Object>) (CompletableFuture<?>)result;
            }
            
            case Server.NBLS_PROJECT_INFO: {
                final CompletableFuture<Object> result = new CompletableFuture<>();
                List<Object> arguments = params.getArguments();
                if (arguments.size() < 1) {
                    result.completeExceptionally(new IllegalArgumentException("Expecting URL or URL[] as an argument to " + command));
                    return result;
                }
                Object o = arguments.get(0);
                URL[] locations = null;
                if (o instanceof JsonArray) {
                    List<URL> locs = new ArrayList<>();
                    JsonArray a = (JsonArray)o;
                    a.forEach((e) -> {
                        if (e instanceof JsonPrimitive) {
                            String s = ((JsonPrimitive)e).getAsString();
                            try {
                                locs.add(new URL(s));
                            } catch (MalformedURLException ex) {
                                throw new IllegalArgumentException("Illegal location: " + s);
                            }
                        }
                    });
                    locations = locs.toArray(new URL[0]);
                } else if (o instanceof JsonPrimitive) {
                    String s = ((JsonPrimitive)o).getAsString();
                    try {
                        locations = new URL[] { new URL(s) };
                    } catch (MalformedURLException ex) {
                        throw new IllegalArgumentException("Illegal location: " + s);
                    }
                }
                if (locations == null || locations.length == 0) {
                    result.completeExceptionally(new IllegalArgumentException("Expecting URL or URL[] as an argument to " + command));
                    return result;
                }
                boolean projectStructure = false;
                boolean actions = false;
                boolean recursive = false;
                
                if (arguments.size() > 1) {
                    Object a2 = arguments.get(1);
                    if (a2 instanceof JsonObject) {
                        JsonObject options = (JsonObject)a2;
                        projectStructure = getOption(options, "projectStructure", false); // NOI18N
                        actions = getOption(options, "actions", false); // NOI18N
                        recursive = getOption(options, "recursive", false); // NOI18N
                    }
                }
                return (CompletableFuture<Object>)(CompletableFuture<?>)new ProjectInfoWorker(locations, projectStructure, recursive, actions).process();
            }
            case Server.JAVA_ENABLE_PREVIEW: {
                String source = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                String newSourceLevel = params.getArguments().size() > 1 ? ((JsonPrimitive) params.getArguments().get(1)).getAsString()
                                                                         : null;
                FileObject file;
                try {
                    file = URLMapper.findFileObject(new URL(source));
                    if (file != null) {
                        for (Factory factory : Lookup.getDefault().lookupAll(Factory.class)) {
                            PreviewEnabler enabler = factory.enablerFor(file);
                            if (enabler != null) {
                                enabler.enablePreview(newSourceLevel);
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }
                return CompletableFuture.completedFuture(true);
            }
            case Server.NBLS_DOCUMENT_SYMBOLS: {
                List<DocumentSymbol> result = new ArrayList<>();
                try {
                    List<Object> arguments = params.getArguments();
                    String source = ((JsonPrimitive) arguments.get(0)).getAsString();
                    String query = arguments.size() > 1 ? ((JsonPrimitive)arguments.get(1)).getAsString() : "";
                    FileObject file = Utils.fromUri(source);
                    Document rawDoc = server.getOpenedDocuments().getDocument(source);
                    if (file != null && rawDoc instanceof StyledDocument) {
                        StyledDocument doc = (StyledDocument)rawDoc;
                        for (StructureProvider structureProvider : MimeLookup.getLookup(DocumentUtilities.getMimeType(doc)).lookupAll(StructureProvider.class)) {
                            if (structureProvider != null) {
                                List<StructureElement> structureElements = structureProvider.getStructure(doc);
                                if (!structureElements.isEmpty()) {
                                    for (StructureElement structureElement : structureElements) {
                                        if (structureElement.getName().startsWith(query)) {
                                            DocumentSymbol ds = TextDocumentServiceImpl.structureElement2DocumentSymbol(doc, structureElement);
                                            if (ds != null) {
                                                result.add(ds);
                                            }
                                        }
                                    }
                                };
                            }
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }
                return CompletableFuture.completedFuture(result);
            }
            case Server.NBLS_GET_DIAGNOSTICS: {
                    List<Object> arguments = params.getArguments();
                    String source = ((JsonPrimitive) arguments.get(0)).getAsString();
                    EnumSet<ErrorProvider.Kind> s;
                    if (arguments.size() > 1 && arguments.get(1) instanceof JsonArray) {
                        s = EnumSet.noneOf(ErrorProvider.Kind.class);
                        for (JsonElement jse : ((JsonArray)arguments.get(1))) {
                            if (jse instanceof JsonPrimitive) {
                                ErrorProvider.Kind k = ErrorProvider.Kind.valueOf(jse.getAsString());
                                s.add(k);
                            }
                        }
                    } else {
                        s = EnumSet.allOf(ErrorProvider.Kind.class);
                    }
                    return (CompletableFuture<Object>)(CompletableFuture)((TextDocumentServiceImpl)server.getTextDocumentService()).computeDiagnostics(source, s);
            }
            case Server.NBLS_GET_SERVER_DIRECTORIES: {
                JsonObject o = new JsonObject();
                o.addProperty("userdir", Places.getUserDirectory().toString());
                o.addProperty("dirs", System.getProperty("netbeans.dirs"));
                o.addProperty("extra.dirs", System.getProperty("netbeans.extra.dirs"));
                o.addProperty("cache", Places.getCacheDirectory().toString());
                o.addProperty("config", FileUtil.toFile(FileUtil.getConfigRoot()).toString());
                return CompletableFuture.completedFuture(o);
            }
            default:
                for (CodeActionsProvider codeActionsProvider : Lookup.getDefault().lookupAll(CodeActionsProvider.class)) {
                    if (codeActionsProvider.getCommands().contains(command)) {
                        return codeActionsProvider.processCommand(client, command, params.getArguments());
                    }
                }
        }
        throw new UnsupportedOperationException("Command not supported: " + params.getCommand());
    }
    
    private class ProjectInfoWorker {
        final URL[] locations;
        final boolean projectStructure;
        final boolean recursive;
        final boolean actions;
        
        Map<FileObject, LspProjectInfo> infos = new HashMap<>();
        Set<Project> toOpen = new HashSet<>();

        public ProjectInfoWorker(URL[] locations, boolean projectStructure, boolean recursive, boolean actions) {
            this.locations = locations;
            this.projectStructure = projectStructure;
            this.recursive = recursive;
            this.actions = actions;
        }

        public CompletableFuture<LspProjectInfo[]> process() {
            List<FileObject> files = new ArrayList<>();
            for (URL u : locations) {
                FileObject f = URLMapper.findFileObject(u);
                if (f != null) {
                    files.add(f);
                }
            }
            return server.asyncOpenSelectedProjects(files, false).thenCompose(this::processProjects);
        }
        
        LspProjectInfo fillProjectInfo(Project p) {
            if (p == null) {
                return null;
            }
            LspProjectInfo info = infos.get(p.getProjectDirectory());
            if (info != null) {
                return info;
            }
            info = new LspProjectInfo();
            
            ProjectInformation pi = ProjectUtils.getInformation(p);
            URL projectURL = URLMapper.findURL(p.getProjectDirectory(), URLMapper.EXTERNAL);
            if (projectURL != null) {
                try {
                    info.projectDirectory = projectURL.toURI();
                } catch (URISyntaxException ex) {
                    // should not happen
                }
            }
            info.name = pi.getName();
            info.displayName = pi.getDisplayName();
            
            // attempt to determine the project type
            ProjectManager.Result r = ProjectManager.getDefault().isProject2(p.getProjectDirectory());
            info.projectType = r.getProjectType();
            
            if (actions) {
                ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
                if (ap != null) {
                    info.projectActionNames = ap.getSupportedActions();
                }
            }

            if (projectStructure) {
                Set<Project> children = ProjectUtils.getContainedProjects(p, false);
                List<URI> subprojectDirs = new ArrayList<>();
                for (Project c : children) {
                    try {
                        subprojectDirs.add(URLMapper.findURL(c.getProjectDirectory(), URLMapper.EXTERNAL).toURI());
                    } catch (URISyntaxException ex) {
                        // should not happen
                    }
                }
                info.subprojects = subprojectDirs.toArray(new URI[0]);
                Project root = ProjectUtils.rootOf(p);
                if (root != null) {
                    try {
                        info.rootProject = URLMapper.findURL(root.getProjectDirectory(), URLMapper.EXTERNAL).toURI();
                    } catch (URISyntaxException ex) {
                        // should not happen
                    }
                }
                if (recursive) {
                    toOpen.addAll(children);
                }
            }
            infos.put(p.getProjectDirectory(), info);
            return info;
        }
        
        CompletableFuture<LspProjectInfo[]> processProjects(Project[] prjs) {
            for (Project p : prjs) {
                fillProjectInfo(p);
            }
            if (toOpen.isEmpty()) {
                return finalizeInfos();
            }
            List<FileObject> dirs = new ArrayList<>(toOpen.size());
            for (Project p : toOpen) {
                dirs.add(p.getProjectDirectory());
            }
            toOpen.clear();
            return server.asyncOpenSelectedProjects(dirs).thenCompose(this::processProjects);
        }
        
        CompletableFuture<LspProjectInfo[]> finalizeInfos() {
            List<LspProjectInfo> list = new ArrayList<>();
            for (URL u : locations) {
                FileObject f = URLMapper.findFileObject(u);
                Project owner = FileOwnerQuery.getOwner(f);
                if (owner != null) {
                    list.add(infos.remove(owner.getProjectDirectory()));
                } else {
                    list.add(null);
                }
            }
            list.addAll(infos.values());
            LspProjectInfo[] toArray = list.toArray(new LspProjectInfo[0]);
            return CompletableFuture.completedFuture(toArray);
        }
    }
    
    private static boolean getOption(JsonObject opts, String member, boolean def) {
        if (!opts.has(member)) {
            return def;
        }
        Object o = opts.get(member);
        if (!(o instanceof JsonPrimitive)) {
            return false;
        }
        return ((JsonPrimitive)o).getAsBoolean();
    }
    
    private final AtomicReference<BiConsumer<FileObject, Collection<TestMethodController.TestMethod>>> testMethodsListener = new AtomicReference<>();
    private final AtomicReference<PropertyChangeListener> openProjectsListener = new AtomicReference<>();

    private static Map<String, Object> attributesMap(JsonObject json) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            JsonPrimitive jp = (JsonPrimitive) entry.getValue();
            Object value = jp.isBoolean() ? jp.getAsBoolean() : jp.isNumber() ? jp.getAsNumber() : jp.getAsString();
            map.put(entry.getKey(), value);
        }
        return map;
    }
    
    private CompletableFuture<Object> findProjectConfigurations(FileObject ownedFile) {
        return server.asyncOpenFileOwner(ownedFile).thenApply(p -> {
            if (p == null) {
                return Collections.emptyList();
            }
            ProjectConfigurationProvider<ProjectConfiguration> provider = p.getLookup().lookup(ProjectConfigurationProvider.class);
            List<String> configDispNames = new ArrayList<>();
            if (provider != null) {
                for (ProjectConfiguration c : provider.getConfigurations()) {
                    configDispNames.add(c.getDisplayName());
                }
            }
            return configDispNames;
        });
    }

    private CompletableFuture<List<FileObject>> getSourceRoots(String uri, String type) {
        FileObject file;
        try {
            file = URLMapper.findFileObject(new URL(uri));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        if (file == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        return server.asyncOpenFileOwner(file).thenApply(project -> {
            if (project != null) {
                List<FileObject> roots = new ArrayList<>();
                for(SourceGroup sourceGroup : ProjectUtils.getSources(project).getSourceGroups(type)) {
                    roots.add(sourceGroup.getRootFolder());
                }
                return roots;
            }
            return Collections.emptyList();
        });
    }

    private static final String[] SOURCE_TYPES = {"java", "groovy"};

    private CompletableFuture<Set<FileObject>> getTestRoots(Project prj) {
        final Set<FileObject> testRoots = new HashSet<>();
        List<FileObject> contained = null;
        if (prj != null) {
            for (String sourceType : SOURCE_TYPES) {
                for (SourceGroup sg : ProjectUtils.getSources(prj).getSourceGroups(sourceType)) {
                    if (isTestGroup(sg)) {
                        testRoots.add(sg.getRootFolder());
                    }
                }
            }
            Set<Project> containedProjects = ProjectUtils.getContainedProjects(prj, true);
            if (containedProjects != null) {
                contained = containedProjects.stream().map(p -> p.getProjectDirectory()).collect(Collectors.toList());
            }
        }
        return server.asyncOpenSelectedProjects(contained).thenApply(projects -> {
            for (Project project : projects) {
                for (String sourceType : SOURCE_TYPES) {
                    for (SourceGroup sg : ProjectUtils.getSources(project).getSourceGroups(sourceType)) {
                        if (isTestGroup(sg)) {
                            testRoots.add(sg.getRootFolder());
                        }
                    }
                }
            }
            return testRoots;
        });
    }

    private boolean isTestGroup(SourceGroup sg) {
        return UnitTestForSourceQuery.findSources(sg.getRootFolder()).length > 0;
    }

    @Override
    public CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> symbol(WorkspaceSymbolParams params) {
        String query = params.getQuery();
        if (query.isEmpty()) {
            //cannot query "all":
            return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));
        }
        boolean exact = false;
        if (query.endsWith(" ")) {
            query = query.substring(0, query.length() - 1);
            exact = true;
        }
        String queryFin = query;
        boolean exactFin = exact;
        AtomicBoolean cancel = new AtomicBoolean();
        CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> result = new CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                cancel.set(mayInterruptIfRunning);
                return super.cancel(mayInterruptIfRunning);
            }
        };
        server.openedProjects().thenAccept(openedProjects -> {
            if (openedProjects.length < 1) {
                result.complete(Either.forLeft(Collections.emptyList()));
            } else {
                ClasspathInfo cpInfo = ClasspathInfo.create(openedProjects[0].getProjectDirectory());
                JavaSource js = JavaSource.create(cpInfo);
                if (js == null) {
                    result.complete(Either.forLeft(Collections.emptyList()));
                } else {
                    try {
                        js.runWhenScanFinished(cc -> {
                            WORKER.post(() -> {
                                try {
                                    List<WorkspaceSymbol> symbols = new ArrayList<>();
                                    SearchType searchType = getSearchType(queryFin, exactFin, false, null, null);

                                    // CSL Part
                                    Collection<? extends IndexSearcher> providers = Lookup.getDefault().lookupAll(IndexSearcher.class);
                                    Set<? extends IndexSearcher.Descriptor> descriptors;
                                    for (Project project : openedProjects) {
                                        if (!providers.isEmpty()) {
                                            for (IndexSearcher provider : providers) {
                                                descriptors = provider.getSymbols(project, queryFin, Utils.searchType2QueryKind(searchType), null);
                                                for (IndexSearcher.Descriptor desc : descriptors) {
                                                    FileObject fo = desc.getFileObject();
                                                    org.netbeans.modules.csl.api.ElementHandle element = desc.getElement();
                                                    if (fo != null) {
                                                        Position pos = Utils.createPosition(fo, desc.getOffset());
                                                        WorkspaceSymbol symbol = new WorkspaceSymbol(
                                                                desc.getSimpleName(),
                                                                Utils.cslElementKind2SymbolKind(element.getKind()),
                                                                Either.forLeft(new Location(Utils.toUri(fo), new Range(pos, pos))),
                                                                desc.getContextName());
                                                        symbols.add(symbol);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Java part
                                    if (client.getNbCodeCapabilities().wantsJavaSupport()) {
                                        JavaSymbolProvider.ResultHandler symbolHandler = new JavaSymbolProvider.ResultHandler() {
                                            @Override
                                            public void setHighlightText(String text) {
                                            }

                                            private Map<ElementHandle<TypeElement>, List<String>> type2Idents;

                                            @Override
                                            public void runRoot(FileObject root, ClassIndexImpl ci, Exec exec) throws IOException, InterruptedException {
                                                ClasspathInfo cpInfo = ClasspathInfo.create(root);
                                                try {
                                                    type2Idents = new HashMap<>();
                                                    exec.run();
                                                    Map<FileObject, Map<ElementHandle<TypeElement>, List<String>>> sources = new HashMap<>();
                                                    for (Entry<ElementHandle<TypeElement>, List<String>> e : type2Idents.entrySet()) {
                                                        FileObject sourceFile = SourceUtils.getFile(e.getKey(), cpInfo);
                                                        sources.computeIfAbsent(sourceFile, s -> new HashMap<>())
                                                               .put(e.getKey(), e.getValue());
                                                    }
                                                    if (!sources.isEmpty()) {
                                                        JavaSource.create(cpInfo, sources.keySet())
                                                                .runUserActionTask(cc -> {
                                                                    if (Phase.ELEMENTS_RESOLVED.compareTo(cc.toPhase(Phase.ELEMENTS_RESOLVED))> 0) {
                                                                        return ;
                                                                    }
                                                                    for (Entry<ElementHandle<TypeElement>, List<String>> e : sources.get(cc.getFileObject()).entrySet()) {
                                                                        TypeElement te = e.getKey().resolve(cc);

                                                                        if (te == null) {
                                                                            //cannot resolve
                                                                            continue;
                                                                        }

                                                                        for (String ident : e.getValue()) {
                                                                            if (ident.equals(getSimpleName(te, null, false))) {
                                                                                TreePath path = cc.getTrees().getPath(te);

                                                                                if (path != null) {
                                                                                    final String symbolName = te.getSimpleName().toString();
                                                                                    final ElementKind kind = te.getKind();
                                                                                    if (!kind.isClass() && !kind.isInterface()) {
                                                                                        WorkspaceSymbol symbol = new WorkspaceSymbol(symbolName, Utils.elementKind2SymbolKind(kind), Either.forLeft(tree2Location(cc, path)), te.getQualifiedName().toString());
                                                                                        symbols.add(symbol);
                                                                                    }
                                                                                }
                                                                            }
                                                                            for (Element ne : te.getEnclosedElements()) {
                                                                                if (ident.equals(getSimpleName(ne, te, false))) {
                                                                                    TreePath path = cc.getTrees().getPath(ne);

                                                                                    if (path != null) {
                                                                                        final Pair<String,String> name = JavaSymbolProvider.getDisplayName(ne, te);
                                                                                        final String symbolName = name.first() + (name.second() != null ? name.second() : "");
                                                                                        final ElementKind kind = ne.getKind();
                                                                                        if (!kind.isClass() && !kind.isInterface()) {
                                                                                            WorkspaceSymbol symbol = new WorkspaceSymbol(symbolName, Utils.elementKind2SymbolKind(kind), Either.forLeft(tree2Location(cc, path)), te.getQualifiedName().toString());
                                                                                            symbols.add(symbol);
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }, true);
                                                    }
                                                    //TODO: handle exceptions
                                                } finally {
                                                    type2Idents = null;
                                                }
                                            }

                                            @Override
                                            public void handleResult(ElementHandle<TypeElement> owner, String ident, boolean caseSensitive) {
                                                type2Idents.computeIfAbsent(owner, s -> new ArrayList<>()).add(ident);
                                            }
                                        };
                                        JavaSymbolProvider.doComputeSymbols(searchType, queryFin, symbolHandler, true, cancel);
                                        List<Pair<ElementHandle<TypeElement>, FileObject>> pairs = new ArrayList<>();
                                        JavaTypeProvider.ResultHandler<Pair<ElementHandle<TypeElement>, FileObject>> typeHandler = new JavaTypeProvider.ResultHandler<Pair<ElementHandle<TypeElement>, FileObject>>() {
                                            private FileObject root;

                                            @Override
                                            public void setMessage(String msg) {
                                            }

                                            @Override
                                            public void setHighlightText(String text) {
                                            }

                                            @Override
                                            public void pendingResult() {
                                            }

                                            @Override
                                            public void runRoot(FileObject root, JavaTypeProvider.ResultHandler.Exec exec) throws IOException, InterruptedException {
                                                this.root = root;
                                                try {
                                                    exec.run();
                                                } finally {
                                                    this.root = null;
                                                }
                                            }

                                            @Override
                                            public Pair<ElementHandle<TypeElement>, FileObject> create(JavaTypeProvider.CacheItem cacheItem, ElementHandle<TypeElement> handle, String simpleName, String relativePath) {
                                                return Pair.of(handle, this.root);
                                            }

                                            @Override
                                            public void addResult(List<? extends Pair<ElementHandle<TypeElement>, FileObject>> types) {
                                                pairs.addAll(types);
                                            }
                                        };
                                        JavaTypeProvider.doComputeTypes(searchType, queryFin, typeHandler, cancel);
                                        for (Pair<ElementHandle<TypeElement>, FileObject> pair : pairs) {
                                            ElementHandle<TypeElement> handle = pair.first();
                                            String fqn = handle.getQualifiedName();
                                            int idx = fqn.lastIndexOf('.');
                                            String simpleName = idx < 0 ? fqn : fqn.substring(idx + 1);
                                            String contextName = idx < 0 ? null : fqn.substring(0, idx);
                                            String uri = (pair.second().toURI().toString()) + '?' + handle.getKind().name() + '#' + handle.getBinaryName();
                                            WorkspaceSymbol symbol = new WorkspaceSymbol(simpleName, Utils.elementKind2SymbolKind(handle.getKind()), Either.forRight(new WorkspaceSymbolLocation(uri)), contextName);
                                            symbols.add(symbol);
                                        }
                                    }
                                    result.complete(Either.forRight(symbols));
                                } catch (Throwable t) {
                                    result.completeExceptionally(t);
                                }
                            });
                        }, true);
                    } catch (IOException ioe) {
                        result.completeExceptionally(ioe);
                    }
                }
            }
        }).exceptionally(ex -> {
            result.completeExceptionally(ex);
            return null;
        });
        return result;
    }

    @Override
    public CompletableFuture<WorkspaceSymbol> resolveWorkspaceSymbol(WorkspaceSymbol workspaceSymbol) {
        String sourceUri = workspaceSymbol.getLocation().isLeft() ? workspaceSymbol.getLocation().getLeft().getUri() : workspaceSymbol.getLocation().getRight().getUri();
        CompletableFuture<WorkspaceSymbol> result = new CompletableFuture<>();
        try {
            int qIdx = sourceUri.lastIndexOf('?');
            int hIdx = sourceUri.lastIndexOf('#');
            if (qIdx < 0 || hIdx < 0 || hIdx <= qIdx) {
                result.complete(workspaceSymbol);
            } else {
                String rootUri = sourceUri.substring(0, qIdx);
                FileObject root = Utils.fromUri(rootUri);
                if (root == null) {
                    throw new IllegalStateException("Unable to find root: " + rootUri);
                }
                ElementHandle typeHandle = ElementHandleAccessor.getInstance().create(ElementKind.valueOf(sourceUri.substring(qIdx + 1, hIdx)), sourceUri.substring(hIdx + 1));
                CompletableFuture<ElementOpen.Location> location = ElementOpen.getLocation(ClasspathInfo.create(root), typeHandle, typeHandle.getQualifiedName().replace('.', '/') + ".class");
                location.exceptionally(ex -> {
                    result.completeExceptionally(ex);
                    return null;
                }).thenAccept(loc -> {
                    if (loc != null) {
                        ShowDocumentParams sdp = new ShowDocumentParams(Utils.toUri(loc.getFileObject()));
                        Position position = Utils.createPosition(loc.getFileObject(), loc.getStartOffset());
                        sdp.setSelection(new Range(position, position));
                        client.showDocument(sdp).thenAccept(res -> {
                            if (res.isSuccess()) {
                                result.complete(null);
                            } else {
                                result.completeExceptionally(new IllegalStateException("Cannot open source for: " + typeHandle.getQualifiedName()));
                            }
                        });
                    } else if (!result.isCompletedExceptionally()) {
                        result.completeExceptionally(new IllegalStateException("Cannot find source for: " + typeHandle.getQualifiedName()));
                    }
                });
            }
        } catch (Throwable t) {
            result.completeExceptionally(t);
        }
        return result;
    }

    private Location tree2Location(CompilationInfo info, TreePath path) {
        return new Location(Utils.toUri(info.getFileObject()),
                            Utils.treeRange(info, path.getLeaf()));
    }

    //from jumpto.Utils:
    public static int containsWildCard( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( text.charAt( i ) == '?' || text.charAt( i ) == '*' ) { // NOI18N
                return i;
            }
        }
        return -1;
    }

    public static boolean isAllUpper( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( !Character.isUpperCase( text.charAt( i ) ) ) {
                return false;
            }
        }

        return true;
    }

    public static SearchType getSearchType(
            @NonNull final String text,
            final boolean exact,
            final boolean isCaseSensitive,
            @NullAllowed final String camelCaseSeparator,
            @NullAllowed final String camelCasePart) {
        int wildcard = containsWildCard(text);
        if (exact) {
            //nameKind = isCaseSensitive ? SearchType.EXACT_NAME : SearchType.CASE_INSENSITIVE_EXACT_NAME;
            return SearchType.EXACT_NAME;
        } else if (wildcard != -1) {
            return isCaseSensitive ? SearchType.REGEXP : SearchType.CASE_INSENSITIVE_REGEXP;
        } else if ((isAllUpper(text) && text.length() > 1) || Queries.isCamelCase(text, camelCaseSeparator, camelCasePart)) {
            return isCaseSensitive ? SearchType.CAMEL_CASE : SearchType.CASE_INSENSITIVE_CAMEL_CASE;
        } else {
            return isCaseSensitive ? SearchType.PREFIX : SearchType.CASE_INSENSITIVE_PREFIX;
        }
    }

    //TODO: from AsyncJavaSymbolDescriptor:
    private static final String INIT = "<init>"; //NOI18N
    @NonNull
    private static String getSimpleName (
            @NonNull final Element element,
            @NullAllowed final Element enclosingElement,
            final boolean caseSensitive) {
        String result = element.getSimpleName().toString();
        if (enclosingElement != null && INIT.equals(result)) {
            result = enclosingElement.getSimpleName().toString();
        }
        if (!caseSensitive) {
            result = result.toLowerCase();
        }
        return result;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        String fullConfigPrefix = client.getNbCodeCapabilities().getConfigurationPrefix();
        String configPrefix = fullConfigPrefix.substring(0, fullConfigPrefix.length() - 1);
        server.openedProjects().thenAccept(projects -> {
            ((TextDocumentServiceImpl)server.getTextDocumentService()).updateJavaHintPreferences(((JsonObject) params.getSettings()).getAsJsonObject(configPrefix).getAsJsonObject(NETBEANS_JAVA_HINTS));
            if (projects != null && projects.length > 0) {
                updateJavaFormatPreferences(projects[0].getProjectDirectory(), ((JsonObject) params.getSettings()).getAsJsonObject(configPrefix).getAsJsonObject("format"));
                updateJavaImportPreferences(projects[0].getProjectDirectory(), ((JsonObject) params.getSettings()).getAsJsonObject(configPrefix).getAsJsonObject("java").getAsJsonObject("imports"));
            }
        });
        String fullAltConfigPrefix = client.getNbCodeCapabilities().getAltConfigurationPrefix();
        String altConfigPrefix = fullAltConfigPrefix.substring(0, fullAltConfigPrefix.length() - 1);
        boolean modified = false;
        String newVMOptions = "";
        JsonObject javaPlus = ((JsonObject) params.getSettings()).getAsJsonObject(altConfigPrefix);
        if (javaPlus != null) {
            JsonObject runConfig = javaPlus.getAsJsonObject("runConfig");
            if (runConfig != null) {
                newVMOptions = runConfig.getAsJsonPrimitive("vmOptions").getAsString();
            }
        }
        for (SingleFileOptionsQueryImpl query : Lookup.getDefault().lookupAll(SingleFileOptionsQueryImpl.class)) {
            modified |= query.setConfiguration(client, newVMOptions);
        }
        if (modified) {
            ((TextDocumentServiceImpl)server.getTextDocumentService()).reRunDiagnostics();
        }
    }

    void updateJavaFormatPreferences(FileObject fo, JsonObject configuration) {
        if (configuration != null && client.getNbCodeCapabilities().wantsJavaSupport()) {
            NbPreferences.Provider provider = Lookup.getDefault().lookup(NbPreferences.Provider.class);
            Preferences prefs = provider != null ? provider.preferencesRoot().node("de/funfried/netbeans/plugins/externalcodeformatter") : null;
            JsonPrimitive formatterPrimitive = configuration.getAsJsonPrimitive("codeFormatter");
            String formatter = formatterPrimitive != null ? formatterPrimitive.getAsString() : null;
            JsonPrimitive pathPrimitive = configuration.getAsJsonPrimitive("settingsPath");
            String path = pathPrimitive != null ? pathPrimitive.getAsString() : null;
            if (formatter == null || "NetBeans".equals(formatter)) {
                if (prefs != null) {
                    prefs.put("enabledFormatter.JAVA", "netbeans-formatter");
                }
                Path p = path != null ? Paths.get(path) : null;
                File file = p != null ? p.toFile() : null;
                try {
                    if (file != null && file.exists() && file.canRead() && file.getName().endsWith(".zip")) {
                        OptionsExportModel.get().doImport(file);
                    } else {
                        OptionsExportModel.get().clean();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (prefs != null) {
                prefs.put("enabledFormatter.JAVA", formatter.toLowerCase(Locale.ENGLISH).concat("-java-formatter"));
                if (path != null) {
                    prefs.put(formatter.toLowerCase(Locale.ENGLISH).concat("FormatterLocation"), path);
                }
            }
        }
    }

    void updateJavaImportPreferences(FileObject fo, JsonObject configuration) {
        Preferences prefs = CodeStylePreferences.get(fo, "text/x-java").getPreferences();
        if (prefs != null && configuration != null) {
            prefs.put("importGroupsOrder", String.join(";", gson.fromJson(configuration.get("groups"), String[].class)));
            prefs.putBoolean("allowConvertToStarImport", true);
            prefs.putInt("countForUsingStarImport", configuration.getAsJsonPrimitive("countForUsingStarImport").getAsInt());
            prefs.putBoolean("allowConvertToStaticStarImport", true);
            prefs.putInt("countForUsingStaticStarImport", configuration.getAsJsonPrimitive("countForUsingStaticStarImport").getAsInt());
        }
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "MSG_ProjectFolderInitializationComplete=Completed initialization of project {0}",
        "# {0} - some project name",
        "# {1} - number of other projects loaded",
        "MSG_ProjectFolderInitializationComplete2=Completed initialization of {0} and {1} other projectss"
    })
    @Override
    public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
        // the client > server notification stream is sequential
        List<FileObject> newWorkspaceFolders = new ArrayList<>(this.clientWorkspaceFolders);
        List<FileObject> refreshProjectFolders = new ArrayList<>();
        for (WorkspaceFolder wkspFolder : params.getEvent().getAdded()) {
            String uri = wkspFolder.getUri();
            try {
                FileObject f = Utils.fromUri(uri);
                if (f != null) {
                    refreshProjectFolders.add(f);
                    // avoid duplicates
                    if (!newWorkspaceFolders.contains(f)) {
                        LOG.log(Level.FINE, "Adding client workspace folder {0}", f);
                        newWorkspaceFolders.add(f);
                    }
                }
            } catch (MalformedURLException ex) {
                // expected, perhaps some client-specific URL scheme ?
                LOG.fine("Workspace folder URI could not be converted into fileobject: {0}");
            }
        }
        
        if (params.getEvent().getRemoved() != null) {
            for (WorkspaceFolder wsf : params.getEvent().getRemoved()) {
                String uri = wsf.getUri();
                try {
                    FileObject f = Utils.fromUri(uri);
                    if (f != null) {
                        LOG.log(Level.FINE, "Removing client workspace folder {0}", f);
                        newWorkspaceFolders.remove(f);
                    }
                } catch (MalformedURLException ex) {
                    // was never added 
                }
            }
        }
        // the client > server notification stream is sequential; no need to sync
        this.clientWorkspaceFolders = newWorkspaceFolders;
        
        if (!refreshProjectFolders.isEmpty()) {
            server.asyncOpenSelectedProjects(refreshProjectFolders, true).thenAccept((projects) -> {
                // report initialization of a project / projects
                String msg;
                Project[] opened = Arrays.asList(projects).stream().filter(Objects::nonNull).toArray(Project[]::new);
                if (opened.length == 0) {
                    // this should happen immediately
                    return;
                } 
                ProjectInformation pi = ProjectUtils.getInformation(opened[0]);
                String n = pi.getDisplayName();
                if (n == null) {
                    n = pi.getName();
                }
                if (n == null) {
                    n = opened[0].getProjectDirectory().getName();
                }
                if (opened.length == 1) {
                    msg = Bundle.MSG_ProjectFolderInitializationComplete(n);
                } else {
                    msg = Bundle.MSG_ProjectFolderInitializationComplete2(n, opened.length);
                }
                StatusDisplayer.getDefault().setStatusText(msg, StatusDisplayer.IMPORTANCE_ANNOTATION);
            });
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams arg0) {
        //TODO: not watching files for now
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = (NbCodeLanguageClient)client;
    }

    private static final class CommandProgress extends ActionProgress {

        private final CompletableFuture<Object> commandFinished = new CompletableFuture<>();
        private int running;
        private int success;
        private int failure;

        @Override
        protected synchronized void started() {
            running++;
            notify();
        }

        @Override
        public synchronized void finished(boolean ok) {
            if (ok) {
                success++;
            } else {
                failure++;
            }
            checkStatus();
        }

        final synchronized void checkStatus() {
            if (running == 0) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                }
            }
            if (running <= success + failure) {
                commandFinished.complete(failure == 0);
            }
        }

        CompletableFuture<Object> getFinishFuture() {
            return commandFinished;
        }
    }
}
