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

package org.netbeans.core.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
/**
 * Test functionality of InstalledFileLocatorImpl.
 * @author Jesse Glick
 */
public class InstalledFileLocatorImplDirTest extends NbTestCase {

    static {
        InstalledFileLocatorImplDirTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    public InstalledFileLocatorImplDirTest(String name) {
        super(name);
    }
    
    private File scratch, nbhome, nbuser, nbdir1, nbdir2;
    private InstalledFileLocator ifl;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        scratch = new File(getWorkDir(), "install");
        nbhome = new File(scratch, "nbhome");
        touch(file(nbhome, "a/b"));
        touch(file(nbhome, "a/c"));
        touch(file(nbhome, "d"));
        touch(file(nbhome, "e/f/g"));
        touch(file(nbhome, "loc/x.html"));
        touch(file(nbhome, "loc/x_ja.html"));
        touch(file(nbhome, "loc/x_foo.html"));
        touch(file(nbhome, "loc/x_foo_ja.html"));
        touch(file(nbhome, "loc/y.html"));
        touch(file(nbhome, "h_ja"));
        nbuser = new File(getWorkDir(), "nbuser");
        touch(file(nbuser, "a/b"));
        nbdir1 = new File(scratch, "nbdir1");
        touch(file(nbdir1, "e/f/g"));
        nbdir2 = new File(scratch, "nbdir2");
        touch(file(nbdir2, "h"));
        touch(file(nbdir2, "loc/y_foo.html"));
        File nbdirx = new File(scratch, "nbdirx"); // nonexistent dir
        System.setProperty("netbeans.home", nbhome.getAbsolutePath());
        System.setProperty("netbeans.user", nbuser.getAbsolutePath());
        System.setProperty("netbeans.dirs",
            nbdir1.getAbsolutePath() + File.pathSeparatorChar +
            nbdir2.getAbsolutePath() + File.pathSeparatorChar +
            // Useless trailing separator intentional:
            nbdirx.getAbsolutePath() + File.pathSeparatorChar);
        NbBundle.setBranding("foo");
        Locale.setDefault(Locale.JAPAN);
        ifl = new InstalledFileLocatorImpl();
    }
    private static File file(File dir, String path) {
        return new File(dir, path.replace('/', File.separatorChar));
    }
    
    private static void touch(File f) throws IOException {
        File p = f.getParentFile();
        if (!p.exists()) {
            if (!p.mkdirs()) {
                throw new IOException(p.getAbsolutePath());
            }
        }
        OutputStream os = new FileOutputStream(f);
        os.close();
    }

    /**
     * Test file locating. Verify that no excessive File.isDirectory() is not
     * called.
     */
    public void testLocate() throws Exception {
        IsDirCntSecurityManager.initialize();
        InstalledFileLocatorImpl.prepareCache();
        assertEquals("[cache] found a simple file", file(nbhome, "d"), ifl.locate("d", null, false));
        assertEquals("[cache] did not find a nonexistent file", null, ifl.locate("d2", null, false));
        assertEquals("[cache] found an override in nbuser", file(nbuser, "a/b"), ifl.locate("a/b", null, false));
        assertEquals("[cache] found a non-override in nbhome", file(nbhome, "a/c"), ifl.locate("a/c", null, false));
        assertEquals("[cache] found an overridden dir in nbuser", file(nbuser, "a"), ifl.locate("a", null, false));
        assertEquals("[cache] did not find a nonexistent file in an existing dir", null, ifl.locate("a/x", null, false));
        assertEquals("[cache] found a multilevel override in an nbdirs component", file(nbdir1, "e/f/g"), ifl.locate("e/f/g", null, false));
        assertEquals("[cache] all nbdirs components scanned", file(nbdir2, "h"), ifl.locate("h", null, false));
        assertEquals("[cache] localized and branded resource can be found", file(nbhome, "loc/x_foo_ja.html"), ifl.locate("loc/x.html", null, true));
        assertEquals("[cache] nbdirs can override location of a branded resource", file(nbdir2, "loc/y_foo.html"), ifl.locate("loc/y.html", null, true));
        assertEquals("[cache] but look in all dirs for most specific resource first", file(nbhome, "h_ja"), ifl.locate("h", null, true));
        assertEquals("[cache] localized lookup a no-op for nonlocalized files", file(nbuser, "a/b"), ifl.locate("a/b", null, true));
        InstalledFileLocatorImpl.discardCache();
        assertEquals("[no cache] found a simple file", file(nbhome, "d"), ifl.locate("d", null, false));
        assertEquals("[no cache] did not find a nonexistent file", null, ifl.locate("d2", null, false));
        touch(file(nbhome, "d2"));
        assertEquals("[no cache] but did find a newly added file", file(nbhome, "d2"), ifl.locate("d2", null, false));
        assertEquals("[no cache] found an override in nbuser", file(nbuser, "a/b"), ifl.locate("a/b", null, false));
        assertEquals("[no cache] found a non-override in nbhome", file(nbhome, "a/c"), ifl.locate("a/c", null, false));
        assertEquals("[no cache] found an overridden dir in nbuser", file(nbuser, "a"), ifl.locate("a", null, false));
        assertEquals("[no cache] did not find a nonexistent file in an existing dir", null, ifl.locate("a/x", null, false));
        assertEquals("[no cache] found a multilevel override in an nbdirs component", file(nbdir1, "e/f/g"), ifl.locate("e/f/g", null, false));
        assertEquals("[no cache] all nbdirs components scanned", file(nbdir2, "h"), ifl.locate("h", null, false));
        assertEquals("[no cache] localized and branded resource can be found", file(nbhome, "loc/x_foo_ja.html"), ifl.locate("loc/x.html", null, true));
        assertEquals("[no cache] nbdirs can override location of a branded resource", file(nbdir2, "loc/y_foo.html"), ifl.locate("loc/y.html", null, true));
        assertEquals("[no cache] but look in all dirs for most specific resource first", file(nbhome, "h_ja"), ifl.locate("h", null, true));
        assertEquals("[no cache] localized lookup a no-op for nonlocalized files", file(nbuser, "a/b"), ifl.locate("a/b", null, true));
        IsDirCntSecurityManager.assertCounts("Excessive File.isDirectory() calls!", 3, 20);
    }
    
}
