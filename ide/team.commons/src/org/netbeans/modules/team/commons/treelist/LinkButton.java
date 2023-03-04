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
package org.netbeans.modules.team.commons.treelist;

import org.netbeans.modules.team.commons.ColorManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

/**
 * Button with no border painted looking like a hypertext link.
 *
 * @author S. Aubrecht
 */
public class LinkButton extends JButton {
    private final boolean handlePopupEvents;
    private boolean underlined;

    /**
     * C'tor
     *
     * @param icon
     * @param al Action to invoke when the button is pressed, can be null but
     * the button is disabled then.
     */
    public LinkButton(Icon icon, Action a) {
        setIcon(icon);
        setPressedIcon(icon);
        this.handlePopupEvents = true;
        this.underlined = true;
        init(a);
    }

    /**
     * C'tor
     *
     * @param text
     * @param icon
     * @param al Action to invoke when the button is pressed, can be null but
     * the button is disabled then.
     */
    public LinkButton(String text, Icon icon, Action a, boolean underlined) {
        super(text);
        this.underlined = underlined;
        setIcon(icon);
        setPressedIcon(icon);
        Object tooltip = a.getValue(Action.SHORT_DESCRIPTION);
        if (null != tooltip) {
            setToolTipText(tooltip.toString());
        }
        this.handlePopupEvents = true;
        init(a);
    }

    /**
     * C'tor
     *
     * @param text
     * @param al Action to invoke when the button is pressed, can be null but
     * the button is disabled then.
     */
    public LinkButton(String text, Action a) {
        this(text, a, true);
    }

    public LinkButton(String text, Action a, boolean underlined) {
        this(text, true, a, underlined);
    }

    /**
     * C'tor
     *
     * @param text
     * @param handlePopupEvents popup trigger events are dispatched for handling to the button 
     *                          The default button behavior is <code>true</code>, set <code>false</code> 
     *                          in case popup events should be handled directly by TreeList instead.
     * @param al Action to invoke when the button is pressed, can be null but
     * the button is disabled then.
     */    
    public LinkButton(String text, boolean handlePopupEvents, Action a) {
        this(text, handlePopupEvents, a, true);
    }

    public LinkButton(String text, boolean handlePopupEvents, Action a, boolean underlined) {
        super(text);
        this.handlePopupEvents = handlePopupEvents;
        this.underlined = underlined;
        
        if (null != a) {
            Icon icon = (Icon) a.getValue(Action.SMALL_ICON);
            if (null != icon) {
                setIcon(icon);
                setPressedIcon(icon);
            }
            Object tooltip = a.getValue(Action.SHORT_DESCRIPTION);
            if (null != tooltip) {
                setToolTipText(tooltip.toString());
            }
        }
        init(a);
    }

    boolean isHandlingPopupEvents() {
        return handlePopupEvents;
    }

    /**
     * Adjust foreground color
     *
     * @param foreground Preferred color
     * @param isSelected True if the button is selected.
     */
    public void setForeground(Color foreground, boolean isSelected) {
        if (isSelected) {
            if (foreground instanceof UIResource) {
                foreground = new Color(foreground.getRGB());
            }
            setForeground(foreground);
        } else if (isEnabled()) {
            setForeground(ColorManager.getDefault().getLinkColor());
        } else {
            setForeground(ColorManager.getDefault().getDisabledColor());
        }
    }

    private void init(Action al) {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setModel(new Model());
        if (null != al) {
            addActionListener(al);
            setForeground(ColorManager.getDefault().getLinkColor());
        } else {
            setEnabled(false);
            setForeground(ColorManager.getDefault().getDisabledColor());
        }
        Font font = UIManager.getFont("Tree.font");//NOI18N
        if (underlined) {
            Map<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
            map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
            font = font.deriveFont(map);
        }
        setFont(font);
    }

    private static class Model extends DefaultButtonModel {

        @Override
        public boolean isPressed() {
            return false;
        }
    }
}
