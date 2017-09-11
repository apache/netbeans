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
        ClassPath.getClassPath(nbRoot().getFileObject("classfile/src"), ClassPath.COMPILE);
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
