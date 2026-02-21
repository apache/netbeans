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
package org.netbeans.modules.php.blade.editor.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.blade.editor.parser.BladeParserResult.Reference;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author bhaidu
 */
public class BladeIndex {

    private final QuerySupport querySupport;
    private static final Map<Project, BladeIndex> INDEXES = new WeakHashMap<>();
    private static boolean areProjectsOpen = false;

    private BladeIndex(QuerySupport querySupport) throws IOException {
        this.querySupport = querySupport;
    }

    public QuerySupport getQuerySupport() {
        return querySupport;
    }

    public static BladeIndex get(Project project) throws IOException {
        if (project == null) {
            return null;
        }
        synchronized (INDEXES) {
            BladeIndex index = INDEXES.get(project);
            if (index == null) {
                if (!areProjectsOpen) {
                    try {
                        // just be sure that the projects are open
                        OpenProjects.getDefault().openProjects().get();
                    } catch (InterruptedException | ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        areProjectsOpen = true;
                    }
                }
                Collection<FileObject> sourceRoots = QuerySupport.findRoots(project,
                        null /* all source roots */,
                        Collections.<String>emptyList(),
                        Collections.<String>emptyList());
                QuerySupport querySupport = QuerySupport.forRoots(BladeIndexer.Factory.NAME, BladeIndexer.Factory.VERSION, sourceRoots.toArray(new FileObject[]{}));
                index = new BladeIndex(querySupport);
                if (!sourceRoots.isEmpty()) {
                    INDEXES.put(project, index);
                }
            }
            return index;
        }
    }

    public List<IndexedReferenceId> queryYieldIds(String prefix) {
        return queryIndexedReferenceId(prefix, BladeIndexer.YIELD_ID);
    }

    public List<IndexedReferenceId> queryStacksIndexedReferences(String prefix) {
        return queryIndexedReferenceId(prefix, BladeIndexer.STACK_ID);
    }

