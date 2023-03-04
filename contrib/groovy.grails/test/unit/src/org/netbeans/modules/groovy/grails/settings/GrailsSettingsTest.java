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

package org.netbeans.modules.groovy.grails.settings;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.groovy.grails.api.GrailsEnvironment;
import org.netbeans.modules.groovy.grails.api.TestProject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class GrailsSettingsTest extends NbTestCase {

    public GrailsSettingsTest(String name) {
        super(name);
    }

    public void testGrailsBase() {
        final GrailsSettings settings = GrailsSettings.getInstance();
        settings.setGrailsBase("test_path");
        assertEquals("test_path", settings.getGrailsBase());
        settings.setGrailsBase("other_path");
        assertEquals("other_path", settings.getGrailsBase());
    }

    public void testPortForProject() throws IOException {
        final GrailsSettings settings = GrailsSettings.getInstance();
        final Project project = new TestProject("test",
                FileUtil.toFileObject(FileUtil.normalizeFile(this.getWorkDir())));
        assertNull(settings.getPortForProject(project));
        settings.setPortForProject(project, "80");
        assertEquals("80", settings.getPortForProject(project));
        settings.setPortForProject(project, "8080");
        assertEquals("8080", settings.getPortForProject(project));
    }

    public void testEnvForProject() throws IOException {
        final GrailsSettings settings = GrailsSettings.getInstance();
        final Project project = new TestProject("test",
                FileUtil.toFileObject(FileUtil.normalizeFile(this.getWorkDir())));
        assertNull(settings.getEnvForProject(project));
        settings.setEnvForProject(project, GrailsEnvironment.PROD);
        assertEquals(GrailsEnvironment.PROD, settings.getEnvForProject(project));
        settings.setEnvForProject(project, GrailsEnvironment.DEV);
        assertEquals(GrailsEnvironment.DEV, settings.getEnvForProject(project));
    }
}
