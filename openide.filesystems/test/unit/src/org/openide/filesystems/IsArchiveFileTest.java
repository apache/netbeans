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

package org.openide.filesystems;


import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.junit.*;

/**
 *Test behavior of FileUtil.isArchiveFile
 *
 * @author Tomas Zezula
 */
public class IsArchiveFileTest extends NbTestCase {
    /**
     * filesystem containing created instances
     */
    private LocalFileSystem lfs;
    private FileObject directory;
    private FileObject brokenArchive;
    private FileObject archive;
    private FileObject file;
    private FileObject emptyFile;

    /**
     * Creates new test
     */
    public IsArchiveFileTest(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite(IsArchiveFileTest.class));
    }

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();                        
        lfs = new LocalFileSystem ();
        lfs.setRootDirectory(this.getWorkDir());
        Repository.getDefault().addFileSystem(lfs);
        FileObject root = lfs.getRoot();        
        directory = root.createFolder("dir");
        brokenArchive = root.createData ("brokenArchive.jar");
        archive = root.createData("archive.jar");
        FileLock lock = archive.lock();
        try {
            JarOutputStream out = new JarOutputStream (archive.getOutputStream(lock));
            try {
                out.putNextEntry(new ZipEntry("foo"));
                out.closeEntry();
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        file = root.createData ("file.txt");
        lock = file.lock ();
        try {
            OutputStream out = file.getOutputStream(lock);
            try {
                out.write ("Test file".getBytes());
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        emptyFile = root.createData("emptyFile.txt");
    }
    
    protected void tearDown() throws Exception {
        Repository.getDefault().removeFileSystem(lfs);
        super.tearDown();
    }
    

    public void testIsArchivFile () throws Exception {
        assertFalse (FileUtil.isArchiveFile(directory));
        assertFalse (FileUtil.isArchiveFile(brokenArchive));
        assertTrue (FileUtil.isArchiveFile(archive));
        assertFalse (FileUtil.isArchiveFile(file));
        assertFalse (FileUtil.isArchiveFile(emptyFile));
        
        assertFalse (FileUtil.isArchiveFile(new URL("jar:file:/foo.jar!/")));
        assertFalse (FileUtil.isArchiveFile(new URL("file:/foo/")));
        assertTrue (FileUtil.isArchiveFile(new URL("file:/foo.jar")));
    }

   
}
  
  
  
