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
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * Test functionality of SimpleAntArtifact.
 * @author Jesse Glick
 */
public class SimpleAntArtifactTest extends NbTestCase {
    
    public SimpleAntArtifactTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject sisterprojdir;
    private ProjectManager pm;
    private AntProjectHelper sisterh;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
        pm = ProjectManager.getDefault();
        sisterprojdir = FileUtil.createFolder(scratch, "proj2");
        sisterh = ProjectGenerator.createProject(sisterprojdir, "test");
        EditableProperties props = sisterh.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "build/proj2.jar");
        props.setProperty("build.jar.absolute", getWorkDir().getAbsolutePath()+"/build/proj3.jar");
        sisterh.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    }
    
    protected void tearDown() throws Exception {
        scratch = null;
        sisterprojdir = null;
        sisterh = null;
        pm = null;
        super.tearDown();
    }
    
    /**
     * Check that {@link SimpleAntArtifact} works as documented.
     */
    public void testSimpleAntArtifact() throws Exception {
        AntArtifact art = sisterh.createSimpleAntArtifact("jar", "build.jar", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        assertEquals("correct type", "jar", art.getType());
        assertEquals("correct target name", "dojar", art.getTargetName());
        assertEquals("correct clean target name", "clean", art.getCleanTargetName());
        assertEquals("correct artifact location", URI.create("build/proj2.jar"), art.getArtifactLocations()[0]);
        assertEquals("no artifact file yet", 0, art.getArtifactFiles().length);
        FileObject artfile = FileUtil.createData(sisterprojdir, "build/proj2.jar");
        assertEquals("now have an artifact file", Collections.singletonList(artfile), Arrays.asList(art.getArtifactFiles()));
        assertEquals("correct script location", new File(FileUtil.toFile(sisterprojdir), "build.xml"), art.getScriptLocation());
        assertEquals("no script file yet", null, art.getScriptFile());
        FileObject scriptfile = FileUtil.createData(sisterprojdir, "build.xml");
        assertEquals("now have a script file", scriptfile, art.getScriptFile());
        assertEquals("correct project", pm.findProject(sisterprojdir), art.getProject());
        
        art = sisterh.createSimpleAntArtifact("jar", "build.jar.absolute", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        assertEquals("correct artifact location", Utilities.toURI((new File(getWorkDir().getAbsolutePath()+"/build/proj3.jar"))), art.getArtifactLocations()[0]);
    }
    
}
