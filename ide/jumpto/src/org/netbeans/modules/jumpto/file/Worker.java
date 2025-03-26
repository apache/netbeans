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

package org.netbeans.modules.jumpto.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.search.provider.SearchFilter;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.modules.jumpto.common.Utils;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.netbeans.spi.jumpto.file.FileProvider;
import org.netbeans.spi.jumpto.file.FileProviderFactory;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class Worker implements Runnable {

    private static final Logger LOG = Logger.getLogger(Worker.class.getName());

    private final Request request;
    private final Strategy strategy;
    private final Collector collector;
    private final long createTime;
    private volatile boolean cancelled;

    private Worker(
            @NonNull final Request request,
            @NonNull final Strategy strategy,
            @NonNull final Collector collector) {
        Parameters.notNull("request", request);     //NOI18N
        Parameters.notNull("strategy", strategy);   //NOI18N
        Parameters.notNull("collector", collector); //NOI18N
        this.request = request;
        this.strategy = strategy;
        this.collector = collector;
        this.createTime = System.currentTimeMillis();
        this.collector.configure(this);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(
                Level.FINE,
                "Worker: {0} for: {1} handled by: {2} created after: {3}ms.",    //NOI18N
                        new Object[]{
                            System.identityHashCode(this),
                            request,
                            strategy,
                            this.createTime - this.collector.startTime
                });
        }
    }

    public void cancel() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(
                Level.FINE,
                "Worker: {0} canceled after {1} ms.",   //NOI18N
                new Object[]{
                    System.identityHashCode(this),
                    System.currentTimeMillis() - createTime
                });
        }
        this.cancelled = true;
        this.strategy.cancel();
    }

    @Override
    public void run() {
        this.collector.start(this);
        try {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                    Level.FINE,
                    "Worker: {0} started after {1} ms.", //NOI18N
                    new Object[]{
                        System.identityHashCode(this),
                        System.currentTimeMillis() - createTime
                    });
            }
            this.strategy.execute(this.request, this);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                    Level.FINE,
                    "Worker: {0} finished after cancel {1} ms.",  //NOI18N
                    new Object[]{
                        System.identityHashCode(this),
                        System.currentTimeMillis() - createTime
                    });
            }
        } finally {
            this.collector.done(this);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "%s (%d) [request: %s, strategy: %s]",  //NOI18N
            getClass().getSimpleName(),
            System.identityHashCode(this),
            this.request,
            this.strategy);
    }

    private void emit(@NonNull final List<? extends FileDescriptor> files) {
        this.collector.emit(this, files);
    }

    @NonNull
    static Request newRequest(
        @NonNull final String text,
        @NonNull final QuerySupport.Kind searchType,
        @NullAllowed final Project project,
        final boolean searchByFolders,
        final int lineNr) {
        return new Request(text, searchType, project, searchByFolders, lineNr);
    }


    @NonNull
    static Collector newCollector(
        @NonNull final Models.MutableListModel<FileDescriptor> model,
        @NonNull final Runnable updateCallBack,
        @NonNull final Runnable doneCallBack,
        final long startTime) {
        return new Collector(model, updateCallBack, doneCallBack, startTime);
    }

    @NonNull
    static Worker newWorker(
        @NonNull final Request request,
        @NonNull final Collector collector,
        @NonNull final Type type) {
        Parameters.notNull("request", request); //NOI18N
        Parameters.notNull("collector", collector); //NOI18N
        Parameters.notNull("type", type);   //NOI18N
        final Strategy strategy = type.createStrategy();
        return new Worker(request, strategy, collector);
    }

    static enum Type {
        PROVIDER {
            @NonNull
            @Override
            Strategy createStrategy() {
                return new ProviderStrategy();
            }
        },
        INDEX {
            @NonNull
            @Override
            Strategy createStrategy() {
                return new IndexStrategy();
            }
        },
        FS {
            @NonNull
            @Override
            Strategy createStrategy() {
                return new FSStrategy();
            }
        };
        @NonNull
        abstract Strategy createStrategy();
    }

    static final class Request {
        private final String text;
        private final QuerySupport.Kind searchType;
        private final Project currentProject;
        private final boolean searchByFolders;
        private final int lineNr;
        private final Set<FileObject> excludes;
        //@GuardedBy("this")
        private Collection<? extends FileObject> sgRoots;
        //@GuardedBy("this")
        private Collection<? extends Project> projects;

        private Request(
            @NonNull final String text,
            @NonNull final QuerySupport.Kind searchType,
            @NullAllowed final Project currentProject,
            final boolean searchByFolders,
            final int lineNr) {
            Parameters.notNull("text", text);   //NOI18N
            Parameters.notNull("searchType", searchType);   //NOI18N
            this.text = text;
            this.searchType = searchType;
            this.currentProject = currentProject;
            this.searchByFolders = searchByFolders;
            this.lineNr = lineNr;
            this.excludes = Collections.newSetFromMap(new ConcurrentHashMap<FileObject, Boolean>());
        }

        @NonNull
        String getText() {
            return text;
        }

        @NonNull
        QuerySupport.Kind getSearchKind() {
            return searchType;
        }

        public boolean isSearchByFolders() {
            return searchByFolders;
        }

        @CheckForNull
        Project getCurrentProject() {
            return currentProject;
        }

        int getLine() {
            return lineNr;
        }

        @Override
        public String toString() {
            return String.format(
                "%s[text: %s, search kind: %s, project: %s, line: %d]", //NOI18N
                getClass().getSimpleName(),
                text,
                searchType,
                currentProject,
                lineNr);
        }

        @NonNull
        private synchronized Collection<? extends Project> getOpenProjects() {
            if (projects == null) {
                final Project[] opa = OpenProjects.getDefault().getOpenProjects();
                final List<Project> pl = new ArrayList<>(opa.length);
                if (currentProject != null) {
                    pl.add(currentProject);
                }
                for (Project p : opa) {
                    Project getRidOfFod = p.getLookup().lookup(Project.class);
                    if (getRidOfFod != null) {
                        p = getRidOfFod;
                    }
                    if (!Objects.equals(p, currentProject)) {
                        pl.add(p);
                    }
                }
                projects = Collections.unmodifiableCollection(pl);
            }
            return projects;
        }

        private synchronized Collection<? extends FileObject> getSourceRoots() {
            if (sgRoots == null) {
                final Collection<? extends Project> projects = getOpenProjects();
                final List<FileObject> newSgRoots = new ArrayList<>();
                for (Project p : projects) {
                    for (SourceGroup group : ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC)) {
                        newSgRoots.add(group.getRootFolder());
                    }
                }
                sgRoots = Collections.unmodifiableCollection(newSgRoots);
            }
            return sgRoots;
        }

        private boolean isExcluded(@NonNull final FileObject file) {
            return excludes.contains(file);
        }

        private void exclude(@NonNull final FileObject file) {
            excludes.add(file);
        }
    }

    static final class Collector {
        private final Models.MutableListModel<FileDescriptor> model;
        private final Runnable updateCallBack;
        private final Runnable doneCallBack;
        private final long startTime;
        private final Set<Worker> active = Collections.newSetFromMap(new ConcurrentHashMap<Worker, Boolean>());
        private volatile boolean frozen;
        private boolean someCancelled;  //Threading: Accessed from a single (worker) thread

        private Collector(
            @NonNull final Models.MutableListModel<FileDescriptor> model,
            @NonNull final Runnable updateCallBack,
            @NonNull final Runnable doneCallBack,
            final long startTime) {
            Parameters.notNull("model", model); //NOI18N
            Parameters.notNull("updateCallBack", updateCallBack);   //NOI18N
            Parameters.notNull("doneCallBack", doneCallBack);   //NOI18N
            this.model = model;
            this.updateCallBack = updateCallBack;
            this.doneCallBack = doneCallBack;
            this.startTime = startTime;
        }

        @Override
        public String toString() {
            return String.format(
                "%s (%d) [frozen: %s, active: %s]", //NOI18N
                getClass().getSimpleName(),
                System.identityHashCode(this),
                frozen,
                active);
        }

        boolean isDone() {
            return frozen && active.isEmpty() && !someCancelled;
        }

        private void configure(@NonNull final Worker worker) {
            Parameters.notNull("worker", worker);   //NOI18N
            if (frozen) {
                throw new IllegalStateException(String.format(
                    "Adding worker: %s to already frozen collector: %s",    //NOI18N
                    worker,
                    this));
            }
            if (!active.add(worker)) {
                throw new IllegalArgumentException(String.format(
                    "Adding already added worker: %s to collector: %s",
                    worker,
                    this
                ));
            }
        }

        private void start(@NonNull final Worker worker) {
            Parameters.notNull("worker", worker);   //NOI18N
            frozen = true;
        }

        private void emit(
            @NonNull final Worker worker,
            @NonNull final List<? extends FileDescriptor> files) {
            Parameters.notNull("worker", worker);   //NOI18N
            Parameters.notNull("files", files); //NOI18N
            final boolean cancelled = worker.cancelled;
            if (!cancelled) {
                model.add(files);
                updateCallBack.run();
            }
        }

        private void done(@NonNull final Worker worker) {
            Parameters.notNull("worker", worker);   //NOI18N
            someCancelled |= worker.cancelled;
            if (!active.remove(worker)) {
                throw new IllegalStateException(String.format(
                    "Trying to removed unknown worker: %s from collector: %s",  //NOI18N
                    worker,
                    this));
            }
            if (isDone()) {
                doneCallBack.run();
            }
        }
    }

    private abstract static class Strategy {
        private volatile boolean cancelled;

        @CheckForNull
        abstract  void execute(@NonNull Request request, @NonNull final Worker worker);

        void cancel() {
            cancelled = true;
        }

        final boolean isCancelled() {
            return cancelled;
        }
    }

    private static final class ProviderStrategy extends Strategy {

        //@GuardedBy("this")
        private List<? extends FileProvider> providers;
        private volatile FileProvider currentProvider;

        @Override
        void execute(@NonNull final Request request, @NonNull final Worker worker) {
            if (isCancelled()) {
                return;
            }
            final List<FileDescriptor> files = new ArrayList<>();
            final SearchType jumpToSearchType = Utils.toSearchType(request.getSearchKind());
            final FileProvider.Context ctx = FileProviderAccessor.getInstance().createContext(
                request.getText(),
                jumpToSearchType,
                request.getLine(),
                request.getCurrentProject());
            final FileProvider.Result fpR = FileProviderAccessor.getInstance().createResult(
                files,
                new String[1],
                ctx);
            for (FileObject root : request.getSourceRoots()) {
                if (request.isExcluded(root)) {
                    continue;
                }
                FileProviderAccessor.getInstance().setRoot(ctx, root);
                boolean recognized = false;
                for (FileProvider provider : getProviders()) {
                    if (isCancelled()) {
                        return;
                    }
                    currentProvider = provider;
                    try {
                        recognized = provider.computeFiles(ctx, fpR);
                        if (recognized) {
                            break;
                        }
                    } finally {
                        currentProvider = null;
                    }
                }
                if (recognized) {
                    request.exclude(root);
                }
                if (!files.isEmpty()) {
                    worker.emit(files);
                    files.clear();
                }
            }
        }

        @Override
        void cancel() {
            super.cancel();
            FileProvider fp = currentProvider;
            if (fp != null) {
                fp.cancel();
            }
        }

        private Iterable<? extends FileProvider> getProviders() {
            synchronized (this) {
                if (providers != null) {
                    return providers;
                }
            }
            final List<FileProvider> result = new ArrayList<FileProvider>();
            for (FileProviderFactory fpf : Lookup.getDefault().lookupAll(FileProviderFactory.class)) {
                result.add(fpf.createFileProvider());
            }
            synchronized (this) {
                if (providers == null) {
                    providers = Collections.unmodifiableList(result);
                }
                return providers;
            }
        }
    }

    private static final class IndexStrategy extends Strategy {

        @Override
        void execute(@NonNull final Request request, @NonNull final Worker worker) {
            if (isCancelled()) {
                return;
            }
            final Pair<String,String> query = createQuery(request);
            final Map<Project,Collection<FileObject>> rbp = collectRoots(request);
            try {
                for (Project p : request.getOpenProjects()) {
                    final Collection<FileObject> roots = rbp.get(p);
                    if (roots != null) {
                        doQuery(
                            query,
                            request,
                            worker,
                            filterExcluded(roots, request));
                    }
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        @NonNull
        private Map<Project,Collection<FileObject>> collectRoots(
            @NonNull final Request request) {
            return QuerySupport.findRoots(
                request.getOpenProjects(),
                null,
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
        }

        private boolean doQuery(
            @NonNull final Pair<String,String> query,
            @NonNull final Request request,
            @NonNull final Worker worker,
            @NonNull Collection<? extends FileObject> roots) throws IOException {
            if (isCancelled()) {
                return false;
            }
            final QuerySupport q = QuerySupport.forRoots(
                FileIndexer.ID,
                FileIndexer.VERSION,
                roots.toArray(new FileObject[0]));
            final QuerySupport.Query.Factory f = q.getQueryFactory().
                    setCamelCaseSeparator(FileSearchAction.CAMEL_CASE_SEPARATOR).
                    setCamelCasePart(Utils.isCaseSensitive(Utils.toSearchType(request.getSearchKind())) ?
                            FileSearchAction.CAMEL_CASE_PART_CASE_SENSITIVE :
                            FileSearchAction.CAMEL_CASE_PART_CASE_INSENSITIVE);
            if (isCancelled()) {
                return false;
            }
            final List<FileDescriptor> files = new ArrayList<>();
            final Collection<? extends IndexResult> results = f.field(
                query.first(),
                query.second(),
                request.getSearchKind()).execute();
            for (IndexResult r : results) {
                FileObject file = r.getFile();
                if (file == null || !file.isValid()) {
                    // the file has been deleted in the meantime
                    continue;
                }
                final Project project = ProjectConvertors.getNonConvertorOwner(file);
                FileDescriptor fd = new FileDescription(
                        file,
                        r.getRelativePath(),
                        project);
                boolean preferred = project != null && request.getCurrentProject() != null ?
                        project.getProjectDirectory() == request.getCurrentProject().getProjectDirectory() :
                        false;
                FileProviderAccessor.getInstance().setFromCurrentProject(fd, preferred);
                FileProviderAccessor.getInstance().setLineNumber(fd, request.getLine());
                files.add(fd);
                LOG.log(
                    Level.FINER,
                    "Found: {0}, project={1}, currentProject={2}, preferred={3}",   //NOI18N
                    new Object[]{
                        file.getPath(),
                        project,
                        request.getCurrentProject(),
                        preferred
                    });
            }
            for (FileObject root : roots) {
                request.exclude(root);
            }
            worker.emit(files);
            return true;
        }

        @NonNull
        private Pair<String,String> createQuery(@NonNull final Request request) {
            String searchField;
            String indexQueryText;
            switch (request.getSearchKind()) {
                case CASE_INSENSITIVE_PREFIX:
                    searchField = FileIndexer.FIELD_CASE_INSENSITIVE_NAME;
                    indexQueryText = request.getText();
                    break;
                case CASE_INSENSITIVE_REGEXP:
                    searchField = FileIndexer.FIELD_CASE_INSENSITIVE_NAME;
                    indexQueryText = NameMatcherFactory.wildcardsToRegexp(request.getText(),true);
                    Pattern.compile(indexQueryText);    //Verify the pattern
                    break;
                case CASE_INSENSITIVE_CAMEL_CASE:
                    searchField = FileIndexer.FIELD_CASE_INSENSITIVE_NAME;
                    indexQueryText = request.getText();
                    break;
                case REGEXP:
                    searchField = FileIndexer.FIELD_NAME;
                    indexQueryText = NameMatcherFactory.wildcardsToRegexp(request.getText(),true);
                    Pattern.compile(indexQueryText);    //Verify the pattern
                    break;
                default:
                    searchField = FileIndexer.FIELD_NAME;
                    indexQueryText = request.getText();
                    break;
            }
            if (request.isSearchByFolders()) {
                if (searchField.equals(FileIndexer.FIELD_CASE_INSENSITIVE_NAME)) {
                    searchField = FileIndexer.FIELD_CASE_INSENSITIVE_RELATIVE_PATH;
                } else {
                    searchField = FileIndexer.FIELD_RELATIVE_PATH;
                }
            }
            return Pair.<String,String>of(searchField, indexQueryText);
        }

        @NonNull
        private static Collection<FileObject> filterExcluded(
            @NonNull final Collection<? extends FileObject> roots,
            @NonNull final Request request) {
            final List<FileObject> result = new ArrayList<>(roots.size());
            for (FileObject root : roots) {
                if (!request.isExcluded(root)) {
                    result.add(root);
                }
            }
            return result;
        }
    }

    private static final class FSStrategy extends Strategy {

        @CheckForNull
        @Override
        void execute(@NonNull final Request request, @NonNull final Worker worker) {
            if (isCancelled()) {
                return;
            }
            final SearchType jumpToSearchType = Utils.toSearchType(request.getSearchKind());
            //Looking for matching files in all found folders
            final NameMatcher matcher = NameMatcherFactory.createNameMatcher(
                    request.getText(),
                    jumpToSearchType,
                    Utils.isCaseSensitive(Utils.toSearchType(request.getSearchKind())) ?
                            FileSearchAction.SEARCH_OPTIONS_CASE_SENSITIVE :
                            FileSearchAction.SEARCH_OPTIONS_CASE_INSENSITIVE);
            final List<FileDescriptor> files = new ArrayList<FileDescriptor>();
            final Collection <FileObject> allFolders = new HashSet<FileObject>();
            List<SearchFilter> filters = SearchInfoUtils.DEFAULT_FILTERS;
            for (FileObject root : request.getSourceRoots()) {
                allFolders.clear();
                for (FileObject folder : searchSources(
                        root,
                        allFolders,
                        request,
                        filters)) {
                    if (isCancelled()) {
                        return;
                    }
                    assert folder.isFolder();
                    Enumeration<? extends FileObject> filesInFolder = folder.getData(false);
                    while (filesInFolder.hasMoreElements()) {
                        FileObject file = filesInFolder.nextElement();
                        if (file.isFolder()) continue;

                        final String rootRelativePath = FileUtil.getRelativePath(root, file);

                        if (matcher.accept(request.isSearchByFolders() ? rootRelativePath : file.getNameExt())) {
                            Project project = ProjectConvertors.getNonConvertorOwner(file);
                            boolean preferred = false;
                            String relativePath = null;
                            if(project != null) { // #176495
                               FileObject pd = project.getProjectDirectory();
                               preferred = request.getCurrentProject() != null ?
                                 pd == request.getCurrentProject().getProjectDirectory() :
                                 false;
                                relativePath = FileUtil.getRelativePath(pd, file);
                            }
                            if (relativePath == null)
                                relativePath ="";   //NOI18N
                            FileDescriptor fd = new FileDescription(
                                file,
                                relativePath,
                                project);
                            FileProviderAccessor.getInstance().setFromCurrentProject(fd, preferred);
                            FileProviderAccessor.getInstance().setLineNumber(fd, request.getLine());
                            files.add(fd);
                        }
                    }
                    request.exclude(folder);
                }
                if (!files.isEmpty()) {
                    worker.emit(files);
                    files.clear();
                }
            }
        }

        @NonNull
        private Collection<FileObject> searchSources(
                @NonNull final FileObject root,
                @NonNull final Collection<FileObject> result,
                @NonNull final Request  request,
                @NonNull final List<SearchFilter> filters) {
            if (isCancelled() ||
                root.getChildren().length == 0 ||
                request.isExcluded(root) ||
                !checkAgainstFilters(root, filters)) {
                return result;
            } else {
                    result.add(root);
                    final Enumeration<? extends FileObject> subFolders = root.getFolders(false);
                    while (subFolders.hasMoreElements()) {
                        searchSources(subFolders.nextElement(), result, request, filters);
                    }
            }
            return result;
        }

        private boolean checkAgainstFilters(FileObject folder, List<SearchFilter> filters) {
            assert folder.isFolder();
            for (SearchFilter filter: filters) {
                if (filter.traverseFolder(folder) == SearchFilter.FolderResult.DO_NOT_TRAVERSE) {
                    return false;
                }
            }
            return true;
        }
    }
}
