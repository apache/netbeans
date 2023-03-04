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
package org.netbeans.modules.openide.loaders;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;
import org.openide.util.test.TestFileUtils;

/**
 * See defect #208779 - DataShadows should update themselves iff they could not find 
 * the target.
 * 
 * @author sdedic
 */
public class DataShadowTranslateTest extends NbTestCase {

    public DataShadowTranslateTest(String name) {
        super(name);
    }
    
    /**
     * Checks that file with just regular characters in name is translated OK
     * @throws Exception 
     */
    public void testRegularURI() throws Exception {
        
        FileObject fo = FileUtil.getConfigRoot();
        
        FileObject origDir = fo.createFolder("origFolder");
        FileObject newFile = origDir.createData("regularFileName.txt");
        
        final FileObject d = fo.createFolder("subfolder");
        OutputStream ostm = d.createAndOpen("regularShadowURI.shadow");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ostm));
        
        URI uri = newFile.toURI();
        String urlString = newFile.toURI().toString();
        bw.write(urlString + ".old");
        bw.newLine();
        bw.newLine();
        bw.close();
        
        FileObject fob = d.getFileObject("regularShadowURI.shadow");
        DataObject dd = DataObject.find(fob);
        
        assertTrue("Shadow must be translated, not broken", dd instanceof DataShadow);
        
        DataShadow ds = (DataShadow)dd;
        assertEquals("Shadow's original must be on the translated location", newFile, ds.getOriginal().getPrimaryFile());
    }
    

    /**
     * Checks translation on Shadows, which use FS name + path, not URI
     * @throws Exception 
     */
    public void testFSNameAndPath() throws Exception {
        FileObject fo = FileUtil.getConfigRoot();
        
        FileObject origDir = fo.createFolder("origFolder3");
        
        // create empty real file with special and non-ASCII chars
        FileObject newFile = origDir.createData("moved-here.txt");
        
        // createa a fake file, just to get its URI right:
        FileObject fake = fo.createData("dead-file-location.old");
        
        final FileObject d = fo.createFolder("subfolder3");
        OutputStream ostm = d.createAndOpen("regularShadowURI.shadow");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ostm));
        
        bw.write(fake.getPath());
        bw.newLine();
        bw.write(fake.getFileSystem().getSystemName());
        bw.newLine();
        
        fake.delete();
        
        bw.close();
        
        FileObject fob = d.getFileObject("regularShadowURI.shadow");
        DataObject dd = DataObject.find(fob);
        
        assertTrue("Shadow must be translated, not broken", dd instanceof DataShadow);
        
        DataShadow ds = (DataShadow)dd;
        assertEquals("Shadow's original must be on the translated location", newFile, ds.getOriginal().getPrimaryFile());
    }
    
    /**
     * Checks that DataShadows to regular (non-SFS) files are not translated
     * even if a translation is defined for their path
     * 
     * @throws Exception 
     */
    public void testNonSFSUriNotAffected() throws Exception {
        File wd = getWorkDir();
        
        clearWorkDir();
        
        FileObject origDir = FileUtil.toFileObject(wd);
        
        FileObject dirWithSpace = origDir.createFolder("Space Dir");
        FileObject newFile = dirWithSpace.createData("testFile.txt");
        

        File subDir = new File(wd, "translate");
        
        subDir.mkdirs();
        
        File metaTranslate = new File(subDir, "META-INF/netbeans");
        metaTranslate.mkdirs();
        
        String workPath = newFile.toURI().getRawPath();
        FileWriter wr = new FileWriter(new File(metaTranslate, "translate.names"));
        BufferedWriter bw = new BufferedWriter(wr);
        
        bw.write(workPath.substring(1) + "/testFile.txt=" + workPath.substring(1) + "/moved/testFile.txt");
        bw.close();
        
        
        FileObject fo = FileUtil.toFileObject(wd);
        
        ClassLoader orig = Lookup.getDefault().lookup(ClassLoader.class);
        
        ClassLoader my = new URLClassLoader(new URL[] {
            subDir.toURL()
        }, orig);
        
        MockLookup.setInstances(my);
        
        
        FileObject cfgRoot = FileUtil.getConfigRoot();
        
        
        OutputStream ostm = cfgRoot.createAndOpen("nonSFSFile.shadow");
        
        bw = new BufferedWriter(new OutputStreamWriter(ostm));
        
        bw.write(newFile.toURI().toString());
        bw.newLine();
        bw.newLine();
        
        newFile.delete();
        
        bw.close();
        
        FileObject fob = cfgRoot.getFileObject("nonSFSFile.shadow");
        DataObject dd = DataObject.find(fob);
        
        assertFalse("Shadow must be still broken", dd instanceof DataShadow);
    }
}

