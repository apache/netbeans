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
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author tomas
 */
public class SwitchToTestHidden extends AbstractCommandTestCase {
    
    public SwitchToTestHidden(String testName) throws Exception {
        super(testName);
    }
    
    public void testSwitchToFile() throws Exception {                                        
        File file = createFile("file");
        add(file);
        commit(file);
                
        File filecopy = createFile("filecopy");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), getFileUrl(filecopy), "copy", SVNRevision.HEAD);

        assertCopy(getFileUrl(filecopy));
        assertInfo(file, getFileUrl(file));
        
        c.switchToUrl(file, getFileUrl(filecopy), SVNRevision.HEAD, false);
        
        assertInfo(file, getFileUrl(filecopy));         
        assertNotifiedFiles();// XXX empty also in svnCA - why?! - no output from cli
    }                 
    
    public void testSwitchToFilePrevRev() throws Exception {                                        
        File file = createFile("file");
        add(file);
        write(file, 1);
        commit(file);
        
        File filecopy = createFile("filecopy");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), getFileUrl(filecopy), "copy", SVNRevision.HEAD);

        assertCopy(getFileUrl(filecopy));
        assertInfo(file, getFileUrl(file));
        
        // switch to copy
        c.switchToUrl(file, getFileUrl(filecopy), SVNRevision.HEAD, false);        
        assertInfo(file, getFileUrl(filecopy));        
        
        SVNRevision prevrev = getRevision(file);
        
        // change copy
        write(file, 2);
        commit(file);
        
        // switch to trunk
        c.switchToUrl(file, getFileUrl(file), SVNRevision.HEAD, false);        
        assertInfo(file, getFileUrl(file));        
        
        // switch to copies prev revision
        c.switchToUrl(file, getFileUrl(filecopy), prevrev, false);    
        
        // test
        assertInfo(file, getFileUrl(filecopy));        
        assertContents(file, 1);
        SVNRevision rev = getRevision(file);
        assertEquals(((SVNRevision.Number)prevrev).getNumber(), ((SVNRevision.Number)rev).getNumber());
        assertNotifiedFiles(file);        
    }                 

    public void testSwitchToFolderNonRec() throws Exception {                                        
        File folder = createFolder("folder");
        File file = createFile(folder, "file");
        File folder1 = createFolder(folder, "folder1");
        File file1 = createFile(folder1, "file1");
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(folder);
                
        File foldercopy = createFolder("foldercopy");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy), "copy", SVNRevision.HEAD);

        assertCopy(getFileUrl(foldercopy));
        assertInfo(folder, getFileUrl(folder));
        assertInfo(file, getFileUrl(folder).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(folder).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(folder).appendPath(folder1.getName()).appendPath(file1.getName()));
        
        c.switchToUrl(folder, getFileUrl(foldercopy), SVNRevision.HEAD, false);
        
        assertInfo(folder, getFileUrl(foldercopy));
        assertInfo(file, getFileUrl(foldercopy).appendPath(file.getName()));        
        assertInfo(folder1, getFileUrl(folder).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(folder).appendPath(folder1.getName()).appendPath(file1.getName()));
        assertNotifiedFiles(new File[] {});  // XXX empty also in svnCA - why?! - no output from cli      
    }

    public void testSwitchToFolderRec() throws Exception {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");
        File folder1 = createFolder(folder, "folder1");
        File file1 = createFile(folder1, "file1");
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(folder);

        File foldercopy = createFolder("foldercopy");

        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy), "copy", SVNRevision.HEAD);

        assertCopy(getFileUrl(foldercopy));
        assertInfo(folder, getFileUrl(folder));
        assertInfo(file, getFileUrl(folder).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(folder).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(folder).appendPath(folder1.getName()).appendPath(file1.getName()));

        c.switchToUrl(folder, getFileUrl(foldercopy), SVNRevision.HEAD, true);

        assertInfo(folder, getFileUrl(foldercopy));
        assertInfo(file, getFileUrl(foldercopy).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(foldercopy).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(foldercopy).appendPath(folder1.getName()).appendPath(file1.getName()));
        assertNotifiedFiles(new File[] {});       // XXX empty also in svnCA - why?! - no output from cli
    }

    public void testSwitchToFolderWithAtSignRec() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        File folder = createFolder("fol@der");
        File file = createFile(folder, "file");
        File folder1 = createFolder(folder, "folder1");
        File file1 = createFile(folder1, "file1");
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(folder);

        File foldercopy = createFolder("folder@copy");

        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy), "copy", SVNRevision.HEAD);

        assertCopy(getFileUrl(foldercopy));
        assertInfo(folder, getFileUrl(folder));
        assertInfo(file, getFileUrl(folder).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(folder).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(folder).appendPath(folder1.getName()).appendPath(file1.getName()));

        c.switchToUrl(folder, getFileUrl(foldercopy), SVNRevision.HEAD, true);

        assertInfo(folder, getFileUrl(foldercopy));
        assertInfo(file, getFileUrl(foldercopy).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(foldercopy).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(foldercopy).appendPath(folder1.getName()).appendPath(file1.getName()));
        assertNotifiedFiles(new File[] {});       // XXX empty also in svnCA - why?! - no output from cli
    }
        
}
