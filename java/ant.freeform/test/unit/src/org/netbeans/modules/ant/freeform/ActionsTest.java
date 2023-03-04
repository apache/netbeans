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

package org.netbeans.modules.ant.freeform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Test functionality of actions in FreeformProject.
 * This class just tests the basic functionality found in the "simple" project.
 * @author Jesse Glick
 */
public class ActionsTest extends TestBase {
    
    private static final class AntTargetInvocation {
        public final FileObject scriptFile;
        public final String[] targetNameArray;
        public final Map<String,String> props;
        public AntTargetInvocation(FileObject scriptFile, String[] targetNameArray, Map<String,String> props) {
            assert scriptFile != null;
            this.scriptFile = scriptFile;
            this.targetNameArray = targetNameArray;
            this.props = props != null ? new HashMap<String,String>(props) : Collections.<String,String>emptyMap();
        }
        public String toString() {
            return "invocation<script=" + scriptFile + ",targets=" + (targetNameArray != null ? Arrays.toString(targetNameArray) : null) + ",props=" + props + ">";
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof AntTargetInvocation)) {
                return false;
            }
            AntTargetInvocation other = (AntTargetInvocation) obj;
            return other.scriptFile == scriptFile &&
                Utilities.compareObjects(other.targetNameArray, targetNameArray) &&
                other.normalizedProps().equals(normalizedProps());
        }
        public int hashCode() {
            int x = scriptFile.hashCode() ^ props.hashCode();
            if (targetNameArray != null) {
                x ^= Arrays.asList(targetNameArray).hashCode();
            }
            return x;
        }
        private Map<String,Set<String>> normalizedProps() {
            Map<String,Set<String>> m = new HashMap<String,Set<String>>();
            for (Map.Entry<String,String> e : props.entrySet()) {
                m.put(e.getKey(), new TreeSet<String>(Arrays.asList(e.getValue().split(","))));
            }
            return m;
        }
    }
    
    private static final List<AntTargetInvocation> targetsRun = new ArrayList<AntTargetInvocation>();
    
    static {
        Actions.TARGET_RUNNER = new Actions.TargetRunner() {
            public void runTarget(FileObject scriptFile, String[] targetNameArray, Properties props, ActionProgress listener) {
                targetsRun.add(new AntTargetInvocation(scriptFile, targetNameArray,
                        NbCollections.checkedMapByFilter(props, String.class, String.class, true)));
            }
        };
    }
    
    public ActionsTest(String name) {
        super(name);
    }
    
    private FileObject buildXml;
    private ActionProvider ap;
    private LogicalViewProvider lvp;
    private DataObject myAppJavaDO, someFileJavaDO, someResourceTxtDO, specialTaskJavaDO;
    
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("sync.project.execution", "true");
        targetsRun.clear();
        buildXml = simple.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("found build.xml", buildXml);
        ap = simple.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        FileObject myAppJava = simple.getProjectDirectory().getFileObject("src/org/foo/myapp/MyApp.java");
        assertNotNull("have MyApp.java", myAppJava);
        myAppJavaDO = DataObject.find(myAppJava);
        FileObject someFileJava = simple.getProjectDirectory().getFileObject("src/org/foo/myapp/SomeFile.java");
        assertNotNull("have SomeFile.java", someFileJava);
        someFileJavaDO = DataObject.find(someFileJava);
        FileObject someResourceTxt = simple.getProjectDirectory().getFileObject("src/org/foo/myapp/some-resource.txt");
        assertNotNull("have some-resource.txt", someResourceTxt);
        someResourceTxtDO = DataObject.find(someResourceTxt);
        FileObject specialTaskJava = simple.getProjectDirectory().getFileObject("antsrc/org/foo/ant/SpecialTask.java");
        assertNotNull("have SpecialTask.java", specialTaskJava);
        specialTaskJavaDO = DataObject.find(specialTaskJava);
        lvp = simple.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
    }
    
    public boolean runInEQ () {
        return true;
    }
    
    public void testBasicActions() throws Exception {
        List<String> actionNames = new ArrayList<String>(Arrays.asList(ap.getSupportedActions()));
        Collections.sort(actionNames);
        assertEquals("right action names", Arrays.asList(
            "build",
            "clean",
            "compile.single",
            "compile.single.noseparator",
            "copy",
            "delete",
            /* Fix for IZ#107597*/
            "deploy",
            "javadoc",
            "move",
            "rebuild",
            // #46886: COMMON_NON_IDE_GLOBAL_ACTIONS are now also enabled
            "redeploy",
            "rename",
            "run",
            "run.single",
            // #46886 again
            "test"),
            actionNames);
        assertTrue("clean is enabled", ap.isActionEnabled("clean", Lookup.EMPTY));
        try {
            ap.isActionEnabled("frobnitz", Lookup.EMPTY);
            fail("Should throw IAE for unrecognized commands");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        try {
            ap.invokeAction("goetterdaemmerung", Lookup.EMPTY);
            fail("Should throw IAE for unrecognized commands");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        ap.invokeAction("rebuild", Lookup.EMPTY);
        AntTargetInvocation inv = new AntTargetInvocation(buildXml, new String[] {"clean", "jar"}, null);
        assertEquals("ran right target", Collections.singletonList(inv), targetsRun);
    }
    
    public void testLogicalViewActions() throws Exception {
        Action[] actions = Actions.contextMenuCustomActions(simple);
        assertNotNull("have some context actions", actions);
        ResourceBundle bundle = NbBundle.getBundle(Actions.class);
        assertEquals("correct labels", Arrays.asList(
            null,
            bundle.getString("CMD_build"),
            bundle.getString("CMD_clean"),
            bundle.getString("CMD_rebuild"),
            null,
            bundle.getString("CMD_run"),
            null,
            bundle.getString("CMD_javadoc"),
            "Generate XDocs",
            null,
            "Create Distribution"),
            findActionLabels(actions));
        Action javadocAction = actions[7];
        assertEquals("this is Run Javadoc", bundle.getString("CMD_javadoc"), javadocAction.getValue(Action.NAME));
        runContextMenuAction(javadocAction, simple);
        AntTargetInvocation inv = new AntTargetInvocation(buildXml, new String[] {"build-javadoc"}, Collections.singletonMap("from-ide", "true"));
        assertEquals("ran right target", Collections.singletonList(inv), targetsRun);
        targetsRun.clear();
        Action xdocsAction = actions[8];
        assertEquals("this is Generate XDocs", "Generate XDocs", xdocsAction.getValue(Action.NAME));
        runContextMenuAction(xdocsAction, simple);
        inv = new AntTargetInvocation(buildXml, new String[] {"generate-xdocs"}, Collections.singletonMap("from-ide", "true"));
        assertEquals("ran right target", Collections.singletonList(inv), targetsRun);
    }
    
    private static List<String> findActionLabels(Action[] actions) {
        String[] labels = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] != null) {
                String label = (String) actions[i].getValue(Action.NAME);
                if (label == null) {
                    label = "???";
                }
                labels[i] = label;
            } else {
                labels[i] = null;
            }
        }
        return Arrays.asList(labels);
    }
    
    /**
     * Run an action as if it were in the context menu of a project.
     */
    private void runContextMenuAction(Action a, Project p) {
        if (a instanceof ContextAwareAction) {
            Lookup l = Lookups.singleton(p);
            a = ((ContextAwareAction) a).createContextAwareInstance(l);
        }
        a.actionPerformed(null);
    }
    
    public void testContextSensitiveActions() throws Exception {
        assertFalse("c.s disabled on empty selection", ap.isActionEnabled("compile.single", Lookup.EMPTY));
        assertTrue("c.s enabled on SomeFile.java", ap.isActionEnabled("compile.single", Lookups.singleton(someFileJavaDO)));
        /* XXX failing: #137764
        assertTrue("c.s enabled on SomeFile.java (FileObject)", ap.isActionEnabled("compile.single", Lookups.singleton(someFileJavaDO.getPrimaryFile())));
         */
        assertTrue("c.s enabled on SpecialTask.java", ap.isActionEnabled("compile.single", Lookups.singleton(specialTaskJavaDO)));
        assertFalse("c.s disabled on some-resource.txt", ap.isActionEnabled("compile.single", Lookups.singleton(someResourceTxtDO)));
        assertTrue("c.s enabled on similar *.java", ap.isActionEnabled("compile.single", Lookups.fixed(someFileJavaDO, myAppJavaDO)));
        assertFalse("c.s disabled on mixed *.java", ap.isActionEnabled("compile.single", Lookups.fixed(someFileJavaDO, specialTaskJavaDO)));
        assertFalse("c.s disabled on mixed types", ap.isActionEnabled("compile.single", Lookups.fixed(someFileJavaDO, someResourceTxtDO)));
        assertFalse("r.s disabled on empty selection", ap.isActionEnabled("run.single", Lookup.EMPTY));
        assertTrue("r.s enabled on SomeFile.java", ap.isActionEnabled("run.single", Lookups.singleton(someFileJavaDO)));
        assertFalse("r.s disabled on SpecialTask.java", ap.isActionEnabled("run.single", Lookups.singleton(specialTaskJavaDO)));
        assertFalse("r.s disabled on some-resource.txt", ap.isActionEnabled("run.single", Lookups.singleton(someResourceTxtDO)));
        assertFalse("r.s disabled on multiple files", ap.isActionEnabled("run.single", Lookups.fixed(someFileJavaDO, myAppJavaDO)));
        ap.invokeAction("compile.single", Lookups.singleton(someFileJavaDO));
        AntTargetInvocation inv = new AntTargetInvocation(buildXml, new String[] {"compile-some-files"}, Collections.singletonMap("files", "org/foo/myapp/SomeFile.java"));
        assertEquals("compiled one file in src", Collections.singletonList(inv), targetsRun);
        /* XXX failing, as above: #137764
        targetsRun.clear();
        ap.invokeAction("compile.single", Lookups.singleton(someFileJavaDO.getPrimaryFile()));
        inv = new AntTargetInvocation(buildXml, new String[] {"compile-some-files"}, Collections.singletonMap("files", "org/foo/myapp/SomeFile.java"));
        assertEquals("compiled one file in src (FileObject)", Collections.singletonList(inv), targetsRun);
         */
        targetsRun.clear();
        ap.invokeAction("compile.single", Lookups.singleton(specialTaskJavaDO));
        inv = new AntTargetInvocation(buildXml, new String[] {"ant-compile-some-files"}, Collections.singletonMap("files", "org/foo/ant/SpecialTask.java"));
        assertEquals("compiled one file in antsrc", Collections.singletonList(inv), targetsRun);
        targetsRun.clear();
        ap.invokeAction("compile.single", Lookups.fixed(someFileJavaDO, myAppJavaDO));
        inv = new AntTargetInvocation(buildXml, new String[] {"compile-some-files"}, Collections.singletonMap("files", "org/foo/myapp/SomeFile.java,org/foo/myapp/MyApp.java"));
        assertEquals("compiled two files in src", Collections.singletonList(inv), targetsRun);
        targetsRun.clear();
        ap.invokeAction("compile.single.noseparator", Lookups.fixed(someFileJavaDO, myAppJavaDO));
        assertEquals("No separator found, so could not compile two files in src", Collections.emptyList(), targetsRun);
        targetsRun.clear();
        ap.invokeAction("run.single", Lookups.singleton(someFileJavaDO));
        inv = new AntTargetInvocation(buildXml, new String[] {"start-with-specified-class"}, Collections.singletonMap("class", "org.foo.myapp.SomeFile"));
        assertEquals("ran one file in src", Collections.singletonList(inv), targetsRun);
    }
    
}
