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

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.w3c.dom.Element;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.java.freeform.JavaProjectGenerator.JavaCompilationUnit;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;

/**
 * Tests for JavaProjectGenerator.
 *
 * @author David Konecny
 */
public class JavaProjectGeneratorTest extends NbTestCase {

    private File lib1;
    private File lib2;
    private File src;
    private File test;
    
    public JavaProjectGeneratorTest(java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    @Override
    protected int timeOut() {
        return 300000;
    }
    
    private AntProjectHelper createEmptyProject(String projectFolder, String projectName, boolean notSoEmpty) throws Exception {
        File base = new File(getWorkDir(), projectFolder);
        base.mkdir();
        File antScript = new File(base, "build.xml");
        antScript.createNewFile();
        src = new File(base, "src");
        src.mkdir();
        test = new File(base, "test");
        test.mkdir();
        File libs = new File(base, "libs");
        libs.mkdir();
        lib1 = new File(libs, "some.jar");
        createRealJarFile(lib1);
        lib2 = new File(libs, "some2.jar");
        createRealJarFile(lib2);
        
// XXX: might need to call refresh here??
//        FileObject fo = FileUtil.toFileObject(getWorkDir());
//        fo.refresh();
        
        ArrayList sources = new ArrayList();
        ArrayList compUnits = new ArrayList();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(base, base, projectName, null);
        if (notSoEmpty) {
            JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
            sf.label = "src";
            sf.type = "java";
            sf.style = "packages";
            sf.location = src.getAbsolutePath();
            sources.add(sf);
            JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
            JavaProjectGenerator.JavaCompilationUnit.CP cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
            cp.classpath = lib1.getAbsolutePath();
            cp.mode = "compile";
            cu.classpath = Collections.singletonList(cp);
            cu.sourceLevel = "1.4";
            cu.packageRoots = Collections.singletonList(src.getAbsolutePath());
            compUnits.add(cu);
            JavaProjectGenerator.putSourceFolders(helper, sources, null);
            JavaProjectGenerator.putSourceViews(helper, sources, null);
            JavaProjectGenerator.putJavaCompilationUnits(helper, Util.getAuxiliaryConfiguration(helper), compUnits);
        }
        return helper;
    }
    
    public void testCreateProject() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj1", "proj-1", false);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        ProjectInformation pi = ProjectUtils.getInformation(p);
        assertEquals("Project name was not set", "proj-1", pi.getName());
    }
    
    public void testRawCreateProject() throws Exception {
        File base = new File(getWorkDir(), "proj");
        base.mkdir();
        File diffFolder = new File(getWorkDir(), "separate");
        diffFolder.mkdir();
        File antScript = new File(diffFolder, "build.xml");
        antScript.createNewFile();
        
// XXX: might need to call refresh here??
//        FileObject fo = FileUtil.toFileObject(getWorkDir());
//        fo.refresh();

        AntProjectHelper helper = FreeformProjectGenerator.createProject(diffFolder, base, "p-r-o-j", antScript);
        Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        assertNotNull("Project was not created", p);
        List mappings = new ArrayList();
        FreeformProjectGenerator.TargetMapping tm = new FreeformProjectGenerator.TargetMapping();
        tm.name = "foo";
        tm.script = "antScript";
        mappings.add(tm);
        List customActions = new ArrayList();
        FreeformProjectGenerator.CustomTarget ct = new FreeformProjectGenerator.CustomTarget();
        ct.label = "customAction1";
        customActions.add(ct);
        List folders = new ArrayList();
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "folder3";
        sf.location = "location3";
        sf.style = "tree";
        folders.add(sf);
        List exports = new ArrayList();
        JavaProjectGenerator.Export e = new JavaProjectGenerator.Export();
        e.type = JavaProjectConstants.ARTIFACT_TYPE_JAR;
        e.location = "folder/output.jar";
        e.buildTarget = "target";
        exports.add(e);
        List subprojects = new ArrayList();
        subprojects.add("/projA");
        
        FreeformProjectGenerator.putTargetMappings(helper, mappings);
        FreeformProjectGenerator.putContextMenuAction(helper, mappings);
        FreeformProjectGenerator.putCustomContextMenuActions(helper, customActions);
        JavaProjectGenerator.putSourceFolders(helper, folders, null);
        JavaProjectGenerator.putSourceViews(helper, folders, null);
        JavaProjectGenerator.putExports(helper, exports);
        JavaProjectGenerator.putSubprojects(helper, subprojects);
//        ProjectManager.getDefault().saveAllProjects();
        
        // check that all elements are written in expected order
        
        Element el = Util.getPrimaryConfigurationData(helper);
        List subElements = XMLUtil.findSubElements(el);
        assertEquals(7, subElements.size());
        assertElementArray(subElements, 
            new String[]{"name", "properties", "folders", "ide-actions", "export", "view", "subprojects"}, 
            new String[]{null, null, null, null, null, null, null});
        Element el2 = (Element)subElements.get(5);
        subElements = XMLUtil.findSubElements(el2);
        assertEquals(2, subElements.size());
        assertElementArray(subElements, 
            new String[]{"items", "context-menu"}, 
            new String[]{null, null});
        Element el3 = (Element)subElements.get(0);
        List subEls = XMLUtil.findSubElements(el3);
        assertEquals(2, subEls.size());
        assertElementArray(subEls, 
            new String[]{"source-folder", "source-file"}, 
            new String[]{null, null});
        el3 = (Element)subElements.get(1);
        subEls = XMLUtil.findSubElements(el3);
        assertEquals(2, subEls.size());
        assertElementArray(subEls, 
            new String[]{"ide-action", "action"}, 
            new String[]{null, null});
            
        // calling getters and setters in random order cannot change order of elements

        mappings = FreeformProjectGenerator.getTargetMappings(helper);
        customActions = FreeformProjectGenerator.getCustomContextMenuActions(helper);
        folders = JavaProjectGenerator.getSourceFolders(helper, null);
        // style is not read by getSourceFolders and needs to be fixed here:
        ((JavaProjectGenerator.SourceFolder)folders.get(0)).style = "tree";
        FreeformProjectGenerator.putTargetMappings(helper, mappings);
        JavaProjectGenerator.putSubprojects(helper, subprojects);
        FreeformProjectGenerator.putContextMenuAction(helper, mappings);
        JavaProjectGenerator.putExports(helper, exports);
        FreeformProjectGenerator.putCustomContextMenuActions(helper, customActions);
        JavaProjectGenerator.putSourceFolders(helper, folders, null);
        JavaProjectGenerator.putSourceViews(helper, folders, null);
        JavaProjectGenerator.putSourceViews(helper, folders, null);
        JavaProjectGenerator.putSourceFolders(helper, folders, null);
        JavaProjectGenerator.putExports(helper, exports);
        FreeformProjectGenerator.putCustomContextMenuActions(helper, customActions);
        FreeformProjectGenerator.putContextMenuAction(helper, mappings);
        JavaProjectGenerator.putSubprojects(helper, subprojects);
        FreeformProjectGenerator.putTargetMappings(helper, mappings);
//        ProjectManager.getDefault().saveAllProjects();
        el = Util.getPrimaryConfigurationData(helper);
        subElements = XMLUtil.findSubElements(el);
        assertEquals(7, subElements.size());
        assertElementArray(subElements, 
            new String[]{"name", "properties", "folders", "ide-actions", "export", "view", "subprojects"}, 
            new String[]{null, null, null, null, null, null, null});
        el2 = (Element)subElements.get(5);
        subElements = XMLUtil.findSubElements(el2);
        assertEquals(2, subElements.size());
        assertElementArray(subElements, 
            new String[]{"items", "context-menu"}, 
            new String[]{null, null});
        el3 = (Element)subElements.get(0);
        subEls = XMLUtil.findSubElements(el3);
        assertEquals(2, subEls.size());
        assertElementArray(subEls, 
            new String[]{"source-folder", "source-file"}, 
            new String[]{null, null});
        el3 = (Element)subElements.get(1);
        subEls = XMLUtil.findSubElements(el3);
        assertEquals(2, subEls.size());
        assertElementArray(subEls, 
            new String[]{"ide-action", "action"}, 
            new String[]{null, null});

        ProjectManager.getDefault().saveAllProjects();
    }
    
