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

package org.netbeans.modules.maven;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.projectapi.nb.TimedWeakReference;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Lookup;

public class NbMavenProjectImplTest extends NbTestCase {

    public NbMavenProjectImplTest(String name) {
        super(name);
    }

    private FileObject wd;

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
        //synchronous reload of maven project asserts sanoty in some tests..
        System.setProperty("test.reload.sync", "true");
    }

    protected @Override Level logLevel() {
        return Level.FINE;
    }

    protected @Override String logRoot() {
        return "org.netbeans.modules.maven";
    }

    public void testPackagingTypeSpecificLookup() throws Exception {
        assertLookupObject("[base, jar]", "jar");
        assertLookupObject("[base, war]", "war");
        assertLookupObject("[base]", "ear");
        // Now test dynamic changes to packaging:
        FileObject pd = wd.getFileObject("prj-war");
        Project prj = ProjectManager.getDefault().findProject(pd);
        ((NbMavenProjectImpl) prj).attachUpdater();
        TestFileUtils.writeFile(pd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj-war</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        assertEquals("[base, jar]", prj.getLookup().lookup(I.class).m());
    }
    private void assertLookupObject(String result, String packaging) throws Exception {
        FileObject pd = wd.createFolder("prj-" + packaging);
        TestFileUtils.writeFile(pd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj-" + packaging + "</artifactId>"
                + "<packaging>" + packaging + "</packaging><version>1.0</version></project>");
        assertEquals(result, ProjectManager.getDefault().findProject(pd).getLookup().lookup(I.class).m());
    }
    public interface I {
        String m();
    }
    @ProjectServiceProvider(service=I.class, projectType="org-netbeans-modules-maven")
    public static class BasePackagingImpl implements I {
        public @Override String m() {
            return "base";
        }
    }
    @ProjectServiceProvider(service=I.class, projectType="org-netbeans-modules-maven/jar")
    public static class JarPackagingImpl implements I {
        public @Override String m() {
            return "jar";
        }
    }
    @ProjectServiceProvider(service=I.class, projectType="org-netbeans-modules-maven/war")
    public static class WarPackagingImpl implements I {
        public @Override String m() {
            return "war";
        }
    }
    @LookupMerger.Registration(projectType="org-netbeans-modules-maven")
    public static class Merger implements LookupMerger<I> {
        public @Override Class<I> getMergeableClass() {
            return I.class;
        }
        public @Override I merge(final Lookup lookup) {
            return new I() {
                public @Override String m() {
                    Set<String> results = new TreeSet<String>();
                    for (I i : lookup.lookupAll(I.class)) {
                        results.add(i.m());
                    }
                    return results.toString();
                }
            };
        }
    }

    @SuppressWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    @RandomlyFails
    public void testMemoryReleased() throws Exception {
        TimedWeakReference.TIMEOUT = 0;
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>g</groupId><artifactId>a</artifactId>"
                + "<version>0</version></project>");
        Project p = ProjectManager.getDefault().findProject(wd);
        ((NbMavenProjectImpl) p).attachUpdater();
        /* Want to avoid leaks even if this is not called for some reason:
        ((NbMavenProjectImpl) p).detachUpdater();
        */
        Reference<?> r = new WeakReference<Object>(p);
        p = null;

        Thread.sleep(5000); //something has changed and the reference only gets cleared after some time. added @RandomlyFails
        // if it keeps on failing regularly, it's a candidate for removal, the point of the test is unclear to me.
        
        assertGC("can release project after updater detached", r, Collections.singleton(wd));
    }

    public void testMavenConfig() throws Exception {
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj</artifactId><version>1.0</version>"
                + "<properties><java>1.5</java><testJava>1.5</testJava></properties>"
                + "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.1</version>"
                + "<configuration><source>${java}</source><testSource>${testJava}</testSource></configuration></plugin></plugins></build>"
                + "<profiles>"
                + "<profile><id>new</id><properties><java>1.6</java></properties></profile>"
                + "<profile><id>testNew</id><properties><testJava>1.6</testJava></properties></profile>"
                + "<profile><id>java8</id><activation><property><name>java8</name><value>true</value></property></activation><properties><java>1.8</java></properties></profile>"
                + "</profiles>"
                + "</project>");
        FileObject source = TestFileUtils.writeFile(wd, "src/main/java/p/C.java", "package p; class C {}");
        FileObject test = TestFileUtils.writeFile(wd, "src/test/java/p/CTest.java", "package p; class CTest {}");
        ((NbMavenProjectImpl) ProjectManager.getDefault().findProject(wd)).attachUpdater();
        SourceLevelQuery.Result slqr = SourceLevelQuery.getSourceLevel2(source);
        SourceLevelQuery.Result testSlqr = SourceLevelQuery.getSourceLevel2(test);
        assertEquals("1.5", slqr.getSourceLevel());
        assertEquals("1.5", testSlqr.getSourceLevel());
        writeMavenConfig("-Pnew");
        assertEquals("1.6", slqr.getSourceLevel());
        writeMavenConfig("-P new");
        assertEquals("1.6", slqr.getSourceLevel());
        writeMavenConfig("--activate-profiles new");
        assertEquals("1.6", slqr.getSourceLevel());
        assertEquals("1.5", testSlqr.getSourceLevel());
        writeMavenConfig("--activate-profiles=testNew");
        assertEquals("1.5", slqr.getSourceLevel());
        assertEquals("1.6", testSlqr.getSourceLevel());
        writeMavenConfig("--activate-profiles new,testNew");
        assertEquals("1.6", slqr.getSourceLevel());
        assertEquals("1.6", testSlqr.getSourceLevel());
        wd.getFileObject(".mvn/maven.config").delete();
        assertEquals("1.5", slqr.getSourceLevel());
        assertEquals("1.5", testSlqr.getSourceLevel());
        writeMavenConfig("-Djava=1.7");
        assertEquals("1.7", slqr.getSourceLevel());
        writeMavenConfig("-D java=1.7");
        assertEquals("1.7", slqr.getSourceLevel());
        writeMavenConfig("--define java=1.6");
        assertEquals("1.6", slqr.getSourceLevel());
        writeMavenConfig("-Djava8");
        assertEquals("1.8", slqr.getSourceLevel());
        writeMavenConfig("-Djava8\n-PtestNew\n");
        assertEquals("1.8", slqr.getSourceLevel());
        assertEquals("1.6", testSlqr.getSourceLevel());
    }

    public void testMavenConfigReactor() throws Exception {
        writeMavenConfig("-Pnew");
        TestFileUtils.writeFile(wd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>parent</artifactId><version>1.0</version><packaging>pom</packaging>"
                + "<modules><module>mod</module></modules>"
                + "</project>");
        TestFileUtils.writeFile(wd, "mod/pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<parent><groupId>test</groupId><artifactId>parent</artifactId><version>1.0</version></parent>"
                + "<artifactId>prj</artifactId>"
                + "<properties><java>1.5</java><testJava>1.5</testJava></properties>"
                + "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.1</version>"
                + "<configuration><source>${java}</source><testSource>${testJava}</testSource></configuration></plugin></plugins></build>"
                + "<profiles>"
                + "<profile><id>new</id><properties><java>1.6</java></properties></profile>"
                + "</profiles>"
                + "</project>");
        FileObject source = TestFileUtils.writeFile(wd, "mod/src/main/java/p/C.java", "package p; class C {}");
        SourceLevelQuery.Result slqr = SourceLevelQuery.getSourceLevel2(source);
        assertEquals("1.6", slqr.getSourceLevel());
        // TODO listening to changes not yet implemented in FileProvider
    }

    private void writeMavenConfig(String text) throws IOException, InterruptedException {
        // Need the touch call, since NbMavenProjectImpl.Updater checks timestamps.
        TestFileUtils.touch(TestFileUtils.writeFile(wd, ".mvn/maven.config", text), null);
    }

}
