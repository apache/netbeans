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
package org.netbeans.modules.java.hints.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;

import static junit.framework.TestCase.assertFalse;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;

/** Note this is duplicated in:
 * java.source.base/test/unit/src/org/netbeans/modules/java/source/BootClassPathUtil.java
 *
 * @author lahvac
 */
public class BootClassPathUtil {

    public static ClassPath getBootClassPath() {
        String cp = System.getProperty("sun.boot.class.path");
        if (cp != null) {
            List<URL> urls = new ArrayList<>();
            String[] paths = cp.split(Pattern.quote(System.getProperty("path.separator")));
            for (String path : paths) {
                File f = new File(path);

                if (!f.canRead())
                    continue;

                FileObject fo = FileUtil.toFileObject(f);
                if (FileUtil.isArchiveFile(fo)) {
                    fo = FileUtil.getArchiveRoot(fo);
                }
                if (fo != null) {
                    urls.add(fo.toURL());
                }
            }
            return ClassPathSupport.createClassPath(urls.toArray(new URL[0]));
        } else {
            try {
                Class.forName("org.netbeans.ProxyURLStreamHandlerFactory").getMethod("register")
                                                                          .invoke(null);
            } catch (ClassNotFoundException | NoSuchMethodException |
                     SecurityException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
            final List<PathResourceImplementation> modules = new ArrayList<>();
            final File installDir = new File(System.getProperty("java.home"));
            final URI imageURI = getImageURI(installDir);
            try {
                final FileObject jrtRoot = URLMapper.findFileObject(imageURI.toURL());
                final FileObject root = getModulesRoot(jrtRoot);
                for (FileObject module : root.getChildren()) {
                    modules.add(ClassPathSupport.createResource(module.toURL()));
                }
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
            assertFalse(modules.isEmpty());
            return ClassPathSupport.createClassPath(modules);
        }
    }

    public static ClassPath getModuleBootPath() {
        if (System.getProperty("sun.boot.class.path") != null) {
            try {
                //JDK 8:
                return getModuleBootOnJDK8();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            return getBootClassPath();
        }
    }

    private static final String PROTOCOL = "nbjrt"; //NOI18N

    private static URI getImageURI(@NonNull final File jdkHome) {
        try {
            return new URI(String.format(
                "%s:%s!/%s",  //NOI18N
                PROTOCOL,
                BaseUtilities.toURI(jdkHome).toString(),
                ""));
        } catch (URISyntaxException e) {
            throw new IllegalStateException();
        }
    }

    @NonNull
    private static FileObject getModulesRoot(@NonNull final FileObject jrtRoot) {
        final FileObject modules = jrtRoot.getFileObject("modules");    //NOI18N
        //jimage v1 - modules are located in the root
        //jimage v2 - modules are located in "modules" folder
        return modules == null ?
            jrtRoot :
            modules;
    }

    //create fake "system module path" while running on JDK 8
    //java.base contains rt.jar and exports all java.* packages:
    private static ClassPath moduleBootOnJDK8;

    private static ClassPath getModuleBootOnJDK8() throws Exception {
        if (moduleBootOnJDK8 == null) {
            List<FileSystem> roots = new ArrayList<>();

            FileSystem output = FileUtil.createMemoryFileSystem();
            FileObject sink = output.getRoot();

            roots.add(output);

            Set<String> packages = new HashSet<>();
            for (FileObject r : getBootClassPath().getRoots()) {
                FileObject javaDir = r.getFileObject("java");

                if (javaDir == null)
                    continue;

                roots.add(r.getFileSystem());

                Enumeration<? extends FileObject> c = javaDir.getChildren(true);

                while (c.hasMoreElements()) {
                    FileObject current = c.nextElement();

                    if (!current.isData() || !current.hasExt("class")) continue;

                    String rel = FileUtil.getRelativePath(r, current.getParent());

                    packages.add(rel.replace('/', '.'));
                }
            }

            FileSystem outS = new MultiFileSystem(roots.toArray(new FileSystem[0])) {
                {
                    setSystemName("module-boot");
                }
            };

            Repository.getDefault().addFileSystem(outS);

            StringBuilder moduleInfo = new StringBuilder();

            moduleInfo.append("module java.base {\n");

            for (String pack : packages) {
                moduleInfo.append("    exports " + pack + ";\n");
            }

            moduleInfo.append("}\n");

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            FileObject javaBase = outS.getRoot();

            try (JavaFileManager fm = compiler.getStandardFileManager(null, null, null);
                 JFMImpl fmImpl = new JFMImpl(fm, javaBase, sink)) {
                compiler.getTask(null, fmImpl, null, Arrays.asList("-proc:none"), null,
                                 Arrays.asList(new SimpleJavaFileObject(new URI("mem:///module-info.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                        return moduleInfo.toString();
                    }
                })).call();
            }

            javaBase.refresh();
            moduleBootOnJDK8 = ClassPathSupport.createClassPath(javaBase);
        }

        return moduleBootOnJDK8;
    }

    private static class JFMImpl extends ForwardingJavaFileManager<JavaFileManager> {
        private final FileObject output;
        private final FileObject sink;

        public JFMImpl(JavaFileManager fileManager, FileObject output, FileObject sink) {
            super(fileManager);
            this.output = output;
            this.sink = sink;
        }

        @Override
        public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
            return Collections.emptyList();
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
            if (location == StandardLocation.CLASS_OUTPUT) {
                List<JavaFileObject> result = new ArrayList<>();
                FileObject pack = output.getFileObject(packageName.replace('.', '/'));

                if (pack != null) {
                    Enumeration<? extends FileObject> c = pack.getChildren(recurse);

                    while (c.hasMoreElements()) {
                        FileObject file = c.nextElement();
                        if (!file.hasExt("class"))
                            continue;
                        result.add(new InferableJavaFileObject(file, JavaFileObject.Kind.CLASS));
                    }
                }

                return result;
            }
            return super.list(location, packageName, kinds, recurse);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling) throws IOException {
            if (location == StandardLocation.CLASS_OUTPUT) {
                String relPath = className.replace('.', '/') + ".class";
                try {
                    return new SimpleJavaFileObject(new URI("mem://" + relPath), kind) {
                        @Override
                        public OutputStream openOutputStream() throws IOException {
                            return new ByteArrayOutputStream() {
                                @Override
                                public void close() throws IOException {
                                    super.close();
                                    FileObject target = FileUtil.createData(sink, relPath);
                                    try (OutputStream out = target.getOutputStream()) {
                                        out.write(toByteArray());
                                    }
                                }
                            };
                        }
                    };
                } catch (URISyntaxException ex) {
                    throw new IOException(ex);
                }
            }
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }

        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            if (file instanceof InferableJavaFileObject) {
                return ((InferableJavaFileObject) file).className;
            }
            return super.inferBinaryName(location, file);
        }

        private class InferableJavaFileObject extends SimpleJavaFileObject {

            private final String className;
            public InferableJavaFileObject(FileObject file, Kind kind) {
                super(file.toURI(), kind);
                String relPath = FileUtil.getRelativePath(output, file);
                className = relPath.substring(0, relPath.length() - ".class".length()).replace('/', '.');
            }
        }

    }

}
