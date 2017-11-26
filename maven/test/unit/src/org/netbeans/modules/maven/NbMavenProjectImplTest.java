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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.netbeans.api.annotations.common.SuppressWarnings;
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

    protected @Override void setUp() throws Exception {
        clearWorkDir();
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
        FileObject pd = FileUtil.toFileObject(getWorkDir()).getFileObject("prj-war");
        Project prj = ProjectManager.getDefault().findProject(pd);
        ((NbMavenProjectImpl) prj).attachUpdater();
        TestFileUtils.writeFile(pd, "pom.xml", "<project><modelVersion>4.0.0</modelVersion>"
                + "<groupId>test</groupId><artifactId>prj-war</artifactId>"
                + "<packaging>jar</packaging><version>1.0</version></project>");
        assertEquals("[base, jar]", prj.getLookup().lookup(I.class).m());
    }
    private void assertLookupObject(String result, String packaging) throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
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
        FileObject wd = FileUtil.toFileObject(getWorkDir());
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

}
