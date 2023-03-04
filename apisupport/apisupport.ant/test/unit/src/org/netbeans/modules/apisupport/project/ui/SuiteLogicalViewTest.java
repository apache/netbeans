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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

/**
 * Test functionality of {@link SuiteLogicalView}.
 *
 * @author Martin Krauskopf
 */
public class SuiteLogicalViewTest extends TestBase {
    
    public SuiteLogicalViewTest(String name) {
        super(name);
    }

    @RandomlyFails // NB-Core-Build #7497: two children expected:<2> but was:<0>
    public void testModulesNode() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        TestBase.generateSuiteComponent(suite1, "module1a");
        Node modulesNode = new ModulesNodeFactory.ModulesNode(suite1);
        modulesNode.getChildren().getNodes(true); // "expand the node" simulation
        waitForGUIUpdate();
        assertEquals("one children", 1, modulesNode.getChildren().getNodes(true).length);
        
        final ModulesNodeFactory.ModulesNode.ModuleChildren children = (ModulesNodeFactory.ModulesNode.ModuleChildren) modulesNode.getChildren();
        TestBase.generateSuiteComponent(suite1, "module1b");
        waitForGUIUpdate();
        assertEquals("two children", 2, children.getNodes(true).length);
        TestBase.generateSuiteComponent(suite1, "module1c");
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                children.stateChanged(null); // #70914
                return null; // #70914
            }
        });
        waitForGUIUpdate();
        assertEquals("three children", 3, children.getNodes(true).length);
    }
    
    public void testNameAndDisplayName() throws Exception {
        SuiteProject p = generateSuite("Sweet Stuff");
        Node n = p.getLookup().lookup(LogicalViewProvider.class).createLogicalView();
        assertEquals("Sweet Stuff", n.getName());
        assertEquals("Sweet Stuff", n.getDisplayName());
        NL nl = new NL();
        n.addNodeListener(nl);
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("app.name", "sweetness");
        ep.setProperty("app.title", "Sweetness is Now!");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        TestBase.waitForNodesUpdate();
        assertEquals(new HashSet<String>(Arrays.asList(Node.PROP_NAME, Node.PROP_DISPLAY_NAME)), nl.changed);
        assertEquals("Sweetness is Now!", n.getName());
        assertEquals("Sweetness is Now!", n.getDisplayName());
    }

    public void testImportantFiles() throws Exception {
        // so getDisplayName is taken from english bundle
        Locale.setDefault(Locale.US);
        
        SuiteProject suite = generateSuite("sweet");
        FileObject master = suite.getProjectDirectory().createData("master.jnlp");
        
        LogicalViewProvider viewProv = suite.getLookup().lookup(LogicalViewProvider.class);
        Node n = viewProv.createLogicalView();
        
        Node[] nodes = n.getChildren().getNodes(true);
        assertEquals("Now there are two", 2, nodes.length);
        assertEquals("Named modules", "modules", nodes[0].getName());
        assertEquals("Named imp files", "important.files", nodes[1].getName());
        
        /* XXX enable once ImportantFilesNodeFactory is rewritten to behave synchronously:
        FileObject projProps = suite.getProjectDirectory().getFileObject("nbproject/project.properties");
        assertNotNull(projProps);
        viewProv.findPath(n, projProps); // ping
        flushRequestProcessor();
        Node nodeForFO = viewProv.findPath(n, projProps);
        
        assertNotNull("found project.properties node", nodeForFO);
        assertEquals("Name of node is localized", "Project Properties", nodeForFO.getDisplayName());
        
        nodeForFO = viewProv.findPath(n, master);
        assertNotNull("found master.jnlp node", nodeForFO);
        assertEquals("same by DataObject", nodeForFO, viewProv.findPath(n, DataObject.find(master)));
        assertEquals("Name of node is localized", "JNLP Descriptor", nodeForFO.getDisplayName());
        
        master.delete();
        
        nodeForFO = viewProv.findPath(n, master);
        assertNull("For file object null", nodeForFO);
         */
    }
    
    private static final class NL extends NodeAdapter {
        public final Set<String> changed = new HashSet<String>();
        public @Override void propertyChange(PropertyChangeEvent evt) {
            changed.add(evt.getPropertyName());
        }
    }
    
    private void waitForGUIUpdate() throws Exception {
        EventQueue.invokeAndWait(new Runnable() { public void run() {} });
    }
    
    private void flushRequestProcessor() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                // flush
            }
        }).waitFinished();
    }
    
}
