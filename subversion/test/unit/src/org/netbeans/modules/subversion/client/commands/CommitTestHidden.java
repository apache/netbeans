/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author tomas
 */
public class CommitTestHidden extends AbstractCommandTestCase {
    
    public CommitTestHidden(String testName) throws Exception {
        super(testName);
    }

    public void testCommitFile() throws Exception {                
        testCommitFile("file");
    }

    public void testCommitFileWithAtSign() throws Exception {
        testCommitFile("@file");
        testCommitFile("fi@le");
        testCommitFile("file@");
    }

    public void testCommitFileInDir() throws Exception {
        testCommitFile("folder/file");
    }

    public void testCommitFileWithAtSignInDir() throws Exception {
        testCommitFile("folder/@file");
        testCommitFile("folder/fi@le");
        testCommitFile("folder/file@");
    }

    private void testCommitFile(String path) throws Exception {
        createAndCommitParentFolders(path);
        File file = createFile(path);

        add(file);
        assertStatus(SVNStatusKind.ADDED, file);

        SVNRevision revisionBefore = (SVNRevision.Number) getRevision(getRepoUrl());

        ISVNClientAdapter client = getNbClient();

        long r = client.commit(new File[] {file}, "commit", true);

        SVNRevision revisionAfter = (SVNRevision.Number) getRevision(getRepoUrl());

        assertTrue(file.exists());
        assertStatus(SVNStatusKind.NORMAL, file);
        assertNotSame(revisionBefore, revisionAfter);
        assertEquals(((SVNRevision.Number)revisionAfter).getNumber(), r);
        assertNotifiedFiles(file);
    }
    
    public void testCommitFolder() throws Exception {                
        testCommitFolder("folder", "file");
    }

    public void testCommitFolderWithAtSign() throws Exception {
        testCommitFolder("@folder", "file1");
        testCommitFolder("fol@der", "file2");
        testCommitFolder("folder@", "file3");
    }

    private void testCommitFolder(String folderName, String fileName) throws Exception {
        File folder = createFolder(folderName);
        File file = createFile(folder, fileName);
                
        add(folder);               
        assertStatus(SVNStatusKind.ADDED, file);
        assertStatus(SVNStatusKind.ADDED, folder);

        SVNRevision revisionBefore = (SVNRevision.Number) getRevision(getRepoUrl());
        
        ISVNClientAdapter client = getNbClient();        
        long r = client.commit(new File[] {folder}, "commit", true);
        
        SVNRevision revisionAfter = (SVNRevision.Number) getRevision(getRepoUrl());
        
        assertTrue(file.exists());        
        assertStatus(SVNStatusKind.NORMAL, file);                
        assertStatus(SVNStatusKind.NORMAL, folder);                
        assertNotSame(revisionBefore, revisionAfter);
        assertEquals(((SVNRevision.Number)revisionAfter).getNumber(), r);
        assertNotifiedFiles(new File[] {file, folder});
        
    }
    
    public void testCommitFolderNonRecursively() throws Exception {                
        File folder = createFolder("folder");
        File file = createFile(folder, "file");
                
        add(folder);               
        assertStatus(SVNStatusKind.ADDED, file);
        assertStatus(SVNStatusKind.ADDED, folder);

        SVNRevision revisionBefore = (SVNRevision.Number) getRevision(getRepoUrl());
        
        ISVNClientAdapter client = getNbClient();        
        long r = client.commit(new File[] {folder}, "commit", false);
        
        SVNRevision revisionAfter = (SVNRevision.Number) getRevision(getRepoUrl());
        
        assertTrue(file.exists());        
        assertStatus(SVNStatusKind.ADDED, file);                
        assertStatus(SVNStatusKind.NORMAL, folder);                
        assertNotSame(revisionBefore, revisionAfter);
        assertEquals(((SVNRevision.Number)revisionAfter).getNumber(), r);
        assertNotifiedFiles(new File[] {folder});
        
    }    

}
