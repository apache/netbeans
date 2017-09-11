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
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

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
        Graphics2D g2 = prepareGraphics( g );
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

    private static Graphics2D prepareGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Map rhints = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
        if( rhints == null && Boolean.getBoolean("swing.aatext") ) { //NOI18N
             g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        } else if( rhints != null ) {
            g2.addRenderingHints( rhints );
        }
        return g2;
    }

    private static Font getButtonFont() {
        Font defaultFont = UIManager.getFont("Button.font"); // NOI18N
        if(defaultFont != null) {
            return defaultFont;
        }
        return new Font(null, Font.PLAIN, 12);
    }

}
