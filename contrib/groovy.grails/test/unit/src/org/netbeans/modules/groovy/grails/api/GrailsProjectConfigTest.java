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

package org.netbeans.modules.groovy.grails.api;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class GrailsProjectConfigTest extends NbTestCase {

    public GrailsProjectConfigTest(String name) {
        super(name);
    }

    public void testProject() throws IOException {
        Project project = new TestProject("test", FileUtil.toFileObject(
                FileUtil.normalizeFile(getWorkDir())));
        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        assertEquals(project, config.getProject());
    }

    public void testPort() throws IOException {
        Project project = new TestProject("test", FileUtil.toFileObject(
                FileUtil.normalizeFile(getWorkDir())));
        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);

        assertEquals("8080", config.getPort());
        config.setPort("9000");
        assertEquals("9000", config.getPort());
        config.setPort("9001");
        assertEquals("9001", config.getPort());
    }

    public void testEnvironment() throws IOException {
        Project project = new TestProject("test", FileUtil.toFileObject(
                FileUtil.normalizeFile(getWorkDir())));
        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);

        assertNull(config.getEnvironment());
        config.setEnvironment(GrailsEnvironment.DEV);
        assertEquals(GrailsEnvironment.DEV, config.getEnvironment());
        GrailsEnvironment env = GrailsEnvironment.valueOf("something");
        config.setEnvironment(env);
        assertEquals(env, config.getEnvironment());
    }
}
