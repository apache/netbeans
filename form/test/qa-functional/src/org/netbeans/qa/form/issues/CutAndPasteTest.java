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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