    /**
     * Asserts that given Element has expected name and its text match expected value.
     * @param element element to test
     * @param expectedName expected name of element; cannot be null
     * @param expectedValue can be null in which case value is not tested
     */
    public static void assertElement(Element element, String expectedName, String expectedValue) {
        String message = "Element "+element+" does not match [name="+expectedName+",value="+expectedValue+"]"; // NOI18N
        assertEquals(message, expectedName, element.getLocalName());
        if (expectedValue != null) {
            assertEquals(message, expectedValue, XMLUtil.findText(element));
        }
    }

    /**
     * See {@link #assertElement(Element, String, String)} for more details. This 
     * method does exactly the same just on the list of elements and expected names. 
     */
    public static void assertElementArray(List<Element> elements, String[] expectedNames, String[] expectedValues) {
        for (int i=0; i<elements.size(); i++) {
            assertElement((Element)elements.get(i), expectedNames[i], expectedValues[i]);
        }
    }
    
    /**
     * Asserts that given Element has expected name and its text match expected value and
     * it also has expect attribute with expected value.
     * @param element element to test
     * @param expectedName expected name of element; cannot be null
     * @param expectedValue can be null in which case value is not tested
     * @param expectedAttrName expected name of attribute; cannot be null
     * @param expectedAttrValue expected value of attribute; cannot be null
     */
    public static void assertElement(Element element, String expectedName, String expectedValue, String expectedAttrName, String expectedAttrValue) {
        String message = "Element "+element+" does not match [name="+expectedName+",value="+
            expectedValue+", attr="+expectedAttrName+", attrvalue="+expectedAttrValue+"]"; // NOI18N
        assertEquals(message, expectedName, element.getLocalName());
        if (expectedValue != null) {
            assertEquals(message, expectedValue, XMLUtil.findText(element));
        }
        String val = element.getAttribute(expectedAttrName);
        assertEquals(expectedAttrValue, val);
    }
    
    /**
     * See {@link #assertElement(Element, String, String)} for more details. This 
     * method does exactly the same just on the list of elements and expected names
     * and expected attributes.
     */
    public static void assertElementArray(List<Element> elements, String[] expectedNames, String[] expectedValues, String[] expectedAttrName, String[] expectedAttrValue) {
        assertEquals(expectedNames.length, elements.size());
        for (int i=0; i<elements.size(); i++) {
            assertElement((Element)elements.get(i), expectedNames[i], expectedValues[i], expectedAttrName[i], expectedAttrValue[i]);
        }
    }

    @RandomlyFails // NB-Core-Build #1002
    public void testSourceFolders() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj3", "proj-3", true);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        Sources ss = ProjectUtils.getSources(p);
        assertEquals("Project must have one java source group", 1, ss.getSourceGroups("java").length);
        assertEquals("Project cannot have csharp source group", 0, ss.getSourceGroups("csharp").length);

        Listener l = new Listener();
        ss.addChangeListener(l);
        
        List sfs = JavaProjectGenerator.getSourceFolders(helper, null);
        assertEquals("There must be one source folder", 1, sfs.size());
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "test";
        sf.type = "java";
        sf.location = test.getAbsolutePath();
        sfs.add(sf);
        JavaProjectGenerator.putSourceFolders(helper, sfs, null);
        assertEquals("Project must have two java source groups", 2, ss.getSourceGroups("java").length);
        assertEquals("Project cannot have csharp source group", 0, ss.getSourceGroups("csharp").length);
        assertEquals("Number of fired events does not match", 1, l.count);
        l.reset();
        
        sfs = new ArrayList();
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "xdoc";
        sf.type = "x-doc";
        // just some path
        sf.location = test.getAbsolutePath();
        sfs.add(sf);
        JavaProjectGenerator.putSourceFolders(helper, sfs, "x-doc");
        assertEquals("Project must have two java source groups", 2, ss.getSourceGroups("java").length);
        assertEquals("Project must have two java source groups", 2, JavaProjectGenerator.getSourceFolders(helper, "java").size());
        assertEquals("Project cannot have csharp source group", 0, ss.getSourceGroups("csharp").length);
        assertEquals("Project must have one x-doc source group", 1, ss.getSourceGroups("x-doc").length);
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "xdoc2";
        sf.type = "x-doc";
        // just some path
        sf.location = src.getAbsolutePath();
        sfs.add(sf);
        JavaProjectGenerator.putSourceFolders(helper, sfs, "x-doc");
        assertEquals("Project must have two java source groups", 2, ss.getSourceGroups("java").length);
        assertEquals("Project must have two java source groups", 2, JavaProjectGenerator.getSourceFolders(helper, "java").size());
        assertEquals("Project cannot have csharp source group", 0, ss.getSourceGroups("csharp").length);
        assertEquals("Project must have two x-doc source groups", 2, ss.getSourceGroups("x-doc").length);
        assertEquals("Project must have two x-doc source groups", 2, JavaProjectGenerator.getSourceFolders(helper, "x-doc").size());
        assertEquals("Project must have four source groups", 4, JavaProjectGenerator.getSourceFolders(helper, null).size());

        sfs = JavaProjectGenerator.getSourceFolders(helper, null);
        JavaProjectGenerator.putSourceFolders(helper, sfs, null);
        assertEquals("Project must have two java source groups", 2, ss.getSourceGroups("java").length);
        assertEquals("Project must have two java source groups", 2, JavaProjectGenerator.getSourceFolders(helper, "java").size());
        assertEquals("Project cannot have csharp source group", 0, ss.getSourceGroups("csharp").length);
        assertEquals("Project must have two x-doc source groups", 2, ss.getSourceGroups("x-doc").length);
        assertEquals("Project must have two x-doc source groups", 2, JavaProjectGenerator.getSourceFolders(helper, "x-doc").size());
        assertEquals("Project must have four source groups", 4, JavaProjectGenerator.getSourceFolders(helper, null).size());

