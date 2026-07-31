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
package org.netbeans.modules.java.source.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.jumpto.symbol.SymbolProviderAccessor;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.support.AsyncDescriptor;
import org.netbeans.spi.jumpto.support.DescriptorChangeEvent;
import org.netbeans.spi.jumpto.support.DescriptorChangeListener;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/* This test was written entirely by Claude Opus 4.6, prompted by Eirik Bakke (ebakke@ultorg.com).
Eirik confirmed that the test failed before the fix to the bug in AsyncJavaSymbolDescriptor, and
passed after the fix was applied. */

/**
 * Regression test for the long-standing Go to Symbol bug where
 * AsyncJavaSymbolDescriptor.resolve() silently returned empty for any
 * Java type whose .sig file used a NetBeans-specific class-file
 * encoding, causing Lucene-indexed matches to flash briefly in the Go
 * to Symbol list and then be deleted by
 * Models.MutableListModelImpl#descriptorChanged. Root cause was that
 * the old hand-rolled javac construction in resolve() called
 * JavacTool.create().getTask(...) directly, which does not pre-register
 * NetBeans' javac context enhancers (NBClassReader, NBAttr, NBEnter,
 * NBMemberEnter, NBResolve, NBClassFinder, NBJavaCompiler,
 * NBClassWriter, etc.). NetBeans .sig files (the per-root cached form
 * of indexed Java classes) are written by NBClassWriter and use a
 * relaxed/extended class-file format that only NBClassReader knows how
 * to read; stock javac's ClassReader rejects them with BadClassFile
 * (CompletionFailure), and ElementUtils.getTypeElementByBinaryName
 * silently returned null.
 *
 * <p>The test creates a single-class source root whose source is
 * intentionally rich enough (a lambda, generics, a static-final String
 * constant, and a separate annotation type) that the indexer's .sig
 * output is empirically rejected by stock javac. The exact attribute
 * that trips stock ClassReader for this particular class has not been
 * pinned down -- a trivial empty class produces a .sig that vanilla
 * javac reads without trouble, so the richer source is required to
 * exercise the regression. The test indexes the source, asks
 * JavaSymbolProvider to find the class by name, triggers
 * AsyncJavaSymbolDescriptor's async resolve() path via getSymbolName(),
 * and asserts that the resulting descriptor-change event carries a
 * non-empty, enriched replacement (not the original
 * AsyncJavaSymbolDescriptor instance). Verified to fail against the
 * pre-fix code path and pass against the fix.
 */
public class JavaSymbolProviderTest extends NbTestCase {

    private static FileObject srcRoot;
    private static ClassPath sourcePath;
    private static ClassPath compilePath;
    private static ClassPath bootPath;
    private static MutableCp spiCp;
    private static MutableCp spiSrc;

    public JavaSymbolProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File cache = new File(getWorkDir(), "cache"); //NOI18N
        cache.mkdirs();
        IndexUtil.setCacheFolder(cache);
        File src = new File(getWorkDir(), "src"); //NOI18N
        src.mkdirs();
        srcRoot = FileUtil.toFileObject(src);
        spiSrc = new MutableCp(Collections.singletonList(ClassPathSupport.createResource(srcRoot.getURL())));
        sourcePath = ClassPathFactory.createClassPath(spiSrc);
        spiCp = new MutableCp();
        compilePath = ClassPathFactory.createClassPath(spiCp);
        bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        MockServices.setServices(ClassPathProviderImpl.class);

