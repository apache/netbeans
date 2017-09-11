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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsPopulateTest extends NbTestCase {

    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private String branding;
    private Locale locale;
    
    
    public StampsPopulateTest(String testName) {
        super(testName);
    }            
    
    
    @Override
    protected void setUp() throws Exception {
        branding = NbBundle.getBranding();
        locale = Locale.getDefault();
        
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        ide = new File(install, "ide");
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath());
        System.setProperty("netbeans.user", userdir.getPath());
        
        createModule("org.openide.awt", platform, 50000L);
        createModule("org.openide.nodes", platform, 60000L);
        createModule("org.netbeans.api.languages", ide, 90000L);
        createModule("org.netbeans.modules.logmanagement", userdir, 10000L);
        
        Thread.sleep(100);
        
        createPopulateZip();

        Logger l = Logger.getLogger("org");
        l.setLevel(Level.OFF);
        l.setUseParentHandlers(false);
    }


    public void testPopulateTheCacheDirectory() throws Exception {
        InputStream is = Stamps.getModulesJARs().asStream("my/dir/file");
        assertNotNull("Cache found", is);
        
        byte[] arr = new byte[10];
        int len = is.read(arr);
        assertEquals("Len is 4", 4, len);
        assertEquals("Ahoj", new String(arr, 0, 4));
    }

    static void createModule(String cnb, File cluster, long accesTime) throws IOException {
        StampsTest.createModule(cnb, cluster, accesTime);
    }

    private void createPopulateZip() throws IOException {
        File cache = new File(new File(new File(ide, "var"), "cache"), "populate.zip");
        cache.getParentFile().mkdirs();
        JarOutputStream os = new JarOutputStream(new FileOutputStream(cache));
        os.putNextEntry(new ZipEntry("my/dir/file"));
        os.write("Ahoj".getBytes());
        os.closeEntry();
        os.close();
    }
}
