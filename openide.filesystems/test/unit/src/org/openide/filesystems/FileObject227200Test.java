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
package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jhavlin
 */
public class FileObject227200Test extends NbTestCase {

    private LocalFileSystem lfs;
    private static final Logger LOG
            = Logger.getLogger(FileObject227200Test.class.getName());

    public FileObject227200Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = (LocalFileSystem) TestUtilHid.createLocalFileSystem(getName(),
                new String[]{getName()});
    }

    @Override
    protected void tearDown() throws Exception {
        for (FileObject fo : lfs.getRoot().getChildren()) {
            fo.delete();
        }
    }

    public void test227200() throws FileStateInvalidException, IOException,
            InterruptedException {

        final FileObject rootFO = lfs.getRoot();
        final File rootF = FileUtil.toFile(rootFO);
        System.out.println(rootF.getAbsolutePath());

        assertNotNull("Root File shouldn't be null", rootF);
        assertNotNull("Root FileObject shouldn't be null", rootFO);

        // Create a folder (using java.io.File API)
        final String folderName = "childFolder";
        final File subDir = new File(rootF, folderName);
        boolean creationResult = subDir.mkdir();
        assertTrue(creationResult);

        rootFO.refresh();
        // Create FileObject for existing folder.
        FileObject fo = rootFO.getFileObject(folderName);
        assertNotNull(fo);

        // Remove the backing folder.
        subDir.delete();

        // Get FileObject for the removed folder.
        fo = rootFO.getFileObject(folderName);
        assertNotNull(fo);

        if (!fo.isValid()) {
            LOG.log(Level.INFO, "FO {0} is invalid, it's OK.", folderName);
        } else if (!fo.isFolder()) {
            LOG.log(Level.INFO, "FO {0} is data, try refresh.", folderName);
            File createAgain = new File(rootF, fo.getNameExt());
            assertFalse(createAgain.exists());
            assertTrue(createAgain.mkdir());
            rootFO.refresh();
            FileObject refreshed = rootFO.getFileObject(fo.getNameExt());
            assertTrue("New folder should be valid.", refreshed.isValid());
            assertTrue("New folder's java.io.File should be a directory",
                    FileUtil.toFile(refreshed).isDirectory());
            assertTrue("New folder's FileObject should be a folder",
                    refreshed.isFolder());
        } else {
            LOG.log(Level.INFO, "FO {0} is valid folder, not covered by this"
                    + " test.", folderName);
        }
    }
}