        ProjectManager.getDefault().saveAllProjects();
    }
    
    public void testRawSourceFolders() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj", false);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        // check that all data are correctly persisted
        
        List folders = new ArrayList();
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "folder1";
        sf.type = "type1";
        sf.location = "location1";
        folders.add(sf);
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "folder2";
        sf.type = "type2";
        sf.location = "location2";
        folders.add(sf);
        JavaProjectGenerator.putSourceFolders(helper, folders, null);
        // test getter and setter here:
        folders = JavaProjectGenerator.getSourceFolders(helper, null);
        JavaProjectGenerator.putSourceFolders(helper, folders, null);
//        ProjectManager.getDefault().saveAllProjects();
        Element el = Util.getPrimaryConfigurationData(helper);
        el = XMLUtil.findElement(el, "folders", Util.NAMESPACE);
        assertNotNull("Source folders were not saved correctly",  el);
        List subElements = XMLUtil.findSubElements(el);
        assertEquals(2, subElements.size());
        // compare first source folder
        Element el2 = (Element)subElements.get(0);
        assertElement(el2, "source-folder", null);
        List l1 = XMLUtil.findSubElements(el2);
        assertEquals(3, l1.size());
        assertElementArray(l1, 
            new String[]{"label", "type", "location"}, 
            new String[]{"folder1", "type1", "location1"});
        // compare second source folder
        el2 = (Element)subElements.get(1);
        assertElement(el2, "source-folder", null);
        l1 = XMLUtil.findSubElements(el2);
        assertEquals(3, l1.size());
        assertElementArray(l1, 
            new String[]{"label", "type", "location"}, 
            new String[]{"folder2", "type2", "location2"});
        ProjectManager.getDefault().saveAllProjects();
            
        // test rewriting of source folder of some type
        
        folders = new ArrayList();
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "folder3";
        sf.type = "type2";
        sf.location = "location3";
        folders.add(sf);
        JavaProjectGenerator.putSourceFolders(helper, folders, "type2");
        ProjectManager.getDefault().saveAllProjects();
        el = Util.getPrimaryConfigurationData(helper);
        el = XMLUtil.findElement(el, "folders", Util.NAMESPACE);
        assertNotNull("Source folders were not saved correctly",  el);
        subElements = XMLUtil.findSubElements(el);
        assertEquals(2, subElements.size());
        // compare first source folder
        el2 = (Element)subElements.get(0);
        assertElement(el2, "source-folder", null);
        l1 = XMLUtil.findSubElements(el2);
        assertEquals(3, l1.size());
        assertElementArray(l1, 
            new String[]{"label", "type", "location"}, 
            new String[]{"folder1", "type1", "location1"});
        // compare second source folder
        el2 = (Element)subElements.get(1);
        assertElement(el2, "source-folder", null);
        l1 = XMLUtil.findSubElements(el2);
        assertEquals(3, l1.size());
        assertElementArray(l1, 
            new String[]{"label", "type", "location"}, 
            new String[]{"folder3", "type2", "location3"});
        ProjectManager.getDefault().saveAllProjects();
    }

    @RandomlyFails // NB-Core-Build #3877: There must be two subnodes in logical view expected:<2> but was:<1>
    public void testSourceViews() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj6", "proj-6", true);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        Sources ss = ProjectUtils.getSources(p);
        assertEquals("Project must have one java source group", 1, ss.getSourceGroups("java").length);

        LogicalViewProvider lvp = (LogicalViewProvider)p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("Project does not have LogicalViewProvider", lvp);
        Node n = lvp.createLogicalView();
        // expected subnodes: #1) src folder and #2) build.xml
        assertEquals("There must be two subnodes in logical view", 2, n.getChildren().getNodesCount(true));
        
        List sfs = JavaProjectGenerator.getSourceViews(helper, null);
        assertEquals("There must be one source view", 1, sfs.size());
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "test";
        sf.style = "packages";
        sf.location = test.getAbsolutePath();
        sfs.add(sf);
        JavaProjectGenerator.putSourceViews(helper, sfs, null);
        assertEquals("Project must have two packages source views", 2, JavaProjectGenerator.getSourceViews(helper, "packages").size());
        assertEquals("Project cannot have any flat source view", 0, JavaProjectGenerator.getSourceViews(helper, "flat").size());
        
        n = lvp.createLogicalView();
        // expected subnodes: #1) src folder and #2) build.xml and #3) tests
