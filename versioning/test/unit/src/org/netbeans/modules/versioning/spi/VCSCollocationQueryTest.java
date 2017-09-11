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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.spi;

import java.io.IOException;
import org.openide.filesystems.FileStateInvalidException;

import java.io.File;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.spi.testvcs.TestVCS;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSCollocationQuery;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Versioning SPI unit tests of VCSVisibilityQuery.
 * 
 * @author Tomas Stupka
 */
public class VCSCollocationQueryTest extends NbTestCase {
    

    public VCSCollocationQueryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
        File userdir = new File(getWorkDir() + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        super.setUp();
    }

    public void testFindRootExisting() throws FileStateInvalidException, IOException, Exception {
        File folder = new File(getWorkDir(), TestVCS.VERSIONED_FOLDER_SUFFIX);
        folder.mkdirs();
        File file = new File(folder, "somefile");
        file.createNewFile();
        
        assertRoot(folder, file);
    }
    
    public void testFindRootNotExisting() throws FileStateInvalidException, IOException, Exception {
        File folder = new File(getWorkDir(), TestVCS.VERSIONED_FOLDER_SUFFIX);
        folder.mkdirs();
        File file = new File(folder, "somefile");
        assertRoot(folder, file);
    }
    
    public void testFindRootBogusFile() throws FileStateInvalidException, IOException, Exception {
        assertNull(CollocationQuery.findRoot(FileUtil.normalizeFile(new File("/a/b/c"))));
    }
    
    private void assertRoot(File root, File file) {
        assertEquals(root, CollocationQuery.findRoot(file));
    }
    
    public void testAreCollocatedExisting() throws FileStateInvalidException, IOException, Exception {
        File folder = new File(getWorkDir(), TestVCS.VERSIONED_FOLDER_SUFFIX);
        folder.mkdirs();

        File file1 = new File(folder, "file2" + TestVCSCollocationQuery.COLLOCATED_FILENAME_SUFFIX);
        file1.createNewFile();
        File file2 = new File(folder, "file1" + TestVCSCollocationQuery.COLLOCATED_FILENAME_SUFFIX);
        file2.createNewFile();
        
        assertCollocated(true, file1, file2);
    }
    
    public void testAreCollocatedNotExisting() throws FileStateInvalidException, IOException, Exception {
        File folder = new File(getWorkDir(), TestVCS.VERSIONED_FOLDER_SUFFIX);
        folder.mkdirs();

        File file1 = new File(folder, "file2" + TestVCSCollocationQuery.COLLOCATED_FILENAME_SUFFIX);
        file1.createNewFile();
        File file2 = new File(folder, "file1" + TestVCSCollocationQuery.COLLOCATED_FILENAME_SUFFIX);
        file2.createNewFile();
        
        assertCollocated(true, file1, file2);
    }
    
    
    public void testNotCollocatedExisting() throws FileStateInvalidException, IOException, Exception {
        File folder = new File(getWorkDir(), TestVCS.VERSIONED_FOLDER_SUFFIX);
        folder.mkdirs();

        File file1 = new File(folder, "file1");
        file1.createNewFile();
        File file2 = new File(folder, "file2");
        file2.createNewFile();
        
        assertCollocated(false, file1, file2);
    }
    
    public void testNotCollocatedNotExisting() throws FileStateInvalidException, IOException, Exception {
        File folder = new File(getWorkDir(), TestVCS.VERSIONED_FOLDER_SUFFIX);
        folder.mkdirs();

        File file1 = new File(folder, "file1");
        file1.createNewFile();
        File file2 = new File(folder, "file2");
        file2.createNewFile();
        
        assertCollocated(false, file1, file2);
    }
    
    void assertCollocated(boolean expected, File file1, File file2) {
        if(expected) {
            assertTrue(CollocationQuery.areCollocated(file1, file2));
        } else {
            assertFalse(CollocationQuery.areCollocated(file1, file2));
        }
    }
    
    
}
