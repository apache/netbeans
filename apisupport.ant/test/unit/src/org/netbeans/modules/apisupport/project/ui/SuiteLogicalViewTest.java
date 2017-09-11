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
