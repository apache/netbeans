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

package org.netbeans.modules.java.platform.queries;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.platform.TestJavaPlatformProvider;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Utilities;

/**
 * @author Tomas Zezula
 */
public class PlatformSourceForBinaryQueryTest extends NbTestCase {

    public PlatformSourceForBinaryQueryTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockServices.setServices(
            PlatformSourceForBinaryQuery.class,
            TestJavaPlatformProvider.class);
    }

    public void testUnregisteredPlatform() throws Exception {
        File wd = getWorkDir();
        FileObject wdo = FileUtil.toFileObject(wd);
        assertNotNull(wdo);
        FileObject p1 = wdo.createFolder("platform1");
        FileObject fo = p1.createFolder("jre");
        fo = fo.createFolder("lib");
        FileObject rt1 = fo.createData("rt.jar");
        FileObject src1 = FileUtil.getArchiveRoot(createSrcZip (p1));

        FileObject p2 = wdo.createFolder("platform2");
        fo = p2.createFolder("jre");
        fo = fo.createFolder("lib");
        FileObject rt2 = fo.createData("rt.jar");

        PlatformSourceForBinaryQuery q = new PlatformSourceForBinaryQuery ();

        SourceForBinaryQuery.Result result = q.findSourceRoots(FileUtil.getArchiveRoot(rt1.getURL()));
        assertEquals(1, result.getRoots().length);
        assertEquals(src1, result.getRoots()[0]);

        result = q.findSourceRoots(FileUtil.getArchiveRoot(rt2.getURL()));
        assertNull(result);
    }

    public void testTwoPlatformsoverSameSDKSourcesChange() throws Exception {
        final File binDir = new File(getWorkDir(),"boot");  //NOI18N
        binDir.mkdir();
        final File jdocFile1 = new File(getWorkDir(),"src1");   //NOI18N
        jdocFile1.mkdir();
        final File jdocFile2 = new File(getWorkDir(),"src2");  //NOI18N
        jdocFile2.mkdir();
        final TestJavaPlatformProvider provider = TestJavaPlatformProvider.getDefault();
        provider.reset();
        final URL binRoot = Utilities.toURI(binDir).toURL();
        final ClassPath bootCp = ClassPathSupport.createClassPath(binRoot);
        final ClassPath src1 = ClassPathSupport.createClassPath(Utilities.toURI(jdocFile1).toURL());
        final ClassPath src2 = ClassPathSupport.createClassPath(Utilities.toURI(jdocFile2).toURL());
        final TestJavaPlatform platform1 = new TestJavaPlatform("platform1", bootCp);   //NOI18N
        final TestJavaPlatform platform2 = new TestJavaPlatform("platform2", bootCp);   //NOI18N
        platform2.setSources(src2);
        provider.addPlatform(platform1);
        provider.addPlatform(platform2);

        final SourceForBinaryQuery.Result result1 = SourceForBinaryQuery.findSourceRoots(binRoot);
        assertEquals(Arrays.asList(src2.getRoots()), Arrays.asList(result1.getRoots()));

        platform1.setSources(src1);
        assertEquals(Arrays.asList(src1.getRoots()), Arrays.asList(result1.getRoots()));

        final SourceForBinaryQuery.Result result2 = SourceForBinaryQuery.findSourceRoots(binRoot);
        assertEquals(Arrays.asList(src1.getRoots()), Arrays.asList(result2.getRoots()));

        platform1.setSources(ClassPath.EMPTY);
        assertEquals(Arrays.asList(src2.getRoots()), Arrays.asList(result1.getRoots()));
        assertEquals(Arrays.asList(src2.getRoots()), Arrays.asList(result2.getRoots()));
    }

    public void testTwoPlatformsoverSameSDKPlatformChange() throws Exception {
        final File binDir = new File(getWorkDir(),"boot");  //NOI18N
        binDir.mkdir();
        final File jdocFile1 = new File(getWorkDir(),"src1");   //NOI18N
        jdocFile1.mkdir();
        final File jdocFile2 = new File(getWorkDir(),"src2");  //NOI18N
        jdocFile2.mkdir();
        final TestJavaPlatformProvider provider = TestJavaPlatformProvider.getDefault();
        provider.reset();
        final URL binRoot = Utilities.toURI(binDir).toURL();
        final ClassPath bootCp = ClassPathSupport.createClassPath(binRoot);
        final ClassPath src1 = ClassPathSupport.createClassPath(Utilities.toURI(jdocFile1).toURL());
        final ClassPath src2 = ClassPathSupport.createClassPath(Utilities.toURI(jdocFile2).toURL());
        final TestJavaPlatform platform1 = new TestJavaPlatform("platform1", bootCp);   //NOI18N
        final TestJavaPlatform platform2 = new TestJavaPlatform("platform2", bootCp);   //NOI18N
        platform1.setSources(src1);
        platform2.setSources(src2);
        provider.addPlatform(platform1);
        provider.addPlatform(platform2);

        final SourceForBinaryQuery.Result result1 = SourceForBinaryQuery.findSourceRoots(binRoot);
        assertEquals(Arrays.asList(src1.getRoots()), Arrays.asList(result1.getRoots()));

        provider.removePlatform(platform1);
        assertEquals(Arrays.asList(src2.getRoots()), Arrays.asList(result1.getRoots()));

        final SourceForBinaryQuery.Result result2 = SourceForBinaryQuery.findSourceRoots(binRoot);
        assertEquals(Arrays.asList(src2.getRoots()), Arrays.asList(result2.getRoots()));

        provider.insertPlatform(platform2, platform1);
        assertEquals(Arrays.asList(src1.getRoots()), Arrays.asList(result1.getRoots()));
        assertEquals(Arrays.asList(src1.getRoots()), Arrays.asList(result2.getRoots()));
    }
    

    private static FileObject createSrcZip (FileObject pf) throws Exception {
        return TestFileUtils.writeZipFile(pf, "src.zip", "Test.java:class Test {}");
    }

}
