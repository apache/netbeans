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

package org.netbeans.modules.java.j2seproject.queries;

import java.net.URL;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.SourceRootsTest;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.test.MockLookup;

/**
 * Tests for UnitTestForSourceQueryImpl
 *
 * @author David Konecny
 */
public class UnitTestForSourceQueryImplTest extends NbTestCase {
    
    public UnitTestForSourceQueryImplTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private ProjectManager pm;
    private FileObject sources;
    private FileObject tests;
    private AntProjectHelper helper;

    Project pp;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances(
            new org.netbeans.modules.java.project.UnitTestForSourceQueryImpl(),
            new org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation()
        );
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null, false);
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        sources = projdir.getFileObject("src");
        tests = projdir.getFileObject("test");
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);        
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        super.tearDown();
    }
    
    @SuppressWarnings("deprecation")
    public void testFindUnitTest() throws Exception {
        URL u = UnitTestForSourceQuery.findUnitTest(projdir);
        assertNull(u);
        
        u = UnitTestForSourceQuery.findUnitTest(sources);
        assertNotNull(u);
        URL result = URLMapper.findURL(tests, URLMapper.EXTERNAL);
        assertNotNull(result);
        assertEquals(result, u);
        
        u = UnitTestForSourceQuery.findSource(tests);
        assertNotNull(u);
        result = URLMapper.findURL(sources, URLMapper.EXTERNAL);
        assertNotNull(result);
        assertEquals(result, u);
        
        //Test the case when the tests folder does not exist
        result = tests.toURL();
        tests.delete();
        u = UnitTestForSourceQuery.findUnitTest (sources);
        assertEquals (result, u);
    }

    public void testFindUnitTestMultiRoots () throws Exception {
        FileObject newRoot = SourceRootsTest.addSourceRoot(helper,projdir,"src.other.dir","other");
        URL[] urls = UnitTestForSourceQuery.findSources(tests);
        assertNotNull(urls);
        assertEquals(2,urls.length);
        assertEquals(sources.toURL(), urls[0]);
        assertEquals(newRoot.toURL(), urls[1]);
    }

}
