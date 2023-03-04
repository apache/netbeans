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

package org.netbeans.modules.mercurial.ui.repository;

import java.lang.reflect.Method;
import java.net.URI;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Marian Petras
 */
public class HgURLTest {

    private Method isWindowsAbsolutePathMethod;
    private Method stripUserInfoFromInvalidUriMethod;
    private Method trimDupliciteInitialSlashesMethod;

    @After
    public void cleanup() {
        isWindowsAbsolutePathMethod = null;
        stripUserInfoFromInvalidUriMethod = null;
        trimDupliciteInitialSlashesMethod = null;
    }

    @Test
    public void testAddAuthenticationData() throws Exception {
        verifyAddAuthenticationMethod("http://server/path", "username", null,
                                      "http://username@server/path");

        verifyAddAuthenticationMethod("http://server/path", "username", "password",
                                      "http://username:password@server/path");

        verifyAddAuthenticationMethod("http://server/path", "username", "strange:passwd",
                                      "http://username:strange%3apasswd@server/path");

        verifyAddAuthenticationMethod("http://server/path", "\u20ac", null,
                                      "http://%e2%82%ac@server/path");

        verifyAddAuthenticationMethod("http://server/path", "username", "password",
                                      "http://username:password@server/path");

        verifyAddAuthenticationMethod("svn+http://svn.somewhere.com/svn/project_name/trunk", "username", "password",
                                      "svn+http://username:password@svn.somewhere.com/svn/project_name/trunk");

        verifyAddAuthenticationMethod("svn+https://svn.somewhere.com/svn/project_name/trunk", "username", "password",
                                      "svn+https://username:password@svn.somewhere.com/svn/project_name/trunk");

        verifyAddAuthenticationMethod("svn+ssh://svn.somewhere.com/svn/project_name/trunk", "username", "password",
                                      "svn+ssh://username:password@svn.somewhere.com/svn/project_name/trunk");

        verifyAddAuthenticationMethod("http://server/path", "ovrabec%40netbeans.org", null,
                                      "http://ovrabec%40netbeans.org@server/path");
    }

    @Test
    public void testIsWindowsAbsolutePath() throws Exception {
        initIsWindowsAbsolutePathMethod();

        testIsWindowsAbsolutePath("C",        false);
        testIsWindowsAbsolutePath("/",        false);
        testIsWindowsAbsolutePath("\\",       false);

        testIsWindowsAbsolutePath(":",        false);
        testIsWindowsAbsolutePath(":/",       false);
        testIsWindowsAbsolutePath(":\\",      false);

        testIsWindowsAbsolutePath("C",        false);
        testIsWindowsAbsolutePath("C/",       false);
        testIsWindowsAbsolutePath("C\\",      false);

        testIsWindowsAbsolutePath("C:",       false);
        testIsWindowsAbsolutePath("C:ahoj",   false);
        testIsWindowsAbsolutePath("C:/",      true);
        testIsWindowsAbsolutePath("C:/ahoj",  true);
        testIsWindowsAbsolutePath("C:\\",     true);
        testIsWindowsAbsolutePath("C:\\ahoj", true);

        testIsWindowsAbsolutePath("/C:",       false);
        testIsWindowsAbsolutePath("/C:ahoj",   false);
        testIsWindowsAbsolutePath("/C:/",      true);
        testIsWindowsAbsolutePath("/C:/ahoj",  true);
        testIsWindowsAbsolutePath("/C:\\",     true);
        testIsWindowsAbsolutePath("/C:\\ahoj", true);

        testIsWindowsAbsolutePath("\\C:",       false);
        testIsWindowsAbsolutePath("\\C:ahoj",   false);
        testIsWindowsAbsolutePath("\\C:/",      true);
        testIsWindowsAbsolutePath("\\C:/ahoj",  true);
        testIsWindowsAbsolutePath("\\C:\\",     true);
        testIsWindowsAbsolutePath("\\C:\\ahoj", true);

        testIsWindowsAbsolutePath("//C:",       false);
        testIsWindowsAbsolutePath("//C:ahoj",   false);
        testIsWindowsAbsolutePath("//C:/",      false);
        testIsWindowsAbsolutePath("//C:/ahoj",  false);
        testIsWindowsAbsolutePath("//C:\\",     false);
        testIsWindowsAbsolutePath("//C:\\ahoj", false);

        //small letters:
        testIsWindowsAbsolutePath("/c:",       false);
        testIsWindowsAbsolutePath("/c:ahoj",   false);
        testIsWindowsAbsolutePath("/c:/",      true);
        testIsWindowsAbsolutePath("/c:/ahoj",  true);
        testIsWindowsAbsolutePath("/c:\\",     true);
        testIsWindowsAbsolutePath("/c:\\ahoj", true);
    }

