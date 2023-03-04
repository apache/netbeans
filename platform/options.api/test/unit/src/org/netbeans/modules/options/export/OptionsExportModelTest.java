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
package org.netbeans.modules.options.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.options.export.OptionsExportModel.Category;
import org.netbeans.modules.options.export.OptionsExportModel.Item;
import org.netbeans.modules.options.export.OptionsExportModel.State;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Jiri Skrivanek
 */
public class OptionsExportModelTest extends NbTestCase {

    static OptionsExportModel model;
    static File sourceUserdir;
    static FileObject optionsExportFolder;
    private static final Logger LOGGER = Logger.getLogger(OptionsExportModel.class.getName());

    public OptionsExportModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        /** create source userdir with files

        sourceUserdir\dir0\subdir0\file0.properties
        sourceUserdir\dir0\subdir0\file1.properties
        sourceUserdir\dir0\subdir1\file0.properties
        sourceUserdir\dir0\subdir1\file1.properties
        sourceUserdir\dir1\subdir0\file0.properties
        sourceUserdir\dir1\subdir0\file1.properties
        sourceUserdir\dir1\subdir1\file0.properties
        sourceUserdir\dir1\subdir1\file1.properties
         */
        sourceUserdir = new File(getWorkDir(), "sourceUserdir");
        sourceUserdir.mkdir();
        for (int i = 0; i < 2; i++) {
            File dir = new File(sourceUserdir, "dir" + i);
            for (int j = 0; j < 2; j++) {
                File subdir = new File(dir, "subdir" + j);
                subdir.mkdirs();
                for (int k = 0; k < 2; k++) {
                    File file = new File(subdir, "file" + k + ".properties");
                    file.createNewFile();
                    Properties properties = new Properties();
                    for (int l = 0; l < 3; l++) {
                        properties.setProperty("key" + l, "value" + l);
                    }
                    OutputStream out = new FileOutputStream(file);
                    properties.store(out, null);
                    out.close();
                }
            }
        }
    }

    public void testExport() throws Exception {
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/.*", null}
                });
        File targetZipFile = new File(getWorkDir(), "export.zip");
        ArrayList<String> enabledItems = new ArrayList<String>();
        model.doExport(targetZipFile, enabledItems);
        LOGGER.finest(OptionsExportModel.listZipFile(targetZipFile).toString());
        List<String> expected = new ArrayList<String>();
        expected.add("dir0/subdir0/file0.properties");
        expected.add("dir0/subdir0/file1.properties");
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));

        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/.*", null},
                    {"Category0", "Item01", "dir1/subdir0/.*", null}
                });
        targetZipFile = new File(getWorkDir(), "export1.zip");
        model.doExport(targetZipFile, enabledItems);
        LOGGER.finest(OptionsExportModel.listZipFile(targetZipFile).toString());
        expected.add("dir1/subdir0/file0.properties");
        expected.add("dir1/subdir0/file1.properties");
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));

        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/.*", null},
                    {"Category0", "Item01", null, "dir0/subdir0/file1.*"}
                });
        targetZipFile = new File(getWorkDir(), "export2.zip");
        model.doExport(targetZipFile, enabledItems);
        LOGGER.finest(OptionsExportModel.listZipFile(targetZipFile).toString());
        expected.clear();
        expected.add("dir0/subdir0/file0.properties");
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));
    }

    public void testExportProperties() throws Exception {
        // only key1 from file0.properties and entire file file1.properties
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/file0[.]properties#key1#|dir1/subdir1/file1[.]properties", null}
                });
        File targetZipFile = new File(getWorkDir(), "export.zip");
        ArrayList<String> enabledItems = new ArrayList<String>();
        model.doExport(targetZipFile, enabledItems);
        LOGGER.finest(OptionsExportModel.listZipFile(targetZipFile).toString());
        List<String> expected = new ArrayList<String>();
        expected.add("dir0/subdir0/file0.properties");
        expected.add("dir1/subdir1/file1.properties");
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));
        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("key1");
        assertProperties(expectedKeys, "dir0/subdir0/file0.properties", targetZipFile);

        // all keys from file0.properties inspite one pattern defines only key1
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/file0[.]properties#key1#|dir0/subdir0/file1[.]properties", null},
                    {"Category0", "Item01", "dir0/subdir0/file0[.]properties|dir0/subdir1/file0[.]properties#key2", null}
                });
        targetZipFile = new File(getWorkDir(), "export1.zip");
        model.doExport(targetZipFile, enabledItems);
        LOGGER.finest(OptionsExportModel.listZipFile(targetZipFile).toString());
        expected = Arrays.asList("dir0/subdir0/file0.properties", "dir0/subdir0/file1.properties", "dir0/subdir1/file0.properties");
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));
        expectedKeys = Arrays.asList("key0", "key1", "key2");
        assertProperties(expectedKeys, "dir0/subdir0/file0.properties", targetZipFile);
        assertProperties(expectedKeys, "dir0/subdir0/file1.properties", targetZipFile);
        expectedKeys = Arrays.asList("key2");
        assertProperties(expectedKeys, "dir0/subdir1/file0.properties", targetZipFile);

        // 2 keys from 2 different files
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/file0[.]properties#key1#|dir0/subdir0/file1[.]properties#key2", null},});
        targetZipFile = new File(getWorkDir(), "export2.zip");
        model.doExport(targetZipFile, enabledItems);
        LOGGER.finest(OptionsExportModel.listZipFile(targetZipFile).toString());
        expected = Arrays.asList("dir0/subdir0/file0.properties", "dir0/subdir0/file1.properties");
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));
        expectedKeys = Arrays.asList("key1");
        assertProperties(expectedKeys, "dir0/subdir0/file0.properties", targetZipFile);
        expectedKeys = Arrays.asList("key2");
        assertProperties(expectedKeys, "dir0/subdir0/file1.properties", targetZipFile);

        // 2 keys in the same file from different patterns
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/file0[.]properties#key1#|dummy.*", null},
                    {"Category0", "Item01", "dummy.*|dir0/subdir0/file0[.]properties#key2#", null}
                });
        targetZipFile = new File(getWorkDir(), "export3.zip");
        model.doExport(targetZipFile, enabledItems);
        LOGGER.finest(OptionsExportModel.listZipFile(targetZipFile).toString());
        expected = Arrays.asList("dir0/subdir0/file0.properties");
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));
        expectedKeys = Arrays.asList("key1", "key2");
        assertProperties(expectedKeys, "dir0/subdir0/file0.properties", targetZipFile);

        // key wildcard
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/file0[.]properties#key.*#|dummy.*", null}
                });
        targetZipFile = new File(getWorkDir(), "export4.zip");
        model.doExport(targetZipFile, enabledItems);
        LOGGER.finest(OptionsExportModel.listZipFile(targetZipFile).toString());
        expected = Arrays.asList("dir0/subdir0/file0.properties");
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));
        expectedKeys = Arrays.asList("key0", "key1", "key2");
        assertProperties(expectedKeys, "dir0/subdir0/file0.properties", targetZipFile);

        // key exclude
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/file0[.]properties#key.*", "dir0/subdir0/file0[.]properties#key1"},
                    {"Category0", "Item01", "dir1/subdir1/file1[.]properties", null},
                    {"Category0", "Item02", null, "dir1/subdir1/file1[.]properties#key1"}
                });
        targetZipFile = new File(getWorkDir(), "export5.zip");
        model.doExport(targetZipFile, enabledItems);
        LOGGER.finest(OptionsExportModel.listZipFile(targetZipFile).toString());
        expected = Arrays.asList("dir0/subdir0/file0.properties", "dir1/subdir1/file1.properties");
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));
        expectedKeys = Arrays.asList("key0", "key2");
        assertProperties(expectedKeys, "dir0/subdir0/file0.properties", targetZipFile);
        assertProperties(expectedKeys, "dir1/subdir1/file1.properties", targetZipFile);

        // include key but exclude entire file => nothing exported
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/file0[.]properties#key0", null},
                    {"Category0", "Item01", null, "dir0/subdir0/file0[.]properties"}
                });
        targetZipFile = new File(getWorkDir(), "export6.zip");
        model.doExport(targetZipFile, enabledItems);
        LOGGER.finest(OptionsExportModel.listZipFile(targetZipFile).toString());
        expected = Collections.emptyList();
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));
    }

    public void testImport() throws Exception {
        // import from source userdir to current userdir
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/.*", null}
                });
        File targetUserdir = new File(getWorkDir(), "userdir");
        targetUserdir.mkdir();
        model.doImport(targetUserdir);
        List<String> expected = new ArrayList<String>();
        expected.add("dir0/subdir0/file0.properties");
        expected.add("dir0/subdir0/file1.properties");
        assertFiles(expected, OptionsExportModel.getRelativePaths(targetUserdir));

        // import from zipped userdir to current userdir
        File sourceZipFile = new File(getWorkDir(), "sourceUserdir.zip");
        OptionsExportModel.createZipFile(sourceZipFile, sourceUserdir, OptionsExportModel.getRelativePaths(sourceUserdir));
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/.*", null}
                }, sourceZipFile);
        targetUserdir = new File(getWorkDir(), "userdir1");
        model.doImport(targetUserdir);
        assertFiles(expected, OptionsExportModel.getRelativePaths(targetUserdir));
    }

    public void test239958() throws Exception {
        // import from source userdir to current userdir
        // exclude pattern equals with another include pattern leads to that pattern being skipped
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/.*", "dir0/subdir1/.*"},
                    {"Category0", "Item01", "dir0/subdir1/.*", null}
                });
        File targetUserdir = new File(getWorkDir(), "userdir");
        targetUserdir.mkdir();
        model.doImport(targetUserdir);
        List<String> expected = new ArrayList<String>();
        expected.add("dir0/subdir0/file0.properties");
        expected.add("dir0/subdir0/file1.properties");
        assertFiles(expected, OptionsExportModel.getRelativePaths(targetUserdir));

        // import from zipped userdir to current userdir
        // exclude pattern does not equal with another include pattern leads to that pattern being processed properly
        File sourceZipFile = new File(getWorkDir(), "sourceUserdir.zip");
        OptionsExportModel.createZipFile(sourceZipFile, sourceUserdir, OptionsExportModel.getRelativePaths(sourceUserdir));
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/.*", null},
                    {"Category0", "Item01", "dir0/subdir1/.*", null}
                }, sourceZipFile);
        targetUserdir = new File(getWorkDir(), "userdir1");
        model.doImport(targetUserdir);
        expected.add("dir0/subdir1/file0.properties");
        expected.add("dir0/subdir1/file1.properties");
        assertFiles(expected, OptionsExportModel.getRelativePaths(targetUserdir));
    }
    
    public void test227906() throws Exception {
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/.*", null},
                });
        File targetZipFile = new File(getWorkDir(), "export.zip");
        ArrayList<String> enabledItems = new ArrayList<String>();
        enabledItems.add("Category0Item01");
        model.doExport(targetZipFile, enabledItems);
        LOGGER.info(OptionsExportModel.listZipFile(targetZipFile).toString());
        List<String> expected = new ArrayList<String>();
        expected.add("dir0/subdir0/file0.properties");
        expected.add("dir0/subdir0/file1.properties");
        assertFiles(expected, OptionsExportModel.listZipFile(targetZipFile));
        try {
            ZipFile zipFile = new ZipFile(targetZipFile);
            // Enumerate each entry
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                if (zipEntry.getName().equals(OptionsExportModel.ENABLED_ITEMS_INFO)) {
                    InputStream stream = zipFile.getInputStream(zipEntry);
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        assertEquals("Wrong data saved in enabledItems.info", "Category0Item01", strLine);
                    }
                }
            }
        } catch (ZipException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void test244640() throws Exception {
        createModel(new String[][]{
            {"Category0", "Item00", "dir0/subdir0/.*", null},});
        ArrayList<String> enabledItems = new ArrayList<String>();
        enabledItems.add("Category0Item01");

        File targetZipFile = new File(getWorkDir(), "export.zip");
        model.doExport(targetZipFile, enabledItems);
        assertEquals(enabledItems, model.getEnabledItemsDuringExport(targetZipFile)); // reading enabledItems.info from zip file
        
        assertEquals(null, model.getEnabledItemsDuringExport(sourceUserdir)); // no enabledItems.info in user dir, e.g. version < 7.4
        // first write the enabledItems.info in user dir
        Files.write(Paths.get(Utilities.toURI(new File(sourceUserdir, OptionsExportModel.ENABLED_ITEMS_INFO))), "Category0Item01".getBytes());
        assertEquals(enabledItems, model.getEnabledItemsDuringExport(sourceUserdir)); // reading enabledItems.info from user dir
    }
    
    public void testGetBuildNumberDuringExport() throws Exception {
        createModel(new String[][]{
            {"Category0", "Item00", "dir0/subdir0/.*", null},});
        ArrayList<String> enabledItems = new ArrayList<String>();
        enabledItems.add("Category0Item01");

        File targetZipFile = new File(getWorkDir(), "export.zip");
        System.setProperty("netbeans.buildnumber", "201408251540");
        model.doExport(targetZipFile, enabledItems);
        assertEquals(Double.parseDouble("201408251540"), model.getBuildNumberDuringExport(targetZipFile));
        
        targetZipFile = new File(getWorkDir(), "export2.zip");
        System.setProperty("netbeans.buildnumber", "20141127-4fefcedaef32");
        model.doExport(targetZipFile, enabledItems);
        assertEquals(Double.parseDouble("201411272359"), model.getBuildNumberDuringExport(targetZipFile));
    }
    
    public void testParseBuildNumber() throws Exception {
        createModel(new String[][]{
            {"Category0", "Item00", "dir0/subdir0/.*", null},});
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("ProductVersion=NetBeans IDE 8.0.1 (Build 201408251540)", "201408251540");
        map.put("ProductVersion=NetBeans IDE 8.0.1 (Build 20141127-4fefcedaef32)", "201411272359");
        map.put("ProductVersion=OptionsImport 201408251545", "201408251545");
        map.put("NetbeansBuildnumber=201408251540", "201408251540");
        map.put("NetbeansBuildnumber=20141127-4fefcedaef32", "201411272359");
        map.put("NetbeansBuildnumber=nbms-and-javadoc-2438-on-20141212", "201412122359");
        map.put("201408251540", "201408251540");
        map.put("20141127", "201411272359");
        map.put("20141127-4fefcedaef32", "201411272359");
        
        for(Map.Entry<String, String> entry : map.entrySet()) {
            assertTrue("Error parsing: " + entry.getKey(), model.parseBuildNumber(entry.getKey()).equals(entry.getValue()));
        }
    }

    public void testImportProperties() throws Exception {
        // import from source userdir to current userdir
        // only key1 from file0.properties and entire file file1.properties
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/file0[.]properties#key1#|dir1/subdir1/file1[.]properties", null}
                });
        File targetUserdir = new File(getWorkDir(), "userdir");
        targetUserdir.mkdir();
        model.doImport(targetUserdir);
        List<String> expected = new ArrayList<String>();
        expected.add("dir0/subdir0/file0.properties");
        expected.add("dir1/subdir1/file1.properties");
        assertFiles(expected, OptionsExportModel.getRelativePaths(targetUserdir));
        List<String> expectedKeys = new ArrayList<String>();
        expectedKeys.add("key1");
        assertProperties(expectedKeys, "dir0/subdir0/file0.properties", targetUserdir);

        // import from zipped userdir to current userdir
        File sourceZipFile = new File(getWorkDir(), "sourceUserdir.zip");
        OptionsExportModel.createZipFile(sourceZipFile, sourceUserdir, OptionsExportModel.getRelativePaths(sourceUserdir));
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/file0[.]properties#key1#|dir1/subdir1/file1[.]properties", null}
                }, sourceZipFile);
        targetUserdir = new File(getWorkDir(), "userdir1");
        model.doImport(targetUserdir);
        assertFiles(expected, OptionsExportModel.getRelativePaths(targetUserdir));
        expectedKeys = new ArrayList<String>();
        expectedKeys.add("key1");
        assertProperties(expectedKeys, "dir0/subdir0/file0.properties", targetUserdir);

        // import from source userdir to current userdir
        // value for key1 should be replaced from source userdir, key2 should be added, key3 should be kept
        createModel(new String[][]{
                    {"Category0", "Item00", "dir0/subdir0/file0[.]properties#key[12]", null}
                });
        targetUserdir = new File(getWorkDir(), "userdir2");
        targetUserdir.mkdir();
        // create dir0/subdir0/file0.properties in target userdir
        File targetPropertiesfile = new File(targetUserdir, "dir0/subdir0/file0.properties");
        targetPropertiesfile.getParentFile().mkdirs();
        targetPropertiesfile.createNewFile();
        Properties properties = new Properties();
        properties.setProperty("key1", "old_value1");  // to be replaced by import
        properties.setProperty("key3", "value3");  // should be kept after import
        OutputStream out = new FileOutputStream(targetPropertiesfile);
        properties.store(out, null);
        out.close();
        model.doImport(targetUserdir);
        expected = new ArrayList<String>();
        expected.add("dir0/subdir0/file0.properties");
        assertFiles(expected, OptionsExportModel.getRelativePaths(targetUserdir));
        Properties expectedProperties = new Properties();
        expectedProperties.load(new FileInputStream(targetPropertiesfile));
        assertEquals("Wrong number of properties.", 3, expectedProperties.size());
        assertEquals("Wrong value of property key1.", "value1", expectedProperties.getProperty("key1"));
        assertEquals("Wrong value of property key2.", "value2", expectedProperties.getProperty("key2"));
        assertEquals("Wrong value of property key3.", "value3", expectedProperties.getProperty("key3"));
    }

    /** Tests that categories and items properly change enabled/disabled state. */
    public void testEnablingDisabling() throws Exception {
        createModel(new String[][]{
                    {"Category0", "Item00", ".*", null},
                    {"Category1", "Item00", ".*", null}
                });
        Category category0 = model.getCategories().get(0);
        category0.setState(State.DISABLED);
        Category category1 = model.getCategories().get(1);
        category1.setState(State.DISABLED);
        assertEquals(OptionsExportModel.State.DISABLED, model.getState());

        category0.setState(OptionsExportModel.State.ENABLED);
        assertEquals(OptionsExportModel.State.ENABLED, category0.getState());
        assertEquals(OptionsExportModel.State.PARTIAL, model.getState());
        for (Item item : category0.getItems()) {
            assertTrue(item.isEnabled());
        }

        category1.setState(OptionsExportModel.State.ENABLED);
        assertEquals(OptionsExportModel.State.ENABLED, model.getState());

        category0.setState(OptionsExportModel.State.DISABLED);
        assertEquals(OptionsExportModel.State.DISABLED, category0.getState());
        assertEquals(OptionsExportModel.State.PARTIAL, model.getState());
        for (Item item : category0.getItems()) {
            assertFalse(item.isEnabled());
        }

        category1.setState(OptionsExportModel.State.DISABLED);
        assertEquals(OptionsExportModel.State.DISABLED, model.getState());
    }

    /** Tests patterns with properties key parts. */
    public void testKeyPattern() {
        String pattern = "config/org-netbeans-api-project-libraries/Libraries/.*\\.xml|build\\.properties#nbplatform[.](?!default[.]netbeans[.]dest[.]dir).+[.].+=.+|var[.].*#";
        Set<String> expected = new TreeSet<String>();
        expected.add("config/org-netbeans-api-project-libraries/Libraries/.*\\.xml");
        expected.add("build\\.properties#nbplatform[.](?!default[.]netbeans[.]dest[.]dir).+[.].+=.+|var[.].*");
        assertPatterns(expected, OptionsExportModel.parsePattern(pattern));

        pattern = "build\\.properties#nbplatform[.](?!default[.]netbeans[.]dest[.]dir).+[.].+=.+|var[.].*#|config/org-netbeans-api-project-libraries/Libraries/.*\\.xml";
        assertPatterns(expected, OptionsExportModel.parsePattern(pattern));

        pattern = "build\\.properties#nbplatform|var|aaa#|BBB|CCC|DDD#EEE|FFF#";
        expected.clear();
        expected.add("build\\.properties#nbplatform|var|aaa");
        expected.add("BBB");
        expected.add("CCC");
        expected.add("DDD#EEE|FFF");
        assertPatterns(expected, OptionsExportModel.parsePattern(pattern));

        // missing ending # never minds
        pattern = "build\\.properties#nbplatform|var|aaa#|BBB|CCC|DDD#EEE|FFF";
        assertPatterns(expected, OptionsExportModel.parsePattern(pattern));

        // parenthesis
        pattern = "AAA#aaa#|BBB(b1|(b2|b3))|CCC|DDD#EEE|FFF(f1|(f2|f3))";
        expected.clear();
        expected.add("AAA#aaa");
        expected.add("BBB(b1|(b2|b3))");
        expected.add("CCC");
        expected.add("DDD#EEE|FFF(f1|(f2|f3))");
        assertPatterns(expected, OptionsExportModel.parsePattern(pattern));
    }

    private void createModel(String[][] modelTemplate) throws Exception {
        createModel(modelTemplate, sourceUserdir);
    }

    /** Creates a new OptionsExportModel based on given template. Model looks
     * like this:
     *
     * <folder name="OptionsExport">
     *     <folder name="Category0">
     *         <attr name="displayName" stringvalue="Category0"/>
     *         <file name="Item00">
     *             <attr name="displayName" stringvalue="Item00"/>
     *             <attr name="include" stringvalue="dir0/subdir0/.*"/>
     *         </file>
     *     </folder>
     *     <folder name="Category1">
     *         <attr name="displayName" stringvalue="Category1"/>
     *         <file name="Item10">
     *             <attr name="displayName" stringvalue="Item11"/>
     *             <attr name="include" stringvalue="dir0/subdir0/file0[.]properties|dir1/subdir1/file1.*"/>
     *         </file>
     *     </folder>
     * </folder>
     *
     * @param modelTemplate array of category display name, item display name,
     * include pattern, exclude pattern
     * @param source source userdir or zip file
     * @throws Exception
     */
    private void createModel(String[][] modelTemplate, File source) throws Exception {
        FileObject root = FileUtil.getConfigRoot();
        optionsExportFolder = root.getFileObject("OptionsExport");
        if (optionsExportFolder != null) {
            optionsExportFolder.delete();
        }
        optionsExportFolder = FileUtil.createFolder(root, "OptionsExport");
        for (String[] fields : modelTemplate) {
            String categoryName = fields[0];
            String itemName = fields[1];
            String include = fields[2];
            String exclude = fields[3];
            createCategory(categoryName, 0);
            createItem(categoryName, itemName, include, exclude);
        }
        model = new OptionsExportModel(source);
        // enable all and print model
        for (OptionsExportModel.Category category : model.getCategories()) {
            LOGGER.info("category=" + category);  //NOI18N
            List<OptionsExportModel.Item> items = category.getItems();
            for (OptionsExportModel.Item item : items) {
                item.setEnabled(true);
                LOGGER.info("    item=" + item);  //NOI18N
            }
        }
    }

    /** Creates a new category in model. */
    private FileObject createCategory(String name, int position) throws Exception {
        FileObject folder = FileUtil.createFolder(optionsExportFolder, name);
        folder.setAttribute("displayName", name);
        folder.setAttribute("position", position);
        return folder;
    }

    /** Creates a new item in model. */
    private FileObject createItem(String categoryName, String name, String include, String exclude) throws Exception {
        FileObject categoryFO = optionsExportFolder.getFileObject(categoryName);
        FileObject file = FileUtil.createData(categoryFO, name);
        if (include != null) {
            file.setAttribute("include", include);
        }
        if (exclude != null) {
            file.setAttribute("exclude", exclude);
        }
        file.setAttribute("displayName", name);
        return file;
    }

    /** Assert files for export/import.*/
    private void assertFiles(List<String> expected, List<String> actual) {
        if (actual.size() > 0 && actual.get(actual.size() - 1).equals("build.info")) {
            // Skip build.info at the last position.
            actual.remove(actual.size() - 1);
        }
        if (actual.size() > 0 && actual.get(actual.size() - 1).equals(OptionsExportModel.ENABLED_ITEMS_INFO)) {
            // Skip enabledItems.info at the last position.
            actual.remove(actual.size() - 1);
        }
        assertEquals("Wrong number of files filtered.", expected.size(), actual.size());
        Iterator<String> iter = new TreeSet<String>(actual).iterator();
        for (String file : expected) {
            assertEquals("Wrong file.", file, iter.next());
        }
    }

    /** Assert properties for export/import. */
    private void assertProperties(List<String> expectedKeys, String relativePath, File zipOrRootFile) throws Exception {
        Properties properties = new Properties();
        properties.load(getInputStream(relativePath, zipOrRootFile));
        assertEquals("Wrong number of property keys.", expectedKeys.size(), properties.size());
        Iterator<Object> iter = new TreeSet<Object>(properties.keySet()).iterator();
        for (String key : expectedKeys) {
            assertEquals("Wrong key.", key, iter.next());
        }
    }

    private InputStream getInputStream(String relativePath, File zipOrRootFile) throws Exception {
        if (zipOrRootFile.isFile()) {
            //zip file
            ZipFile zipFile = new ZipFile(zipOrRootFile);
            ZipEntry zipEntry = zipFile.getEntry(relativePath);
            return zipFile.getInputStream(zipEntry);
        } else {
            // userdir
            return new FileInputStream(new File(zipOrRootFile, relativePath));
        }
    }

    private void assertPatterns(Set<String> expected, Set<String> actual) {
        assertEquals("Wrong number of patterns", expected.size(), actual.size());
        Iterator<String> iter = new TreeSet<String>(actual).iterator();
        for (String pattern : expected) {
            assertEquals("Wrong pattern.", pattern, iter.next());
        }
    }
}
