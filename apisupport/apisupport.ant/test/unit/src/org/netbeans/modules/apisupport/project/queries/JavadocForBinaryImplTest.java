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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileUtil;

/**
 * Test {@link JavadocForBinaryImpl}.
 *
 * @author Jesse Glick
 */
public class JavadocForBinaryImplTest extends TestBase {

    static {
        JavadocForBinaryImpl.ignoreNonexistentRoots = false;
    }
    
    private File suite2, suite3;
    
    public JavadocForBinaryImplTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        suite2 = resolveEEPFile("suite2");
        suite3 = resolveEEPFile("suite3");
    }
    
    public void testJavadocForNetBeansOrgModules() throws Exception {
        // Have to load at least one module to get the scan going.
        ClassPath.getClassPath(nbRoot().getFileObject("java/classfile/src"), ClassPath.COMPILE);
        File classfileJar = file("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/modules/org-netbeans-modules-classfile.jar");
        URL[] roots = JavadocForBinaryQuery.findJavadoc(FileUtil.urlForArchiveOrDir(classfileJar)).getRoots();
        URL[] expectedRoots = {
            FileUtil.urlForArchiveOrDir(file("nbbuild/build/javadoc/org-netbeans-modules-classfile")),
            urlForJar(apisZip, "org-netbeans-modules-classfile/"),
        };
        assertEquals("correct Javadoc roots for classfile", urlSet(expectedRoots), urlSet(roots));
    }
    
    public void testJavadocForExternalModules() throws Exception {
        ClassPath.getClassPath(resolveEEP("/suite2/misc-project/src"), ClassPath.COMPILE);
        File miscJar = resolveEEPFile("/suite2/build/cluster/modules/org-netbeans-examples-modules-misc.jar");
        URL[] roots = JavadocForBinaryQuery.findJavadoc(FileUtil.urlForArchiveOrDir(miscJar)).getRoots();
        URL[] expectedRoots = new URL[] {
            FileUtil.urlForArchiveOrDir(file(suite2, "misc-project/build/javadoc/org-netbeans-examples-modules-misc")),
            // It is inside ${netbeans.home}/.. so read this.
            urlForJar(apisZip, "org-netbeans-examples-modules-misc/"),
        };
        assertEquals("correct Javadoc roots for misc", urlSet(expectedRoots), urlSet(roots));
        ClassPath.getClassPath(resolveEEP("/suite3/dummy-project/src"), ClassPath.COMPILE);
        File dummyJar = file(suite3, "dummy-project/build/cluster/modules/org-netbeans-examples-modules-dummy.jar");
        roots = JavadocForBinaryQuery.findJavadoc(FileUtil.urlForArchiveOrDir(dummyJar)).getRoots();
        expectedRoots = new URL[] {
            FileUtil.urlForArchiveOrDir(file(suite3, "dummy-project/build/javadoc/org-netbeans-examples-modules-dummy")),
        };
        assertEquals("correct Javadoc roots for dummy", urlSet(expectedRoots), urlSet(roots));
    }
    
    private static URL urlForJar(File jar, String path) throws Exception {
        return new URL(FileUtil.urlForArchiveOrDir(jar), path);
    }
    
    private static SortedSet<String> urlSet(URL[] urls) {
        SortedSet<String> set = new TreeSet<String>();
        for (URL url : urls) {
            set.add(url.toExternalForm());
        }
        return set;
    }
    
}
