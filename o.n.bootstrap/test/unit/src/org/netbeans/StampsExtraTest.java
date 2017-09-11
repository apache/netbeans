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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.Places;
import org.openide.modules.api.PlacesTestUtils;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsExtraTest extends NbTestCase {
    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private File extra;
    
    public StampsExtraTest(String testName) {
        super(testName);
    }            

    public void testTimeStampsWhenAddingCluster() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        platform.mkdirs();
        ide = new File(install, "ide");
        ide.mkdirs();
        extra = new File(install, "extra");
        userdir = new File(getWorkDir(), "tmp");
        userdir.mkdirs();
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + extra.getPath());
        PlacesTestUtils.setUserDirectory(userdir);

        touch(platform, ".lastModified", 50000L);
        touch(ide, ".lastModified", 90000L);
        assertFalse("One cluster does not exists", extra.isDirectory());
        
        Stamps.main("init");
        
        Thread.sleep(100);

        Logger l = Logger.getLogger("org");
        l.setLevel(Level.OFF);
        l.setUseParentHandlers(false);

        long stamp = Stamps.moduleJARs();
        assertEquals("Timestamp is taken from api.languages module", 90000L, stamp);

        Stamps.main("clear");

        CountingSecurityManager.initialize(install.getPath());

        long newStamp = Stamps.moduleJARs();

        CountingSecurityManager.assertCounts("Just few accesses to installation", 6);
        assertEquals("Stamps are the same", stamp, newStamp);

        assertFalse("File has not been created for non-existing cluster", Places.getCacheSubfile("lastModified/extra").canRead());

        extra.mkdirs();
        File lastModified = new File(extra, ".lastModified");
        lastModified.createNewFile();
        lastModified.setLastModified(200000L);
        assertEquals("Correct last modified", 200000L, lastModified.lastModified());

        Stamps.main("clear");
        stamp = Stamps.moduleJARs();
        if (stamp < 200000L) {
            fail("lastModified has not been updated: " + stamp);
        }
    }

    private static void touch(File root, String rel, long time) throws IOException {
        File f = new File(root, rel.replace('/', File.separatorChar));
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        f.setLastModified(time);
        assertEquals("Correct last modified for " + f, time, f.lastModified());
    }
}
