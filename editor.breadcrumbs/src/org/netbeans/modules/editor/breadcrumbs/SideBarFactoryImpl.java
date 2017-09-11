/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011-2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.breadcrumbs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.SideBarFactory;
import org.openide.awt.CloseButtonFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.util.WeakListeners;

/**
 *
 * @author lahvac
 */
public class SideBarFactoryImpl implements SideBarFactory {

    public static final String KEY_BREADCRUMBS = "enable.breadcrumbs";
    public static final boolean DEF_BREADCRUMBS = true;
    
    @Override
    public JComponent createSideBar(JTextComponent target) {
        final Document doc = target.getDocument();
        
        return new SideBar(doc);
    }
    
    private static final class SideBar extends JPanel implements ExplorerManager.Provider, PreferenceChangeListener {
        private final Document forDocument;
        private final Preferences prefs;
        private boolean enabled;

        public SideBar(Document forDocument) {
            super(new BorderLayout());
            this.forDocument = forDocument;
            add(new BreadCrumbComponent(), BorderLayout.CENTER);

            JButton closeButton = CloseButtonFactory.createBigCloseButton();

            add(closeButton, BorderLayout.EAST);
            
            prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
            
            closeButton.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    prefs.putBoolean(KEY_BREADCRUMBS, false);
                }
            });
            
            setBorder(new SeparatorBorder());
            preferenceChange(null);
        }
        
        @Override public ExplorerManager getExplorerManager() {
            return HolderImpl.get(forDocument).getManager();
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt == null || KEY_BREADCRUMBS.equals(evt.getKey())) {
                enabled = prefs.getBoolean(KEY_BREADCRUMBS, DEF_BREADCRUMBS);
                updatePreferredSize();
            }
        }
        
        private void updatePreferredSize() {
            if (enabled) {
                setPreferredSize(new Dimension(Integer.MAX_VALUE, BreadCrumbComponent.COMPONENT_HEIGHT + SeparatorBorder.BORDER_WIDTH));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            }else{
                setPreferredSize(new Dimension(0,0));
                setMaximumSize(new Dimension(0,0));
            }
            revalidate();
        }
    }
    
    private static final class SeparatorBorder implements Border {
        private static final int BORDER_WIDTH = 1;
        private final Insets INSETS = new Insets(BORDER_WIDTH, 0, 0, 0);

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color originalColor = g.getColor();
            g.setColor (UIManager.getColor ("controlShadow")); //NOI18N
            g.drawLine(0, 0, c.getWidth(), 0);
            g.setColor(originalColor);
        }

        @Override public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        @Override public boolean isBorderOpaque() {
            return true;
        }
    }

}
