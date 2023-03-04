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
package org.netbeans.qa.form.issues;

import java.io.IOException;
import java.awt.Point;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Issue #103199 test - Undo after Cut&Paste removes componets from designer
 * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=103199">issue</a>
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class CutAndPasteTest extends ExtJellyTestCase {

    /** Constructor required by JUnit */
    public CutAndPasteTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(CutAndPasteTest.class).addTest(
                "testCutAndPaste").gui(true).clusters(".*").enableModules(".*"));

    }

    /** Cut and paste test. */
    public void testCutAndPaste() throws IOException, InterruptedException {
        openDataProjects(_testProjectName);
        String frameName = createJFrameFile();
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        designer.source();
        designer.design();
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                //designer.source();
                //designer.design();
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
               
                Node actNode = new Node(inspector.treeComponents(), "[JFrame]"); // NOI18N
                
                //designer.source();
                //designer.design();
                //actNode.select();
                //Thread.sleep(10000);
                actNode.expand();
                //actNode.callPopup();
                runPopupOverNode("Add From Palette|Swing Controls|Button", actNode);
                
                runPopupOverNode("Add From Palette|Swing Containers|Panel", actNode); // NOI18N
                //runPopupOverNode("Add From Palette|Swing Controls|Button", actNode); // NOI18N
                //designer.source(); 
                //designer.design();
                inspector = new ComponentInspectorOperator();

                actNode = new Node(inspector.treeComponents(), "[JFrame]|jButton1 [JButton]"); // NOI18N
                
                new FormDesignerOperator(null).makeComponentVisible();
                JTreeOperator treeOper = inspector.getTree();
                // do not use Node.performPopup() because it changes context of navigator
                Node node = actNode;
                TreePath treePath = node.getTreePath();
                treeOper.expandPath(treePath.getParentPath());
                treeOper.scrollToPath(treePath);
                Point point = treeOper.getPointToClick(treePath);
                new JPopupMenuOperator(JPopupMenuOperator.callPopup(treeOper, (int) point.getX(), (int) point.getY(), ComponentInspectorOperator.getPopupMouseButton())).pushMenu("Cut");

                inspector = new ComponentInspectorOperator();

                //inspector.restore();

                actNode = new Node(inspector.treeComponents(), "[JFrame]|jPanel1 [JPanel]"); // NOI18N
                
                actNode.expand();
                runPopupOverNode("Paste", actNode); // NOI18N
            }
        });


        findInCode("javax.swing.JButton jButton1", designer); // NOI18N

        removeFile(frameName);
    }
}
