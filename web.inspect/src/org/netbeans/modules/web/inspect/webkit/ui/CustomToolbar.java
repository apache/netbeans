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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

public class CustomToolbar extends Box {

    private static final Dimension space = new Dimension(4, 0);
    private JToolBar toolbar;

    public CustomToolbar() {
        super(BoxLayout.X_AXIS);
        initPanel();
    }

    private void initPanel() {
        setBorder(new EmptyBorder(1, 2, 1, 2));

        // configure toolbar
        toolbar = new NoBorderToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorderPainted(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder());
        toolbar.setOpaque(false);
        toolbar.setFocusable(false);

        add(toolbar);
    }

    public void addButtons(Collection<AbstractButton> buttons) {
        for (AbstractButton button : buttons) {
            addButton(button);
        }
    }

    public void addButton(AbstractButton button) {
        Icon icon = button.getIcon();
        Dimension size = new Dimension(icon.getIconWidth() + 6, icon.getIconHeight() + 10);
        button.setMinimumSize(size);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setMargin(new Insets(5, 4, 5, 4));
        toolbar.add(button);
    }
    
    public void addSpaceSeparator() {
        toolbar.addSeparator(space);
    }
    
    public void addLineSeparator() {
        toolbar.addSeparator(space);
        toolbar.add(new JSeparator(JSeparator.VERTICAL));
        toolbar.addSeparator(space);
    }

    /**
     * ToolBar that doesn't paint any border.
     *
     * @author S. Aubrecht
     */
    public static class NoBorderToolBar extends JToolBar {

        /**
         * Creates a new instance of NoBorderToolbar
         */
        public NoBorderToolBar() {
        }

        /**
         * Creates a new instance of NoBorderToolbar
         *
         * @param layout
         */
        public NoBorderToolBar(int layout) {
            super(layout);
        }

        @Override
        protected void paintComponent(Graphics g) {
        }
    }
}
