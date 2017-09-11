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

package org.netbeans.modules.project.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.openide.awt.CloseButtonFactory;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;

/**
 *
 * @author mkozeny
 */
public class NodeSelectionProjectPanel extends javax.swing.JPanel implements PreferenceChangeListener {

    public static final Preferences prefs = NbPreferences.forModule(NodeSelectionProjectPanel.class);

    public static final String KEY_ACTUALSELECTIONPROJECT = "enable.actualselectionproject";
    private boolean enabled;
    private boolean isMinimized;

    public static final int COMPONENT_HEIGHT = 22;
    private static final int BORDER_WIDTH = 1;
    
    /**
     * Creates new form ActualSelectionProjectPanel
     */
    public NodeSelectionProjectPanel() {
        super(new BorderLayout());
        JButton closeButton = CloseButtonFactory.createBigCloseButton();
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.putBoolean(KEY_ACTUALSELECTIONPROJECT, false);
            }
        });
        add(closeButton, BorderLayout.EAST);

        setBorder(new SeparatorBorder());
        preferenceChange(null);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt == null || KEY_ACTUALSELECTIONPROJECT.equals(evt.getKey())) {
            enabled = prefs.getBoolean(KEY_ACTUALSELECTIONPROJECT, false);
            updatePreferredSize();
        }
    }

    private void updatePreferredSize() {
        if (enabled) {
            setPreferredSize(new Dimension(Integer.MAX_VALUE, COMPONENT_HEIGHT + BORDER_WIDTH));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            isMinimized = false;
        } else {
            setPreferredSize(new Dimension(0, 0));
            setMaximumSize(new Dimension(0, 0));
            isMinimized = true;
        }
        revalidate();
    }
    
    void minimize() {
        setPreferredSize(new Dimension(0, 0));
        setMaximumSize(new Dimension(0, 0));
        isMinimized = true;
        revalidate();
    }
    
    void maximize() {
        setPreferredSize(new Dimension(Integer.MAX_VALUE, COMPONENT_HEIGHT + BORDER_WIDTH));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        isMinimized = false;
        revalidate();
    }
    
    public boolean isMinimized() {
        return isMinimized;
    }

    private static final class SeparatorBorder implements Border {

        private static final int BORDER_WIDTH = 1;
        private final Insets INSETS = new Insets(BORDER_WIDTH, 0, 0, 0);

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color originalColor = g.getColor();
            g.setColor(UIManager.getColor("controlShadow")); //NOI18N
            g.drawLine(0, 0, c.getWidth(), 0);
            g.setColor(originalColor);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }
}