//        assertEquals("There must be three subnodes in logical view", 3, n.getChildren().getNodesCount());

        sfs = new ArrayList();
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "xdoc";
        sf.style = "tree";
        // just some path
        sf.location = test.getAbsolutePath();
        sfs.add(sf);
        JavaProjectGenerator.putSourceViews(helper, sfs, "tree");
        assertEquals("Project must have two packages source views", 2, JavaProjectGenerator.getSourceViews(helper, "packages").size());
        assertEquals("Project cannot have any flat source view", 0, JavaProjectGenerator.getSourceViews(helper, "flat").size());
        assertEquals("Project must have one tree source view", 1, JavaProjectGenerator.getSourceViews(helper, "tree").size());
        assertEquals("Project must have three source views", 3, JavaProjectGenerator.getSourceViews(helper, null).size());
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "xdoc2";
        sf.style = "tree";
        // just some path
        sf.location = src.getAbsolutePath();
        sfs.add(sf);
        JavaProjectGenerator.putSourceViews(helper, sfs, "tree");
        assertEquals("Project must have two packages source views", 2, JavaProjectGenerator.getSourceViews(helper, "packages").size());
        assertEquals("Project cannot have any flat source view", 0, JavaProjectGenerator.getSourceViews(helper, "flat").size());
        assertEquals("Project must have two tree source views", 2, JavaProjectGenerator.getSourceViews(helper, "tree").size());
        assertEquals("Project must have four source views", 4, JavaProjectGenerator.getSourceViews(helper, null).size());

        sfs = JavaProjectGenerator.getSourceViews(helper, null);
        JavaProjectGenerator.putSourceViews(helper, sfs, null);
        assertEquals("Project must have two packages source views", 2, JavaProjectGenerator.getSourceViews(helper, "packages").size());
        assertEquals("Project cannot have any flat source view", 0, JavaProjectGenerator.getSourceViews(helper, "flat").size());
        assertEquals("Project must have two tree source views", 2, JavaProjectGenerator.getSourceViews(helper, "tree").size());
        assertEquals("Project must have four source views", 4, JavaProjectGenerator.getSourceViews(helper, null).size());

        ProjectManager.getDefault().saveAllProjects();
    }
    
    public void testRawSourceViews() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj", false);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        // check that all data are correctly persisted
        
        List folders = new ArrayList();
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "folder1";
        sf.style = "tree";
        sf.location = "location1";
        folders.add(sf);
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "folder2";
        sf.style = "packages";
        sf.location = "location2";
        folders.add(sf);
        JavaProjectGenerator.putSourceViews(helper, folders, null);
        // test getter and setter here:
        folders = JavaProjectGenerator.getSourceViews(helper, null);
        JavaProjectGenerator.putSourceViews(helper, folders, null);
        ProjectManager.getDefault().saveAllProjects();
        Element el = Util.getPrimaryConfigurationData(helper);
        el = XMLUtil.findElement(el, "view", Util.NAMESPACE);
        assertNotNull("View folders were not saved correctly",  el);
        el = XMLUtil.findElement(el, "items", Util.NAMESPACE);
        assertNotNull("View folders were not saved correctly",  el);
        List subElements = XMLUtil.findSubElements(el);
        // there will be three sublements: <source-file> is added for build.xml during project.creation
        assertEquals(3, subElements.size());
        // compare first source view
        Element el2 = (Element)subElements.get(0);
        assertElement(el2, "source-folder", null, "style", "tree");
        List l1 = XMLUtil.findSubElements(el2);
        assertEquals(2, l1.size());
        assertElementArray(l1, 
            new String[]{"label", "location"}, 
            new String[]{"folder1", "location1"});
        // compare second source view
        el2 = (Element)subElements.get(1);
        assertElement(el2, "source-folder", null, "style", "packages");
        l1 = XMLUtil.findSubElements(el2);
        assertEquals(2, l1.size());
        assertElementArray(l1, 
            new String[]{"label", "location"}, 
            new String[]{"folder2", "location2"});
        ProjectManager.getDefault().saveAllProjects();
            
        // test rewriting of source view of some style
        
        folders = new ArrayList();
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "folder3";
        sf.style = "packages";
        sf.location = "location3";
        folders.add(sf);
        JavaProjectGenerator.putSourceViews(helper, folders, "packages");
        ProjectManager.getDefault().saveAllProjects();
        el = Util.getPrimaryConfigurationData(helper);
        el = XMLUtil.findElement(el, "view", Util.NAMESPACE);
        assertNotNull("Source views were not saved correctly",  el);
        el = XMLUtil.findElement(el, "items", Util.NAMESPACE);
        assertNotNull("View folders were not saved correctly",  el);
        subElements = XMLUtil.findSubElements(el);
        // there will be three sublements: <source-file> is added for build.xml during project.creation
        assertEquals("3 elements in " + subElements, 3, subElements.size());
        // compare first source view
        el2 = (Element)subElements.get(0);
        assertElement(el2, "source-folder", null, "style", "tree");
        l1 = XMLUtil.findSubElements(el2);
        assertEquals(2, l1.size());
        assertElementArray(l1, 
            new String[]{"label", "location"}, 
            new String[]{"folder1", "location1"});
        // compare second source view
        el2 = (Element)subElements.get(1);
        assertElement(el2, "source-folder", null, "style", "packages");
        l1 = XMLUtil.findSubElements(el2);
        assertEquals(2, l1.size());
        assertElementArray(l1, 
            new String[]{"label", "location"}, 
            new String[]{"folder3", "location3"});
        ProjectManager.getDefault().saveAllProjects();
    }

    public void testJavaCompilationUnits() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj5", "proj-5", true);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        ClassPathProvider cpp = (ClassPathProvider)p.getLookup().lookup(ClassPathProvider.class);
        assertNotNull("Project does not have ClassPathProvider", cpp);
        ClassPath cp = cpp.findClassPath(FileUtil.toFileObject(src), ClassPath.COMPILE);
        assertEquals("Project must have one classpath root", 1, cp.getRoots().length);
        assertEquals("Classpath root does not match", "jar:"+Utilities.toURI(lib1).toURL()+"!/", (cp.getRoots()[0]).getURL().toExternalForm());
        cp = cpp.findClassPath(FileUtil.toFileObject(src).getParent(), ClassPath.COMPILE);
        assertEquals("There is no classpath for this file", null, cp);
        
        AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper);
        List cus = JavaProjectGenerator.getJavaCompilationUnits(helper, aux);
        assertEquals("There must be one compilation unit", 1, cus.size());
        JavaProjectGenerator.JavaCompilationUnit cu = (JavaProjectGenerator.JavaCompilationUnit)cus.get(0);
        assertEquals("The compilation unit must have one classpath", 1, cu.classpath.size());
        
        JavaProjectGenerator.JavaCompilationUnit.CP cucp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cucp.classpath = lib2.getAbsolutePath();
        cucp.mode = "execute";
        cu.classpath.add(cucp);
        ArrayList outputs = new ArrayList();
        outputs.add("output1.jar");
        outputs.add("output2.jar");
        outputs.add("output3.jar");
        cu.output = outputs;
        JavaProjectGenerator.putJavaCompilationUnits(helper, aux, cus);
        cus = JavaProjectGenerator.getJavaCompilationUnits(helper, aux);
        assertEquals("There must be one compilation unit", 1, cus.size());
        cu = (JavaProjectGenerator.JavaCompilationUnit)cus.get(0);
        assertEquals("The compilation unit must have one classpath", 2, cu.classpath.size());
        assertEquals("The compilation unit must have one classpath", 3, cu.output.size());
        
        cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.sourceLevel = "1.4";
        cucp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cucp.classpath = lib2.getAbsolutePath();
        cucp.mode = "compile";
        cu.classpath = Collections.singletonList(cucp);
        cu.packageRoots = Collections.singletonList(test.getAbsolutePath());
        cus.add(cu);
        JavaProjectGenerator.putJavaCompilationUnits(helper, aux, cus);
        cus = JavaProjectGenerator.getJavaCompilationUnits(helper, aux);
        assertEquals("There must be two compilation units", 2, cus.size());
        cp = cpp.findClassPath(FileUtil.toFileObject(src), ClassPath.COMPILE);
        assertEquals("Project must have one classpath root", 1, cp.getRoots().length);
        assertEquals("Classpath root does not match", "jar:"+Utilities.toURI(lib1).toURL()+"!/", (cp.getRoots()[0]).getURL().toExternalForm());
        cp = cpp.findClassPath(FileUtil.toFileObject(src).getParent(), ClassPath.COMPILE);
        assertEquals("There is no classpath for this file", null, cp);
        cp = cpp.findClassPath(FileUtil.toFileObject(test), ClassPath.COMPILE);
        assertEquals("Project must have one classpath root", 1, cp.getRoots().length);
        assertEquals("Classpath root does not match", "jar:"+Utilities.toURI(lib2).toURL()+"!/", (cp.getRoots()[0]).getURL().toExternalForm());
        
        ProjectManager.getDefault().saveAllProjects();
    }
    
    public void testRawJavaCompilationUnits() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj", false);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        // check that all data are correctly persisted
        
        List<JavaCompilationUnit> units = new ArrayList<>();
        JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = new ArrayList();
        cu.packageRoots.add("pkgroot1");
        cu.packageRoots.add("pkgroot2");
        cu.output = new ArrayList();
        cu.output.add("output1");
        cu.output.add("output2");
        cu.classpath = new ArrayList();
        JavaProjectGenerator.JavaCompilationUnit.CP cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cp.mode = "compile";
        cp.classpath = "classpath1";
        cu.classpath.add(cp);
        cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cp.mode = "boot";
        cp.classpath = "classpath2";
        cu.classpath.add(cp);
        cu.sourceLevel = "1.3";
        units.add(cu);
        cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = new ArrayList();
        cu.packageRoots.add("sec-pkgroot1");
        cu.packageRoots.add("sec-pkgroot2");
        cu.output = new ArrayList();
        cu.output.add("sec-output1");
        cu.output.add("sec-output2");
        cu.classpath = new ArrayList();
        cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cp.mode = "compile";
        cp.classpath = "sec-classpath1";
        cu.classpath.add(cp);
        cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cp.mode = "boot";
        cp.classpath = "sec-classpath2";
        cu.classpath.add(cp);
        cu.sourceLevel = "1.4";
        units.add(cu);
        AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper);
        JavaProjectGenerator.putJavaCompilationUnits(helper, aux, units);
        // test getter and setter here:
        units = JavaProjectGenerator.getJavaCompilationUnits(helper, aux);
        JavaProjectGenerator.putJavaCompilationUnits(helper, aux, units);
