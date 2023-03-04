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

package org.netbeans.modules.versioning.history;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.openide.awt.GraphicsUtils;

/**
 *
 * @author S. Aubrecht, Tomas Stupka
 */
public class LinkButton extends JButton implements MouseListener {

    private static final Font  BUTTON_FONT = getButtonFont();
    private static final Color LINK_IN_FOCUS_COLOR;
    private static final Color LINK_COLOR;
    private static final Color MOUSE_OVER_LINK_COLOR;
    private static final Color VISITED_LINK_COLOR;
    private static final Stroke LINK_IN_FOCUS_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_BEVEL, 0, new float[] {0, 2}, 0);

    private boolean underline = false;

    static {
        Color c = UIManager.getColor("nb.html.link.foreground.focus"); //NOI18N
        LINK_IN_FOCUS_COLOR = c == null ? new Color(0xFF8E00) : c;

        c = UIManager.getColor("nb.html.link.foreground"); //NOI18N
        LINK_COLOR = c == null ? new Color(0x164B7B) : c;

        c = UIManager.getColor("nb.html.link.foreground.hover"); //NOI18N
        MOUSE_OVER_LINK_COLOR = c == null ? new Color(0xFF8E00) : c;

        c = UIManager.getColor("nb.html.link.foreground.visited"); //NOI18N
        VISITED_LINK_COLOR = c == null ? new Color(0x5591D2) : c;
    }

    public LinkButton(String text, Icon icon) {
        super(text, icon);
        init();
    }

    public LinkButton(Action a) {
        super(a);
        init();
    }

    public LinkButton(String text) {
        super(text);
        init();
    }

    public LinkButton(Icon icon) {
        super(icon);
        init();
    }

    public LinkButton() {
        super();
        init();
    }

    private void init() {
        setForeground( LINK_COLOR );
        setFont( BUTTON_FONT );
        setBorder( new EmptyBorder(1, 1, 1, 1) );
        setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
        setHorizontalAlignment( JLabel.LEFT );
        addMouseListener(this);
        setFocusable( true );

        setMargin( new Insets(0, 0, 0, 0) );
        setBorderPainted( false );
        setFocusPainted( false );
        setRolloverEnabled( true );
        setContentAreaFilled( false );
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if( isEnabled() ) {
            underline = true;
            setForeground( LINK_IN_FOCUS_COLOR );
            repaint();
            onMouseEntered( e );
            setForeground( MOUSE_OVER_LINK_COLOR );
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if( isEnabled() ) {
            underline = false;
            setForeground( isVisited() ? VISITED_LINK_COLOR : LINK_COLOR );
            repaint();
            onMouseExited( e );
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        GraphicsUtils.configureDefaultRenderingHints(g2);
        super.paintComponent(g2);

        Dimension size = getSize();
        if( hasFocus() && isEnabled() ) {
            g2.setStroke( LINK_IN_FOCUS_STROKE );
            g2.setColor( LINK_IN_FOCUS_COLOR );
            g2.drawRect( 0, 0, size.width - 1, size.height - 1 );
        }
    }

    protected void onMouseExited(MouseEvent e) {
    }

    protected void onMouseEntered(MouseEvent e) {
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if( underline && isEnabled() ) {
            Font f = getFont();
            FontMetrics fm = getFontMetrics(f);
            int iconWidth = 0;
            if( null != getIcon() ) {
                iconWidth = getIcon().getIconWidth()+getIconTextGap();
            }
            int x1 = iconWidth;
            int y1 = fm.getHeight();
            int x2 = fm.stringWidth(getText()) + iconWidth;
            if( getText().length() > 0 )
                g.drawLine(x1, y1, x2, y1);
        }
    }
    
    protected boolean isVisited() {
        return false;
    }

    private static Font getButtonFont() {
        Font defaultFont = UIManager.getFont("Button.font"); // NOI18N
        if(defaultFont != null) {
            return defaultFont;
        }
        return new Font(null, Font.PLAIN, 12);
    }

}
