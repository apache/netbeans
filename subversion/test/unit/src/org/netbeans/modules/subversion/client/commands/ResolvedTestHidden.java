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
public class ResolvedTestHidden extends AbstractCommandTestCase {
    
    public ResolvedTestHidden(String testName) throws Exception {
        super(testName);
    }
           
    protected void setUp() throws Exception {
        importWC = true;
        if(getName().startsWith("testResolved")) {
            importWC = false;            
        } 
        super.setUp();      
    }
    
    public void testResolved() throws Exception {
        testResolved("wc1", "wc2", "file");
    }

    public void testResolvedFileWithAtSign1() throws Exception {
        testResolved("wc1", "wc2", "@file");
    }

    public void testResolvedFileWithAtSign2() throws Exception {
        testResolved("wc1", "wc2", "fi@le");
    }

    public void testResolvedFileWithAtSign3() throws Exception {
        testResolved("wc1", "wc2", "file@");
    }

    private void testResolved(String folder1Name,
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
        
        write(file2, 2);        
        assertStatus(SVNStatusKind.MODIFIED, file2);
        commit(file2);
        assertStatus(SVNStatusKind.NORMAL, file2);
                        
        write(file1, 3);
        
        c.update(file1, SVNRevision.HEAD, false);
        
        assertStatus(SVNStatusKind.CONFLICTED, file1);
        
        write(file1, 2);
        clearNotifiedFiles();
        c.resolved(file1);
        
        assertStatus(SVNStatusKind.NORMAL, file1);
        
        if (isSvnkit()) {
            // svnkit does not notify about resolved files
            assertNotifiedFiles();
        } else {
            assertNotifiedFiles(file1);
        }
    }        
}
