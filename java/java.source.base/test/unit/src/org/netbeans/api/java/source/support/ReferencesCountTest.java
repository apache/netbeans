/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.java.source.support;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.DocumentUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class ReferencesCountTest extends NbTestCase {
    
    private FileObject srcRoot;
    
    public ReferencesCountTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();        
        MockServices.setServices(CPPImpl.class,FEQImpl.class, SLQImpl.class, PRImpl.class);
        FileUtil.setMIMEType(JavaDataLoader.JAVA_EXTENSION, JavaDataLoader.JAVA_MIME_TYPE);
        final File wd = getWorkDir();
        final FileObject cache = FileUtil.createFolder(new File(wd,"cache"));   //NOI18N
        CacheFolder.setCacheFolder(cache);
        srcRoot = FileUtil.createFolder(new File(wd,"src"));   //NOI18N
        Lookup.getDefault().lookup(CPPImpl.class).setRoot(srcRoot);
        FEQImpl.interestedIn = srcRoot;
        SLQImpl.interestedIn = srcRoot;
        createSource(
            srcRoot,
            "test.Foo",                     //NOI18N
            "package test;\n"+              //NOI18N
            "class Foo {\n"+                //NOI18N
            "    String toString(java.util.List l, java.util.Comparator c){\n"+ //NOI18N
            "        return null;\n"+   //NOI18N
            "    }\n"+  //NOI18N
            "};");  //NOI18N
        createSource(
            srcRoot,
            "test.Bar",                     //NOI18N
            "package test;\n"+              //NOI18N
            "class Bar {\n"+                //NOI18N
            "    String toString(){\n"+ //NOI18N
            "        return null;\n"+   //NOI18N
            "    }\n"+  //NOI18N
            "};");  //NOI18N
        IndexingManager.getDefault().refreshIndexAndWait(srcRoot.toURL(), null, true);
    }
    
    

    public void testGetTypeReferenceCount() throws Exception {
        final ClassIndexImpl ciImpl = ClassIndexManager.getDefault().getUsagesQuery(srcRoot.toURL(), false);
        assertNotNull(ciImpl);
        final Set<ElementHandle<TypeElement>> res = new HashSet<ElementHandle<TypeElement>>();
        ciImpl.getDeclaredElements(
            "", //NOI18N
            ClassIndex.NameKind.PREFIX,
            EnumSet.of(ClassIndex.SearchScope.SOURCE),
            DocumentUtil.declaredTypesFieldSelector(false, false),
            DocumentUtil.typeElementConvertor(),
            res);
        assertEquals(2, res.size());
        final ReferencesCount rc = ReferencesCount.get(srcRoot.toURL());
        assertNotNull(rc);
        final Map<String,Integer> expectedUsedTypes = new HashMap<String,Integer>() {
            {
             put("java.lang.Object",2);         //NOI18N
             put("java.lang.String",2);         //NOI18N
             put("java.util.Comparator",1);     //NOI18N
             put("java.util.List",1);           //NOI18N
             put("test.Bar",1);                 //NOI18N
             put("test.Foo",1);                 //NOI18N
            }
        };
        final Iterable<? extends ElementHandle<? extends TypeElement>> types = rc.getUsedTypes();
        final Set<String> usedTypes = new HashSet<String>();
        for (ElementHandle<? extends TypeElement> type : types) {
            usedTypes.add(type.getBinaryName());
        }
        assertEquals(expectedUsedTypes.keySet(), usedTypes);
        for (ElementHandle<? extends TypeElement> type : types) {
            assertEquals(expectedUsedTypes.get(type.getBinaryName()),Integer.valueOf(rc.getTypeReferenceCount(type)));
        }
    }
    
    public void testGetPackageReferenceCount() throws Exception {
        final ClassIndexImpl ciImpl = ClassIndexManager.getDefault().getUsagesQuery(srcRoot.toURL(), false);
        assertNotNull(ciImpl);
        final Set<ElementHandle<TypeElement>> res = new HashSet<ElementHandle<TypeElement>>();
        ciImpl.getDeclaredElements(
            "", //NOI18N
            ClassIndex.NameKind.PREFIX,
            EnumSet.of(ClassIndex.SearchScope.SOURCE),
            DocumentUtil.declaredTypesFieldSelector(false, false),
            DocumentUtil.typeElementConvertor(),
            res);
        assertEquals(2, res.size());
        final ReferencesCount rc = ReferencesCount.get(srcRoot.toURL());
        assertNotNull(rc);
        final Map<String,Integer> expectedUsedPackages = new HashMap<String,Integer>() {
            {
             put("java.lang",4);         //NOI18N
             put("java.util",2);         //NOI18N
             put("test",2);              //NOI18N
            }
        };
        final Iterable<? extends ElementHandle<? extends PackageElement>> packages = rc.getUsedPackages();
        final Set<String> usedPackages = new HashSet<String>();
        for (ElementHandle<? extends PackageElement> pkg : packages) {
            usedPackages.add(SourceUtils.getJVMSignature(pkg)[0]);
        }
        assertEquals(expectedUsedPackages.keySet(), usedPackages);
        for (ElementHandle<? extends PackageElement> pkg : packages) {
            assertEquals(expectedUsedPackages.get(SourceUtils.getJVMSignature(pkg)[0]),Integer.valueOf(rc.getPackageReferenceCount(pkg)));
        }
    }
    
    private FileObject createSource(
            @NonNull final FileObject root,
            @NonNull final String fqn,
            @NonNull final String content) throws IOException {
        final FileObject file = FileUtil.createData(root, fqn.replace('.', '/')+"."+JavaDataLoader.JAVA_EXTENSION);   //NOI18N
        final FileLock lck = file.lock();
        try {
            final PrintWriter out = new PrintWriter(new OutputStreamWriter(file.getOutputStream(lck), StandardCharsets.UTF_8));
            try {
                out.print(content);
            } finally {
                out.close();
            }
        } finally {
            lck.releaseLock();
        }
        return file;
    }
    
    
    public static class FEQImpl extends FileEncodingQueryImplementation {
        
        static volatile FileObject interestedIn;

        @Override
        public Charset getEncoding(FileObject file) {
            if (file == interestedIn || FileUtil.isParentOf(interestedIn, file)) {
                return StandardCharsets.UTF_8;
            }
            return null;
        }
    }
    
    public static class SLQImpl implements SourceLevelQueryImplementation2 {
        
        static volatile FileObject interestedIn;
        
        private final SourceLevelQueryImplementation2.Result res = new SourceLevelQueryImplementation2.Result() {
            @Override
            public String getSourceLevel() {
                return "1.5";   //NOI18N
            }

            @Override
            public void addChangeListener(ChangeListener listener) {
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
            }
        };

        @Override
        public Result getSourceLevel(FileObject file) {
            if (file == interestedIn || FileUtil.isParentOf(interestedIn, file)) {
                return res;
            }
            return null;
        }
        
    }
    
    public static class CPPImpl implements ClassPathProvider {

        private volatile FileObject interestedIn;        
        private final AtomicReference<ClassPath> srcPath = new AtomicReference<ClassPath>();
        private final AtomicReference<ClassPath> cmpPath = new AtomicReference<ClassPath>();
        private final AtomicReference<ClassPath> bootPath = new AtomicReference<ClassPath>();
        
        void setRoot(@NonNull final FileObject fo) {
            this.interestedIn = fo;
            srcPath.set(null);
        }
        
        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            if (file == interestedIn || FileUtil.isParentOf(interestedIn, file)) {
                if (ClassPath.SOURCE.equals(type)) {
                    return getSrcPath();
                }
                if (ClassPath.COMPILE.equals(type)) {
                    return getCompilePath();
                }
                if (ClassPath.BOOT.equals(type)) {
                    return getBootPath();
                }
            }
            return null;
        }
        
        private ClassPath getSrcPath() {
            ClassPath res =  srcPath.get();
            if (res == null) {
                res = ClassPathSupport.createClassPath(interestedIn);
                if (!srcPath.compareAndSet(null, res)) {
                    res = srcPath.get();
                }
            }
            return res;
        }
        
        private ClassPath getCompilePath() {
            ClassPath res =  cmpPath.get();
            if (res == null) {
                res = ClassPathSupport.createClassPath(new URL[0]);
                if (!cmpPath.compareAndSet(null, res)) {
                    res = cmpPath.get();
                }
            }
            return res;
        }
        
        private ClassPath getBootPath() {
            ClassPath res =  bootPath.get();
            if (res == null) {
                res = BootClassPathUtil.getBootClassPath();  //NOI18N
                if (!bootPath.compareAndSet(null, res)) {
                    res = bootPath.get();
                }
            }
            return res;
        }   
    }
    
    public static class PRImpl extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.<String>singleton(ClassPath.SOURCE);
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return Collections.<String>emptySet();
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return Collections.unmodifiableSet(
                    new HashSet<String>(Arrays.asList(ClassPath.BOOT, ClassPath.COMPILE)));
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.<String>singleton(JavaDataLoader.JAVA_MIME_TYPE);
        }
        
    }
}
