/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author tomas
 */
public class MergeTestHidden extends AbstractCommandTestCase {
    
    public MergeTestHidden(String testName) throws Exception {
        super(testName);
    }
    
    public void testMergeBack() throws Exception {                                        
        File file = createFile("file");
        add(file);
        write(file, "1");
        commit(file);
        assertInfo(file, getFileUrl(file));

        SVNRevision r1 = getRevision(file);
        write(file, "1\n2");
        commit(file);        
        assertEquals("1\n2", read(file));
        SVNRevision r2 = getRevision(file);
        
        ISVNClientAdapter c = getNbClient();
        // used to fail with svnkit: http://issues.tmatesoft.com/issue/SVNKIT-229
        c.merge(getFileUrl(file), r2, getFileUrl(file), r1, file, false, false);
        assertTrue(file.exists());
        assertEquals("1", read(file));
        assertNotifiedFiles(new File[] {file});        
    }
    
    public void testMergeFolderNonRec() throws Exception {                                        
        // init wc
        File folder = createFolder("folder");
        add(folder);
        commit(folder);
        assertInfo(folder, getFileUrl(folder));
        
        // init copy from wc
        File foldercopy = new File(getWC(), "foldercopy");
                
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy), "copy", SVNRevision.HEAD);
        // switch to copy
        c.switchToUrl(folder, getFileUrl(foldercopy), SVNRevision.HEAD, true);

        assertCopy(getFileUrl(foldercopy));
        assertInfo(folder, getFileUrl(foldercopy));
        
        // add new files to copy
        File folder1 = createFolder(folder, "folder1");
        File file = createFile(folder, "file");
        File file1 = createFile(folder1, "file");
        add(file);
        add(folder1);
        add(file1);
        commit(folder);
        assertTrue(file.exists());
        
        // switch back to wc
        c.switchToUrl(folder, getFileUrl(folder), SVNRevision.HEAD, true);
        assertCopy(getFileUrl(folder));
        assertInfo(folder, getFileUrl(folder));        
        assertFalse(file.exists());
        assertFalse(file1.exists());
        assertFalse(folder1.exists());
        
        // merge wc with copy
        ISVNLogMessage[] log = getCompleteLog(getFileUrl(folder));
        c.merge(getFileUrl(foldercopy), log[0].getRevision(), getFileUrl(foldercopy), SVNRevision.HEAD, folder, false, false);
        assertTrue(file.exists());
        assertFalse(file1.exists());
        assertFalse(folder1.exists());
        
        assertNotifiedFiles(new File[] {file, folder1});
    }
    
    public void testMergeFolderFrom2Urls() throws Exception {                                        
        File folder = createFolder("folder");
        add(folder);
        commit(folder);
        assertInfo(folder, getFileUrl(folder));
        
        File foldercopy1 = new File(getWC(), "foldercopy1");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy1), "copy", SVNRevision.HEAD);
        c.switchToUrl(folder, getFileUrl(foldercopy1), SVNRevision.HEAD, true);

        assertCopy(getFileUrl(foldercopy1));
        assertInfo(folder, getFileUrl(foldercopy1));
        
        File file1 = createFile(folder, "file1");
        add(file1);
        commit(folder);
        assertTrue(file1.exists());

        File foldercopy2 = new File(getWC(), "foldercopy2");
        
        c = getNbClient();
        c.copy(getFileUrl(foldercopy1), getFileUrl(foldercopy2), "copy", SVNRevision.HEAD);
        c.switchToUrl(folder, getFileUrl(foldercopy2), SVNRevision.HEAD, true);

        assertCopy(getFileUrl(foldercopy2));
        assertInfo(folder, getFileUrl(foldercopy2));
        
        File file2 = createFile(folder, "file2");
        add(file2);
        commit(folder);
        assertTrue(file2.exists());
        
        c.switchToUrl(folder, getFileUrl(folder), SVNRevision.HEAD, true);
        assertCopy(getFileUrl(folder));
        assertInfo(folder, getFileUrl(folder));                
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        
        ISVNLogMessage[] log = getCompleteLog(getFileUrl(foldercopy1));
        c.merge(getFileUrl(foldercopy1), log[0].getRevision(), getFileUrl(foldercopy2), SVNRevision.HEAD, folder, true, true);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertNotifiedFiles(new File[] {file1, file2});        
    }
    
    public void testFolderMergeRec() throws Exception {                                        
        File folder = createFolder("folder");
        add(folder);
        commit(folder);
        assertInfo(folder, getFileUrl(folder));
        
        File foldercopy = new File(getWC(), "foldercopy");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy), "copy", SVNRevision.HEAD);
        c.switchToUrl(folder, getFileUrl(foldercopy), SVNRevision.HEAD, true);

        assertCopy(getFileUrl(foldercopy));
        assertInfo(folder, getFileUrl(foldercopy));
        
        File folder1 = createFolder(folder, "folder1");
        File file = createFile(folder, "file");
        File file1 = createFile(folder1, "file");
        add(file);
        add(folder1);
        add(file1);
        commit(folder);
        assertTrue(file.exists());
        
        c.switchToUrl(folder, getFileUrl(folder), SVNRevision.HEAD, true);
        assertCopy(getFileUrl(folder));
        assertInfo(folder, getFileUrl(folder));        
        assertFalse(file.exists());
        assertFalse(file1.exists());
        assertFalse(folder1.exists());
        
        ISVNLogMessage[] log = getCompleteLog(getFileUrl(folder));
        c.merge(getFileUrl(foldercopy), log[0].getRevision(), getFileUrl(foldercopy), SVNRevision.HEAD, folder, false, true);
        assertTrue(file.exists());
        assertTrue(file1.exists());
        assertTrue(folder1.exists());
        assertNotifiedFiles(new File[] {file, file1, folder1});        
    }
    
}
