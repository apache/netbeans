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
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsRenameTest extends NbTestCase implements Stamps.Updater{
    private File install;
    private File userdir;
    
    public StampsRenameTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    public void testTimeStampsWhenSystemTimeDifferentThanFileSystem() throws Exception {
        clearWorkDir();

        install = new File(getWorkDir(), "install");
        userdir = new File(getWorkDir(), "tmp");
        userdir.mkdirs();
        setNetBeansProperties(install, userdir, -1);


        Thread.sleep(500);
        long between = System.currentTimeMillis();
        Thread.sleep(500);

        
        Stamps.main("init");
        
        Stamps.getModulesJARs().scheduleSave(this, "test-cache", false);
        Stamps.getModulesJARs().waitFor(true);
        int[] arr = { 0 };
        File f = Stamps.getModulesJARs().file("test-cache", arr);
        assertNotNull("Cache found", f);
        final long previousLastModified = Stamps.moduleJARs();
        assertEquals("Stamps of caches shall be the same as stamps of .lastModified",
            f.lastModified(), previousLastModified
        );

        File i2 = new File(getWorkDir(), "inst2");
        assertTrue("Rename of the dir successful", install.renameTo(i2));
        
        setNetBeansProperties(i2, userdir, Stamps.moduleJARs());
        
        Thread.sleep(500);

        Stamps.main("clear");
        
        //assertEquals("Modified time remains the same", previousLastModified, Stamps.moduleJARs());
        File newF = Stamps.getModulesJARs().file("test-cache", arr);
        assertNotNull("Cache can still be found", newF);
    }

    private static void setNetBeansProperties(File install, File userdir, long stamp) throws IOException {
        File platform = new File(install, "platform");
        final File platformLast = new File(platform, ".lastModified");
        if (stamp == -1) {
            platform.mkdirs();
            platformLast.createNewFile();
        } else {
            platformLast.setLastModified(stamp);
        }
        File ide = new File(install, "ide");
        final File ideLast = new File(ide, ".lastModified");
        if (stamp == -1) {
            ide.mkdirs();
            ideLast.createNewFile();
        } else {
            ideLast.setLastModified(stamp);
        }
        File extra = new File(install, "extra");
        assertFalse("One cluster does not exists", extra.isDirectory());
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + extra.getPath());
        System.setProperty("netbeans.user", userdir.getPath());
    }

    @Override
    public void flushCaches(DataOutputStream os) throws IOException {
        os.write(1);
    }

    @Override
    public void cacheReady() {
    }
}
