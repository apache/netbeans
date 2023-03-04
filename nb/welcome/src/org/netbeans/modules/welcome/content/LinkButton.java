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

package org.netbeans.modules.welcome.content;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.openide.awt.GraphicsUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
public abstract class LinkButton extends JButton
        implements Constants, MouseListener, ActionListener, FocusListener {

    private boolean underline = false;
    private final boolean showBorder;
    private Font rollOverFont;
    private Font regularFont;

    private final Color defaultForeground;

    private static final Border regularBorder = ButtonBorder.createRegular();
    private static final Border mouseoverBorder = ButtonBorder.createMouseOver();

    private String usageTrackingId;

    public LinkButton( String label, String usageTrackingId ) {
        this( label, false, usageTrackingId );
    }

    public LinkButton( String label, boolean showBorder, String usageTrackingId ) {
        this( label, Utils.getLinkColor(), showBorder, usageTrackingId );
    }

    public LinkButton( String label, Color foreground, String usageTrackingId ) {
        this( label, foreground, false, usageTrackingId );
    }

    public LinkButton( String label, Color foreground, boolean showBorder, String usageTrackingId ) {
        super( label );
        this.defaultForeground = foreground;
        this.showBorder = showBorder;
        if( !showBorder || !Utils.isDefaultButtons() )
            setForeground( defaultForeground );
        setFont( BUTTON_FONT );
        this.usageTrackingId = usageTrackingId;

        if( showBorder ) {
            if( !Utils.isDefaultButtons() ) {
                setBorder( BorderFactory.createEmptyBorder(6, 12, 6, 12) );
                setMargin( new Insets(12,12,12,12) );
            }
        } else {
            setBorder( new EmptyBorder(1, 1, 1, 1) );
            setMargin( new Insets(0, 0, 0, 0) );
        }

        setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
        setHorizontalAlignment( JLabel.LEFT );
        addMouseListener(this);
        setFocusable( true );

        if( !showBorder || !Utils.isDefaultButtons() ) {
            setBorderPainted( false );
            setFocusPainted( false );
            setRolloverEnabled( true );
            setContentAreaFilled( false );
        }

        addActionListener( this );
        addFocusListener( this );
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
            repaint();
            onMouseEntered( e );
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if( isEnabled() ) {
            underline = false;
            if( !showBorder )
                setForeground( isVisited() ? Utils.getVisitedLinkColor(): defaultForeground );
            repaint();
            onMouseExited( e );
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        GraphicsUtils.configureDefaultRenderingHints(g);
        Graphics2D g2 = (Graphics2D) g;
        if( showBorder && !Utils.isDefaultButtons() ) {
            Border b = underline ? mouseoverBorder : regularBorder;
            b.paintBorder(this, g2, 0, 0, getWidth(), getHeight());
        }
        super.paintComponent(g2);

        if( showBorder && Utils.isDefaultButtons() )
            return;

        Dimension size = getSize();
        if( hasFocus() && isEnabled() ) {
            g2.setStroke( LINK_IN_FOCUS_STROKE );
            g2.setColor( Utils.getFocusedLinkColor() );
            g2.drawRect( 0, 0, size.width - 1, size.height - 1 );
        }
    }
    
    @Override
    public void focusLost(FocusEvent e) {
    }

    @Override
    public void focusGained(FocusEvent e) {
        Rectangle rect = getBounds();
        rect.grow( 0, FONT_SIZE );
        scrollRectToVisible( rect );
    }

    protected void onMouseExited(MouseEvent e) {
        if( null != regularFont )
            super.setFont( regularFont );
    }

    protected void onMouseEntered(MouseEvent e) {
        if( null != rollOverFont )
            super.setFont( rollOverFont );
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if( underline && isEnabled() && !showBorder ) {
            g.setColor( getForeground() );
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
    
    public void setUsageTrackingId( String id ) {
        this.usageTrackingId = id;
    }

    protected void logUsage() {
        LogRecord rec = new LogRecord(Level.INFO, "USG_START_PAGE_LINK"); //NOI18N
        String id = usageTrackingId;
        if( null == id )
            id = getText();
        rec.setParameters(new Object[] {id} );
        rec.setLoggerName(Constants.USAGE_LOGGER.getName());
        rec.setResourceBundle(NbBundle.getBundle(BundleSupport.BUNDLE_NAME));
        rec.setResourceBundleName(BundleSupport.BUNDLE_NAME);

        Constants.USAGE_LOGGER.log(rec);
        System.err.println("usage: " + id);
    }

    @Override
    public void setFont( Font font ) {
        super.setFont( font );
        regularFont = font;
        if( showBorder ) {
            rollOverFont = font.deriveFont( Font.BOLD );
        } else {
            rollOverFont = null;
        }
    }
}
