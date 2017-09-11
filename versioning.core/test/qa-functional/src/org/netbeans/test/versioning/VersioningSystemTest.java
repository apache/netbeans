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

package org.netbeans.test.versioning;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.textui.TestRunner;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test Compatibility Kit xtest class.
 * 
 * @author Maros Sandor
 */
public class VersioningSystemTest extends JellyTestCase {
    
    private File    propertiesFile;
    private String  versioningSystemClassName;
    private File    rootDir;
    private VersioningSystem testedSystem;

    public VersioningSystemTest(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
//        NbTestSuite suite = new NbTestSuite();
//        suite.addTest(new VersioningSystemTest("testOwnership"));
//        suite.addTest(new VersioningSystemTest("testInterceptor"));
//        return suite;
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration()
                .addTest(VersioningSystemTest.class, 
                        "testOwnership",
                        "testInterceptor")
                .enableModules(".*").clusters(".*"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        propertiesFile = new File(getDataDir(), "tck.properties");
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propertiesFile);
        props.load(fis);
        versioningSystemClassName = props.getProperty("test.vcs");
        rootDir = new File(props.getProperty("test.root"));

        testedSystem = VersioningManager.getInstance().getOwner(VCSFileProxy.createFileProxy(rootDir));
        assertNotNull(testedSystem);
        assertEquals(testedSystem.getClass().getName(), versioningSystemClassName);
    }

    public void testInterceptor() throws IOException {
        File newFile = new File(rootDir, "vcs-tck-created.txt");
        assertFalse(newFile.exists());
        FileObject fo = FileUtil.toFileObject(rootDir);

        // test creation
        FileObject newfo = fo.createData("vcs-tck-created.txt");
        
        sleep(1000);

        // test delete
        newfo.delete();
    }
    
    public void testOwnership() throws IOException {
        VersioningSystem vs;
        VCSFileProxy rootProxy = VCSFileProxy.createFileProxy(rootDir);
        vs = VersioningManager.getInstance().getOwner(rootProxy.getParentFile());
        assertNull(vs);

        testOwnershipRecursively(rootProxy);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(VersioningSystemTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void testOwnershipRecursively(VCSFileProxy dir) {
        VersioningSystem vs = VersioningManager.getInstance().getOwner(dir);
        assertEquals(testedSystem, vs);
        VCSFileProxy [] children = dir.listFiles();
        if (children == null) return;
        for (VCSFileProxy child : children) {
            testOwnershipRecursively(child);
        }
    }
}
