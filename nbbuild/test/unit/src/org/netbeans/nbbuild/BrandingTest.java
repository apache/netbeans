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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.netbeans.junit.NbTestCase;

public class BrandingTest extends NbTestCase {

    public BrandingTest(String n) {
        super(n);
    }

    public void testDefaultExclusions() throws Exception { // #161724
        File cluster = new File(getWorkDir(), "cluster");
        cluster.mkdir();
        File overrides = new File(getWorkDir(), "overrides");
        File overriding = new File(overrides, "core/core.jar/org/netbeans/core/startup/Bundle.properties");
        overriding.getParentFile().mkdirs();
        new FileOutputStream(overriding).close();
        File junk = new File(overrides, "core/core.jar/org/netbeans/core/startup/.svn/props");
        junk.getParentFile().mkdirs();
        new FileOutputStream(junk).close();
        junk = new File(overrides, "core/.svn/props");
        junk.getParentFile().mkdirs();
        new FileOutputStream(junk).close();
        Project p = new Project();
        p.init();
        p.addBuildListener(new BuildListener() {
            public void buildStarted(BuildEvent event) {}
            public void buildFinished(BuildEvent event) {}
            public void targetStarted(BuildEvent event) {}
            public void targetFinished(BuildEvent event) {}
            public void taskStarted(BuildEvent event) {}
            public void taskFinished(BuildEvent event) {}
            public void messageLogged(BuildEvent event) {
                if (event.getPriority() < Project.MSG_INFO && event.getMessage().contains("stray")) {
                    fail(event.getMessage());
                }
            }
        });
        Branding b = new Branding();
        b.setProject(p);
        b.setCluster(cluster);
        b.setOverrides(overrides);
        b.setToken("myapp");
        b.setLocales("en");
        b.execute();
        File jar = new File(cluster, "core/locale/core_myapp.jar");
        assertTrue(jar.isFile());
        try (JarFile jf = new JarFile(jar)) {
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                assertEquals("META-INF/MANIFEST.MF", entry.getName());
                while (entries.hasMoreElements()) {
                    entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    assertEquals("org/netbeans/core/startup/Bundle_myapp.properties", entry.getName());
                }
            }
        }
    }

}
