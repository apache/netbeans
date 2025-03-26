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
package org.netbeans.modules.parsing.spi.indexing.support;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.indexing.IndexingTestBase;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdaterTest;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class QuerySupportTest extends IndexingTestBase {

    private final Map<String, Set<ClassPath>> registeredClasspaths = new HashMap<String, Set<ClassPath>>();

    public QuerySupportTest(String name) {
        super(name);
    }

    @Override
    protected void getAdditionalServices(List<Class> clazz) {
        super.getAdditionalServices(clazz);
        clazz.add(JavaLikePathRecognizer.class);
        clazz.add(ScriptingLikePathRecognizer.class);
        clazz.add(ClassPathProviderImpl.class);
        clazz.add(SourceForBinaryQueryImpl.class);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }

    @Override
    protected void tearDown() throws Exception {        
        for(String id : registeredClasspaths.keySet()) {
            final Set<ClassPath> classpaths = registeredClasspaths.get(id);
            GlobalPathRegistry.getDefault().unregister(id, classpaths.toArray(new ClassPath[0]));
        }
        registeredClasspaths.clear();
        ClassPathProviderImpl.register(Collections.<FileObject,Map<String,ClassPath>>emptyMap());
        SourceForBinaryQueryImpl.register(Collections.<URL,Pair<Boolean,List<FileObject>>>emptyMap());
        LowMemoryWatcher.getInstance().free();
        RepositoryUpdater.getDefault().waitUntilFinished(RepositoryUpdaterTest.TIME);
        super.tearDown();
    }



    public void testGetDependentRootsJavaLikeDependenciesAllProjectsOpened() throws IOException, InterruptedException {
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject src1 = FileUtil.createFolder(wd,"src1");   //NOI18N
        final URL build1 = FileUtil.urlForArchiveOrDir(new File (getWorkDir(),"build1"));//NOI18N
        final FileObject src2 = FileUtil.createFolder(wd,"src2");   //NOI18N
        final URL build2 = FileUtil.urlForArchiveOrDir(new File (getWorkDir(),"build2"));    //NOI18N
        final FileObject src3 = FileUtil.createFolder(wd,"src3");   //NOI18N
        final URL build3 = FileUtil.urlForArchiveOrDir(new File (getWorkDir(),"build3"));    //NOI18N
        final FileObject src4 = FileUtil.createFolder(wd,"src4");   //NOI18N
        final URL build4 = FileUtil.urlForArchiveOrDir(new File (getWorkDir(),"build4"));   //NOI18N

        final ClassPath src1Source = ClassPathSupport.createClassPath(src1);
        final ClassPath src2Source = ClassPathSupport.createClassPath(src2);
        final ClassPath src2Compile = ClassPathSupport.createClassPath(build1);
        final ClassPath src3Source = ClassPathSupport.createClassPath(src3);
        final ClassPath src3Compile = ClassPathSupport.createClassPath(build1);
        final ClassPath src4Source = ClassPathSupport.createClassPath(src4);
        final ClassPath src4Compile = ClassPathSupport.createClassPath(build2);

        final Map<URL,Pair<Boolean,List<FileObject>>> sfbq = new HashMap<URL, Pair<Boolean,List<FileObject>>>();
        sfbq.put(build1,Pair.of(true,Collections.singletonList(src1)));
        sfbq.put(build2,Pair.of(true,Collections.singletonList(src2)));
        sfbq.put(build3,Pair.of(true,Collections.singletonList(src3)));
        sfbq.put(build4,Pair.of(true,Collections.singletonList(src4)));
        SourceForBinaryQueryImpl.register(sfbq);


        final ClassPath boot = ClassPath.EMPTY;
        final Map<String,ClassPath> src1cps = new HashMap<String, ClassPath>();
        src1cps.put(JavaLikePathRecognizer.BOOT, boot);
        src1cps.put(JavaLikePathRecognizer.COMPILE, ClassPath.EMPTY);
        src1cps.put(JavaLikePathRecognizer.SRC, src1Source);
        final Map<String,ClassPath> src2cps = new HashMap<String, ClassPath>();
        src2cps.put(JavaLikePathRecognizer.BOOT, boot);
        src2cps.put(JavaLikePathRecognizer.COMPILE, src2Compile);
        src2cps.put(JavaLikePathRecognizer.SRC, src2Source);
        final Map<String,ClassPath> src3cps = new HashMap<String, ClassPath>();
        src3cps.put(JavaLikePathRecognizer.BOOT, boot);
        src3cps.put(JavaLikePathRecognizer.COMPILE, src3Compile);
        src3cps.put(JavaLikePathRecognizer.SRC, src3Source);
        final Map<String,ClassPath> src4cps = new HashMap<String, ClassPath>();
        src4cps.put(JavaLikePathRecognizer.BOOT, boot);
        src4cps.put(JavaLikePathRecognizer.COMPILE, src4Compile);
        src4cps.put(JavaLikePathRecognizer.SRC, src4Source);
        final Map<FileObject,Map<String,ClassPath>> cps = new HashMap<FileObject, Map<String, ClassPath>>();
        cps.put(src1,src1cps);
        cps.put(src2,src2cps);
        cps.put(src3,src3cps);
        cps.put(src4,src4cps);
        ClassPathProviderImpl.register(cps);

        globalPathRegistry_register(
                JavaLikePathRecognizer.BOOT,
                new ClassPath[]{
                    boot
                });
        globalPathRegistry_register(
                JavaLikePathRecognizer.COMPILE,
                new ClassPath[]{
                    src2Compile,
                    src3Compile,
                    src4Compile
                });
        globalPathRegistry_register(
                JavaLikePathRecognizer.SRC,
                new ClassPath[]{
                    src1Source,
                    src2Source,
                    src3Source,
                    src4Source
                });

        RepositoryUpdater.getDefault().waitUntilFinished(RepositoryUpdaterTest.TIME);
        assertEquals(
                IndexingController.getDefault().getRootDependencies().keySet().toString(),
                4,
                IndexingController.getDefault().getRootDependencies().size());

        assertEquals(new FileObject[] {src4}, QuerySupport.findDependentRoots(src4, true));
        assertEquals(new FileObject[] {src3}, QuerySupport.findDependentRoots(src3, true));
        assertEquals(new FileObject[] {src2, src4}, QuerySupport.findDependentRoots(src2, true));
        assertEquals(new FileObject[] {src1, src2, src3, src4}, QuerySupport.findDependentRoots(src1, true));


        assertEquals(new FileObject[] {src4}, QuerySupport.findDependentRoots(src4, false));
        assertEquals(new FileObject[] {src3}, QuerySupport.findDependentRoots(src3, false));
        assertEquals(new FileObject[] {src2, src4}, QuerySupport.findDependentRoots(src2, false));
        assertEquals(new FileObject[] {src1, src2, src3, src4}, QuerySupport.findDependentRoots(src1, false));
    }


    public void testGetDependentRootsJavaLikeDependenciesProjectsClosed() throws IOException, InterruptedException {
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject src1 = FileUtil.createFolder(wd,"src1");   //NOI18N
        final URL build1 = FileUtil.urlForArchiveOrDir(new File (getWorkDir(),"build1"));//NOI18N
        final FileObject src2 = FileUtil.createFolder(wd,"src2");   //NOI18N
        final URL build2 = FileUtil.urlForArchiveOrDir(new File (getWorkDir(),"build2"));    //NOI18N
        final FileObject src3 = FileUtil.createFolder(wd,"src3");   //NOI18N
        final URL build3 = FileUtil.urlForArchiveOrDir(new File (getWorkDir(),"build3"));    //NOI18N
        final FileObject src4 = FileUtil.createFolder(wd,"src4");   //NOI18N
        final URL build4 = FileUtil.urlForArchiveOrDir(new File (getWorkDir(),"build4"));   //NOI18N

        final ClassPath src1Source = ClassPathSupport.createClassPath(src1);
        final ClassPath src2Source = ClassPathSupport.createClassPath(src2);
        final ClassPath src2Compile = ClassPathSupport.createClassPath(build1);
        final ClassPath src3Source = ClassPathSupport.createClassPath(src3);
        final ClassPath src3Compile = ClassPathSupport.createClassPath(build1);
        final ClassPath src4Source = ClassPathSupport.createClassPath(src4);
        final ClassPath src4Compile = ClassPathSupport.createClassPath(build2);

        final Map<URL,Pair<Boolean,List<FileObject>>> sfbq = new HashMap<URL, Pair<Boolean,List<FileObject>>>();
        sfbq.put(build1,Pair.of(true,Collections.singletonList(src1)));
        sfbq.put(build2,Pair.of(true,Collections.singletonList(src2)));
        sfbq.put(build3,Pair.of(true,Collections.singletonList(src3)));
        sfbq.put(build4,Pair.of(true,Collections.singletonList(src4)));
        SourceForBinaryQueryImpl.register(sfbq);


        final ClassPath boot = ClassPath.EMPTY;
        final Map<String,ClassPath> src1cps = new HashMap<String, ClassPath>();
        src1cps.put(JavaLikePathRecognizer.BOOT, boot);
        src1cps.put(JavaLikePathRecognizer.COMPILE, ClassPath.EMPTY);
        src1cps.put(JavaLikePathRecognizer.SRC, src1Source);
        final Map<String,ClassPath> src2cps = new HashMap<String, ClassPath>();
        src2cps.put(JavaLikePathRecognizer.BOOT, boot);
        src2cps.put(JavaLikePathRecognizer.COMPILE, src2Compile);
        src2cps.put(JavaLikePathRecognizer.SRC, src2Source);
        final Map<String,ClassPath> src3cps = new HashMap<String, ClassPath>();
        src3cps.put(JavaLikePathRecognizer.BOOT, boot);
        src3cps.put(JavaLikePathRecognizer.COMPILE, src3Compile);
        src3cps.put(JavaLikePathRecognizer.SRC, src3Source);
        final Map<String,ClassPath> src4cps = new HashMap<String, ClassPath>();
        src4cps.put(JavaLikePathRecognizer.BOOT, boot);
        src4cps.put(JavaLikePathRecognizer.COMPILE, src4Compile);
        src4cps.put(JavaLikePathRecognizer.SRC, src4Source);
        final Map<FileObject,Map<String,ClassPath>> cps = new HashMap<FileObject, Map<String, ClassPath>>();
        cps.put(src1,src1cps);
        cps.put(src2,src2cps);
        cps.put(src3,src3cps);
        cps.put(src4,src4cps);
        ClassPathProviderImpl.register(cps);

        globalPathRegistry_register(
                JavaLikePathRecognizer.BOOT,
                new ClassPath[]{
                    boot
                });
        globalPathRegistry_register(
                JavaLikePathRecognizer.COMPILE,
                new ClassPath[]{
                    src4Compile
                });
        globalPathRegistry_register(
                JavaLikePathRecognizer.SRC,
                new ClassPath[]{
                    src4Source
                });

        RepositoryUpdater.getDefault().waitUntilFinished(RepositoryUpdaterTest.TIME);
        assertEquals(
                IndexingController.getDefault().getRootDependencies().keySet().toString(),
                3,
                IndexingController.getDefault().getRootDependencies().size());

        assertEquals(new FileObject[] {src4}, QuerySupport.findDependentRoots(src4, true));       
        assertEquals(new FileObject[] {src4}, QuerySupport.findDependentRoots(src2, true));
        assertEquals(new FileObject[] {src4}, QuerySupport.findDependentRoots(src1, true));


        assertEquals(new FileObject[] {src4}, QuerySupport.findDependentRoots(src4, false));
        assertEquals(new FileObject[] {src2, src4}, QuerySupport.findDependentRoots(src2, false));
        assertEquals(new FileObject[] {src1, src2, src4}, QuerySupport.findDependentRoots(src1, false));
    }


    public void testGetDependentRootsJavaLikeBinaryDependencies() throws IOException, InterruptedException {

        IndexingController.getDefault().getRootDependencies().keySet();

        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject bin1 = FileUtil.createFolder(wd, "bin1");  //NOI18N
        final FileObject bin1Sources = FileUtil.createFolder(wd, "bin1src");    //NOI18N
        final FileObject src1 = FileUtil.createFolder(wd,"src1");   //NOI18N
        final FileObject src2 = FileUtil.createFolder(wd,"src2");   //NOI18N
        final FileObject src3 = FileUtil.createFolder(wd,"src3");   //NOI18N

        final ClassPath src1Source = ClassPathSupport.createClassPath(src1);
        final ClassPath src1Compile = ClassPathSupport.createClassPath(bin1);
        final ClassPath src2Source = ClassPathSupport.createClassPath(src2);
        final ClassPath src2Compile = ClassPathSupport.createClassPath(bin1);
        final ClassPath src3Source = ClassPathSupport.createClassPath(src3);        

        final Map<URL,Pair<Boolean,List<FileObject>>> sfbq = new HashMap<URL, Pair<Boolean,List<FileObject>>>();
        sfbq.put(bin1.toURL(),Pair.of(false,Collections.singletonList(bin1Sources)));
        SourceForBinaryQueryImpl.register(sfbq);


        final ClassPath boot = ClassPath.EMPTY;
        final Map<String,ClassPath> src1cps = new HashMap<String, ClassPath>();
        src1cps.put(JavaLikePathRecognizer.BOOT, boot);
        src1cps.put(JavaLikePathRecognizer.COMPILE, src1Compile);
        src1cps.put(JavaLikePathRecognizer.SRC, src1Source);
        final Map<String,ClassPath> src2cps = new HashMap<String, ClassPath>();
        src2cps.put(JavaLikePathRecognizer.BOOT, boot);
        src2cps.put(JavaLikePathRecognizer.COMPILE, src2Compile);
        src2cps.put(JavaLikePathRecognizer.SRC, src2Source);
        final Map<String,ClassPath> src3cps = new HashMap<String, ClassPath>();
        src3cps.put(JavaLikePathRecognizer.BOOT, boot);
        src3cps.put(JavaLikePathRecognizer.COMPILE, ClassPath.EMPTY);
        src3cps.put(JavaLikePathRecognizer.SRC, src3Source);        
        final Map<FileObject,Map<String,ClassPath>> cps = new HashMap<FileObject, Map<String, ClassPath>>();
        cps.put(src1,src1cps);
        cps.put(src2,src2cps);
        cps.put(src3,src3cps);
        ClassPathProviderImpl.register(cps);

        globalPathRegistry_register(
                JavaLikePathRecognizer.BOOT,
                new ClassPath[]{
                    boot
                });
        globalPathRegistry_register(
                JavaLikePathRecognizer.COMPILE,
                new ClassPath[]{
                    src1Compile,
                    src2Compile                    
                });
        globalPathRegistry_register(
                JavaLikePathRecognizer.SRC,
                new ClassPath[]{
                    src1Source,
                    src2Source,
                    src3Source
                });

        RepositoryUpdater.getDefault().waitUntilFinished(RepositoryUpdaterTest.TIME);
        assertEquals(
                IndexingController.getDefault().getRootDependencies().keySet().toString(),
                3,
                IndexingController.getDefault().getRootDependencies().size());

        assertEquals(new FileObject[] {src1, src2}, QuerySupport.findDependentRoots(bin1Sources, true));
    }

    public void testGetDependentRootsJavaLikeDependenciesWithPeerRoots() throws IOException, InterruptedException {
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject src1a = FileUtil.createFolder(wd,"src1a");   //NOI18N
        final FileObject src1b = FileUtil.createFolder(wd,"src1b");   //NOI18N

        final ClassPath src1Source = ClassPathSupport.createClassPath(src1a, src1b);


        final ClassPath boot = ClassPath.EMPTY;
        final Map<String,ClassPath> src1cps = new HashMap<String, ClassPath>();
        src1cps.put(JavaLikePathRecognizer.BOOT, boot);
        src1cps.put(JavaLikePathRecognizer.COMPILE, ClassPath.EMPTY);
        src1cps.put(JavaLikePathRecognizer.SRC, src1Source);        
        final Map<FileObject,Map<String,ClassPath>> cps = new HashMap<FileObject, Map<String, ClassPath>>();
        cps.put(src1a,src1cps);
        cps.put(src1b,src1cps);
        ClassPathProviderImpl.register(cps);

        globalPathRegistry_register(
                JavaLikePathRecognizer.BOOT,
                new ClassPath[]{
                    boot
                });        
        globalPathRegistry_register(
                JavaLikePathRecognizer.SRC,
                new ClassPath[]{
                    src1Source
                });

        RepositoryUpdater.getDefault().waitUntilFinished(RepositoryUpdaterTest.TIME);
        assertEquals(
                IndexingController.getDefault().getRootDependencies().keySet().toString(),
                2,
                IndexingController.getDefault().getRootDependencies().size());

        assertEquals(new FileObject[] {src1a, src1b}, QuerySupport.findDependentRoots(src1a, true));
        assertEquals(new FileObject[] {src1a, src1b}, QuerySupport.findDependentRoots(src1b, true));

        assertEquals(new FileObject[] {src1a, src1b}, QuerySupport.findDependentRoots(src1a, false));
        assertEquals(new FileObject[] {src1a, src1b}, QuerySupport.findDependentRoots(src1b, false));
    }

    public void testGetDependentRootsScriptingLikeDependenciesAllProjectsOpened() throws IOException, InterruptedException {
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject libSrc = FileUtil.createFolder(wd,"libSrc");   //NOI18N
        final FileObject src1 = FileUtil.createFolder(wd,"src1");   //NOI18N
        final FileObject src2 = FileUtil.createFolder(wd,"src2");   //NOI18N
        final FileObject src3 = FileUtil.createFolder(wd,"src3");   //NOI18N
        final FileObject src4 = FileUtil.createFolder(wd,"src4");   //NOI18N

        final ClassPath src1Source = ClassPathSupport.createClassPath(src1);
        final ClassPath src1Compile = ClassPathSupport.createClassPath(libSrc);
        final ClassPath src2Source = ClassPathSupport.createClassPath(src2);
        final ClassPath src2Compile = ClassPathSupport.createClassPath(src1);
        final ClassPath src3Source = ClassPathSupport.createClassPath(src3);
        final ClassPath src3Compile = ClassPathSupport.createClassPath(src1);
        final ClassPath src4Source = ClassPathSupport.createClassPath(src4);
        final ClassPath src4Compile = ClassPathSupport.createClassPath(src2);


        final Map<String,ClassPath> src1cps = new HashMap<String, ClassPath>();
        src1cps.put(ScriptingLikePathRecognizer.COMPILE, src1Compile);
        src1cps.put(ScriptingLikePathRecognizer.SRC, src1Source);
        final Map<String,ClassPath> src2cps = new HashMap<String, ClassPath>();
        src2cps.put(ScriptingLikePathRecognizer.COMPILE, src2Compile);
        src2cps.put(ScriptingLikePathRecognizer.SRC, src2Source);
        final Map<String,ClassPath> src3cps = new HashMap<String, ClassPath>();
        src3cps.put(ScriptingLikePathRecognizer.COMPILE, src3Compile);
        src3cps.put(ScriptingLikePathRecognizer.SRC, src3Source);
        final Map<String,ClassPath> src4cps = new HashMap<String, ClassPath>();
        src4cps.put(ScriptingLikePathRecognizer.COMPILE, src4Compile);
        src4cps.put(ScriptingLikePathRecognizer.SRC, src4Source);
        final Map<FileObject,Map<String,ClassPath>> cps = new HashMap<FileObject, Map<String, ClassPath>>();
        cps.put(src1,src1cps);
        cps.put(src2,src2cps);
        cps.put(src3,src3cps);
        cps.put(src4,src4cps);
        ClassPathProviderImpl.register(cps);

        globalPathRegistry_register(
                ScriptingLikePathRecognizer.COMPILE,
                new ClassPath[]{
                    src2Compile,
                    src3Compile,
                    src4Compile
                });
        globalPathRegistry_register(
                ScriptingLikePathRecognizer.SRC,
                new ClassPath[]{
                    src1Source,
                    src2Source,
                    src3Source,
                    src4Source
                });

        RepositoryUpdater.getDefault().waitUntilFinished(RepositoryUpdaterTest.TIME);
        assertEquals(
                IndexingController.getDefault().getRootDependencies().keySet().toString(),
                5,
                IndexingController.getDefault().getRootDependencies().size());

        assertEquals(new FileObject[] {src4}, QuerySupport.findDependentRoots(src4, true));
        assertEquals(new FileObject[] {src3}, QuerySupport.findDependentRoots(src3, true));
        assertEquals(new FileObject[] {src2, src4}, QuerySupport.findDependentRoots(src2, true));
        assertEquals(new FileObject[] {src1, src2, src3, src4}, QuerySupport.findDependentRoots(src1, true));
        assertEquals(new FileObject[] {src1, src2, src3, src4}, QuerySupport.findDependentRoots(libSrc, true));

        assertEquals(new FileObject[] {src4}, QuerySupport.findDependentRoots(src4, false));
        assertEquals(new FileObject[] {src3}, QuerySupport.findDependentRoots(src3, false));
        assertEquals(new FileObject[] {src2, src4}, QuerySupport.findDependentRoots(src2, false));
        assertEquals(new FileObject[] {src1, src2, src3, src4}, QuerySupport.findDependentRoots(src1, false));
        assertEquals(new FileObject[] {libSrc, src1, src2, src3, src4}, QuerySupport.findDependentRoots(libSrc, false));
    }

    private void assertEquals(FileObject[] expected, Collection<FileObject> res) {
        final Set<FileObject> set = new HashSet<FileObject>();
        Collections.addAll(set, expected);
        for (FileObject fo : res) {
            assertTrue(
                String.format("expected: %s res: %s", Arrays.toString(expected), res),  //NOI18N
                set.remove(fo));
        }
        assertTrue(
            String.format("expected: %s res: %s", Arrays.toString(expected), res),      //NOI8N
            set.isEmpty());
    }

    private void globalPathRegistry_register(String id, ClassPath [] classpaths) {
        Set<ClassPath> set = registeredClasspaths.get(id);
        if (set == null) {
            set = Collections.newSetFromMap(new IdentityHashMap<ClassPath, Boolean>());
            registeredClasspaths.put(id, set);
        }
        for (ClassPath cp :  classpaths) {
            set.add(cp);
        }
        GlobalPathRegistry.getDefault().register(id, classpaths);
    }    

    public static final class JavaLikePathRecognizer extends PathRecognizer {

        public static final String SRC = "source";          //NOI18N
        public static final String COMPILE = "compile";     //NOI18N
        public static final String BOOT = "boot";           //NOI18N
        public static final String MIME_JAVA = "text/x-java";   //NOI18N

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(SRC);
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return new HashSet<String>(Arrays.asList(BOOT, COMPILE));
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.singleton(MIME_JAVA);
        }

    }

    public static final class ScriptingLikePathRecognizer extends PathRecognizer {

        public static final String SRC = "scripts";          //NOI18N
        public static final String COMPILE = "included";     //NOI18N
        public static final String MIME_JAVA = "text/x-script";   //NOI18N

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(SRC);
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return Collections.singleton(COMPILE);
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.singleton(MIME_JAVA);
        }

    }

    public static final class ClassPathProviderImpl implements ClassPathProvider {

        private static volatile Map<FileObject,Map<String,ClassPath>> cps = Collections.emptyMap();

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            for (Map.Entry<FileObject,Map<String,ClassPath>> e : cps.entrySet()) {
                if (e.getKey().equals(file) || FileUtil.isParentOf(e.getKey(), file)) {
                    return e.getValue().get(type);
                }
            }
            return null;
        }

        static void register(Map<FileObject,Map<String,ClassPath>> newClassPaths) {
            cps = newClassPaths;
        }

    }

    public static final class SourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation2 {

        private static volatile Map<URL, Pair<Boolean,List<FileObject>>> mapping = Collections.emptyMap();

        @Override
        public Result findSourceRoots2(final URL binaryRoot) {
            final Pair<Boolean,List<FileObject>> sources = mapping.get(binaryRoot);
            if (sources == null) {
                return null;
            }
            return new Result() {
                @Override
                public boolean preferSources() {
                    return sources.first();
                }

                @Override
                public FileObject[] getRoots() {
                    return sources.second().toArray(new FileObject[sources.second().size()]);
                }

                @Override
                public void addChangeListener(ChangeListener l) {
                }

                @Override
                public void removeChangeListener(ChangeListener l) {
                }
            };
        }

        @Override
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            return findSourceRoots2(binaryRoot);
        }

        static void register(Map<URL,Pair<Boolean,List<FileObject>>> newMapping) {
            mapping = newMapping;
        }

    }    

}
