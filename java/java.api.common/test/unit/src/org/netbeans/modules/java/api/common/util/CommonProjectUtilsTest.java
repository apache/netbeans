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
package org.netbeans.modules.java.api.common.util;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.libraries.LibraryFactory;

/**
 *
 * @author Tomas Zezula
 */
public class CommonProjectUtilsTest extends NbTestCase {

    public CommonProjectUtilsTest(@NonNull final String name) {
        super(name);
    }

    public void testCreateJavaLibraryImplementation() throws IOException {
        final String name = "test-lib"; //NOI18N
        final URL[] classpath = { new URL("file:///a/cp/cp1.jar"), new URL("file:///a/cp/b/cp2.jar")};  //NOI18N
        final URL[] src = { new URL("file:///a/src/x/src1/"), new URL("file:///a/srcplus/src2.zip"), new URL("file:///a/src/src3")};    //NOI18N
        final URL[] javadoc = { new URL("http://example.com/my/remote/javadoc")};   //NOI18N
        final String[] mavendeps = {
            "com.sun.jersey.contribs:jersey-multipart:1.8:jar",         //NOI18N
            "com.sun.jersey.contribs:jersey-guice:1.8:jar",             //NOI18N
            "com.sun.jersey.contribs:jersey-simple-server:1.8:jar",     //NOI18N
            "com.sun.jersey.contribs.jersey-oauth:oauth-client:1.8:jar" //NOI18N
        };
        final String[] mavenrepos = {"default:http://download.eclipse.org/rt/eclipselink/maven.repo/"}; //NOI18N
        final Library lib = LibraryFactory.createLibrary(CommonProjectUtils.createJavaLibraryImplementation(
                name,
                classpath,
                src,
                javadoc,
                mavendeps,
                mavenrepos));
        assertJavaLibrary(name, classpath, src, javadoc, mavendeps, mavenrepos, lib);
    }


    private void assertJavaLibrary(
            @NonNull final String expectedName,
            @NonNull final URL[] expectedClassPath,
            @NonNull final URL[] expectedSrc,
            @NonNull final URL[] expectedJavadoc,
            @NonNull final String[] expectedMavenDeps,
            @NonNull final String[] expectedMavenRepos,
            @NonNull final Library lib) {
        assertEquals("Names are not equal", expectedName, lib.getName());   //NOI18N
        assertEquals("ClassPaths are not equal", Arrays.asList(expectedClassPath), lib.getContent("classpath"));    //NOI18N
        assertEquals("Sources are not equal", Arrays.asList(expectedSrc), lib.getContent("src"));    //NOI18N
        assertEquals("Javadocs are not equal", Arrays.asList(expectedJavadoc), lib.getContent("javadoc"));    //NOI18N
        assertEquals(
                "MavenDeps are not equal",          //NOI18N
                Arrays.asList(expectedMavenDeps),
                Arrays.asList(lib.getProperties().get("maven-dependencies").split("\\s"))); //NOI18N
        assertEquals(
                "MavenRepositories are not equal",          //NOI18N
                Arrays.asList(expectedMavenRepos),
                Arrays.asList(lib.getProperties().get("maven-repositories").split("\\s"))); //NOI18N

    }
}
