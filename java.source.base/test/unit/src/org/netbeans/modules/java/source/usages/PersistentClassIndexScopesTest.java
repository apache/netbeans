/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
        scopes.add(ClassIndex.createPackageSearchScope(
            ClassIndex.SearchScope.SOURCE,
            pkgs.toArray(new String[pkgs.size()])));
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
        scopes.add(ClassIndex.createPackageSearchScope(
            ClassIndex.SearchScope.SOURCE,
            pkgs.toArray(new String[pkgs.size()])));
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
        scopes.add(ClassIndex.createPackageSearchScope(
            ClassIndex.SearchScope.SOURCE,
            pkgs.toArray(new String[pkgs.size()])));
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
                ClassIndexEventsTransaction.create(true));
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
