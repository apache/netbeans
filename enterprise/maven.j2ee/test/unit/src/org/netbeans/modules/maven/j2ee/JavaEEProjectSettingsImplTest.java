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
package org.netbeans.modules.maven.j2ee;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JavaEEProjectSettingsImplTest extends JavaEEMavenTestBase {

    public JavaEEProjectSettingsImplTest(String name) {
        super(name);
    }

    public void testJavaEEProjectSettingsInMavenProjects() throws Exception {
        // web project
        project = createMavenWebProject();
        checkProjectForProfileChange(project);

        // ejb project
        project = createMavenEjbProject();
        checkProjectForProfileChange(project);

        // ear project
        project = createMavenEarProject();
        checkProjectForProfileChange(project);

        // osgi project
        project = createMavenOSGIProject();
        checkProjectForProfileChange(project);
    }

    public void checkProjectForProfileChange(Project prj) {
        JavaEEProjectSettings.setProfile(prj, Profile.JAKARTA_EE_11_FULL);
        assertEquals(Profile.JAKARTA_EE_11_FULL, JavaEEProjectSettings.getProfile(prj));
        JavaEEProjectSettings.setProfile(prj, Profile.JAKARTA_EE_10_FULL);
        assertEquals(Profile.JAKARTA_EE_10_FULL, JavaEEProjectSettings.getProfile(prj));
        JavaEEProjectSettings.setProfile(prj, Profile.JAKARTA_EE_9_1_FULL);
        assertEquals(Profile.JAKARTA_EE_9_1_FULL, JavaEEProjectSettings.getProfile(prj));
        JavaEEProjectSettings.setProfile(prj, Profile.JAKARTA_EE_9_FULL);
        assertEquals(Profile.JAKARTA_EE_9_FULL, JavaEEProjectSettings.getProfile(prj));
        JavaEEProjectSettings.setProfile(prj, Profile.JAKARTA_EE_8_FULL);
        assertEquals(Profile.JAKARTA_EE_8_FULL, JavaEEProjectSettings.getProfile(prj));
        JavaEEProjectSettings.setProfile(prj, Profile.JAVA_EE_8_FULL);
        assertEquals(Profile.JAVA_EE_8_FULL, JavaEEProjectSettings.getProfile(prj));
        JavaEEProjectSettings.setProfile(prj, Profile.JAVA_EE_7_FULL);
        assertEquals(Profile.JAVA_EE_7_FULL, JavaEEProjectSettings.getProfile(prj));
        JavaEEProjectSettings.setProfile(prj, Profile.JAVA_EE_6_FULL);
        assertEquals(Profile.JAVA_EE_6_FULL, JavaEEProjectSettings.getProfile(prj));
    }

}
