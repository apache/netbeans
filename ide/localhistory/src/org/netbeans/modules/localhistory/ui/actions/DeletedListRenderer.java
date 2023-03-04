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
package org.netbeans.modules.localhistory.ui.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import org.netbeans.modules.localhistory.ui.actions.FileNode.StoreEntryNode;
import org.openide.awt.HtmlRenderer;

/**
 *
 * @author Pavel Flaska
 * @author Tomas Stupka
 */
class DeletedListRenderer extends JPanel implements TreeCellRenderer {

    protected JCheckBox check;
    protected HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();
    private static Dimension checkDim;

    static Rectangle checkBounds;

    static {
        Dimension old = new JCheckBox().getPreferredSize();
        checkDim = new Dimension(old.width, old.height - 5);
    }

    public DeletedListRenderer() {
        setLayout(null);
        add(check = new JCheckBox());
        check.setPreferredSize(checkDim);
    }

    /** The component returned by HtmlRenderer.Renderer.getTreeCellRendererComponent() */
    private Component stringDisplayer = new JLabel(" "); //NOI18N

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if(!(value instanceof FileNode)) {
            return this;
        }
        FileNode node = (FileNode) value;
        stringDisplayer = renderer.getTreeCellRendererComponent(tree, 
            node.getText(), isSelected, expanded, leaf, row, hasFocus);

        if(!(value instanceof StoreEntryNode)) {
//            renderer.setIcon (null);
        }
        stringDisplayer.setEnabled(value instanceof StoreEntryNode);

        //HtmlRenderer does not tolerate null colors - real ones are needed to
        //ensure fg/bg always diverge enough to be readable
        if (stringDisplayer.getBackground() == null) {
            stringDisplayer.setBackground (tree.getBackground());
        }
        if (stringDisplayer.getForeground() == null) {
            stringDisplayer.setForeground (tree.getForeground());
        }

        if (check != null) {
            check.setSelected(node.isSelected());
        }
        return this;
    }

    @Override
    public void paintComponent (Graphics g) {
        Dimension d_check = check == null ? new Dimension(0, 0) : check.getSize();
        Dimension d_label = stringDisplayer == null ? new Dimension(0,0) : 
            stringDisplayer.getPreferredSize();

        int y_check = 0;
        int y_label = 0;

        if (d_check.height >= d_label.height) {
            y_label = (d_check.height - d_label.height) / 2;
        }
        if (check != null) {
            check.setBounds (0, 0, d_check.width, d_check.height);
            check.paint(g);
        }
        if (stringDisplayer != null) {
            int y = y_label-2;
            stringDisplayer.setBounds (d_check.width, y, 
                d_label.width, getHeight()-1);
            g.translate (d_check.width, y_label);
            stringDisplayer.paint(g);
            g.translate (-d_check.width, -y_label);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (stringDisplayer != null) {
            stringDisplayer.setFont(getFont());
        }
        Dimension d_check = check == null ? null: check.getPreferredSize();
        d_check = d_check == null ? new Dimension(0, checkDim.height) : d_check;

        Dimension d_label = stringDisplayer == null
                ? null : stringDisplayer.getPreferredSize();
        d_label = d_label == null ? new Dimension(0, 0) : d_label;

        return new Dimension(d_check.width  + d_label.width, (d_check.height < d_label.height ? d_label.height : d_check.height));
    }

    @Override
    public void doLayout() {
        Dimension d_check = check == null ? new Dimension(0, 0) : check.getPreferredSize();
        Dimension d_label = stringDisplayer == null ? new Dimension (0,0) : stringDisplayer.getPreferredSize();
        int y_check = 0;

        if (d_check.height < d_label.height) {
            y_check = (d_label.height - d_check.height) / 2;
        }
        
        if (check != null) {
            check.setLocation(0, y_check);
            check.setBounds(0, y_check, d_check.width, d_check.height);
            if (checkBounds == null)
                checkBounds = check.getBounds();
        }
    }

    static Rectangle getCheckBoxRectangle() {
        return (Rectangle) checkBounds.clone();
    }
}   
