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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 * Test functionality of {@link ModuleLogicalView}.
 * @author Jesse Glick
 */
public class ModuleLogicalViewTest extends TestBase {
    
    public ModuleLogicalViewTest(String name) {
        super(name);
    }
   
    @RandomlyFails // NB-Core-Build #7306: same result for DataObject as for FileObject expected:<null> but was:<FilterNode[Name=FreeformProjectTest...]>
    public void testFindPath() throws Exception {
        Project freeform = ProjectManager.getDefault().findProject(FileUtil.toFileObject(file("ant.freeform")));
        assertNotNull("have project in ant.freeform", freeform);
        LogicalViewProvider lvp = freeform.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        assertNotNull("found arch.xml", find(lvp, "ant.freeform/arch.xml"));
        assertNotNull("found FreeformProject.java", find(lvp, "ant.freeform/src/org/netbeans/modules/ant/freeform/FreeformProject.java"));
        assertNotNull("found freeform-project-general.xsd", find(lvp, "ant.freeform/src/org/netbeans/modules/ant/freeform/resources/freeform-project-general.xsd"));
        assertNotNull("found FreeformProjectTest.java", find(lvp, "ant.freeform/test/unit/src/org/netbeans/modules/ant/freeform/FreeformProjectTest.java"));
        Node layer = find(lvp, "ant.freeform/src/org/netbeans/modules/ant/freeform/resources/layer.xml");
        assertNotNull("Found layer", layer);
        assertEquals("Sources is parent parent of the layer", "${src.dir}", layer.getParentNode().getParentNode().getName());
        assertFalse("Has children: " + layer, layer.isLeaf());
        DataObject obj = layer.getLookup().lookup(DataObject.class);
        assertNotNull("There is a data object", obj);
        Node origLayer = obj.getNodeDelegate();
        assertFalse("Also has children", origLayer.isLeaf());
    }
    
    public void testImportantFilesListening() throws Exception {
        Project p = generateStandaloneModule("module");
        LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        Node iFiles = root.getChildren().findChild(ImportantFilesNodeFactory.IMPORTANT_FILES_NAME);
        assertNotNull("have the Important Files node", iFiles);
        /* XXX inherently unreliable:
        FileObject propsFO = p.getProjectDirectory().getFileObject("nbproject/project.properties");
        propsFO = FileUtil.moveFile(propsFO, p.getProjectDirectory().getFileObject("nbproject"), "project-bck");
        TestBase.assertAsynchronouslyUpdatedChildrenNodes(iFiles, 5);
        FileUtil.moveFile(propsFO, p.getProjectDirectory().getFileObject("nbproject"), "project");
        TestBase.assertAsynchronouslyUpdatedChildrenNodes(iFiles, 6);
         */
    }
    
    private Node find(LogicalViewProvider lvp, String path) throws Exception {
        FileObject f = FileUtil.toFileObject(file(path));
        assertNotNull("found " + path, f);
        Node root = new FilterNode(lvp.createLogicalView());
        
        lvp.findPath(root, f); // ping
        waitForNodesUpdate();
        
        DataObject d = DataObject.find(f);
        Node n = lvp.findPath(root, f);
        assertEquals("same result for DataObject as for FileObject", n, lvp.findPath(root, d));
        if (n != null) {
            assertEquals("right DataObject", d, n.getLookup().lookup(DataObject.class));
        }
        return n;
    }
    
        //    XXX: failing test, fix or delete
//    public void testNewlyCreatedSourceRootsDisplayed() throws Exception { // #72476
//        Project p = generateStandaloneModule("module");
//        LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
//        Node root = lvp.createLogicalView();
//        p.getProjectDirectory().getFileObject("test").delete();
//        Children ch = root.getChildren();
//        assertEquals(Arrays.asList(new String[] {"${src.dir}", "important.files", "libraries"}), findKids(ch));
//        /* XXX does not work reliably; ChildrenArray.finalize removes listener!
//        final boolean[] added = new boolean[1];
//        root.addNodeListener(new NodeAdapter() {
//            public void childrenAdded(NodeMemberEvent ev) {
//                added[0] = true;
//            }
//        });
//         */
//        p.getProjectDirectory().createFolder("javahelp");
//        //assertTrue("got node added event", added[0]);
//        assertEquals(Arrays.asList(new String[] {"${src.dir}", "javahelp", "important.files", "libraries"}), findKids(ch));
//    }
    
    private static List<String> findKids(Children ch) {
        List<String> l = new ArrayList<String>();
        Node[] kids = ch.getNodes(true);
        for (int i = 0; i < kids.length; i++) {
            l.add(kids[i].getName());
        }
        return l;
    }
    
}
