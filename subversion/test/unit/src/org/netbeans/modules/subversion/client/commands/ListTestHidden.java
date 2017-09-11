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
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author tomas
 */
public class ListTestHidden extends AbstractCommandTestCase {
    
    public ListTestHidden(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        try {
            super.setUp();
        } catch (Exception e) {
            stopSvnServer();
        }
        if(getName().equals("testListNullAuthor")) {
            setAnnonWriteAccess();
            runSvnServer();
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        if(getName().equals("testListNullAuthor")) {
            restoreAuthSettings();
        }
        super.tearDown();
    }    
    
    @Override
    protected String getRepoURLProtocol() {
        if(getName().equals("testListNullAuthor")) {        
            return "svn://localhost/";
        }
        return super.getRepoURLProtocol();
    }                

    public void testListWrongFile() throws Exception {                                
        // XXX add refclient
        SVNClientException e1 = null;
        try {
            getNbClient().getList(getRepoUrl().appendPath("arancha"), SVNRevision.HEAD, false);
        } catch (SVNClientException e) {
            e1 = e;
        }
        SVNClientException e2 = null;
        try {
            list(getRepoUrl().appendPath("arancha"));        
        } catch (SVNClientException e) {
            e2 = e;
        }
        assertNotNull(e1);
        assertNotNull(e2);
        assertTrue(SvnClientExceptionHandler.isWrongUrl(e1.getMessage()));
        assertTrue(SvnClientExceptionHandler.isWrongUrl(e2.getMessage()));
    }
    
    public void testListNoFile() throws Exception {                                
        ISVNClientAdapter c = getNbClient();
        ISVNDirEntry[] entries1 = c.getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, false);
                        
        assertEquals(0, entries1.length);
    }
    
    public void testListFiles() throws Exception {                        
        File file1 = createFile("file1");
        File file2 = createFile("file2");
        File file3 = createFile("file3");
                
        add(file1);                       
        add(file2);                       
        add(file3);                       
        commit(getWC());
                                
        ISVNDirEntry[] entries1 = getNbClient().getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, false);        
        assertEquals(3, entries1.length);
        ISVNDirEntry[] entries2 = getFullWorkingClient().getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, false);
        
        assertEntryArrays(entries1, entries2);
    }
    
    public void testListFilesRecursively() throws Exception {                        
        File folder = createFolder("file1");
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        File file3 = createFile(folder, "file3");
                
        add(folder);                       
        add(file1);                       
        add(file2);                       
        add(file3);                       
        commit(getWC());
                        
        ISVNDirEntry[] entries1 = getNbClient().getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, true);        
        assertEquals(4, entries1.length);
        ISVNDirEntry[] entries2 = getFullWorkingClient().getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, true);
        
        assertEntryArrays(entries1, entries2);
    }

//    XXX not idea how to push a null username through svnclientadapter
//    public void testListNullAuthor() throws Exception {
//        File file = createFile("file");
//
//        add(file);
//        commit(getWC());
//
//        ISVNClientAdapter c = getNbClient();
//        ISVNDirEntry[] entries = c.getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, false);
//
//        assertNull(entries[0].getLastCommitAuthor());
//
//    }
    
}