    private void verifyAddAuthenticationMethod(String original,
                                               String username,
                                               String password,
                                               String withAuthentication)
                                                            throws Exception {
        assertEquals(withAuthentication,
                     addAuthenticationData(original, username, password));
        System.out.println("Parsing URI " + withAuthentication + ":");
        URI uri = new URI(withAuthentication);
        System.out.println("   scheme: " + uri.getScheme());
        System.out.println("   authority: " + uri.getAuthority());
        System.out.println("   path: " + uri.getPath());
        System.out.println();
    }

    private String addAuthenticationData(String urlString,
                                         String username,
                                         String password) throws Exception {
        return new HgURL(urlString, username, password == null ? null : password.toCharArray()).toHgCommandUrlString();
    }

    private void testIsWindowsAbsolutePath(String path, boolean expected) throws Exception {
        assert isWindowsAbsolutePathMethod != null;

        Object resultObj = isWindowsAbsolutePathMethod.invoke(null, path);
        assert resultObj instanceof Boolean;
        assertTrue(Boolean.TRUE.equals(resultObj) == expected);
    }

    @Test
    public void testStripUserInfoFromInvalidUri() throws Exception {
        initStripUserInfoFromInvalidUriMethod();

        assertEquals("", stripUserInfoFromInvalidURI(""));
        assertEquals("a", stripUserInfoFromInvalidURI("a"));
        assertEquals("abcd", stripUserInfoFromInvalidURI("abcd"));
        assertEquals("abcd:", stripUserInfoFromInvalidURI("abcd:"));
        assertEquals(":efgh", stripUserInfoFromInvalidURI(":efgh"));
        assertEquals("abcd:efgh", stripUserInfoFromInvalidURI("abcd:efgh"));
        assertEquals("world", stripUserInfoFromInvalidURI("hello@world"));
        assertEquals("abcd:/kuku", stripUserInfoFromInvalidURI("abcd:/kuku"));
        assertEquals("hehe",  stripUserInfoFromInvalidURI(":kuku@hehe"));
        assertEquals("hehe/", stripUserInfoFromInvalidURI(":kuku@hehe/"));
        assertEquals("hehe",  stripUserInfoFromInvalidURI("://kuku@hehe"));
        assertEquals("//hehe",  stripUserInfoFromInvalidURI("//kuku@hehe"));
        assertEquals("kuku:tam",   stripUserInfoFromInvalidURI("kuku:hehe@tam"));
        assertEquals("http://s",   stripUserInfoFromInvalidURI("http://h:p@s"));
        assertEquals("http://s/p", stripUserInfoFromInvalidURI("http://h:p@s/p"));
        assertEquals("file:",  stripUserInfoFromInvalidURI("file:"));
        assertEquals("file:/", stripUserInfoFromInvalidURI("file:/"));
        assertEquals("file:thefile",      stripUserInfoFromInvalidURI("file:thefile"));
        assertEquals("file:thefile.txt",  stripUserInfoFromInvalidURI("file:thefile.txt"));
        assertEquals("file:/thefile",     stripUserInfoFromInvalidURI("file:/thefile"));
        assertEquals("file:/thefile.txt", stripUserInfoFromInvalidURI("file:/thefile.txt"));
        assertEquals("file://thefile",     stripUserInfoFromInvalidURI("file://thefile"));
        assertEquals("file://thefile.txt", stripUserInfoFromInvalidURI("file://thefile.txt"));
        assertEquals("file:///thefile",     stripUserInfoFromInvalidURI("file:///thefile"));
        assertEquals("file:///thefile.txt", stripUserInfoFromInvalidURI("file:///thefile.txt"));
    }

