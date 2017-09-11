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

package org.netbeans.modules.apisupport.project.ui;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

// XXX much more to test

/**
 * Test Actions.
 * @author Jesse Glick
 */
public class ModuleActionsTest extends TestBase {
    
    public ModuleActionsTest(String name) {
        super(name);
    }
    
    public void testDebugFix() throws Exception {
        // Track down #47012.
        Project freeform = ProjectManager.getDefault().findProject(FileUtil.toFileObject(file("ant.freeform")));
        assertNotNull("have project in ant/freeform", freeform);
        ActionProvider ap = freeform.getLookup().lookup(ActionProvider.class);
        assertNotNull("have ActionProvider", ap);
        FileObject actionsJava = FileUtil.toFileObject(file("ant.freeform/src/org/netbeans/modules/ant/freeform/Actions.java"));
        assertNotNull("have Actions.java", actionsJava);
        assertTrue("Fix enabled on it", ap.isActionEnabled(JavaProjectConstants.COMMAND_DEBUG_FIX, Lookups.singleton(DataObject.find(actionsJava))));
    }

    public void testFindTestSources() throws Exception {
        NbModuleProject p = generateStandaloneModule("p");
        FileObject test = p.getTestSourceDirectory("unit");
        FileObject oneTest = FileUtil.createData(test, "p/OneTest.java");
        FileObject r = FileUtil.createData(test, "p/r.png");
        FileObject otherTest = FileUtil.createData(test, "p/OtherTest.java");
        FileObject pkg = test.getFileObject("p");
        FileObject thirdTest = FileUtil.createData(test, "p2/ThirdTest.java");
        ModuleActions a = new ModuleActions(p);
        assertEquals("null", String.valueOf(a.findTestSources(Lookup.EMPTY, false)));
        assertEquals("unit:p/OneTest.java", String.valueOf(a.findTestSources(Lookups.singleton(oneTest), false)));
        assertEquals("unit:p/OneTest.java,p/OtherTest.java", String.valueOf(a.findTestSources(Lookups.fixed(oneTest, otherTest), false)));
        assertEquals("null", String.valueOf(a.findTestSources(Lookups.singleton(pkg), false)));
        assertEquals("null", String.valueOf(a.findTestSources(Lookups.singleton(r), false)));
        assertEquals("null", String.valueOf(a.findTestSources(Lookups.fixed(oneTest, r), false)));
        assertEquals("null", String.valueOf(a.findTestSources(Lookup.EMPTY, true)));
        assertEquals("unit:p/OneTest.java", String.valueOf(a.findTestSources(Lookups.singleton(oneTest), true)));
        assertEquals("unit:p/OneTest.java,p/OtherTest.java", String.valueOf(a.findTestSources(Lookups.fixed(oneTest, otherTest), true)));
        assertEquals("unit:p/**", String.valueOf(a.findTestSources(Lookups.singleton(pkg), true)));
        assertEquals("unit:p/**,p2/ThirdTest.java", String.valueOf(a.findTestSources(Lookups.fixed(pkg, thirdTest), true)));
        assertEquals("null", String.valueOf(a.findTestSources(Lookups.singleton(r), true)));
        assertEquals("null", String.valueOf(a.findTestSources(Lookups.fixed(oneTest, r), true)));
    }
    
    public void testFindTestSourcesForSources() throws Exception {
        NbModuleProject p = generateStandaloneModule("p");
        FileObject source = p.getSourceDirectory();
        FileObject one = FileUtil.createData(source, "p/One.java");
        FileObject r = FileUtil.createData(source, "p/r.png");
        FileObject other = FileUtil.createData(source, "p/Other.java");
        FileObject pkg = source.getFileObject("p");
        FileObject third = FileUtil.createData(source, "p2/Third.java");
        
        FileObject test = p.getTestSourceDirectory("unit");
        FileObject oneTest = FileUtil.createData(test, "p/OneTest.java");
        FileObject otherTest = FileUtil.createData(test, "p/OtherTest.java");
        FileObject thirdTest = FileUtil.createData(test, "p2/ThirdTest.java");
        
        ModuleActions a = new ModuleActions(p);
        assertEquals("null", String.valueOf(a.findTestSourcesForSources(Lookup.EMPTY)));
        assertEquals("unit:p/OneTest.java", String.valueOf(a.findTestSourcesForSources(Lookups.singleton(one))));
        assertEquals("unit:p/OneTest.java,p/OtherTest.java", String.valueOf(a.findTestSourcesForSources(Lookups.fixed(one, other))));
        assertEquals("unit:p/**", String.valueOf(a.findTestSourcesForSources(Lookups.singleton(pkg))));
        assertEquals("null", String.valueOf(a.findTestSourcesForSources(Lookups.singleton(r))));
        assertEquals("null", String.valueOf(a.findTestSourcesForSources(Lookups.fixed(one, r))));
        assertEquals("null", String.valueOf(a.findTestSourcesForSources(Lookup.EMPTY)));
        assertEquals("unit:p/OneTest.java", String.valueOf(a.findTestSourcesForSources(Lookups.singleton(one))));
        assertEquals("unit:p/OneTest.java,p/OtherTest.java", String.valueOf(a.findTestSourcesForSources(Lookups.fixed(one, other))));
        assertEquals("null", String.valueOf(a.findTestSourcesForSources(Lookups.singleton(r))));
        assertEquals("null", String.valueOf(a.findTestSourcesForSources(Lookups.fixed(one, r))));
}

    public void testFindTestSourcesForFiles() throws Exception {
        NbModuleProject p = generateStandaloneModule("p");
        FileObject source = p.getSourceDirectory();
        FileObject one = FileUtil.createData(source, "p/One.java");
        FileObject r = FileUtil.createData(source, "p/r.png");
        FileObject other = FileUtil.createData(source, "p/Other.java");
        FileObject pkg = source.getFileObject("p");
        FileObject third = FileUtil.createData(source, "p2/Third.java");
        
        FileObject test = p.getTestSourceDirectory("unit");
        FileObject oneTest = FileUtil.createData(test, "p/OneTest.java");
        FileObject otherTest = FileUtil.createData(test, "p/OtherTest.java");
        FileObject thirdTest = FileUtil.createData(test, "p2/ThirdTest.java");
        
        ModuleActions a = new ModuleActions(p);
        assertEquals("null", String.valueOf(a.findTestSourcesForFiles(Lookup.EMPTY)));
        String actual = String.valueOf(a.findTestSourcesForFiles(Lookups.fixed(oneTest, other, third)));
        String testType = "unit";
        String expOne = "p/OneTest.java";
        String expOther = "p/OtherTest.java";
        String expThird = "p2/ThirdTest.java";
        assertTrue(actual.startsWith(testType.concat(":")));
        assertTrue(actual.contains(expOne));
        assertTrue(actual.contains(expOther));
        assertTrue(actual.contains(expThird));
        
        actual = String.valueOf(a.findTestSourcesForFiles(Lookups.fixed(one, otherTest)));
        assertTrue(actual.startsWith(testType.concat(":")));
        assertTrue(actual.contains(expOne));
        assertTrue(actual.contains(expOther));
    }
    
}
