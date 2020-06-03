/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
