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

package org.apache.tools.ant.module.spi;

import java.util.logging.Level;
import java.io.File;
import java.net.URL;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach
 */
public class AutomaticExtraClasspathTest extends NbTestCase {
    private static URL wd;
    
    
    FileObject fo, bad;
    
    public AutomaticExtraClasspathTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        URL u = getClass().getResource("AutomaticExtraClasspathTest.xml");
        FileSystem fs = new XMLFileSystem(u);
        fo = fs.findResource("testAutoProvider");
        assertNotNull("There is the resource", fo);
        bad = fs.findResource("brokenURL");
        assertNotNull("There is the bad", bad);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    public static URL getWD() {
        return wd;
    }
    
    public void testReadWorkDir() throws Exception {
        URL u = Utilities.toURI(getWorkDir()).toURL();
        wd = u;
        
        Object value = fo.getAttribute("instanceCreate");
        assertTrue("provider created: " + value, value instanceof AutomaticExtraClasspathProvider);
        
        AutomaticExtraClasspathProvider auto = (AutomaticExtraClasspathProvider)value;
        File[] arr = auto.getClasspathItems();
        assertNotNull(arr);
        assertEquals("One item", 1, arr.length);
        assertEquals("It is our work dir", getWorkDir(), arr[0]);
    }

    public void testBadURL() throws Exception {
        CharSequence log = Log.enable("", Level.INFO);
        Object value = bad.getAttribute("instanceCreate");
        assertNull("no provider created: " + value, value);
        
        if (log.toString().indexOf("IllegalArgumentException") == -1) {
            fail("IllegalArgumentException shall be thrown:\n" + log);
        }
    }

    public void testFailIfTheFileDoesNotExists() throws Exception {
        URL u = Utilities.toURI(new File(getWorkDir(), "does-not-exists.txt")).toURL();
        wd = u;
        
        CharSequence log = Log.enable("", Level.INFO);
        Object value = fo.getAttribute("instanceCreate");
        AutomaticExtraClasspathProvider auto = (AutomaticExtraClasspathProvider)value;
        assertNotNull(auto);
        assertEquals(0, auto.getClasspathItems().length);
        if (log.toString().indexOf("No File found") == -1) {
            fail("should have warned:\n" + log);
        }
    }
    
}
