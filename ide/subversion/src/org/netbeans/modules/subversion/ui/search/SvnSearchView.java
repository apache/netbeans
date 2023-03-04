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
package org.netbeans.modules.subversion.ui.search;

import javax.swing.event.ListSelectionListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * Shows Search results in a JList.
 *
 * @author Tomas Stupka
 */
class SvnSearchView implements ComponentListener {

    private final JList resultsList;
    private ISVNLogMessage[] lm;
    private final AttributeSet searchHiliteAttrs;
    private final JScrollPane pane;


    public SvnSearchView() {
        FontColorSettings fcs = (FontColorSettings) MimeLookup.getMimeLookup("text/x-java").lookup(FontColorSettings.class); // NOI18N
        searchHiliteAttrs = fcs.getFontColors("highlight-search"); // NOI18N

        resultsList = new JList(new SvnSearchListModel());
        resultsList.setFixedCellHeight(-1);
        resultsList.setCellRenderer(new SvnSearchListCellRenderer());
        resultsList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SvnSearchView.class, "ACSN_SummaryView_ListName"));
        resultsList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SvnSearchView.class, "ACSD_SummaryView_ListDesc"));
        resultsList.addComponentListener(this);
        pane = new JScrollPane(resultsList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    JComponent getComponent() {
        return pane;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        int [] selection = resultsList.getSelectedIndices();
        resultsList.setModel(new SvnSearchListModel());
        resultsList.setSelectedIndices(selection);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // not interested
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // not interested
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // not interested
    }

    public void setResults(ISVNLogMessage[] lm) {
        this.lm = lm;
        resultsList.setModel(new SvnSearchListModel());
    }

    SVNRevision getSelectedValue() {
        Object selection = resultsList.getSelectedValue();
        if(selection == null) {
            return null;
        }
        if(!(selection instanceof ISVNLogMessage)) {
            return null;
        }
        ISVNLogMessage message = (ISVNLogMessage) selection;
        return message.getRevision();
    }

    void addListSelectionListener(ListSelectionListener listener) {
        resultsList.addListSelectionListener(listener);
    }

    void removeListSelectionListener(ListSelectionListener listener) {
        resultsList.removeListSelectionListener(listener);
    }

    private class SvnSearchListModel extends AbstractListModel {

        @Override
        public int getSize() {
            if(lm == null) {
                return 0;
            }
            return lm.length;
        }

        @Override
        public Object getElementAt(int index) {
            return lm[index];
        }
    }

    private class SvnSearchListCellRenderer extends JPanel implements ListCellRenderer {

        private static final String FIELDS_SEPARATOR = "        "; // NOI18N
        private static final double DARKEN_FACTOR = 0.95;

        private final Style selectedStyle;
        private final Style normalStyle;
        private final Style boldStyle;
        private final Style hiliteStyle;

        private final JTextPane textPane = new JTextPane();

        private final DateFormat defaultFormat;

        private final Color selectionBackground;
        private final Color selectionForeground;

        public SvnSearchListCellRenderer() {
            JList list = new JList();
            selectionBackground = list.getSelectionBackground();
            selectionForeground = list.getSelectionForeground();

            selectedStyle = textPane.addStyle("selected", null); // NOI18N
            StyleConstants.setForeground(selectedStyle, selectionForeground); // NOI18N
            normalStyle = textPane.addStyle("normal", null); // NOI18N
            StyleConstants.setBackground(selectedStyle, selectionBackground); // NOI18N
            boldStyle = textPane.addStyle("filename", normalStyle); // NOI18N
            StyleConstants.setBold(boldStyle, true);
            defaultFormat = DateFormat.getDateTimeInstance();

            hiliteStyle = textPane.addStyle("hilite", normalStyle); // NOI18N
            Color c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Background);
            if(c != null) StyleConstants.setBackground(hiliteStyle, c);
            c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Foreground);
            if(c != null) StyleConstants.setForeground(hiliteStyle, c);

            setLayout(new BorderLayout());
            add(textPane);
            textPane.setBorder(null);
            //fix for nimbus laf
            textPane.setOpaque(false);
            textPane.setBackground(new Color(0, 0, 0, 0));
        }

        public Color darker(Color c) {
            return new Color(Math.max((int)(c.getRed() * DARKEN_FACTOR), 0),
                 Math.max((int)(c.getGreen() * DARKEN_FACTOR), 0),
                 Math.max((int)(c.getBlue() * DARKEN_FACTOR), 0));
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value instanceof ISVNLogMessage) {
                ISVNLogMessage message = (ISVNLogMessage) value;
                StyledDocument sd = textPane.getStyledDocument();

                Style style;
                if (isSelected) {
                    setBackground(selectionBackground);
                    style = selectedStyle;
                } else {
                    Color c = UIManager.getColor("List.background"); // NOI18N
                    setBackground((index & 1) == 0 ? c : darker(c));
                    style = normalStyle;
                }

                try {
                    sd.remove(0, sd.getLength());
                    sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, true);
                    sd.insertString(0, message.getRevision().toString(), null);
                    sd.setCharacterAttributes(0, sd.getLength(), boldStyle, false);
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + message.getAuthor(), null);
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR +  defaultFormat.format(message.getDate()), null);
                    sd.insertString(sd.getLength(), "\n" + message.getMessage(), null); // NOI18N
                    sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, false);
                } catch (BadLocationException e) {
                    Subversion.LOG.log(Level.SEVERE, null, e);
                }

                if (message.getMessage() != null) {
                    int width = resultsList.getWidth();
                    if (width > 0) {
                        FontMetrics fm = list.getFontMetrics(list.getFont());
                        Rectangle2D rect = fm.getStringBounds(message.getMessage(), textPane.getGraphics());
                        int nlc, i;
                        for (nlc = -1, i = 0; i != -1 ; i = message.getMessage().indexOf('\n', i + 1), nlc++);
                        //if (indentation == 0) nlc++;
                        int lines = (int) (rect.getWidth() / (width - 80) + 1);
                        int ph = fm.getHeight() * (lines + nlc + 1) + 0;
                        textPane.setPreferredSize(new Dimension(width - 50, ph));
                    }
                }

            }
            return this;
        }
    }
}
