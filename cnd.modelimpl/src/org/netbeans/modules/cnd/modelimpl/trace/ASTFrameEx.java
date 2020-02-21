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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.netbeans.modules.cnd.antlr.debug.misc.*;
import org.netbeans.modules.cnd.antlr.collections.*;

/**
 */
public class ASTFrameEx extends JFrame {

    JTree tree;
    JTextArea text;

    class MyTreeSelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent event) {
            TreePath path = event.getPath();
//            System.out.println("Selected: " + path.getLastPathComponent());
//            Object elements[] = path.getPath();
//            for (int i = 0; i < elements.length; i++) {
//                System.out.print("->" + elements[i]);
//            }
            AST ast = (AST) path.getLastPathComponent();
            displayText("name:\t" + ast.getText()); // NOI18N
            appendText("\ntype:\t" + TraceUtils.getTokenTypeName(ast)); // NOI18N
            appendText("\npos:\t" + ast.getLine() + ':' + ast.getColumn()); // NOI18N
        }
    }

    
    private void displayText(String s) {
        text.setText(s);
    }

    private void appendText(String s) {
        text.setText(text.getText() + s);
    }
    
    public ASTFrameEx(String lab, AST r) {

        super(lab);

        JTreeASTModel model = new JTreeASTModel(r);
        tree = new JTree(model);
        tree.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N

        TreeSelectionListener listener = new MyTreeSelectionListener();
        tree.addTreeSelectionListener(listener);

        JScrollPane treeScroller = new JScrollPane(tree);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        
        text = new JTextArea() {
            @Override
            public Insets getInsets() {
                return new Insets(6, 6,  6,  6);
            }
        };
        text.setEditable(false);
        text.setTabSize(4);
        JScrollPane textScroller = new JScrollPane(text);
        
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitter.setLeftComponent(treeScroller);
        splitter.setRightComponent(textScroller);
        splitter.setDividerSize(2);
        splitter.setResizeWeight(0.6);
        
        content.add(splitter, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Frame f = (Frame)e.getSource();
                f.setVisible(false);
                f.dispose();
                // System.exit(0);
            }
        });
        setSize(320, 480);
    }
    
}
