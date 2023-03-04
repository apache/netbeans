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
package org.netbeans.modules.maven.newproject;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import junit.framework.Test;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class ArchetypeCreateTest extends NbTestCase {

    public ArchetypeCreateTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(ArchetypeCreateTest.class).gui(false).suite();
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testCreatingAnArchetype() throws Exception {
        FileObject root = FileUtil.toFileObject(getWorkDir());

        final FileObject template = root.createData("template.txt");
        try (OutputStream os = template.getOutputStream()) {
            os.write(("\n"
                    + "archetypeArtifactId=maven-archetype-quickstart\n"
                    + "archetypeGroupId=org.apache.maven.archetypes\n"
                    + "archetypeVersion=1.4\n"
                    + "\n").getBytes(StandardCharsets.UTF_8));
        }

        final FileObject targetFolder = root.createFolder("targetFolder");

        CreateDescriptor desc = new FileBuilder(template, targetFolder).
                param("artifactId", "myprj").
                param("groupId", "org.netbeans.test").
                createDescriptor(true);

        ArchetypeTemplateHandler cftg = new ArchetypeTemplateHandler();
        List<FileObject> out = cftg.createFromTemplate(desc);

        assertEquals("One dir: " + out, 1, out.size());
        assertEquals("Creates in right folder", targetFolder, out.get(0).getParent());
    }
}
