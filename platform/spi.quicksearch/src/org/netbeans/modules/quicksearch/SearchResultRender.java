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
package org.netbeans.modules.quicksearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;
import org.openide.awt.HtmlRenderer;
import org.openide.util.Utilities;

/**
 * ListCellRenderer for SearchResults
 * @author Jan Becicka
 */
class SearchResultRender extends JLabel implements ListCellRenderer {

    private QuickSearchPopup popup;

    private JLabel categoryLabel;

    private JPanel rendererComponent;

    private JLabel resultLabel, shortcutLabel;

    private JPanel dividerLine;

    private JPanel itemPanel;

    private JPanel itemLinePanel;

    private JLabel cutLabel;

    public SearchResultRender (QuickSearchPopup popup) {
        super();
        this.popup = popup;
        configRenderer();
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (!(value instanceof ItemResult)) {
            return new JLabel();
        }

        ItemResult ir = (ItemResult) value;
        List<? extends KeyStroke> shortcut = ir.getShortcut();
        resultLabel.setText(ir.getDisplayName());
        if (shortcut != null && shortcut.size() > 0 && shortcut.get(0) != null) {
            // TBD - display multi shortcuts
            shortcutLabel.setText(getKeyStrokeAsText(shortcut.get(0)));
            itemPanel.add(shortcutLabel, BorderLayout.EAST);
        } else {
            itemPanel.remove(shortcutLabel);
        }

        CategoryResult cr = ir.getCategory();
        if (cr.isFirstItem(ir)) {
            categoryLabel.setText(cr.getCategory().getDisplayName());
            if (index > 0) {
                rendererComponent.add(dividerLine, BorderLayout.NORTH);
            }
        } else {
            categoryLabel.setText("");
            rendererComponent.remove(dividerLine);
        }
        categoryLabel.setPreferredSize(new Dimension(popup.getCategoryWidth(),
                categoryLabel.getPreferredSize().height));
        itemPanel.setPreferredSize(new Dimension(popup.getResultWidth(),
                itemPanel.getPreferredSize().height));

        if ( isCut(ir.getDisplayName(), resultLabel.getWidth()) ) {
            itemLinePanel.add(cutLabel, BorderLayout.EAST);
        } else {
            itemLinePanel.remove(cutLabel);
        }

        if (isSelected) {
            resultLabel.setBackground(list.getSelectionBackground());
            resultLabel.setForeground(list.getSelectionForeground());
            shortcutLabel.setBackground(list.getSelectionBackground());
            shortcutLabel.setForeground(list.getSelectionForeground());
            cutLabel.setBackground(list.getSelectionBackground());
            cutLabel.setForeground(list.getSelectionForeground());
        } else {
            resultLabel.setBackground(QuickSearchComboBar.getResultBackground());
            resultLabel.setForeground(list.getForeground());
            shortcutLabel.setBackground(QuickSearchComboBar.getResultBackground());
            shortcutLabel.setForeground(list.getForeground());
            cutLabel.setBackground(QuickSearchComboBar.getResultBackground());
            cutLabel.setForeground(list.getForeground());
        }
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            rendererComponent.setOpaque(false);

        return rendererComponent;
    }

    private boolean isCut (String text, int realWidth) {
        double width = HtmlRenderer.renderHTML(text, resultLabel.getGraphics(), 0, 10, Integer.MAX_VALUE, 20, resultLabel.getFont(), Color.BLACK, HtmlRenderer.STYLE_CLIP, false);
        return ((int)width) > (realWidth-4);
    }

    private void configRenderer () {
        categoryLabel = new JLabel();
        categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.BOLD));
        categoryLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        categoryLabel.setForeground(QuickSearchComboBar.getCategoryTextColor());

        resultLabel = HtmlRenderer.createLabel();
        ((HtmlRenderer.Renderer)resultLabel).setHtml(true);
        ((HtmlRenderer.Renderer)resultLabel).setRenderStyle(HtmlRenderer.STYLE_CLIP);
        resultLabel.setOpaque(true);
        resultLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        shortcutLabel = new JLabel();
        shortcutLabel.setOpaque(true);
        shortcutLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

        cutLabel = new JLabel("...");
        cutLabel.setOpaque(true);
        cutLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));

        itemLinePanel = new JPanel();
        itemLinePanel.setBackground(QuickSearchComboBar.getResultBackground());
        itemLinePanel.setLayout(new BorderLayout());
        itemLinePanel.add(resultLabel, BorderLayout.CENTER);


        itemPanel = new JPanel();
        itemPanel.setBackground(QuickSearchComboBar.getResultBackground());
        itemPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 3));
        itemPanel.setLayout(new BorderLayout());
        itemPanel.add(itemLinePanel, BorderLayout.CENTER);

        dividerLine = new JPanel();
        dividerLine.setBackground(QuickSearchComboBar.getPopupBorderColor());
        dividerLine.setPreferredSize(new Dimension(dividerLine.getPreferredSize().width, 1));

        rendererComponent = new JPanel();
        rendererComponent.setLayout(new BorderLayout());
        rendererComponent.add(itemPanel, BorderLayout.CENTER);
        rendererComponent.add(categoryLabel, BorderLayout.WEST);
    }

    static String getKeyStrokeAsText (KeyStroke keyStroke) {
        if (keyStroke == null)
            return "";
        int modifiers = keyStroke.getModifiers ();
        StringBuffer sb = new StringBuffer ();
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) > 0)
            sb.append ("Ctrl+");
        if ((modifiers & InputEvent.ALT_DOWN_MASK) > 0)
            sb.append ("Alt+");
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) > 0)
            sb.append ("Shift+");
        if ((modifiers & InputEvent.META_DOWN_MASK) > 0)
            if (Utilities.isMac()) {
                // Mac cloverleaf symbol
                sb.append ("\u2318+");
            } else if (isSolaris()) {
                // Sun meta symbol
                sb.append ("\u25C6+");
            } else {
                sb.append ("Meta+");
            }
        if (keyStroke.getKeyCode () != KeyEvent.VK_SHIFT &&
            keyStroke.getKeyCode () != KeyEvent.VK_CONTROL &&
            keyStroke.getKeyCode () != KeyEvent.VK_META &&
            keyStroke.getKeyCode () != KeyEvent.VK_ALT &&
            keyStroke.getKeyCode () != KeyEvent.VK_ALT_GRAPH
        )
            sb.append (Utilities.keyToString (
                KeyStroke.getKeyStroke (keyStroke.getKeyCode (), 0)
            ));
        return sb.toString ();
    }

    private static boolean isSolaris () {
        String osName = System.getProperty ("os.name");
        return osName != null && osName.startsWith ("SunOS");
    }
}
