/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.upgrade;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;

/** Tests to check that copy of files works.
 *
 * @author Jaroslav Tulach
 */
public final class CopyTest extends org.netbeans.junit.NbTestCase {
    public CopyTest (String name) {
        super (name);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        
        clearWorkDir();
    }    
   
    public void testAppendSelectedLines() throws Exception {
        //setup
        List expectedLines = new ArrayList();
        File wDir = getWorkDir();
        File sFile = new File(wDir,this.getName()+".file");
        assertTrue(sFile.createNewFile());
        File tFolder = new File(wDir,this.getName());
        assertTrue(tFolder.mkdir());
        File tFile = new File(tFolder,this.getName()+".file");
        assertTrue(tFile.createNewFile());
        FileOutputStream fos = new FileOutputStream(tFile);
        try {
            String line = "nbplatform.default.harness.dir=${nbplatform.default.netbeans.dest.dir}/harness \n";
            fos.write(line.getBytes());
            expectedLines.add(line);                        
        } finally {
            fos.close();
        }
        
        fos = new FileOutputStream(sFile);
        try {
            String line = "nbplatform.id.netbeans.dest.dir=/work/nball/nbbuild/netbeans \n";
            fos.write(line.getBytes());
            expectedLines.add(line);
            
            line = "nbplatform.id.netbeans.dest.dir=/work/nbide/netbeans \n";
            fos.write(line.getBytes());
            expectedLines.add(line);
            
            line = "nbplatform.default.netbeans.dest.dir=/work/nbide/netbeans \n";
            fos.write(line.getBytes());
            //lines.add(line); -- should be excluded
        } finally {
            fos.close();
        }
        String[] regexForSelection = new String[] {
            "^nbplatform[.](?![dD]efault).+[.](netbeans[.]dest[.]dir|label|harness[.]dir)=.+$"//NOI18N
        };
        
        Copy.appendSelectedLines(sFile, tFolder, regexForSelection);
        String line = null;
        List resultLines = new ArrayList();
        BufferedReader reader = new BufferedReader(new FileReader(tFile));
        try {
            while ((line = reader.readLine()) != null) {
                resultLines.add(line+"\n");
            }
        } finally {
            reader.close();
        }
        assertEquals(expectedLines,resultLines);
    }
    
    public void testCopy () throws Exception {
        copyTest ("path/X.txt");        
    }
    
    public void testDoesDeepCopy () throws Exception {
        copyTest ("path/dir/subdir/deepDir/X.txt");
    }
    
    public void testCopyAttributes () throws Exception {
        copyTest (true, "path/X.txt");
    }    

    public void testCopyFolderAttributes () throws Exception {
        copyTest (true, new String[]{"path/folder/","path/folder/f.txt"});
    }    
    
    public void testDoNotCopyEmptyDirs () throws Exception {
        copyTest("path/emptyDir/");
    }
    
    public void testDoNotCopyEmptyDirs2 () throws Exception {
        copyTest(new String[] {"path/emptyDir/", "path/emptyDir/emptyDir2/"});
    }
    
    public void testDoesCopyHiddenFiles () throws Exception {
        String[] res = {
            "root/Yes.txt", 
            "root/X.txt_hidden", 
        };
        LocalFileSystem fs = createLocalFileSystem (res);
        URL url = getClass().getResource("layer5.5.xml");
        assertNotNull("found sample layer", url);
        XMLFileSystem xfs = new XMLFileSystem(url);
        
        MultiFileSystem mfs = AutoUpgrade.createLayeredSystem(fs, xfs); 
        
        FileObject fo = mfs.findResource ("root");
        
        FileSystem original = FileUtil.createMemoryFileSystem();
        
        MultiFileSystem tgfs = new MultiFileSystem(new FileSystem[] { fs, original });
        FileObject tg = tgfs.getRoot().createFolder ("target");
        FileObject toBeHidden = FileUtil.createData(original.getRoot(), "target/X.txt");
        
        assertEquals ("One file is there", 1, tg.getChildren().length);
        assertEquals ("X.txt", tg.getChildren()[0].getNameExt());
        
        
        HashSet set = new HashSet ();
        set.add ("root/Yes.txt");
        set.add ("root/X.txt_hidden");
        Copy.copyDeep (fo, tg, set);
        
        assertEquals("After the copy there is still one file", 1, tg.getFileObject("root").getChildren().length);
        assertEquals ("but the file is Yes.txt, as X.txt is hidden by txt_hidden", "Yes.txt", tg.getFileObject("root").getChildren()[0].getNameExt());
    }
    
