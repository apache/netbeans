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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.FreeformProjectType;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.w3c.dom.Document;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// XXX testRootNodeDisplayNameChange

/**
 * Test {@link View}: changes in children etc.
 * @author Jesse Glick
 */
public class ViewTest extends TestBase {
    
    public ViewTest(String name) {
        super(name);
    }
    
    public void testViewItemBasic() throws Exception {
        Node root = extsrcroot.getLookup().lookup(LogicalViewProvider.class).createLogicalView();
        assertEquals("lookup has project", extsrcroot, root.getLookup().lookup(Project.class));
        Children ch = root.getChildren();
        Node[] kids = ch.getNodes(true);
        assertEquals("two child nodes", 2, kids.length);
        // Do not check anything about #1, since it is provided by java/freeform.
        assertEquals("correct code name #2", "nbproject/project.xml", kids[1].getName());
        assertEquals("correct display name #2", "project.xml", kids[1].getDisplayName());
        assertEquals("correct cookie #2",
                DataObject.find(egdirFO.getFileObject("extsrcroot/proj/nbproject/project.xml")),
                kids[1].getLookup().lookup(DataObject.class));
    }

    @RandomlyFails // NB-Core-Build #1012
    public void testViewItemChanges() throws Exception {
        FileObject top = FileUtil.toFileObject(copyFolder(FileUtil.toFile(egdirFO.getFileObject("extsrcroot"))));
        FreeformProject extsrcroot_ = (FreeformProject) ProjectManager.getDefault().findProject(top.getFileObject("proj"));
        Node root = extsrcroot_.getLookup().lookup(LogicalViewProvider.class).createLogicalView();
        Children ch = root.getChildren();
        Node[] kids = ch.getNodes(true);
        assertEquals("two child nodes", 2, kids.length);
        assertEquals("correct code name #1", "../src", kids[0].getName());
        assertEquals("correct code name #2", "nbproject/project.xml", kids[1].getName());
        TestNL l = new TestNL();
        root.addNodeListener(l);
        Element data = extsrcroot_.getPrimaryConfigurationData();
        Element view = XMLUtil.findElement(data, "view", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <view>", view);
        Element items = XMLUtil.findElement(view, "items", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <items>", items);
        Element sourceFolder = XMLUtil.findElement(items, "source-folder", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <source-folder>", sourceFolder);
        Element location = XMLUtil.findElement(sourceFolder, "location", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <location>", location);
        NodeList nl = location.getChildNodes();
        assertEquals("one child", 1, nl.getLength());
        location.removeChild(nl.item(0));
        location.appendChild(location.getOwnerDocument().createTextNode("../src2"));
        Element sourceFile =  XMLUtil.findElement(items, "source-file", FreeformProjectType.NS_GENERAL);
        assertNotNull("have <source-file>", sourceFile);
        items.removeChild(sourceFile);
        extsrcroot_.putPrimaryConfigurationData(data);
        // children keys are updated asynchronously. give them a time
        Thread.sleep(500);
        assertFalse("got some changes in children", l.probeChanges().isEmpty());
        kids = ch.getNodes(true);
        assertEquals("one child node", 1, kids.length);
        assertEquals("correct code name #1", "../src2", kids[0].getName());
        assertEquals("correct display name #1", "External Sources", kids[0].getDisplayName());
        assertEquals("correct cookie #1",
                DataObject.find(top.getFileObject("src2")),
                kids[0].getLookup().lookup(DataObject.class));
    }
    
    public void testFindPath() throws Exception {
        // Do not test packages style - provided only by java/freeform.
        LogicalViewProvider lvp2 = simple.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull(lvp2);
        Node root = lvp2.createLogicalView();
        doTestFindPathPositive(lvp2, root, simple, "xdocs/foo.xml");
        doTestFindPathPositive(lvp2, root, simple, "xdocs");
        doTestFindPathPositive(lvp2, root, simple, "build.properties");
        doTestFindPathPositive(lvp2, root, simple, "build.xml");
        doTestFindPathNegative(lvp2, root, simple, "nbproject/project.xml");
        doTestFindPathNegative(lvp2, root, simple, "nbproject");
    }
    
    public static void doTestFindPathPositive(LogicalViewProvider lvp, Node root, Project project, String path) throws Exception {
        FileObject file = project.getProjectDirectory().getFileObject(path);
        assertNotNull("found " + path, file);
        DataObject d = DataObject.find(file);
        Node nDO = lvp.findPath(root, d);
        Node nFO = lvp.findPath(root, file);
        assertNotNull("found node for " + path, nDO);
        assertNotNull("found node for " + path, nFO);
        assertEquals("correct node", d, nDO.getLookup().lookup(DataObject.class));
        //not exactly fullfilling the contract:
        assertEquals("correct node", d, nFO.getLookup().lookup(DataObject.class));
    }
    
    public static void doTestFindPathNegative(LogicalViewProvider lvp, Node root, Project project, String path) throws Exception {
        FileObject file = project.getProjectDirectory().getFileObject(path);
        assertNotNull("found " + path, file);
        DataObject d = DataObject.find(file);
        Node n = lvp.findPath(root, d);
        assertNull("did not find node for " + path, n);
    }
    
    public static void doTestIncludesExcludes(NbTestCase test, String style, String appearanceEverything, String appearanceIncludesExcludes, String appearanceExcludes, String appearanceFloating) throws Exception {
        FolderNodeFactory.synchronous = true;
        test.clearWorkDir();
        File d = test.getWorkDir();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(d, d, "prj", null);
        Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        FileUtil.createData(new File(d, "s/relevant/included/file"));
        FileUtil.createData(new File(d, "s/relevant/excluded/file"));
        FileUtil.createData(new File(d, "s/ignored/file"));
        Element data = Util.getPrimaryConfigurationData(helper);
        Document doc = data.getOwnerDocument();
        Element items = (Element) data.getElementsByTagName("items").item(0);
        items.removeChild(items.getElementsByTagName("source-file").item(0)); // build.xml
        Element sf = (Element) items.appendChild(doc.createElementNS(Util.NAMESPACE, "source-folder"));
        sf.setAttribute("style", style);
        sf.appendChild(doc.createElementNS(Util.NAMESPACE, "location")).appendChild(doc.createTextNode("s"));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        EventQueue.invokeAndWait(new Runnable() {public void run() {}});
        Node r = p.getLookup().lookup(LogicalViewProvider.class).createLogicalView();
        assertEquals(appearanceEverything, expand(r));
        // Now configure includes and excludes.
        EditableProperties ep = new EditableProperties();
        ep.put("includes", "relevant/");
        ep.put("excludes", "**/excluded/");
        helper.putProperties("config.properties", ep);
        data = Util.getPrimaryConfigurationData(helper);
        doc = data.getOwnerDocument();
        data.getElementsByTagName("properties").item(0).
                appendChild(doc.createElementNS(Util.NAMESPACE, "property-file")).
                appendChild(doc.createTextNode("config.properties"));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        EventQueue.invokeAndWait(new Runnable() {public void run() {}});
        data = Util.getPrimaryConfigurationData(helper);
        doc = data.getOwnerDocument();
        sf = (Element) data.getElementsByTagName("source-folder").item(0);
        sf.appendChild(doc.createElementNS(Util.NAMESPACE, "includes")).
                appendChild(doc.createTextNode("${includes}"));
        sf.appendChild(doc.createElementNS(Util.NAMESPACE, "excludes")).
                appendChild(doc.createTextNode("${excludes}"));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        EventQueue.invokeAndWait(new Runnable() {public void run() {}});
        assertEquals(appearanceIncludesExcludes, expand(r));
        // Now change them.
        ep = helper.getProperties("config.properties");
        ep.remove("includes");
        helper.putProperties("config.properties", ep);
        ProjectManager.getDefault().saveProject(p);
        EventQueue.invokeAndWait(new Runnable() {public void run() {}});
        assertEquals(appearanceExcludes, expand(r));
        // Also check floating includes.
        ep = helper.getProperties("config.properties");
        ep.put("includes", "relevant/included/");
        helper.putProperties("config.properties", ep);
        ProjectManager.getDefault().saveProject(p);
        EventQueue.invokeAndWait(new Runnable() {public void run() {}});
        assertEquals(appearanceFloating, expand(r));
    }
    public void testIncludesExcludes() throws Exception {
        doTestIncludesExcludes(this, "tree",
                "prj{s{ignored{file} relevant{excluded{file} included{file}}}}",
                "prj{s{relevant{included{file}}}}",
                "prj{s{ignored{file} relevant{included{file}}}}",
                "prj{s{relevant{included{file}}}}");
    }
    private static String expand(Node n) {
        Node[] kids = n.getChildren().getNodes(true);
        String nm = n.getDisplayName();
        if (kids.length == 0) {
            return nm;
        } else {
            SortedSet<String> under = new TreeSet<String>();
            for (Node kid : kids) {
                under.add(expand(kid));
            }
            StringBuilder b = new StringBuilder(nm).append('{');
            boolean first = true;
            for (String s : under) {
                if (first) {
                    first = false;
                } else {
                    b.append(' ');
                }
                b.append(s);
            }
            return b.append('}').toString();
        }
    }
    
    private static final class TestNL implements NodeListener {
        private final Set<String> changes = new HashSet<String>();
        public TestNL() {}
        public synchronized void childrenRemoved(NodeMemberEvent ev) {
            changes.add("childrenRemoved");
        }
        public synchronized void childrenAdded(NodeMemberEvent ev) {
            changes.add("childrenAdded");
        }
        public synchronized void childrenReordered(NodeReorderEvent ev) {
            changes.add("childrenReordered");
        }
        public synchronized void nodeDestroyed(NodeEvent ev) {
            changes.add("nodeDestroyed");
        }
        public synchronized void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            changes.add(propertyChangeEvent.getPropertyName());
        }
        /** Get a set of all change event names since the last call. Clears set too. */
        public synchronized Set<String> probeChanges() {
            Set<String> _changes = new HashSet<String>(changes);
            changes.clear();
            return _changes;
        }
    }
    
}
