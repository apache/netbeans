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
package org.netbeans.modules.maven.execute.cmd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.maven.options.MavenSettings;
import org.openide.util.Utilities;

import static org.junit.Assert.*;
/**
 *
 *
 */
public class ShellConstructorTest {

    private static final File MAVENMOCK_DIR = new File(System.getProperty("xtest.data"), "mavenmock");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Prepare mock maven directories
        createMock("2.2", "2.2.1");
        createMock("3.0.5", "3.0.5");
        createMock("3.3.1", "3.3.1");
        createMock("4.0.0", "4.0.0");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Files.walkFileTree(MAVENMOCK_DIR.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path t, BasicFileAttributes bfa) throws IOException {
                Files.delete(t);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
                throw ioe;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
                Files.delete(t);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        resetOs();
    }

    @Before
    public void setUp() throws Exception {
        resetOs();
    }

    private static void createMock(String dirName, String version) throws IOException {
        File mockDir = new File(new File(MAVENMOCK_DIR, dirName), "lib");
        mockDir.mkdirs();
        Properties properties = new Properties();
        properties.setProperty("version", version);
        properties.setProperty("groupId", "org.apache.maven");
        properties.setProperty("artifactId", "maven.core");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Ant-Version", "Apache Ant 1.9.4");
        manifest.getMainAttributes().putValue("Created-By", "1.8.0_40-b25 (Oracle Corporation)");
        try (FileOutputStream fos = new FileOutputStream(new File(mockDir, "fake" + version.replaceAll("\\D", "") + ".jar"));
                JarOutputStream jf = new JarOutputStream(fos, manifest)) {
            jf.putNextEntry(new JarEntry("META-INF/maven/org.apache.maven/maven-core/pom.properties"));
            properties.store(jf, "Maven mock properties");
        }
    }

    private void resetOs() throws Exception {
        // hack to call reset OS of BaseUtilies
        Class<?> classz = Class.forName("org.openide.util.BaseUtilities");
        Method m = classz.getDeclaredMethod("resetOperatingSystem");
        m.setAccessible(true);
        m.invoke(null);
    }

    /**
     * Test of construct method, of class ShellConstructor.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testShellConstructoronLinux() throws Exception {
        String previous = System.getProperty("os.name");
        System.getProperties().put("os.name", "Linux");
        assertFalse("Must be linux", Utilities.isWindows());
        System.getProperties().put("os.name", previous);

        assertTrue("2.2 linux", getCLI("2.2", "2.2.1", "mvn"));
        assertTrue("3.0.5 linux", getCLI("3.0.5", "3.0.5", "mvn"));
        assertTrue("3.3.1 linux", getCLI("3.3.1", "3.3.1", "mvn"));
        assertTrue("4.0.0 linux", getCLI("4.0.0", "4.0.0", "mvn"));
        System.getProperties().put("os.name", previous);
    }

    @Test
    public void testShellconstructoronWindows() throws Exception {
        String previous = System.getProperty("os.name");
        System.getProperties().put("os.name", "Windows ");
        assertTrue("Must be windows", Utilities.isWindows());
        System.getProperties().put("os.name", previous);
        assertTrue("2.2 windows", getCLI("2.2", "2.2.1", "mvn.bat"));
        assertTrue("3.0.5 windows", getCLI("3.0.5", "3.0.5", "mvn.bat"));
        assertTrue("3.3.1 windows", getCLI("3.3.1", "3.3.1", "mvn.cmd"));
        assertTrue("4.0.0 windows", getCLI("4.0.0", "4.0.0", "mvn.cmd"));
        
        System.getProperties().put("os.name", previous);
    }

    private boolean getCLI(String folder, String requestedversion, String mvn) {
        File sourceJar = new File(MAVENMOCK_DIR, folder + "/");
        String version = MavenSettings.getCommandLineMavenVersion(sourceJar);
        assertEquals(requestedversion, version);
        ShellConstructor shellConstructor = new ShellConstructor(sourceJar);
        List<String> construct = shellConstructor.construct();
        if (Utilities.isWindows()) {
            assertTrue("cli must contains " + mvn, construct.get(2).contains(mvn));
        } else {
            assertTrue("cli must contains " + mvn, construct.get(0).contains(mvn));
        }
        return true;
    }
}
