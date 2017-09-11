/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
