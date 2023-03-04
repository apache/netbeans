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
