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
package org.netbeans.api.diff;


import java.io.*;
import java.lang.reflect.Field;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.diff.PatchAction;
import org.openide.filesystems.FileUtil;

/**
 * 
 *
 * @author Tomas Stupka
 */
public class PatchUtilsTest extends NbTestCase {
    private File dataDir;
    
    public PatchUtilsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        dataDir = new File(getDataDir(), "patch");
        Field declaredField = PatchAction.class.getDeclaredField("skipReport");
        declaredField.setAccessible(true);
        declaredField.set(PatchAction.class, true);
    }

    public void testIsPatchNoFile() {
        try {
            assertFalse(PatchUtils.isPatch(new File(getWorkDir(), "nofile")));
        } catch (IOException ex) {
            return;
        }
        fail("Exception should be thrown");
    }

    public void testIsNoPatch() throws IOException {
        assertFalse(PatchUtils.isPatch(new File(dataDir, "emptyFile")));
        assertFalse(PatchUtils.isPatch(new File(dataDir, "plainTextFile")));
    }

    public void testIsPatchContext() throws IOException {
        assertTrue(PatchUtils.isPatch(new File(dataDir, "contextPatchFile")));
        assertTrue(PatchUtils.isPatch(new File(dataDir, "contextPatchNoTimestamp")));
    }

    public void testIsPatchNormal() throws IOException {
        assertTrue(PatchUtils.isPatch(new File(dataDir, "normalPatchFile")));
    }

    public void testIsPatchUnified() throws IOException {
        assertTrue(PatchUtils.isPatch(new File(dataDir, "unifiedPatchFile")));
        assertTrue(PatchUtils.isPatch(new File(dataDir, "unifiedPatchNoTimestamp")));
    }

    public void testFileApplyPatchNormal() throws IOException {
        File normalPatch = new File(dataDir, "normalPatchFile");
        File file = createFile();

        PatchUtils.applyPatch(normalPatch, file);
        assertFiles(new File(dataDir, "goldenFileAfter"), file, true);
    }

    public void testFileApplyPatchContext() throws IOException {
        File normalPatch = new File(dataDir, "contextPatchFile");
        File file = createFile();

        PatchUtils.applyPatch(normalPatch, file);
        assertFiles(new File(dataDir, "goldenFileAfter"), file, true);
    }

    public void testFileApplyPatchUnified() throws IOException {
        File normalPatch = new File(dataDir, "unifiedPatchFile");
        File file = createFile();

        PatchUtils.applyPatch(normalPatch, file);
        assertFiles(new File(dataDir, "goldenFileAfter"), file, true);
    }
    
    public void testFileSingleEdit () throws Exception {
        File dir = new File(dataDir, "testFileSingleEdit");
        File normalPatch = new File(dir, "patch.patch");
        File file = new File(dir, "NewClass.java");
        
        PatchUtils.applyPatch(normalPatch, dir);
        assertFiles(new File(dir, "golden/" + file.getName()), file, true);
    }
    
    public void testFileSingleCopy () throws Exception {
        File dir = new File(dataDir, "testFileSingleCopy");
        File normalPatch = new File(dir, "patch.patch");
        File file = new File(dir, "NewClass.java");
        File copied = new File(dir, "NewClassCopy.java");
        
        PatchUtils.applyPatch(normalPatch, dir);
        assertFiles(new File(dir, "golden/" + file.getName()), file, true);
        assertFiles(new File(dir, "golden/" + copied.getName()), copied, true);
    }
    
    public void testFileCopyCopyEdit () throws Exception {
        File dir = new File(dataDir, "testFileCopyCopyEdit");
        File normalPatch = new File(dir, "patch.patch");
        File file = new File(dir, "NewClass.java");
        File copied1 = new File(dir, "NewClassCopy1.java");
        File copied2 = new File(dir, "NewClassCopy2.java");
        
        PatchUtils.applyPatch(normalPatch, dir);
        assertFiles(new File(dir, "golden/" + file.getName()), file, true);
        assertFiles(new File(dir, "golden/" + copied1.getName()), copied1, true);
        assertFiles(new File(dir, "golden/" + copied2.getName()), copied2, true);
    }
    
    public void testFileSingleRename () throws Exception {
        File dir = new File(dataDir, "testFileSingleRename");
        File normalPatch = new File(dir, "patch.patch");
        File file = new File(dir, "NewClass.java");
        File renamed = new File(dir, "NewClassRename.java");
        
        PatchUtils.applyPatch(normalPatch, dir);
        assertFalse(file.exists());
        assertFiles(new File(dir, "golden/" + renamed.getName()), renamed, true);
    }
    
    public void testFileRenameRename () throws Exception {
        File dir = new File(dataDir, "testFileRenameRename");
        File normalPatch = new File(dir, "patch.patch");
        File file = new File(dir, "NewClass.java");
        File rename1 = new File(dir, "NewClassRename1.java");
        File rename2 = new File(dir, "NewClassRename2.java");
        
        PatchUtils.applyPatch(normalPatch, dir);
        assertFalse(file.exists());
        assertFiles(new File(dir, "golden/" + rename1.getName()), rename1, true);
        assertFiles(new File(dir, "golden/" + rename2.getName()), rename2, true);
    }
    
    public void testFileCopyRename () throws Exception {
        File dir = new File(dataDir, "testFileCopyRename");
        File normalPatch = new File(dir, "patch.patch");
        File file = new File(dir, "NewClass.java");
        File rename = new File(dir, "NewClassRename.java");
        File copy = new File(dir, "NewClassCopy.java");
        
        PatchUtils.applyPatch(normalPatch, dir);
        assertFalse(file.exists());
        assertFiles(new File(dir, "golden/" + rename.getName()), rename, true);
        assertFiles(new File(dir, "golden/" + copy.getName()), copy, true);
    }

    private void assertFiles(File golden, File file, boolean equal) throws FileNotFoundException, IOException {
        if(equal) {
            assertEquals(golden.length(), file.length());
        } else {
            if(golden.length() != file.length()) {
                return;
            }
        }

        BufferedReader goldenReader = new BufferedReader(new FileReader(golden));
        BufferedReader fileReader = new BufferedReader(new FileReader(file));

        String gl, fl;
        boolean diff = false;
        while( (gl = goldenReader.readLine()) != null ) {
            fl = fileReader.readLine();
            if(equal) assertEquals(gl, fl);
            if(!equal) {
                diff = !gl.equals(fl);
                if(diff) {
                    break;
                }
            }
        }
        if(!equal && !diff) fail("files are the same");
        if(equal) {
            assertNull(fileReader.readLine());
        }
    }

    private File createFile() throws IOException {
        return createFile(new File(getWorkDir(), "file"));
    }

    private File createFile(File file) throws IOException {
        final FileInputStream fileInputStream = new FileInputStream(new File(dataDir, "goldenFileBefore"));
        final FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            FileUtil.copy(fileInputStream, fileOutputStream);
        } finally {
            fileInputStream.close();
            fileOutputStream.close();
        }
        return file;
    }

}
