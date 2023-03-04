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

package org.netbeans.lib.editor.codetemplates.storage;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;
import org.netbeans.modules.editor.settings.storage.LocatorTest;
import org.netbeans.modules.editor.settings.storage.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vita Stejskal
 */
public class CodeTemplatesLocatorTest extends NbTestCase {
    
    private static final String CT_CONTENTS = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE codetemplates PUBLIC \"-//NetBeans//DTD Editor Code Templates settings 1.0//EN\" \"http://www.netbeans.org/dtds/EditorCodeTemplates-1_0.dtd\">\n" +
        "<codetemplates></codetemplates>";
    
    
    /** Creates a new instance of LocatorTest */
    public CodeTemplatesLocatorTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/lib/editor/codetemplates/resources/layer.xml"),
                getClass().getClassLoader().getResource(
                    "org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );

        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
        // which is needed by Nb EntityCatalog (org.netbeans.core).
        // Also see the test dependencies in project.xml
        Main.initializeURLFactory();
    }

    public void testFullCodeTemplatesMixedLayout() throws Exception {
        String writableUserFile = "Editors/" + LocatorTest.getWritableFileName(CodeTemplatesStorage.ID, null, null, null, false);
        String [] files = new String [] {
            "Editors/Defaults/abbreviations.xml",
            "Editors/CodeTemplates/Defaults/zz.xml",
            "Editors/CodeTemplates/Defaults/dd.xml",
            "Editors/CodeTemplates/Defaults/kk.xml",
            "Editors/CodeTemplates/Defaults/aa.xml",
            "Editors/abbreviations.xml",
            "Editors/CodeTemplates/papap.xml",
            "Editors/CodeTemplates/kekeke.xml",
            "Editors/CodeTemplates/dhdhdddd.xml",
            writableUserFile
        };
        
        
        LocatorTest.createOrderedFiles(files, CT_CONTENTS);
//        TestUtilities.createFile(writableUserFile, CT_CONTENTS);
//        LocatorTest.orderFiles("Editors/CodeTemplates/dhdhdddd.xml", writableUserFile);
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors");
        Map<String, List<Object []>> results = new HashMap<String, List<Object []>>();
        LocatorTest.scan(CodeTemplatesStorage.ID, baseFolder, null, null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get(null);
        LocatorTest.checkProfileFiles(files, writableUserFile, profileFiles, null);
    }

    public void testFullCodeTemplatesLegacyLayout() throws Exception {
        String [] files = new String [] {
            "Editors/Defaults/abbreviations.xml",
            "Editors/abbreviations.xml",
        };
        
        LocatorTest.createOrderedFiles(files, CT_CONTENTS);
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors");
        Map<String, List<Object []>> results = new HashMap<String, List<Object []>>();
        LocatorTest.scan(CodeTemplatesStorage.ID, baseFolder, null, null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get(null);
        LocatorTest.checkProfileFiles(files, null, profileFiles, null);
    }
}
