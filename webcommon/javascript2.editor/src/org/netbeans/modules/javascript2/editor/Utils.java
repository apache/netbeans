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
package org.netbeans.modules.javascript2.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Pisl
 */
public class Utils {

    public static final List<JsTokenId> LOOK_FOR_IMPORT_EXPORT_TOKENS = Collections.unmodifiableList(Arrays.asList(JsTokenId.KEYWORD_IMPORT, JsTokenId.KEYWORD_EXPORT, JsTokenId.OPERATOR_SEMICOLON));

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    //refreshTaskListIndex fields
    private static final int REFRESH_INDEX_SLIDING = 2_500; //ms
    private static final Set</*@GuardedBy("todo")*/Request> todo = new HashSet<>();
    private static final RequestProcessor REFRESH_INDEX_FIRER = new RequestProcessor(Utils.class);
    private static final RequestProcessor.Task REFRESH_INDEX_TASK = REFRESH_INDEX_FIRER.create(() -> {
        synchronized (todo) {
            for (Iterator<Request> it = todo.iterator(); it.hasNext();) {
                final Request r = it.next();
                it.remove();
                if (r.root != null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(
                                Level.FINE,
                                "Schedule reindex for generic root: {0}",   //NOI18N
                                FileUtil.getFileDisplayName(r.root));
                    }
                    //Todo: for now no way how to pass the root to parsing API.
                    //Api change in default
                    IndexingManager.getDefault().refreshAllIndices("TLIndexer");    //NOI18N
                } else {
                    LOG.log(
                                Level.FINE,
                                "Schedule global reindex.");   //NOI18N
                    IndexingManager.getDefault().refreshAllIndices("TLIndexer");    //NOI18N
                }
            }
        }
    });

    /**
     * Converts the types names to the display names. It can return empty collection
     * if the all types in input refers anonymous objects or functions.
     * @param types collection of types
     * @return collection of display names
     */
    public static Collection<String> getDisplayNames(Collection<? extends Type> types) {
        List<String> displayNames = new ArrayList<>(types.size());
        for (Type type : types) {
            String displayName = ModelUtils.getDisplayName(type);
            if (displayName != null && !displayName.isEmpty() && !displayNames.contains(displayName)) {
                displayNames.add(displayName);
            }
        }
        Collections.sort(displayNames);
        return displayNames;
    }

    /**
     * Converts the fully qualified names to the display names. It can return empty collection
     * if the all types in input refers anonymous objects or functions.
     * @param types collection of fully qualified names
     * @return collection of display names
     */
    public static Collection<String> getDisplayNamesFromStrings(Collection<String> fqns) {
        List<String> displayNames = new ArrayList<>(fqns.size());
        for (String fqn : fqns) {
            String displayName = ModelUtils.getDisplayName(fqn);
            if (displayName.length() != 0) {
                displayNames.add(displayName);
            }
        }
        return displayNames;
    }

    /**
     * Refreshes a task list indexer.
     * If an artifact is given it tries to minimize the refresh only to project
     * owning the artifact.
     * @param artifact the optional project artifact to minimize the refresh
     */
    public static void refreshTaskListIndex(@NullAllowed FileObject artifact) {
        final Optional<FileObject> maybeFile = Optional.ofNullable(artifact);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(
                    Level.FINE,
                    "Refresh task list index with artefact: {0}",   //NOI18N
                    maybeFile.map((f)->FileUtil.getFileDisplayName(f)).orElse(null));
        }
        final Optional<Collection<Request>> roots = maybeFile
                .map((fo) -> FileOwnerQuery.getOwner(fo))
                .map((p) -> {
                    return Arrays.stream(ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC))
                            .map((sg) -> sg.getRootFolder())
                            .filter((root) -> root != null)
                            .map((root) -> Request.forRoot(root)).
                            collect(Collectors.toList());
                });
        final Collection<Request> toAdd = roots.isPresent() ?
                roots.get() :
                Collections.singleton(Request.all());
        final boolean shouldAdd;
        synchronized (todo) {
            shouldAdd = !todo.contains(Request.all());
            if (shouldAdd) {
                todo.addAll(toAdd);
            }
        }
        if (LOG.isLoggable(Level.FINE) && shouldAdd) {
            LOG.log(
                    Level.FINE,
                    "Added requests: {0}",  //NOI18N
                    toAdd);
        }
        REFRESH_INDEX_TASK.schedule(REFRESH_INDEX_SLIDING);
    }

    private static final class Request {
        private static final Request ALL = new Request(null);
        final FileObject root;

        private Request(@NullAllowed FileObject root) {
            this.root = root;
        }

        @Override
        public int hashCode() {
            return root == null ? 0 : root.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Request)) {
                return false;
            }
            final Request other = (Request) obj;
            return root == null ? other.root == null : root.equals(other.root);
        }

        @Override
        public String toString() {
            return root == null ?
                    "<all>" :   //NOI18N
                    FileUtil.getFileDisplayName(root);
        }

        @NonNull
        static Request all() {
            return ALL;
        }

        @NonNull
        static Request forRoot(@NonNull final FileObject root) {
            assert root != null;
            return new Request(root);
        }
    }
}
