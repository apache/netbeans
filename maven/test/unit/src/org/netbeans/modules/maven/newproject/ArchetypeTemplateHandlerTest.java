/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
        ArchetypeTemplateHandler.collectFiles(root, fos, "wizard/src/main/java/.*/MyClass.java");
        assertEquals("One item: " + fos, 1, fos.size());
        FileObject fo = fos.iterator().next();
        assertEquals("root/wizard/src/main/java/com/yourorg/somepkg/MyClass.java", fo.getPath());
    }

    @Test
    public void testMatchIndexHTML() {
        Set<FileObject> fos = new HashSet<>();
        ArchetypeTemplateHandler.collectFiles(root, fos, "wizard/src/main/webapp/index.html");
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
