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

package org.netbeans.modules.editor.settings.storage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.fontscolors.ColoringStorage;
import org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Vita Stejskal
 */
public class LocatorTest extends NbTestCase {
    
    private static final String FC_CONTENTS = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE fontscolors PUBLIC \"-//NetBeans//DTD Editor Fonts and Colors settings 1.1//EN\" \"http://www.netbeans.org/dtds/EditorFontsColors-1_1.dtd\">\n" +
        "<fontscolors></fontscolors>";
    
    private static final String KB_CONTENTS = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE bindings PUBLIC \"-//NetBeans//DTD Editor KeyBindings settings 1.1//EN\" \"http://www.netbeans.org/dtds/EditorKeyBindings-1_1.dtd\">\n" +
        "<bindings></bindings>";

    /** Creates a new instance of LocatorTest */
    public LocatorTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource(
                    "org/netbeans/modules/editor/settings/storage/layer.xml"),
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

    public void testOsSpecificFiles() throws Exception {
        String currentOs = getCurrentOsId();
        String [] files = new String [] {
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/f.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/e.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/d.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/a.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file4.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file1.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file99.xml",
        };
        
        createOrderedFiles(files, FC_CONTENTS);
        
        FileObject f = FileUtil.getConfigFile("Editors/text/x-whatever/FontsColors/PPP/Defaults/e.xml");
        f.setAttribute("nbeditor-settings-targetOS", currentOs);

        String [] osOrderedFiles = new String [] {
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/f.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/d.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/a.xml",
            "Editors/text/x-whatever/FontsColors/PPP/Defaults/e.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file4.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file1.xml",
            "Editors/text/x-whatever/FontsColors/PPP/file99.xml",
        };
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors");
        Map<String, List<Object []>> results = new HashMap<>();
        scan(ColoringStorage.ID, baseFolder, "text/x-whatever", null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get("PPP");
        checkProfileFiles(osOrderedFiles, null, profileFiles, "PPP");
    }
    
    public void testFullLayout() throws Exception {
        String [] files1 = new String [] {
            "Editors/text/x-whatever/FontsColors/MyProfileA/Defaults/file1.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/Defaults/file2.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/Defaults/file3.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/Defaults/file4.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/file1.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/file2.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/file3.xml",
            "Editors/text/x-whatever/FontsColors/MyProfileA/org-netbeans-modules-editor-settings-CustomFontsColors.xml",
        };
        
        String [] files2 = new String [] {
            "Editors/text/x-whatever/FontsColors/MyProfile2/Defaults/xyz.xml",
            "Editors/text/x-whatever/FontsColors/MyProfile2/Defaults/abc.xml",
            "Editors/text/x-whatever/FontsColors/MyProfile2/mrkev.xml",
            "Editors/text/x-whatever/FontsColors/MyProfile2/okurka.xml",
            "Editors/text/x-whatever/FontsColors/MyProfile2/cibule.xml",
            "Editors/text/x-whatever/FontsColors/MyProfile2/org-netbeans-modules-editor-settings-CustomFontsColors.xml",
        };
        
        createOrderedFiles(files1, FC_CONTENTS);
        createOrderedFiles(files2, FC_CONTENTS);
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors");
        Map<String, List<Object []>> results = new HashMap<>();
        scan(ColoringStorage.ID, baseFolder, "text/x-whatever", null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 2, results.size());
        
        List<Object []> profileAFiles = results.get("MyProfileA");
        checkProfileFiles(files1, "Editors/text/x-whatever/FontsColors/MyProfileA/org-netbeans-modules-editor-settings-CustomFontsColors.xml", profileAFiles, "ProfileA");
        
        List<Object []> profile2Files = results.get("MyProfile2");
        checkProfileFiles(files2, "Editors/text/x-whatever/FontsColors/MyProfile2/org-netbeans-modules-editor-settings-CustomFontsColors.xml", profile2Files, "ProfileA");
    }
    
    public void testFullFontsColorsLegacyLayout() throws Exception {
        String [] files = new String [] {
            "Editors/NetBeans/Defaults/defaultColoring.xml",
            "Editors/NetBeans/Defaults/coloring.xml",
            "Editors/NetBeans/Defaults/editorColoring.xml",
            "Editors/NetBeans/coloring.xml",
            "Editors/NetBeans/editorColoring.xml",
        };
        
        createOrderedFiles(files, FC_CONTENTS);
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors");
        Map<String, List<Object []>> results = new HashMap<>();
        scan(ColoringStorage.ID, baseFolder, null, null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get("NetBeans");
        checkProfileFiles(files, null, profileFiles, "NetBeans");
    }

    public void testFullFontsColorsMixedLayout() throws Exception {
        String writableUserFile = "Editors/" + getWritableFileName(ColoringStorage.ID, "text/x-whatever", "NetBeans", "xyz", false);
        String [] files = new String [] {
            "Editors/text/x-whatever/NetBeans/Defaults/defaultColoring.xml",
            "Editors/text/x-whatever/NetBeans/Defaults/coloring.xml",
            "Editors/text/x-whatever/NetBeans/Defaults/editorColoring.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/Defaults/file1.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/Defaults/file2.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/Defaults/file3.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/Defaults/file4.xml",
            "Editors/text/x-whatever/NetBeans/coloring.xml",
            "Editors/text/x-whatever/NetBeans/editorColoring.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/file1.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/file2.xml",
            "Editors/text/x-whatever/FontsColors/NetBeans/file3.xml",
            writableUserFile,
        };
        
        createOrderedFiles(files, FC_CONTENTS);
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors");
        Map<String, List<Object []>> results = new HashMap<>();
        scan(ColoringStorage.ID, baseFolder, "text/x-whatever", null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get("NetBeans");
        checkProfileFiles(files, writableUserFile, profileFiles, "NetBeans");
    }
    
    public void testFullKeybindingsLegacyLayout() throws Exception {
        String writableUserFile = "Editors/" + getWritableFileName(KeyMapsStorage.ID, null, "NetBeans", null, false);
        String [] files = new String [] {
            "Editors/text/base/Defaults/keybindings.xml",
            "Editors/Keybindings/NetBeans/Defaults/zz.xml",
            "Editors/Keybindings/NetBeans/Defaults/dd.xml",
            "Editors/Keybindings/NetBeans/Defaults/kk.xml",
            "Editors/Keybindings/NetBeans/Defaults/aa.xml",
            "Editors/text/base/keybindings.xml",
            "Editors/Keybindings/NetBeans/papap.xml",
            "Editors/Keybindings/NetBeans/kekeke.xml",
            "Editors/Keybindings/NetBeans/dhdhdddd.xml",
            writableUserFile,
        };
        
        createOrderedFiles(files, KB_CONTENTS);
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors");
        Map<String, List<Object []>> results = new HashMap<>();
        scan(KeyMapsStorage.ID, baseFolder, null, null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get("NetBeans");
        checkProfileFiles(files, "Editors/Keybindings/NetBeans/org-netbeans-modules-editor-settings-CustomKeybindings.xml", profileFiles, "NetBeans");
    }

    public void testFullKeybindingsMixedLayout() throws Exception {
        String [] files = new String [] {
            "Editors/text/base/Defaults/keybindings.xml",
            "Editors/text/base/keybindings.xml",
        };
        
        createOrderedFiles(files, KB_CONTENTS);
        
        FileObject baseFolder = FileUtil.getConfigFile("Editors");
        Map<String, List<Object []>> results = new HashMap<>();
        scan(KeyMapsStorage.ID, baseFolder, null, null, true, true, true, results);
        
        assertNotNull("Scan results should not null", results);
        assertEquals("Wrong number of profiles", 1, results.size());
        
        List<Object []> profileFiles = results.get("NetBeans");
        checkProfileFiles(files, null, profileFiles, "NetBeans");
    }

    public static void checkProfileFiles(String [] paths, String writablePath, List<Object []> files, String profileId) {
        assertNotNull(profileId + ": No files", files);
        assertEquals(profileId + ": Wrong number of files", paths.length, files.size());
        
        int nrOfFiles = writablePath != null ? paths.length - 1 : paths.length;
        for(int i = 0; i < nrOfFiles; i++) {
            FileObject profileHome = (FileObject) files.get(i)[0];
            FileObject settingFile = (FileObject) files.get(i)[1];
            boolean modulesFile = ((Boolean) files.get(i)[2]).booleanValue();
            
            assertEquals(profileId + ": wrong file", paths[i], settingFile.getPath());
        }
        
        if (writablePath != null) {
            FileObject profileHome = (FileObject) files.get(files.size() - 1)[0];
            FileObject settingFile = (FileObject) files.get(files.size() - 1)[1];
            boolean modulesFile = ((Boolean) files.get(files.size() - 1)[2]).booleanValue();

            assertEquals(profileId + ": wrong writable file", writablePath, settingFile.getPath());
        }
    }
    
    public static void createOrderedFiles(String [] files, String contents) throws IOException {
        LinkedList<FileObject> createdFiles = new LinkedList<>();
        for(int i = 0; i < files.length; i++) {
            FileObject f = TestUtilities.createFile(files[i], contents);
            if(!createdFiles.isEmpty() && createdFiles.getLast().getParent() != f.getParent()) {
                FileUtil.setOrder(createdFiles);
                createdFiles.clear();
            }
            createdFiles.add(f);
        }
        FileUtil.setOrder(createdFiles);
        createdFiles.clear();
    }
    
    private String getCurrentOsId() {
        int osId = Utilities.getOperatingSystem();
        for(Field field : Utilities.class.getDeclaredFields()) {
            try {
                int value = field.getInt(null);
                if (value == osId) {
                    return field.getName();
                }
            } catch (Exception e) {
                // ignore
            }
        }
        fail("Can't detect OS type ");
        return null; // not reachable
    }
    
    public static void scan(String settingsTypeId, FileObject baseFolder, String mimeType, String profileId, boolean fullScan, boolean scanModules, boolean scanUsers, Map<String, List<Object []>> results) {
        SettingsType.Locator l = SettingsType.getLocator(SettingsType.find(settingsTypeId));
        assertNotNull("Can't find locator for '" + settingsTypeId + "'");
        l.scan(baseFolder, mimeType, profileId, fullScan, scanModules, scanUsers, false, results);
    }
    
    public static String getWritableFileName(String settingsTypeId, String mimeType, String profileId, String fileId, boolean modulesFile) {
        SettingsType.Locator l = SettingsType.getLocator(SettingsType.find(settingsTypeId));
        assertNotNull("Can't find locator for '" + settingsTypeId + "'");
        return l.getWritableFileName(mimeType, profileId, fileId, modulesFile);
    }
}
