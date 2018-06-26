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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestBase;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

// XXX much more to test

/**
 * Test {@link EjbJarActionProvider}.
 *
 * @author Martin Krauskopf, Andrei Badea
 */
public class EjbJarActionProviderTest extends NbTestCase {
    
    private Project project;
    private ActionProvider ap;
    
    public EjbJarActionProviderTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        ap = project.getLookup().lookup(ActionProvider.class);
        assertNotNull("have ActionProvider", ap);
    }
    
    public void testDebugSingle() throws Exception { // #72733
        FileObject test = project.getProjectDirectory().getFileObject("test/pkg/NewClassTest.java");
        assertNotNull("have test/pkg/NewClassTest.java", test);
        assertTrue("Debug File is enabled on test", ap.isActionEnabled(
                ActionProvider.COMMAND_DEBUG_SINGLE,
                Lookups.singleton(DataObject.find(test))));
        
        // Test removed from suite since it accesses MDR repository and as such
        // it must be executed by ide executor, see issue #82795
        // FileObject source = project.getProjectDirectory().getFileObject("src/java/pkg/NewClass.java");
        // assertNotNull("have src/java/pkg/NewClass.java", source);
        // assertFalse("Debug File is disabled on source file", ap.isActionEnabled(
        //         ActionProvider.COMMAND_DEBUG_SINGLE,
        //         Lookups.singleton(DataObject.find(source))));
    }
    
    public void testCompileSingle() throws Exception { // #79581
        assertFalse("Compile Single is disabled on empty context", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookup.EMPTY));
        assertFalse("Compile Single is disabled on project directory", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(project.getProjectDirectory()))));
        
        FileObject testPackage = project.getProjectDirectory().getFileObject("test/pkg");
        assertNotNull("have test/pkg", testPackage);
        assertTrue("Compile Single is enabled on test package", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(testPackage))));
        FileObject test = project.getProjectDirectory().getFileObject("test/pkg/NewClassTest.java");
        assertNotNull("have test/pkg/NewClassTest.java", test);
        assertTrue("Compile Single is enabled on test", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(test))));

        FileObject srcPackage = project.getProjectDirectory().getFileObject("src/java/pkg");
        assertNotNull("have src/java/pkg", srcPackage);
        assertTrue("Compile Single is enabled on source package", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(srcPackage))));
        FileObject src = project.getProjectDirectory().getFileObject("src/java/pkg/NewClass.java");
        assertNotNull("have src/java/pkg/NewClass.java", src);
        assertTrue("Compile Single is enabled on source", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(src))));
    }
}
