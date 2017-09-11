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

package org.netbeans;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsClusterMovedTest extends NbTestCase implements Stamps.Updater{
    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private File mainCluster;
    
    public StampsClusterMovedTest(String testName) {
        super(testName);
    }            

    public void testMoveOfAClusterIsDetected() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        platform.mkdirs();
        new File(platform, ".lastModified").createNewFile();
        ide = new File(install, "ide");
        ide.mkdirs();
        new File(ide, ".lastModified").createNewFile();
        mainCluster = new File(install, "extra");
        mainCluster.mkdirs();
        assertTrue("Extra cluster exists", mainCluster.isDirectory());
        new File(mainCluster, ".lastModified").createNewFile();
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + mainCluster.getPath());
        System.setProperty("netbeans.user", userdir.getPath());


        Thread.sleep(500);
        long between = System.currentTimeMillis();
        Thread.sleep(500);

        
        Stamps.main("init");
        
        Stamps.getModulesJARs().scheduleSave(this, "test-cache", false);
        Stamps.getModulesJARs().waitFor(true);
        int[] arr = { 0 };
        File f = Stamps.getModulesJARs().file("test-cache", arr);
        assertNotNull("Cache found", f);
        assertEquals("Stamps of caches shall be the same as stamps of .lastModified",
            f.lastModified(), Stamps.moduleJARs()
        );


        Thread.sleep(500);

        File subDir = new File(getWorkDir(), "subdir");
        subDir.mkdirs();
        final File newExtra = new File(subDir, mainCluster.getName());
        boolean renRes = mainCluster.renameTo(newExtra);
        assertTrue("Rename succeeded", renRes);
        assertTrue("Extra renamed: " + newExtra, newExtra.isDirectory());
        
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + newExtra.getPath());
        
        Stamps.main("init");
        
        assertNull("Cache invalidated as relative location of clusters changed", 
            Stamps.getModulesJARs().asByteBuffer("test-cache")
        );
    }
    
    public void testChangeOfClustersIsDetectedInSharedConfig() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        platform.mkdirs();
        new File(platform, ".lastModified").createNewFile();
        ide = new File(install, "ide");
        ide.mkdirs();
        new File(ide, ".lastModified").createNewFile();
        mainCluster = new File(install, "extra");
        mainCluster.mkdirs();
        assertTrue("Extra cluster exists", mainCluster.isDirectory());
        new File(mainCluster, ".lastModified").createNewFile();
        userdir = new File(getWorkDir(), "tmp");
        userdir.mkdirs();
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath());
        // generate the cache to mainCluster directory
        System.setProperty("netbeans.user", mainCluster.getPath());


        Thread.sleep(500);
        long between = System.currentTimeMillis();
        Thread.sleep(500);

        
        Stamps.main("init");
        
        Stamps.getModulesJARs().scheduleSave(this, "test-cache", false);
        Stamps.getModulesJARs().waitFor(true);
        int[] arr = { 0 };
        File f = Stamps.getModulesJARs().file("test-cache", arr);
        assertNotNull("Cache found", f);
        assertEquals("Stamps of caches shall be the same as stamps of .lastModified",
            f.lastModified(), Stamps.moduleJARs()
        );
        
        File lmdir = new File(new File(new File(mainCluster, "var"), "cache"), "lastModified");
        assertTrue(lmdir + " is dir", lmdir.isDirectory());
        lmdir.renameTo(new File(lmdir.getParentFile(), "ignore"));
        assertFalse(lmdir + " is no longer dir", lmdir.isDirectory());

        Thread.sleep(500);

        System.setProperty("netbeans.user", userdir.getPath());
        // use mainCluster as cluster
        System.setProperty("netbeans.dirs", mainCluster.getPath() + File.pathSeparator + ide.getPath());
        
        Stamps.main("init");
        
        assertNull("Cache invalidated set of clusters changed", 
            Stamps.getModulesJARs().asByteBuffer("test-cache")
        );
    }

    @Override
    public void flushCaches(DataOutputStream os) throws IOException {
        os.write(1);
    }

    @Override
    public void cacheReady() {
    }
}