//        ProjectManager.getDefault().saveAllProjects();
        Element el = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_1, true);
        assertNotNull("Java compilation units were not saved correctly",  el);
        List subElements = XMLUtil.findSubElements(el);
        assertEquals(2, subElements.size());
        // compare first compilation unit
        Element el2 = (Element)subElements.get(0);
        assertElement(el2, "compilation-unit", null);
        List l1 = XMLUtil.findSubElements(el2);
        assertEquals(7, l1.size());
        assertElementArray(l1, 
            new String[]{"package-root", "package-root", "classpath", "classpath", "built-to", "built-to", "source-level"}, 
            new String[]{"pkgroot1", "pkgroot2", "classpath1", "classpath2", "output1", "output2", "1.3"});
        el2 = (Element)l1.get(2);
        assertElement(el2, "classpath", "classpath1", "mode", "compile");
        el2 = (Element)l1.get(3);
        assertElement(el2, "classpath", "classpath2", "mode", "boot");
        // compare second compilation unit
        el2 = (Element)subElements.get(1);
        assertElement(el2, "compilation-unit", null);
        l1 = XMLUtil.findSubElements(el2);
        assertEquals(7, l1.size());
        assertElementArray(l1, 
            new String[]{"package-root", "package-root", "classpath", "classpath", "built-to", "built-to", "source-level"}, 
            new String[]{"sec-pkgroot1", "sec-pkgroot2", "sec-classpath1", "sec-classpath2", "sec-output1", "sec-output2", "1.4"});
        el2 = (Element)l1.get(2);
        assertElement(el2, "classpath", "sec-classpath1", "mode", "compile");
        el2 = (Element)l1.get(3);
        assertElement(el2, "classpath", "sec-classpath2", "mode", "boot");
        ProjectManager.getDefault().saveAllProjects();
            
        // test updating
            
        units = new ArrayList<>();
        cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = new ArrayList();
        cu.packageRoots.add("foo-package-root");
        units.add(cu);
        JavaProjectGenerator.putJavaCompilationUnits(helper, aux, units);
