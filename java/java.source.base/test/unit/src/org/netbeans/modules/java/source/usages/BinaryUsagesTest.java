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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.lang.model.element.ElementKind;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author Tomas Zezula
 */
public class BinaryUsagesTest extends NbTestCase {

        private static final String MIME_JAVA = "text/x-java";  //NOI18N
        private static final String MIME_CLASS = "application/x-class-file";  //NOI18N

        private LocalFileSystem lfs;
        private FileObject cache;
        private FileObject src;
        private FileObject java;
        private FileObject clz;

        public BinaryUsagesTest(final String name) {
            super(name);
        }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockServices.setServices(OrderedURLMapper.class, CPProvider.class, SLQ.class);
        MockMimeLookup.setInstances(
                MimePath.get(MIME_JAVA),    //NOI18N
                new JavaCustomIndexer.Factory(),
                new JavacParserFactory());
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        cache = FileUtil.createFolder(wd, "cache"); //NOI18N
        final File srcDir = FileUtil.normalizeFile(new File(getWorkDir(),"src"));
        srcDir.mkdirs();
        File[] fos = prepareContent(srcDir);
        lfs = new OrderedFS(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                final int e1 = rank(FileObjects.getExtension(o1));
                final int e2 = rank(FileObjects.getExtension(o2));
                int res = e1 - e2;
                if (res == 0) {
                    res = o1.compareTo(o2);
                }
                return res;
            }

