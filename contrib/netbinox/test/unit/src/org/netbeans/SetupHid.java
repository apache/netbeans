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
package org.netbeans;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.CRC32;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.netbeans.junit.NbTestCase;

/** Some infrastructure for module system tests.
 * @author Jesse Glick
 */
public abstract class SetupHid extends NbTestCase {


    public SetupHid(String name) {
        super(name);
    }

    /** directory of data files for JARs */
    protected File data;
    /** directory full of JAR files to test */
    protected File jars;
    /** shall the generated JARs have all dir entries? */
    protected static boolean generateAllDirs;

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        createTestJARs();
        
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        
        System.setProperty("netbeans.user", ud.getPath());
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    /** same as FileUtil.copy */
    protected static void copyStreams(InputStream is, OutputStream os) throws IOException {
        final byte[] BUFFER = new byte[4096];
        int len;
        for (;;) {
            len = is.read(BUFFER);
            if (len == -1) {
                return;
            }
            os.write(BUFFER, 0, len);
        }
    }

    protected static void copy(File a, File b) throws IOException {
        OutputStream os = new FileOutputStream(b);
        try {
            copyStreams(new FileInputStream(a), os);
        } finally {
            os.close();
        }
    }

    /**
     * Create a fresh JAR file.
     * @param jar the file to create
     * @param contents keys are JAR entry paths, values are text contents (will be written in UTF-8)
     * @param manifest a manifest to store (key/value pairs for main section)
     */
    public static void createJar(File jar, Map<String,String> contents, Map<String,String> manifest) throws IOException {
        // XXX use TestFileUtils.writeZipFile
        Manifest m = new Manifest();
        m.getMainAttributes().putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        for (Map.Entry<String,String> line : manifest.entrySet()) {
            m.getMainAttributes().putValue(line.getKey(), line.getValue());
        }
        jar.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(jar);
        try {
            JarOutputStream jos = new JarOutputStream(os, m);
            Iterator it = contents.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String path = (String) entry.getKey();
                byte[] data = ((String) entry.getValue()).getBytes(StandardCharsets.UTF_8);
                JarEntry je = new JarEntry(path);
                je.setSize(data.length);
                CRC32 crc = new CRC32();
                crc.update(data);
                je.setCrc(crc.getValue());
                jos.putNextEntry(je);
                jos.write(data);
            }
            jos.close();
        } finally {
            os.close();
        }
    }

    private void createTestJARs() throws IOException {
        File simpleModule = createTestJAR("simple-module", null);
        File dependsOnSimpleModule = createTestJAR("depends-on-simple-module", null, simpleModule);
        createTestJAR("dep-on-dep-on-simple", null, simpleModule, dependsOnSimpleModule);
        File cyclic1;
        { // cyclic-1
            File cyclic1Src = new File(data, "cyclic-1");
            File cyclic2Src = new File(data, "cyclic-2");
            compile(Arrays.asList(
                    "-sourcepath", cyclic1Src + File.pathSeparator + cyclic2Src,
                    "-d", cyclic1Src.getAbsolutePath()),
                    findSourceFiles(cyclic1Src, cyclic2Src));
            cyclic1 = new File(jars, "cyclic-1.jar");
            OutputStream os = new FileOutputStream(cyclic1);
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(new File(data, "cyclic-1.mf")));
                try {
                    jarUp(jos, new File(cyclic1Src, "org/foo"), "org/foo/");
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
        }
        File cyclic2 = createTestJAR("cyclic-2", null, cyclic1);
        createTestJAR("depends-on-cyclic-1", null, cyclic1, cyclic2);
        File libraryUndecl = createTestJAR("library-undecl", "library-src");
        createTestJAR("library-unvers", "library-src");
        createTestJAR("library-vers", "library-src");
        createTestJAR("library-vers-partial", "library-src");
        createTestJAR("depends-on-lib-undecl", "depends-on-library-src", libraryUndecl);
        createTestJAR("depends-on-lib-unvers", "depends-on-library-src", libraryUndecl);
        createTestJAR("depends-on-lib-vers", "depends-on-library-src", libraryUndecl);
        createTestJAR("depends-on-lib-vers-partial", "depends-on-library-src", libraryUndecl);
        createTestJAR("fails-on-lib-undecl", "depends-on-library-src", libraryUndecl);
        createTestJAR("fails-on-non-existing-package", "depends-on-library-src", libraryUndecl);
        createTestJAR("fails-on-lib-unvers", "depends-on-library-src", libraryUndecl);
        createTestJAR("fails-on-lib-old", "depends-on-library-src", libraryUndecl);
        createTestJAR("prov-foo", null);
        createTestJAR("req-foo", null);
        createTestJAR("prov-foo-bar", null);
        createTestJAR("req-foo-baz", null);
        createTestJAR("prov-baz", null);
        createTestJAR("prov-foo-req-bar", null);
        createTestJAR("prov-bar-req-foo", null);
        createTestJAR("prov-bar-dep-cyclic", null);
        createTestJAR("rel-ver-2", null);
        createTestJAR("dep-on-relvertest-1", null);
        createTestJAR("dep-on-relvertest-1-2", null);
        createTestJAR("dep-on-relvertest-1-2-nospec", null);
        createTestJAR("dep-on-relvertest-2", null);
        createTestJAR("dep-on-relvertest-2-3", null);
        createTestJAR("dep-on-relvertest-2-3-late", null);
        createTestJAR("dep-on-relvertest-2-impl", null);
        createTestJAR("dep-on-relvertest-2-impl-wrong", null);
        createTestJAR("dep-on-relvertest-2-late", null);
        createTestJAR("dep-on-relvertest-3-4", null);
        createTestJAR("dep-on-relvertest-some", null);
        createTestJAR("depends-on-simple-module-2", null);
        createTestJAR("needs-foo", null);
        createTestJAR("recommends-foo", null);
        createTestJAR("prov-foo-depends-needs_foo", "prov-foo");
        createTestJAR("api-mod-export-all", "exposes-api");
        createTestJAR("api-mod-export-none", "exposes-api");
        File exposesAPI = createTestJAR("api-mod-export-api", "exposes-api");
        createTestJAR("api-mod-export-friend", "exposes-api");
        createTestJAR("uses-api-simple-dep", "uses-api", exposesAPI);
        createTestJAR("uses-api-impl-dep", "uses-api", exposesAPI);
        createTestJAR("uses-api-impl-dep-for-friends", "uses-api", exposesAPI);
        createTestJAR("uses-api-spec-dep", "uses-api", exposesAPI);
        createTestJAR("dep-on-two-modules", null);
        File usesAPI = createTestJAR("uses-and-exports-api", "uses-api", exposesAPI);
        createTestJAR("uses-api-transitively", null, exposesAPI, usesAPI);
        createTestJAR("uses-api-directly", "uses-api-transitively", exposesAPI, usesAPI);
        createTestJAR("uses-api-transitively-old", "uses-api-transitively", exposesAPI, usesAPI);
        createTestJAR("uses-api-directly-old", "uses-api-transitively", exposesAPI, usesAPI);
        createTestJAR("look-for-myself", null);
        createTestJAR("uses-api-friend", "uses-api", exposesAPI);
        createTestJAR("little-manifest", null);
        createTestJAR("medium-manifest", null);
        createTestJAR("big-manifest", null);
        createTestJAR("patchable", null);
        { // Make the patch JAR specially:
            File src = new File(data, "patch");
            String srcS = src.getAbsolutePath();
            compile(Arrays.asList("-sourcepath", srcS, "-d", srcS), findSourceFiles(src));
            File jar = new File(jars, "patches/pkg-subpkg/some-patch.jar");
            jar.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(jar);
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(null));
                try {
                    jarUp(jos, src, "");
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
        }
        File locale = new File(jars, "locale");
        locale.mkdirs();
        {
            OutputStream os = new FileOutputStream(new File(jars, "localized-manifest.jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(new File(data, "localized-manifest.mf")));
                try {
                    writeJarEntry(jos, "locmani/Bundle.properties", new File(data, "localized-manifest/locmani/Bundle.properties"));
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
            os = new FileOutputStream(new File(locale, "localized-manifest_cs.jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(null));
                try {
                    writeJarEntry(jos, "locmani/Bundle_cs.properties", new File(data, "localized-manifest/locmani/Bundle_cs.properties"));
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
        }
        {
            OutputStream os = new FileOutputStream(new File(jars, "base-layer-mod.jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(new File(data, "base-layer-mod.mf")));
                try {
                    writeJarEntry(jos, "baselayer/layer.xml", new File(data, "base-layer-mod/baselayer/layer.xml"));
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
            os = new FileOutputStream(new File(locale, "base-layer-mod_cs.jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(null));
                try {
                    writeJarEntry(jos, "baselayer/layer_cs.xml", new File(data, "base-layer-mod/baselayer/layer_cs.xml"));
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
            os = new FileOutputStream(new File(locale, "base-layer-mod_foo.jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, loadManifest(null));
                try {
                    writeJarEntry(jos, "baselayer/layer_foo.xml", new File(data, "base-layer-mod/baselayer/layer_foo.xml"));
                } finally {
                    jos.close();
                }
            } finally {
                os.close();
            }
            createTestJAR("override-layer-mod", null);
        }
    }
    private static void compile(List<String> options, Iterable<File> files) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager mgr = compiler.getStandardFileManager(null, null, null);
        List<String> fullOptions = new ArrayList<String>(options);
        fullOptions.addAll(Arrays.asList("-source", "1.8", "-target", "1.8"));
        if (!compiler.getTask(null, mgr, null, fullOptions, null, mgr.getJavaFileObjectsFromFiles(files)).call()) {
            throw new IOException("compilation failed");
        }
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
    public static File createTestJAR(File data, File jars, String name, String srcdir, File... classpath) throws IOException {
        File srcdirF = null;
        File d = new File(data, srcdir != null ? srcdir : name);
        if (d.isDirectory()) {
            srcdirF = d;
        }
        File manifestF = null;
        File f = new File(data, name + ".mf");
        if (f.isFile()) {
            manifestF = f;
        }
        if (srcdirF != null) {
            assert srcdirF.isDirectory();
            List<File> sourceFiles = findSourceFiles(srcdirF);
            if (!sourceFiles.isEmpty()) {
                StringBuilder cp = new StringBuilder(System.getProperty("java.class.path")); // o.o.util, o.o.modules
                for (File j : classpath) {
                    cp.append(File.pathSeparatorChar);
                    cp.append(j);
                }
                compile(Arrays.asList(
                        "-classpath", cp.toString(),
                        "-sourcepath", srcdirF.getAbsolutePath(),
                        "-d", srcdirF.getAbsolutePath()),
                        sourceFiles);
            }
        }
        // Cannot trivially use TestFileUtils.writeZipFile here since we have binary content (classes).
        File jar = new File(jars, name + ".jar");
        jars.mkdirs();
        OutputStream os = new FileOutputStream(jar);
        try {
            JarOutputStream jos = new JarOutputStream(os, loadManifest(manifestF));
            try {
                if (srcdirF != null) {
                    jarUp(jos, srcdirF, "");
                }
            } finally {
                jos.close();
            }
        } finally {
            os.close();
        }
        return jar;
    }
    private static Manifest loadManifest(File mani) throws IOException {
        Manifest m = new Manifest();
        if (mani != null) {
            InputStream is = new FileInputStream(mani);
            try {
                m.read(is);
            } finally {
                is.close();
            }
        }
        m.getMainAttributes().putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        return m;
    }
    private static List<File> findSourceFiles(File... roots) {
        List<File> sourceFiles = new ArrayList<File>();
        for (File root : roots) {
            doFindSourceFiles(sourceFiles, root);
        }
        return sourceFiles;
    }
    private static void doFindSourceFiles(List<File> sourceFiles, File srcdir) {
        for (File k : srcdir.listFiles()) {
            if (k.getName().endsWith(".java")) {
                sourceFiles.add(k);
            } else if (k.isDirectory()) {
                doFindSourceFiles(sourceFiles, k);
            }
        }
    }
    private static void jarUp(JarOutputStream jos, File dir, String prefix) throws IOException {
        for (File f : dir.listFiles()) {
            String path = prefix + f.getName();
            if (f.getName().endsWith(".java")) {
                continue;
            } else if (f.isDirectory()) {
                if (generateAllDirs) {
                    JarEntry je = new JarEntry(path + "/");
                    jos.putNextEntry(je);
                }
                jarUp(jos, f, path + "/");
            } else {
                writeJarEntry(jos, path, f);
            }
        }
    }
    private static void writeJarEntry(JarOutputStream jos, String path, File f) throws IOException, FileNotFoundException {
        JarEntry je = new JarEntry(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = new FileInputStream(f);
        try {
            copyStreams(is, baos);
        } finally {
            is.close();
        }
        byte[] data = baos.toByteArray();
        je.setSize(data.length);
        CRC32 crc = new CRC32();
        crc.update(data);
        je.setCrc(crc.getValue());
        jos.putNextEntry(je);
        jos.write(data);
    }

}
