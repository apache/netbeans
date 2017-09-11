/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
            sun.security.tools.jarsigner.Main.main(new String[]{
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
                InputStreamReader isr = new InputStreamReader(bis, "UTF-8");
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
