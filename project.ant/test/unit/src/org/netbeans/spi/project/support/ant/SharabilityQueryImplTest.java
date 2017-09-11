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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.net.URI;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * Test functionality of SharabilityQueryImpl.
 * @author Jesse Glick
 */
public class SharabilityQueryImplTest extends NbTestCase {

    public SharabilityQueryImplTest(String name) {
        super(name);
    }
    
    /** Location of top of testing dir (contains projdir and external). */
    private File scratchF;
    /** Tested impl. */
    private SharabilityQueryImplementation2 sqi;
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
        FileObject scratch = TestUtil.makeScratchDir(this);
        scratchF = FileUtil.toFile(scratch);
        FileObject projdir = scratch.createFolder("projdir");
        AntProjectHelper h = ProjectGenerator.createProject(projdir, "test");
        assertEquals("right project directory", projdir, h.getProjectDirectory());
        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.dir", "build");
        props.setProperty("build2.dir", "build/2");
        props.setProperty("dist.dir", "dist");
        props.setProperty("src.dir", "src");
        File externalF = new File(scratchF, "external");
        props.setProperty("src2.dir", new File(externalF, "src").getAbsolutePath());
        props.setProperty("build3.dir", new File(externalF, "build").getAbsolutePath());
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(projdir));
        sqi = h.createSharabilityQuery2(h.getStandardPropertyEvaluator(), new String[] {"${src.dir}", "${src2.dir}"},
                                       new String[] {"${build.dir}", "${build2.dir}", "${build3.dir}", "${dist.dir}"});
    }
    
    private URI file(String path) {
        return Utilities.toURI(new File(scratchF, path.replace('/', File.separatorChar)));
    }
    
    public void testBasicIncludesExcludes() throws Exception {
        assertEquals("project directory is mixed", SharabilityQuery.Sharability.MIXED, sqi.getSharability(file("projdir")));
        assertEquals("build.xml is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/build.xml")));
        assertEquals("src/ is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/src")));
        assertEquals("src/org/foo/ is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/src/org/foo")));
        assertEquals("src/org/foo/Foo.java is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/src/org/foo/Foo.java")));
        assertEquals("nbproject/ is mixed", SharabilityQuery.Sharability.MIXED, sqi.getSharability(file("projdir/nbproject")));
        assertEquals("nbproject/project.xml is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/nbproject/project.xml")));
        assertEquals("nbproject/private/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/nbproject/private")));
        assertEquals("nbproject/private/private.properties is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/nbproject/private/private.properties")));
        assertEquals("build/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build")));
        assertEquals("build/classes/org/foo/Foo.class is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build/classes/org/foo/Foo.class")));
        assertEquals("dist/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/dist")));
    }
    
    public void testOverlaps() throws Exception {
        assertEquals("build/2/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build/2")));
        assertEquals("build/2/whatever is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build/2/whatever")));
        // overlaps in includePaths tested in basicIncludesExcludes: src is inside projdir
    }
    
    public void testExternalFiles() throws Exception {
        assertEquals("external/src is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("external/src")));
        assertEquals("external/src/org/foo/Foo.java is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("external/src/org/foo/Foo.java")));
        assertEquals("external/build is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("external/build")));
        assertEquals("external/build/classes/org/foo/Foo.class is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("external/build/classes/org/foo/Foo.class")));
    }
    
    public void testUnknownFiles() throws Exception {
        assertEquals("some other dir is unknown", SharabilityQuery.Sharability.UNKNOWN, sqi.getSharability(file("something")));
        assertEquals("some other file is unknown", SharabilityQuery.Sharability.UNKNOWN, sqi.getSharability(file("something/else")));
        assertEquals("external itself is unknown", SharabilityQuery.Sharability.UNKNOWN, sqi.getSharability(file("external")));
    }
    
    public void testDirNamesEndingInSlash() throws Exception {
        assertEquals("project directory is mixed", SharabilityQuery.Sharability.MIXED, sqi.getSharability(file("projdir/")));
        assertEquals("src/ is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/src/")));
        assertEquals("src/org/foo/ is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/src/org/foo/")));
        assertEquals("nbproject/ is mixed", SharabilityQuery.Sharability.MIXED, sqi.getSharability(file("projdir/nbproject/")));
        assertEquals("nbproject/private/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/nbproject/private/")));
        assertEquals("build/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build/")));
        assertEquals("dist/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/dist/")));
        assertEquals("build/2/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build/2/")));
        assertEquals("some other dir is unknown", SharabilityQuery.Sharability.UNKNOWN, sqi.getSharability(file("something/")));
        assertEquals("external itself is unknown", SharabilityQuery.Sharability.UNKNOWN, sqi.getSharability(file("external/")));
        assertEquals("external/src is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("external/src/")));
        assertEquals("external/build is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("external/build/")));
    }
    
    public void testSubprojectFiles() throws Exception {
        assertEquals("nbproject/private from a subproject is sharable as far as this impl is concerned", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/subproj/nbproject/private")));
    }
    
    // XXX testChangedProperties
    // XXX testExternalSourceDirs
    
}
