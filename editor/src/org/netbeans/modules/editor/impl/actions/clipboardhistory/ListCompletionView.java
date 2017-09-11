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

package org.netbeans.modules.editor.impl.actions.clipboardhistory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseListener;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import org.netbeans.lib.editor.util.StringEscapeUtils;
import org.openide.awt.HtmlRenderer;
import org.openide.util.ImageUtilities;

/**
 * borrowed org.netbeans.modules.editor.hints.borrowed.ListCompletionView
 */
public class ListCompletionView extends JList {

    public static final int COMPLETION_ITEM_HEIGHT = 16;
    private static final int DARKER_COLOR_COMPONENT = 5;
    private final static Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/editor/hints/resources/suggestion.gif", false); // NOI18N
    private final int fixedItemHeight;
    private Font font;
    private final RenderComponent renderComponent;
                
    public ListCompletionView(MouseListener mouseListener) {
        addMouseListener(mouseListener);
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        font = getFont();
        if (font.getSize() < 15 ) {
            font = font.deriveFont(font.getSize2D() + 1);
        }
        
        setFont( font );
        setFixedCellHeight(fixedItemHeight = Math.max(COMPLETION_ITEM_HEIGHT, getFontMetrics(getFont()).getHeight()));
        renderComponent = new RenderComponent();
        setCellRenderer(new ListCellRenderer() {
            private final ListCellRenderer defaultRenderer = new DefaultListCellRenderer();
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if( value instanceof ClipboardHistoryElement ) {
                    ClipboardHistoryElement val = (ClipboardHistoryElement)value;
                    renderComponent.setClipboardHistoryValue(val);
                    renderComponent.setSelected(isSelected);
//                    renderComponent.setSeparator(smartIndex > 0 && smartIndex == index);
                    Color bgColor;
                    Color fgColor;
                    if (isSelected) {
                        bgColor = list.getSelectionBackground();
                        fgColor = list.getSelectionForeground();
                    } else { // not selected
                        bgColor = list.getBackground();
                        if ((index % 2) == 0) { // every second item slightly different
                            bgColor = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
                        }
                        fgColor = list.getForeground();
                    }
                    // quick check Component.setBackground() always fires change
                    if (renderComponent.getBackground() != bgColor) {
                        renderComponent.setBackground(bgColor);
                    }
                    if (renderComponent.getForeground() != fgColor) {
                        renderComponent.setForeground(fgColor);
                    }
                    return renderComponent;

                } else {
                    return defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus);
                }
            }
        });
        setBorder( BorderFactory.createEmptyBorder() );
