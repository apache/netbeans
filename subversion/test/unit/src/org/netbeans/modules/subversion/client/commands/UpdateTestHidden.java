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
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class UpdateTestHidden extends AbstractCommandTestCase {
    
    public UpdateTestHidden(String testName) throws Exception {
        super(testName);
    }
           
    protected void setUp() throws Exception {
        importWC = true;
        if(getName().startsWith("testUpdate")) {
            importWC = false;            
        } 
        super.setUp();
//        if(getName().startsWith("testUpdate")) {
//            cleanUpRepo(new String[] {CI_FOLDER});
//        }        
    }
    
    public void testUpdateFile() throws Exception {
        testUpdateFile("wc1", "wc2", "file");
    }

    public void testUpdateFileWithAtSign1() throws Exception {
        testUpdateFile("wc11", "wc21", "@file");
        testUpdateFile("wc12", "wc22", "fi@le");
        testUpdateFile("wc13", "wc23", "file@");
    }

    private void testUpdateFile(String folder1Name,
                                String folder2Name,
                                String fileName) throws Exception {
        File wc1 = createFolder(folder1Name);
        File file1 = createFile(wc1, fileName);
        write(file1, 1);
        importFile(wc1);
        assertStatus(SVNStatusKind.NORMAL, wc1);
        assertStatus(SVNStatusKind.NORMAL, file1);
                        
        File wc2 = createFolder(folder2Name);
        File file2 = new File(wc2, fileName);
        
        SVNUrl url = getTestUrl().appendPath(wc1.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, wc2, SVNRevision.HEAD, true);
        assertStatus(SVNStatusKind.NORMAL, file2);
        assertContents(file2, 1);
        
        SVNRevision revisionBefore = (SVNRevision.Number) getRevision(getRepoUrl());
        
        write(file2, 2);
        assertStatus(SVNStatusKind.MODIFIED, file2);
        commit(file2);
        assertStatus(SVNStatusKind.NORMAL, file2);
                        
        clearNotifiedFiles();     
        long r = c.update(file1, SVNRevision.HEAD, false);
        
        SVNRevision revisionAfter = (SVNRevision.Number) getRevision(getRepoUrl());
        
        assertNotSame(revisionBefore, revisionAfter);
        assertEquals(((SVNRevision.Number)revisionAfter).getNumber(), r);
        assertStatus(SVNStatusKind.NORMAL, file1);
        assertContents(file1, 2);     
        assertNotifiedFiles(new File[] {file1});
    }    
    
    public void testUpdateFilePrevRevision() throws Exception {
        testUpdateFilePrevRevision("wc1", "wc2", "file");
    }

    public void testUpdateFileWithAtSignPrevRevision() throws Exception {
        testUpdateFilePrevRevision("wc11", "wc21", "@file");
        testUpdateFilePrevRevision("wc12", "wc22", "fi@le");
        testUpdateFilePrevRevision("wc13", "wc23", "file@");
    }

    private void testUpdateFilePrevRevision(String folder1Name,
                                            String folder2Name,
                                            String fileName) throws Exception {
        File wc1 = createFolder(folder1Name);
        File file1 = createFile(wc1, fileName);
        write(file1, 1);
        importFile(wc1);
                        
        assertStatus(SVNStatusKind.NORMAL, wc1);
        assertStatus(SVNStatusKind.NORMAL, file1);
                                        
        File wc2 = createFolder(folder2Name);
        File file2 = new File(wc2, fileName);
        
        SVNUrl url = getTestUrl().appendPath(wc1.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, wc2, SVNRevision.HEAD, true);
        assertStatus(SVNStatusKind.NORMAL, file2);
        assertContents(file2, 1);
                
        SVNRevision prevRev = (SVNRevision.Number) getRevision(getRepoUrl());
        
        write(file2, 2);
        assertStatus(SVNStatusKind.MODIFIED, file2);
        commit(file2);
        assertStatus(SVNStatusKind.NORMAL, file2);

        clearNotifiedFiles();             
        long r = c.update(file1, prevRev, false);
        
        SVNRevision revisionAfter = (SVNRevision.Number) getRevision(getRepoUrl());
                
        assertNotSame(r, ((SVNRevision.Number)revisionAfter).getNumber());
        assertEquals(((SVNRevision.Number)prevRev).getNumber(), r);
        assertStatus(SVNStatusKind.NORMAL, file1);
        assertContents(file1, 1);        
        assertNotifiedFiles(new File[] { /*file1*/ }); // XXX no output from cli!
    }    

    public void testUpdateFolderRecusivelly() throws Exception {                                                                        
        File wc1 = createFolder("wc1");
        File fileA1 = createFile(wc1, "fileA");
        File fileB1 = createFile(wc1, "fileB");
        File folder1 = createFolder(wc1, "folder");
        File fileC1 = createFile(folder1, "fileC");
        
        write(fileA1, 1);
        write(fileB1, 1);
        write(fileC1, 1);
        importFile(wc1);
        assertStatus(SVNStatusKind.NORMAL, wc1);
        assertStatus(SVNStatusKind.NORMAL, fileA1);
        assertStatus(SVNStatusKind.NORMAL, fileB1);
        assertStatus(SVNStatusKind.NORMAL, fileC1);
                        
        File wc2 = createFolder("wc2");      
        File fileA2 = new File(wc2, "fileA");
        File fileB2 = new File(wc2, "fileB");
        File fileC2 = new File(new File(wc2, folder1.getName()), "fileC");
        
        SVNUrl url = getTestUrl().appendPath(wc1.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, wc2, SVNRevision.HEAD, true);
        assertStatus(SVNStatusKind.NORMAL, fileA2);
        assertStatus(SVNStatusKind.NORMAL, fileB2);
        assertStatus(SVNStatusKind.NORMAL, fileC2);
        assertContents(fileA2, 1);
        assertContents(fileB2, 1);
        assertContents(fileC2, 1);
        
        SVNRevision revisionBefore = (SVNRevision.Number) getRevision(getRepoUrl());
        
        write(fileA2, 2);
        write(fileB2, 2);
        write(fileC2, 2);
        assertStatus(SVNStatusKind.MODIFIED, fileA2);
        assertStatus(SVNStatusKind.MODIFIED, fileB2);
        assertStatus(SVNStatusKind.MODIFIED, fileC2);
        commit(wc2);
        assertStatus(SVNStatusKind.NORMAL, fileA2);
        assertStatus(SVNStatusKind.NORMAL, fileB2);
        assertStatus(SVNStatusKind.NORMAL, fileC2);
        
        clearNotifiedFiles();             
        long r = c.update(wc1, SVNRevision.HEAD, true);        
        SVNRevision revisionAfter = (SVNRevision.Number) getRevision(getRepoUrl());
        
        assertNotSame(revisionBefore, revisionAfter);
        assertEquals(((SVNRevision.Number)revisionAfter).getNumber(), r);
        assertStatus(SVNStatusKind.NORMAL, fileA1);
        assertStatus(SVNStatusKind.NORMAL, fileB1);
        assertStatus(SVNStatusKind.NORMAL, fileC1);
        assertContents(fileA1, 2);        
        assertContents(fileB1, 2);        
        assertContents(fileC1, 2);        
        assertNotifiedFiles(new File[] {fileA1, fileB1, fileC1});
    }    
    
    public void testUpdateFolderNonRecusivelly() throws Exception {                                                                        
        // XXX setproperty on folder
        
        File wc1 = createFolder("wc1");
        File fileA1 = createFile(wc1, "fileA");
        File fileB1 = createFile(wc1, "fileB");
        File folder1 = createFolder(wc1, "folder");
        File fileC1 = createFile(folder1, "fileC");
        
        write(fileA1, 1);
        write(fileB1, 1);
        write(fileC1, 1);
        importFile(wc1);
        assertStatus(SVNStatusKind.NORMAL, wc1);
        assertStatus(SVNStatusKind.NORMAL, fileA1);
        assertStatus(SVNStatusKind.NORMAL, fileB1);
        assertStatus(SVNStatusKind.NORMAL, fileC1);
                        
        File wc2 = createFolder("wc2");      
        File fileA2 = new File(wc2, "fileA");
        File fileB2 = new File(wc2, "fileB");
        File fileC2 = new File(new File(wc2, folder1.getName()), "fileC");
        
        SVNUrl url = getTestUrl().appendPath(wc1.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, wc2, SVNRevision.HEAD, true);
        assertStatus(SVNStatusKind.NORMAL, fileA2);
        assertStatus(SVNStatusKind.NORMAL, fileB2);
        assertStatus(SVNStatusKind.NORMAL, fileC2);
        assertContents(fileA2, 1);
        assertContents(fileB2, 1);
        assertContents(fileC2, 1);
        
        SVNRevision revisionBefore = (SVNRevision.Number) getRevision(getRepoUrl());
        
        write(fileA2, 2);
        write(fileB2, 2);
        write(fileC2, 2);
        assertStatus(SVNStatusKind.MODIFIED, fileA2);
        assertStatus(SVNStatusKind.MODIFIED, fileB2);
        assertStatus(SVNStatusKind.MODIFIED, fileC2);
        commit(wc2);
        assertStatus(SVNStatusKind.NORMAL, fileA2);
        assertStatus(SVNStatusKind.NORMAL, fileB2);
        assertStatus(SVNStatusKind.NORMAL, fileC2);
        
        clearNotifiedFiles();             
        long r = c.update(wc1, SVNRevision.HEAD, false);        
        SVNRevision revisionAfter = (SVNRevision.Number) getRevision(getRepoUrl());

        assertNotSame(revisionBefore, revisionAfter);
        assertEquals(((SVNRevision.Number)revisionAfter).getNumber(), r);
        assertStatus(SVNStatusKind.NORMAL, fileA1);
        assertStatus(SVNStatusKind.NORMAL, fileB1);
        assertStatus(SVNStatusKind.NORMAL, fileC1);
        assertContents(fileA1, 2);        
        assertContents(fileB1, 2);        
        assertContents(fileC1, 1);        
        assertNotifiedFiles(new File[] {fileA1, fileB1});
    }    
    
}
