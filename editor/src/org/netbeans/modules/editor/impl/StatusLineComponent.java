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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.editor.impl;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.ext.GotoDialogSupport;
import org.netbeans.editor.ext.KeyEventBlocker;
import org.openide.awt.MouseUtils;
import org.openide.util.NbBundle;


/**
 * Status line component implementation.
 *
 * @author Miloslav Metelka
 */
public final class StatusLineComponent extends JLabel {

    private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);

    private static final String MAX_LINE_COLUMN_STRING = "99999:999/9999:9999";

    private static final String MAX_LINE_COLUMN_OFFSET_STRING = "99999:999/9999:9999 <99999999>";

    private static final String INSERT_LOCALE = "status-bar-insert"; // NOI18N

    private static final String OVERWRITE_LOCALE = "status-bar-overwrite"; // NOI18N

    private static final Logger LOG = Logger.getLogger(StatusLineComponent.class.getName());

    /**
     * Besides line|column display also caret offset in status bar.
     */
    // -J-Dorg.netbeans.editor.caret.offset.level=FINE
    private static final Logger CARET_OFFSET_LOG = Logger.getLogger("org.netbeans.editor.caret.offset");
    
    private Dimension minDimension;

    StatusLineComponent(Type type) {
        switch (type) {
            case LINE_COLUMN:
                initMinDimension(CARET_OFFSET_LOG.isLoggable(Level.FINE) ?
                        MAX_LINE_COLUMN_OFFSET_STRING : MAX_LINE_COLUMN_STRING);
                break;

            case TYPING_MODE:
                ResourceBundle bundle = NbBundle.getBundle(BaseKit.class);
                String insText = bundle.getString(INSERT_LOCALE);
                String ovrText = bundle.getString(OVERWRITE_LOCALE);
                initMinDimension(insText, ovrText);
                break;
            default:
                throw new IllegalStateException();
        }
        setHorizontalAlignment(SwingConstants.CENTER);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (MouseUtils.isDoubleClick(e)) {
                    JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
                    if (lastFocusedComponent!=null)
                        new GotoDialogSupport().showGotoDialog(new KeyEventBlocker(lastFocusedComponent, false));
                }
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return minDimension;
    }

    private void initMinDimension(String... maxStrings) {
        FontMetrics fm = getFontMetrics(getFont());
        int minWidth = 0;
        for (String s : maxStrings) {
            minWidth = Math.max(minWidth, fm.stringWidth(s));
        }
        Border b = getBorder();
        Insets ins = (b != null) ? b.getBorderInsets(this) : NULL_INSETS;
        minWidth += ins.left + ins.right;
        int minHeight = fm.getHeight() + ins.top + ins.bottom;
        minDimension = new Dimension(minWidth, minHeight);
    }

    enum Type { // Type of component
        LINE_COLUMN,
        TYPING_MODE;
    }
}
