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
package org.netbeans.modules.versioning.ui.history;


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSHistoryProvider;
import org.openide.nodes.Node;

import org.openide.util.Lookup;

/**
 * Versioning SPI unit tests of VCSInterceptor.
 * 
 * @author Maros Sandor
 */
public class VCSSPIHistoryTest extends NbTestCase {
    
    private File dataRootDir;

    public VCSSPIHistoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = getDataDir(); 
        File userdir = new File(dataRootDir + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        if(!dataRootDir.exists()) dataRootDir.mkdirs();
        Lookup.getDefault().lookupAll(VersioningSystem.class);
        File f = new File(dataRootDir, "workdir");
        deleteRecursively(f);
        f.mkdirs();
        f = new File(dataRootDir, "workdir/root-test-versioned");
        f.mkdirs();
    }

    public void testHistoryNodesProperlySetup() throws IOException {
        File f1 = new File(dataRootDir, "workdir/root-test-versioned/file1" + TestVCSHistoryProvider.FILE_PROVIDES_REVISIONS_SUFFIX);
        f1.createNewFile();
        VCSFileProxy proxy1 = VCSFileProxy.createFileProxy(f1);
        File f2 = new File(dataRootDir, "workdir/root-test-versioned/file2" + TestVCSHistoryProvider.FILE_PROVIDES_REVISIONS_SUFFIX);
        f2.createNewFile();
        VCSFileProxy proxy2 = VCSFileProxy.createFileProxy(f2);
        VCSSystemProvider.VersioningSystem pvs = Utils.getOwner(proxy1);
        assertNotNull(pvs);
        org.netbeans.modules.versioning.core.spi.VCSHistoryProvider php = pvs.getVCSHistoryProvider();
        assertNotNull(php);

        org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEntry[] phistory = php.getHistory(new VCSFileProxy[] {proxy1, proxy2}, null);
        assertNotNull(phistory);
        assertTrue(phistory.length > 0);

        Node node = HistoryTestKit.createHistoryNode(phistory[0]);
        assertNotNull(node.getLookup().lookup(HistoryEntry.class));

        Collection<? extends File> fos = node.getLookup().lookupAll(File.class);
        assertNotNull(fos);
        assertTrue(fos.size() >= 2);
        assertTrue(fos.contains(f1));
        assertTrue(fos.contains(f2));
    }

    private void deleteRecursively(File f) {
        if(f.isFile()) {
            f.delete();
        } else {
            File[] files = f.listFiles();
            if(files != null) {
                for (File file : files) {
                    deleteRecursively(file);
                    file.delete();
                }
            }
        }
    }
    
    private class RevisionProviderImpl implements VCSHistoryProvider.RevisionProvider {
        boolean revisionprovided = false;
        @Override
        public void getRevisionFile(File originalFile, File revisionFile) {
            revisionprovided = true;
        }
    }
    
    private class ParentProviderImpl implements VCSHistoryProvider.ParentProvider {
        static final String PARENT_MSG = "im.the.parent";
        @Override
        public HistoryEntry getParentEntry(File file) {
            return new VCSHistoryProvider.HistoryEntry(
                    new File[] {file}, 
                    new Date(System.currentTimeMillis()), 
                    PARENT_MSG, 
                    "user", 
                    "username", 
                    "12345", 
                    "1234567890", 
                    new Action[0], 
                    null,
                    null);
        }
    }
    
    private class MessageEditProviderImpl implements VCSHistoryProvider.MessageEditProvider {
        String message;
        @Override
        public void setMessage(String message) throws IOException {
            this.message = message;
        }
    }       
}
