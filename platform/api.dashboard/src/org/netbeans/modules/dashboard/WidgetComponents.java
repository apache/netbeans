/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.dashboard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.spi.dashboard.WidgetElement;
import org.netbeans.spi.dashboard.WidgetElement.ActionElement;
import org.netbeans.spi.dashboard.WidgetElement.ComponentElement;
import org.netbeans.spi.dashboard.WidgetElement.ImageElement;
import org.netbeans.spi.dashboard.WidgetElement.LinkElement;
import org.netbeans.spi.dashboard.WidgetElement.SeparatorElement;
import org.netbeans.spi.dashboard.WidgetElement.TextElement;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Default creation of JComponents for WidgetElements.
 */
@NbBundle.Messages({
    "# {0} - link URI",
    "TXT_statusLink=Open {0}"
})
final class WidgetComponents {

    private WidgetComponents() {

    }

    static JComponent titleComponentFor(String title) {
        MultiLineText text = new MultiLineText(title);
        Font font = text.getFont();
        text.setFont(font.deriveFont(font.getSize() * 1.4f));
        return text;
    }

    static JComponent componentFor(WidgetElement element) {
        if (element instanceof TextElement textElement) {
            return componentForText(textElement);
        } else if (element instanceof ImageElement imageElement) {
            return componentForImage(imageElement);
        } else if (element instanceof ActionElement actionElement) {
            return componentForAction(actionElement);
        } else if (element instanceof LinkElement linkElement) {
            return componentForLink(linkElement);
        } else if (element instanceof SeparatorElement sepElement) {
            return componentForSeparator(sepElement);
        } else if (element instanceof ComponentElement cmpElement) {
            return cmpElement.component();
        }
        return null;
    }

    private static JComponent componentForText(TextElement element) {
        MultiLineText text = new MultiLineText(element.text());
        TextElement.Kind kind = element.kind();
        if (kind == TextElement.Kind.SUBHEADING) {
            Font font = text.getFont();
            text.setFont(font.deriveFont(font.getSize() * 1.1f));
        } else if (kind == TextElement.Kind.ASIDE || kind == TextElement.Kind.UNAVAILABLE) {
            Font font = text.getFont();
            text.setFont(font.deriveFont(font.getSize() * 0.9f));
            Color color = UIManager.getColor("controlDkShadow");
            if (color != null) {
                text.setForeground(color);
            }
        }
        return text;
    }

    private static JComponent componentForImage(ImageElement element) {
        Image image = ImageUtilities.loadImage(element.resourcePath(), true);
        Icon icon = ImageUtilities.image2Icon(image);
        JLabel label = new JLabel(icon);
        label.setMinimumSize(new Dimension(0, 0));
        return label;
    }

    private static JComponent componentForAction(ActionElement element) {
        if (element.asLink()) {
            return new LinkButton(element.action(), element.showIcon());
        } else {
            return new WidgetButton(element.action(), element.showIcon());
        }
    }

    private static JComponent componentForLink(LinkElement element) {
        Action action = new URIOpenAction(element.text(), element.link());
        if (element.asButton()) {
            return new WidgetButton(action, false);
        } else {
            return new LinkButton(action, false);
        }
    }

    private static JComponent componentForSeparator(WidgetElement.SeparatorElement element) {
        JSeparator sep = new JSeparator();
        sep.setPreferredSize(new Dimension(75, 8));
        return sep;
    }

    private static class MultiLineText extends JTextArea {

        private MultiLineText(String text) {
            super(text);
            setEditable(false);
            setCursor(null);
            setOpaque(false);
            // some LAFs (looking at you Nimbus!) require transparent bg color
            setBackground(new Color(0, 0, 0, 0));
            setFocusable(false);
            setBorder(BorderFactory.createEmptyBorder());
            setMargin(new Insets(0, 0, 0, 0));
            Font labelFont = UIManager.getFont("Label.font");
            if (labelFont != null) {
                setFont(labelFont);
            }
            Color labelColor = UIManager.getColor("Label.foreground");
            if (labelColor != null) {
                setForeground(labelColor);
            }
            setWrapStyleWord(true);
            setLineWrap(true);
        }

    }

    private static abstract class AbstractWidgetButton extends JButton {

        private final boolean allowIcon;

