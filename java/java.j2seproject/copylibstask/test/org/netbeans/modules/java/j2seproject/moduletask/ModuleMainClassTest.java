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
package org.netbeans.modules.java.j2seproject.moduletask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.java.j2seproject.moduletask.classfile.Attribute;
import org.netbeans.modules.java.j2seproject.moduletask.classfile.ClassFile;
import org.netbeans.modules.java.j2seproject.moduletask.classfile.ConstantPool;

/**
 *
 * @author Tomas Zezula
 */
public final class ModuleMainClassTest {
    private static final String PROP_TEST_ROOT = "ModuleMainClassTest.testRoot";    //NOI18N
    private static final String explicitTestRoot = null; //"/Users/tom/Projects/netbeans/jet-main/java.source.base/build/classes";
    
    public ModuleMainClassTest() {
    }

    @Test
    public void testClassWriterCorrectness() throws IOException {
        clearWorkDir();
        final File testRoot = getTestRoot();
        if (testRoot == null) {
            System.err.println("No test root.");
            return;
        }
        final Path from = testRoot.toPath();
        final Path to = getWorkDir().toPath();
        copyClasses(from, to);
        compare(from, to);
    }

    @Test
    public void testClassToString() throws IOException {
        clearWorkDir();
        final File testRoot = getTestRoot();
        if (testRoot == null) {
            System.err.println("No test root.");
            return;
        }
        final Path from = testRoot.toPath();
        Files.walk(from)
                .filter(new Predicate<Path>() {
                    @Override
                    public boolean test(Path p) {
                        return Files.isRegularFile(p) &&
                                p.getName(p.getNameCount()-1).toString().endsWith(".class") &&
                                !p.getName(p.getNameCount()-1).toString().contains("$");    //NOI18N
                    }
                })
                .limit(1)
                .forEach(new Consumer<Path>() {
                        @Override
                        public void accept(Path p) {
                            try {
                                System.out.println(p);
                                try(InputStream in = Files.newInputStream(p)) {
                                    final ClassFile cf = new ClassFile(in);
                                    System.out.println(cf);
                                }
                            } catch (IOException ioe) {
                                throw new RuntimeException(ioe);
                            }
                        }
                });
    }

    @Test
    public void testPatchModuleInfo() throws IOException {
        clearWorkDir();
        final Path wd = getWorkDir().toPath();
        final Path mi = wd.resolve("module-info.class");    //NOI18N
        try(final InputStream in = this.getClass().getResourceAsStream("resources/module-info.bin")) {  //NOI18N
            Files.copy(in, mi);
        }
        assertNull(getMainClass(mi));
        ModuleMainClass mmc = new ModuleMainClass();
        mmc.setMainclass("javaapplication40.Main1");    //NOI18N
        mmc.setModuleinfo(mi.toFile());
        mmc.execute();
        assertEquals("javaapplication40.Main1", getMainClass(mi));  //NOI18N
        mmc = new ModuleMainClass();
        mmc.setMainclass("javaapplication40.Main2");    //NOI18N
        mmc.setModuleinfo(mi.toFile());
        mmc.execute();
        assertEquals("javaapplication40.Main2", getMainClass(mi));  //NO18N
    }

    private File getWorkDir() {
        final File tmp  = new File(System.getProperty("java.io.tmpdir"));   //NOI18N
        final File workDir = new File(getClass().getName());
        workDir.mkdirs();
        return workDir;
    }

    private void clearWorkDir() {
        delete(getWorkDir());
    }

    private String getMainClass(Path p) throws IOException {
        try(final InputStream in = Files.newInputStream(p)) {
            final ClassFile cf = new ClassFile(in);
            final ConstantPool cp = cf.getConstantPool();
            for (Attribute attr : cf.getAttributes()) {
                int nameIndex = attr.getNameIndex();
                ConstantPool.CPInfo entry = cp.get(nameIndex);
                if ("ModuleMainClass".equals(entry.getValue())) {   //NOI18N
                    byte[] value = attr.getValue();
                    nameIndex = (value[0] & 0xff) << 8 | (value[1] & 0xff);
                    entry = cp.get(nameIndex);
                    return ((String)entry.getValue()).replace('/', '.');    //NOI18N
                }
            }
        }
        return null;
    }

    private static void delete(File file) {
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    delete(child);
                }
            }
        }
        file.delete();
    }

    private static File getTestRoot() {
        String path = null;
        if (explicitTestRoot != null) {
            path = explicitTestRoot;
        } else {
            path = System.getProperty(PROP_TEST_ROOT);
        }
        return path != null ?
                new File(path) :
                null;
    }


    private static void copyClasses(final Path from, final Path to) throws IOException {
        Files.walk(from, FileVisitOption.FOLLOW_LINKS)
                .filter(new Predicate<Path>() {
                    @Override
                    public boolean test(Path p) {
                        return Files.isRegularFile(p) &&
                                p.getName(p.getNameCount()-1).toString().endsWith(".class");    //NOI18N
                    }
                })
                .forEach(new Consumer<Path>() {
                        @Override
                        public void accept(Path p) {
                            try {
                                final Path rel = from.relativize(p);
                                final Path dest = to.resolve(rel);
                                Files.createDirectories(dest.getParent());
                                System.out.println("Copy: " + rel);
                                copyClass(p,dest);
                            } catch (IOException ioe) {
                                throw new RuntimeException(ioe);
                            }
                        }
                });
    }

    private static void copyClass(
            final Path from,
            final Path to) throws IOException {
        try(InputStream in = Files.newInputStream(from);
            OutputStream out = Files.newOutputStream(to)) {
            final ClassFile cf = new ClassFile(in);
            cf.write(out);
        }
    }

    private static void compare(
        final Path source,
        final Path target) throws IOException {
        Files.walk(source, FileVisitOption.FOLLOW_LINKS)
                .filter(new Predicate<Path>() {
                    @Override
                    public boolean test(Path p) {
                        return Files.isRegularFile(p) &&
                                p.getName(p.getNameCount()-1).toString().endsWith(".class");    //NOI18N
                    }
                })
                .forEach(new Consumer<Path>() {
                        @Override
                        public void accept(Path p) {
                            try {
                                final Path rel = source.relativize(p);
                                final Path dest = target.resolve(rel);
                                final byte[] sd = Files.readAllBytes(p);
                                final byte[] td = Files.readAllBytes(dest);
                                System.out.println("Compare: " + rel);
                                assertTrue(Arrays.equals(sd,td));
                            } catch (IOException ioe) {
                                throw new RuntimeException(ioe);
                            }
                        }
                });
    }
}
