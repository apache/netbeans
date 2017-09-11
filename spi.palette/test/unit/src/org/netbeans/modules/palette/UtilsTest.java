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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
