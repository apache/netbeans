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
import com.google.gson.JsonPrimitive;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.source.ui.JavaSymbolProvider;
import org.netbeans.modules.java.source.ui.JavaSymbolProvider.ResultHandler;
import org.netbeans.modules.java.source.ui.JavaSymbolProvider.ResultHandler.Exec;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
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
    private final LanguageServer server;
    private NbCodeLanguageClient client;

    public WorkspaceServiceImpl(LanguageServer server) {
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
            case Server.JAVA_BUILD_WORKSPACE: {
                final CommandProgress progressOfCompilation = new CommandProgress();
                final Lookup ctx = Lookups.singleton(progressOfCompilation);
                for (Project prj : OpenProjects.getDefault().getOpenProjects()) {
                    ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
                    if (ap != null && ap.isActionEnabled(ActionProvider.COMMAND_BUILD, Lookup.EMPTY)) {
                        ap.invokeAction(ActionProvider.COMMAND_REBUILD, ctx);
                    }
                }
                progressOfCompilation.checkStatus();
                return progressOfCompilation.getFinishFuture();
            }
            case Server.JAVA_SUPER_IMPLEMENTATION:
                String uri = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                Position pos = gson.fromJson(gson.toJson(params.getArguments().get(1)), Position.class);
                return (CompletableFuture)((TextDocumentServiceImpl)server.getTextDocumentService()).superImplementation(uri, pos);
            case Server.JAVA_TEST_SINGLE_METHOD:
                CommandProgress progressOfCommand = new CommandProgress();
                String uriStr = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                FileObject file;
                try {
                    file = URLMapper.findFileObject(new URL(uriStr));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                    return CompletableFuture.completedFuture(true);
                }
                String methodName = ((JsonPrimitive) params.getArguments().get(1)).getAsString();
                SingleMethod method = new SingleMethod(file, methodName);
                runSingleMethodCommand(method, SingleMethod.COMMAND_RUN_SINGLE_METHOD, progressOfCommand);
                progressOfCommand.checkStatus();
                return progressOfCommand.getFinishFuture();
            case Server.JAVA_RUN_MAIN_METHOD:
                progressOfCommand = new CommandProgress();
                uriStr = ((JsonPrimitive) params.getArguments().get(0)).getAsString();
                try {
                    file = URLMapper.findFileObject(new URL(uriStr));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                    return CompletableFuture.completedFuture(true);
                }
                runSingleFile(file, ActionProvider.COMMAND_RUN_SINGLE, progressOfCommand);
                progressOfCommand.checkStatus();
                return progressOfCommand.getFinishFuture();
            default:
                for (CodeGenerator codeGenerator : Lookup.getDefault().lookupAll(CodeGenerator.class)) {
                    if (codeGenerator.getCommands().contains(command)) {
                        return codeGenerator.processCommand(client, command, params.getArguments());
                    }
                }
        }
        throw new UnsupportedOperationException("Command not supported: " + params.getCommand());
    }

    @NbBundle.Messages("No_Method_Found=No method found")
    private void runSingleMethodCommand(SingleMethod singleMethod, String command, CommandProgress progressOfCommand) {
        if (singleMethod == null) {
            StatusDisplayer.getDefault().setStatusText(Bundle.No_Method_Found());
            progressOfCommand.getFinishFuture().complete(true);
        } else {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    Project owner = FileOwnerQuery.getOwner(singleMethod.getFile());
                    if (owner != null) {
                        ActionProvider ap = owner.getLookup().lookup(ActionProvider.class);
                        if (ap != null) {
                            if (Arrays.asList(ap.getSupportedActions()).contains(command) && ap.isActionEnabled(command, Lookups.singleton(singleMethod))) {
                                ap.invokeAction(command, Lookups.fixed(singleMethod, progressOfCommand));
                            }
                        }
                    }
                }
            });
        }
    }

    private void runSingleFile(FileObject file, String command, CommandProgress progressOfCommand) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                Project owner = FileOwnerQuery.getOwner(file);
                if (owner != null) {
                    ActionProvider ap = owner.getLookup().lookup(ActionProvider.class);
                    if (ap != null) {
                        if (Arrays.asList(ap.getSupportedActions()).contains(command) && ap.isActionEnabled(command, Lookups.singleton(file))) {
                            ap.invokeAction(command, Lookups.fixed(file, progressOfCommand));
                        }
                    }
                }
            }
        });
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
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
                ResultHandler handler = new ResultHandler() {
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
                                                              SymbolInformation symbol = new SymbolInformation(symbolName, Utils.elementKind2SymbolKind(kind), tree2Location(cc, path), te.getQualifiedName().toString());

                                                              symbol.setDeprecated(false);
                                                              symbols.add(symbol);
                                                          }
                                                      }
                                                      for (Element ne : te.getEnclosedElements()) {
                                                          if (ident.equals(getSimpleName(ne, te, false))) {
                                                              TreePath path = cc.getTrees().getPath(ne);

                                                              if (path != null) {
                                                                  final Pair<String,String> name = JavaSymbolProvider.getDisplayName(ne, te);
                                                                  final String symbolName = name.first() + (name.second() != null ? name.second() : "");
                                                                  final ElementKind kind = ne.getKind();
                                                                  SymbolInformation symbol = new SymbolInformation(symbolName, Utils.elementKind2SymbolKind(kind), tree2Location(cc, path), te.getQualifiedName().toString());

                                                                  symbol.setDeprecated(false);
                                                                  symbols.add(symbol);
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
                JavaSymbolProvider.doComputeSymbols(getSearchType(queryFin, exactFin, false, null, null), queryFin, handler, true, cancel);
                Collections.sort(symbols, (i1, i2) -> i1.getName().compareToIgnoreCase(i2.getName()));
                result.complete(symbols);
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
    public void didChangeConfiguration(DidChangeConfigurationParams arg0) {
        //TODO: no real configuration right now
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
            if (running <= success + failure) {
                commandFinished.complete(failure == 0);
            }
        }

        CompletableFuture<Object> getFinishFuture() {
            return commandFinished;
        }
    }
}
