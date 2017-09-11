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
import java.io.FileInputStream;
import java.io.InputStream;
import org.netbeans.modules.versioning.util.FileUtils;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author tomas
 */
public class CatTestHidden extends AbstractCommandTestCase {
    
    public CatTestHidden(String testName) throws Exception {
        super(testName);
    }
             
    public void testCatFile() throws Exception {                                                
        testCatFile("file");
    }                   

    public void testCatFileWithAtSign() throws Exception {
        testCatFile("@file");
        testCatFile("fi@le");
        testCatFile("file@");
    }

    public void testCatFileInDir() throws Exception {
        testCatFile("folder/file");
    }

    public void testCatFileInDirWithAtSign() throws Exception {
        testCatFile("folder/@file");
        testCatFile("folder/fi@le");
        testCatFile("folder/file@");
    }

    private void testCatFile(String path) throws Exception {
        createAndCommitParentFolders(path);
        File file = createFile(path);
        write(file, 1);
        add(file);
        commit(file);

        InputStream is1 = getNbClient().getContent(file, SVNRevision.HEAD);
        InputStream is2 = new FileInputStream(file);

        assertInputStreams(is2, is1);
    }
    
    public void testCatFilePrevRev() throws Exception {
        testCatFilePrevRev("file");
    }

    public void testCatFileWithAtSignPrevRev() throws Exception {
        testCatFilePrevRev("@file");
        testCatFilePrevRev("fi@le");
        testCatFilePrevRev("file@");
    }

    private void testCatFilePrevRev(String path) throws Exception {
        createAndCommitParentFolders(path);
        File file = createFile(path);
        write(file, 1);
        add(file);
        commit(file);

        File prevRevisionCopy = new File(file.getParentFile(), "prevRevisionCopy");
        FileUtils.copyFile(file, prevRevisionCopy);

        ISVNClientAdapter c = getNbClient();        
        SVNRevision prevrev = getRevision(file);
        write(file, 2);        
        commit(file);
        
        InputStream is1 = c.getContent(file, prevrev);
        InputStream is2 = new FileInputStream(prevRevisionCopy);
        
        assertInputStreams(is2, is1);
    }               

    public void testCatURL() throws Exception {
        testCatURL("file");
    }

    public void testCatURLWithAtSign() throws Exception {
        testCatURL("@file");
        testCatURL("fi@le");
        testCatURL("file@");
    }

    public void testCatURLInDir() throws Exception {
        testCatURL("folder/file");
    }
    
    public void testCatURLInDirWithAtSign() throws Exception {
        testCatURL("folder/@file");
        testCatURL("folder/fi@le");
        testCatURL("folder/file@");
    }
    
    private void testCatURL(String path) throws Exception {
        createAndCommitParentFolders(path);
        File file = createFile(path);
        write(file, 1);
        add(file);
        commit(file);
        
        ISVNClientAdapter c = getNbClient();        
        InputStream is1 = c.getContent(getFileUrl(file), SVNRevision.HEAD);
        InputStream is2 = new FileInputStream(file);
        
        assertInputStreams(is2, is1);
    }               

    public void testCatURLPrevRev() throws Exception {
        testCatURLPrevRev("file");
    }

    public void testCatURLWithAtSignPrevRev() throws Exception {
        testCatURLPrevRev("@file");
        testCatURLPrevRev("fi@le");
        testCatURLPrevRev("file@");
    }

    private void testCatURLPrevRev(String path) throws Exception {
        File file = createFile(path);
        write(file, 1);
        add(file);
        commit(file);

        File prevRevisionCopy = new File(file.getParentFile(), "prevRevisionCopy");
        FileUtils.copyFile(file, prevRevisionCopy);

        ISVNClientAdapter c = getNbClient();        
        SVNRevision prevrev = getRevision(file);
        write(file, 2);        
        commit(file);
        
        InputStream is1 = c.getContent(getFileUrl(file), prevrev);
        InputStream is2 = new FileInputStream(prevRevisionCopy);
        
        assertInputStreams(is2, is1);
    }               
    
}
