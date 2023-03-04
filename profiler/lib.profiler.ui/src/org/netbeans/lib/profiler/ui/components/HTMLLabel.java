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

package org.netbeans.lib.profiler.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.lib.profiler.ui.UIUtils;


/**
 * @author Ian Formanek
 */
public class HTMLLabel extends JEditorPane implements HyperlinkListener {
    
    private int halign = SwingConstants.LEADING;
    

    public HTMLLabel() {
        this(null);
    }

    public HTMLLabel(String text) {
        setEditorKit(new HTMLEditorKit());
        setEditable(false);
        setOpaque(false);
        setNavigationFilter(new NavigationFilter() {
                public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
                    super.moveDot(fb, 0, bias);
                }

                public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
                    super.setDot(fb, 0, bias);
                }

                public int getNextVisualPositionFrom(JTextComponent text, int pos, Position.Bias bias, int direction,
                                                     Position.Bias[] biasRet)
                                              throws BadLocationException {
                    return 0;
                }
            });
        setFont(UIManager.getFont("Label.font")); //NOI18N
        addHyperlinkListener(this);
        
        if (text != null) setText(text);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    public void setOpaque(boolean o) {
        super.setOpaque(o);
        if (UIUtils.isNimbusLookAndFeel() && !o)
            setBackground(new Color(0, 0, 0, 0));
        if (txt != null) setText(txt);
    }
    
    private String txt;

    public void setText(String value) {
        txt = value;
        
        Font font = getFont();
        Color fgColor = getForeground();
        Color bgColor = getBackground();
        
        value = value.replaceAll("\\n\\r|\\r\\n|\\n|\\r", "<br>"); //NOI18N
        value = value.replace("<code>", "<code style=\"font-size: " + font.getSize() + "pt;\">"); //NOI18N
        
        String fgText = "rgb(" + fgColor.getRed() + "," + fgColor.getGreen() + "," + fgColor.getBlue() + ")"; //NOI18N
        String bgText = isOpaque() ? "rgb(" + bgColor.getRed() + "," + bgColor.getGreen() + "," + bgColor.getBlue() + ")" : null; //NOI18N
        
        String alignText = null;
        switch (halign) {
            case SwingConstants.CENTER:
                alignText = "center"; //NOI18N
                break;
            case SwingConstants.RIGHT:
            case SwingConstants.TRAILING:
                alignText = "right"; //NOI18N
                break;
        }
        
        String bodyFlags = "text=\"" + fgText + "\""; //NOI18N
        if (bgText != null) bodyFlags += " bgcolor=\"" + bgText + "\""; //NOI18N
        if (alignText != null) bodyFlags += " align=\"" + alignText + "\""; //NOI18N
        
        super.setText("<html><body " + bodyFlags + " style=\"font-size: " + font.getSize() //NOI18N
                      + "pt; font-family: " + font.getName() + ";\">" + value + "</body></html>"); //NOI18N
    }
    
    public void setForeground(Color fg) {
        super.setForeground(fg);
        if (txt != null) setText(txt);
    }
    
    public void setBackground(Color bg) {
        super.setBackground(bg);
//        setBorder(getBorder());
        if (txt != null) setText(txt);
    }
    
//    public void setBorder(Border b) {
//        Insets i = b == null ? new Insets(0, 0, 0, 0) : b.getBorderInsets(this);
//        if (!isOpaque()) super.setBorder(BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right));
//        else super.setBorder(BorderFactory.createMatteBorder(i.top, i.left, i.bottom, i.right, getBackground()));
//    }
    
    public void setHorizontalAlignment(int alignment) {
        if (alignment == halign) return;
        halign = alignment;
        if (txt != null) setText(txt);
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (!isEnabled()) {
            return;
        }

        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            showURL(e.getURL());
        } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    protected void showURL(URL url) {
        // override to react to URL clicks
    }
}
