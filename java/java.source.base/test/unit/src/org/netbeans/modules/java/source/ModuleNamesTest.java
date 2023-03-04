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
package org.netbeans.modules.java.source;

import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.ClientCodeWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.swing.event.ChangeListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.netbeans.ProxyURLStreamHandlerFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.Archive;
import org.netbeans.modules.java.source.parsing.CachingArchiveProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.InferableJavaFileObject;
import org.netbeans.modules.java.source.usages.ClassIndexEventsTransaction;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 * Test for {@link ModuleNames}.
 * @author Tomas Zezula
 */
public class ModuleNamesTest extends NbTestCase {
    private static final String RES_MANIFEST = "META-INF/MANIFEST.MF";  //NOI18N
    private FileObject wd;
    private ModuleNames names;

    public ModuleNamesTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        ProxyURLStreamHandlerFactory.register();
        MockServices.setServices(BinAndSrc.class, NBJRTStreamHandlerFactory.class, AutomaticModuleName.class);
        wd = FileUtil.toFileObject(getWorkDir());
        assertNotNull(wd);
        final FileObject cache = FileUtil.createFolder(wd, "cache");    //NOI18N
        CacheFolder.setCacheFolder(cache);
        names = ModuleNames.getInstance();
        assertNotNull(names);
    }

    public void testAutomaticModule() throws Exception {
        final TraceHandler th = TraceHandler.register();
        try {
            FileObject mod1 = FileUtil.getArchiveRoot(jar(wd, "app-core-1.0.jar", null).get());    //NOI18N
            final FileObject mod2 = FileUtil.getArchiveRoot(jar(wd,"app-main-1.0.jar", null).get());     //NOI18N
            final FileObject mod3 = FileUtil.getArchiveRoot(jar(wd,"app-util2-1.0.jar", null).get());     //NOI18N
            String moduleName = names.getModuleName(mod1.toURL(), false);
            assertEquals("app.core", moduleName);   //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(mod2.toURL(), false);
            assertEquals("app.main", moduleName);   //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(mod1.toURL(), false);
            assertEquals("app.core", moduleName);   //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(mod2.toURL(), false);
            assertEquals("app.main", moduleName);   //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            final URL origURL = mod2.toURL();
            final FileObject mod2new = rename(mod2, "app-ui-1.0.jar");    //NOI18N
            moduleName = names.getModuleName(mod2new.toURL(), false);
            assertEquals("app.ui", moduleName);   //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(origURL, false);
            assertNull(moduleName);
            assertTrue(th.isCalculated());
            th.reset();
            mod1.getFileSystem().removeNotify();    //Close JAR before changing it, otherwise JarFS may be created with wrong content.
            FileUtil.getArchiveFile(mod1).delete();
            FileObject jar = jar(wd,
                    "app-core-1.0.jar", //NOI18N
                    () -> Collections.singleton(Pair.of(
                            "module-info.class",    //NOI18N
                            moduleInfoClz(moduleInfoJava("org.me.app.core", Collections.emptyList())).get()))   //NOI18N
                    ).get();
            mod1 = FileUtil.getArchiveRoot(jar);
            moduleName = names.getModuleName(mod1.toURL(), false);
            assertTrue(th.isCalculated());
            assertEquals("org.me.app.core", moduleName);    //NOI18N
            th.reset();
            moduleName = names.getModuleName(mod3.toURL(), false);
            assertEquals("app.util2", moduleName);   //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
        } finally {
            th.unregister();
        }
    }

    public void testAutomaticModuleWithManifestAttribute() throws Exception {
        final TraceHandler th = TraceHandler.register();
        try {
            FileObject mod = FileUtil.getArchiveRoot(jar(
                    wd,
                    "app-core-1.0.jar", //NOI18N
                    () -> {
                        try {
                            final ByteArrayOutputStream out = new ByteArrayOutputStream();
                            final Manifest mf = new Manifest();
                            mf.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");    //NOI18N
                            mf.getMainAttributes().putValue("Automatic-Module-Name", "org.me.app.core");    //NOI18N
                            mf.write(out);
                            return Collections.singleton(Pair.of(
                                    RES_MANIFEST,
                                    out.toByteArray()
                            ));
                        } catch (IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                    }
            ).get());
            String moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("org.me.app.core", moduleName);    //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("org.me.app.core", moduleName);    //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            mod.getFileSystem().removeNotify();    //Close JAR before changing it, otherwise JarFS may be created with wrong content.
            mod = FileUtil.getArchiveRoot(jar(
                    wd,
                    "app-core-1.0.jar", //NOI18N
                    null).get());
            moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("app.core", moduleName);    //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("app.core", moduleName);    //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            mod.getFileSystem().removeNotify();    //Close JAR before changing it, otherwise JarFS may be created with wrong content.
            mod = FileUtil.getArchiveRoot(jar(
                    wd,
                    "app-core-1.0.jar", //NOI18N
                    () -> {
                        try {
                            final ByteArrayOutputStream out = new ByteArrayOutputStream();
                            final Manifest mf = new Manifest();
                            mf.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");    //NOI18N
                            mf.getMainAttributes().putValue("Automatic-Module-Name", "org.me.app.core");    //NOI18N
                            mf.write(out);
                            return Collections.singleton(Pair.of(
                                    RES_MANIFEST,
                                    out.toByteArray()
                            ));
                        } catch (IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                    }
            ).get());    //NOI18N
            mod = FileUtil.getArchiveRoot(jar(
                    wd,
                    "app-core-1.0.jar", //NOI18N
                    () -> {
                        try {
                            final ByteArrayOutputStream out = new ByteArrayOutputStream();
                            final Manifest mf = new Manifest();
                            mf.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");    //NOI18N
                            mf.getMainAttributes().putValue("Automatic-Module-Name", "com.me.app.core");    //NOI18N
                            mf.write(out);
                            return Collections.singleton(Pair.of(
                                    RES_MANIFEST,
                                    out.toByteArray()
                            ));
                        } catch (IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                    }
            ).get());    //NOI18N
            moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("com.me.app.core", moduleName);    //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("com.me.app.core", moduleName);    //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
        } finally {
            th.unregister();
        }
    }

    public void testNamedModule() throws Exception {
        final TraceHandler th = TraceHandler.register();
        try {
            FileObject mod = FileUtil.getArchiveRoot(jar(
                    wd,
                    "dist.jar", //NOI18N
                    () -> Collections.singleton(Pair.of(
                            "module-info.class",    //NOI18N
                            moduleInfoClz(moduleInfoJava("org.me.app", Collections.emptyList())).get()))        //NOI18N
                    ).get());
            String moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("org.me.app", moduleName);    //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("org.me.app", moduleName);    //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            mod.getFileSystem().removeNotify();    //Close JAR before changing it, otherwise JarFS may be created with wrong content.
            FileUtil.getArchiveFile(mod).delete();
            FileObject jar = jar(wd,
                    "dist.jar", //NOI18N
                    () -> Collections.singleton(Pair.of(
                            "module-info.class",    //NOI18N
                            moduleInfoClz(moduleInfoJava("com.me.app", Collections.emptyList())).get()))    //NOI18N
                    ).get();
            mod = FileUtil.getArchiveRoot(jar);
            moduleName = names.getModuleName(mod.toURL(), false);
            assertTrue(th.isCalculated());
            assertEquals("com.me.app", moduleName);    //NOI18N
            th.reset();
            moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("com.me.app", moduleName);    //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            mod.getFileSystem().removeNotify();    //Close JAR before changing it, otherwise JarFS may be created with wrong content.
            FileUtil.getArchiveFile(mod).delete();
            jar = jar(wd, "dist.jar", null).get();   //NOI18N
            mod = FileUtil.getArchiveRoot(jar);
            moduleName = names.getModuleName(mod.toURL(), false);
            assertTrue(th.isCalculated());
            assertEquals("dist", moduleName);    //NOI18N
            th.reset();
            moduleName = names.getModuleName(mod.toURL(), false);
            assertFalse(th.isCalculated());
            assertEquals("dist", moduleName);    //NOI18N
        } finally {
            th.unregister();
        }
    }

    public void testProject() throws Exception {
        final FileObject src = FileUtil.createFolder(wd, "src");    //NOI18N
        final Supplier<Pair<Boolean, String>> mij = moduleInfoJava("org.me.prj", Collections.emptySet());   //NOI18N
        writeFile(src, "module-info.java", () -> mij.get().second()).get();   //NOI18N
        final FileObject dist = FileUtil.createFolder(wd, "dist");      //NOI18N
        final File distJar = new File(FileUtil.toFile(dist), "prj.jar");    //NOI18N
        final URL distJarURL = FileUtil.urlForArchiveOrDir(distJar);
        BinAndSrc.getInstance().register(src.toURL(), distJarURL);
        final SourceForBinaryQuery.Result2 sres = SourceForBinaryQuery.findSourceRoots2(distJarURL);
        assertTrue(sres.preferSources());
        assertEquals(Arrays.asList(src), Arrays.asList(sres.getRoots()));
        final BinaryForSourceQuery.Result bres = BinaryForSourceQuery.findBinaryRoots(src.toURL());
        assertEquals(Arrays.asList(distJarURL), Arrays.asList(bres.getRoots()));

        final TraceHandler th = TraceHandler.register();
        try {
            String moduleName = names.getModuleName(distJarURL, false);
            assertNull(moduleName);
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(distJarURL, false);
            assertNull(moduleName);
            assertTrue(th.isCalculated());  //null not cached as it may differ for getModuleName(root, true);
            th.reset();
            fakeIndex(src, distJarURL, "org.me.prj");   //NOI18N
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("org.me.prj", moduleName);     //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("org.me.prj", moduleName);     //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            final Supplier<Pair<Boolean, String>> mij2 = moduleInfoJava("org.me.foo", Collections.emptySet());   //NOI18N
            final FileObject modInfo = writeFile(src, "module-info.java", () -> mij2.get().second()).get();   //NOI18N
            fakeIndex(src, distJarURL, "org.me.foo");   //NOI18N
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("org.me.foo", moduleName);     //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("org.me.foo", moduleName);     //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            modInfo.delete();
            fakeIndex(src, distJarURL, null);
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("prj", moduleName);     //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("prj", moduleName);     //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
        } finally {
            th.unregister();
        }
    }

    public void testAutomaticProject() throws IOException {
        final FileObject src = FileUtil.createFolder(wd, "src");    //NOI18N
        final FileObject dist = FileUtil.createFolder(wd, "dist");      //NOI18N
        final File distJar = new File(FileUtil.toFile(dist), "prj.jar");    //NOI18N
        final URL distJarURL = FileUtil.urlForArchiveOrDir(distJar);
        BinAndSrc.getInstance().register(src.toURL(), distJarURL);
        final SourceForBinaryQuery.Result2 sres = SourceForBinaryQuery.findSourceRoots2(distJarURL);
        assertTrue(sres.preferSources());
        assertEquals(Arrays.asList(src), Arrays.asList(sres.getRoots()));
        final BinaryForSourceQuery.Result bres = BinaryForSourceQuery.findBinaryRoots(src.toURL());
        assertEquals(Arrays.asList(distJarURL), Arrays.asList(bres.getRoots()));
        final TraceHandler th = TraceHandler.register();
        AutomaticModuleName.getInstance().register(src, null);  //Register Result for root to listen on it.
        try {
            fakeIndex(src, distJarURL, null);
            String moduleName = names.getModuleName(distJarURL, false);
            assertEquals("prj", moduleName);    //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("prj", moduleName);    //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            AutomaticModuleName.getInstance().register(src, "org.me.foo");  //NOI18N
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("org.me.foo", moduleName);    //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("org.me.foo", moduleName);    //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            AutomaticModuleName.getInstance().register(src, "org.me.boo");  //NOI18N
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("org.me.boo", moduleName);    //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("org.me.boo", moduleName);    //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            AutomaticModuleName.getInstance().register(src, null);
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("prj", moduleName);    //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(distJarURL, false);
            assertEquals("prj", moduleName);    //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
        } finally {
            th.unregister();
        }
    }

    public void testVersionedModuleInfo() throws IOException {
        final TraceHandler th = TraceHandler.register();
        try {
            FileObject mod = FileUtil.getArchiveRoot(jar(
                    wd,
                    "dist.jar", //NOI18N
                    () -> Collections.singleton(Pair.of(
                            "META-INF/versions/9/module-info.class",    //NOI18N
                            moduleInfoClz(moduleInfoJava("org.me.app", Collections.emptyList())).get()))        //NOI18N
                    ).get());
            String moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("org.me.app", moduleName);    //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            mod = FileUtil.getArchiveRoot(jar(
                    wd,
                    "dist2.jar", //NOI18N
                    () -> Arrays.asList(Pair.of(
                            "META-INF/versions/9/module-info.class",    //NOI18N
                            moduleInfoClz(moduleInfoJava("org.me.app", Collections.emptyList())).get()),        //NOI18N
                            Pair.of(
                            "META-INF/versions/12/module-info.class",    //NOI18N
                            moduleInfoClz(moduleInfoJava("org.me.app2", Collections.emptyList())).get()),        //NOI18N
                            Pair.of(
                            "META-INF/versions/broken/module-info.class",    //NOI18N
                            moduleInfoClz(moduleInfoJava("broken", Collections.emptyList())).get()),        //NOI18N
                            Pair.of(
                            "module-info.class",    //NOI18N
                            moduleInfoClz(moduleInfoJava("old", Collections.emptyList())).get()))        //NOI18N
                    ).get());
            moduleName = names.getModuleName(mod.toURL(), false);
            assertEquals("org.me.app2", moduleName);    //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
        } finally {
            th.unregister();
        }
    }

    public void testPlatform() throws Exception {
        final TraceHandler th = TraceHandler.register();
        final URL JAVA_BASE = new URL("nbjrt:file:/Library/Java/JavaVirtualMachines/jdk-9.jdk/Contents/Home/!/modules/java.base/");   //NOI18N
        final URL JVMCI = new URL("nbjrt:file:/Library/Java/JavaVirtualMachines/jdk-9.jdk/Contents/Home/!/modules/jdk.internal.vm.ci/"); //NOI18N
        try {
            String moduleName = names.getModuleName(JAVA_BASE, false);
            assertEquals("java.base", moduleName);  //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(JAVA_BASE, false);
            assertEquals("java.base", moduleName);  //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(JVMCI, false);
            assertEquals("jdk.internal.vm.ci", moduleName); //NOI18N
            assertTrue(th.isCalculated());
            th.reset();
            moduleName = names.getModuleName(JVMCI, false);
            assertEquals("jdk.internal.vm.ci", moduleName); //NOI18N
            assertFalse(th.isCalculated());
            th.reset();
        } finally {
            th.unregister();
        }
    }

    private void fakeIndex(
            @NonNull final FileObject srcRoot,
            @NonNull final URL binRoot,
            @NullAllowed final String moduleName) throws IOException {
        ClassIndexImpl q = ClassIndexManager.getDefault().createUsagesQuery(srcRoot.toURL(), true, ClassIndexEventsTransaction.create(true, ()->true));
        q.setState(ClassIndexImpl.State.INITIALIZED);
        JavaIndex.setAttribute(srcRoot.toURL(), JavaIndex.ATTR_MODULE_NAME, moduleName);
        ModuleNames.getInstance().reset(binRoot);
    }

    private Supplier<FileObject> writeFile(
            @NonNull final FileObject folder,
            @NonNull final String name,
            @NonNull final Supplier<String> content) {
        return () -> {
            try {
                final FileObject file = FileUtil.createData(folder, name);
                try (PrintWriter out = new PrintWriter(new OutputStreamWriter(file.getOutputStream(), StandardCharsets.UTF_8))) {
                    out.println(content.get());
                }
                return file;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        };
    }

    @NonNull
    private FileObject rename(
            @NonNull final FileObject root,
            @NonNull final String name) throws IOException {
        final String simpleName = FileObjects.stripExtension(name);
        final String ext = FileObjects.getExtension(name);
        final FileObject file = FileUtil.getArchiveFile(root);
        final FileLock lck = file.lock();
        try {
            file.rename(lck, simpleName, ext);
        } finally {
            lck.releaseLock();
        }
        return FileUtil.getArchiveRoot(file);
    }

    @NonNull
    private final Supplier<FileObject> jar(
            @NonNull final FileObject folder,
            @NonNull final String name,
            @NullAllowed final Supplier<Collection<Pair<String,byte[]>>> content) {
        return () -> {
            try {
                final FileObject zf = FileUtil.createData(folder, name);
                final Map<String,byte[]> files = new HashMap<>();
                Manifest mf = new Manifest();
                if (content != null) {
                    for (Pair<String,byte[]> c : content.get()) {
                        if (RES_MANIFEST.equals(c.first())) {
                            mf = new Manifest(new ByteArrayInputStream(c.second()));
                        } else {
                            files.put(c.first(), c.second());
                        }
                    }
                }
                try (final JarOutputStream out = new JarOutputStream(zf.getOutputStream(), mf)) {
                    out.setComment("Test zip file");
                    for (Map.Entry<String,byte[]> file : files.entrySet()) {
                        final String path = file.getKey();
                        final byte[] data = file.getValue();
                        out.putNextEntry(new ZipEntry(path));
                        out.write(data, 0, data.length);
                        out.closeEntry();
                    }
                }
                return zf;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        };
    }

    @NonNull
    private static Supplier<Pair<Boolean,String>> moduleInfoJava(
            @NonNull final String moduleName,
            @NonNull final Iterable<String> exports) {
        return () -> {
            final StringBuilder info = new StringBuilder();
            info.append("module ")          //NOI18N
                    .append(moduleName)
                    .append(" {\n");        //NOI18N
            for (String pkg : exports) {
                info.append("exports ")     //NO18N
                        .append(pkg)
                        .append(";\n");     //NOI18N
            }
            info.append("}\n");             //NOI18N
            return Pair.of(
                    "java.base".equals(moduleName), //NOI18N
                    info.toString());
        };
    }

    @NonNull
    private static Supplier<byte[]> moduleInfoClz(
            @NonNull final Supplier<Pair<Boolean,String>> moduleInfoJava) {
        return () -> {
            try {
                final JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
                final List<String> opts = new ArrayList<>();
                opts.add("-source");        //NOI18N
                opts.add("9");              //NOI18N
                opts.add("-target");        //NOI18N
                opts.add("9");              //NOI18N
                final Pair<Boolean,String> p = moduleInfoJava.get();
                final JavaFileObject moduleInfo = FileObjects.memoryFileObject(
                        "",                 //NOI18N
                        "module-info.java", //NOI18N
                        p.second());
                final JavaFileManager fm = new MemFM(p.first());
                final JavacTask task = (JavacTask) jc.getTask(null, fm, null, opts, null, Collections.singleton(moduleInfo));
                final Iterator<? extends JavaFileObject> res = task.generate().iterator();
                final JavaFileObject fo = res.hasNext() ?
                        res.next() :
                        null;
                if (fo != null) {
                    try (InputStream in = fo.openInputStream()) {
                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        FileUtil.copy(in, out);
                        out.close();
                        return out.toByteArray();
                    }
                } else {
                    return null;
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        };
    }

    @ClientCodeWrapper.Trusted
    private static final class MemFM implements JavaFileManager {
        private final boolean bootstrap;
        private final Supplier<byte[]> javaBaseModInfo;

        MemFM(boolean bootstrap) {
            this.bootstrap = bootstrap;
            javaBaseModInfo = bootstrap ?
                    null :
                    moduleInfoClz(moduleInfoJava("java.base", Arrays.asList("java.lang","java.io"))); //NOI18N
        }

        @Override
        public ClassLoader getClassLoader(Location location) {
            return null;
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
            if (location == StandardLocation.CLASS_OUTPUT) {
                return bootstrap ?
                        listModuleContent(
                                (ModLoc) listLocationsForModules(StandardLocation.SYSTEM_MODULES).iterator().next().iterator().next(),
                                packageName,
                                kinds) :
                        Collections.emptyList();
            } else if (location instanceof ModLoc) {
                return listModuleContent((ModLoc)location, packageName, kinds);
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            if (file instanceof InferableJavaFileObject) {
                return ((InferableJavaFileObject)file).inferBinaryName();
            }
            return null;
        }

        @Override
        public boolean isSameFile(javax.tools.FileObject a, javax.tools.FileObject b) {
            return a.toUri().equals(b.toUri());
        }

        @Override
        public boolean handleOption(String current, Iterator<String> remaining) {
            return false;
        }

        @Override
        public boolean hasLocation(Location location) {
            return location == StandardLocation.CLASS_OUTPUT ||
                    location == StandardLocation.SYSTEM_MODULES;
        }

        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
            return null;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling) throws IOException {
            return new MemJFO(className);
        }

        @Override
        public javax.tools.FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            return null;
        }

        @Override
        public javax.tools.FileObject getFileForOutput(Location location, String packageName, String relativeName, javax.tools.FileObject sibling) throws IOException {
            return null;
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public int isSupportedOption(String option) {
            return -1;
        }

        @Override
        public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
            if (location == StandardLocation.CLASS_OUTPUT) {
                return Collections.emptySet();
            } else if (location == StandardLocation.SYSTEM_MODULES) {
                final ClassPath cp = JavaPlatform.getDefault().getBootstrapLibraries();
                Collection<? extends URL> javaBase = findJavaBase(cp);
                if (javaBase.isEmpty()) {
                    javaBase = fakeJavaBase(cp);
                }
                return Collections.singleton(Collections.singleton(new ModLoc(
                        StandardLocation.SYSTEM_MODULES,
                        "java.base",    //NOI18N
                        javaBase)));
            } else {
                return Collections.emptySet();
            }
        }


        @Override
        public String inferModuleName(Location location) throws IOException {
            return location instanceof ModLoc ?
                    ((ModLoc)location).getModuleName() :
                    null;
        }

        @NonNull
        private Iterable<JavaFileObject> listModuleContent(
                @NonNull final ModLoc modLoc,
                @NonNull final String packageName,
                @NonNull final Set<JavaFileObject.Kind> kinds) throws IOException {
            final CachingArchiveProvider cap = CachingArchiveProvider.getDefault();
            final Collection<JavaFileObject> res = new ArrayList<>();
            if (javaBaseModInfo != null && "java.base".equals(modLoc.getModuleName()) && packageName.isEmpty()) {
                final JavaFileObject jfo = new MemJFO("module-info");
                try(OutputStream out = jfo.openOutputStream()) {
                    assertNotNull("output stream cannot be null", out);
                    final byte[] data = javaBaseModInfo.get();
                    if (data != null) {
                        out.write(data, 0, data.length);
                    }
                }
                res.add(jfo);
            }
            for (URL url : modLoc.getRoots()) {
                final Archive ca = cap.getArchive(url, false);
                if (ca != null) {
                    StreamSupport.stream(
                            ca.getFiles(FileObjects.convertPackage2Folder(packageName), null, kinds, null, false).spliterator(),
                            false)
                            .forEach(res::add);
                }
            }
            return res;
        }

        @NonNull
        private static Collection<? extends URL> findJavaBase(@NonNull final ClassPath cp) {
            return cp.entries().stream()
                    .map(ClassPath.Entry::getURL)
                    .filter((u) -> u.getProtocol().equals(FileObjects.PROTO_NBJRT) && u.toString().endsWith("/java.base/")) //NOI18N
                    .map(Collections::singleton)
                    .findAny()
                    .orElse(Collections.emptySet());
        }

        @NonNull
        private static Collection<? extends URL> fakeJavaBase(@NonNull final ClassPath cp) {
            return cp.entries().stream()
                    .map(ClassPath.Entry::getURL)
                    .collect(Collectors.toList());
        }

        private static final class ModLoc implements Location {
            private final Location base;
            private final String modName;
            private final Collection<? extends URL> roots;

            ModLoc(
                    @NonNull final Location base,
                    @NonNull final String modName,
                    @NonNull final Collection<? extends URL> roots) {
                this.base = base;
                this.modName = modName;
                this.roots = roots;
            }

            @NonNull
            Location getBase() {
                return base;
            }

            @NonNull
            String getModuleName() {
                return modName;
            }

            @NonNull
            Collection<? extends URL> getRoots() {
                return roots;
            }

            @Override
            public String getName() {
                return getModuleName();
            }

            @Override
            public boolean isOutputLocation() {
                return false;
            }

            @Override
            public boolean isModuleOrientedLocation() {
                return false;
            }
        }
    }

    @ClientCodeWrapper.Trusted
    private static final class MemJFO implements InferableJavaFileObject {
        private final String fqn;
        private byte[] data;
        private long lm = -1L;

        MemJFO(@NonNull final String fqn) {
            this.fqn = fqn;
        }

        @Override
        public Kind getKind() {
            return Kind.CLASS;
        }

        @Override
        public boolean isNameCompatible(String simpleName, Kind kind) {
            return kind == Kind.CLASS && FileObjects.getBaseName(fqn, '.').equals(simpleName);
        }

        @Override
        public NestingKind getNestingKind() {
            return null;
        }

        @Override
        public Modifier getAccessLevel() {
            return null;
        }

        @Override
        public URI toUri() {
            try {
                return new URI(FileObjects.convertPackage2Folder(fqn));
            } catch (URISyntaxException e) {
               throw new RuntimeException(e);
            }
        }

        @Override
        public String getName() {
            return FileObjects.convertPackage2Folder(fqn);
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    super.close();
                    data = toByteArray();
                    lm = System.currentTimeMillis();
                }
            };
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return null;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return null;
        }

        @Override
        public Writer openWriter() throws IOException {
            return null;
        }

        @Override
        public long getLastModified() {
            return lm;
        }

        @Override
        public boolean delete() {
            return true;
        }

        @Override
        public String inferBinaryName() {
            return getName();
        }
    }

    private static final class TraceHandler extends Handler {
        private static final String MSG_NO_CACHE = "No cache for: {0}"; //NOI18N

        private final Logger logger;
        private final Level origLevel;
        private boolean calculated;


        private TraceHandler(
                @NonNull final Logger logger,
                @NonNull final Level origLevel) {
            this.logger = logger;
            this.origLevel = origLevel;
        }

        boolean isCalculated() {
            return calculated;
        }

        void reset() {
            calculated = false;
        }

        void unregister() {
            this.logger.setLevel(origLevel);
            this.logger.removeHandler(this);
        }

        static TraceHandler register() {
            final Logger l = Logger.getLogger(ModuleNames.class.getName());
            final Level origLevel = l.getLevel();
            final TraceHandler th = new TraceHandler(l, origLevel);
            l.setLevel(Level.FINE);
            l.addHandler(th);
            return th;
        }

        @Override
        public void publish(LogRecord record) {
            final String message = record.getMessage();
            switch (message) {
                case MSG_NO_CACHE:
                    calculated = true;
                    break;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    public static final class BinAndSrc implements BinaryForSourceQueryImplementation, SourceForBinaryQueryImplementation2 {
        private final Map<URL,Reference<BinR>> emmittedBinRs;
        private final Map<URL,Reference<SrcR>> emmittedSrcRs;
        private volatile Pair<URL,URL> root;

        public BinAndSrc() {
            this.emmittedBinRs = new HashMap<>();
            this.emmittedSrcRs = new HashMap<>();
        }

        @CheckForNull
        static BinAndSrc getInstance() {
            return Lookup.getDefault().lookup(BinAndSrc.class);
        }

        void register(
                @NonNull final URL srcRoot,
                @NonNull final URL binRoot) {
            this.root = Pair.of(srcRoot, binRoot);
            emmittedSrcRs.values().stream()
                    .map(Reference::get)
                    .filter((r) -> r != null)
                    .forEach(SrcR::changed);
            emmittedBinRs.values().stream()
                    .map(Reference::get)
                    .filter((r) -> r != null)
                    .forEach(BinR::changed);
        }

        @Override
        public BinaryForSourceQuery.Result findBinaryRoots(URL sourceRoot) {
            final Reference<BinR> rr = emmittedBinRs.get(sourceRoot);
            BinR r;
            if (rr != null && (r=rr.get()) != null) {
                return r;
            }
            final Pair<URL,URL> current = root;
            if (current != null && current.first().equals(sourceRoot)) {
                r = new BinR(current.first());
                emmittedBinRs.put(sourceRoot, new WeakReference<>(r));
                return r;
            }
            return null;
        }

        @Override
        public SourceForBinaryQueryImplementation2.Result findSourceRoots2(URL binaryRoot) {
            final Reference<SrcR> rr = emmittedSrcRs.get(binaryRoot);
            SrcR r;
            if (rr != null && (r=rr.get()) != null) {
                return r;
            }
            final Pair<URL,URL> current = root;
            if (current != null && current.second().equals(binaryRoot)) {
                r = new SrcR(current.second());
                emmittedSrcRs.put(binaryRoot, new WeakReference<>(r));
                return r;
            }
            return null;
        }

        @Override
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            return findSourceRoots2(binaryRoot);
        }

        private final class BinR implements BinaryForSourceQuery.Result {
            private final URL src;
            private final ChangeSupport listeners;

            BinR(
                    @NonNull final URL src) {
                this.src = src;
                this.listeners = new ChangeSupport(this);
            }

            @Override
            public URL[] getRoots() {
                final Pair<URL,URL> current = root;
                if (current != null && src.equals(current.first())) {
                    return new URL[] {current.second()};
                }
                return new URL[0];
            }

            void changed() {
                listeners.fireChange();
            }

            @Override
            public void addChangeListener(ChangeListener l) {
                listeners.addChangeListener(l);
            }

            @Override
            public void removeChangeListener(ChangeListener l) {
                listeners.removeChangeListener(l);
            }
        }

        private final class SrcR implements SourceForBinaryQueryImplementation2.Result {
            private final URL bin;
            private final ChangeSupport listeners;

            SrcR(
                    @NonNull final URL bin) {
                this.bin = bin;
                this.listeners = new ChangeSupport(this);
            }

            @Override
            public boolean preferSources() {
                return true;
            }

            @Override
            public FileObject[] getRoots() {
                final Pair<URL,URL> current = root;
                if (current != null && bin.equals(current.second())) {
                    final URL src = current.first();
                    final FileObject srcFo = URLMapper.findFileObject(src);
                    if (srcFo != null) {
                        return new FileObject[] {srcFo};
                    }
                }
                return new FileObject[0];
            }

            void changed() {
                listeners.fireChange();
            }

            @Override
            public void addChangeListener(ChangeListener l) {
                this.listeners.addChangeListener(l);
            }

            @Override
            public void removeChangeListener(ChangeListener l) {
                this.listeners.removeChangeListener(l);
            }
        }
    }

    public static final class NBJRTStreamHandlerFactory implements URLStreamHandlerFactory {

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if ("nbjrt".equals(protocol)) { //NOI18N
                return new NBJRTURLStreamHandler();
            }
            return null;
        }

        private static class NBJRTURLStreamHandler extends URLStreamHandler {

            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                //Not needed
                return null;
            }
        }
    }

    public static final class AutomaticModuleName implements CompilerOptionsQueryImplementation {

        private Map<FileObject, R> roots = new HashMap<>();

        @Override
        public Result getOptions(FileObject file) {
            for (Map.Entry<FileObject,R> e : roots.entrySet()) {
                if (e.getKey().equals(file) || FileUtil.isParentOf(e.getKey(), file)) {
                    return e.getValue();
                }
            }
            return null;
        }

        void register(
                @NonNull final FileObject root,
                @NullAllowed final String moduleName) {
            R r = roots.get(root);
            if (r == null) {
                r = new R();
                roots.put(root, r);
            }
            r.setModuleName(moduleName);
        }

        @CheckForNull
        public static AutomaticModuleName getInstance() {
            return Lookup.getDefault().lookup(AutomaticModuleName.class);
        }

        private static class R extends CompilerOptionsQueryImplementation.Result {
            private final ChangeSupport listeners;
            private String moduleName;

            private R() {
                this.listeners = new ChangeSupport(this);
            }

            void setModuleName(@NullAllowed final String moduleName) {
                this.moduleName = moduleName;
                this.listeners.fireChange();
            }

            @Override
            public List<? extends String> getArguments() {
                if (moduleName != null) {
                    return Collections.singletonList(String.format(
                            "-XDautomatic-module-name:%s",  //NOI18N
                            moduleName
                    ));
                } else {
                    return Collections.emptyList();
                }
            }

            @Override
            public void addChangeListener(ChangeListener listener) {
                this.listeners.addChangeListener(listener);
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
                this.listeners.removeChangeListener(listener);
            }
        }
    }
}