        private AbstractWidgetButton(Action action, boolean allowIcon) {
            super(action);
            this.allowIcon = allowIcon;
            if (!allowIcon) {
                setIcon(null);
            }
            setHorizontalAlignment(LEFT);
            setToolTipText(null);
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e) {
                    onMouseEnter();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    onMouseExit();
                }

            });
        }

        void onMouseEnter() {
            Action action = getAction();
            if (action != null) {
                Object status = action.getValue(Action.SHORT_DESCRIPTION);
                if (status instanceof String statusText) {
                    StatusDisplayer.getDefault().setStatusText(statusText);
                }
            }
        }

        void onMouseExit() {
            StatusDisplayer.getDefault().setStatusText("");
        }

        @Override
        protected void actionPropertyChanged(Action action, String propertyName) {
            if (allowedPropertyChange(propertyName)) {
                super.actionPropertyChanged(action, propertyName);
            }
        }

        private boolean allowedPropertyChange(String propertyName) {
            return switch (propertyName) {
                case Action.SMALL_ICON ->
                    allowIcon;
                case Action.LARGE_ICON_KEY ->
                    allowIcon;
                case Action.SHORT_DESCRIPTION ->
                    false;
                case Action.LONG_DESCRIPTION ->
                    false;
                default ->
                    true;
            };
        }

    }

    private static class WidgetButton extends AbstractWidgetButton {

        private final Color linkColor;
        private final Color hoverLinkColor;
        private final Color backgroundColor;
        private final Border border;
        private final Border hoverBorder;

        private boolean rollover;

        private WidgetButton(Action action, boolean allowIcon) {
            super(action, allowIcon);
            Color link = UIManager.getColor("nb.html.link.foreground");
            if (link != null) {
                linkColor = link;
                Color hover = UIManager.getColor("nb.html.link.foreground.hover");
                hoverLinkColor = hover == null ? linkColor : hover;
            } else {
                linkColor = new Color(0x164B7B);
                hoverLinkColor = linkColor;
            }
            Color bg = UIManager.getColor("Panel.background");
            if (bg == null) {
                bg = getBackground();
            }
            backgroundColor = bg;
            Border padding = BorderFactory.createEmptyBorder(4, 6, 4, 6);
            border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(linkColor),
                    padding);
            hoverBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(hoverLinkColor),
                    padding);
            setForeground(linkColor);
            setContentAreaFilled(false);
            setBorder(border);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        void onMouseEnter() {
            super.onMouseEnter();
            setForeground(new Color(backgroundColor.getRGB()));
            rollover = true;
            setBorder(hoverBorder);
            repaint();
        }

        @Override
        void onMouseExit() {
            super.onMouseExit();
            setForeground(linkColor);
            rollover = false;
            setBorder(border);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (rollover) {
                Color c = g.getColor();
                g.setColor(linkColor);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(c);
            }
            super.paintComponent(g);
        }

    }

    private static class LinkButton extends AbstractWidgetButton {

        private final Color linkColor;
        private final Color hoverLinkColor;

        private boolean rollover;

        private LinkButton(Action action, boolean allowIcon) {
            super(action, allowIcon);
            Color link = UIManager.getColor("nb.html.link.foreground");
            if (link != null) {
                linkColor = link;
                Color hover = UIManager.getColor("nb.html.link.foreground.hover");
                hoverLinkColor = hover == null ? linkColor : hover;
            } else {
                linkColor = new Color(0x164B7B);
                hoverLinkColor = linkColor;
            }
            setContentAreaFilled(false);
            setBorder(new LinkBorder());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setForeground(linkColor);
        }

        @Override
        void onMouseEnter() {
            super.onMouseEnter();
            setForeground(hoverLinkColor);
            rollover = true;
            repaint();
        }

        @Override
        void onMouseExit() {
            super.onMouseExit();
            setForeground(linkColor);
            rollover = false;
            repaint();
        }

        private class LinkBorder implements Border {

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                if (rollover) {
                    String text = getText();
                    if (text.isEmpty()) {
                        return;
                    }
                    g.setColor(getForeground());
                    Font font = getFont();
                    FontMetrics metrics = getFontMetrics(font);
                    Icon icon = getIcon();
                    int iconWidth = icon == null ? 0 : icon.getIconWidth() + getIconTextGap();
                    int x1 = iconWidth;
                    int x2 = metrics.stringWidth(text) + iconWidth;
                    int y1 = metrics.getHeight();
                    g.drawLine(x1, y1, x2, y1);

                }
            }

        }

    }

    private static class URIOpenAction extends AbstractAction {

        private final String text;
        private final URI uri;

        private URIOpenAction(String text, URI uri) {
            super(text);
            this.text = text;
            this.uri = uri;
            putValue(Action.SHORT_DESCRIPTION, Bundle.TXT_statusLink(uri));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURLExternal(uri.toURL());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

}
