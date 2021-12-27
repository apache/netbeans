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
package org.netbeans.modules.gradle.java.classpath;

import java.io.IOException;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lkishalmi
 */
public class GradleSourcesImplTest extends AbstractGradleProjectTestCase {

    public GradleSourcesImplTest(String name) {
        super(name);
    }

    public void testGeneratedSources() throws Exception { // #187595
        FileObject d = createGradleProject(null,
                "apply plugin: 'java'\n" +
                "sourceSets { main { java { srcDirs = [ 'src', 'build/gen-src' ] }}}", "");
        FileObject src = FileUtil.createFolder(d, "src/");
        FileObject gsrc = FileUtil.createFolder(d, "build/gen-src");
        FileObject source = src.createData("Whatever.java");
        FileObject generated = gsrc.createData("WhateverGen.java");
        Project prj = openProject(d);
        Sources srcs = ProjectUtils.getSources(prj);
        SourceGroup[] groups = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(2, groups.length);
        assertTrue(groups[0].contains(source));
        assertFalse(groups[0].contains(generated));
        assertTrue(groups[1].contains(generated));
        assertFalse(groups[1].contains(source));
    }

    public void testRootProjectSourceGroup() throws IOException {
        FileObject d = createGradleProject(null,
                "apply plugin: 'java'\n" +
                "sourceSets { main { java { srcDirs = [ 'src', 'build/gen-src' ] }}}", "");
        Project p = ProjectManager.getDefault().findProject(d);
        SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals(1, groups.length);
    }

    public void testSourceProviderChange() throws Exception {
        FileObject d = createGradleProject(null, "", "");
        Project p = ProjectManager.getDefault().findProject(d);
        ProjectTrust.getDefault().trustProject(p);
        SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals(1, groups.length);
        groups = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(0, groups.length);
        FileObject src = FileUtil.createFolder(d, "src/");
        FileObject gsrc = FileUtil.createFolder(d, "build/gen-src");
        FileObject source = src.createData("Whatever.java");
        FileObject generated = gsrc.createData("WhateverGen.java");
        createGradleProject(null,
                "apply plugin: 'java'\n" +
                "sourceSets { main { java { srcDirs = [ 'src', 'build/gen-src' ] }}}", "");
        reloadProject(p);
        Sources srcs = ProjectUtils.getSources(p);
        groups = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(2, groups.length);
        assertTrue(groups[0].contains(source));
        assertFalse(groups[0].contains(generated));
        assertTrue(groups[1].contains(generated));
        assertFalse(groups[1].contains(source));
    }
}
