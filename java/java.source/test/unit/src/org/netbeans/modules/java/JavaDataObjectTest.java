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

package org.netbeans.modules.java;

import java.io.IOException;
import static org.junit.Assert.assertNotEquals;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jan Lahoda
 */
public class JavaDataObjectTest extends NbTestCase {

    public JavaDataObjectTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.init();
    }

    public void testJES() throws Exception {
        MockLookup.setInstances(JavaDataLoader.getLoader(JavaDataLoader.class));

        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createData("test.java");

        DataObject od = DataObject.find(f);

        assertTrue(od instanceof JavaDataObject);

        Object c = od.getCookie(EditorCookie.class);

//        assertTrue(c instanceof JavaDataObject.JavaEditorSupport);
        assertTrue(c == od.getCookie(OpenCookie.class));

        assertTrue(c == od.getLookup().lookup(EditorCookie.class));
        assertTrue(c == od.getLookup().lookup(OpenCookie.class));
        assertTrue(c == od.getLookup().lookup(CloneableEditorSupport.class));
    }

    public void testNewFromTemplateWithDots() throws Exception {
        MockLookup.setInstances(JavaDataLoader.getLoader(JavaDataLoader.class));

        FileObject clazzFo = FileUtil.getConfigFile("Templates/Classes/Main.java");
        assertNotNull("Java template found", clazzFo);

        DataObject clazzDo = DataObject.find(clazzFo);
        assertTrue("Template is template", clazzDo.isTemplate());

        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createFolder("src");
        DataFolder folder = DataFolder.findFolder(f);

        DataObject java = clazzDo.createFromTemplate(folder, "my.great.pkg.and.Name");
        assertTrue(java instanceof JavaDataObject);

        EditorCookie c = java.getCookie(EditorCookie.class);
        assertNotNull("Editor support is found", c);

        String text = c.openDocument().getText(0, c.openDocument().getLength());
        int at = text.indexOf("class Name");
        assertNotEquals("found class Name in\n" + text, at, -1);

        assertEquals("and", java.getFolder().getName());
        assertEquals("pkg", java.getFolder().getFolder().getName());
        assertEquals("great", java.getFolder().getFolder().getFolder().getName());
        assertEquals("my", java.getFolder().getFolder().getFolder().getFolder().getName());
    }

    public void testInvalidCharacters() throws Exception {
        MockLookup.setInstances(JavaDataLoader.getLoader(JavaDataLoader.class));

        FileObject clazzFo = FileUtil.getConfigFile("Templates/Classes/Main.java");
        assertNotNull("Java template found", clazzFo);

        DataObject clazzDo = DataObject.find(clazzFo);
        assertTrue("Template is template", clazzDo.isTemplate());

        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createFolder("src");
        DataFolder folder = DataFolder.findFolder(f);

        try {
            DataObject java = clazzDo.createFromTemplate(folder, "my#class");
            fail("No Java class should be created: " + java.getName());
        } catch (IOException ex) {
            String msg = Exceptions.findLocalizedMessage(ex);
            assertNotEquals("Contains my#class: " + msg, -1, msg.indexOf("my#class"));
        }
    }

    public void testInvalidCharactersWithDot() throws Exception {
        MockLookup.setInstances(JavaDataLoader.getLoader(JavaDataLoader.class));

        FileObject clazzFo = FileUtil.getConfigFile("Templates/Classes/Main.java");
        assertNotNull("Java template found", clazzFo);

        DataObject clazzDo = DataObject.find(clazzFo);
        assertTrue("Template is template", clazzDo.isTemplate());

        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createFolder("src");
        DataFolder folder = DataFolder.findFolder(f);

        try {
            DataObject java = clazzDo.createFromTemplate(folder, "pkg.my/class");
            fail("No Java class should be created: " + java.getName());
        } catch (IOException ex) {
            String msg = Exceptions.findLocalizedMessage(ex);
            assertNotEquals("Contains my/class: " + msg, -1, msg.indexOf("my/class"));
        }
    }
}