//        getAccessibleContext().setAccessibleName(LocaleSupport.getString("ACSN_CompletionView"));
//        getAccessibleContext().setAccessibleDescription(LocaleSupport.getString("ACSD_CompletionView"));
    }

    public void setResult(ClipboardHistory data) {
        if (data != null) {
            Model model = new Model(data);
            
            setModel(model);
            if (model.getSize() > 0) {
                setSelectedIndex(0);
            }
        }
    }

    /** Force the list to ignore the visible-row-count property */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public void up() {
        int size = getModel().getSize();
        if (size > 0) {
            int idx = (getSelectedIndex() - 1 + size) % size;
            setSelectedIndex(idx);
            ensureIndexIsVisible(idx);
            repaint();
        }
    }

    public void down() {
        int size = getModel().getSize();
        if (size > 0) {
            int idx = (getSelectedIndex() + 1) % size;
            if (idx == size)
                idx = 0;
            setSelectedIndex(idx);
            ensureIndexIsVisible(idx);
            validate();
        }
    }

    public void pageUp() {
        if (getModel().getSize() > 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.max(getSelectedIndex() - pageSize, 0);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void pageDown() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.min(getSelectedIndex() + pageSize, lastInd);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void begin() {
        if (getModel().getSize() > 0) {
            setSelectedIndex(0);
            ensureIndexIsVisible(0);
        }
    }

    public void end() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            setSelectedIndex(lastInd);
            ensureIndexIsVisible(lastInd);
        }
    }

    public @Override void paint(Graphics g) {
        Object value = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //NOI18N
        Map renderingHints = (value instanceof Map) ? (java.util.Map)value : null;
        if (renderingHints != null && g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            RenderingHints oldHints = g2d.getRenderingHints();
            g2d.setRenderingHints(renderingHints);
            try {
                super.paint(g2d);
            } finally {
                g2d.setRenderingHints(oldHints);
            }
        } else {
            super.paint(g);
        }
    }

    static class Model extends AbstractListModel {
        private ClipboardHistory data;
        
        static final long serialVersionUID = 3292276783870598274L;

        public Model(ClipboardHistory data) {
            this.data = data;
        }

        @Override
        public synchronized int getSize() {
            return data.getData().size();
        }

        @Override
        public synchronized Object getElementAt(int index) {
            return data.getData().get(index);
        }
    }

    private Graphics cellPreferredSizeGraphics;
    
    private final class RenderComponent extends JComponent {

        private ClipboardHistoryElement value;

        private boolean selected;

        void setClipboardHistoryValue(ClipboardHistoryElement value) {
            this.value = value;
        }

        void setSelected(boolean selected) {
            this.selected = selected;
        }

        public @Override void paintComponent(Graphics g) {
            // Although the JScrollPane without horizontal scrollbar
            // is explicitly set with a preferred size
            // it does not force its items with the only width into which
            // they can render (and still leaves them with the preferred width
            // of the widest item).
            // Therefore the item's render width is taken from the viewport's width.
            int itemRenderWidth = ((JViewport)ListCompletionView.this.getParent()).getWidth();
            Color bgColor = getBackground();
            Color fgColor = getForeground();
            int height = getHeight();

            // Clear the background
            g.setColor(bgColor);
            g.fillRect(0, 0, itemRenderWidth, height);
            g.setColor(fgColor);

            // Render the item
            renderHtml(value, g, ListCompletionView.this.getFont(), getForeground(),
                    itemRenderWidth, getHeight(), selected);

//            if (separator) {
//                g.setColor(Color.gray);
//                g.drawLine(0, 0, itemRenderWidth, 0);
//                g.setColor(fgColor);
//            }
        }

        public @Override Dimension getPreferredSize() {
            if (cellPreferredSizeGraphics == null) {
                // CompletionJList.this.getGraphics() is null
                cellPreferredSizeGraphics = java.awt.GraphicsEnvironment.
                        getLocalGraphicsEnvironment().getDefaultScreenDevice().
                        getDefaultConfiguration().createCompatibleImage(1, 1).getGraphics();
                assert (cellPreferredSizeGraphics != null);
            }
            return new Dimension(getPreferredWidth(value, cellPreferredSizeGraphics, ListCompletionView.this.getFont()),
                    fixedItemHeight);
        }

    }

    private static final int BEFORE_ICON_GAP = 1;

    private static final int AFTER_ICON_GAP = 4;

    private static final int AFTER_TEXT_GAP = 5;

    private static int getPreferredWidth(ClipboardHistoryElement f, Graphics g, Font defaultFont) {
        int width = BEFORE_ICON_GAP + icon.getIconWidth() + AFTER_ICON_GAP + AFTER_TEXT_GAP;
        width += (int)HtmlRenderer.renderHTML(StringEscapeUtils.escapeHtml(f.getShortenText()), g, 0, 0, Integer.MAX_VALUE, 0,
                defaultFont, Color.black, HtmlRenderer.STYLE_CLIP, false);
        return width;
    }

    private static void renderHtml(ClipboardHistoryElement f, Graphics g, Font defaultFont, Color defaultColor,
    int width, int height, boolean selected) {
        int textEnd = width - AFTER_ICON_GAP - AFTER_TEXT_GAP;
        FontMetrics fm = g.getFontMetrics(defaultFont);
        int textY = (height - fm.getHeight())/2 + fm.getHeight() - fm.getDescent();

        HtmlRenderer.renderHTML(f.getNumber() + " " + StringEscapeUtils.escapeHtml(f.getShortenText()), g, 1, textY, textEnd, textY, //NOI18N
                defaultFont, defaultColor, HtmlRenderer.STYLE_TRUNCATE, true);//, selected);
    }
}
