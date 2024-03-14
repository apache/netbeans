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
package org.netbeans.modules.javascript2.editor.classpath;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.javascript2.editor.index.JsIndexer;
import org.netbeans.modules.javascript2.model.spi.PlatformProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Defines classpaths (boot CP) of JavaScript files.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@ServiceProviders({
    @ServiceProvider(service = ClassPathProvider.class),
    @ServiceProvider(service = PlatformProvider.class)
})
public class ClassPathProviderImpl implements ClassPathProvider, PlatformProvider {

    private static final Logger LOG = Logger.getLogger(ClassPathProviderImpl.class.getName());
    protected static final RequestProcessor RP = new RequestProcessor(ClassPathProviderImpl.class);

    public static final String BOOT_CP = "classpath/javascript-boot"; //NOI18N
    public static final AtomicBoolean JS_CLASSPATH_REGISTERED = new AtomicBoolean(false);

    // GuardedBy(ClassPathProviderImpl.class)
    private static ClassPath cachedBootClassPath;

    // GuardedBy(ClassPathProviderImpl.class)
    private static List<FileObject> roots;

    /** Names of JavaScript signature bundles. */
    private static final StubsBundle[] STUBS_BUNDLES = { // Keep order, unittests assume core is first
        new StubsBundle("core-stubs.zip", "core-doc-stubs.zip"),  //NOI18N
        new StubsBundle("dom-stubs.zip", "dom-doc-stubs.zip"),  //NOI18N
    };

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(BOOT_CP)) {
            return getBootClassPath();
        }
        return null;
    }

    @Override
    public List<FileObject> getPlatformStubs() {
        return Collections.unmodifiableList(getJsStubs());
    }

    public static synchronized ClassPath getBootClassPath() {
        if (cachedBootClassPath == null) {
            List<FileObject> stubs = getJsStubs();
            cachedBootClassPath = ClassPathSupport.createClassPath(stubs.toArray(new FileObject[0]));
        }
        return cachedBootClassPath;
    }

    public static synchronized List<FileObject> getJsStubs() {
        if (roots == null) {
            List<FileObject> result = new ArrayList<>(STUBS_BUNDLES.length);
            for (StubsBundle bundle : STUBS_BUNDLES) {
                File stubFile = InstalledFileLocator.getDefault().locate("jsstubs/" + bundle.getNameOfDocumented(), "org.netbeans.modules.javascript2.editor", false); //NOI18N
                if (stubFile == null || !stubFile.exists()) {
                    stubFile = InstalledFileLocator.getDefault().locate("jsstubs/" + bundle.getNameOfPruned(), "org.netbeans.modules.javascript2.editor", false); //NOI18N
                }
                if (stubFile == null) {
                    // Probably inside unit test.
                    LOG.log(Level.INFO, "Stubfile not found for ({0} / {1}) using InstalledFileLocator, using fallback", new Object[]{bundle.getNameOfPruned(), bundle.getNameOfDocumented()});
                    try {
                        URI moduleJarUri = ClassPathProviderImpl.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                        LOG.log(Level.FINE, "Module JAR: {0}", moduleJarUri);
                        if ("jar".equals(moduleJarUri.getScheme())) {
                            JarURLConnection jarUrlConnection = (JarURLConnection) moduleJarUri.toURL().openConnection();
                            moduleJarUri = jarUrlConnection.getJarFileURL().toURI();
                            LOG.log(Level.FINE, "Module JAR (unwrapped): {0}", moduleJarUri);
                        }
                        File moduleJar = Utilities.toFile(moduleJarUri);
                        LOG.log(Level.FINE, "Module File: {0}", moduleJar);
                        stubFile = new File(moduleJar.getParentFile().getParentFile(), "jsstubs/" + bundle.getNameOfPruned()); //NOI18N
                    } catch (URISyntaxException | IllegalArgumentException | IOException x) {
                        assert false : x;
                    }
                }
                if (stubFile == null || !stubFile.isFile() || !stubFile.exists()) {
                    LOG.log(Level.WARNING, "JavaScript stubs file was not found: {0}", stubFile != null ? stubFile.getAbsolutePath() : null);
                } else {
                    result.add(FileUtil.getArchiveRoot(FileUtil.toFileObject(stubFile)));
                }
            }
            roots = result;
        }
        return Collections.unmodifiableList(roots);
    }

    /**
     * Registers JavaScript classpath if not already done. <p> Class synchronized since more language instances can be
     * created in an undefined way. <p> The registration is done lazily in EDT task so it is not ensured that the
     * JavaScript classpath is properly initialized after returning from this method. <p> The JavaScript classpath
     * unregistration is done in module's install class.
     */
    public static void registerJsClassPathIfNeeded() {
        final Runnable action = () -> {
            registerJsClassPathIfNeededImpl();
        };
        if (JsIndexer.Factory.isScannerThread()) {
            JsIndexer.Factory.addPostScanTask(action);
        } else {
            action.run();
        }

    }

    private static void registerJsClassPathIfNeededImpl() {
        if (JS_CLASSPATH_REGISTERED.compareAndSet(false, true)) {
            SwingUtilities.invokeLater(() -> {
                ClassPath cp = ClassPathProviderImpl.getBootClassPath();
                if (cp != null) {
                    GlobalPathRegistry.getDefault().register(ClassPathProviderImpl.BOOT_CP, new ClassPath[]{cp});
                }
            });
        }
    }

    private static class StubsBundle {

        private final String nameOfPruned;
        private final String nameOfDocumented;

        public StubsBundle(String nameOfPruned, String nameOfDocumented) {
            this.nameOfPruned = nameOfPruned;
            this.nameOfDocumented = nameOfDocumented;
        }

        public String getNameOfPruned() {
            return nameOfPruned;
        }

        public String getNameOfDocumented() {
            return nameOfDocumented;
        }
    }
}
