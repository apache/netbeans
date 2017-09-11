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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
