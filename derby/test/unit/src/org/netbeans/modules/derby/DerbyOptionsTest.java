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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.derby;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.derby.test.TestBase;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author abadea
 */
public class DerbyOptionsTest extends TestBase {

    private File userdir;
    private File externalDerby;
    
    public DerbyOptionsTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        userdir = new File(getWorkDir(), ".netbeans");
        userdir.mkdirs();
        
        // create a fake installation of an external derby database
        externalDerby = new File(userdir, "derby");
        createFakeDerbyInstallation(externalDerby);
    }

    public void testDerbyLocationIsNullWhenBundledDerbyNotInstalled() throws IOException {
        // assert the bundled derby is installed
        Lookups.executeWith(Lookups.singleton(new InstalledFileLocatorImpl(userdir)),
                new Runnable() {

                    @Override
                    public void run() {
                        final File bundledDerby = new File(userdir, DerbyOptions.INST_DIR);
                        if (bundledDerby.exists()) {
                            try {
                                FileUtil.toFileObject(bundledDerby).delete();
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }

                        assertNull(DerbyOptions.getDefaultInstallLocation());

                        DerbyOptions.getDefault().setLocation(externalDerby.getAbsolutePath());
                        assertFalse(DerbyOptions.getDefault().isLocationNull());

                        DerbyOptions.getDefault().setLocation("");
                        assertTrue(DerbyOptions.getDefault().isLocationNull());
                    }
                });
    }
    
    public void testLocationWhenNDSHPropertySetIssue76908() throws IOException {
        DerbyOptions.getDefault().setSystemHome(null);
        
        assertEquals("", DerbyOptions.getDefault().getSystemHome());

        File ndshSystemHome = new File(getWorkDir(), ".netbeans-derby-ndsh");
        if (!ndshSystemHome.mkdirs()) {
            throw new IOException("Could not create " + ndshSystemHome);
        }
        File systemHome = new File(getWorkDir(), ".netbeans-derby");
        if (!systemHome.mkdirs()) {
            throw new IOException("Could not create " + systemHome);
        }

        // returning the value of the netbeans.derby.system.home property when systemHome is not set...
        System.setProperty(DerbyOptions.NETBEANS_DERBY_SYSTEM_HOME, ndshSystemHome.getAbsolutePath());
        assertEquals(ndshSystemHome.getAbsolutePath(), DerbyOptions.getDefault().getSystemHome());

        // ... but returning systemHome when it is set
        DerbyOptions.getDefault().setSystemHome(systemHome.getAbsolutePath());
        assertEquals(systemHome.getAbsolutePath(), DerbyOptions.getDefault().getSystemHome());
    }
    
    public static final class InstalledFileLocatorImpl extends InstalledFileLocator {
        
        private final File userdir;
        
        public InstalledFileLocatorImpl(File userdir) {
            this.userdir = userdir;
        }
        
        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            File f = new File(userdir, relativePath);
            return f.exists() ? f : null;
        }
    }
}