    @Test
    public void testBug163731() throws Exception {
        initStripUserInfoFromInvalidUriMethod();

        assertEquals("http://server/path", stripUserInfoFromInvalidURI("http://user:password@with-at-sign@server/path"));
    }

    private String stripUserInfoFromInvalidURI(String invalidUri) throws Exception {
        assert stripUserInfoFromInvalidUriMethod != null;
        return (String) stripUserInfoFromInvalidUriMethod.invoke(null, invalidUri);
    }

    @Test
    public void testTrimDupliciteInitialSlashes() throws Exception {
        initTrimDupliciteInitialSlashesMethod();

        assertEquals("",        trimDupliciteInitialSlashes(""));
        assertEquals("a",       trimDupliciteInitialSlashes("a"));
        assertEquals("ab",      trimDupliciteInitialSlashes("ab"));
        assertEquals("abc",     trimDupliciteInitialSlashes("abc"));
        assertEquals("a/",      trimDupliciteInitialSlashes("a/"));
        assertEquals("a/b",     trimDupliciteInitialSlashes("a/b"));
        assertEquals("a//",     trimDupliciteInitialSlashes("a//"));
        assertEquals("a//b",    trimDupliciteInitialSlashes("a//b"));
        assertEquals("a/b/c",   trimDupliciteInitialSlashes("a/b/c"));
        assertEquals("/",       trimDupliciteInitialSlashes("/"));
        assertEquals("/a",      trimDupliciteInitialSlashes("/a"));
        assertEquals("/a/",     trimDupliciteInitialSlashes("/a/"));
        assertEquals("/aa",     trimDupliciteInitialSlashes("/aa"));
        assertEquals("/aa/",    trimDupliciteInitialSlashes("/aa/"));
        assertEquals("/a/b/",   trimDupliciteInitialSlashes("/a/b/"));
        assertEquals("/",       trimDupliciteInitialSlashes("//"));
        assertEquals("/a",      trimDupliciteInitialSlashes("//a"));
        assertEquals("/ab",     trimDupliciteInitialSlashes("//ab"));
        assertEquals("/ab/",    trimDupliciteInitialSlashes("//ab/"));
        assertEquals("/a//",    trimDupliciteInitialSlashes("//a//"));
        assertEquals("/ab//",   trimDupliciteInitialSlashes("//ab//"));
        assertEquals("/a/b",    trimDupliciteInitialSlashes("//a/b"));
        assertEquals("/a//b",   trimDupliciteInitialSlashes("//a//b"));
        assertEquals("/",       trimDupliciteInitialSlashes("///"));
        assertEquals("/a",      trimDupliciteInitialSlashes("///a"));
        assertEquals("/ab",     trimDupliciteInitialSlashes("///ab"));
        assertEquals("/abc",    trimDupliciteInitialSlashes("///abc"));
        assertEquals("/abc///", trimDupliciteInitialSlashes("///abc///"));
    }

    private String trimDupliciteInitialSlashes(String text) throws Exception {
        assert trimDupliciteInitialSlashesMethod != null;
        return (String) trimDupliciteInitialSlashesMethod.invoke(null, text);
    }

    private void initIsWindowsAbsolutePathMethod() throws Exception {
        isWindowsAbsolutePathMethod = HgURL.class.getDeclaredMethod("isWindowsAbsolutePath", String.class);
        isWindowsAbsolutePathMethod.setAccessible(true);
    }

    private void initStripUserInfoFromInvalidUriMethod() throws Exception {
       stripUserInfoFromInvalidUriMethod = HgURL.class.getDeclaredMethod("stripUserInfoFromInvalidURI", String.class);
       stripUserInfoFromInvalidUriMethod.setAccessible(true);
    }

    private void initTrimDupliciteInitialSlashesMethod() throws Exception {
        trimDupliciteInitialSlashesMethod = HgURL.class.getDeclaredMethod("trimDupliciteInitialSlashes", String.class);
        trimDupliciteInitialSlashesMethod.setAccessible(true);
    }

}
