/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.util;

import java.io.File;
import java.net.URI;
import java.util.Locale;
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

    public void testParseParameters1() {
        String[] args = BaseUtilities.parseParameters("\"c:\\program files\\jdk\\bin\\java\" -Dmessage=\"Hello /\\\\/\\\\ there!\" -Xmx128m");
        assertEquals(3, args.length);
        assertEquals("c:\\program files\\jdk\\bin\\java", args[0]);
        assertEquals("-Dmessage=Hello /\\/\\ there!", args[1]);
        assertEquals("-Xmx128m", args[2]);
    }

    public void testParseParameters2() {
        String[] args = BaseUtilities.parseParameters("c:\\program files\\jdk\\bin\\java   -Xmx128m");
        assertEquals(3, args.length);
        assertEquals("c:\\program", args[0]);
        assertEquals("files\\jdk\\bin\\java", args[1]);
        assertEquals("-Xmx128m", args[2]);
    }

    public void testParseParameters3() {
        String[] args = BaseUtilities.parseParameters("\"-Xmx128m");
        assertEquals(1, args.length);
        assertEquals("-Xmx128m", args[0]);
    }

    public void testParseParameters4() {
        String[] args = BaseUtilities.parseParameters("'-Xmx128m");
        assertEquals(1, args.length);
        assertEquals("-Xmx128m", args[0]);
    }

    public void testParseParameters5() {
        String[] args = BaseUtilities.parseParameters("-Dmessage='Hello \"NetBeans\"'");
        assertEquals(1, args.length);
        assertEquals("-Dmessage=Hello \"NetBeans\"", args[0]);
    }

    public void testParseParameters6() {
        String[] args = BaseUtilities.parseParameters("'c:\\program files\\jdk\\bin\\java'\n-Dmessage='Hello /\\/\\ there!' \t -Xmx128m");
        assertEquals(3, args.length);
        assertEquals("c:\\program files\\jdk\\bin\\java", args[0]);
        assertEquals("-Dmessage=Hello /\\/\\ there!", args[1]);
        assertEquals("-Xmx128m", args[2]);
    }

    public void testParseParameters7() {
        String[] args = BaseUtilities.parseParameters("-Dmessage=\"NetBeans\" \"\" 'third\narg'");
        assertEquals(3, args.length);
        assertEquals("-Dmessage=NetBeans", args[0]);
        assertEquals("", args[1]);
        assertEquals("third\narg", args[2]);
    }

    public void testParseParameters8() {
        String[] args = BaseUtilities.parseParameters("-Dmessage=\"NetBeans\" \"\" \"third\\narg\"");
        assertEquals(3, args.length);
        assertEquals("-Dmessage=NetBeans", args[0]);
        assertEquals("", args[1]);
        assertEquals("third\\narg", args[2]);
    }

}
