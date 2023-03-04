/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.freeform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test how well JavaActions is working to generate targets.
 * @author Jesse Glick
 */
public class JavaActionsTest extends TestBase {
    
    static {
        // Simplify testing of stuff that can include localized messages.
        Locale.setDefault(Locale.US);
    }
    
    public JavaActionsTest(String name) {
        super(name);
    }
    
    private FreeformProject prj;
    private JavaActions ja;
    private FileObject src, myAppJava, someFileJava, someResourceTxt, antsrc, specialTaskJava, buildProperties;

    protected void setUp() throws Exception {
        super.setUp();
        prj = copyProject(simple);
        // Remove existing context-sensitive bindings to make a clean slate.
        Element data = prj.getPrimaryConfigurationData();
        Element ideActions = XMLUtil.findElement(data, "ide-actions", Util.NAMESPACE);
        assertNotNull(ideActions);
        Iterator<Element> actionsIt = XMLUtil.findSubElements(ideActions).iterator();
        while (actionsIt.hasNext()) {
            Element action = actionsIt.next();
            assertEquals("action", action.getLocalName());
            if (XMLUtil.findElement(action, "context", Util.NAMESPACE) != null) {
                ideActions.removeChild(action);
            }
        }
        prj.putPrimaryConfigurationData(data);
        ProjectManager.getDefault().saveProject(prj);
        AuxiliaryConfiguration origAux = prj.getLookup().lookup(AuxiliaryConfiguration.class);
        AuxiliaryConfiguration aux = new LookupProviderImpl.UpgradingAuxiliaryConfiguration(origAux);
        ja = new JavaActions(prj, prj.helper(), prj.evaluator(), aux);
        src = prj.getProjectDirectory().getFileObject("src");
        assertNotNull(src);
        myAppJava = src.getFileObject("org/foo/myapp/MyApp.java");
        assertNotNull(myAppJava);
        someFileJava = src.getFileObject("org/foo/myapp/SomeFile.java");
        assertNotNull(someFileJava);
        someResourceTxt = src.getFileObject("org/foo/myapp/some-resource.txt");
        assertNotNull(someResourceTxt);
        antsrc = prj.getProjectDirectory().getFileObject("antsrc");
        assertNotNull(antsrc);
        specialTaskJava = antsrc.getFileObject("org/foo/ant/SpecialTask.java");
        assertNotNull(specialTaskJava);
        buildProperties = prj.getProjectDirectory().getFileObject("build.properties");
        assertNotNull(buildProperties);
    }
    
    public void testContainsSelectedJavaSources() throws Exception {
        assertTrue(ja.containsSelectedJavaSources(src, contextDO(new FileObject[] {myAppJava})));
        assertFalse(ja.containsSelectedJavaSources(src, contextDO(new FileObject[] {myAppJava, someResourceTxt})));
    }
    
    public void testFindPackageRoot() throws Exception {
        Lookup context = contextDO(new FileObject[] {myAppJava});
        JavaActions.AntLocation loc = ja.findPackageRoot(context);
        assertNotNull("found a package root for " + context, loc);
        assertEquals("right name", "${src.dir}", loc.virtual);
        assertEquals("right physical", src, loc.physical);
        context = contextDO(new FileObject[] {myAppJava, someFileJava});
        loc = ja.findPackageRoot(context);
        assertNotNull("found a package root for " + context, loc);
        assertEquals("right name", "${src.dir}", loc.virtual);
        assertEquals("right physical", src, loc.physical);
        context = contextDO(new FileObject[] {src});
        loc = ja.findPackageRoot(context);
        assertNotNull("found a package root for " + context, loc);
        assertEquals("right name", "${src.dir}", loc.virtual);
        assertEquals("right physical", src, loc.physical);
        context = contextDO(new FileObject[] {myAppJava, someResourceTxt});
        loc = ja.findPackageRoot(context);
        assertNull("found no package root for " + context + ": " + loc, loc);
        context = contextDO(new FileObject[] {myAppJava, specialTaskJava});
        loc = ja.findPackageRoot(context);
        assertNull("found no package root for " + context, loc);
        context = contextDO(new FileObject[] {});
        loc = ja.findPackageRoot(context);
        assertNull("found no package root for " + context, loc);
        context = contextDO(new FileObject[] {specialTaskJava});
        loc = ja.findPackageRoot(context);
        assertNotNull("found a package root for " + context, loc);
        assertEquals("right name", "${ant.src.dir}", loc.virtual);
        assertEquals("right physical", antsrc, loc.physical);
        context = contextDO(new FileObject[] {buildProperties});
        loc = ja.findPackageRoot(context);
        assertNull("found no package root for " + context, loc);
    }
    
