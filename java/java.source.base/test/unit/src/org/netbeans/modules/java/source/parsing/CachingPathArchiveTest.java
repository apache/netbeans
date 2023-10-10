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
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Zezula
 */
public final class CachingPathArchiveTest extends NbTestCase {

    public CachingPathArchiveTest(String name) {
        super(name);
    }

    public void testArchiveCorrectness() throws IOException {
        final Path fsRoot = TestUtilities.getJRTFS();
        if (fsRoot != null) {
            List<? extends Path> modules = getModules(fsRoot);
            for (Path module : modules) {
                verifyModule(module);
            }
        } else {
            System.out.println("No JDK 9, nothing to test");
        }
    }

    public void testFileObjects() throws IOException {
        final Path fsRoot = TestUtilities.getJRTFS();
        if (fsRoot != null) {
            List<? extends Path> modules = getModules(fsRoot);
            for (Path module : modules) {
                verifyURIs(module);
            }
        } else {
            System.out.println("No JDK 9, nothing to test");
        }
    }

    public void testGetDirectory() throws IOException {
        clearWorkDir();
        File wd = getWorkDir();
        File dir1 = new File(new File(wd, "dir1"), "a");
        assertTrue(dir1.mkdirs());
        File dir2 = new File(new File(wd, "dir2"), "a");
        assertTrue(dir2.mkdirs());
        new FileOutputStream(new File(dir2, "test.txt")).close();
        Archive a = new CachingPathArchive(wd.toPath(), wd.toURI());
        assertEquals(dir1.toURI(), a.getDirectory("dir1/a"));
        assertEquals(dir2.toURI(), a.getDirectory("dir2/a"));
    }

    private static void verifyURIs(Path module) throws IOException {
        final URI javaHome = BaseUtilities.toURI(TestUtilities.getJava9Home());
        final URI rootURI = URI.create("nbjrt:"+javaHome+"!"+module+"/");
        final CachingPathArchive cpa = new CachingPathArchive(module, rootURI);
        final PathArchive pa = new PathArchive(module, rootURI);
        final Set<String> pkgs = getPackages(module).keySet();
        for (String pkg : pkgs) {
            final Iterable<? extends JavaFileObject> cjfos = cpa.getFiles(pkg, null, null, null, false);
            final Iterable<? extends JavaFileObject> jfos = pa.getFiles(pkg, null, null, null, false);
            assertJFOEquals(jfos, cjfos);
        }
    }

    private static void assertJFOEquals(
        @NonNull final Iterable<? extends JavaFileObject> i1,
        @NonNull final Iterable<? extends JavaFileObject> i2) {
        final Map<String,JavaFileObject> i1byName = byName(i1);
        final Map<String,JavaFileObject> i2byName = byName(i2);
        assertEquals(i1byName.size(), i2byName.size());
        for (Map.Entry<String,JavaFileObject> i1e : i1byName.entrySet()) {
            final FileObjects.Base jfo1 = (FileObjects.Base) i1e.getValue();
            final FileObjects.Base jfo2 = (FileObjects.Base) i2byName.get(i1e.getKey());
            assertNotNull(jfo2);
            assertEquals(jfo1.getPackage(), jfo2.getPackage());
            assertEquals(jfo1.getNameWithoutExtension(), jfo2.getNameWithoutExtension());
            assertEquals(jfo1.getExt(), jfo2.getExt());
            assertEquals(jfo1.getKind(), jfo2.getKind());
            assertEquals(jfo1.getPath(), jfo2.getPath());
            assertEquals(jfo1.inferBinaryName(), jfo2.inferBinaryName());            
            assertEquals(jfo1.toUri(), jfo2.toUri());            
            assertEquals(jfo1.getLastModified(), jfo2.getLastModified());
        }
    }

    private static Map<String,JavaFileObject> byName(@NonNull final Iterable<? extends JavaFileObject> it) {
        final Map<String,JavaFileObject> res = new HashMap<>();
        for (JavaFileObject jfo : it) {
            res.put(jfo.getName(), jfo);
        }
        return res;
    }

    private static void verifyModule(Path module) throws IOException {
        final CachingPathArchive cpa = new CachingPathArchive(module, null);
        final Map<String,Set<String>> pkgs = getPackages(module);
        for (Map.Entry<String,Set<String>> pkg : pkgs.entrySet()) {
            final Iterable<? extends JavaFileObject> res = cpa.getFiles(pkg.getKey(), null, null, null, false);
            assertPkgEquals(pkg.getKey(), pkg.getValue(), res);
        }
    }

    private static void assertPkgEquals(final String pkgName, final Set<String> expected, Iterable<? extends JavaFileObject> res) {
        final Set<String> ecp = new HashSet<>(expected);
        for (JavaFileObject jfo : res) {
            assertTrue(
                    String.format("In: %s expected: %s got: %s",
                        pkgName,
                        sorted(expected),
                        sorted(asNames(res))),
                    ecp.remove(jfo.getName()));
        }
        assertTrue(
                String.format("In: %s expected: %s got: %s",
                    pkgName,
                    sorted(expected),
                    sorted(asNames(res))),
                ecp.isEmpty());
    }

    @NonNull
    private static <T extends Comparable> List<T> sorted (@NonNull final Collection<T> c) {
        final List<T> res = (c instanceof List) ?
                (List<T>) c :
                new ArrayList<>(c);
        Collections.sort(res);
        return res;
    }

    @NonNull
    private static List<String> asNames(Iterable<? extends JavaFileObject> files) {
        final List<String> res = new ArrayList<>();
        for (JavaFileObject jfo : files) {
            res.add(jfo.getName());
        }
        return res;
    }

    private static Map<String,Set<String>> getPackages(@NonNull final Path module) throws IOException {
        final Map<String,Set<String>> res = new HashMap<>();
        Files.walkFileTree(module, new FileVisitor<Path>() {
            final Deque<Set<String>> state = new ArrayDeque<>();
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                state.offer(new HashSet<String>());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                state.getLast().add(file.getFileName().toString());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                final Set<String> folder = state.removeLast();
                if (!folder.isEmpty()) {
                    res.put(module.relativize(dir).toString(), folder);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return res;
    }

    @NonNull
    private static List<? extends Path> getModules(Path fsRoot) throws IOException {
        final List<Path> modules = new ArrayList<>();
        for (Path p : Files.newDirectoryStream(fsRoot.resolve("modules"))) {    //NOI18N
            if (Files.isDirectory(p)) {
                modules.add(p);
            }
        }
        return modules;
    }    
}
