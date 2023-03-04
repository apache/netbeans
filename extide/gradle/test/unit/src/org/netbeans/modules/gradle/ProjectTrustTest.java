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
package org.netbeans.modules.gradle;

import java.io.IOException;
import java.util.Random;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;

/**
 *
 * @author lkishalmi
 */
public class ProjectTrustTest extends AbstractGradleProjectTestCase {


    public ProjectTrustTest(String name) {
        super(name);
    }
    
    public void testIsTrusted4Untrusted() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "");
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust trust = new ProjectTrust(NbPreferences.root().node("org/netbeans/modules/gradle/trust"));
        assertFalse(trust.isTrusted(prjA));
    }

    public void testIsTrusted4Trusted() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "");
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust trust = new ProjectTrust(NbPreferences.root().node("org/netbeans/modules/gradle/trust"));
        trust.trustProject(prjA);
        assertTrue(trust.isTrusted(prjA));
    }

    public void testIsTrusted4TrustedSub() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "include 'projectB'\n");
        FileObject b = createGradleProject("projectA-" + rnd + "/projectB",
                "apply plugin: 'java'\n", null);
        Project prjA = ProjectManager.getDefault().findProject(a);
        Project prjB = ProjectManager.getDefault().findProject(b);
        ProjectTrust trust = new ProjectTrust(NbPreferences.root().node("org/netbeans/modules/gradle/trust"));
        trust.trustProject(prjA);
        assertTrue(trust.isTrusted(prjA));
        assertTrue(trust.isTrusted(prjB));
    }

    public void testIsTrusted4UnTrustedSub() throws IOException {
        int rnd = new Random().nextInt(1000000);
        FileObject a = createGradleProject("projectA-" + rnd,
                "apply plugin: 'java'\n", "include 'projectB'\n");
        FileObject b = createGradleProject("projectA-" + rnd + "/projectB",
                "apply plugin: 'java'\n", null);
        Project prjA = ProjectManager.getDefault().findProject(a);
        Project prjB = ProjectManager.getDefault().findProject(b);
        ProjectTrust trust = new ProjectTrust(NbPreferences.root().node("org/netbeans/modules/gradle/trust"));
        trust.trustProject(prjA);
        assertTrue(trust.isTrusted(prjA));
        assertTrue(trust.isTrusted(prjB));
        trust.distrustProject(prjB);
        assertFalse(trust.isTrusted(prjA));
        assertFalse(trust.isTrusted(prjB));
    }
}