    private static void writeTo (FileSystem fs, String res, String content) throws java.io.IOException {
        FileObject fo = org.openide.filesystems.FileUtil.createData (fs.getRoot (), res);
        org.openide.filesystems.FileLock lock = fo.lock ();
        java.io.OutputStream os = fo.getOutputStream (lock);
        os.write (content.getBytes ());
        os.close ();
        lock.releaseLock ();
    }
    
    public LocalFileSystem createLocalFileSystem(String[] resources) throws IOException {
        File mountPoint = new File(getWorkDir(), "tmpfs"); 
        mountPoint.mkdir();
        
        for (int i = 0; i < resources.length; i++) {                        
            File f = new File (mountPoint,resources[i]);
            if (f.isDirectory() || resources[i].endsWith("/")) {
              FileUtil.createFolder(f);
            } else {
              FileUtil.createData(f);  
            }
        }
        
        LocalFileSystem lfs = new LocalFileSystem();
        try {
            lfs.setRootDirectory(mountPoint);
        } catch (Exception ex) {}
        
        return lfs;
    }

    private void copyTest(String... pathXtxt) throws IOException {    
        copyTest(false, pathXtxt);
    }
    private void copyTest(boolean testAttribs, String... allPath) throws IOException {
        String atribName = "attribName";
        String testPath = allPath[0];
        ArrayList<String> fileList = new ArrayList<String>();
        fileList.addAll(Arrays.asList(allPath));
        fileList.addAll(Arrays.asList(new java.lang.String[]{ 
        "path/Yes.txt", "path/No.txt", "path/Existing.txt"}));
        
        FileSystem fs = createLocalFileSystem(fileList.toArray(new String[fileList.size()]));

        
        FileObject path = fs.findResource("path");
        assertNotNull(path);
        FileObject tg = fs.getRoot().createFolder("target");
        assertNotNull(tg);
        FileObject existing = FileUtil.createData(tg, "path/Existing.txt");
        assertNotNull(existing);
        writeTo (fs, "target/path/Existing.txt", "existing-content");
        
        FileObject toCopyOne = fs.findResource (testPath);
        boolean isFolder = toCopyOne.isFolder();
        boolean isEmptyFolder = isFolder && !toCopyOne.getData(true).hasMoreElements();
        assertNotNull(toCopyOne);
        if (testAttribs) {
            toCopyOne.setAttribute (atribName, atribName);
        }
        
        HashSet set = new HashSet();
        for (String currentPath : allPath) {
            currentPath = currentPath.endsWith("/") ? currentPath.substring(0, currentPath.length()-1) : currentPath;
            set.add(currentPath);
        }

        set.add("path/Yes.txt");
        set.add("path/Existing.txt");
        org.netbeans.upgrade.Copy.copyDeep(path, tg, set);

        assertNotNull("file not copied: " + "path/Existing.txt", tg.getFileObject("path/Existing.txt"));        
        assertNotNull("file not copied: " + "path/Yes.txt", tg.getFileObject("path/Yes.txt"));
        assertNull("file not copied: " + "path/No.txt", tg.getFileObject("path/No.txt"));
        org.openide.filesystems.FileObject copiedOne = tg.getFileObject(testPath);

        FileObject copiedPath = tg.getFileObject("path");
        assertNotNull("file copied: " + "path", copiedPath);        
        assertEquals("file copied: " + testPath, isEmptyFolder, copiedOne == null);
        if (!isEmptyFolder) {
            String expected = testPath.endsWith("/") ? testPath.substring(0, testPath.length()-1) : testPath;
            assertEquals("file copied: " + testPath,  expected, org.openide.filesystems.FileUtil.getRelativePath(tg, copiedOne));
            if (testAttribs) {
                assertEquals ("attribute copied", atribName, copiedOne.getAttribute(atribName));                
            }            
        }
        
        
        byte[] arr = new byte[300];
        int len = existing.getInputStream ().read (arr);
        String content = new String (arr, 0, len);

        //testDoNotOverwriteFiles
        assertEquals ("The content is kept from project", content, "existing-content");
        
    }
 }
