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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
public class StampsIdeLessThanPlatformTest extends NbTestCase {
    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private File nonexist;
    
    public StampsIdeLessThanPlatformTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        nonexist = new File(install, "nonexist7");
        ide = new File(install, "ide");
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath() + File.pathSeparator + nonexist.getPath());
        PlacesTestUtils.setUserDirectory(userdir);
        
        StampsTest.createModule("org.openide.awt", platform, 50000L);
        StampsTest.createModule("org.openide.nodes", platform, 60000L);
        StampsTest.createModule("org.netbeans.api.languages", ide, 50000L);
        StampsTest.createModule("org.netbeans.modules.logmanagement", userdir, 10000L);
        
        Stamps.main("reset");
        
        Thread.sleep(100);

        Logger l = Logger.getLogger("org");
        l.setLevel(Level.OFF);
        l.setUseParentHandlers(false);
    }
    
    public void testGenerateTimeStamps() throws IOException {
        long stamp = Stamps.moduleJARs();
        assertEquals("Timestamp is taken from nodes module", 60000L, stamp);
        
        StampsTest.assertStamp(60000L, platform, false, true);
        StampsTest.assertStamp(50000L, ide, false, true);
        StampsTest.assertStamp(-1L, userdir, false, false);


        File checkSum = Places.getCacheSubfile("lastModified/all-checksum.txt");
        assertTrue("Checksum created" , checkSum.isFile());

        byte[] arr = new byte[30000];
        int len = new FileInputStream(checkSum).read(arr);

        String r = new String(arr, 0, len);
        if (r.contains("nonexist")) {
            fail(r);
        }

        int check = 0;
        BufferedReader in = new BufferedReader(new FileReader(checkSum));
        
        for (;;) {
            String line = in.readLine();
            if (line == null) {
                break;
            }
            String[] seg = line.split("=");
            assertEquals("There should be one = in the: " + line, 2, seg.length);
            String s = seg[0];
            if (s.endsWith("platform")) {
                assertEquals("Correct for platform: " + line, "60000", seg[1]);
                check ++;
            }
            if (s.endsWith("ide")) {
                assertEquals("Correct for ide: " + line, "50000", seg[1]);
                check ++;
            }
        }
        assertEquals("Two checks", 2, check);
    }        
    

}
