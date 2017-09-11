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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java.ui.elements;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class JCheckBoxIcon implements Icon {
    private final JPanel delegate;
    private final Dimension dimension;
    private final boolean selected;

    public JCheckBoxIcon(boolean selected, Dimension dimension) {
        this.selected = selected;
        this.dimension = dimension;
        BorderLayout layout = new BorderLayout();
        this.delegate = new JPanel(layout, false);
        this.delegate.setBorder(null);
        this.delegate.setOpaque(false);
        JCheckBox jCheckBox = new JCheckBox(null, null, selected);
        jCheckBox.setMargin(new Insets(0, 0, 0, 0));
        this.delegate.add(jCheckBox, BorderLayout.CENTER);
        this.delegate.setSize(jCheckBox.getPreferredSize());
        this.delegate.addNotify();
        this.delegate.validate();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (delegate.getWidth() != 0 && delegate.getHeight() != 0) {
            BufferedImage img = new BufferedImage(delegate.getWidth(), delegate.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = img.createGraphics();
            delegate.paintAll(graphics);
            g.drawImage(img, x, y, dimension.width, dimension.height, null);
        }
    }

    @Override
    public int getIconWidth() {
        return dimension.width;
    }

    @Override
    public int getIconHeight() {
        return dimension.height;
    }
    
}
