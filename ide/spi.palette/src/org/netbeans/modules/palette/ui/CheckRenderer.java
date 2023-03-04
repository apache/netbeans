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
package org.netbeans.modules.palette.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.netbeans.modules.palette.DefaultSettings;
import org.openide.explorer.view.NodeRenderer;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 * @author Pavel Flaska, S. Aubrecht
 */
class CheckRenderer extends JPanel implements TreeCellRenderer {

    protected JCheckBox check;
    private NodeRenderer nodeRenderer;
    private DefaultSettings settings;
    private static Dimension checkDim;

    static Rectangle checkBounds;
    
    static {
        Dimension old = new JCheckBox().getPreferredSize();
        checkDim = new Dimension(old.width, old.height - 5);
    }
    
    public CheckRenderer( DefaultSettings settings ) {
        this.nodeRenderer = new NodeRenderer();
        this.settings = settings;
        setLayout(null);
        add(check = new JCheckBox());
        check.setOpaque(false);
        Dimension dim = check.getPreferredSize();
        check.setPreferredSize(checkDim);
    }
    
    /** The component returned by HtmlRenderer.Renderer.getTreeCellRendererComponent() */
    private Component stringDisplayer = new JLabel(" "); //NOI18N
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        stringDisplayer = nodeRenderer.getTreeCellRendererComponent(tree, 
            value, isSelected, expanded, leaf, row, hasFocus);

        TreePath path = tree.getPathForRow( row );
        if( null != path && 1 == path.getPathCount() ) {
            //do not show checkbox for the root node
            return stringDisplayer;
        }
        
        if( stringDisplayer instanceof JComponent ) {
            setToolTipText( ((JComponent)stringDisplayer).getToolTipText() );
        }
        
        //HtmlRenderer does not tolerate null colors - real ones are needed to
        //ensure fg/bg always diverge enough to be readable
        if (stringDisplayer.getBackground() == null) {
            stringDisplayer.setBackground (tree.getBackground());
        }
        if (stringDisplayer.getForeground() == null) {
            stringDisplayer.setForeground (tree.getForeground());
        }

        if( check != null ) {
            Node node;
            if( value instanceof Node ) {
                node = (Node)value;
            } else {
                node = Visualizer.findNode( value );
            }
            check.setSelected( null == node || settings.isNodeVisible( node ) );//node.isSelected());
            check.setEnabled( true );//!node.isDisabled());
        }
        return this;
    }
    
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
    
    public Dimension getPreferredSize() {
        if (stringDisplayer != null) {
            stringDisplayer.setFont(getFont());
        }
        Dimension d_check = check == null ? new Dimension(0, checkDim.height) : 
            check.getPreferredSize();
            
        Dimension d_label = stringDisplayer != null ? 
            stringDisplayer.getPreferredSize() : new Dimension(0,0);
            
        return new Dimension(d_check.width  + d_label.width, (d_check.height < d_label.height ? d_label.height : d_check.height));
    }
    
    public void doLayout() {
        Dimension d_check = check == null ? new Dimension(0, 0) : check.getPreferredSize();
        Dimension d_label = stringDisplayer == null ? new Dimension (0,0) : stringDisplayer.getPreferredSize();
        int y_check = 0;
        int y_label = 0;
        
        if (d_check.height < d_label.height)
            y_check = (d_label.height - d_check.height) / 2;
        else
            y_label = (d_check.height - d_label.height) / 2;

        if (check != null) {
            check.setLocation(0, y_check);
            check.setBounds(0, y_check, d_check.width, d_check.height);
            if (checkBounds == null)
                checkBounds = check.getBounds();
        }
    }

    public static Rectangle getCheckBoxRectangle() {
        if( null == checkBounds )
            return new Rectangle(0,0,0,0);
        return (Rectangle) checkBounds.clone();
    }
}
