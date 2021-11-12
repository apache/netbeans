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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodFinder;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.debugging.attach.AttachConfigurations;
import org.netbeans.modules.java.lsp.server.debugging.attach.AttachNativeConfigurations;
import org.netbeans.modules.java.source.ui.JavaSymbolProvider;
import org.netbeans.modules.java.source.ui.JavaTypeProvider;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public final class WorkspaceServiceImpl implements WorkspaceService, LanguageClientAware {

    private static final RequestProcessor WORKER = new RequestProcessor(WorkspaceServiceImpl.class.getName(), 1, false, false);

    private final Gson gson = new Gson();
    private final LspServerState server;
    private NbCodeLanguageClient client;

    WorkspaceServiceImpl(LspServerState server) {
        this.server = server;
    }

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        String command = params.getCommand();
        switch (command) {
            case Server.GRAALVM_PAUSE_SCRIPT:
                ActionsManager am = DebuggerManager.getDebuggerManager().getCurrentEngine().getActionsManager();
                am.doAction("pauseInGraalScript");
                return CompletableFuture.completedFuture(true);
            case Server.JAVA_NEW_FROM_TEMPLATE:
                return LspTemplateUI.createFromTemplate("Templates", client, params);
            case Server.JAVA_NEW_PROJECT:
                return LspTemplateUI.createProject("Templates/Project", client, params);
            case Server.JAVA_BUILD_WORKSPACE: {
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
            case Server.JAVA_LOAD_WORKSPACE_TESTS: {
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
                return server.asyncOpenFileOwner(file).thenCompose(this::getTestRoots).thenCompose(testRoots -> {
                    CompletableFuture<Object> future = new CompletableFuture<>();
                    JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY));
                    try {
                        js.runWhenScanFinished(controller -> {
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
                                Logger.getLogger(WorkspaceServiceImpl.class.getName()).info("FileObject reindexed: " + fo.getPath());
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
                            Logger.getLogger(WorkspaceServiceImpl.class.getName()).info("Attaching listener: " + testMethodsListener.get());
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
            case Server.JAVA_SUPER_IMPLEMENTATION:
                String uri = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                Position pos = gson.fromJson(gson.toJson(params.getArguments().get(1)), Position.class);
                return (CompletableFuture)((TextDocumentServiceImpl)server.getTextDocumentService()).superImplementations(uri, pos);
                
            case Server.JAVA_FIND_PROJECT_CONFIGURATIONS: {
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
                return AttachConfigurations.findConnectors();
            }
            case Server.JAVA_FIND_DEBUG_PROCESS_TO_ATTACH: {
                return AttachConfigurations.findProcessAttachTo(client);
            }
            case Server.NATIVE_IMAGE_FIND_DEBUG_PROCESS_TO_ATTACH: {
                return AttachNativeConfigurations.findProcessAttachTo(client);
            }
            case Server.JAVA_PROJECT_CONFIGURATION_COMPLETION: {
                // We expect one, two or three arguments.
                // The first argument is always the URI of the launch.json file.
                // When not more arguments are provided, all available configurations ought to be provided.
                // When only a second argument is present, it's a map of the current attributes in a configuration,
                // and additional attributes valid in that particular configuration ought to be provided.
                // When a third argument is present, it's an attribute name whose possible values ought to be provided.
                List<Object> arguments = params.getArguments();
                Collection<? extends LaunchConfigurationCompletion> configurations = Lookup.getDefault().lookupAll(LaunchConfigurationCompletion.class);
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
            default:
                for (CodeActionsProvider codeActionsProvider : Lookup.getDefault().lookupAll(CodeActionsProvider.class)) {
                    if (codeActionsProvider.getCommands().contains(command)) {
                        return codeActionsProvider.processCommand(client, command, params.getArguments());
                    }
                }
        }
        throw new UnsupportedOperationException("Command not supported: " + params.getCommand());
    }

    private final AtomicReference<BiConsumer<FileObject, Collection<TestMethodController.TestMethod>>> testMethodsListener = new AtomicReference<>();

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
                return CompletableFuture.completedFuture(Collections.emptyList());
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
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        String query = params.getQuery();
        if (query.isEmpty()) {
            //cannot query "all":
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        System.err.println("query=" + query);
        boolean exact = false;
        if (query.endsWith(" ")) {
            query = query.substring(0, query.length() - 1);
            exact = true;
        }
        String queryFin = query;
        boolean exactFin = exact;
        AtomicBoolean cancel = new AtomicBoolean();
        CompletableFuture<List<? extends SymbolInformation>> result = new CompletableFuture<List<? extends SymbolInformation>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                cancel.set(mayInterruptIfRunning);
                return super.cancel(mayInterruptIfRunning);
            }
        };
        WORKER.post(() -> {
            try {
                List<SymbolInformation> symbols = new ArrayList<>();
                SearchType searchType = getSearchType(queryFin, exactFin, false, null, null);
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
                                                                SymbolInformation symbol = new SymbolInformation(symbolName, Utils.elementKind2SymbolKind(kind), tree2Location(cc, path), te.getQualifiedName().toString());
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
                                                                    SymbolInformation symbol = new SymbolInformation(symbolName, Utils.elementKind2SymbolKind(kind), tree2Location(cc, path), te.getQualifiedName().toString());
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
                List<Pair<ElementHandle<TypeElement>, ClasspathInfo>> pairs = new ArrayList<>();
                JavaTypeProvider.ResultHandler<Pair<ElementHandle<TypeElement>, ClasspathInfo>> typeHandler = new JavaTypeProvider.ResultHandler<Pair<ElementHandle<TypeElement>, ClasspathInfo>>() {
                    private ClasspathInfo cpInfo;

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
                        cpInfo = ClasspathInfo.create(root);
                        try {
                            exec.run();
                        } finally {
                            cpInfo = null;
                        }
                    }

                    @Override
                    public Pair<ElementHandle<TypeElement>, ClasspathInfo> create(JavaTypeProvider.CacheItem cacheItem, ElementHandle<TypeElement> handle, String simpleName, String relativePath) {
                        return Pair.of(handle, cpInfo);
                    }

                    @Override
                    public void addResult(List<? extends Pair<ElementHandle<TypeElement>, ClasspathInfo>> types) {
                        pairs.addAll(types);
                    }
                };
                JavaTypeProvider.doComputeTypes(searchType, queryFin, typeHandler, cancel);
                Map<CompletableFuture<ElementOpen.Location>, ElementHandle<TypeElement>> location2Handles = new HashMap<>();
                CompletableFuture<ElementOpen.Location>[] futures = pairs.stream().map(pair -> {
                    CompletableFuture<ElementOpen.Location> future = ElementOpen.getLocation(pair.second(), pair.first(), pair.first().getQualifiedName().replace('.', '/') + ".class");
                    location2Handles.put(future, pair.first());
                    return future;
                }).toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(futures).thenRun(() -> {
                    for (CompletableFuture<ElementOpen.Location> future : futures) {
                        ElementOpen.Location loc = future.getNow(null);
                        ElementHandle<TypeElement> handle = location2Handles.get(future);
                        if (loc != null && handle != null) {
                            FileObject fo = loc.getFileObject();
                            Location location = new Location(Utils.toUri(fo), new Range(Utils.createPosition(fo, loc.getStartOffset()), Utils.createPosition(fo, loc.getEndOffset())));
                            String fqn = handle.getQualifiedName();
                            int idx = fqn.lastIndexOf('.');
                            String simpleName = idx < 0 ? fqn : fqn.substring(idx + 1);
                            String contextName = idx < 0 ? null : fqn.substring(0, idx);
                            SymbolInformation symbol = new SymbolInformation(simpleName, Utils.elementKind2SymbolKind(handle.getKind()), location, contextName);
                            symbols.add(symbol);
                        }
                    }
                    Collections.sort(symbols, (i1, i2) -> i1.getName().compareToIgnoreCase(i2.getName()));
                    result.complete(symbols);
                });
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
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
        server.openedProjects().thenAccept(projects -> {
            if (projects != null && projects.length > 0) {
                updateJavaImportPreferences(projects[0].getProjectDirectory(), ((JsonObject) params.getSettings()).getAsJsonObject("netbeans").getAsJsonObject("java").getAsJsonObject("imports"));
            }
        });
    }

    void updateJavaImportPreferences(FileObject fo, JsonObject configuration) {
        Preferences prefs = CodeStylePreferences.get(fo, "text/x-java").getPreferences();
        if (prefs != null) {
            prefs.put("importGroupsOrder", String.join(";", gson.fromJson(configuration.get("groups"), String[].class)));
            prefs.putBoolean("allowConvertToStarImport", true);
            prefs.putInt("countForUsingStarImport", configuration.getAsJsonPrimitive("countForUsingStarImport").getAsInt());
            prefs.putBoolean("allowConvertToStaticStarImport", true);
            prefs.putInt("countForUsingStaticStarImport", configuration.getAsJsonPrimitive("countForUsingStaticStarImport").getAsInt());
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

        private final CompletableFuture<Object> commandFinished = new CompletableFuture<>();;
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

        synchronized final void checkStatus() {
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
