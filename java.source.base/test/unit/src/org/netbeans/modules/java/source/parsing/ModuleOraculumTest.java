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
package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.util.Options;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.usages.ClassIndexEventsTransaction;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class ModuleOraculumTest extends NbTestCase {

    private FileObject root1;
    private FileObject moduleFile1;
    private FileObject javaFile1;
    private FileObject root2;
    private FileObject moduleFile2;
    private FileObject javaFile2;

    public ModuleOraculumTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        root1 = FileUtil.createFolder(wd, "root1");
        moduleFile1 = createModule(root1, "Test");  //NOI18N
        javaFile1 = createClass(root1, "org.nb.Test");  //NOI18N
        root2 = FileUtil.createFolder(wd, "root2");
        moduleFile2 = createModule(root2, "Next");  //NOI18N
        javaFile2 = createClass(root2, "org.nb.Next");  //NOI18N
        MockServices.setServices(CPP.class, COQ.class);
        FileUtil.setMIMEType(FileObjects.JAVA, JavacParser.MIME_TYPE);
    }

    public void testOraculumLibrarySourceWithRoot() {
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile1,
                root1,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertEquals("Test", opts.get("-XD-Xmodule:"));    //NOI18N
    }

    public void testOraculumLibrarySourceWithoutRootWithSourcePath() {
        Lookup.getDefault().lookup(CPP.class).add(
                root1,
                ClassPath.SOURCE,
                ClassPathSupport.createClassPath(root1));
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile1,
                null,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertEquals("Test", opts.get("-XD-Xmodule:"));    //NOI18N
    }

    public void testOraculumLibrarySourceWithoutRootWithoutSourcePath() {
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile1,
                null,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertEquals("Test", opts.get("-XD-Xmodule:"));    //NOI18N
    }

    public void testOraculumLibrarySourceNoModuleInfo() throws IOException {
        moduleFile1.delete();
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile1,
                root1,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertNull(opts.get("-XD-Xmodule:"));    //NOI18N
    }

    public void testOraculumProjectSource() throws IOException {
        scan(root1);
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile1,
                root1,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertNull(opts.get("-XD-Xmodule:"));    //NOI18N
    }

    public void testOraculumLibrarySourceWithRootExpliciteXModule() throws IOException {
        Lookup.getDefault().lookup(COQ.class)
                .forRoot(root1)
                .apply("-XD-Xmodule:SomeModule");  //NOI18N
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile1,
                root1,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertEquals("SomeModule", opts.get("-XD-Xmodule:"));    //NOI18N
    }


    public void testRootCache() {
        Lookup.getDefault().lookup(CPP.class)
                .add(
                        root1,
                        ClassPath.SOURCE,
                        ClassPathSupport.createClassPath(root1))
                .add(
                        root2,
                        ClassPath.SOURCE,
                        ClassPathSupport.createClassPath(root2));
        final Logger l = Logger.getLogger(ModuleOraculum.class.getName());
        final Level origLogLevel = l.getLevel();
        final H h = new H();
        l.setLevel(Level.FINE);
        l.addHandler(h);
        try {
            final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
            final JavacParser parser = new JavacParser(Collections.emptyList(), true);
            JavacTaskImpl impl = JavacParser.createJavacTask(
                    javaFile1,
                    null,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("Test", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            List<? extends FileObject> roots = h.getRoots();
            assertEquals(1, roots.size());
            assertEquals(root1, roots.get(0));
            h.reset();
            impl = JavacParser.createJavacTask(
                    javaFile1,
                    null,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("Test", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            roots = h.getRoots();
            assertEquals(0, roots.size());
            impl = JavacParser.createJavacTask(
                    javaFile2,
                    null,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("Next", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            roots = h.getRoots();
            assertEquals(1, roots.size());
            assertEquals(root2, roots.get(0));
        } finally {
            l.removeHandler(h);
            l.setLevel(origLogLevel);
        }
    }

    public void testModuleNameCache() {
        final Logger l = Logger.getLogger(ModuleOraculum.class.getName());
        final Level origLogLevel = l.getLevel();
        final H h = new H();
        l.setLevel(Level.FINE);
        l.addHandler(h);
        try {
            final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
            final JavacParser parser = new JavacParser(Collections.emptyList(), true);
            JavacTaskImpl impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("Test", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            List<? extends String> names = h.getModuleNames();
            assertEquals(1, names.size());
            assertEquals("Test", names.get(0)); //NOI18N
            h.reset();
            impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("Test", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            names = h.getModuleNames();
            assertEquals(0, names.size());
            impl = JavacParser.createJavacTask(
                    javaFile2,
                    root2,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("Next", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            names = h.getModuleNames();
            assertEquals(1, names.size());
            assertEquals("Next", names.get(0)); //NOI18N
        } finally {
            l.removeHandler(h);
            l.setLevel(origLogLevel);
        }
    }

    public void testModuleNameCache_ModuleInfoUpdated() throws IOException {
        final Logger l = Logger.getLogger(ModuleOraculum.class.getName());
        final Level origLogLevel = l.getLevel();
        final H h = new H();
        l.setLevel(Level.FINE);
        l.addHandler(h);
        try {
            final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
            final JavacParser parser = new JavacParser(Collections.emptyList(), true);
            JavacTaskImpl impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("Test", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            List<? extends String> names = h.getModuleNames();
            assertEquals(1, names.size());
            assertEquals("Test", names.get(0)); //NOI18N
            h.reset();
            createModule(root1, "TestUpdated");
            impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("TestUpdated", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            names = h.getModuleNames();
            assertEquals(1, names.size());
            assertEquals("TestUpdated", names.get(0)); //NOI18N
            h.reset();
            impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("TestUpdated", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            names = h.getModuleNames();
            assertEquals(0, names.size());
        } finally {
            l.removeHandler(h);
            l.setLevel(origLogLevel);
        }
    }

    public void testModuleNameCache_ModuleInfoDeleted() throws IOException {
        final Logger l = Logger.getLogger(ModuleOraculum.class.getName());
        final Level origLogLevel = l.getLevel();
        final H h = new H();
        l.setLevel(Level.FINE);
        l.addHandler(h);
        try {
            final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
            final JavacParser parser = new JavacParser(Collections.emptyList(), true);
            JavacTaskImpl impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("Test", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            List<? extends String> names = h.getModuleNames();
            assertEquals(1, names.size());
            assertEquals("Test", names.get(0)); //NOI18N
            h.reset();
            moduleFile1.delete();
            impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertNull(Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            names = h.getModuleNames();
            assertEquals(1, names.size());
            assertEquals(null, names.get(0));
            h.reset();
            impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertNull(Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            names = h.getModuleNames();
            assertEquals(0, names.size());
        } finally {
            l.removeHandler(h);
            l.setLevel(origLogLevel);
        }
    }

    public void testModuleNameCache_ModuleInfoCreated() throws IOException {
        final Logger l = Logger.getLogger(ModuleOraculum.class.getName());
        final Level origLogLevel = l.getLevel();
        final H h = new H();
        l.setLevel(Level.FINE);
        l.addHandler(h);
        try {
            moduleFile1.delete();
            final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
            final JavacParser parser = new JavacParser(Collections.emptyList(), true);
            JavacTaskImpl impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertNull(Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            List<? extends String> names = h.getModuleNames();
            assertEquals(1, names.size());
            assertNull(names.get(0));
            h.reset();
            createModule(root1, "TestNew");
            impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("TestNew", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            names = h.getModuleNames();
            assertEquals(1, names.size());
            assertEquals("TestNew", names.get(0));  //NOI18N
            h.reset();
            impl = JavacParser.createJavacTask(
                    javaFile1,
                    root1,
                    cpInfo,
                    parser,
                    null,
                    null,
                    false);
            assertEquals("TestNew", Options.instance(impl.getContext()).get("-XD-Xmodule:"));    //NOI18N
            names = h.getModuleNames();
            assertEquals(0, names.size());
        } finally {
            l.removeHandler(h);
            l.setLevel(origLogLevel);
        }
    }

    private static void scan(@NonNull final FileObject root) throws IOException {
        final TransactionContext ctx = TransactionContext.beginTrans()
                .register(ClassIndexEventsTransaction.class, ClassIndexEventsTransaction.create(true));
        try {
            ClassIndexImpl ci = ClassIndexManager.getDefault().createUsagesQuery(root.toURL(), true);
            if (ci == null) {
                throw new IllegalStateException();
            }
            ci.setState(ClassIndexImpl.State.INITIALIZED);
        } finally {
            ctx.commit();
        }
    }

    @NonNull
    private static FileObject createModule(
            @NonNull final FileObject root,
            @NonNull final String moduleName) throws IOException {
        final String content = String.format(
                "module %s {}", //NOI18N
                moduleName);
        return writeFile(
                root,
                "",
                "module-info.java",
                content);
    }

    @NonNull
    private static FileObject createClass(
            @NonNull final FileObject root,
            @NonNull final String clzName) throws IOException {
        final String[] pnp = FileObjects.getPackageAndName(clzName);
        final String content = String.format(
                "package %s;\nclass %s {}", //NOI18N
                pnp[0],
                pnp[1]);
        return writeFile(
                root,
                FileObjects.convertPackage2Folder(pnp[0]),
                String.format("%s.java",pnp[1]),    //NOI18N
                content);
    }

    @NonNull
    private static FileObject writeFile(
            @NonNull final FileObject root,
            @NonNull final String folder,
            @NonNull final String name,
            @NonNull final String content) throws IOException {
        final FileObject fld;
        if (!folder.isEmpty()) {
            fld = FileUtil.createFolder(root, folder);
        } else {
            fld = root;
        }
        final FileObject file = FileUtil.createData(fld, name);
        try(PrintWriter out = new PrintWriter(new OutputStreamWriter(file.getOutputStream(), "UTF-8"))) {   //NOI18N
            out.println(content);
        }
        return file;
    }

    private final class H extends Handler {
        private final List<FileObject> roots = new ArrayList<>();
        private final List<String> moduleNames = new ArrayList<>();

        H() {
        }

        void reset() {
            roots.clear();
            moduleNames.clear();
        }

        @NonNull
        List< ? extends FileObject> getRoots() {
            return new ArrayList<>(roots);
        }

        @NonNull
        List< ? extends String> getModuleNames() {
            return new ArrayList<>(moduleNames);
        }

        @Override
        public void publish(LogRecord record) {
            final String msg = record.getMessage();
            if (msg != null) {
                switch (msg) {
                    case "rootCache updated: {0}":
                        roots.add((FileObject)record.getParameters()[0]);
                        break;
                    case "modNameCache updated: {0}":
                        moduleNames.add((String)record.getParameters()[0]);
                        break;
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    public static final class CPP implements ClassPathProvider {
        private final Map<FileObject,Map<String,ClassPath>> paths = new HashMap<>();

        @NonNull
        CPP clear() {
            paths.clear();
            return this;
        }

        @NonNull
        public CPP add(
                @NonNull final FileObject root,
                @NonNull final String type,
                @NonNull final ClassPath cp) {
            paths.computeIfAbsent(root, (r) -> new HashMap<>())
                    .put(type,cp);
            return this;
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            for (Map.Entry<FileObject,Map<String,ClassPath>> e : paths.entrySet()) {
                final FileObject root = e.getKey();
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    return e.getValue().get(type);
                }
            }
            return null;
        }
    }

    public static final class COQ implements CompilerOptionsQueryImplementation {
        private final Map<FileObject,List<String>> options = new HashMap<>();

        @NonNull
        <T extends Function<String,T>> Function<String,T> forRoot(@NonNull FileObject root) {
            return new Function<String,T>() {
                @Override
                public T apply(String t) {
                    options.computeIfAbsent(root, (r) -> new ArrayList<>())
                            .add(t);
                    return (T) this;
                }
            };
        }

        @Override
        public Result getOptions(FileObject file) {
            for (Map.Entry<FileObject,List<String>> e : options.entrySet()) {
                final FileObject root = e.getKey();
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    return new Result() {
                        @Override
                        public List<? extends String> getArguments() {
                            return e.getValue();
                        }

                        @Override
                        public void addChangeListener(ChangeListener listener) {
                        }

                        @Override
                        public void removeChangeListener(ChangeListener listener) {
                        }
                    };
                }
            }
            return null;
        }
    }
}