//        ProjectManager.getDefault().saveAllProjects();
        el = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_1, true);
        assertNotNull("Java compilation units were not saved correctly",  el);
        subElements = XMLUtil.findSubElements(el);
        assertEquals(1, subElements.size());
        // compare first compilation unit
        el2 = (Element)subElements.get(0);
        assertElement(el2, "compilation-unit", null);
        l1 = XMLUtil.findSubElements(el2);
        assertEquals(1, l1.size());
        assertElementArray(l1, 
            new String[]{"package-root"}, 
            new String[]{"foo-package-root"});
        ProjectManager.getDefault().saveAllProjects();
        
        //update to /4:
        units = JavaProjectGenerator.getJavaCompilationUnits(helper, aux);
        units.iterator().next().sourceLevel = "1.8";
        JavaProjectGenerator.putJavaCompilationUnits(helper, aux, units);
        assertNull("Java compilation units were not saved correctly",  aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_1, true));
        assertNotNull("Java compilation units were not saved correctly",  aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_4, true));
    }

    public void testCompilationUnitUpgrades() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj", false);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        // Start with a /1-friendly data set.
        List<JavaProjectGenerator.JavaCompilationUnit> units = new ArrayList<>();
        JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = new ArrayList();
        cu.packageRoots.add("pkgroot1");
        units.add(cu);
        AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper);
        JavaProjectGenerator.putJavaCompilationUnits(helper, aux, units);
        // Check that the correct /1 data was saved.
        Element el = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_1, true);
        assertNotNull("Java compilation units were saved in /1",  el);
        List<Element> subElements = XMLUtil.findSubElements(el);
        assertEquals(1, subElements.size());
        // compare the compilation unit
        Element el2 = (Element) subElements.get(0);
        assertElement(el2, "compilation-unit", null);
        assertElementArray(XMLUtil.findSubElements(el2),
            new String[] {"package-root"},
            new String[] {"pkgroot1"});
        ProjectManager.getDefault().saveAllProjects();
        // Now check that setting isTests = true on that element forces a /2 save.
        units = new ArrayList<>();
        cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = new ArrayList();
        cu.packageRoots.add("pkgroot1");
        cu.isTests = true;
        units.add(cu);
        JavaProjectGenerator.putJavaCompilationUnits(helper, aux, units);
        // Check that we now have it in /2.
        el = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_1, true);
        assertNull("No /1 data", el);
        el = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true);
        assertNotNull("Have /2 data", el);
        subElements = XMLUtil.findSubElements(el);
        assertEquals(1, subElements.size());
        // compare the compilation unit
        el2 = (Element) subElements.get(0);
        assertElement(el2, "compilation-unit", null);
        assertElementArray(XMLUtil.findSubElements(el2),
            new String[] {"package-root", "unit-tests"},
            new String[] {"pkgroot1", null});
        ProjectManager.getDefault().saveAllProjects();
        // Now try fresh save of /2-requiring data (using javadoc).
        assertTrue("removed /2 data", aux.removeConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true));
        units = new ArrayList<>();
        cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = new ArrayList();
        cu.packageRoots.add("pkgroot1");
        cu.javadoc = new ArrayList();
        cu.javadoc.add("javadoc1");
        cu.javadoc.add("javadoc2");
        units.add(cu);
        JavaProjectGenerator.putJavaCompilationUnits(helper, aux, units);
        // Check that we have it in /2.
        el = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_1, true);
        assertNull("No /1 data", el);
        el = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, true);
        assertNotNull("Have /2 data", el);
        subElements = XMLUtil.findSubElements(el);
        assertEquals(1, subElements.size());
        // compare the compilation unit
        el2 = (Element) subElements.get(0);
        assertElement(el2, "compilation-unit", null);
        assertElementArray(XMLUtil.findSubElements(el2),
            new String[] {"package-root", "javadoc-built-to", "javadoc-built-to"},
            new String[] {"pkgroot1", "javadoc1", "javadoc2"});
        ProjectManager.getDefault().saveAllProjects();
    }

    public void testGuessExports() throws Exception {
        JavaProjectGenerator.TargetMapping tm = new JavaProjectGenerator.TargetMapping();
        tm.name = "build";
        tm.script = "${ant}";
        tm.targets = new ArrayList();
        tm.targets.add("target-1");
        ArrayList targets = new ArrayList();
        targets.add(tm);
        
        JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.output = new ArrayList();
        cu.output.add("${outputfile}");
        ArrayList units = new ArrayList();
        units.add(cu);
        
        Map m = new HashMap();
        m.put("outputfile", "out.jar");
        m.put("ant", getWorkDir().getAbsolutePath()+"/etc/antScript");
        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[]{
            PropertyUtils.fixedPropertyProvider(m)});
        
        List exports = JavaProjectGenerator.guessExports(evaluator, getWorkDir(), targets, units);
        assertEquals("one export was created even though build script is not in project folder", 1, exports.size());
        // XXX test stuff about that export
        
        m.put("ant", "etc/antScript");
        evaluator = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[]{
            PropertyUtils.fixedPropertyProvider(m)});
        exports = JavaProjectGenerator.guessExports(evaluator, getWorkDir(), targets, units);
        assertEquals("one export was created", 1, exports.size());
        
        tm.script = null;
        exports = JavaProjectGenerator.guessExports(evaluator, getWorkDir(), targets, units);
        assertEquals("one export was created", 1, exports.size());
        JavaProjectGenerator.Export e = (JavaProjectGenerator.Export)exports.get(0);
        assertEquals("export is properly configured", JavaProjectConstants.ARTIFACT_TYPE_JAR, e.type);
        assertEquals("export is properly configured", "${outputfile}", e.location);
        assertEquals("export is properly configured", null, e.script);
        assertEquals("export is properly configured", "target-1", e.buildTarget);
        
        tm.targets.add("target-2");
        exports = JavaProjectGenerator.guessExports(evaluator, getWorkDir(), targets, units);
        assertEquals("no export was created when there are two targets", 0, exports.size());
        
        tm.targets.remove("target-2");
        exports = JavaProjectGenerator.guessExports(evaluator, getWorkDir(), targets, units);
        assertEquals("one export was created", 1, exports.size());
        
        tm.name = "buildXX";
        exports = JavaProjectGenerator.guessExports(evaluator, getWorkDir(), targets, units);
        assertEquals("no export was created when there is no action with build name", 0, exports.size());
        
        tm.name = "build";
        exports = JavaProjectGenerator.guessExports(evaluator, getWorkDir(), targets, units);
        assertEquals("one export was created", 1, exports.size());

        JavaProjectGenerator.JavaCompilationUnit cu2 = new JavaProjectGenerator.JavaCompilationUnit();
        cu2.output = new ArrayList();
        cu2.output.add("build/classes");
        units.add(cu2);
        exports = JavaProjectGenerator.guessExports(evaluator, getWorkDir(), targets, units);
        assertEquals("two exports was created", 2, exports.size());
        
        cu2.output.add("dist/proj.jar");
        cu2.output.add("dist/proj2.jar");
        tm.script = "antScript";
        exports = JavaProjectGenerator.guessExports(evaluator, getWorkDir(), targets, units);
        assertEquals("four exports were created", 4, exports.size());
        e = (JavaProjectGenerator.Export)exports.get(0);
        assertEquals("export is properly configured", JavaProjectConstants.ARTIFACT_TYPE_JAR, e.type);
        assertEquals("export is properly configured", "${outputfile}", e.location);
        assertEquals("export is properly configured", "antScript", e.script);
        assertEquals("export is properly configured", "target-1", e.buildTarget);
        e = (JavaProjectGenerator.Export)exports.get(1);
        assertEquals("export is properly configured", JavaProjectConstants.ARTIFACT_TYPE_FOLDER, e.type);
        assertEquals("export is properly configured", "build/classes", e.location);
        assertEquals("export is properly configured", "antScript", e.script);
        assertEquals("export is properly configured", "target-1", e.buildTarget);
        e = (JavaProjectGenerator.Export)exports.get(2);
        assertEquals("export is properly configured", JavaProjectConstants.ARTIFACT_TYPE_JAR, e.type);
        assertEquals("export is properly configured", "dist/proj.jar", e.location);
        assertEquals("export is properly configured", "antScript", e.script);
        assertEquals("export is properly configured", "target-1", e.buildTarget);
        e = (JavaProjectGenerator.Export)exports.get(3);
        assertEquals("export is properly configured", JavaProjectConstants.ARTIFACT_TYPE_JAR, e.type);
        assertEquals("export is properly configured", "dist/proj2.jar", e.location);
        assertEquals("export is properly configured", "antScript", e.script);
        assertEquals("export is properly configured", "target-1", e.buildTarget);
    }
    
    public void testPutExports() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj", false);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        // check that all data are correctly persisted
        
        List exports = new ArrayList();
        JavaProjectGenerator.Export e = new JavaProjectGenerator.Export();
        e.type = JavaProjectConstants.ARTIFACT_TYPE_JAR;
        e.location = "path/smth.jar";
        e.script = "someScript";
        e.buildTarget = "build_target";
        e.cleanTarget = "clean_target";
        exports.add(e);
        e = new JavaProjectGenerator.Export();
        e.type = JavaProjectConstants.ARTIFACT_TYPE_JAR;
        e.location = "something/else.jar";
        e.buildTarget = "bldtrg";
        exports.add(e);
        
        JavaProjectGenerator.putExports(helper, exports);
        Element el = Util.getPrimaryConfigurationData(helper);
        List subElements = XMLUtil.findSubElements(el);
        // 4, i.e. name, two exports and one view of build.xml file
        assertEquals(5, subElements.size());
        // compare first compilation unit
        Element el2 = (Element)subElements.get(0);
        assertElement(el2, "name", null);
        el2 = (Element)subElements.get(1);
        assertElement(el2, "properties", null);
        el2 = (Element)subElements.get(2);
        assertElement(el2, "export", null);
        List l1 = XMLUtil.findSubElements(el2);
        assertEquals(5, l1.size());
        assertElementArray(l1, 
            new String[]{"type", "location", "script", "build-target", "clean-target"}, 
            new String[]{JavaProjectConstants.ARTIFACT_TYPE_JAR, "path/smth.jar", "someScript", "build_target", "clean_target"});
        // compare second compilation unit
        el2 = (Element)subElements.get(3);
        assertElement(el2, "export", null);
        l1 = XMLUtil.findSubElements(el2);
        assertEquals(3, l1.size());
        assertElementArray(l1, 
            new String[]{"type", "location", "build-target"}, 
            new String[]{JavaProjectConstants.ARTIFACT_TYPE_JAR, "something/else.jar", "bldtrg"});
        el2 = (Element)subElements.get(4);
        assertElement(el2, "view", null);
        ProjectManager.getDefault().saveAllProjects();
            
        // now test updating
        
        exports = new ArrayList();
        e = new JavaProjectGenerator.Export();
        e.type = JavaProjectConstants.ARTIFACT_TYPE_JAR;
        e.location = "aaa/bbb.jar";
        e.buildTarget = "ccc";
        exports.add(e);
        
        JavaProjectGenerator.putExports(helper, exports);
        el = Util.getPrimaryConfigurationData(helper);
        subElements = XMLUtil.findSubElements(el);
        // 3, i.e. name, export and one view of build.xml file
        assertEquals(4, subElements.size());
        // compare first compilation unit
        el2 = (Element)subElements.get(0);
        assertElement(el2, "name", null);
        el2 = (Element)subElements.get(1);
        assertElement(el2, "properties", null);
        el2 = (Element)subElements.get(2);
        assertElement(el2, "export", null);
        l1 = XMLUtil.findSubElements(el2);
        assertEquals(3, l1.size());
        assertElementArray(l1, 
            new String[]{"type", "location", "build-target"}, 
            new String[]{JavaProjectConstants.ARTIFACT_TYPE_JAR, "aaa/bbb.jar", "ccc"});
        el2 = (Element)subElements.get(3);
        assertElement(el2, "view", null);
        ProjectManager.getDefault().saveAllProjects();
            
    }
    
    public void testGuessSubprojects() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj1", "proj1", false);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        ArrayList exports = new ArrayList();
        JavaProjectGenerator.Export e = new JavaProjectGenerator.Export();
        e.type = JavaProjectConstants.ARTIFACT_TYPE_JAR;
        e.location = "libs/some.jar"; // this jar is created in createEmptyProject() so let's use it as export
        e.buildTarget = "build_target";
        exports.add(e);
        JavaProjectGenerator.putExports(helper, exports);
        ProjectManager.getDefault().saveAllProjects();
        String lib1path = lib1.getAbsolutePath();
        String proj1path = FileUtil.toFile(base).getAbsolutePath();
        
        AntProjectHelper helper2 = createEmptyProject("proj2", "proj2", false);
        FileObject base2 = helper.getProjectDirectory();
        File projBase = FileUtil.toFile(base2);
        Project p2 = ProjectManager.getDefault().findProject(base2);
        assertNotNull("Project was not created", p2);
        assertEquals("Project folder is incorrect", base2, p.getProjectDirectory());
        
        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[]{
            PropertyUtils.fixedPropertyProvider(
            Collections.singletonMap("lib1", lib1path))});
            
        ArrayList units = new ArrayList();
        JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
        JavaProjectGenerator.JavaCompilationUnit.CP cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cp.mode = "compile";
        cp.classpath = "../something.jar;${lib1};";
        cu.classpath = new ArrayList();
        cu.classpath.add(cp);
        units.add(cu);
        cu = new JavaProjectGenerator.JavaCompilationUnit();
        cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cp.mode = "compile";
        cp.classpath = lib1path+";";
        cu.classpath = new ArrayList();
        cu.classpath.add(cp);
        units.add(cu);
        
        List l = JavaProjectGenerator.guessSubprojects(evaluator, units, projBase, projBase);
        assertEquals("one subproject", 1, l.size());
        assertEquals("project1 is subproject", /*proj1path*/ ".", l.get(0));
    }
    
    public void testPutSubprojects() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj", false);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        // check that all data are correctly persisted
        
        List subprojects = new ArrayList();
        subprojects.add("/some/path/projA");
        subprojects.add("C:\\dev\\projB");
        
        JavaProjectGenerator.putSubprojects(helper, subprojects);
        Element el = Util.getPrimaryConfigurationData(helper);
        Element subprojectsEl = XMLUtil.findElement(el, "subprojects", Util.NAMESPACE);
        assertNotNull("<subprojects> element exists", subprojectsEl);
        List subElements = XMLUtil.findSubElements(subprojectsEl);
        assertEquals("project depends on two subprojects", 2, subElements.size());
        Element el2 = (Element)subElements.get(0);
        assertElement(el2, "project", "/some/path/projA");
        el2 = (Element)subElements.get(1);
        assertElement(el2, "project", "C:\\dev\\projB");
        
        ProjectManager.getDefault().saveAllProjects();
        
        // now test updating
        
        subprojects = new ArrayList();
        subprojects.add("/projC");
        JavaProjectGenerator.putSubprojects(helper, subprojects);
        el = Util.getPrimaryConfigurationData(helper);
        subprojectsEl = XMLUtil.findElement(el, "subprojects", Util.NAMESPACE);
        subElements = XMLUtil.findSubElements(subprojectsEl);
        assertEquals("project depends on one subproject", 1, subElements.size());
        el2 = (Element)subElements.get(0);
        assertElement(el2, "project", "/projC");
        subprojects = new ArrayList();
        JavaProjectGenerator.putSubprojects(helper, subprojects);
        el = Util.getPrimaryConfigurationData(helper);
        subprojectsEl = XMLUtil.findElement(el, "subprojects", Util.NAMESPACE);
        subElements = XMLUtil.findSubElements(subprojectsEl);
        assertEquals("project depends on one subproject", 0, subElements.size());
        
        ProjectManager.getDefault().saveAllProjects();
        
    }    

    public void testGuessBuildFolders() throws Exception {
        File base = new File(getWorkDir(), "folder");
        File proj1 = new File(base, "proj1");
        proj1.mkdir();
        File base2 = new File(getWorkDir(), "folder2");
        base2.mkdir();
                
        JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.output = new ArrayList();
        cu.output.add("${outputfile}");
        ArrayList units = new ArrayList();
        units.add(cu);
        
        Map m = new HashMap();
        m.put("outputfile", "out.jar");
        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[]{
            PropertyUtils.fixedPropertyProvider(m)});
        List buildFolders = JavaProjectGenerator.guessBuildFolders(evaluator, units, proj1, proj1);
        assertEquals("no build folder", 0, buildFolders.size());
                
        cu.output.add(base2.getAbsolutePath());
        buildFolders = JavaProjectGenerator.guessBuildFolders(evaluator, units, proj1, proj1);
        assertEquals("one build-folder created", 1, buildFolders.size());
        assertEquals("export is properly configured", base2.getAbsolutePath(), buildFolders.get(0));
        
        cu.output.add(getWorkDir().getAbsolutePath());
        buildFolders = JavaProjectGenerator.guessBuildFolders(evaluator, units, proj1, proj1);
        assertEquals("one build-folder created", 1, buildFolders.size());
        assertEquals("export is properly configured", getWorkDir().getAbsolutePath(), buildFolders.get(0));
        
        // check that root of this is handled correctly
        File diskRoot = getWorkDir();
        while (diskRoot.getParentFile() != null) {
            diskRoot = diskRoot.getParentFile();
        }
        cu.output.add(diskRoot.getAbsolutePath());
        buildFolders = JavaProjectGenerator.guessBuildFolders(evaluator, units, proj1, proj1);
        assertEquals("one build-folder created", 1, buildFolders.size());
        assertEquals("export is properly configured", diskRoot.getAbsolutePath(), buildFolders.get(0));
    }
    
    public void testPutBuildFolders() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj", false);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        // check that all data are correctly persisted
        
        List buildFolders = new ArrayList();
        buildFolders.add("/some/path/projA");
        buildFolders.add("C:\\dev\\projB");
        
        JavaProjectGenerator.putBuildFolders(helper, buildFolders);
        Element el = Util.getPrimaryConfigurationData(helper);
        Element foldersEl = XMLUtil.findElement(el, "folders", Util.NAMESPACE);
        assertNotNull("<folders> element exists", foldersEl);
        List subElements = XMLUtil.findSubElements(foldersEl);
        assertEquals("project has two build-folders", 2, subElements.size());
        Element el2 = (Element)subElements.get(0);
        assertElement(el2, "build-folder", null);
        assertEquals("build-folder has one subelement", 1, XMLUtil.findSubElements(el2).size());
        assertElement((Element)XMLUtil.findSubElements(el2).get(0), "location", "/some/path/projA");
        el2 = (Element)subElements.get(1);
        assertElement(el2, "build-folder", null);
        assertEquals("build-folder has one subelement", 1, XMLUtil.findSubElements(el2).size());
        assertElement((Element)XMLUtil.findSubElements(el2).get(0), "location", "C:\\dev\\projB");
        
        ProjectManager.getDefault().saveAllProjects();
        
        // now test updating
        
        buildFolders = new ArrayList();
        buildFolders.add("/projC");
        JavaProjectGenerator.putBuildFolders(helper, buildFolders);
        el = Util.getPrimaryConfigurationData(helper);
        foldersEl = XMLUtil.findElement(el, "folders", Util.NAMESPACE);
        subElements = XMLUtil.findSubElements(foldersEl);
        assertEquals("project has one build-folder", 1, subElements.size());
        el2 = (Element)subElements.get(0);
        assertElement(el2, "build-folder", null);
        assertEquals("build-folder has one subelement", 1, XMLUtil.findSubElements(el2).size());
        assertElement((Element)XMLUtil.findSubElements(el2).get(0), "location", "/projC");
        buildFolders = new ArrayList();
        JavaProjectGenerator.putBuildFolders(helper, buildFolders);
        el = Util.getPrimaryConfigurationData(helper);
        foldersEl = XMLUtil.findElement(el, "folders", Util.NAMESPACE);
        subElements = XMLUtil.findSubElements(foldersEl);
        assertEquals("project has no build-folder", 0, subElements.size());
        
        ProjectManager.getDefault().saveAllProjects();
        
    }    
    
    public void testPutBuildFiles() throws Exception {
        AntProjectHelper helper = createEmptyProject("proj", "proj", false);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", base, p.getProjectDirectory());
        
        List buildFiles = new ArrayList();
        buildFiles.add("/some/path/projA/archive.jar");
        buildFiles.add("C:\\dev\\projB\\library.jar");
        
        JavaProjectGenerator.putBuildFiles(helper, buildFiles);
        Element el = Util.getPrimaryConfigurationData(helper);
        Element foldersEl = XMLUtil.findElement(el, "folders", Util.NAMESPACE);
        assertNotNull("<folders> element exists", foldersEl);
        List subElements = XMLUtil.findSubElements(foldersEl);
        assertEquals("project has two build-files", 2, subElements.size());
        Element el2 = (Element)subElements.get(0);
        assertElement(el2, "build-file", null);
        assertEquals("build-file has one subelement", 1, XMLUtil.findSubElements(el2).size());
        assertElement((Element)XMLUtil.findSubElements(el2).get(0), "location", "/some/path/projA/archive.jar");
        el2 = (Element)subElements.get(1);
        assertElement(el2, "build-file", null);
        assertEquals("build-file has one subelement", 1, XMLUtil.findSubElements(el2).size());
        assertElement((Element)XMLUtil.findSubElements(el2).get(0), "location", "C:\\dev\\projB\\library.jar");
        
        ProjectManager.getDefault().saveAllProjects();
        
        // now test updating
        buildFiles = new ArrayList();
        buildFiles.add("/projC/dist/projC.jar");
        JavaProjectGenerator.putBuildFiles(helper, buildFiles);
        el = Util.getPrimaryConfigurationData(helper);
        foldersEl = XMLUtil.findElement(el, "folders", Util.NAMESPACE);
        subElements = XMLUtil.findSubElements(foldersEl);
        assertEquals("project has one build-file", 1, subElements.size());
        el2 = (Element)subElements.get(0);
        assertElement(el2, "build-file", null);
        assertEquals("build-file has one subelement", 1, XMLUtil.findSubElements(el2).size());
        assertElement((Element)XMLUtil.findSubElements(el2).get(0), "location", "/projC/dist/projC.jar");
        
        buildFiles = new ArrayList();
        JavaProjectGenerator.putBuildFiles(helper, buildFiles);
        el = Util.getPrimaryConfigurationData(helper);
        foldersEl = XMLUtil.findElement(el, "folders", Util.NAMESPACE);
        subElements = XMLUtil.findSubElements(foldersEl);
        assertEquals("project has no build-file", 0, subElements.size());
        
        ProjectManager.getDefault().saveAllProjects();
    }
    
    private static class Listener implements ChangeListener {
        int count = 0;
        public void stateChanged(ChangeEvent ev) {
            count++;
        }
        public void reset() {
            count = 0;
        }
    }

    // create real Jar otherwise FileUtil.isArchiveFile returns false for it
    public void createRealJarFile(File f) throws Exception {
        OutputStream os = new FileOutputStream(f);
        try {
            JarOutputStream jos = new JarOutputStream(os);
//            jos.setMethod(ZipEntry.STORED);
            JarEntry entry = new JarEntry("foo.txt");
//            entry.setSize(0L);
//            entry.setTime(System.currentTimeMillis());
//            entry.setCrc(new CRC32().getValue());
            jos.putNextEntry(entry);
            jos.flush();
            jos.close();
        } finally {
            os.close();
        }
    }

}
