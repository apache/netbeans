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
package org.netbeans.core.validation;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

public class ValidateClassFilesTest extends NbTestCase {
    public ValidateClassFilesTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(ValidateClassFilesTest.class).
                clusters(".*").gui(false).honorAutoloadEager(true).suite();
    }

    public void testCheckClassFiles() throws IOException {
        String platformDir = System.getProperty("netbeans.home");
        final StringBuilder err = new StringBuilder();
        checkClassFiles(platformDir, err);
        for (String cluster : System.getProperty("netbeans.dirs").split(File.pathSeparator)) {
            checkClassFiles(cluster, err);
        }
        if (err.length() != 0) {
            fail(err.toString());
        }
    }

    private void checkClassFiles(String c, final StringBuilder err) throws IOException {
        if (c == null) {
            return;
        }
        File cluster = new File(c);
        Files.walkFileTree(cluster.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                final File file = path.toFile();
                if (file.getName().endsWith(".jar")) {
                    if (file.getName().startsWith("javafx-")) {
                        if (file.getParentFile().getName().equals("ext")) {
                            File c = file.getParentFile().getParentFile().getParentFile();
                            if (c.getName().equals("extra")) {
                                return FileVisitResult.CONTINUE;
                            }
                        }
                    }

                    JarFile jf = new JarFile(file);
                    Enumeration<JarEntry> en = jf.entries();
                    while (en.hasMoreElements()) {
                        JarEntry entry = en.nextElement();
                        if (entry.getName().equals("module-info.class")) {
                            continue;
                        }
                        if (entry.getName().startsWith("META-INF/versions/")) {
                            continue;
                        }

                        if (entry.getName().endsWith(".class")) {
                            try (DataInputStream dis = new DataInputStream(jf.getInputStream(entry))) {
                                long magic = dis.readInt();
                                short minor = dis.readShort();
                                short major = dis.readShort();

                                if (magic != 0xcafebabe || major > 52) {
                                    err.append("\n found version " + major + "." + minor + " in " + entry.getName() + " in " + file);
                                }
                            }
                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
