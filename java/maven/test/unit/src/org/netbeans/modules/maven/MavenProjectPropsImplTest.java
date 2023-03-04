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

package org.netbeans.modules.maven;

import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.test.MockLookup;

public class MavenProjectPropsImplTest extends NbTestCase {

    public MavenProjectPropsImplTest(String name) {
        super(name);
    }

    private FileObject d;
    private Project prj;
    private Preferences p;

    protected @Override void setUp() throws Exception {
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        MockLookup.setInstances(new Modules() {
            @Override public ModuleInfo ownerOf(Class<?> clazz) {
                return null;
            }
        });
        clearWorkDir();
        d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "pom.xml", "<project><modelVersion>4.0.0</modelVersion><groupId>g</groupId><artifactId>a</artifactId><version>0</version></project>");
        prj = ProjectManager.getDefault().findProject(d);
        p = ProjectUtils.getPreferences(prj, MavenProjectPropsImplTest.class, true);
    }

    public void testBasicUsage() throws Exception {
        p.put("k", "v");
        assertEquals("v", p.get("k", null));
    }

    public void testInvalidNames() throws Exception { // #200901
        p.putBoolean("a<b", true);
        p.flush();
    }

    public void testHintPackaging() throws Exception {
        NbMavenProject nbmp = prj.getLookup().lookup(NbMavenProject.class);
        assertEquals("jar", nbmp.getPackagingType());
        TestFileUtils.writeFile(d, "nb-configuration.xml", "<project-shared-configuration><properties xmlns='http://www.netbeans.org/ns/maven-properties-data/1'><netbeans.hint.packaging>war</netbeans.hint.packaging></properties></project-shared-configuration>");
        assertEquals("war", nbmp.getPackagingType());
    }

}
