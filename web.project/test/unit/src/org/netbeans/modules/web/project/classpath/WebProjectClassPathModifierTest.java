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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.project.classpath;

import java.io.File;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.test.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Test for {@link WebProjectClassPathModifier}.
 * @author tmysik
 */
public class WebProjectClassPathModifierTest extends NbTestCase {
    
    private FileObject scratch;
    
    public WebProjectClassPathModifierTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances();
        scratch = TestUtil.makeScratchDir(this);
    }

    // #113390
    public void testRemoveRoots() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        FileObject projdir = FileUtil.toFileObject(f);
        WebProject webProject = (WebProject) ProjectManager.getDefault().findProject(projdir);
        
        Sources sources = ProjectUtils.getSources(webProject);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject srcJava = webProject.getSourceRoots().getRoots()[0];
        assertEquals("We should edit sources", "${src.dir}", groups[0].getName());
        String classPathProperty = webProject.getClassPathProvider().getPropertyName(groups[0], ClassPath.COMPILE)[0];
        
        AntProjectHelper helper = webProject.getAntProjectHelper();
        
        // create src folder
        final String srcFolder = "srcFolder";
        File folder = new File(getDataDir().getAbsolutePath(), srcFolder);
        if (folder.exists()) {
            folder.delete();
        }
        FileUtil.createFolder(folder);
        URL[] cpRoots = new URL[]{folder.toURL()};
        
        // init
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String cpProperty = props.getProperty(classPathProperty);
        boolean alreadyOnCp = cpProperty.indexOf(srcFolder) != -1;
        //assertFalse("srcFolder should not be on cp", alreadyInCp);
        
        // add
        boolean addRoots = ProjectClassPathModifier.addRoots(cpRoots, srcJava, ClassPath.COMPILE);
        // we do not check this - it can be already on cp (tests are created only before the 1st test starts)
        if (!alreadyOnCp) {
            assertTrue(addRoots);
        }
        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        cpProperty = props.getProperty(classPathProperty);
        assertTrue("srcFolder should be on cp", cpProperty.indexOf(srcFolder) != -1);
        
        // simulate #113390
        folder.delete();
        assertFalse("srcFolder should not exist.", folder.exists());
        
        // remove
        boolean removeRoots = ProjectClassPathModifier.removeRoots(cpRoots, srcJava, ClassPath.COMPILE);
        assertTrue(removeRoots);
        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        cpProperty = props.getProperty(classPathProperty);
        assertTrue("srcFolder should not be on cp", cpProperty.indexOf(srcFolder) == -1);
    }
}
