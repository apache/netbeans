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
package org.netbeans.modules.palette;

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.palette.Utils.SortCategoriesAction;
import java.util.Arrays;
import org.netbeans.spi.palette.AbstractPaletteTestHid;
import org.netbeans.spi.palette.DummyActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;

/**
 *
 * @author Jiri Skrivanek
 */
public class UtilsTest extends AbstractPaletteTestHid {

    public UtilsTest(String testName) {
        super(testName);
    }

    /** Tests order of nodes after SortCategoriesAction is performed (see issue 146337). */
    public void testSortAction() throws Exception {
        FileObject rootFO = FileUtil.getConfigRoot().createFolder(getName());
        FileObject cat1FO = rootFO.createData("cat1");
        FileObject cat2FO = rootFO.createData("cat2");
        FileObject cat3FO = rootFO.createData("cat3");
        FileObject cat4FO = rootFO.createData("cat4");
        cat1FO.setAttribute("position", 100);
        cat4FO.setAttribute("position", 200);
        cat2FO.setAttribute("position", 300);
        cat3FO.setAttribute("position", 400);

        PaletteController pc = PaletteFactory.createPalette(rootFO.getNameExt(), new DummyActions());
        Model model = getModel(pc);
        Node rootNode = model.getRoot().lookup(Node.class);

        rootNode.addNodeListener(new NodeListener() {

            public void childrenAdded(NodeMemberEvent ev) {
            }

            public void childrenRemoved(NodeMemberEvent ev) {
            }

            public void childrenReordered(NodeReorderEvent ev) {
                reordered[0] = true;
            }

            public void nodeDestroyed(NodeEvent ev) {
            }

            public void propertyChange(PropertyChangeEvent evt) {
            }
        });

        SortCategoriesAction sortAction = new SortCategoriesAction(rootNode);
        sortAction.actionPerformed(null);
        waitReordered();
        String[] expectedOrder = {"cat1", "cat2", "cat3", "cat4"};
        assertOrder(expectedOrder, rootNode);

        cat4FO.setAttribute("position", 100);
        cat3FO.setAttribute("position", 200);
        cat2FO.setAttribute("position", 300);
        cat1FO.setAttribute("position", 400);
        waitReordered();
        sortAction.actionPerformed(null);
        waitReordered();
        assertOrder(expectedOrder, rootNode);

        cat2FO.setAttribute("position", 100);
        cat1FO.setAttribute("position", 200);
        cat4FO.setAttribute("position", 300);
        cat3FO.setAttribute("position", 400);
        waitReordered();
        sortAction.actionPerformed(null);
        waitReordered();
        assertOrder(expectedOrder, rootNode);
    }

    private void assertOrder(String[] expectedOrder, Node rootNode) {
        Node[] nodes = rootNode.getChildren().getNodes();
        String[] order = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            order[i] = nodes[i].getDisplayName();
        }
        assertEquals("Wrong order of nodes.", Arrays.asList(expectedOrder), Arrays.asList(order));
    }

    final boolean reordered[] = {false};

    private void waitReordered() throws InterruptedException {
        while (!reordered[0]) {
            Thread.sleep(100);
        }
        reordered[0] = false;
    }

}
