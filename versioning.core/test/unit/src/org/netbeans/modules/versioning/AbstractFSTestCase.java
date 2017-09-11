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

import java.io.IOException;
import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.testvcs.TestVCS;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

public class AbstractFSTestCase extends NbTestCase {
    
    protected String workDirPath;
    protected FileObject versionedFolder;
    protected FileObject unversionedFolder;
    private String versionedPath;

    public AbstractFSTestCase(String testName) {
        super(testName);
    }

    protected FileObject getVersionedFolder() throws IOException {
        if (versionedFolder == null) {
            versionedFolder = createFolder(versionedPath);
            FileObject md = versionedFolder.getFileObject(TestVCS.TEST_VCS_METADATA);
            if(md == null || !md.isValid()) {
                createFolder(versionedPath + "/" + TestVCS.TEST_VCS_METADATA);
            }
            // cleanup the owner cache, this folder just became versioned 
            VersioningManager.getInstance().flushNullOwners(); 
        }
        return versionedFolder;
    }
    
    protected FileObject getNotVersionedFolder() throws IOException {
        if (unversionedFolder == null) {
            unversionedFolder = createFolder(workDirPath + "/unversioned/");
        }
        return unversionedFolder;
    }

    protected String getRoot(String path) {
        int idx = path.indexOf(versionedPath);
        return idx > 0 ? path.substring(0, idx) : null;
    } 
    
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
        File workDir = getWorkDir();
        workDirPath = workDir.getParentFile().getName() + "/" + workDir.getName();
        versionedPath = workDirPath + "/root" + TestVCS.VERSIONED_FOLDER_SUFFIX;
        File userdir = new File(workDir, "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());

        createFolder(workDirPath).delete();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        createFolder(workDirPath).delete();
    }
     
    
    protected boolean isMasterFS() throws IOException {
        File f = VCSFileProxy.createFileProxy(getVersionedFolder()).toFile(); // reurns null if not masterfs
        return f != null;
    }

    protected FileObject createFolder (String path) throws IOException {
        return VCSFilesystemTestFactory.getInstance(this).createFolder(path);
    }
    
    protected VCSFileProxy toVCSFileProxy(FileObject fo) throws IOException {
        return VCSFileProxy.createFileProxy(fo);
    }

    protected Class getVCS() {
        return TestVCS.class;
    }    
}
