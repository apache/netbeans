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

package org.netbeans.modules.debugger.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import static javax.swing.JComponent.WHEN_FOCUSED;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Keymap;
import org.netbeans.spi.debugger.ui.ViewFactory;
import org.openide.util.ImageUtilities;

public final class ToolTipView extends JComponent implements org.openide.util.HelpCtx.Provider {

    public static final String TOOLTIP_VIEW_NAME = "ToolTipView";               // NOI18N
    private static final String TOOLTIP_HELP_ID = "NetbeansDebuggerToolTipNode";// NOI18N

    private static volatile String expression;
    private static volatile Object variable;

    private transient JComponent contentComponent;

    private ToolTipView(String expression, Object v, String icon) {
        ToolTipView.expression = expression;
        variable = v;
        JComponent c = ViewFactory.getDefault().createViewComponent(
                icon,
                ToolTipView.TOOLTIP_VIEW_NAME,
                TOOLTIP_HELP_ID,
                null);
        setLayout (new BorderLayout ());
        add (c, BorderLayout.CENTER);  //NOI18N
    }

    public static String getExpression() {
        return expression;
    }

    public static Object getVariable() {
        return variable;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        variable = null;
    }

    // <RAVE>
    // Implement getHelpCtx() with the correct help ID
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(TOOLTIP_HELP_ID);
    }
    // </RAVE>

    @Override
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        if (contentComponent == null) {
            return false;
        }
        return contentComponent.requestFocusInWindow ();
    }

    /** Creates the view. */
    public static synchronized ToolTipView createToolTipView(String expression, Object variable) {
        return new ToolTipView(
                expression,
                variable,
                "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png"
        );
    }

    public static class ExpandableTooltip extends JPanel {

        private static final String UI_PREFIX = "ToolTip"; // NOI18N

        private JButton expButton;
        private JButton pinButton;
        private JComponent textToolTip;
        private boolean widthCheck = true;
        private boolean sizeSet = false;

        public ExpandableTooltip(String toolTipText, boolean expandable, boolean pinnable) {
            Font font = UIManager.getFont(UI_PREFIX + ".font");                 // NOI18N
            Color backColor = UIManager.getColor(UI_PREFIX + ".background");    // NOI18N
            Color foreColor = UIManager.getColor(UI_PREFIX + ".foreground");    // NOI18N

            if (backColor != null) {
                setBackground(backColor);
            }
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getForeground()),
                BorderFactory.createEmptyBorder(0, 3, 0, 3)
            ));

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            if (pinnable) {
                pinButton = new JButton(ImageUtilities.loadImageIcon("org/netbeans/editor/resources/pin.png", false));
                pinButton.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 0));
                pinButton.setBorderPainted(false);
                pinButton.setContentAreaFilled(false);
                add(pinButton);
            }
            if (expandable) {
                Icon expIcon = UIManager.getIcon ("Tree.collapsedIcon");        // NOI18N
                expButton = new JButton(expIcon);
                expButton.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 5));
                expButton.setBorderPainted(false);
                expButton.setContentAreaFilled(false);
                add(expButton);
            }
            //JLabel l = new JLabel(toolTipText);
            // Multi-line tooltip:
            JTextArea l = createMultiLineToolTip(toolTipText, true);
            if (font != null) {
                l.setFont(font);
            }
            if (foreColor != null) {
                l.setForeground(foreColor);
            }
            if (backColor != null) {
                l.setBackground(backColor);
            }
            l.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 3));
            textToolTip = l;
            add(l);
            if (expandable || pinnable) {
                InputMap im = new InputMap();
                im.setParent(getInputMap());
                setInputMap(WHEN_FOCUSED, im);
                ActionMap am = new ActionMap();
                am.setParent(getActionMap());
                setActionMap(am);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "expand"); // NOI18N
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "pin");    // NOI18N
                if (expandable) {
                    am.put("expand", new AbstractAction() {                     // NOI18N
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            expButton.doClick();
                        }
                    });
                }
                if (pinnable) {
                    am.put("pin", new AbstractAction() {                        // NOI18N
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            SwingUtilities.invokeLater(() -> pinButton.doClick());
                        }
                    });
                }
            }
        }

        public void addExpansionListener(ActionListener treeExpansionListener) {
            expButton.addActionListener(treeExpansionListener);
        }

        public void addPinListener(ActionListener treeExpansionListener) {
            pinButton.addActionListener(treeExpansionListener);
        }

        public void setWidthCheck(boolean widthCheck) {
            this.widthCheck = widthCheck;
        }

        @Override
        public Dimension getPreferredSize() {
            if (!sizeSet) {
                // Be big enough initially.
                return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
            Dimension preferredSize = super.getPreferredSize();
            // Let the width be as long as it can be
            return new Dimension(Integer.MAX_VALUE, preferredSize.height);
        }

        @Override
        public void setSize(int width, int height) {
            Dimension prefSize = getPreferredSize();
            Dimension button1Size = (expButton != null) ? expButton.getPreferredSize() : new Dimension(0, 0);
            Dimension button2Size = pinButton.getPreferredSize();
            if (widthCheck) {
                Insets insets = getInsets();
                int textWidth = width - insets.left - button1Size.width - button2Size.width - insets.right;
                height = Math.max(Math.max(height, button1Size.height), button2Size.height);
                textToolTip.setSize(textWidth, height);
                Dimension textPreferredSize = textToolTip.getPreferredSize();
                super.setSize(
                        insets.left + button1Size.width + button2Size.width + textPreferredSize.width + insets.right,
                        insets.top + Math.max(Math.max(button1Size.height, textPreferredSize.height), button2Size.height) + insets.bottom);
            } else {
                if (height >= prefSize.height) { // enough height
                    height = prefSize.height;
                }
                super.setSize(width, height);
            }
            sizeSet = true;
        }

        private static JTextArea createMultiLineToolTip(String toolTipText, boolean wrapLines) {
            JTextArea ta = new TextToolTip(wrapLines);
            ta.setText(toolTipText);
            return ta;
        }

        private static class TextToolTip extends JTextArea {

            private static final String ELIPSIS = "..."; //NOI18N

            private final boolean wrapLines;

            public TextToolTip(boolean wrapLines) {
                this.wrapLines = wrapLines;
                setLineWrap(false); // It's necessary to have a big width of preferred size first.
            }

            public @Override void setSize(int width, int height) {
                Dimension prefSize = getPreferredSize();
                if (width >= prefSize.width) {
                    width = prefSize.width;
                } else { // smaller available width
                    // Set line wrapping and do super.setSize() to determine
                    // the real height (it will change due to line wrapping)
                    if (wrapLines) {
                        setLineWrap(true);
                        setWrapStyleWord(true);
                    }
                    
                    super.setSize(width, Integer.MAX_VALUE); // the height is unimportant
                    prefSize = getPreferredSize(); // re-read new pref width
                }
                if (height >= prefSize.height) { // enough height
                    height = prefSize.height;
                } else { // smaller available height
                    // Check how much can be displayed - cannot rely on line count
                    // because line wrapping may display single physical line
                    // into several visual lines
                    // Before using viewToModel() a setSize() must be called
                    // because otherwise the viewToModel() would return -1.
                    super.setSize(width, Integer.MAX_VALUE);
                    int offset = viewToModel(new Point(0, height));
                    Document doc = getDocument();
                    try {
                        if (offset > ELIPSIS.length()) {
                            offset -= ELIPSIS.length();
                            doc.remove(offset, doc.getLength() - offset);
                            doc.insertString(offset, ELIPSIS, null);
                        }
                    } catch (BadLocationException ble) {
                        // "..." will likely not be displayed but otherwise should be ok
                    }
                    // Recalculate the prefSize as it may be smaller
                    // than the present preferred height
                    height = Math.min(height, getPreferredSize().height);
                }
                super.setSize(width, height);
            }

            @Override
            public void setKeymap(Keymap map) {
                //#181722: keymaps are shared among components with the same UI
                //a default action will be set to the Keymap of this component below,
                //so it is necessary to use a Keymap that is not shared with other JTextAreas
                super.setKeymap(addKeymap(null, map));
            }
        }
    }

}
