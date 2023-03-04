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
package org.netbeans.modules.templates.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.netbeans.junit.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.test.MockLookup;

/**
 * Tests creating/renaming/removing templates via TemplatesPanel.
 *
 * @author Jiri Rechtacek
 */
public class TemplatesPanelTest extends NbTestCase {
    File popural;
    FileObject templateFolder;
    DataFolder f;

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public TemplatesPanelTest(String s) {
        super(s);
    }
    
    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(new Repository(FileUtil.createMemoryFileSystem()));
        try {
            templateFolder = FileUtil.getConfigRoot ().createFolder ("TestTemplates");
        } catch (IOException ioe) {
            fail (ioe.getMessage ());
        }
        assertNotNull ("TestTemplates folder exists on SFS", templateFolder);
        clearWorkDir();
        try {
            popural = File.createTempFile("popural", "java", getWorkDir());
        } catch (IOException ioe) {
            fail (ioe.getMessage ());
        }
        assertTrue ("popural.tmp exists", popural.exists ());
        
        f = DataFolder.findFolder (templateFolder);
        
        assertNotNull ("DataFolder found for FO " + templateFolder, f);
        
    }
    
    @Override
    protected void tearDown() {
        try {
            FileLock l = templateFolder.lock ();
            templateFolder.delete (l);
            l.releaseLock ();
        } catch (IOException ioe) {
            fail (ioe.getMessage ());
        }
    }
    
    public void testNewTemplateFromFile () throws Exception {
        DataObject dobj = TemplatesPanel.createTemplateFromFile (popural, f);
        assertNotNull ("New DataObject found.", dobj);
        assertTrue ("Is template.", dobj.isTemplate ());
        assertEquals ("Template is in the preffered folder.", f, dobj.getFolder ());
    }
    
    public void testTwiceNewFromTemplate () throws Exception {
        testNewTemplateFromFile ();
        testNewTemplateFromFile ();
    }
    
    public void testDuplicateTemplate () {
        DataObject dobj = TemplatesPanel.createTemplateFromFile (popural, f);
        DataObject dupl = TemplatesPanel.createDuplicateFromNode (dobj.getNodeDelegate ());
        assertNotNull ("Duplicate DataObject found.", dobj);
        assertTrue ("Duplicate is template.", dobj.isTemplate ());
        assertEquals ("Template is in same folder as original.", dobj.getFolder (), dupl.getFolder ());
        assertTrue ("Name is derived from original.", dupl.getNodeDelegate ().getName ().startsWith (dobj.getNodeDelegate ().getName ()));
    }
    public void testIgnoresSimplefolders() throws Exception {
        FileObject root = FileUtil.getConfigRoot();
        FileObject fo = FileUtil.createFolder(root, "Templates/SimpleFolder");
        try {
            fo.setAttribute("simple", Boolean.FALSE);
            Node n = TemplatesPanel.getTemplateRootNode();
            Node[] arr = n.getChildren().getNodes(true);
            assertEquals("Empty: " + Arrays.asList(arr), 0, arr.length);
        } finally {
            // Cleanup Templates folder
            fo.getParent().delete();
        }
    }
    public void testIgnoresSimpleNonFolders() throws Exception {
        FileObject root = FileUtil.getConfigRoot();
        FileObject fo = FileUtil.createData(root, "Templates/SimpleFolder.java");
        try {
            fo.setAttribute("simple", Boolean.FALSE);
            fo.setAttribute("template", Boolean.TRUE);
            Node n = TemplatesPanel.getTemplateRootNode();
            Node[] arr = n.getChildren().getNodes(true);
            assertEquals("Empty: " + Arrays.asList(arr), 0, arr.length);
        } finally {
            // Cleanup Templates folder
            fo.getParent().delete();
        }
    }
}