    public void testGetSupportedActions() throws Exception {
        assertEquals("initially all context-sensitive actions supported",
            Arrays.asList(new String[] {
                ActionProvider.COMMAND_COMPILE_SINGLE,
                ActionProvider.COMMAND_DEBUG,
                ActionProvider.COMMAND_PROFILE,
                ActionProvider.COMMAND_RUN_SINGLE,
                ActionProvider.COMMAND_DEBUG_SINGLE,
                ActionProvider.COMMAND_PROFILE_SINGLE
            }),
            Arrays.asList(ja.getSupportedActions()));
        /* Not really necessary; once there is a binding, the main ant/freeform Actions will mask this anyway:
        ja.addBinding(ActionProvider.COMMAND_COMPILE_SINGLE, "target", "prop", "${dir}", null, "relative-path", null);
        assertEquals("binding a context-sensitive action makes it not be supported any longer",
            Collections.EMPTY_LIST,
            Arrays.asList(ja.getSupportedActions()));
         */
    }
    
    public void testIsActionEnabled() throws Exception {
        assertTrue("enabled on some source files", ja.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context(new FileObject[] {myAppJava, someFileJava})));
        assertFalse("disabled on other stuff", ja.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, context(new FileObject[] {buildProperties})));
    }
    
    private static boolean useDO = false; // exercise lookup of both FO and DO
    private Lookup context(FileObject[] files) throws Exception {
        Object[] objs = new Object[files.length];
        for (int i = 0; i < files.length; i++) {
            objs[i] = useDO ? (Object) DataObject.find(files[i]) : files[i];
            useDO = !useDO;
        }
        return Lookups.fixed(objs);
    }
    
    private Lookup contextDO(FileObject[] files) throws Exception {
        Object[] objs = new Object[files.length];
        for (int i = 0; i < files.length; i++) {
            objs[i] = (Object) DataObject.find(files[i]);
        }
        return Lookups.fixed(objs);
    }
    
    public void testFindClassesOutputDir() throws Exception {
        assertEquals("Output for src", "${classes.dir}", ja.findClassesOutputDir("${src.dir}"));
        assertEquals("Output for antsrc", "${ant.classes.dir}", ja.findClassesOutputDir("${ant.src.dir}"));
        assertEquals("No output for bogussrc", null, ja.findClassesOutputDir("${bogus.src.dir}"));
    }
    
    public void testAddBinding() throws Exception {
        ja.addBinding("some.action", "special.xml", "special-target", "selection", "${some.src.dir}", "\\.java$", "relative-path", ",");
        Element data = prj.getPrimaryConfigurationData();
        assertNotNull(data);
        Element ideActions = XMLUtil.findElement(data, "ide-actions", Util.NAMESPACE);
        assertNotNull(ideActions);
        List<Element> actions = XMLUtil.findSubElements(ideActions);
        Element lastAction = actions.get(actions.size() - 1);
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<action xmlns=\"" + Util.NAMESPACE + "\" name=\"some.action\">\n" +
            "    <script>special.xml</script>\n" +
            "    <target>special-target</target>\n" +
            "    <context>\n" +
            "        <property>selection</property>\n" +
            "        <folder>${some.src.dir}</folder>\n" +
            "        <pattern>\\.java$</pattern>\n" +
            "        <format>relative-path</format>\n" +
            "        <arity>\n" +
            "            <separated-files>,</separated-files>\n" +
            "        </arity>\n" +
            "    </context>\n" +
            "</action>\n";
        assertEquals(expectedXml, xmlToString(lastAction));
        ja.addBinding("some.other.action", "special.xml", "special-target", "selection", "${some.src.dir}", null, "relative-path", null);
        data = prj.getPrimaryConfigurationData();
        ideActions = XMLUtil.findElement(data, "ide-actions", Util.NAMESPACE);
        actions = XMLUtil.findSubElements(ideActions);
        lastAction = actions.get(actions.size() - 1);
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<action xmlns=\"" + Util.NAMESPACE + "\" name=\"some.other.action\">\n" +
            "    <script>special.xml</script>\n" +
            "    <target>special-target</target>\n" +
            "    <context>\n" +
            "        <property>selection</property>\n" +
            "        <folder>${some.src.dir}</folder>\n" +
            "        <format>relative-path</format>\n" +
            "        <arity>\n" +
            "            <one-file-only/>\n" +
            "        </arity>\n" +
            "    </context>\n" +
            "</action>\n";
        assertEquals(expectedXml, xmlToString(lastAction));
        // Non-context-sensitive bindings have no <context> but need to add a view item.
        ja.addBinding("general.action", "special.xml", "special-target", null, null, null, null, null);
        data = prj.getPrimaryConfigurationData();
        ideActions = XMLUtil.findElement(data, "ide-actions", Util.NAMESPACE);
        actions = XMLUtil.findSubElements(ideActions);
        lastAction = actions.get(actions.size() - 1);
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<action xmlns=\"" + Util.NAMESPACE + "\" name=\"general.action\">\n" +
            "    <script>special.xml</script>\n" +
            "    <target>special-target</target>\n" +
            "</action>\n";
        assertEquals(expectedXml, xmlToString(lastAction));
        Element view = XMLUtil.findElement(data, "view", Util.NAMESPACE);
        assertNotNull(view);
        Element contextMenu = XMLUtil.findElement(view, "context-menu", Util.NAMESPACE);
        assertNotNull(contextMenu);
        // Currently (no FPG to help) it is always added as the last item.
        List<Element> contextMenuActions = XMLUtil.findSubElements(contextMenu);
        Element lastContextMenuAction = contextMenuActions.get(contextMenuActions.size() - 1);
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<ide-action xmlns=\"" + Util.NAMESPACE + "\" name=\"general.action\"/>\n";
        assertEquals(expectedXml, xmlToString(lastContextMenuAction));
        
        //test #58442:
        data = prj.getPrimaryConfigurationData();
        ideActions = XMLUtil.findElement(data, "ide-actions", Util.NAMESPACE);
        data.removeChild(ideActions);
        
        ja.addBinding("some.other.action", "special.xml", "special-target", "selection", "${some.src.dir}", null, "relative-path", null);
        data = prj.getPrimaryConfigurationData();
        ideActions = XMLUtil.findElement(data, "ide-actions", Util.NAMESPACE);
        actions = XMLUtil.findSubElements(ideActions);
        lastAction = actions.get(actions.size() - 1);
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<action xmlns=\"" + Util.NAMESPACE + "\" name=\"some.other.action\">\n" +
            "    <script>special.xml</script>\n" +
            "    <target>special-target</target>\n" +
            "    <context>\n" +
            "        <property>selection</property>\n" +
            "        <folder>${some.src.dir}</folder>\n" +
            "        <format>relative-path</format>\n" +
            "        <arity>\n" +
            "            <one-file-only/>\n" +
            "        </arity>\n" +
            "    </context>\n" +
            "</action>\n";
        assertEquals(expectedXml, xmlToString(lastAction));
    }
    
    public void testCreateCompileSingleTarget() throws Exception {
        Document doc = XMLUtil.createDocument("fake", null, null, null);
        Lookup context = context(new FileObject[] {someFileJava});
        Element target = ja.createCompileSingleTarget(doc, context, "files", new JavaActions.AntLocation("${src.dir}", src));
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<target name=\"compile-selected-files-in-src\">\n" +
            "    <fail unless=\"files\">Must set property 'files'</fail>\n" +
            "    <mkdir dir=\"${classes.dir}\"/>\n" +
            "    <javac destdir=\"${classes.dir}\" includes=\"${files}\" source=\"1.4\" srcdir=\"${src.dir}\">\n" +
            "        <classpath path=\"${src.cp}\"/>\n" +
            "    </javac>\n" +
            "</target>\n";
        assertEquals(expectedXml, xmlToString(target));
    }
    
    public void testReadWriteCustomScript() throws Exception {
        Document script = ja.readCustomScript(JavaActions.FILE_SCRIPT_PATH);
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project name=\"Simple Freeform Project-IDE\"/>\n";
        assertEquals(expectedXml, xmlToString(script.getDocumentElement()));
        script.getDocumentElement().appendChild(script.createElement("foo"));
        ja.writeCustomScript(script, JavaActions.FILE_SCRIPT_PATH);
        script = ja.readCustomScript(JavaActions.FILE_SCRIPT_PATH);
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project name=\"Simple Freeform Project-IDE\">\n" +
            "    <foo/>\n" +
            "</project>\n";
        assertEquals(expectedXml, xmlToString(script.getDocumentElement()));
    }
    
    public void testFindSourceLevel() throws Exception {
        assertEquals("1.4", ja.findSourceLevel("${src.dir}"));
        assertEquals("1.4", ja.findSourceLevel("${ant.src.dir}"));
        assertEquals(null, ja.findSourceLevel("${bogus.src.dir}"));
    }
    
    public void testFindCompileClasspath() throws Exception {
        assertEquals("${src.cp}", ja.findCUClasspath("${src.dir}", "compile"));
        assertEquals("${ant.src.cp}", ja.findCUClasspath("${ant.src.dir}", "compile"));
        assertEquals(null, ja.findCUClasspath("${bogus.src.dir}", "compile"));
    }
    
    public void testFindLine() throws Exception {
        Document script = ja.readCustomScript("special.xml");
        Element target = script.createElement("target");
        target.setAttribute("name", "targ1");
        target.appendChild(script.createElement("task1"));
        target.appendChild(script.createElement("task2"));
        script.getDocumentElement().appendChild(target);
        target = script.createElement("target");
        target.setAttribute("name", "targ2");
        target.appendChild(script.createElement("task3"));
        script.getDocumentElement().appendChild(target);
        ja.writeCustomScript(script, "special.xml");
        FileObject scriptFile = prj.getProjectDirectory().getFileObject("special.xml");
        assertNotNull(scriptFile);
        //0 <?xml?>
        //1 <project>
        //2     <target name="targ1">
        //3         <task1/>
        //4         <task2/>
        //5     </>
        //6     <target name="targ2">
        //7         <task3/>
        //8     </>
        //9 </>
        assertEquals(2, JavaActions.findLine(scriptFile, "targ1", "target", "name"));
        assertEquals(6, JavaActions.findLine(scriptFile, "targ2", "target", "name"));
        assertEquals(-1, JavaActions.findLine(scriptFile, "no-such-targ", "target", "name"));
        // Try w/ project.xml which uses namespaces, too.
        FileObject pxml = prj.getProjectDirectory().getFileObject("nbproject/project.xml");
        assertNotNull(pxml);
        assertTrue(JavaActions.findLine(pxml, "build", "action", "name") != -1);
        assertEquals(-1, JavaActions.findLine(pxml, "nonexistent", "action", "name"));
    }
    
    public void testFindCommandBinding() throws Exception {
        String[] binding = ja.findCommandBinding(ActionProvider.COMMAND_RUN);
        assertNotNull(binding);
        assertEquals(Arrays.asList(new String[] {"build.xml", "start"}), Arrays.asList(binding));
        binding = ja.findCommandBinding(ActionProvider.COMMAND_REBUILD);
        assertNotNull(binding);
        assertEquals(Arrays.asList(new String[] {"build.xml", "clean", "jar"}), Arrays.asList(binding));
        binding = ja.findCommandBinding("bogus");
        assertNull(binding);
    }
    
    public void testFindExistingBuildTarget() throws Exception {
        Element target = ja.findExistingBuildTarget(ActionProvider.COMMAND_RUN);
        assertNotNull("found a target for 'run'", target);
        assertEquals("found correct target", "start", target.getAttribute("name"));
    }
    
    public void testTargetUsesTaskExactlyOnce() throws Exception {
        Element runTarget = ja.findExistingBuildTarget(ActionProvider.COMMAND_RUN);
        Element javaTask = ja.targetUsesTaskExactlyOnce(runTarget, "java");
        assertNotNull("found <java>", javaTask);
        assertEquals("java", javaTask.getLocalName());
        assertEquals("org.foo.myapp.MyApp", javaTask.getAttribute("classname"));
        assertNull("no <javac> here", ja.targetUsesTaskExactlyOnce(runTarget, "javac"));
        Element cleanTarget = ja.findExistingBuildTarget(ActionProvider.COMMAND_CLEAN);
        assertNotNull(cleanTarget);
        assertNull(">1 <delete> found so skipping", ja.targetUsesTaskExactlyOnce(cleanTarget, "delete"));
    }
    
    public void testEnsurePropertiesCopied() throws Exception {
        Document doc = XMLUtil.createDocument("project", null, null, null);
        Element root = doc.getDocumentElement();
        ja.ensurePropertiesCopied(root);
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project basedir=\"..\">\n" +
            "    <property name=\"build.properties\" value=\"build.properties\"/>\n" +
            "    <property file=\"${build.properties}\"/>\n" +
            "</project>\n";
        assertEquals("Correct code generated", expectedXml, xmlToString(root));
        ja.ensurePropertiesCopied(root);
        assertEquals("Idempotent", expectedXml, xmlToString(root));
    }
    
    public void testEnsureImports() throws Exception {
        // Start with the simple case:
        Element root = XMLUtil.createDocument("project", null, null, null).getDocumentElement();
        ja.ensureImports(root, "build.xml");
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project basedir=\"..\">\n" +
            "    <import file=\"../build.xml\"/>\n" +
            "</project>\n";
        assertEquals("Correct code generated", expectedXml, xmlToString(root));
        ja.ensureImports(root, "build.xml");
        assertEquals("Idempotent", expectedXml, xmlToString(root));
        // Test strange locations too. Make a script somewhere different.
        File testdir = getWorkDir();
        File subtestdir = new File(testdir, "sub");
        subtestdir.mkdir();
        File script = new File(subtestdir, "external.xml");
        Document doc = XMLUtil.createDocument("project", null, null, null);
        doc.getDocumentElement().setAttribute("basedir", "..");
        OutputStream os = new FileOutputStream(script);
        try {
            XMLUtil.write(doc, os, "UTF-8");
        } finally {
            os.close();
        }
        root = XMLUtil.createDocument("project", null, null, null).getDocumentElement();
        String scriptPath = script.getAbsolutePath();
        ja.ensureImports(root, scriptPath);
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project basedir=\"" + testdir.getAbsolutePath() + "\">\n" +
            "    <import file=\"" + scriptPath + "\"/>\n" +
            "</project>\n";
        assertEquals("Correct code generated for external script", expectedXml, xmlToString(root));
        // And also with locations defined as special properties in various ways...
        Element data = prj.getPrimaryConfigurationData();
        Element properties = XMLUtil.findElement(data, "properties", Util.NAMESPACE);
        assertNotNull(properties);
        Element property = data.getOwnerDocument().createElementNS(Util.NAMESPACE, "property");
        property.setAttribute("name", "external.xml");
        property.appendChild(data.getOwnerDocument().createTextNode(scriptPath));
        properties.appendChild(property);
        property = data.getOwnerDocument().createElementNS(Util.NAMESPACE, "property");
        property.setAttribute("name", "subtestdir");
        property.appendChild(data.getOwnerDocument().createTextNode(subtestdir.getAbsolutePath()));
        properties.appendChild(property);
        property = data.getOwnerDocument().createElementNS(Util.NAMESPACE, "property");
        property.setAttribute("name", "testdir");
        property.appendChild(data.getOwnerDocument().createTextNode(testdir.getAbsolutePath()));
        properties.appendChild(property);
        prj.putPrimaryConfigurationData(data);
        ProjectManager.getDefault().saveProject(prj); // ease of debugging
        root = XMLUtil.createDocument("project", null, null, null).getDocumentElement();
        ja.ensureImports(root, "${external.xml}");
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project basedir=\"" + testdir.getAbsolutePath() + "\">\n" +
            "    <import file=\"" + scriptPath +  "\"/>\n" +
            "</project>\n";
        assertEquals("Correct code generated for ${external.xml}", expectedXml, xmlToString(root));
        root = XMLUtil.createDocument("project", null, null, null).getDocumentElement();
        ja.ensureImports(root, "${subtestdir}" + File.separator + "external.xml");
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project basedir=\"" + testdir.getAbsolutePath() + "\">\n" +
            "    <import file=\"" + scriptPath +  "\"/>\n" +
            "</project>\n";
        assertEquals("Correct code generated for ${subtestdir}/external.xml", expectedXml, xmlToString(root));
        root = XMLUtil.createDocument("project", null, null, null).getDocumentElement();
        ja.ensureImports(root, "${testdir}" + File.separator + "sub" +File.separator + "external.xml");
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project basedir=\"" + testdir.getAbsolutePath() + "\">\n" +
            "    <import file=\"" + scriptPath +  "\"/>\n" +
            "</project>\n";
        assertEquals("Correct code generated for ${testdir}/sub/external.xml", expectedXml, xmlToString(root));
        // XXX try also <import file="somewhere-relative/build.xml"/>
    }
    
    public void testCreateDebugTargetFromTemplate() throws Exception {
        Document doc = XMLUtil.createDocument("project", null, null, null);
        Document origDoc = XMLUtil.createDocument("target", null, null, null);
        Element origTarget = origDoc.getDocumentElement();
        origTarget.setAttribute("name", "ignored");
        origTarget.setAttribute("depends", "compile");
        origTarget.appendChild(origDoc.createElement("task1"));
        Element task = origDoc.createElement("java");
        // XXX also test nested <classpath>:
        task.setAttribute("classpath", "${cp}");
        task.appendChild(origDoc.createElement("stuff"));
        origTarget.appendChild(task);
        origTarget.appendChild(origDoc.createElement("task2"));
        Element genTarget = ja.createDebugTargetFromTemplate("debug", origTarget, task, doc);
        doc.getDocumentElement().appendChild(genTarget);
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project>\n" +
            "    <target depends=\"compile\" name=\"debug\">\n" +
            "        <task1/>\n" +
            "        <nbjpdastart addressproperty=\"jpda.address\" name=\"Simple Freeform Project\" transport=\"dt_socket\">\n" +
            "            <classpath path=\"${cp}\"/>\n" +
            "        </nbjpdastart>\n" +
            "        <java classpath=\"${cp}\" fork=\"true\">\n" +
            "            <stuff/>\n" +
            "            <jvmarg value=\"-agentlib:jdwp=transport=dt_socket,address=${jpda.address}\"/>\n" +
            "        </java>\n" +
            "        <task2/>\n" +
            "    </target>\n" +
            "</project>\n";
        assertEquals(expectedXml, xmlToString(doc.getDocumentElement()));
    }
    
    public void testCreateDebugTargetFromScratch() throws Exception {
        Document doc = XMLUtil.createDocument("project", null, null, null);
        Element genTarget = ja.createDebugTargetFromScratch("debug", doc);
        doc.getDocumentElement().appendChild(genTarget);
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project>\n" +
            "    <target name=\"debug\">\n" +
            "        <path id=\"cp\">\n" +
            "            <!---->\n" +
            "        </path>\n" +
            "        <nbjpdastart addressproperty=\"jpda.address\" name=\"Simple Freeform Project\" transport=\"dt_socket\">\n" +
            "            <classpath refid=\"cp\"/>\n" +
            "        </nbjpdastart>\n" +
            "        <!---->\n" +
            "        <java classname=\"some.main.Class\" fork=\"true\">\n" +
            "            <classpath refid=\"cp\"/>\n" +
            "            <jvmarg value=\"-agentlib:jdwp=transport=dt_socket,address=${jpda.address}\"/>\n" +
            "        </java>\n" +
            "    </target>\n" +
            "</project>\n";
        assertEquals(expectedXml, xmlToString(doc.getDocumentElement()));
    }
    
    public void testCreateProfileTargetFromScratch() throws Exception {
        Document doc = XMLUtil.createDocument("project", null, null, null);
        Element genTarget = ja.createProfileTargetFromScratch("profile", doc);
        doc.getDocumentElement().appendChild(genTarget);
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project>\n" +
            "    <target name=\"-profile-check\">\n" +
            "        <startprofiler freeform=\"true\"/>\n" +
            "    </target>\n" +
            "    <target depends=\"-profile-check\" if=\"profiler.configured\" name=\"profile\">\n" +
            "        <path id=\"cp\">\n" +
            "            <!---->\n" +
            "        </path>\n" +
            "        <!---->\n" +
            "        <java classname=\"some.main.Class\" fork=\"true\">\n" +
            "            <classpath refid=\"cp\"/>\n" +
            "            <jvmarg line=\"${agent.jvmargs}\"/>\n" +
            "        </java>\n" +
            "    </target>\n" +
            "</project>\n";
        assertEquals(expectedXml, xmlToString(doc.getDocumentElement()));
    }
    
    public void testCreateProfileTargetFromTemplate() throws Exception {
        Document doc = XMLUtil.createDocument("project", null, null, null);
        Document origDoc = XMLUtil.createDocument("target", null, null, null);
        Element origTarget = origDoc.getDocumentElement();
        origTarget.setAttribute("name", "ignored");
        origTarget.setAttribute("depends", "compile");
        origTarget.appendChild(origDoc.createElement("task1"));
        Element task = origDoc.createElement("java");
        // XXX also test nested <classpath>:
        task.setAttribute("classpath", "${cp}");
        task.appendChild(origDoc.createElement("stuff"));
        origTarget.appendChild(task);
        origTarget.appendChild(origDoc.createElement("task2"));
        Element genTarget = ja.createProfileTargetFromTemplate("profile", origTarget, task, doc);
        doc.getDocumentElement().appendChild(genTarget);
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project>\n" +
            "    <target name=\"-profile-check\">\n" +
            "        <startprofiler freeform=\"true\"/>\n" +
            "    </target>\n" +
            "    <target depends=\"-profile-check\" if=\"profiler.configured\" name=\"profile\">\n" +
            "        <task1/>\n" +
            "        <java classpath=\"${cp}\" fork=\"true\">\n" +
            "            <stuff/>\n" +
            "            <jvmarg line=\"${agent.jvmargs}\"/>\n" +
            "        </java>\n" +
            "        <task2/>\n" +
            "    </target>\n" +
            "</project>\n";
        assertEquals(expectedXml, xmlToString(doc.getDocumentElement()));
    }

    public void testCreateRunSingleTargetElem() throws Exception {
        Document doc = XMLUtil.createDocument("project", null, null, null);
        Lookup context = contextDO(new FileObject[] {myAppJava});
        JavaActions.AntLocation root = ja.findPackageRoot(context);
        Element targetElem = ja.createRunSingleTargetElem(doc, "run-single-test-target", "test.class", root);
        doc.getDocumentElement().appendChild(targetElem);
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project>\n" +
            "    <target name=\"run-single-test-target\">\n" +
            "        <fail unless=\"test.class\">Must set property 'test.class'</fail>\n" +
            "        <ant antfile=\"build.xml\" inheritall=\"false\" target=\"jar\"/>\n" +
            "        <java classname=\"${test.class}\" failonerror=\"true\" fork=\"true\">\n" +
            "            <classpath>\n" +
            "                <pathelement path=\"${src.cp}\"/>\n" +
            "                <pathelement location=\"${classes.dir}\"/>\n" +
            "                <pathelement location=\"${main.jar}\"/>\n" +
            "            </classpath>\n" +
            "        </java>\n" +
            "    </target>\n" +
            "</project>\n";
        assertEquals(expectedXml, xmlToString(doc.getDocumentElement()));
    }

    public void testCreateDebugSingleTargetElem() throws Exception {
        Document doc = XMLUtil.createDocument("project", null, null, null);
        Lookup context = contextDO(new FileObject[] {myAppJava});
        JavaActions.AntLocation root = ja.findPackageRoot(context);
        Element targetElem = ja.createDebugSingleTargetElem(doc, "debug-single-test-target", "test.class", root);
        doc.getDocumentElement().appendChild(targetElem);
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project>\n" +
            "    <target name=\"debug-single-test-target\">\n" +
            "        <fail unless=\"test.class\">Must set property 'test.class'</fail>\n" +
            "        <ant antfile=\"build.xml\" inheritall=\"false\" target=\"jar\"/>\n" +
            "        <path id=\"cp\">\n" +
            "            <pathelement path=\"${src.cp}\"/>\n" +
            "            <pathelement location=\"${classes.dir}\"/>\n" +
            "            <pathelement location=\"${main.jar}\"/>\n" +
            "        </path>\n" +
            "        <nbjpdastart addressproperty=\"jpda.address\" name=\"Simple Freeform Project\" transport=\"dt_socket\">\n" +
            "            <classpath refid=\"cp\"/>\n" +
            "        </nbjpdastart>\n" +
            "        <java classname=\"${test.class}\" fork=\"true\">\n" +
            "            <classpath refid=\"cp\"/>\n" +
            "            <jvmarg value=\"-agentlib:jdwp=transport=dt_socket,address=${jpda.address}\"/>\n" +
            "        </java>\n" +
            "    </target>\n" +
            "</project>\n";
        assertEquals(expectedXml, xmlToString(doc.getDocumentElement()));
    }

    public void testCreatePathLikeElem() throws Exception {
        Document doc = XMLUtil.createDocument("testdoc", null, null, null);
        Element pathElem = ja.createPathLikeElem(doc, "path", "id",
                new String[] {"lib/File.jar;lib/File2.jar", "testlib/Lib1.jar;testlib/Lib2"},
                new String[] {"c:\\workfiles\\library.jar", "/workfiles/library2.jar"},
                "refid", "comment");
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<path id=\"id\" refid=\"refid\">\n" +
            "    <!---->\n" +
            "    <pathelement path=\"lib/File.jar;lib/File2.jar\"/>\n" +
            "    <pathelement path=\"testlib/Lib1.jar;testlib/Lib2\"/>\n" +
            "    <pathelement location=\"c:\\workfiles\\library.jar\"/>\n" +
            "    <pathelement location=\"/workfiles/library2.jar\"/>\n" +
            "</path>\n";
        assertEquals(expectedXml, xmlToString(pathElem));
        pathElem = ja.createPathLikeElem(doc, "classpath", null, null, null, null, "comment");
        expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<classpath>\n" +
            "    <!---->\n" +
            "</classpath>\n";
        assertEquals(expectedXml, xmlToString(pathElem));
    }

    public void testCreateAntElem() throws Exception {
        Document doc = XMLUtil.createDocument("testdoc", null, null, null);
        Element antElem = ja.createAntElem(doc, "antscript.xml", "test.target");
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<ant antfile=\"antscript.xml\" inheritall=\"false\" target=\"test.target\"/>\n";
        assertEquals(expectedXml, xmlToString(antElem));
    }

    public void testGetPathFromCU() throws Exception {
        Document doc = XMLUtil.createDocument("testdoc", null, null, null);
        Lookup context = contextDO(new FileObject[] {myAppJava});
        JavaActions.AntLocation root = ja.findPackageRoot(context);
        Element cpElem = ja.getPathFromCU(doc, root.virtual, "classpath");
        String expectedXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<classpath>\n" +
            "    <pathelement path=\"${src.cp}\"/>\n" +
            "    <pathelement location=\"${classes.dir}\"/>\n" +
            "    <pathelement location=\"${main.jar}\"/>\n" +
            "</classpath>\n";
        assertEquals(expectedXml, xmlToString(cpElem));
    }

    public void testGetRunDepends() throws Exception {
        String s[] = ja.getRunDepends();
        assertEquals("build.xml", s[0]);
        assertEquals("jar", s[1]);
    }

    /**
     * Format XML as a string. Assumes Xerces serializer in current impl.
     * Collapse all comments to no body.
     */
    private static String xmlToString(Element el) throws Exception {
        Document doc = XMLUtil.createDocument("fake", null, null, null);
        doc.removeChild(doc.getDocumentElement());
        doc.appendChild(doc.importNode(el, true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        return baos.toString("UTF-8").replaceAll("<!--([^-]|-[^-])*-->", "<!---->").replaceAll(System.getProperty("line.separator"), "\n");
    }
    
}
