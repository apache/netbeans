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

package org.openide.filesystems;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JarFileSystemHidden extends NbTestCase {

    public JarFileSystemHidden(String name) {
        super(name);
    }

    public void testLazyJarForNonExistingConstructor() throws Exception {
        File f = new File(getWorkDir(), "broken.jar");
        f.createNewFile();

        JarFileSystem fs = new JarFileSystem(f);

        URL u = fs.getRoot().toURL();
        assertNotNull("URL is OK", u);
        if (!u.toExternalForm().startsWith("jar:file") || !u.toExternalForm().endsWith("broken.jar!/")) {
            fail("Unexpected URL: " + u);

        }
        assertEquals("No children", 0, fs.getRoot().getChildren().length);
    }

    public void testEagerJarForNonExistingSetter() throws Exception {
        File f = new File(getWorkDir(), "broken.jar");
        f.createNewFile();

        JarFileSystem fs = new JarFileSystem();
        try {
            fs.setJarFile(f);
            fail("This shall fail, with JarException as the file cannot be opened");
        } catch (FSException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Error in JAR"));
        }

        assertEquals("No children", 0, fs.getRoot().getChildren().length);
    }

    public void testLazyOpen() throws Exception {
        File f = new File(getWorkDir(), "ok.jar");
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(f));
        jos.putNextEntry(new JarEntry("one"));
        jos.putNextEntry(new JarEntry("two.txt"));
        jos.putNextEntry(new JarEntry("3.txt"));
        jos.close();

        CharSequence log = Log.enable(JarFileSystem.class.getName(), Level.FINE);
        JarFileSystem fs = new JarFileSystem(f);
        final String match = "opened: " + f.getAbsolutePath();
        if (log.toString().contains(match)) {
            fail("The file " + f + " shall not be opened when fs created:\n" + log);
        }

        URL u = fs.getRoot().toURL();
        assertNotNull("URL is OK", u);
        if (!u.toExternalForm().startsWith("jar:file") || !u.toExternalForm().endsWith("ok.jar!/")) {
            fail("Unexpected URL: " + u);
        }
        if (log.toString().contains(match)) {
            fail("The file " + f + " shall not be opened yet:\n" + log);
        }
        assertEquals("Three files", 3, fs.getRoot().getChildren().length);
        if (!log.toString().contains(match)) {
            fail("The file " + f + " shall be opened now:\n" + log);
        }
    }

    /**
     * Test for bug 238632.
     *
     * @throws java.io.IOException
     */
    public void testBrokenSignature() throws IOException {

        clearWorkDir();
        File dir = getWorkDir();
        File jarFile = new File(dir, "file.jar");
        File keystoreFile = new File(dir, "keystore.jks");

        // create the jar
        Manifest manifest = new Manifest();
        try {
            OutputStream os = new FileOutputStream(jarFile);
            JarOutputStream jos = new JarOutputStream(os, manifest);
            jos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace(System.out);
            return;
        }
        try {
            // create a key store
            final String[] args = new String[]{"-genkey",
                "-alias", "t_alias",
                "-keyalg", "RSA",
                "-storepass", "testpass",
                "-keypass", "testpass",
                "-dname", "CN=Test, OU=QA, O=Test Org, L=Test Village,"
                + " S=Testonia, C=Test Republic",
                "-keystore", keystoreFile.getAbsolutePath()};
            //sun.security.tools.keytool.Main not public API and no more in
            //ct.sym, compilation requires -XDignore.symbol.file which will not work in JDK 9.
            //Better to use reflection
            final Class<?> clz = Class.forName("sun.security.tools.keytool.Main");
            final Method m = clz.getDeclaredMethod("main", args.getClass());
            m.setAccessible(true);  //JDK9 requires
            m.invoke(null, (Object)args);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            return;
        }

        // sign the jar
        try {
            Class<?> jarSignererMain = Class.forName("sun.security.tools.jarsigner.Main");
            Method main = jarSignererMain.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{
                "-keystore", keystoreFile.getAbsolutePath(),
                "-storepass", "testpass",
                jarFile.getAbsolutePath(),
                "t_alias"});
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            return;
        }

        // break the signature
        File brokenJar = new File(dir, "broken.jar");
        File extractDir = new File(dir, "extracted");
        extractDir.mkdir();
        unzip(jarFile, extractDir);
        damageSignature(extractDir);
        zip(extractDir, brokenJar);

        // check the broken MANIFEST.MF can be read
        FileSystem fs = new JarFileSystem(brokenJar);
        FileObject rootFO = fs.getRoot();
        FileObject manifestFO = rootFO.getFileObject("META-INF/MANIFEST.MF");
        assertNotNull(manifestFO);
        InputStream is = manifestFO.getInputStream();
        try {
            is.read();
        } finally {
            is.close();
        }
    }

    private void unzip(File zipFile, File targetDirectory)
            throws FileNotFoundException, IOException {

        InputStream fis = new FileInputStream(zipFile);
        try {
            ZipInputStream zis = new ZipInputStream(fis);
            try {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String name = entry.getName();
                    String[] parts = name.split("/");
                    File file = targetDirectory;
                    for (int i = 0; i < parts.length - 1; i++) {
                        file = new File(file, parts[i]);
                    }
                    file.mkdirs();
                    file = new File(file, parts[parts.length - 1]);
                    OutputStream os = new FileOutputStream(file);
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(os);
                        byte[] buffer = new byte[1024];
                        int read;
                        try {
                            while ((read = zis.read(buffer)) != -1) {
                                bos.write(buffer, 0, read);
                            }
                        } finally {
                            bos.close();
                        }
                    } finally {
                        os.close();
                    }
                }
            } finally {
                zis.close();
            }
        } finally {
            fis.close();
        }
    }

    private void zip(File directory, File targetZipFile)
            throws FileNotFoundException, IOException {

        OutputStream os = new FileOutputStream(targetZipFile);
        try {
            ZipOutputStream zos = new ZipOutputStream(os);
            try {
                zipFiles("", directory, zos);
            } finally {
                zos.close();
            }
        } finally {
            os.close();
        }
    }

    private void zipFiles(String path, File root, ZipOutputStream zos)
            throws IOException {

        for (File f : root.listFiles()) {
            ZipEntry ze = new ZipEntry(path + f.getName());
            zos.putNextEntry(ze);
            if (f.isDirectory()) {
                zipFiles(ze.getName() + "/", f, zos);
            } else {
                InputStream fis = new FileInputStream(f);
                try {
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    try {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = bis.read(buffer)) != -1) {
                            zos.write(buffer, 0, read);
                        }
                    } finally {
                        bis.close();
                    }
                } finally {
                    fis.close();
                }
            }
        }
    }

    /**
     * Replace the first equals character in META-INF/T_ALIAS.SF "=" found
     * after text "Digest-Manifest-Main-Attributes" with character "!".
     */
    private void damageSignature(File extractDir)
            throws FileNotFoundException, IOException {

        File metaInfFile = new File(extractDir, "META-INF");
        File tAliasSfFile = new File(metaInfFile, "T_ALIAS.SF");
        FileInputStream fis = new FileInputStream(tAliasSfFile);
        StringBuilder sb = new StringBuilder();
        try {
            BufferedInputStream bis = new BufferedInputStream(fis);
            try {
                InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8);
                try {
                    char[] buffer = new char[1024];
                    int read;
                    while ((read = isr.read(buffer)) != -1) {
                        sb.append(buffer, 0, read);
                    }
                } finally {
                    isr.close();
                }
            } finally {
                bis.close();
            }
        } finally {
            fis.close();
        }
        int mainAttsStart = sb.indexOf("Digest-Manifest-Main-Attributes");
        int replacePos = sb.indexOf("=", mainAttsStart);
        sb.replace(replacePos, replacePos + 1, "!");
        FileOutputStream fos = new FileOutputStream(tAliasSfFile);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try {
                PrintStream ps = new PrintStream(bos);
                try {
                    ps.append(sb);
                } finally {
                    ps.close();
                }
            } finally {
                bos.close();
            }
        } finally {
            fos.close();
        }
    }

    public void testToFileNotUsingURLMapper() throws Exception {

        MockServices.setServices(CheckUrlMapper.class);
        try {
            CheckUrlMapper m = Lookup.getDefault().lookup(CheckUrlMapper.class);
            assertNotNull(m);
            m.clear();

            File f = new File(getWorkDir(), "some.jar");
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(f));
            jos.putNextEntry(new JarEntry("text.txt"));
            jos.close();

            CharSequence log = Log.enable(JarFileSystem.class.getName(), Level.FINE);
            JarFileSystem fs = new JarFileSystem(f);

            FileObject fo = fs.getRoot().getFileObject("text.txt");
            File file = FileUtil.toFile(fo);

            assertNotNull("FileObject for archived file should exist", fo);
            assertNull("java.io.File for archived file should not exist", file);
            assertTrue("URLMapper should not be used in FileUtil.toFile",
                    m.getCalls() == 0);
            m.clear();
        } finally {
            MockServices.setServices();
        }
    }

    /**
     * Entries over 100000 bytes are read through a temporary file. Two streams
     * must not share that file: here the two jar paths have the same
     * {@code String.hashCode()} ("Aa" and "BB" hash alike), and each stream must
     * still return the content of its own jar.
     */
    public void testLargeEntriesOfJarsWithEqualPathHashesAreReadCorrectly() throws Exception {
        clearWorkDir();
        File jarA = new File(getWorkDir(), "Aa.jar");
        File jarB = new File(getWorkDir(), "BB.jar");
        assertEquals(jarA.getPath().hashCode(), jarB.getPath().hashCode());
        byte[] contentA = randomBytes(200000, 1);
        byte[] contentB = randomBytes(200000, 2);
        writeJar(jarA, "big.bin", contentA);
        writeJar(jarB, "big.bin", contentB);

        FileObject foA = new JarFileSystem(jarA).findResource("big.bin");
        FileObject foB = new JarFileSystem(jarB).findResource("big.bin");
        try (InputStream inA = foA.getInputStream(); InputStream inB = foB.getInputStream()) {
            assertTrue("Content of Aa.jar", Arrays.equals(contentA, inA.readAllBytes()));
            assertTrue("Content of BB.jar", Arrays.equals(contentB, inB.readAllBytes()));
        }
    }

    /**
     * Concurrent reads of an entry over 100000 bytes must each return the whole
     * entry.
     */
    public void testConcurrentReadsOfLargeEntry() throws Exception {
        clearWorkDir();
        File jar = new File(getWorkDir(), "big.jar");
        byte[] content = randomBytes(1 << 20, 3);
        writeJar(jar, "big.bin", content);
        FileObject fo = new JarFileSystem(jar).findResource("big.bin");

        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < 8; t++) {
                futures.add(executor.submit(() -> {
                    for (int i = 0; i < 50; i++) {
                        assertTrue("Whole entry read", Arrays.equals(content, fo.asBytes()));
                    }
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static byte[] randomBytes(int length, long seed) {
        byte[] bytes = new byte[length];
        new Random(seed).nextBytes(bytes);
        return bytes;
    }

    private static void writeJar(File jar, String entryName, byte[] content) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar))) {
            jos.putNextEntry(new JarEntry(entryName));
            jos.write(content);
        }
    }

    /**
     * Used in {@link #testToFileNotUsingURLMapper()}. To check that NO URL
     * mappers are used in {@link FileUtil#toFile(FileObject) } when the passed
     * FileObject represents a JAR archived file (which is sure not to have
     * {@link java.io.File} representation).
     */
    public static class CheckUrlMapper extends URLMapper {

        private int calls = 0;

        @Override
        public URL getURL(FileObject fo, int type) {
            calls += 1;
            return null;
        }

        @Override
        public FileObject[] getFileObjects(URL url) {
            return null;
        }

        void clear() {
            calls = 0;
        }

        int getCalls() {
            return calls;
        }
    }
}
