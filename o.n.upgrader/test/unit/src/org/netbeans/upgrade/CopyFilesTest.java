/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.upgrade;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;

public class CopyFilesTest extends org.netbeans.junit.NbTestCase {

    public CopyFilesTest(String name) {
	super(name);
    }

    @Before
    @Override
    public void setUp() throws Exception {
	super.setUp();
        clearWorkDir();
    }

    @Test
    public void testCopyDeep() throws Exception {
	ArrayList<String> fileList = new ArrayList<String>();
	fileList.addAll(Arrays.asList(new java.lang.String[]{"source/foo/X.txt",
		    "source/foo/A.txt", "source/foo/B.txt", "source/foo/foo2/C.txt"}));

	FileSystem fs = createLocalFileSystem(fileList.toArray(new String[fileList.size()]));

	FileObject path = fs.findResource("source");
	assertNotNull(path);
	FileObject tg = fs.getRoot().createFolder("target");
	assertNotNull(tg);
	FileObject patterns = FileUtil.createData(fs.getRoot(), "source/foo/etc/patterns.import");
	assertNotNull(patterns);
	String pattern = "# ignore comment\n"
		+ "include foo/.*\n"
		+ "translate foo=>bar\n";
	writeTo(fs, "source/foo/etc/patterns.import", pattern);

	org.netbeans.upgrade.CopyFiles.copyDeep(FileUtil.toFile(path), FileUtil.toFile(tg), FileUtil.toFile(patterns));

	assertNotNull("file not copied: " + "foo/X.txt", tg.getFileObject("bar/X.txt"));
	assertNotNull("file not copied: " + "foo/A.txt", tg.getFileObject("bar/A.txt"));
	assertNotNull("file not copied: " + "foo/B.txt", tg.getFileObject("bar/B.txt"));
	assertNotNull("file not copied: " + "foo/foo2/C.txt", tg.getFileObject("bar/foo2/C.txt"));
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
}