            private int rank(String ext) {
                switch (ext) {
                    case FileObjects.CLASS:
                        return 0;
                    case FileObjects.JAVA:
                        return 1;
                    default:
                        return 2;
                }
            }
        });
        lfs.setRootDirectory(srcDir);
        Repository.getDefault().addFileSystem(lfs);
        src = lfs.getRoot();
        CacheFolder.setCacheFolder(cache);
        java = FileUtil.toFileObject(fos[0]);
        clz = FileUtil.toFileObject(fos[1]);
        FileUtil.setMIMEType(FileObjects.CLASS, MIME_CLASS);
        FileUtil.setMIMEType(FileObjects.JAVA, MIME_JAVA);
        Lookup.getDefault().lookup(CPProvider.class).configure(src);
        Lookup.getDefault().lookup(SLQ.class).configure(src);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Repository.getDefault().removeFileSystem(lfs);
    }

    public void testMixed() throws Exception {
        FileObject fo = URLMapper.findFileObject(src.toURL());
        IndexingManager.getDefault().refreshIndexAndWait(src.toURL(), null, true);
        final ClasspathInfo cpInfo = ClasspathInfo.create(src);
        final Collection<? extends FileObject> res = cpInfo.getClassIndex().getResources(
                ElementHandleAccessor.getInstance().create(ElementKind.CLASS, "org.me.Foo"),    //NOI18N
                EnumSet.allOf(ClassIndex.SearchKind.class),
                EnumSet.of(ClassIndex.SearchScope.SOURCE),
                EnumSet.of(ClassIndex.ResourceType.SOURCE));
        assertEquals(1, res.size());
        assertEquals(java, res.iterator().next());

    }

    private static File[] prepareContent(final File src) throws IOException {
        final File java = TestFileUtils.writeFile(
                FileUtil.normalizeFile(new File(src,"org/me/Foo.java")),
                "package org.me;\n" +                                            //NOI18N
                "public class Foo {}");                                         //NOI18N
        final File clz = TestFileUtils.writeFile(
                FileUtil.normalizeFile(new File(src,"org/me/Foo.class")),    //NOI18N
                "");    //NOI18N
        return new File[]{java, clz};
    }

    public static final class CPProvider implements ClassPathProvider {

        private final AtomicReferenceArray<ClassPath> cache = new AtomicReferenceArray<>(3);
        private volatile FileObject srcRoot;

        void configure(@NullAllowed final FileObject srcRoot) {
            for (int i=0; i< cache.length(); i++) {
                cache.set(i, null);
            }
            this.srcRoot = srcRoot;
        }

        @Override
        @CheckForNull
        public ClassPath findClassPath(FileObject file, String type) {
            final FileObject root = srcRoot;
            if (root != null && (root.equals(file) || FileUtil.isParentOf(root, file))) {
                switch (type) {
                    case ClassPath.BOOT:
                        return getBootCp();
                    case ClassPath.SOURCE:
                        return getSrcCp(root);
                    case ClassPath.COMPILE:
                        return getCompileCp();
                }
            }
            return null;
        }

        @NonNull
        private ClassPath getBootCp() {
            ClassPath cp = cache.get(0);
            if (cp == null) {
                cp = BootClassPathUtil.getBootClassPath();
                if (!cache.compareAndSet(0, null, cp)) {
                    cp = cache.get(0);
                }
            }
            return cp;
        }

        @NonNull
        private ClassPath getSrcCp(@NonNull final FileObject root) {
            ClassPath cp = cache.get(1);
            if (cp == null) {
                cp = ClassPathSupport.createClassPath(root);
                if (!cache.compareAndSet(1, null, cp)) {
                    cp = cache.get(1);
                }
            }
            return cp;
        }


        @NonNull
        private ClassPath getCompileCp() {
            ClassPath cp = cache.get(2);
            if (cp == null) {
                cp = ClassPath.EMPTY;
                if (!cache.compareAndSet(2, null, cp)) {
                    cp = cache.get(2);
                }
            }
            return cp;
        }
    }

    public static final class SLQ implements SourceLevelQueryImplementation2 {

        private final AtomicReference<R> cache = new AtomicReference<>();
        private volatile FileObject srcRoot;

        void configure(@NullAllowed final FileObject srcRoot) {
            cache.set(null);
            this.srcRoot = srcRoot;
        }

        @Override
        public Result getSourceLevel(FileObject javaFile) {
            final FileObject root = srcRoot;
            R r  = null;
            if (root != null && (root.equals(javaFile) || FileUtil.isParentOf(root, javaFile))) {
                r = cache.get();
                if (r == null) {
                    r = new R();
                    if (!cache.compareAndSet(null, r)) {
                        r = cache.get();
                    }
                }
            }
            return r;
        }

        private static final class R implements Result {

                @Override
                public String getSourceLevel() {
                    return "1.6";   //NOI18N
                }

                @Override
                public void addChangeListener(ChangeListener listener) {
                }

                @Override
                public void removeChangeListener(ChangeListener listener) {
                }
        }
    }

    public static final class OrderedURLMapper extends URLMapper {

        @CheckForNull
        public FileObject[] getFileObjects(@NonNull final URL url) {
            final String prot = url.getProtocol();
            if (prot.equals("file")) { //NOI18N
                File f = toFile(url);
                if (f != null) {
                    FileObject[] foRes = findFileObjectsInRepository(f);
                    if ((foRes != null) && (foRes.length > 0)) {
                        return foRes;
                    }
                }
            }
            return null;
        }

        private FileObject[] findFileObjectsInRepository(File f) {
            if (!f.equals(FileUtil.normalizeFile(f))) {
                throw new IllegalArgumentException(
                    "Parameter file was not " + // NOI18N
                    "normalized. Was " + f + " instead of " + FileUtil.normalizeFile(f)
                ); // NOI18N
            }

            @SuppressWarnings("deprecation") // keep for backward compatibility w/ NB 3.x
            Enumeration<? extends FileSystem> en = Repository.getDefault().getFileSystems();
            List<FileObject> list = new LinkedList<FileObject>();
            String fileName = f.getAbsolutePath();

            while (en.hasMoreElements()) {
                FileSystem fs = en.nextElement();
                String rootName = null;
                FileObject fsRoot = fs.getRoot();
                File root = findFileInRepository(fsRoot);

                if (root == null) {
                    Object rootPath = fsRoot.getAttribute("FileSystem.rootPath"); //NOI18N

                    if (rootPath instanceof String) {
                        rootName = (String) rootPath;
                    } else {
                        continue;
                    }
                }

                if (rootName == null) {
                    rootName = root.getAbsolutePath();
                }

                /**root is parent of file*/
                if (fileName.indexOf(rootName) == 0) {
                    String res = fileName.substring(rootName.length()).replace(File.separatorChar, '/');
                    FileObject fo = fs.findResource(res);
                    File file2Fo = (fo != null) ? findFileInRepository(fo) : null;
                    if ((fo != null) && (file2Fo != null) && f.equals(file2Fo)) {
                        list.add(fo);
                    }
                }
            }
            FileObject[] results = new FileObject[list.size()];
            list.toArray(results);
            return results;
        }

        // implements  URLMapper.getURL(FileObject fo, int type)
        public URL getURL(FileObject fo, int type) {
            return null;
        }

        private static File findFileInRepository(FileObject fo) {
            File f = (File) fo.getAttribute("java.io.File"); // NOI18N

            return (f != null) ? FileUtil.normalizeFile(f) : null;
        }

        private static File toFile(URL u) {
            if (u == null) {
                throw new NullPointerException();
            }
            try {
                URI uri = new URI(u.toExternalForm());
                return FileUtil.normalizeFile(BaseUtilities.toFile(uri));
            } catch (URISyntaxException | IllegalArgumentException use) {
                // malformed URL
                return null;
            }
        }
    }

    public static final class OrderedFS extends LocalFileSystem {
        private final Comparator<? super String> comparator;

        OrderedFS(@NonNull final Comparator<? super String> comparator) {
            assert comparator != null;
            this.comparator = comparator;
        }

        @Override
        protected String[] children(@NonNull final String name) {
            String[] children = super.children(name);
            Arrays.sort(children, comparator);
            return children;
        }
    }

}
