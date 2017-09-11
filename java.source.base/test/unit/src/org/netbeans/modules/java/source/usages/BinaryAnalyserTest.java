/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.usages;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.apache.lucene.search.BooleanClause.Occur;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.BinaryAnalyser.Changes;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcherAccessor;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Lahoda
 * @author Tomas Zezula
 */
public class BinaryAnalyserTest extends NbTestCase {

    private static final boolean RUN_PERF_TESTS = true; //Boolean.getBoolean("BinaryAnalyserTest.perf");
    private static final int QUERY_ROUNDS = 100;

    private int flushCount = 0;

    public BinaryAnalyserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.setLookup(new Object[] {new MockCfg()}, getClass().getClassLoader());
        clearWorkDir();
    }


    @Override
    protected void tearDown() throws Exception {
        requireFullIndex(false);
        LowMemoryWatcherAccessor.setLowMemory(false);
        super.tearDown();
    }

    public void testAnnotationsIndexed() throws Exception {
        requireFullIndex(true);
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject indexDir = workDir.createFolder("index");
        File binaryAnalyzerDataDir = new File(getDataDir(), "Annotations.jar");

        final Index index = IndexManager.createIndex(FileUtil.toFile(indexDir), DocumentUtil.createAnalyzer());
        BinaryAnalyser a = new BinaryAnalyser(new IndexWriter(index), getWorkDir());

        assertTrue(a.analyse(FileUtil.getArchiveRoot(Utilities.toURI(binaryAnalyzerDataDir).toURL())).done);
        assertReference(index, "annotations.NoArgAnnotation", "usages.ClassAnnotations", "usages.MethodAnnotations", "usages.FieldAnnotations");
        assertReference(index, "annotations.ArrayOfStringArgAnnotation", "usages.ClassAnnotations", "usages.ClassArrayAnnotations", "usages.MethodAnnotations", "usages.MethodArrayAnnotations", "usages.FieldAnnotations", "usages.FieldArrayAnnotations");
        assertReference(index, "annotations.TestEnum", "usages.ClassAnnotations", "usages.ClassArrayAnnotations", "usages.MethodAnnotations", "usages.MethodArrayAnnotations", "usages.FieldAnnotations", "usages.FieldArrayAnnotations");
        assertReference(index, "java.util.List", "usages.ClassAnnotations", "usages.ClassArrayAnnotations", "usages.MethodAnnotations", "usages.MethodArrayAnnotations", "usages.FieldAnnotations", "usages.FieldArrayAnnotations");
    }

    public void testDeleteClassFolderContent() throws Exception {
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject indexDir = workDir.createFolder("index");
        File jar = new File(getDataDir(), "Annotations.jar");
        FileObject classFolderFO = workDir.createFolder("classes");
        File classFolder = FileUtil.toFile(classFolderFO);

        assertNotNull(classFolder);

        unzip(jar, classFolder);

        final Index index = IndexManager.createIndex(FileUtil.toFile(indexDir), DocumentUtil.createAnalyzer());
        BinaryAnalyser a = new BinaryAnalyser(new IndexWriter(index), getWorkDir());

        assertTrue(a.analyse(Utilities.toURI(classFolder).toURL()).done);

        Set<String> origClasses = listClasses(index);

        assertTrue(origClasses.toString(), !origClasses.isEmpty());

        for (File c : classFolder.listFiles()) {
            delete(c);
        }

        a = new BinaryAnalyser(new IndexWriter(index), getWorkDir());

        final Changes changes = a.analyse(Utilities.toURI(classFolder).toURL());
        assertTrue(changes.done);

        Set<String> removedClasses = new HashSet<String>();

        for (ElementHandle<TypeElement> eh : changes.removed) {
            removedClasses.add(eh.getBinaryName());
        }

        assertEquals(origClasses, removedClasses);
        assertEquals(new HashSet<String>(), listClasses(index));
    }

    public void testDeleteClassFolder() throws Exception {
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject indexDir = workDir.createFolder("index");
        File jar = new File(getDataDir(), "Annotations.jar");
        FileObject classFolderFO = workDir.createFolder("classes");
        File classFolder = FileUtil.toFile(classFolderFO);

        assertNotNull(classFolder);

        unzip(jar, classFolder);

        final Index index = IndexManager.createIndex(FileUtil.toFile(indexDir), DocumentUtil.createAnalyzer());
        BinaryAnalyser a = new BinaryAnalyser(new IndexWriter(index), getWorkDir());

        assertTrue(a.analyse(Utilities.toURI(classFolder).toURL()).done);

        Set<String> origClasses = listClasses(index);

        assertTrue(origClasses.toString(), !origClasses.isEmpty());

        delete(classFolder);

        a = new BinaryAnalyser(new IndexWriter(index), getWorkDir());

        final Changes changes = a.analyse(Utilities.toURI(classFolder).toURL());
        assertTrue(changes.done);

        Set<String> removedClasses = new HashSet<String>();

        for (ElementHandle<TypeElement> eh : changes.removed) {
            removedClasses.add(eh.getBinaryName());
        }

        assertEquals(origClasses, removedClasses);
        assertEquals(new HashSet<String>(), listClasses(index));
    }

    public void testTransactionalFlush() throws Exception {
        requireFullIndex(true);
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject indexDir = workDir.createFolder("index");
        File binaryAnalyzerDataDir = new File(getDataDir(), "Annotations.jar");

        final Index index = IndexManager.createIndex(FileUtil.toFile(indexDir), DocumentUtil.createAnalyzer());
        BinaryAnalyser a = new BinaryAnalyser(
            new IndexWriter(index) {
                @Override
                public void deleteAndFlush(List<Pair<Pair<BinaryName, String>, Object[]>> refs, Set<Pair<String, String>> toDelete) throws IOException {
                    super.deleteAndFlush(refs, toDelete);
                try {
                    dataFlushed(index);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                }
            }, getWorkDir()
        );

        LowMemoryWatcherAccessor.setLowMemory(true);
        assertTrue(a.analyse(FileUtil.getArchiveRoot(Utilities.toURI(binaryAnalyzerDataDir).toURL())).done);
        // at least one flush occured.
        assertFalse(flushCount == 0);

        assertReference(index, "annotations.NoArgAnnotation", "usages.ClassAnnotations", "usages.MethodAnnotations", "usages.FieldAnnotations");
        assertReference(index, "annotations.ArrayOfStringArgAnnotation", "usages.ClassAnnotations", "usages.ClassArrayAnnotations", "usages.MethodAnnotations", "usages.MethodArrayAnnotations", "usages.FieldAnnotations", "usages.FieldArrayAnnotations");
        assertReference(index, "annotations.TestEnum", "usages.ClassAnnotations", "usages.ClassArrayAnnotations", "usages.MethodAnnotations", "usages.MethodArrayAnnotations", "usages.FieldAnnotations", "usages.FieldArrayAnnotations");
        assertReference(index, "java.util.List", "usages.ClassAnnotations", "usages.ClassArrayAnnotations", "usages.MethodAnnotations", "usages.MethodArrayAnnotations", "usages.FieldAnnotations", "usages.FieldArrayAnnotations");
    }

    /**
     * This method is eventually called from the middle of BinaryAnalyser work, after it flushes some data from memory. The method
     * must check & store information that the data is NOT visible to IndexReaders yet
     */
    private void dataFlushed(Index index) throws IOException, InterruptedException {
        Collection<String> names = new LinkedList<String>();
        // check using collected usages

        index.query(
                names,
                DocumentUtil.binaryNameConvertor(),
                DocumentUtil.declaredTypesFieldSelector(false, false),
                null,
                QueryUtil.createUsagesQuery("java.util.List", EnumSet.of(UsageType.TYPE_REFERENCE), Occur.SHOULD));
        names.retainAll(
                Arrays.asList(
                "usages.ClassAnnotations",
                "usages.ClassArrayAnnotations",
                "usages.MethodAnnotations",
                "usages.MethodArrayAnnotations",
                "usages.FieldAnnotations",
                "usages.FieldArrayAnnotations")
        );
        assertTrue(names.isEmpty());

        flushCount++;
    }


    private static class IndexWriter implements ClassIndexImpl.Writer {
        Index index;

        public IndexWriter(Index index) {
            this.index = index;
        }

        @Override
        public void clear() throws IOException {
            index.clear();
        }
        @Override
        public void deleteAndStore(List<Pair<Pair<BinaryName, String>, Object[]>> refs, Set<Pair<String, String>> toDelete) throws IOException {
            index.store(refs, toDelete, DocumentUtil.documentConvertor(), DocumentUtil.queryClassConvertor(),true);
        }
        @Override
        public void deleteAndFlush(List<Pair<Pair<BinaryName, String>, Object[]>> refs, Set<Pair<String, String>> toDelete) throws IOException {
            ((Index.Transactional)index).txStore(refs, toDelete, DocumentUtil.documentConvertor(), DocumentUtil.queryClassConvertor());
        }

        @Override
        public void commit() throws IOException {
            ((Index.Transactional)index).commit();
        }

        @Override
        public void rollback() throws IOException {
            ((Index.Transactional)index).rollback();
        }


    }

    public void testCRCDiff () throws Exception {
        final List<Pair<ElementHandle<TypeElement>,Long>> first = new ArrayList<Pair<ElementHandle<TypeElement>, Long>>();
        final List<Pair<ElementHandle<TypeElement>,Long>> second = new ArrayList<Pair<ElementHandle<TypeElement>, Long>>();
        BinaryAnalyser.Changes c = BinaryAnalyser.diff(first, second, false);
        assertTrue(c.added.isEmpty());
        assertTrue(c.removed.isEmpty());
        assertTrue(c.changed.isEmpty());
        first.add(create("test/afirst",10));
        first.add(create("test/bsecond",10));
        first.add(create("test/cthird",10));
        c = BinaryAnalyser.diff(first, first, false);
        assertTrue(c.added.isEmpty());
        assertTrue(c.removed.isEmpty());
        assertTrue(c.changed.isEmpty());
        c = BinaryAnalyser.diff(first, second, false);
        assertTrue(c.added.isEmpty());
        assertTrue(c.changed.isEmpty());
        assertEquals(3,c.removed.size());
        assertEquals((create("test/afirst", "test/bsecond", "test/cthird")), c.removed);
        c = BinaryAnalyser.diff(second, first, false);
        assertTrue(c.removed.isEmpty());
        assertTrue(c.changed.isEmpty());
        assertEquals(3,c.added.size());
        assertEquals((create("test/afirst", "test/bsecond", "test/cthird")), c.added);
        first.add(create("test/dfourth",10));
        second.add(create("test/bsecond",10));
        second.add(create("test/bsecond_and_half",10));
        second.add(create("test/cthird",10));
        second.add(create("test/efifth",10));
        second.add(create("test/fsixth",10));
        c = BinaryAnalyser.diff(first, second, false);
        assertTrue(c.changed.isEmpty());
        assertEquals(3,c.added.size());
        assertEquals((create("test/bsecond_and_half", "test/efifth", "test/fsixth")), c.added);
        assertEquals(2,c.removed.size());
        assertEquals((create("test/afirst", "test/dfourth")), c.removed);
        second.clear();
        second.add(create("test/afirst",10));
        second.add(create("test/bsecond",15));
        second.add(create("test/cthird",10));
        second.add(create("test/dfourth",15));
        c = BinaryAnalyser.diff(first, second, false);
        assertTrue(c.added.isEmpty());
        assertTrue(c.removed.isEmpty());
        assertEquals(2,c.changed.size());
        assertEquals((create("test/bsecond", "test/dfourth")), c.changed);
    }

    public void testFullIndexPerformance() throws Exception {
        final URL rtJar = findRtJar();
        if (RUN_PERF_TESTS && rtJar != null) {
            final File wd = FileUtil.normalizeFile(getWorkDir());

            //Warm up
            index(new File(wd, "wuIndex"), rtJar, false);    //NOI18N
            query(new File(wd, "wuIndex"), QUERY_ROUNDS);       //NOI18N

            //Partial index
            long[] res = index(new File(wd, "index"), rtJar, false);    //NOI18N
            final long indexSize = res[0];
            final long indexTime = res[1];
            final long indexQTime = query(new File(wd, "index"), QUERY_ROUNDS);       //NOI18N

            //Full index
            res = index(new File(wd, "fullIndex"), rtJar, true);    //NOI18N
            final long fullIndexSize = res[0];
            final long fullIndexTime = res[1];
            final long fullIndexQTime = query(new File(wd, "fullIndex"), QUERY_ROUNDS);       //NOI18N

            System.out.println("Index size: " + (indexSize>>>10) +"KB, FullIndex size: " + (fullIndexSize>>>10)+"KB.");   //NOI18N
            System.out.println("Index time: " + (indexTime/1_000_000) +"ms, FullIndex time: " + (fullIndexTime/1_000_000)+"ms.");   //NOI18N
            System.out.println("Query time: " + (indexQTime/1_000_000) +"ms, FullIndex time: " + (fullIndexQTime/1_000_000)+"ms.");   //NOI18N
        }
    }

    private Pair<ElementHandle<TypeElement>,Long> create (String name, long crc) {
        return Pair.<ElementHandle<TypeElement>,Long>of(ElementHandle.createTypeElementHandle(ElementKind.CLASS, name),crc);
    }

    private List<ElementHandle<TypeElement>> create (String... names) {
        List<ElementHandle<TypeElement>> result = new ArrayList<ElementHandle<TypeElement>>();
        for (String name : names) {
            result.add(ElementHandle.createTypeElementHandle(ElementKind.CLASS, name));
        }
        return result;
    }

    private void assertReference(Index index, String refered, String... in) throws IOException, InterruptedException {
        final Set<String> result = new HashSet<String>();
        index.query(
                result,
                DocumentUtil.binaryNameConvertor(),
                DocumentUtil.declaredTypesFieldSelector(false, false),
                null,
                QueryUtil.createUsagesQuery(refered, EnumSet.of(UsageType.TYPE_REFERENCE), Occur.SHOULD));
        assertTrue(result.containsAll(Arrays.asList(in)));
    }

    private void unzip(File what, File where) throws IOException {
        JarFile jf = new JarFile(what);
        Enumeration<JarEntry> en = jf.entries();

        while (en.hasMoreElements()) {
            JarEntry current = en.nextElement();
            if (current.isDirectory()) continue;
            File target = new File(where, current.getName());
            target.getParentFile().mkdirs();
            assertTrue(target.getParentFile().isDirectory());
            InputStream in = jf.getInputStream(current);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(target));

            FileUtil.copy(in, out);

            in.close();
            out.close();
        }
    }

    private void delete(File what) {
        File[] children = what.listFiles();

        if (children != null) {
            for (File c : children) {
                delete(c);
            }
        }

        assertTrue(what.delete());
    }

    private Set<String> listClasses(Index index) throws IOException, InterruptedException {
        final Set<String> result = new HashSet<String>();
        index.query(
                result,
                DocumentUtil.binaryNameConvertor(),
                DocumentUtil.declaredTypesFieldSelector(false, false),
                null,
                Queries.createQuery(
                DocumentUtil.FIELD_SIMPLE_NAME,
                DocumentUtil.FIELD_CASE_INSENSITIVE_NAME,
                "",
                DocumentUtil.translateQueryKind(NameKind.PREFIX)));
        return result;
    }

    private static void requireFullIndex(final boolean fullIndex) throws ReflectiveOperationException {
        final MockCfg cfg = Lookup.getDefault().lookup(MockCfg.class);
        cfg.usgLvl = fullIndex ? BinaryAnalyser.Config.UsagesLevel.ALL : BinaryAnalyser.Config.UsagesLevel.EXEC_VAR_REFS;
    }

    @CheckForNull
    private static URL findRtJar() {
        final String boot = System.getProperty("sun.boot.class.path");  //NOI18N
        if (boot != null) {
            for (String part : boot.split(File.pathSeparator)) {
                if (part.contains("rt.jar")) {  //NOI18N
                    final File rtJar = FileUtil.normalizeFile(new File(part));
                    try {
                        return FileUtil.getArchiveRoot(BaseUtilities.toURI(rtJar).toURL());
                    } catch (MalformedURLException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        }
        return null;
    }

    private static long dirSize(@NonNull final File directory) {
        long res = 0;
        final File[] children = directory.listFiles();
        if (children != null) {
            for (File c : children) {
                res += c.isDirectory() ? dirSize(c) : c.length();
            }
        }
        return res;
    }

    private static long[] index(
            @NonNull final File cacheFolder,
            @NonNull final URL binary,
            final boolean fullIndex) throws ReflectiveOperationException, IOException {
        requireFullIndex(fullIndex);
        final File refs = new File (cacheFolder, "refs"); //NOI18N
        refs.mkdirs();
        final long st = System.nanoTime();
        final Index index = IndexManager.createIndex(refs, DocumentUtil.createAnalyzer());
        try {
            final ClassIndexImpl.Writer writer = new IndexWriter(index);
            final BinaryAnalyser analyzer = new BinaryAnalyser(writer, cacheFolder);
            analyzer.analyse(binary);
        } finally {
            index.close();
        }
        final long et = System.nanoTime();
        return new long[] {
            dirSize(refs),
            et - st
        };
    }

    /**
     * Query - worst case
     */
    private static long query(
        @NonNull final File cacheFolder,
        final int rounds) throws IOException, InterruptedException {
        long time = 0;
        final File refs = new File (cacheFolder, "refs"); //NOI18N
        final List<String> res = new ArrayList<>();
        for (int i = 0; i< rounds; i++) {
            final long st = System.nanoTime();
            final Index index = IndexManager.createIndex(refs, DocumentUtil.createAnalyzer());
            index.query(
                res,
                DocumentUtil.binaryNameConvertor(),
                DocumentUtil.declaredTypesFieldSelector(false, false),
                null,
                Queries.createQuery("simpleName", "ciName", "", Queries.QueryKind.PREFIX)); //NOI18N
            index.close();
            time += System.nanoTime() - st;
            res.clear();
        }
        return time/rounds;
    }

    public static final class MockCfg extends BinaryAnalyser.Config {

        volatile UsagesLevel usgLvl = UsagesLevel.EXEC_VAR_REFS;
        volatile IdentLevel idLvl = IdentLevel.VISIBLE;

        @Override
        protected UsagesLevel getUsagesLevel() {
            return usgLvl;
        }

        @Override
        protected IdentLevel getIdentLevel() {
            return idLvl;
        }
    }
}
