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

package org.netbeans.modules.bugtracking.api;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.RepositoryQueryImplementation;
import org.netbeans.modules.bugtracking.BugtrackingOwnerSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class RepositoryQueryTest extends NbTestCase {

    public RepositoryQueryTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
        MockLookup.setLayersAndInstances();
        APITestConnector.init();
    }

    @Override
    protected void tearDown() throws Exception {   
    }

    public void testDefaultImpl() throws IOException {
        Repository repo = getRepo();
        
        File file = new File(getWorkDir(), "testfile");
        file.createNewFile();
        
        FileObject fo = FileUtil.toFileObject(file);
        BugtrackingOwnerSupport.getInstance().setFirmAssociation(file, repo.getImpl());
        
        assertNotNull(fo);
        
        Repository r = RepositoryQuery.getRepository(fo, false);
        assertNotNull(r);
        assertEquals(repo, r);
    }

    public void testCustomImpl() throws IOException {
        File noRepoFile = new File(getWorkDir(), "norepo");
        noRepoFile.createNewFile();
        File assocFile = new File(getWorkDir(), "someassocfile");
        assocFile.createNewFile();
        
        FileObject noRepoFO = FileUtil.toFileObject(noRepoFile);
        assertNotNull(noRepoFO);
        Repository r = RepositoryQuery.getRepository(noRepoFO, false);
        assertNull(r);
        
        FileObject assocFO = FileUtil.toFileObject(assocFile);
        assertNotNull(assocFO);
        r = RepositoryQuery.getRepository(assocFO, false);
        assertNotNull(r);
        assertEquals(getRepo(), r);
    }
    
    private Repository getRepo() {
        return APITestKit.getRepo(APITestRepository.ID);
    }
    
    @org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.bugtracking.spi.RepositoryQueryImplementation.class)
    public static class RepositoryQImpl implements RepositoryQueryImplementation {
        @Override
        public String getRepositoryUrl(FileObject fileObject) {
            if(fileObject.getName().endsWith("norepo")) {
                return null;
            }
            return APITestKit.getRepo(APITestRepository.ID).getUrl();
        }
    }
    
}
