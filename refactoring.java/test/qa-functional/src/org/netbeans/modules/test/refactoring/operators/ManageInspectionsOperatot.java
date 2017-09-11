/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain x copy of the License at
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate x
 * single choice of license, x recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.test.refactoring.operators;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/*
 javax.swing.JPanel
 __org.netbeans.modules.java.hints.spiimpl.options.HintsPanel
 ____javax.swing.JSplitPane
 ______com.sun.java.swing.plaf.windows.WindowsSplitPaneDivider
 ______javax.swing.JPanel
 ________javax.swing.JScrollPane
 __________javax.swing.JViewport
 ____________org.netbeans.modules.java.hints.spiimpl.options.HintsPanel$EditableJTree
 ______________javax.swing.CellRendererPane
 __________javax.swing.JScrollPane$ScrollBar
 ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 __________javax.swing.JScrollPane$ScrollBar
 ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 ______javax.swing.JPanel
 ________javax.swing.JPanel
 __________javax.swing.JLabel
 __________javax.swing.JComboBox
 ____________com.sun.java.swing.plaf.windows.WindowsComboBoxUI$XPComboBoxButton
 ____________javax.swing.CellRendererPane
 __________javax.swing.JCheckBox
 __________javax.swing.JPanel
 ________javax.swing.JPanel
 __________javax.swing.JScrollPane
 ____________javax.swing.JViewport
 ______________javax.swing.JEditorPane
 ____________javax.swing.JScrollPane$ScrollBar
 ______________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 ______________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 ____________javax.swing.JScrollPane$ScrollBar
 ______________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 ______________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 __________javax.swing.JLabel
 __________javax.swing.JPanel
 ____________javax.swing.JButton
 ____________javax.swing.JButton
 ____________javax.swing.JButton
 __________javax.swing.JScrollPane
 ____________javax.swing.JViewport
 ______________javax.swing.JEditorPane
 ____________javax.swing.JScrollPane$ScrollBar
 ______________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 ______________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 ____________javax.swing.JScrollPane$ScrollBar
 ______________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 ______________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
 ____javax.swing.JPanel
 ______javax.swing.JButton
 ______javax.swing.JButton
 ______javax.swing.JButton
 ______javax.swing.JButton
 ______javax.swing.JButton
 ______javax.swing.JButton
 ____javax.swing.JPanel
 ______javax.swing.JLabel
 ______javax.swing.JComboBox
 ________com.sun.java.swing.plaf.windows.WindowsComboBoxUI$XPComboBoxButton
 ________javax.swing.CellRendererPane
 ____javax.swing.JPanel
 ______javax.swing.JLabel
 ______javax.swing.JLabel
 ______javax.swing.JTextField
 */
/**
 * <p>
 * @author (stanislav.sazonov@oracle.com)
 */
public class ManageInspectionsOperatot extends NbDialogOperator {

    public ManageInspectionsOperatot() {
        super("Manage Inspections");
    }

    public void setConfiguration(String s) {
        System.out.println(">>>>>>>>>>>>>>>>>>>> (1)");
        JComboBoxOperator comboBox = new JComboBoxOperator(this, 1);
        System.out.println(">>>>>>>>>>>>>>>>>>>> (2)");
        comboBox.pushKey(KeyEvent.VK_N);
        System.out.println(">>>>>>>>>>>>>>>>>>>> (3)");
    }

    public void createNewConfiguration(String s) {
        JComboBoxOperator comboBox = new JComboBoxOperator(this, 1);
        comboBox.pushKey(KeyEvent.VK_N);
        new EventTool().waitNoEvent(500);
        for (int i = 0; i < 10; i++) {
            comboBox.pushKey(KeyEvent.VK_BACK_SPACE);
            new EventTool().waitNoEvent(100);
        }
        new EventTool().waitNoEvent(100);
        comboBox.typeText(s);
        comboBox.pushKey(KeyEvent.VK_ENTER);
    }

    public void pressNew() {
        JButtonOperator button = new JButtonOperator(this, "New...");
        button.pushNoBlock();
    }

    public void pushText(String s) {
        JTextFieldOperator field = new JTextFieldOperator(this);
        field.typeText(s);
    }

    public void selectInspections(String item) {
        JTreeOperator tree = new JTreeOperator(this);
        TreeNode[] nodePath = getPath(tree.getRoot(), "", item, false);
        TreePath treePath = new TreePath(nodePath);
        try {
            tree.clickOnPath(treePath);
        } catch (Exception e) {
            throw e;
        }
    }

    public void checkInspections(String[] items) {
        for (String item : items) {
            JTreeOperator tree = new JTreeOperator(this);
            TreeNode[] treeNode = getPath(tree.getRoot(), "", item, false);
            TreePath treePath = new TreePath(treeNode);
            try {
                tree.clickOnPath(treePath);
                new EventTool().waitNoEvent(1000);
                tree.pushKey(KeyEvent.VK_SPACE);
                new EventTool().waitNoEvent(1000);
            } catch (Exception e) {
                throw e;
            }
        }
    }

    private TreeNode[] getPath(Object node, String deap, String item, boolean debug) {
        if (node instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) node;
            if (debug) {
                System.out.println(deap + dmtn.toString());
            }
            if (dmtn.toString().equals(item)) {
                if (debug) {
                    System.out.println("EQUAL!!! <" + item + ">");
                }
                return dmtn.getPath();
            }
            TreeNode[] curPath;
            for (int i = 0; i < dmtn.getChildCount(); i++) {
                curPath = getPath(dmtn.getChildAt(i), deap + "__", item, debug);
                if (curPath != null) {
                    return curPath;
                }
            }
        }
        return null;
    }

    public void presstEditScript() {
        JButtonOperator button = new JButtonOperator(this, "Edit Script");
        button.pushNoBlock();
    }

    public void pressOK() {
        JButtonOperator button = new JButtonOperator(this, "OK");
        button.pushNoBlock();
    }

    public void pressCancel() {
        JButtonOperator button = new JButtonOperator(this, "Cancel");
        button.pushNoBlock();
    }

    public void printAllComponents() {
        System.out.println("**************************");
        printComp(getContentPane(), "");
        System.out.println("**************************");
    }

    public void printComp(Container c, String s) {
        System.out.println(s + c.getClass().getName());
        if (c instanceof Container) {
            for (Component com : c.getComponents()) {
                printComp((Container) com, s + "__");
            }
        }
    }
}