        /* The class is intentionally rich (a lambda, generics, a
           static-final String constant, and a separate annotation
           type) so that the .sig file the indexer writes is
           empirically rejected by stock javac's ClassReader. A trivial
           empty class produces a .sig that vanilla javac can read, and
           so would not exercise the bug we are regressing against.
           The exact attribute that trips stock ClassReader for this
           class has not been pinned down -- it is enough to know that
           the differential exists. */
        createJavaFile(srcRoot, "org.me.test", "FooBar",
                "package org.me.test;\n"
                + "import java.lang.annotation.ElementType;\n"
                + "import java.lang.annotation.Retention;\n"
                + "import java.lang.annotation.RetentionPolicy;\n"
                + "import java.lang.annotation.Target;\n"
                + "@Retention(RetentionPolicy.SOURCE)\n"
                + "@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})\n"
                + "@interface SourceMark {}\n"
                + "@SourceMark\n"
                + "public class FooBar {\n"
                + "    public static final String GREETING = \"hello\";\n"
                + "    @SourceMark public final java.util.function.Supplier<String> SUPPLIER = () -> GREETING;\n"
                + "    @SourceMark public void doSomething() {}\n"
                + "}\n");
        /* JavaSymbolProvider queries GlobalPathRegistry for SOURCE
           roots; it does not take a ClasspathInfo the way
           JavaTypeProvider does. Register our test source path there
           so findRoots(SOURCE, ...) picks it up. */
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {sourcePath});
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[] {bootPath});
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {compilePath});
        IndexingManager.getDefault().refreshIndexAndWait(srcRoot.getURL(), null, true);
    }

    @Override
    protected void tearDown() throws Exception {
        GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] {sourcePath});
        GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, new ClassPath[] {bootPath});
        GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, new ClassPath[] {compilePath});
        MockServices.setServices();
    }

    /**
     * Regression test: asks JavaSymbolProvider for "FooBar", lets the
     * returned AsyncJavaSymbolDescriptor fire its async resolve(), and
     * asserts the resulting replacement is non-empty and not the
     * original async descriptor instance. Prior to the fix, resolve()
     * silently returned an empty collection and this assertion failed.
     */
    public void testAsyncDescriptorResolvesForIndexedType() throws Exception {
        final JavaSymbolProvider provider = new JavaSymbolProvider();
        final List<SymbolDescriptor> results = new ArrayList<>();
        final String[] message = new String[1];
        final SymbolProvider.Context ctx = SymbolProviderAccessor.DEFAULT
                .createContext(null, "FooBar", SearchType.PREFIX); //NOI18N
        final SymbolProvider.Result res = SymbolProviderAccessor.DEFAULT
                .createResult(results, message, ctx, provider);
        provider.computeSymbolNames(ctx, res);
        assertFalse("JavaSymbolProvider returned no hits for FooBar; "
                + "the index may not have been populated in setUp", results.isEmpty());

        // Find the AsyncDescriptor corresponding to the class itself.
        AsyncDescriptor<SymbolDescriptor> async = null;
        for (SymbolDescriptor d : results) {
            if (d instanceof AsyncDescriptor && "FooBar".equals(d.getSimpleName())) { //NOI18N
                @SuppressWarnings("unchecked")
                final AsyncDescriptor<SymbolDescriptor> cast = (AsyncDescriptor<SymbolDescriptor>) d;
                async = cast;
                break;
            }
        }
        assertNotNull("JavaSymbolProvider did not return an AsyncDescriptor "
                + "for class FooBar; results=" + results, async);
        final AsyncDescriptor<SymbolDescriptor> original = async;

        // Subscribe to the descriptor-change event that initialize() ->
        // resolve() -> fireDescriptorChange() will fire on the WORKER
        // thread once we touch getSymbolName() / getIcon().
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Collection<? extends SymbolDescriptor>> replacementRef = new AtomicReference<>();
        final DescriptorChangeListener<SymbolDescriptor> listener =
                new DescriptorChangeListener<SymbolDescriptor>() {
                    @Override
                    public void descriptorChanged(DescriptorChangeEvent<SymbolDescriptor> event) {
                        replacementRef.set(event.getReplacement());
                        latch.countDown();
                    }
                };
        original.addDescriptorChangeListener(listener);
        try {
            // Triggers AsyncJavaSymbolDescriptor.initialize() which
            // schedules resolve() on the WORKER RequestProcessor.
            ((SymbolDescriptor) original).getSymbolName();
            assertTrue("Async resolve() did not complete within 30s.",
                    latch.await(30, TimeUnit.SECONDS));
        } finally {
            original.removeDescriptorChangeListener(listener);
        }

        final Collection<? extends SymbolDescriptor> replacement = replacementRef.get();
        assertNotNull("replacement was null", replacement);

        /* The actual regression assertion.
           Before the fix: resolve()'s hand-rolled
                           JavacTool.create().getTask(...) call had no NB
                           context enhancers, so javac used the stock
                           ClassReader, which threw CompletionFailure on
                           the .sig file (NetBeans .sig files use a relaxed
                           class-file format that only NBClassReader can
                           read). ElementUtils.getTypeElementByBinaryName
                           silently caught that and returned null, so
                           resolve() returned an empty collection and the
                           assertion below tripped.
           After the fix:  resolve() goes through
                           JavacParser.createJavacTask(ClasspathInfo.create(
                           root), ...), which pre-registers NBClassReader
                           and the rest of the NB javac context, so the
                           .sig file loads correctly and resolve() returns a
                           ResolvedJavaSymbolDescriptor distinct from the
                           source AsyncJavaSymbolDescriptor. */
        assertFalse("resolve() returned an empty replacement -- the bug is "
                + "still present: AsyncJavaSymbolDescriptor.resolve() is "
                + "silently failing to enrich the Lucene-indexed hit. Most "
                + "likely cause: the JavacTask was built without "
                + "pre-registering NBClassReader, so stock javac could not "
                + "parse the per-root .sig file.",
                replacement.isEmpty());
        for (SymbolDescriptor d : replacement) {
            assertNotSame("resolve() fell back to its singleton(this) "
                    + "defense-in-depth path instead of actually enriching "
                    + "the descriptor. This still means javac could not "
                    + "complete() the type; most likely the JavacTask was "
                    + "not built with NB context enhancers (NBClassReader, "
                    + "etc.) and so cannot read NetBeans .sig files.",
                    original, d);
        }
    }

    private static FileObject createJavaFile(
            final FileObject root,
            final String pkg,
            final String name,
            final String content) throws IOException {
        final FileObject file = FileUtil.createData(
                root,
                String.format("%s/%s.java",
                        FileObjects.convertPackage2Folder(pkg),
                        name));
        final FileLock lck = file.lock();
        try {
            final PrintWriter out = new PrintWriter(new OutputStreamWriter(file.getOutputStream(lck)));
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

    public static class ClassPathProviderImpl implements ClassPathProvider {
        @Override
        public ClassPath findClassPath(final FileObject file, final String type) {
            final FileObject[] roots = sourcePath.getRoots();
            for (FileObject root : roots) {
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    if (type == ClassPath.SOURCE) {
                        return sourcePath;
                    }
                    if (type == ClassPath.COMPILE) {
                        return compilePath;
                    }
                    if (type == ClassPath.BOOT) {
                        return bootPath;
                    }
                }
            }
            return null;
        }
    }

    private static final class MutableCp implements ClassPathImplementation {

        private final PropertyChangeSupport support;
        private List<? extends PathResourceImplementation> impls;

        MutableCp() {
            this(Collections.<PathResourceImplementation>emptyList());
        }

        MutableCp(final List<? extends PathResourceImplementation> impls) {
            assert impls != null;
            support = new PropertyChangeSupport(this);
            this.impls = impls;
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            return impls;
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener listener) {
            assert listener != null;
            support.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            assert listener != null;
            support.removePropertyChangeListener(listener);
        }
    }
}
