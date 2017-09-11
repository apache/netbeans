/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
