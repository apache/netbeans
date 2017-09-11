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

    private final static Border regularBorder = ButtonBorder.createRegular();
    private final static Border mouseoverBorder = ButtonBorder.createMouseOver();

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
        Graphics2D g2 = Utils.prepareGraphics( g );
        if( showBorder && !Utils.isDefaultButtons() ) {
            Border b = underline ? mouseoverBorder : regularBorder;
            b.paintBorder(this, g, 0, 0, getWidth(), getHeight());
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
