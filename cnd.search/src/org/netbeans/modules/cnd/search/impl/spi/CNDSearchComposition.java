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
package org.netbeans.modules.cnd.search.impl.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.cnd.search.MatchingFileData;
import org.netbeans.modules.cnd.search.SearchParams;
import org.netbeans.modules.cnd.search.SearchResult;
import org.netbeans.modules.cnd.search.Searcher;
import org.netbeans.modules.cnd.search.impl.UnixFindBasedSearcher;
import org.netbeans.modules.cnd.search.ui.SearchResultNode;
import org.netbeans.modules.cnd.search.ui.SearchResultPropertySet;
import org.netbeans.modules.cnd.search.util.OutlineSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.search.provider.DefaultSearchResultsDisplayer;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider;
import org.netbeans.spi.search.provider.SearchProvider.Presenter;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.netbeans.spi.search.provider.SearchResultsDisplayer.NodeDisplayer;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 */
public final class CNDSearchComposition extends SearchComposition<SearchResult> {

    private static final RequestProcessor RP = new RequestProcessor(CNDSearchComposition.class.getName(), 2);
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private final SearchParams params;
    private DefaultSearchResultsDisplayer<SearchResult> displayer;
    private final String title;
    private final Presenter presenter;
    private Cancellable cancel;

    public CNDSearchComposition(String title, SearchProvider.Presenter presenter, SearchParams params) {
        this.title = title;
        this.presenter = presenter;
        this.params = params;
    }

    @Override
    public void start(SearchListener listener) {
        try {
            for (SearchRoot root : params.getSearchRoots()) {
                searchInRoot(root, listener);
            }
        } catch (Exception e) {
            listener.generalError(e);
        } finally {
            terminate();
        }
    }

    @Override
    public void terminate() {
        if (terminated.compareAndSet(false, true)) {
            if (cancel != null) {
                cancel.cancel();
                cancel = null;
            }
        }
    }

    @Override
    public boolean isTerminated() {
        return terminated.get();
    }

    @Override
    public synchronized SearchResultsDisplayer<SearchResult> getSearchResultsDisplayer() {
        if (displayer == null) {
            displayer = SearchResultsDisplayer.createDefault(new DisplayerHelper(), this, presenter, title);
            displayer.getVisualComponent();
            OutlineView view = displayer.getOutlineView();
            view.setPropertyColumns(SearchResultPropertySet.getNamesAndDisplayNames());
            OutlineSupport.get(CNDSearchComposition.class).installFor(view.getOutline());
        }
        return displayer;
    }

    private void searchInRoot(SearchRoot root, final SearchListener listener) {
        if (isTerminated()) {
            return;
        }

        final ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(root.getFileObject());

        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            return;
        }

        if (!HostInfoUtils.isHostInfoAvailable(env)) {
            return;
        }

        final Searcher find;
        try {
            if (HostInfoUtils.getHostInfo(env).getOSFamily().isUnix()) {
                find = new UnixFindBasedSearcher(root, params);
            } else {
                find = null;
            }
        } catch (Exception ex) {
            return;
        }

        if (find == null) {
            return;
        }

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable(find.getCommand()).setArguments(find.getCommandArguments());

        try {
            final NativeProcess process = npb.call();
            final Runnable readOutputTask = new Runnable() {

                @Override
                public void run() {
                    try {
                        BufferedReader br = ProcessUtils.getReader(process.getInputStream(), env.isRemote());
                        String line;
                        while (!isTerminated() && (line = br.readLine()) != null) {
                            MatchingFileData data = find.processOutputLine(line.trim());

                            if (data != null) {
                                displayer.addMatchingObject(new SearchResult(env, data));
                            }
                        }
                    } catch (IOException ex) {
                        if (!isTerminated()) {
                            listener.generalError(ex);
                        }
                    }
                }
            };

            final Runnable readErrorTask = new Runnable() {

                @Override
                public void run() {
                    try {
                        BufferedReader br = ProcessUtils.getReader(process.getErrorStream(), env.isRemote());
                        String line;
                        while (!isTerminated() && (line = br.readLine()) != null) {
                            Throwable ex = new Throwable(line);
                            listener.generalError(ex);
                        }
                    } catch (IOException ex) {
                        if (!isTerminated()) {
                            listener.generalError(ex);
                        }
                    }
                }
            };

            final Task outTask = RP.post(readOutputTask);
            final Task errTask = RP.post(readErrorTask);

            cancel = new Cancellable() {

                @Override
                public boolean cancel() {
                    process.destroy();
                    outTask.cancel();
                    errTask.cancel();
                    outTask.waitFinished();
                    errTask.waitFinished();
                    return true;
                }
            };

            process.waitFor();
        } catch (InterruptedException ex) {
            // Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            listener.generalError(ex);
        }
    }

    private static class DisplayerHelper extends NodeDisplayer<SearchResult> {

        @Override
        public Node matchToNode(final SearchResult result) {
            return new SearchResultNode(result);
        }
    }
}