    private List<IndexedReferenceId> queryIndexedReferenceId(String prefix, String indexKey) {
        List<IndexedReferenceId> indexedReferences = new ArrayList<>();

        try {
            Collection<? extends IndexResult> result = querySupport.query(indexKey, prefix, QuerySupport.Kind.PREFIX, indexKey);

            if (result == null || result.isEmpty()) {
                return indexedReferences;
            }

            for (IndexResult indexResult : result) {
                String[] values = indexResult.getValues(indexKey);
                for (String value : values) {
                    if (value.startsWith(prefix)) {
                        indexedReferences.add(new IndexedReferenceId(value, indexResult.getFile()));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return indexedReferences;
    }

    public List<IndexedReference> queryYieldIndexedReferences(String prefix) {
        return queryIndexedReferences(prefix,
                BladeIndexer.YIELD_REFERENCE,
                new IndexReferenceCallback() {
            @Override
            public Reference createIndexReference(String value) {
                return BladeIndexer.extractYieldDataFromIndex(value);
            }
        }
        );
    }

    public List<IndexedReference> queryStacksIdsReference(String prefix) {
        return queryIndexedReferences(prefix,
                BladeIndexer.STACK_REFERENCE,
                new IndexReferenceCallback() {
            @Override
            public Reference createIndexReference(String value) {
                return BladeIndexer.extractStackDataFromIndex(value);
            }
        }
        );
    }

    private List<IndexedReference> queryIndexedReferences(String prefix, String indexKey, IndexReferenceCallback callback) {
        List<IndexedReference> references = new ArrayList<>();
        try {
            Collection<? extends IndexResult> result = querySupport.query(indexKey,
                    prefix, QuerySupport.Kind.PREFIX, indexKey);

            if (result == null || result.isEmpty()) {
                return references;
            }

            for (IndexResult indexResult : result) {
                String[] values = indexResult.getValues(indexKey);
                for (String value : values) {
                    if (value.startsWith(prefix)) {
                        references.add(new IndexedReference(callback.createIndexReference(value), indexResult.getFile()));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return references;
    }

    public List<IndexedReference> findYieldIndexedReferences(String prefix) {
        return findIndexedReferences(prefix,
                BladeIndexer.YIELD_ID,
                new String[]{BladeIndexer.YIELD_ID, BladeIndexer.YIELD_REFERENCE},
                BladeIndexer.YIELD_REFERENCE,
                new IndexReferenceCallback() {
            @Override
            public Reference createIndexReference(String value) {
                return BladeIndexer.extractYieldDataFromIndex(value);
            }
        }
        );
    }
    
    public List<IndexedReference> findStackIdIndexedReferences(String prefix) {
        return findIndexedReferences(prefix,
                BladeIndexer.STACK_ID,
                new String[]{BladeIndexer.STACK_ID, BladeIndexer.STACK_REFERENCE},
                BladeIndexer.STACK_REFERENCE,
                new IndexReferenceCallback() {
            @Override
            public Reference createIndexReference(String value) {
                return BladeIndexer.extractStackDataFromIndex(value);
            }
        }
        );
    }

    private List<IndexedReference> findIndexedReferences(String prefix,
            String indexKey, String[] valuesKeys, String valueKey,
            IndexReferenceCallback callback) {
        List<IndexedReference> references = new ArrayList<>();
        try {
            Collection<? extends IndexResult> result = querySupport.query(indexKey,
                    prefix, QuerySupport.Kind.EXACT, valuesKeys);

            if (result == null || result.isEmpty()) {
                return references;
            }

            for (IndexResult indexResult : result) {
                String[] values = indexResult.getValues(valueKey);
                for (String value : values) {
                    String name = BladeIndexer.getIdFromSignature(value);
                    if (name != null && name.equals(prefix)) {
                        references.add(
                                new IndexedReference(callback.createIndexReference(value),
                                indexResult.getFile()));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return references;
    }

    public List<IndexedOffsetReference> getIncludePaths(String prefix) {
        List<IndexedOffsetReference> references = new ArrayList<>();
        Collection<? extends IndexResult> result;
        try {
            result = querySupport.query(BladeIndexer.INCLUDE_PATH, prefix, QuerySupport.Kind.PREFIX, BladeIndexer.INCLUDE_PATH);

            if (result == null || result.isEmpty()) {
                return references;
            }

            for (IndexResult indexResult : result) {
                String[] values = indexResult.getValues(BladeIndexer.INCLUDE_PATH);
                for (String value : values) {
                    Reference templatePathRef = BladeIndexer.extractTemplatePathDataFromIndex(value);
                    if (!templatePathRef.identifier.equals(prefix)) {
                        continue;
                    }
                    references.add(new IndexedOffsetReference(templatePathRef.identifier, indexResult.getFile(), templatePathRef.defOffset));
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return references;
    }

    public static interface IndexReferenceCallback {

        public Reference createIndexReference(String value);
    }

    public static class IndexedReferenceId {

        private final String identifier;
        private final FileObject originFile;

        public IndexedReferenceId(String identifier, FileObject originFile) {
            this.identifier = identifier;
            this.originFile = originFile;
        }

        public String getIdenfiier() {
            return this.identifier;
        }

        public FileObject getOriginFile() {
            return this.originFile;
        }
    }

    public static class IndexedReference {

        private final Reference reference;
        private final FileObject originFile;

        public IndexedReference(Reference reference, FileObject originFile) {
            this.reference = reference;
            this.originFile = originFile;
        }

        public Reference getReference() {
            return this.reference;
        }

        public FileObject getOriginFile() {
            return this.originFile;
        }
    }

    public static class IndexedOffsetReference {

        private final String identifier;
        private final FileObject originFile;
        private final OffsetRange range;

        public IndexedOffsetReference(String identifier,
                FileObject originFile, OffsetRange range) {
            this.identifier = identifier;
            this.originFile = originFile;
            this.range = range;
        }

        public String getReference() {
            return this.identifier;
        }

        public FileObject getOriginFile() {
            return this.originFile;
        }

        public int getStart() {
            return this.range.getStart();
        }
    }
}
