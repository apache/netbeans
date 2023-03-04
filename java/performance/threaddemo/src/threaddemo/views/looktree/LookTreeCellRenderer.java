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

package threaddemo.views.looktree;

import java.awt.Component;
import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.netbeans.spi.looks.Look;

/**
 * @author Jesse Glick
 */
@SuppressWarnings("unchecked")
class LookTreeCellRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        LookTreeNode n = (LookTreeNode)value;
        Look l = n.getLook();
        if (leaf) {
            Image i = l.getIcon(n.getData(), BeanInfo.ICON_COLOR_16x16, n.getLookup() );
            if (i != null) {
                setLeafIcon(new ImageIcon(i));
            } else {
                setLeafIcon(getDefaultLeafIcon());
            }
        } else if (expanded) {
            Image i = l.getOpenedIcon(n.getData(), BeanInfo.ICON_COLOR_16x16, n.getLookup() );
            if (i != null) {
                setOpenIcon(new ImageIcon(i));
            } else {
                setOpenIcon(getDefaultOpenIcon());
            }
        } else {
            Image i = l.getIcon(n.getData(), BeanInfo.ICON_COLOR_16x16, n.getLookup() );
            if (i != null) {
                setClosedIcon(new ImageIcon(i));
            } else {
                setClosedIcon(getDefaultClosedIcon());
            }
        }
        String displayName = l.getDisplayName(n.getData(), n.getLookup() );
        setToolTipText(l.getShortDescription(n.getData(), n.getLookup() ));
        return super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
    }
    
}
