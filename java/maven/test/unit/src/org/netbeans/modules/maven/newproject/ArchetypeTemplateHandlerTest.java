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

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.FileBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

public class ArchetypeTemplateHandlerTest {
    private FileObject root;
    public ArchetypeTemplateHandlerTest() {
    }

    @Before
    public void setUpMethod() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileUtil.createData(fs.getRoot(), "root/wizard/src/main/java/com/yourorg/somepkg/MyClass.java");
        FileUtil.createData(fs.getRoot(), "root/wizard/src/main/java/com/yourorg/somepkg/Main.java");
        FileUtil.createData(fs.getRoot(), "root/wizard/src/main/webapp/index.html");
        root = fs.findResource("root");
        assertNotNull(root);
    }


    @Test
    public void testMatchDataModelJava() {
        Set<FileObject> fos = new HashSet<>();
        TemplateUtils.collectFiles(root, fos, "wizard/src/main/java/.*/MyClass.java");
        assertEquals("One item: " + fos, 1, fos.size());
        FileObject fo = fos.iterator().next();
        assertEquals("root/wizard/src/main/java/com/yourorg/somepkg/MyClass.java", fo.getPath());
    }

    @Test
    public void testMatchIndexHTML() {
        Set<FileObject> fos = new HashSet<>();
        TemplateUtils.collectFiles(root, fos, "wizard/src/main/webapp/index.html");
        assertEquals("One item: " + fos, fos.size(), 1);
        FileObject fo = fos.iterator().next();
        assertEquals("root/wizard/src/main/webapp/index.html", fo.getPath());
    }

    @Test
    public void testOrderOfMerging1() throws Exception {
        CreateDescriptor desc = new FileBuilder(root.createData("template.txt"), root.createFolder("targetFolder")).
            param("wizard", Collections.singletonMap("key1", "value1")).
            createDescriptor(true);
        Properties archetypeFile = new Properties();

        ArchetypeTemplateHandler.mergeProperties(desc, archetypeFile);

        assertEquals("value1", archetypeFile.get("key1"));
    }

    @Test
    public void testOrderOfMerging2() throws Exception {
        CreateDescriptor desc = new FileBuilder(root.createData("template.txt"), root.createFolder("targetFolder")).
            param("key1", "value1").
            createDescriptor(true);
        Properties archetypeFile = new Properties();

        ArchetypeTemplateHandler.mergeProperties(desc, archetypeFile);

        assertEquals("value1", archetypeFile.get("key1"));
    }

    @Test
    public void testOrderOfMerging3() throws Exception {
        CreateDescriptor desc = new FileBuilder(root.createData("template.txt"), root.createFolder("targetFolder")).
            param("key2", "value2").
            param("wizard", Collections.singletonMap("key3", "value3")).
            createDescriptor(true);
        Properties archetypeFile = new Properties();
        archetypeFile.put("key1", "value1");

        ArchetypeTemplateHandler.mergeProperties(desc, archetypeFile);

        assertEquals("value1", archetypeFile.get("key1"));
        assertEquals("value2", archetypeFile.get("key2"));
        assertEquals("value3", archetypeFile.get("key3"));
    }

    @Test
    public void testOrderOfMergingDescriptorTakePrecedence() throws Exception {
        CreateDescriptor desc = new FileBuilder(root.createData("template.txt"), root.createFolder("targetFolder")).
            param("key1", "value1").
            createDescriptor(true);
        Properties archetypeFile = new Properties();
        archetypeFile.put("key1", "value2");

        ArchetypeTemplateHandler.mergeProperties(desc, archetypeFile);

        assertEquals("value1", archetypeFile.get("key1"));
    }

    @Test
    public void testOrderOfMergingWizardTakePrecedence() throws Exception {
        CreateDescriptor desc = new FileBuilder(root.createData("template.txt"), root.createFolder("targetFolder")).
            param("key1", "value1").
            param("wizard", Collections.singletonMap("key1", "value2")).
            createDescriptor(true);
        Properties archetypeFile = new Properties();
        archetypeFile.put("key1", "value3");

        ArchetypeTemplateHandler.mergeProperties(desc, archetypeFile);

        assertEquals("value2", archetypeFile.get("key1"));
    }
}
