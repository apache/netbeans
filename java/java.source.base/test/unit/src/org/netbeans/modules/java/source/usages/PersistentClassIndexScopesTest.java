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
package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class PersistentClassIndexScopesTest extends NbTestCase {

    private static final int PKG_COUNT = 1<<12;
    private static final int CLZ_IN_PKG_COUNT = 1<<5;
    private static final String BIN_FORMAT = "%s%d.%s%dC";  //NOI18N
    private static final String PKG_FORMAT = "%s%d";        //NOI18N
    private static final String PKG_NAME = "pkg";           //NOI18N
    private static final String CLZ_NAME = "Clz";           //NOI18N

    private FileObject cache;
    private FileObject src;

    public PersistentClassIndexScopesTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        final FileObject workDir = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        cache = FileUtil.createFolder(workDir, "cache");    //NOI18N
        src = FileUtil.createFolder(workDir, "src");    //NOI18N
        CacheFolder.setCacheFolder(cache);
        IndexingManager.getDefault().refreshIndexAndWait(src.toURL(), Collections.<URL>emptySet() , true);
        fakeIndex(src.toURL());
        super.setUp();
    }

    public void testOverruningClassIndexScopes() throws IOException, InterruptedException {
        final ClassIndexImpl index = ClassIndexManager.getDefault().getUsagesQuery(src.toURL(), true);
        assertNotNull(index);
        final List<Document> res = new ArrayList<>(PKG_COUNT*CLZ_IN_PKG_COUNT);
        Set<ClassIndex.SearchScopeType> scopes = new HashSet<>();
        scopes.add(ClassIndex.SearchScope.SOURCE);
        index.getDeclaredElements(
                "", //NOI18N
                ClassIndex.NameKind.PREFIX,
                scopes,
                null,
                Identity.<Document>getInstance(),
                res);
        assertEquals(PKG_COUNT*CLZ_IN_PKG_COUNT, res.size());
        res.clear();
        scopes.clear();
        final Set<String> pkgs = new HashSet<>();
        index.getPackageNames("", true, pkgs);
        assertEquals(PKG_COUNT, pkgs.size());
        scopes.add(ClassIndex.createPackageSearchScope(ClassIndex.SearchScope.SOURCE, pkgs.toArray(new String[0])));
        index.getDeclaredElements(
                "", //NOI18N
                ClassIndex.NameKind.PREFIX,
                scopes,
                null,
                Identity.<Document>getInstance(),
                res);
        assertEquals(PKG_COUNT*CLZ_IN_PKG_COUNT, res.size());
    }

    public void testFiltering() throws IOException, InterruptedException {
        final ClassIndexImpl index = ClassIndexManager.getDefault().getUsagesQuery(src.toURL(), true);
        assertNotNull(index);
        final List<Document> res = new ArrayList<>(PKG_COUNT*CLZ_IN_PKG_COUNT);
        Set<ClassIndex.SearchScopeType> scopes = new HashSet<>();
        scopes.add(ClassIndex.SearchScope.SOURCE);
        index.getDeclaredElements(
                "", //NOI18N
                ClassIndex.NameKind.PREFIX,
                scopes,
                null,
                Identity.<Document>getInstance(),
                res);
        assertEquals(PKG_COUNT*CLZ_IN_PKG_COUNT, res.size());
        res.clear();
        scopes.clear();
        final Set<String> pkgs = new HashSet<>();
        pkgs.add(String.format(PKG_FORMAT, PKG_NAME, 0));
        scopes.add(ClassIndex.createPackageSearchScope(ClassIndex.SearchScope.SOURCE, pkgs.toArray(new String[0])));
        index.getDeclaredElements(
                "", //NOI18N
                ClassIndex.NameKind.PREFIX,
                scopes,
                null,
                Identity.<Document>getInstance(),
                res);
        assertEquals(CLZ_IN_PKG_COUNT, res.size());
        res.clear();
        scopes.clear();
        pkgs.clear();
        pkgs.add(String.format(PKG_FORMAT, PKG_NAME, 0));
        pkgs.add(String.format(PKG_FORMAT, PKG_NAME, 1));
        pkgs.add(String.format(PKG_FORMAT, PKG_NAME, 2));
        scopes.add(ClassIndex.createPackageSearchScope(ClassIndex.SearchScope.SOURCE, pkgs.toArray(new String[0])));
        index.getDeclaredElements(
                "", //NOI18N
                ClassIndex.NameKind.PREFIX,
                scopes,
                null,
                Identity.<Document>getInstance(),
                res);
        assertEquals(pkgs.size() * CLZ_IN_PKG_COUNT, res.size());
        res.clear();
    }

    private static void fakeIndex(
            @NonNull final URL root) throws IOException {
        TransactionContext.beginTrans().register(
                ClassIndexEventsTransaction.class,
                ClassIndexEventsTransaction.create(true, ()->true));
        try {
            ClassIndexManager.getDefault().removeRoot(root);
        } finally {
            TransactionContext.get().commit();
        }
        final File indexFolder = new File(JavaIndex.getIndex(root), "refs");    //NOI18N
        final Index index = IndexManager.createIndex(indexFolder, DocumentUtil.createAnalyzer());
        try {
            final List<Pair<Pair<BinaryName,String>,Object[]>> docs = new ArrayList<>(PKG_COUNT * CLZ_IN_PKG_COUNT);
            for (int i = 0; i < PKG_COUNT; i++) {
                for (int j = 0; j < CLZ_IN_PKG_COUNT; j++ ) {
                    final Pair<BinaryName,String> name = Pair.of(BinaryName.create(String.format(
                            BIN_FORMAT,
                            PKG_NAME,
                            i,
                            CLZ_NAME,
                            j),
                            ElementKind.CLASS),
                        null);
                    final Object[] usagesData = new Object[] {
                        Collections.emptyList(),
                        null,
                        null
                    };
                    docs.add(Pair.of(name, usagesData));
                }
            }
            index.store(
                    docs,
                    Collections.<Query>emptySet(),
                    DocumentUtil.documentConvertor(),
                    Identity.<Query>getInstance(),
                    true);
        } finally {
            index.close();
        }
    }

    private static final class Identity<T> implements Convertor<T, T> {
        private static final Identity<?> INSTANCE = new Identity<>();

        @Override
        public T convert(T p) {
            return p;
        }

        @SuppressWarnings("unchecked")
        static <T> Identity<T> getInstance() {
            return (Identity<T>) INSTANCE;
        }
    };
}
