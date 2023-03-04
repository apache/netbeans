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

package org.netbeans.modules.web.beans.navigation;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * Copy of JavaTreeCellRenderer at java.navigation.
 *
 * @author ads
 */
public final class JavaTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 8126878473944648830L;

    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value,
                sel, expanded, leaf, row, hasFocus);

        if (!sel) {
            setBackgroundNonSelectionColor(tree.getBackground());
        }

        if (value instanceof JavaElement) {
            JavaElement javaElement  = (JavaElement) value;
            label.setIcon(javaElement.getIcon());
            label.setToolTipText(javaElement.getTooltip());
            label.setEnabled( !javaElement.isDisabled() );
        }

        return label;
    }
}
