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
package org.netbeans.modules.netbinox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.ModuleManager;
import org.openide.util.test.TestFileUtils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static junit.framework.TestCase.assertEquals;

public class NetbinoxMultiversionJarTest extends NetigsoHid {

    public NetbinoxMultiversionJarTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        Locale.setDefault(new Locale("te", "ST"));
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());

        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();

        File classes = new File(getWorkDir(), "classes");
        classes.mkdirs();
        ToolProvider.getSystemJavaCompiler()
                .getTask(null, null, d -> {
                    throw new IllegalStateException(d.toString());
                }, Arrays.asList("-d", classes.getAbsolutePath()), null,
                        Arrays.asList(new SourceFileObject("test/Impl.java", "package test; public class Impl { public static String get() { return \"base\"; } }"),
                                new SourceFileObject("api/API.java", "package api; public class API { public static String run() { return test.Impl.get(); } }")))
                .call();
        File classes9 = new File(new File(new File(classes, "META-INF"), "versions"), "9");
        classes9.mkdirs();
        ToolProvider.getSystemJavaCompiler()
                .getTask(null, null, d -> {
                    throw new IllegalStateException(d.toString());
                }, Arrays.asList("-d", classes9.getAbsolutePath(), "-classpath", classes.getAbsolutePath()), null,
                        Arrays.asList(new SourceFileObject("test/Impl.java", "package test; public class Impl { public static String get() { return \"9\"; } }")))
                .call();
        Map<String, byte[]> jarContent = new LinkedHashMap<>();
        String manifest
                = "Manifest-Version: 1.0\n"
                + "Bundle-SymbolicName: test.module\n"
                + "Bundle-Version: 1.0\n"
                + "Multi-Release: true\n"
                + "";
        jarContent.put("META-INF/MANIFEST.MF", manifest.getBytes(UTF_8));
        Path classesPath = classes.toPath();
        Files.walk(classesPath)
                .filter(p -> Files.isRegularFile(p))
                .forEach(p -> {
                    try {
                        jarContent.put(classesPath.relativize(p).toString(), TestFileUtils.readFileBin(p.toFile()));
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                });
        jarContent.put("test/dummy.txt", "base".getBytes(UTF_8));
        jarContent.put("META-INF/versions/9/test/dummy.txt", "9".getBytes(UTF_8));
        simpleModule = new File(jars, "multi-release.jar");
        try ( OutputStream out = new FileOutputStream(simpleModule)) {
            TestFileUtils.writeZipFile(out, jarContent);
        }
    }

    public void testMultiReleaseJar() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        Set<org.netbeans.Module> all = null;
        try {
            org.netbeans.Module m1 = mgr.create(simpleModule, null, false, false, false);
            all = Collections.singleton(m1);

            mgr.enable(all);

            // Check multi release class loading
            Class<?> impl = m1.getClassLoader().loadClass("test.Impl");
            Method get = impl.getMethod("get");
            String output = (String) get.invoke(null);

            String expected;
            try {
                Class.forName("java.lang.Runtime$Version");
                expected = "9";
            } catch (ClassNotFoundException ex) {
                expected = "base";
            }
            assertEquals(expected, output);

            // Check multi release resource loading
            try(InputStream is = m1.getClassLoader().getResourceAsStream("test/dummy.txt")) {
                assertEquals(expected, loadUTF8(is));
            }

        } finally {
            if (all != null) {
                mgr.disable(all);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }

    }

    private static String loadUTF8(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int read;
        while ((read = is.read(buffer)) > 0) {
            baos.write(buffer, 0, read);
        }
        return baos.toString("UTF-8");
    }

    private static final class SourceFileObject extends SimpleJavaFileObject {

        private final String content;

        public SourceFileObject(String path, String content) throws URISyntaxException {
            super(new URI("mem://" + path), JavaFileObject.Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return content;
        }

    }
}
