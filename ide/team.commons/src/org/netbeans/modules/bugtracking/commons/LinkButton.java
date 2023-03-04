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

package org.netbeans.modules.bugtracking.commons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.logging.Level;
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
public class LinkButton extends JButton implements MouseListener, FocusListener {

    private static final Font  BUTTON_FONT = getButtonFont();
    static Color linkInFocusColor   = null;
    private static Color linkColor          = null;
    private static Color mouseOverLinkColor = null;
    private static Color visitedLinkColor   = null;
    private static final Stroke LINK_IN_FOCUS_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_BEVEL, 0, new float[] {0, 2}, 0);

    private boolean underline = false;

    static {
        linkInFocusColor = UIManager.getColor( "nb.html.link.foreground.focus" ); //NOI18N
        if( null == linkInFocusColor )
            linkInFocusColor = new Color(0xFF8E00);

        linkColor = UIManager.getColor( "nb.html.link.foreground" ); //NOI18N
        if( null == linkColor ) {
            Color labelColor = javax.swing.UIManager.getDefaults().getColor("Label.foreground"); // NOI18N
            if (labelColor == null || (labelColor.getRed() < 192 && labelColor.getGreen() < 192 && labelColor.getBlue() < 192)) {
                linkColor = new Color(0x164B7B);
            } else { // hack for high-contrast black
                linkColor = new Color(0x2170B8);
            }
        }

        mouseOverLinkColor = UIManager.getColor( "nb.html.link.foreground.hover" ); //NOI18N
        if( null == mouseOverLinkColor )
            mouseOverLinkColor = new Color(0xFF8E00);

        visitedLinkColor = UIManager.getColor( "nb.html.link.foreground.visited" ); //NOI18N
        if( null == visitedLinkColor )
            visitedLinkColor = new Color(0x5591D2);
    }
    private Color alternativeLinkColor;

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
    
    public void setColors(Color linkColor, Color linkInFocusColor, Color mouseOverLinkColor, Color visitedLinkColor) {
        LinkButton.linkInFocusColor = linkInFocusColor;
        LinkButton.linkColor = linkColor;
        LinkButton.mouseOverLinkColor = mouseOverLinkColor;
        LinkButton.visitedLinkColor   = visitedLinkColor;
    }

    private void init() {
        setForeground( getLinkColor() );
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

        addFocusListener( this );
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) {
        if( isEnabled() ) {
            underline = true;
            setForeground( linkInFocusColor );
            repaint();
            onMouseEntered( e );
            setForeground( mouseOverLinkColor );
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
            underline = false;
            setForeground( isVisited() ? visitedLinkColor : getLinkColor() );
            repaint();
            onMouseExited( e );
    }
    
    void setAlternativeLinkColor(Color c) {
        alternativeLinkColor = c;
    }

    Color getLinkColor() {
        Color c = alternativeLinkColor;
        return c != null ? c : linkColor;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        GraphicsUtils.configureDefaultRenderingHints(g2);
        super.paintComponent(g2);

        Dimension size = getSize();
        if( hasFocus() && isEnabled() ) {
            g2.setStroke( LINK_IN_FOCUS_STROKE );
            g2.setColor( linkInFocusColor );
            g2.drawRect( 0, 0, size.width - 1, size.height - 1 );
        }
    }
    
    @Override
    public void focusLost(FocusEvent e) { }

    @Override
    public void focusGained(FocusEvent e) {
/*        Rectangle rect = getBounds();
        rect.grow( 0, BUTTON_FONT.getSize() );
        scrollRectToVisible( rect );*/
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

    
    public static class MailtoButton extends LinkButton {
        public MailtoButton(String text, String accessibleCtx, final String mail) {
            this(text, accessibleCtx, mail, null, null);
        }
        
        public MailtoButton(String text, String accessibleCtx, final String mail, String subject, String body) {
            super(text);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    StringBuilder mailtoURI = new StringBuilder();
                    mailtoURI.append("mailto:");
                    mailtoURI.append(mail);

                    boolean amp = false;
                    if(subject != null || body != null) {
                        mailtoURI.append("?");                        
                    }
                    if(subject != null) {
                        mailtoURI.append("subject=");                                                
                        mailtoURI.append(encodeURI(subject));                                                
                        amp = true;
                    } 
                    if(body != null) {
                        if(amp) {
                            mailtoURI.append("&");                                                                            
                        }
                        mailtoURI.append("body=");                                                
                        mailtoURI.append(encodeURI(body));
                    }
                    try {
                        Desktop.getDesktop().mail(new URI(mailtoURI.toString()));
                    } catch (URISyntaxException | IOException ex) {
                        Support.LOG.log(Level.INFO, "unable to invoke: \n" + mailtoURI.toString(), ex); // NOI18N
                    }
                }

                private String encodeURI(String text) {
                    try {
                        return URLEncoder.encode(text, "UTF-8")
                                .replaceAll("\\%28", "(")
                                .replaceAll("\\%29", ")")
                                .replaceAll("\\+", "%20")
                                .replaceAll("\\%21", "!")
                                .replaceAll("\\%27", "'")
                                .replaceAll("\\%7E", "~");
                    } catch (UnsupportedEncodingException e) {
                        Support.LOG.log(Level.WARNING, null, e);
                    }
                    return text;
                }
            });  
            getAccessibleContext().setAccessibleDescription(accessibleCtx != null ? accessibleCtx : "");
        }
    }      
}
