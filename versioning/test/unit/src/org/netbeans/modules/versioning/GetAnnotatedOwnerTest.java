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
package org.netbeans.modules.versioning;

import org.netbeans.modules.versioning.core.VersioningManager;
import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.testvcs.TestAnnotatedVCS;
import org.openide.util.test.MockLookup;

public class GetAnnotatedOwnerTest extends GetOwnerTest {
    
    public GetAnnotatedOwnerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
    }
    
    @Override
    protected File getVersionedFolder() {
        if (versionedFolder == null) {
            versionedFolder = new File(dataRootDir, "workdir/root-" + TestAnnotatedVCS.VERSIONED_FOLDER_SUFFIX);
            versionedFolder.mkdirs();
            new File(versionedFolder, TestAnnotatedVCS.TEST_VCS_METADATA).mkdirs();
        }
        return versionedFolder;
    }
    
    @Override
    protected Class getVCS() {
        return TestAnnotatedVCS.class;
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new GetAnnotatedOwnerTest("testVCSSystemDoesntAwakeOnUnrelatedGetOwner"));
        suite.addTest(new GetAnnotatedOwnerTest("testNoOwnerIfManagedByOtherSPI"));
        return suite;
    }

    // must run as the first test in the suite
    public void testVCSSystemDoesntAwakeOnUnrelatedGetOwner() throws IOException {
        
        assertNull(TestAnnotatedVCS.INSTANCE);
        
        File f = new File(getUnversionedFolder(), "sleepingfile");
        f.createNewFile();
        
        assertNull(TestAnnotatedVCS.INSTANCE);
        VCSSystemProvider.VersioningSystem owner = VersioningManager.getInstance().getOwner(VCSFileProxy.createFileProxy(f));
        assertNull(owner);
        
        assertNull(TestAnnotatedVCS.INSTANCE);
    }
    
    public void testNoOwnerIfManagedByOtherSPI() throws IOException {
        File f = new File(dataRootDir, OtherSPIVCS.MANAGED_FOLDER_PREFIX);
        f.mkdirs();
        f = new File(f, "file.txt");
        assertNull(VersioningSupport.getOwner(f));
        
        f = new File(getVersionedFolder(), "file.txt");
        assertNull(org.netbeans.modules.versioning.core.api.VersioningSupport.getOwner(VCSFileProxy.createFileProxy(f)));
    }
    
    @org.netbeans.modules.versioning.core.spi.VersioningSystem.Registration(
            actionsCategory="fileproxyvcs",
            displayName="fileproxyvcs",
            menuLabel="fileproxyvcs",
            metadataFolderNames="")
    public static class OtherSPIVCS extends org.netbeans.modules.versioning.core.spi.VersioningSystem {
        static String MANAGED_FOLDER_PREFIX = "fileproxyspi";
        @Override
        public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
            if(file.getParentFile() != null && file.getParentFile().getName().startsWith(MANAGED_FOLDER_PREFIX)) {
                return file.getParentFile();
            }
            return null;
        }
    }
    
}
