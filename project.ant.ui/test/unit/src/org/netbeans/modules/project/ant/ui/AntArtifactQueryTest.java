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

package org.netbeans.modules.project.ant.ui;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/* XXX tests needed:
 * - testFindArtifactFromNonexistentFile
 * like testFindArtifactFromFile but file does not yet exist on disk
 * - testFindArtifactByTarget
 * - testFindArtifactsByType
 */

/**
 * Test functionality of AntArtifactQuery, StandardAntArtifactQueryImpl, etc.
 * @author Jesse Glick
 */
public class AntArtifactQueryTest extends NbTestCase {
    
    public AntArtifactQueryTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sisterprojdir;
    private FileObject dummyprojdir;
    private ProjectManager pm;
    
    protected @Override void setUp() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Services");
        if (fo != null) {
            fo.delete();
        }

        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType(), TestUtil.testProjectFactory());
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        ProjectGenerator.createProject(projdir, "test");
        pm = ProjectManager.getDefault();
        sisterprojdir = FileUtil.createFolder(scratch, "proj2");
        AntProjectHelper sisterh = ProjectGenerator.createProject(sisterprojdir, "test");
        EditableProperties props = sisterh.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "dist/proj2.jar");
        props.setProperty("build.javadoc", "build/javadoc");
        sisterh.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        dummyprojdir = scratch.createFolder("dummy");
        dummyprojdir.createFolder("testproject");
    }

    protected @Override void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        sisterprojdir = null;
        pm = null;
        super.tearDown();
    }
    
    public void testFindArtifactFromFile() throws Exception {
        FileObject proj2JarFO = FileUtil.createData(sisterprojdir, "dist/proj2.jar");
        File proj2Jar = FileUtil.toFile(proj2JarFO);
        assertNotNull("have dist/proj2.jar on disk", proj2Jar);
        AntArtifact art = AntArtifactQuery.findArtifactFromFile(proj2Jar);
        assertNotNull("found an artifact matching " + proj2Jar, art);
        assertEquals("correct project", pm.findProject(sisterprojdir), art.getProject());
        assertEquals("correct artifact file", proj2JarFO, art.getArtifactFiles()[0]);
        assertEquals("correct target name", "dojar", art.getTargetName());
        assertEquals("correct clean target name", "clean", art.getCleanTargetName());
        assertEquals("correct type", "jar", art.getType());
        assertEquals("correct script location", new File(FileUtil.toFile(sisterprojdir), "build.xml"), art.getScriptLocation());
    }
    
    public void testFindArtifactsByType() throws Exception {
        Project p = pm.findProject(projdir);
        assertNotNull("have a project in " + projdir, p);
        AntArtifact[] arts = AntArtifactQuery.findArtifactsByType(p, "jar");
        assertEquals("one JAR artifact", 1, arts.length);
        assertEquals("correct project", p, arts[0].getProject());
        assertEquals("correct target name", "dojar", arts[0].getTargetName());
        p = pm.findProject(dummyprojdir);
        assertNotNull("have a dummy project in " + dummyprojdir, p);
        arts = AntArtifactQuery.findArtifactsByType(p, "jar");
        assertEquals("no JAR artifacts", 0, arts.length);
    }
    
}
