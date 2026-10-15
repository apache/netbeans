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
        Project freeform = ProjectManager.getDefault().findProject(FileUtil.toFileObject(file("java/ant.freeform")));
        assertNotNull("have project in ant/freeform", freeform);
        ActionProvider ap = freeform.getLookup().lookup(ActionProvider.class);
        assertNotNull("have ActionProvider", ap);
        FileObject actionsJava = FileUtil.toFileObject(file("java/ant.freeform/src/org/netbeans/modules/ant/freeform/Actions.java"));
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
