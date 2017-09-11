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
package org.netbeans.modules.editor.search;

import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class SearchButton {

    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);

    public static JButton createButton(final String imageIcon, final String resName) {
        JButton button = new JButton(
                ImageUtilities.loadImageIcon(imageIcon, false));
        if (resName != null) {
            Mnemonics.setLocalizedText(button, NbBundle.getMessage(SearchButton.class, resName));
        }
        processButton(button);

        return button;
    }

    public static JToggleButton createToggleButton(final String imageIcon) {
        JToggleButton button = new JToggleButton(ImageUtilities.loadImageIcon(imageIcon, false)) {

            @Override
            public void setSelected(boolean b) {
                setContentAreaFilled(b);
                setBorderPainted(b);
                super.setSelected(b);
            }       
        };
        
        processButton(button);

        return button;
    }
    
    private static void processButton(AbstractButton button) {
        removeButtonContentAreaAndBorder(button);
        button.setMargin(BUTTON_INSETS);
        button.addMouseListener(sharedMouseListener);
        button.setFocusable(false);
    }

    private static void removeButtonContentAreaAndBorder(AbstractButton button) {
        boolean canRemove = true;
        if (button instanceof JToggleButton) {
            canRemove = !button.isSelected();
        }
        if (canRemove) {
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
        }
    }
    private static final MouseListener sharedMouseListener = new org.openide.awt.MouseUtils.PopupMouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent evt) {
            Object src = evt.getSource();

            if (src instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) evt.getSource();
                if (button.isEnabled()) {
                    button.setContentAreaFilled(true);
                    button.setBorderPainted(true);
                }
            }
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            Object src = evt.getSource();
            if (src instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) evt.getSource();
                removeButtonContentAreaAndBorder(button);
            }
        }

        @Override
        protected void showPopup(MouseEvent evt) {
        }
    };
}
