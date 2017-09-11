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
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsTimeShiftTest extends NbTestCase implements Stamps.Updater{
    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private File extra;
    
    public StampsTimeShiftTest(String testName) {
        super(testName);
    }            

    public void testTimeStampsWhenSystemTimeDifferentThanFileSystem() throws Exception {
        clearWorkDir();

        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        platform.mkdirs();
        new File(platform, ".lastModified").createNewFile();
        ide = new File(install, "ide");
        ide.mkdirs();
        new File(ide, ".lastModified").createNewFile();
        extra = new File(install, "extra");
        userdir = new File(getWorkDir(), "tmp");
        userdir.mkdirs();
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + extra.getPath());
        System.setProperty("netbeans.user", userdir.getPath());

        assertFalse("One cluster does not exists", extra.isDirectory());

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

        extra.mkdirs();
        File lastModified = new File(extra, ".lastModified");
        lastModified.createNewFile();
        lastModified.setLastModified(between);
        long now = System.currentTimeMillis();
        if (lastModified.lastModified() >= now) {
            fail("Last modified shall be set to past: " + lastModified.lastModified() + "\nbut was: " + now);
        }
        Stamps.main("clear");
        assertNull("Cache invalidated", Stamps.getModulesJARs().asByteBuffer("test-cache"));
    }

    public void flushCaches(DataOutputStream os) throws IOException {
        os.write(1);
    }

    public void cacheReady() {
    }
}
