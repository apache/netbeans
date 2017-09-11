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

package org.openide.util;

import java.io.File;
import java.net.URI;
import java.util.Locale;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jiri Rechtacek et al.
 */
public class BaseUtilitiesTest extends NbTestCase {
    private String originalOsName;

    public BaseUtilitiesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BaseUtilities.resetOperatingSystem ();
        originalOsName = System.getProperty("os.name");
    }
    
    @Override
    protected void tearDown() throws Exception {
        System.setProperty("os.name", originalOsName);
        super.tearDown();
    }

    public void testGetOperatingSystemWinNT () {
        System.setProperty ("os.name", "Windows NT");
        //assertEquals ("System.getProperty (os.name) returns Windows NT", "Windows NT", System.getProperty ("os.name"));
        assertEquals ("Windows NT recognized as OS_WINNT", BaseUtilities.OS_WINNT, BaseUtilities.getOperatingSystem ());
    }

    public void testGetOperatingSystemFreebsd () {
        System.setProperty ("os.name", "FreeBSD");
        assertEquals ("System.getProperty (os.name) returns FreeBSD", "FreeBSD", System.getProperty ("os.name"));
        assertEquals ("System.getProperty (os.name) returns freebsd", "freebsd", System.getProperty ("os.name").toLowerCase (Locale.US));
        assertEquals ("FreeBSD recognized as OS_FREEBSD", BaseUtilities.OS_FREEBSD, BaseUtilities.getOperatingSystem ());
    }

    public void testGetOperatingSystemFreeBSDLowerCase () {
        System.setProperty ("os.name", "freebsd");
        assertEquals ("FreeBSD recognized as OS_FREEBSD", BaseUtilities.OS_FREEBSD, BaseUtilities.getOperatingSystem ());
    }

    public void testGetUnknownOperatingSystem () {
        System.setProperty ("os.name", "Unknown");
        if (File.pathSeparatorChar == ':') {
            assertTrue("Unknown os.name should be recognized as Unix.", BaseUtilities.isUnix());
        } else {
            assertEquals("Unknown os.name not OS_OTHER.", BaseUtilities.OS_OTHER, BaseUtilities.getOperatingSystem());
        }
    }

    public void testWhatIsWinXP () {
        System.setProperty ("os.name", "Windows XP");
        assertTrue ("Windows XP isWindows", BaseUtilities.isWindows ());
        assertFalse ("Windows XP not isUnix", BaseUtilities.isUnix ());
    }

    public void testWhatIsLinux () {
        System.setProperty ("os.name", "Linux");
        assertFalse ("Linux not isWindows", BaseUtilities.isWindows ());
        assertTrue ("Linux isUnix", BaseUtilities.isUnix ());
    }

    public void testWhatIsMac () {
        System.setProperty ("os.name", "Mac OS X");
        assertFalse ("Mac not isWindows", BaseUtilities.isWindows ());
        assertTrue ("Mac isMac", BaseUtilities.isMac ());
    }

    public void testWhatIsFreeBSD () {
        System.setProperty ("os.name", "freebsd");
        assertFalse ("freebsd is not isWindows", BaseUtilities.isWindows ());
        assertTrue ("freebsd isUnix", BaseUtilities.isUnix ());
    }

    public void testIsJavaIdentifier() throws Exception {
        assertTrue(BaseUtilities.isJavaIdentifier("whatever"));
        assertTrue(BaseUtilities.isJavaIdentifier("Ю"));
        assertTrue(BaseUtilities.isJavaIdentifier("_someThing$99"));
        assertFalse(BaseUtilities.isJavaIdentifier("99z"));
        assertFalse(BaseUtilities.isJavaIdentifier("assert"));
        assertFalse(BaseUtilities.isJavaIdentifier("null"));
        assertFalse(BaseUtilities.isJavaIdentifier(""));
        assertFalse(BaseUtilities.isJavaIdentifier(null));
        assertFalse(BaseUtilities.isJavaIdentifier("some.thing"));
    }

    public void testFileURI() throws Exception {
        if (BaseUtilities.isWindows()) {
            assertFileURI("C:\\some\\path #1", "file:/C:/some/path%20%231");
            assertEquals(new File("C:\\some\\path"), BaseUtilities.toFile(new URI("file:/C:/some/path")));
            assertEquals(new File("C:\\some\\path"), BaseUtilities.toFile(new URI("file:///C:/some/path")));
            assertEquals(new File("C:\\some\\path"), BaseUtilities.toFile(new URI("file:/C:/some/path/")));
            assertFileURI("\\\\server\\share\\path", "file://server/share/path");
            assertEquals(new File("\\\\server\\share\\path"), BaseUtilities.toFile(new URI("file:////server/share/path")));
            assertEquals(new File("\\\\server\\share\\path #1"), BaseUtilities.toFile(new URI("file:////server/share/path%20%231")));
        } else {
            assertFileURI("/some/path #1", "file:/some/path%20%231");
            assertEquals(new File("/some/path"), BaseUtilities.toFile(new URI("file:/some/path")));
            assertEquals(new File("/some/path"), BaseUtilities.toFile(new URI("file:///some/path")));
            assertEquals(new File("/some/path"), BaseUtilities.toFile(new URI("file:/some/path/")));
        }
        String s = BaseUtilities.toURI(getWorkDir()).toString();
        assertTrue(s, s.endsWith("/"));
        URI jar = BaseUtilities.toURI(new File(getWorkDir(), "some.jar"));
        URI jarN = jar.resolve("some.jar");
        assertEquals(jar, jarN);
        URI jarR = new URI("jar:" + jar + "!/");
        URI jarNR = new URI("jar:" + jarN + "!/");
        assertEquals("#214131: equal even when wrapped", jarR, jarNR);
        // XXX test that IllegalArgumentException is thrown where appropriate
    }
    private static void assertFileURI(String file, String uri) throws Exception {
        URI u = new URI(uri);
        File f = new File(file);
        assertEquals(u, BaseUtilities.toURI(f));
        assertEquals(f, BaseUtilities.toFile(u));
    }

}
