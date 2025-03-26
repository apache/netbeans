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

package org.netbeans.modules.maven.indexer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.ClassUsage;
import org.netbeans.modules.maven.indexer.spi.ClassUsageQuery;
import org.netbeans.modules.maven.indexer.spi.ClassesQuery;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.JarBuilder;
import org.openide.util.test.TestFileUtils;

public class ClassDependencyIndexCreatorTest extends NexusTestBase {

    public ClassDependencyIndexCreatorTest(String n) {
        super(n);
    }

    public void testCrc32base32() throws Exception {
        assertEquals("jyiuhmy", ClassDependencyIndexCreator.crc32base32("whatever"));
        assertEquals("w2sd5ia", ClassDependencyIndexCreator.crc32base32("mod1/Stuff"));
    }

    public void testFindClassUsages() throws Exception {
        installPOM("test", "mod1", "0", "jar");
        File mod1 = new JarBuilder(getWorkDir()).
                source("mod1.API", "public class API {}").
                source("mod1.Util", "public class Util {}").
                source("mod1.Stuff", "public class Stuff implements Outer {}").
                source("mod1.Outer", "public interface Outer {interface Inner {} interface Unused {}}").
                build();
        install(mod1, "test", "mod1", "0", "jar");

        installPOM("test", "mod2", "0", "jar");
        install(new JarBuilder(getWorkDir()).
                source("mod2.Client", "class Client extends mod1.API {}").
                source("mod2.OtherClient", "class OtherClient extends mod1.API {}").
                source("mod2.Outer", "class Outer implements mod1.Outer, mod1.Outer.Inner {static class Inner implements mod1.Outer.Inner {}}").
                classpath(mod1).build(), "test", "mod2", "0", "jar");

        installPOM("test", "mod3", "0", "jar");
        install(new JarBuilder(getWorkDir()).
                source("mod3.Client", "class Client extends mod1.API {}").
                classpath(mod1).build(), "test", "mod3", "0", "jar");
        // This is what nbm:populate-repository currently produces:
        install(TestFileUtils.writeFile(new File(getWorkDir(), "mod4.pom"),
                "<project><modelVersion>4.0.0</modelVersion>" +
                "<groupId>test</groupId><artifactId>mod4</artifactId>" +
                "<version>0</version></project>"), "test", "mod4", "0", "pom");
        install(new JarBuilder(getWorkDir()).
                source("mod4.Install", "class Install extends mod1.Util {}").
                classpath(mod1).build(), "test", "mod4", "0", "jar");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "mod4.nbm"), "Info/info.xml:<whatever/>"), "test", "mod4", "0", "nbm");

        // And as produced by a Maven source build of a module:
        installPOM("test", "mod5", "0", "nbm");
        install(new JarBuilder(getWorkDir()).
                source("mod5.Install", "class Install extends mod1.Stuff {}").
                classpath(mod1).build(), "test", "mod5", "0", "jar");
        install(TestFileUtils.writeZipFile(new File(getWorkDir(), "mod5.nbm"), "Info/info.xml:<whatever/>"), "test", "mod5", "0", "nbm");

        // repo set up, now index and query:
        nrii.indexRepo(info);
        List<RepositoryInfo> repo = List.of(info);

        ClassUsageQuery query = nrii.getClassUsageQuery();
        List<ClassUsage> list = query.findClassUsages("mod1.API", repo).getResults();
        assertEquals("[test:mod2:0:test[mod2.Client, mod2.OtherClient], test:mod3:0:test[mod3.Client]]", list.toString());

        list = query.findClassUsages("mod1.Util", repo).getResults();
        assertEquals("[test:mod4:0:test[mod4.Install]]", list.toString());
        assertEquals("jar", list.get(0).getArtifact().getType());

        list = query.findClassUsages("mod1.Stuff", repo).getResults();
        assertEquals("[test:mod5:0:test[mod5.Install]]", list.toString());
        assertEquals("jar", list.get(0).getArtifact().getType());

        assertEquals("[]", query.findClassUsages("java.lang.Object", repo).getResults().toString());
        assertEquals("[test:mod2:0:test[mod2.Outer]]", query.findClassUsages("mod1.Outer", repo).getResults().toString());
        assertEquals("[test:mod2:0:test[mod2.Outer]]", query.findClassUsages("mod1.Outer$Inner", repo).getResults().toString());
        assertEquals("[]", query.findClassUsages("mod1.Outer$Unused", repo).getResults().toString());
        // XXX InnerClass attribute will produce spurious references to outer classes even when just an inner is used
    }

    public void testFindVersionsByClass() throws Exception {
        // single version
        installPOM("test", "mod1", "0", "jar");
        File mod1 = new JarBuilder(getWorkDir())
            .source("mod1.API", "public class API {}")
            .source("mod1.Util", "public class Util {}")
            .build();
        install(mod1, "test", "mod1", "42", "jar");

        // two versions
        installPOM("test", "mod2", "0", "jar");
        File mod2v1 = new JarBuilder(getWorkDir())
            .source("mod2.API2", "public class API2 {}")
            .build();
        install(mod2v1, "test", "mod2", "42", "jar");
        File mod2v2 = new JarBuilder(getWorkDir())
            .source("mod2.API2", "public class API2 {}")
            .source("mod2.Util2", "public class Util2 {}")
            .build();
        install(mod2v2, "test", "mod2", "43", "jar");

        nrii.indexRepo(info);
        List<RepositoryInfo> repo = List.of(info);

        ClassesQuery query = nrii.getClassesQuery();
        // single version
        List<NBVersionInfo> list = query.findVersionsByClass("mod1.API", repo).getResults();
        assertEquals("[test:mod1:42:test]", list.toString());

        list = query.findVersionsByClass("mod1.Util", repo).getResults();
        assertEquals("[test:mod1:42:test]", list.toString());

        // two versions
        list = query.findVersionsByClass("mod2.API2", repo).getResults();
        assertEquals("[test:mod2:43:test, test:mod2:42:test]", list.toString());

        list = query.findVersionsByClass("mod2.Util2", repo).getResults();
        assertEquals("[test:mod2:43:test]", list.toString());
    }

    public void testRead() throws Exception { // #206111
        File jar = TestFileUtils.writeZipFile(new File(getWorkDir(), "x.jar"),
                // XXX failed to produce a manifest that would generate a SecurityException if loaded with verify=true
                "pkg/Clazz.class:ABC");
        Map<String, byte[]> content = new TreeMap<>();
        new ClassDependencyIndexCreator().read(jar, (String name, InputStream classData, Set<String> siblings) -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            FileUtil.copy(classData, out);
            content.put(name, out.toByteArray());
        });
        assertEquals("[pkg/Clazz]", content.keySet().toString());
        assertEquals("[65, 66, 67]", Arrays.toString(content.get("pkg/Clazz")));
    }

}
