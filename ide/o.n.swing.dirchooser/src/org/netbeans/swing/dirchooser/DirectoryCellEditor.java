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
/*
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;

/**
 * A simple tree cell editor helper used to properly display a node while in editing mode.
 * @author Soot Phengsy
 */
class DirectoryCellEditor extends DefaultCellEditor {
    
    private final JPanel editorPanel = new JPanel(new BorderLayout());
    private static JTextField textField;
    private static JTree tree;
    private static JFileChooser fileChooser;
    
    public DirectoryCellEditor(JTree tree, JFileChooser fileChooser, final JTextField textField) {
        super(textField);
        this.tree = tree;
        this.textField = textField;
        this.fileChooser = fileChooser;
    }
    
    @Override
    public boolean isCellEditable(EventObject event) {
        return !(event instanceof MouseEvent);
    }
    
    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        Component c = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
        DirectoryNode node = (DirectoryNode)value;
        editorPanel.setOpaque(false);
        editorPanel.add(new JLabel(fileChooser.getIcon(node.getFile())), BorderLayout.CENTER);
        editorPanel.add(c, BorderLayout.EAST);
        textField = (JTextField)getComponent();
        String text = fileChooser.getName(node.getFile());
        textField.setText(text);
        textField.setColumns(text.length());
        //#232162
        if( "Nimbus".equals( UIManager.getLookAndFeel().getID() ) ) { //NOI18N
            textField.setBorder( BorderFactory.createEmptyBorder() );
        }
        return editorPanel;
    }
    
    public static JTextField getTextField() {
        return textField;
    }
}
