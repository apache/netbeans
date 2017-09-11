/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.js.breakpoints;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class URLEqualityTest extends NbTestCase {
    private File orig;
    
    public URLEqualityTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File odir = new File(getWorkDir(), "orig");
        odir.mkdir();
        orig = new File(odir, "test.js");
        orig.createNewFile();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEqualSymlinks() throws Exception {
        if (!Utilities.isUnix()) {
            return;
        }
        File copy = new File(getWorkDir(), "copy");
        int ret = new ProcessBuilder("ln", "-s", "orig", copy.getPath()).start().waitFor();
        assertEquals("Symlink created", ret, 0);
        assertTrue("Dir exists", copy.exists());
        File f = new File(copy, "test.js");
        assertTrue("File exists", f.exists());
        
        URLEquality oe = new URLEquality(orig.toURI().toURL());
        URLEquality ne = new URLEquality(f.toURI().toURL());

        assertEquals("Same hashCode", oe.hashCode(), ne.hashCode());
        assertEquals("They are similar", oe, ne);
        
    }

    public void testDifferentInSiblinks() throws Exception {
        File copy = new File(getWorkDir(), "copy");
        copy.mkdir();
        File f = new File(copy, "test.js");
        f.createNewFile();
        
        URLEquality oe = new URLEquality(orig.toURI().toURL());
        URLEquality ne = new URLEquality(f.toURI().toURL());
        
        assertEquals("Same hashCode", oe.hashCode(), ne.hashCode());
        assertFalse("Not equals", oe.equals(ne));
    }
    
}
