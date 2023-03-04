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
package org.netbeans.modules.java.source.parsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.java.source.base.SourceLevelUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class MRJARModuleFileManagerTest extends NbTestCase {

    private ClassPath bCp;
    private ClassPath mvCp;

    public MRJARModuleFileManagerTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final File wd = FileUtil.normalizeFile(getWorkDir());
        final File bJar = createTestJar(new File(wd,"broken.jar"), false); //NOI18N
        final File mvJar = createTestJar(new File(wd,"mr.jar"), true); //NOI18N
        bCp = ClassPathSupport.createClassPath(FileUtil.urlForArchiveOrDir(bJar));
        mvCp = ClassPathSupport.createClassPath(FileUtil.urlForArchiveOrDir(mvJar));
    }

    public void testList() throws IOException {
        //Source level 9, broken jar
        ModuleFileManager fm = new ModuleFileManager(
                CachingArchiveProvider.getDefault(),
                bCp,
                (u)->Collections.singleton(u),
                SourceLevelUtils.JDK1_9,
                StandardLocation.MODULE_PATH,
                false);
        JavaFileManager.Location l = StreamSupport.stream(fm.listLocationsForModules(StandardLocation.MODULE_PATH).spliterator(), true)
                .flatMap((s)->s.stream())
                .findFirst()
                .orElse(null);
        assertNotNull(l);
        Iterable<JavaFileObject> res = fm.list(l, "org.me", EnumSet.of(JavaFileObject.Kind.CLASS), false);    //NOI18N
        assertEquals(Arrays.asList("A Base","B Base"), toContent(res));  //NOI18N
        assertEquals(Arrays.asList("org.me.A","org.me.B"), toInferedName(fm, res));  //NOI18N
        //Source level 9, multi release jar
        fm = new ModuleFileManager(
                CachingArchiveProvider.getDefault(),
                mvCp,
                (u)->Collections.singleton(u),
                SourceLevelUtils.JDK1_9,
                StandardLocation.MODULE_PATH,
                false);
        l = StreamSupport.stream(fm.listLocationsForModules(StandardLocation.MODULE_PATH).spliterator(), true)
                .flatMap((s)->s.stream())
                .findFirst()
                .orElse(null);
        assertNotNull(l);
        res = fm.list(l, "org.me", EnumSet.of(JavaFileObject.Kind.CLASS), false);    //NOI18N
        assertEquals(Arrays.asList("A 9","B Base"), toContent(res));  //NOI18N
        assertEquals(Arrays.asList("org.me.A","org.me.B"), toInferedName(fm, res));  //NOI18N
    }

    public void testListRecursive() throws IOException {
        //Source level 9, broken jar
        ModuleFileManager fm = new ModuleFileManager(
                CachingArchiveProvider.getDefault(),
                bCp,
                (u)->Collections.singleton(u),
                SourceLevelUtils.JDK1_9,
                StandardLocation.MODULE_PATH,
                false);
        JavaFileManager.Location l = StreamSupport.stream(fm.listLocationsForModules(StandardLocation.MODULE_PATH).spliterator(), true)
                .flatMap((s)->s.stream())
                .findFirst()
                .orElse(null);
        assertNotNull(l);
        Iterable<JavaFileObject> res = fm.list(l, "", EnumSet.of(JavaFileObject.Kind.CLASS), true);    //NOI18N
        assertEquals(Arrays.asList("A Base", "B Base", "N Base"), toContent(res));  //NOI18N
        assertEquals(Arrays.asList("org.me.A", "org.me.B", "org.nb.N"), toInferedName(fm, res));  //NOI18N
        //Source level 9, multi release jar
        fm = new ModuleFileManager(
                CachingArchiveProvider.getDefault(),
                mvCp,
                (u)->Collections.singleton(u),
                SourceLevelUtils.JDK1_9,
                StandardLocation.MODULE_PATH,
                false);
        l = StreamSupport.stream(fm.listLocationsForModules(StandardLocation.MODULE_PATH).spliterator(), true)
                .flatMap((s)->s.stream())
                .findFirst()
                .orElse(null);
        assertNotNull(l);
        res = fm.list(l, "", EnumSet.of(JavaFileObject.Kind.CLASS), true);    //NOI18N
        assertEquals(Arrays.asList("A 9", "B Base", "N Base"), toContent(res));  //NOI18N
        assertEquals(Arrays.asList("org.me.A", "org.me.B", "org.nb.N"), toInferedName(fm, res));  //NOI18N
    }

    public void testGetFileForInput() throws IOException {
        //Source level 9, broken jar
        ModuleFileManager fm = new ModuleFileManager(
                CachingArchiveProvider.getDefault(),
                bCp,
                (u)->Collections.singleton(u),
                SourceLevelUtils.JDK1_9,
                StandardLocation.MODULE_PATH,
                false);
        JavaFileManager.Location l = StreamSupport.stream(fm.listLocationsForModules(StandardLocation.MODULE_PATH).spliterator(), true)
                .flatMap((s)->s.stream())
                .findFirst()
                .orElse(null);
        assertNotNull(l);
        JavaFileObject res = (JavaFileObject) fm.getFileForInput(l, "org.me", "A.class");    //NOI18N
        assertEquals(Arrays.asList("A Base"), toContent(Collections.<JavaFileObject>singleton(res)));  //NOI18N
        assertEquals(Arrays.asList("org.me.A"), toInferedName(fm, Collections.<JavaFileObject>singleton(res)));  //NOI18N
        //Source level 9, multi release jar
        fm = new ModuleFileManager(
                CachingArchiveProvider.getDefault(),
                mvCp,
                (u)->Collections.singleton(u),
                SourceLevelUtils.JDK1_9,
                StandardLocation.MODULE_PATH,
                false);
        l = StreamSupport.stream(fm.listLocationsForModules(StandardLocation.MODULE_PATH).spliterator(), true)
                .flatMap((s)->s.stream())
                .findFirst()
                .orElse(null);
        assertNotNull(l);
        res = (JavaFileObject) fm.getFileForInput(l, "org.me", "A.class");    //NOI18N
        assertEquals(Arrays.asList("A 9"), toContent(Collections.<JavaFileObject>singleton(res)));  //NOI18N
        assertEquals(Arrays.asList("org.me.A"), toInferedName(fm, Collections.<JavaFileObject>singleton(res)));  //NOI18N
    }

    public void testGetJavaFileForInput() throws IOException {
        //Source level 9, broken jar
        ModuleFileManager fm = new ModuleFileManager(
                CachingArchiveProvider.getDefault(),
                bCp,
                (u)->Collections.singleton(u),
                SourceLevelUtils.JDK1_9,
                StandardLocation.MODULE_PATH,
                false);
        JavaFileManager.Location l = StreamSupport.stream(fm.listLocationsForModules(StandardLocation.MODULE_PATH).spliterator(), true)
                .flatMap((s)->s.stream())
                .findFirst()
                .orElse(null);
        assertNotNull(l);
        JavaFileObject res = fm.getJavaFileForInput(l, "org.me.A", JavaFileObject.Kind.CLASS);
        assertEquals(Arrays.asList("A Base"), toContent(Collections.<JavaFileObject>singleton(res)));  //NOI18N
        assertEquals(Arrays.asList("org.me.A"), toInferedName(fm, Collections.<JavaFileObject>singleton(res)));  //NOI18N
        //Source level 9, multi release jar
        fm = new ModuleFileManager(
                CachingArchiveProvider.getDefault(),
                mvCp,
                (u)->Collections.singleton(u),
                SourceLevelUtils.JDK1_9,
                StandardLocation.MODULE_PATH,
                false);
        l = StreamSupport.stream(fm.listLocationsForModules(StandardLocation.MODULE_PATH).spliterator(), true)
                .flatMap((s)->s.stream())
                .findFirst()
                .orElse(null);
        assertNotNull(l);
        res = fm.getJavaFileForInput(l, "org.me.A", JavaFileObject.Kind.CLASS);
        assertEquals(Arrays.asList("A 9"), toContent(Collections.<JavaFileObject>singleton(res)));  //NOI18N
        assertEquals(Arrays.asList("org.me.A"), toInferedName(fm, Collections.<JavaFileObject>singleton(res)));  //NOI18N
    }

    private static List<String> toContent(final Iterable<? extends JavaFileObject> jfos) {
        return StreamSupport.stream(jfos.spliterator(), false)
                .map((jfo) -> {
                    try {
                        final BufferedReader in = new BufferedReader(new InputStreamReader(
                                jfo.openInputStream(), StandardCharsets.UTF_8));
                        return in.readLine();
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                })
                .sorted()
                .collect(Collectors.toList());
    }

    private static List<String> toInferedName(
            final JavaFileManager jfm,
            final Iterable<? extends JavaFileObject> jfos) {
        return StreamSupport.stream(jfos.spliterator(), false)
                .map((jfo) -> jfm.inferBinaryName(StandardLocation.CLASS_PATH, jfo))
                .sorted()
                .collect(Collectors.toList());
    }

    private static File createMultiReleaseJar(
            @NonNull final File loc,
            final boolean hasMultiVersionAttr,
            @NonNull final Collection<Pair<String,Collection<Integer>>> spec) throws IOException {
        final Manifest mf = new Manifest();
        final Attributes attrs = mf.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0"); //NOI18N
        if (hasMultiVersionAttr) {
            attrs.putValue(
                    "Multi-Release",      //NOI18N
                    Boolean.TRUE.toString());
        }
        try (JarOutputStream jar = new JarOutputStream(new FileOutputStream(loc), mf)) {
            for (Pair<String,Collection<Integer>> p : spec) {
                final String fqn = p.first();
                final Collection<Integer> versions = p.second();
                final String path = FileObjects.convertPackage2Folder(fqn) + ".class";  //NOI18N
                final String name = FileObjects.getBaseName(fqn,'.');                   //NOI18N
                final Collection<String[]> prefixes = new ArrayList<>();
                for (Integer version : versions) {
                    if (version == 0) {
                        prefixes.add(new String[]{"","Base"});                  //NOI18N
                    } else {
                        prefixes.add(new String[]{"META-INF/versions/"+version, version.toString()});   //NOI18N
                    }
                }
                for (String[] prefix : prefixes) {
                    final String pathWithScope = prefix[0].isEmpty() ?
                            path :
                            String.format("%s/%s", prefix[0], path);            //NOI18N
                    jar.putNextEntry(new ZipEntry(pathWithScope));
                    jar.write(String.format("%s %s", name, prefix[1]).getBytes(StandardCharsets.UTF_8));  //NOI18N
                    jar.closeEntry();
                }
            }
        }
        return loc;
    }

    private static Pair<String,Collection<Integer>> clz(
            @NonNull final String fqn,
            int version,
            int... otherVersions) {
        final Collection<Integer> vers = new ArrayList<>();
        vers.add(version);
        for (int otherVersion : otherVersions) {
            vers.add(otherVersion);
        }
        return Pair.of(fqn, vers);
    }

    private static File createTestJar(
            @NonNull final File loc,
            final boolean hasMultiVersionAttr) throws IOException {
        final Collection<Pair<String,Collection<Integer>>> spec = new ArrayList<>();
        spec.add(clz("org.me.A", 0, 9));
        spec.add(clz("org.me.B", 0));
        spec.add(clz("org.nb.N", 0));
        return createMultiReleaseJar(
                loc,
                hasMultiVersionAttr,
                spec);
    }
}
