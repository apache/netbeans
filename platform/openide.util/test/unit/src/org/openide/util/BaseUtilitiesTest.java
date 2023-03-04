/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import static org.openide.util.BaseUtilities.*;
import static java.lang.Boolean.*;

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
    //--------------------------------------------------------------------------
    public void test_IsJavaIdentifier_returnsTrue_whenGivenJavaIdentifier()
            throws Exception {

        assertTrue(isJavaIdentifier("whatever"));
        assertTrue(isJavaIdentifier("Ю"));
        assertTrue(isJavaIdentifier("_someThing$99"));
    }
    
    //--------------------------------------------------------------------------
    public void test_IsJavaIdentifier_returnsFalse_whenImproperArgument()
            throws Exception {

        assertFalse(isJavaIdentifier("99z"));
        assertFalse(isJavaIdentifier("assert"));
        assertFalse(isJavaIdentifier("null"));
        assertFalse(isJavaIdentifier(""));
        assertFalse(isJavaIdentifier(null));
        assertFalse(isJavaIdentifier("some.thing"));
    }

    //--------------------------------------------------------------------------
    public void test_toFile_throwsNullPonter_whenArgumentIsNull()
            throws Exception {

        try {
            toFile(null);
            fail();
        } catch (final NullPointerException e) {
            //good
        }
    }
    
    //--------------------------------------------------------------------------
    public void test_toFile_throwsIllegalArgument_whenGivenNonFileURI()
            throws Exception {

        try {
            toFile(new URI("http://example.com"));
            fail();
        } catch (final IllegalArgumentException e) {
            //good
        }
        try {
            toFile(new URI("mailto:person@example.com"));
            fail();
        } catch (final IllegalArgumentException e) {
            //good
        }
        try {
            toFile(new URI("isbn:12345"));
            fail();
        } catch (final IllegalArgumentException e) {
            //good
        }
    }
    //--------------------------------------------------------------------------
    public void test_toFile_throwsIllegalArgument_whenGivenFileURIWithIllegalCharacter()
            throws Exception {

        try {
            toFile(new URI("file:/C:/some/?"));
            fail();
        } catch (final IllegalArgumentException e) {
            //good
        }
    }
    
    //--------------------------------------------------------------------------
    public void test_toFile_returnsFile_whenGivenCorrectURI()
          throws Exception {

        if (isUnix()) {
            assertEquals(new File("/some/path"), toFile(new URI("file:/some/path")));
            assertEquals(new File("/some/path"), toFile(new URI("file:///some/path")));
            assertEquals(new File("/some/path"), toFile(new URI("file:/some/path/")));
            assertEquals(new File("/some/path #1"), toFile(new URI("file:/some/path%20%231")));
            assertEquals(new File("/tmp/hezky česky"), toFile(new URI("file:/tmp/hezky%20česky")));

            assertEquals(new File("/a/b/c"), toFile(new URI("file", null, "/a/b/c", null)));
            assertEquals(new File("/a/b/c"), toFile(new URI("file", "", "/a/b/c", null)));
        }
        if (isWindows()) {
            assertEquals(new File("C:\\some\\path #1"), toFile(new URI("file:/C:/some/path%20%231")));
            assertEquals(new File("C:\\some\\path"), toFile(new URI("file:/C:/some/path")));
            assertEquals(new File("C:\\some\\path"), toFile(new URI("file:///C:/some/path")));
            assertEquals(new File("C:\\some\\path"), toFile(new URI("file:/C:/some/path/")));
            assertEquals(new File("\\\\server\\share\\path"), toFile(new URI("file://server/share/path")));
            assertEquals(new File("\\\\server\\share\\path"), toFile(new URI("file:////server/share/path")));
            assertEquals(new File("\\\\server\\share\\path #1"), toFile(new URI("file:////server/share/path%20%231")));
            assertEquals(new File("C:\\tmp\\hezky česky"), toFile(new URI("file:/C:/tmp/hezky%20česky")));

            assertEquals(new File("\\a\\b\\c"), toFile(new URI("file", null, "/a/b/c", null)));
            assertEquals(new File("\\a\\b\\c"), toFile(new URI("file", "", "/a/b/c", null)));
        }
    }
    
    //--------------------------------------------------------------------------
    public void test_toFile_acceptsURIsCreatedBy_toURI()
            throws Exception {

        if (isUnix()) {
            assertThatFileEqualsItself(new File("/some/path"));
            assertThatFileEqualsItself(new File("/some/path"));
            assertThatFileEqualsItself(new File("/some/path/"));
            assertThatFileEqualsItself(new File("/some/path #1"));
            assertThatFileEqualsItself(new File("/tmp/hezky česky"));
        }
        if (isWindows()) {
            assertThatFileEqualsItself(new File("C:\\some\\path #1"));
            assertThatFileEqualsItself(new File("C:\\some\\path"));
            assertThatFileEqualsItself(new File("C:\\some\\path"));
            assertThatFileEqualsItself(new File("C:\\some\\path\\"));
            assertThatFileEqualsItself(new File("C:\\tmp\\hezky česky"));
            assertThatFileEqualsItself(new File("\\\\server\\share\\path"));
            assertThatFileEqualsItself(new File("\\\\server\\share\\path\\"));
            assertThatFileEqualsItself(new File("\\\\server\\share\\path #1"));
        }
    }
    
    //--------------------------------------------------------------------------
    private void assertThatFileEqualsItself(final File file) {
        
        assertEquals(file, toFile(toURI(file)));
    }
    
    //--------------------------------------------------------------------------
    public void test_toURI_retunrURI_containingTrailingSlash()
            throws Exception {

        assertTrue(toURI(getWorkDir()).toString().endsWith("/"));
    }
    
    //--------------------------------------------------------------------------
    public void test_toURI_throwsNullPonter_whenArgumentIsNull()
            throws Exception {

        try {
            toURI(null);
            fail();
        } catch (final NullPointerException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    public void test_toURI_returnsURI_whenGivenCorrectPath()
          throws Exception {

        if (isUnix()) {
            assertEquals(new URI("file:/some/path"), toURI(new File("/some/path")));
            assertEquals(new URI("file:///some/path"), toURI(new File("/some/path")));
            assertEquals(new URI("file:/some/path"), toURI(new File("/some/path/")));
            assertEquals(new URI("file:/some/path%20%231"), toURI(new File("/some/path #1")));
            assertEquals(new URI("file:/tmp/hezky%20česky"), toURI(new File("/tmp/hezky česky")));
        }
        if (isWindows()) {
            assertEquals(new URI("file:/C:/some/path%20%231"), toURI(new File("C:\\some\\path #1")));
            assertEquals(new URI("file:/C:/some/path"), toURI(new File("C:\\some\\path")));
            assertEquals(new URI("file:///C:/some/path"), toURI(new File("C:\\some\\path")));
            assertEquals(new URI("file:/C:/some/path"), toURI(new File("C:\\some\\path\\")));
            assertEquals(new URI("file:/C:/tmp/hezky%20česky"), toURI(new File("C:\\tmp\\hezky česky")));
            assertEquals(new URI("file://server/share/path"), toURI(new File("\\\\server\\share\\path")));
            assertEquals(new URI("file://server/share/path"), toURI(new File("\\\\server\\share\\path\\")));
            assertEquals(new URI("file://server/share/path%20%231"), toURI(new File("\\\\server\\share\\path #1")));
        }
    }
    //--------------------------------------------------------------------------
    public void test_toURI_acceptsFilesCreatedBy_toFile()
            throws Exception {

        if (isUnix()) {
            assertThatURIEqualsItself(new URI("file:/some/path"));
            assertThatURIEqualsItself(new URI("file:///some/path"));
            assertEquals(new URI("file:/some/path"), toURI(toFile(new URI("file:/some/path/"))));
            assertThatURIEqualsItself(new URI("file:/some/path%20%231"));
            assertThatURIEqualsItself(new URI("file:/tmp/hezky%20česky"));
        }
        if (isWindows()) {
            assertThatURIEqualsItself(new URI("file:/C:/some/path"));
            assertThatURIEqualsItself(new URI("file:///C:/some/path"));
            assertEquals(new URI("file:/C:/some/path"), toURI(toFile(new URI("file:/C:/some/path/"))));
            assertThatURIEqualsItself(new URI("file:/C:/some/path%20%231"));
            assertThatURIEqualsItself(new URI("file:/C:/tmp/hezky%20česky"));
            assertThatURIEqualsItself(new URI("file://server/share/path"));
            assertEquals(new URI("file://server/share/path"), toURI(toFile((new URI("file:////server/share/path")))));
            assertEquals(new URI("file://server/share/path%20%231"), toURI(toFile(new URI("file:////server/share/path%20%231"))));
        }
    }
    
    //--------------------------------------------------------------------------
    private void assertThatURIEqualsItself(final URI uri) {
        
        assertEquals(uri, toURI(toFile(uri)));
    }
    
    //--------------------------------------------------------------------------
    public void test_toURI_producesURI_wchichResolvesAndWrapsProperly()
          throws Exception {

        URI uri = toURI(new File("c:\\dir", "some.jar"));
        URI resolvedURI = uri.resolve("some.jar");
        assertEquals(uri, resolvedURI);

        // these are supposed to test bugfix https://bz.apache.org/netbeans/show_bug.cgi?id=214131
        URI wrappedURI = new URI("jar:" + uri + "!/");
        URI wrappedResolvedURI = new URI("jar:" + resolvedURI + "!/");
        assertEquals(wrappedURI, wrappedResolvedURI);
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
    //--------------------------------------------------------------------------

    public void test_normalizeURI_ThrowsNullPointer_whenGivenNull()
            throws Exception {

        try {
            normalizeURI(null);
            fail();
        } catch (final NullPointerException e) {
            //good
        }
    }
    //--------------------------------------------------------------------------
    public void test_normalizeURI_ReturnsTheSameURI_whenNormalizationIsNotNeeded()
            throws Exception {

        assertEquals("file:////wsl$/Target/",
                normalizeURI(new URI("file:////wsl$/Target/")).toString());
    }
    //--------------------------------------------------------------------------
    public void test_normalizeURI_ReturnsNorMalizadURI()
            throws Exception {

        assertEquals("file:////wsl$/Target/",
                normalizeURI(new URI("file:////wsl$/Target/home/./../")).toString());
    }
    //--------------------------------------------------------------------------
    public void test_toObjectArray_throwsIllegalArgumentException_whenGivenNullArgument() {

        try {
            toObjectArray(null);
            fail();
        } catch (final IllegalArgumentException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    public void test_toObjectArray_returnsEmptyArray_whenGivenEmptyArray() {

        assertEquals(0, toObjectArray(new boolean[0]).length);

        assertEquals(0, toObjectArray(new char[0]).length);

        assertEquals(0, toObjectArray(new byte[0]).length);
        assertEquals(0, toObjectArray(new short[0]).length);
        assertEquals(0, toObjectArray(new int[0]).length);
        assertEquals(0, toObjectArray(new long[0]).length);

        assertEquals(0, toObjectArray(new float[0]).length);
        assertEquals(0, toObjectArray(new double[0]).length);
    }
    //--------------------------------------------------------------------------
    public void test_toObjectArray_returnsArrayOfObject_whenGivenArrayOfPrimitives() {

        Object[] result;
        
        result = toObjectArray(new boolean[]{true, false});
        assertTrue(result instanceof Boolean[]);
        assertEquals(2, result.length);
        assertEquals(TRUE, result[0]);
        assertEquals(FALSE, result[1]);

        result = toObjectArray(new char[]{'a', 'b'});
        assertTrue(result instanceof Character[]);
        assertEquals(2, result.length);
        assertEquals(new Character('a'), result[0]);
        assertEquals(new Character('b'), result[1]);
        
        result = toObjectArray(new byte[]{(byte)0, (byte)1});
        assertTrue(result instanceof Byte[]);
        assertEquals(2, result.length);
        assertEquals(new Byte((byte)0), result[0]);
        assertEquals(new Byte((byte)1), result[1]);
        
        result = toObjectArray(new short[]{(short)0, (short)1});
        assertTrue(result instanceof Short[]);
        assertEquals(2, result.length);
        assertEquals(new Short((short)0), result[0]);
        assertEquals(new Short((short)1), result[1]);
        
        result = toObjectArray(new int[]{0, 1});
        assertTrue(result instanceof Integer[]);
        assertEquals(2, result.length);
        assertEquals(new Integer(0), result[0]);
        assertEquals(new Integer(1), result[1]);
        
        result = toObjectArray(new long[]{(long)0, (long)1});
        assertTrue(result instanceof Long[]);
        assertEquals(2, result.length);
        assertEquals(new Long(0), result[0]);
        assertEquals(new Long(1), result[1]);
        
        result = toObjectArray(new float[]{(float)0.0, (float)1.0});
        assertTrue(result instanceof Float[]);
        assertEquals(2, result.length);
        assertEquals(new Float(0.0), result[0]);
        assertEquals(new Float(1.0), result[1]);
        
        result = toObjectArray(new double[]{0.0, 1.0});
        assertTrue(result instanceof Double[]);
        assertEquals(2, result.length);
        assertEquals(new Double(0.0), result[0]);
        assertEquals(new Double(1.0), result[1]);
    }
    
    //--------------------------------------------------------------------------
    public void test_toObjectArray_returnsTheArgument_whenGivenArrayOfObjects() {
        
        Object[] array = new Object[2];
        assertTrue(array == toObjectArray(array));
    }
    //--------------------------------------------------------------------------
    public void test_toPrimitiveArray_throwsIllegalArgumentException_whenGivenNullArgument() {

        try {
            toPrimitiveArray(null);
            fail();
        } catch (final IllegalArgumentException e) {
            //good
        }
    }
    //--------------------------------------------------------------------------
    public void test_toPrimitiveArray_returnsEmptyArray_whenGivenEmptyArray() {

        assertEquals(0, toObjectArray(new Boolean[0]).length);

        assertEquals(0, toObjectArray(new Character[0]).length);

        assertEquals(0, toObjectArray(new Byte[0]).length);
        assertEquals(0, toObjectArray(new Short[0]).length);
        assertEquals(0, toObjectArray(new Integer[0]).length);
        assertEquals(0, toObjectArray(new Long[0]).length);

        assertEquals(0, toObjectArray(new Float[0]).length);
        assertEquals(0, toObjectArray(new Double[0]).length);
    }
    //--------------------------------------------------------------------------
    public void test_toPrimitiveArray_returnsArrayOfPrimitives_whenGivenArrayOfObjects() {

        Object result;
        
        result = toPrimitiveArray(new Boolean[]{TRUE, null});
        assertTrue(result instanceof boolean[]);
        assertEquals(2, ((boolean[])result).length);
        assertEquals(true, ((boolean[])result)[0]);
        assertEquals(false, ((boolean[])result)[1]);

        result = toPrimitiveArray(new Character[]{new Character('a'), null});
        assertTrue(result instanceof char[]);
        assertEquals(2, ((char[])result).length);
        assertEquals('a', ((char[])result)[0]);
        assertEquals('\0', ((char[])result)[1]);
        
        result = toPrimitiveArray(new Byte[]{null, new Byte((byte)1)});
        assertTrue(result instanceof byte[]);
        assertEquals(2, ((byte[])result).length);
        assertEquals((byte)0, ((byte[])result)[0]);
        assertEquals((byte)1, ((byte[])result)[1]);
        
        result = toPrimitiveArray(new Short[]{null, new Short((short)1)});
        assertTrue(result instanceof short[]);
        assertEquals(2, ((short[])result).length);
        assertEquals((short)0, ((short[])result)[0]);
        assertEquals((short)1, ((short[])result)[1]);
        
        result = toPrimitiveArray(new Integer[]{null, new Integer(1)});
        assertTrue(result instanceof int[]);
        assertEquals(2, ((int[])result).length);
        assertEquals(0, ((int[])result)[0]);
        assertEquals(1, ((int[])result)[1]);
        
        result = toPrimitiveArray(new Long[]{null, new Long(1)});
        assertTrue(result instanceof long[]);
        assertEquals(2, ((long[])result).length);
        assertEquals(0, ((long[])result)[0]);
        assertEquals(1, ((long[])result)[1]);
        
        result = toPrimitiveArray(new Float[]{null, new Float(1.0)});
        assertTrue(result instanceof float[]);
        assertEquals(2, ((float[])result).length);
        assertEquals((float)0.0, ((float[])result)[0], 0.00001);
        assertEquals((float)1.0, ((float[])result)[1], 0.00001);
        
        result = toPrimitiveArray(new Double[]{null, new Double(1.0)});
        assertTrue(result instanceof double[]);
        assertEquals(2, ((double[])result).length);
        assertEquals(0.0, ((double[])result)[0], 0.00001);
        assertEquals(1.0, ((double[])result)[1], 0.00001);
    }
}
