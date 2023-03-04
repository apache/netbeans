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

package org.netbeans.core.ui.notifications;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.openide.util.ImageUtilities;

/**
 * Shows, hides balloon-like tooltip windows.
 * 
 * @author S. Aubrecht
 */
class BalloonManager {

    private static Balloon currentBalloon;
    private static JLayeredPane currentPane;
    private static ComponentListener listener;
    private static WindowStateListener windowListener;
    private static Window ownerWindow;
    
    /**
     * Show balloon-like tooltip pointing to the given component. The balloon stays
     * visible until dismissed by clicking its 'close' button or by invoking its default action.
     * @param owner The component which the balloon will point to
     * @param content Content to be displayed in the balloon.
     * @param defaultAction Action to invoked when the balloon is clicked, can be null.
     * @param timeoutMillies Number of milliseconds before the balloon disappears, 0 to keep it visible forever
     */
    public static synchronized void show( final JComponent owner, JComponent content, ActionListener defaultAction, ActionListener dismissAction, int timeoutMillis ) {
        assert null != owner;
        assert null != content;
        
        //hide current balloon (if any)
        dismiss();
            
        currentBalloon = new Balloon( content, defaultAction, dismissAction, timeoutMillis );
        currentPane = JLayeredPane.getLayeredPaneAbove( owner );
        
        listener = new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                dismiss();
            }

            public void componentMoved(ComponentEvent e) {
                dismiss();
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
                dismiss();
            }
        };
        windowListener = new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                dismiss();
            }
        };
        ownerWindow = SwingUtilities.getWindowAncestor(owner);
        if( null != ownerWindow ) {
            ownerWindow.addWindowStateListener(windowListener);
        }
        currentPane.addComponentListener( listener );
        configureBalloon( currentBalloon, currentPane, owner );
        currentPane.add( currentBalloon, new Integer(JLayeredPane.POPUP_LAYER-1) );
    }
    
    /**
     * Dismiss currently showing balloon tooltip (if any)
     */
    public static synchronized void dismiss() {
        if( null != currentBalloon ) {
            currentBalloon.setVisible( false );
            currentBalloon.stopDismissTimer();
            currentPane.remove( currentBalloon );
            currentPane.repaint();
            currentPane.removeComponentListener( listener );
            if( null != ownerWindow ) {
                ownerWindow.removeWindowStateListener(windowListener);
            }
            currentBalloon.content.removeMouseListener (currentBalloon.mouseListener);
            currentBalloon = null;
            currentPane = null;
            listener = null;
            ownerWindow = null;
            windowListener = null;
        }
    }
    
    public static synchronized void dismissSlowly (final int timeout) {
        if( null != currentBalloon ) {
            if( currentBalloon.timeoutMillis > 0 ) {
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        if (currentBalloon != null) {
                            currentBalloon.startDismissTimer (timeout);
                        }
                    }
                });
            } else {
                dismiss ();
            }
        }
    }
    
    public static synchronized void stopDismissSlowly () {
        if( null != currentBalloon ) {
            if( currentBalloon.timeoutMillis > 0 ) {
                currentBalloon.timeoutMillis = ToolTipManager.sharedInstance ().getDismissDelay (); // on MouseEnter cut timeout on 100ms
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        if (currentBalloon != null) {
                            currentBalloon.stopDismissTimer ();
                        }
                    }
                });
            }
        }
    }
    
    private static void configureBalloon( Balloon balloon, JLayeredPane pane, JComponent ownerComp ) {
        Rectangle ownerCompBounds = ownerComp.getBounds();
        ownerCompBounds = SwingUtilities.convertRectangle( ownerComp.getParent(), ownerCompBounds, pane );
        
        int paneWidth = pane.getWidth();
        int paneHeight = pane.getHeight();
        
        Dimension balloonSize = balloon.getPreferredSize();
        balloonSize.height += Balloon.ARC;
        
        //first try lower right corner
        if( ownerCompBounds.x + ownerCompBounds.width + balloonSize.width < paneWidth
            && 
            ownerCompBounds.y + ownerCompBounds.height + balloonSize.height + Balloon.ARC < paneHeight ) {
            
            balloon.setArrowLocation( GridBagConstraints.SOUTHEAST );
            balloon.setBounds( ownerCompBounds.x+ownerCompBounds.width-Balloon.ARC/2, 
                    ownerCompBounds.y+ownerCompBounds.height, balloonSize.width+Balloon.ARC, balloonSize.height );
        
        //upper right corner
        } else  if( ownerCompBounds.x + ownerCompBounds.width + balloonSize.width < paneWidth
                    && 
                    ownerCompBounds.y - balloonSize.height - Balloon.ARC > 0 ) {
            
            balloon.setArrowLocation( GridBagConstraints.NORTHEAST );
            balloon.setBounds( ownerCompBounds.x+ownerCompBounds.width-Balloon.ARC/2, 
                    ownerCompBounds.y-balloonSize.height, balloonSize.width+Balloon.ARC, balloonSize.height );
        
        //lower left corner
        } else  if( ownerCompBounds.x - balloonSize.width > 0
                    && 
                    ownerCompBounds.y + ownerCompBounds.height + balloonSize.height + Balloon.ARC < paneHeight ) {
            
            balloon.setArrowLocation( GridBagConstraints.SOUTHWEST );
            balloon.setBounds( ownerCompBounds.x-balloonSize.width+Balloon.ARC/2, 
                    ownerCompBounds.y+ownerCompBounds.height, balloonSize.width+Balloon.ARC, balloonSize.height );
        //upper left corent
        } else {
            balloon.setArrowLocation( GridBagConstraints.NORTHWEST );
            balloon.setBounds( ownerCompBounds.x-balloonSize.width/*+Balloon.ARC/2*/, 
                    ownerCompBounds.y-balloonSize.height, balloonSize.width+Balloon.ARC, balloonSize.height );
        }
    }

    private static class Balloon extends JPanel {

        private static final int Y_OFFSET = 8;
        private static final int ARC = 15;
        private static final int SHADOW_SIZE = 3;


        private JComponent content;
        private MouseListener mouseListener;
        private ActionListener defaultAction;
        private JButton btnDismiss;
        private int arrowLocation = GridBagConstraints.SOUTHEAST;
        private float currentAlpha = 1.0f;
        private Timer dismissTimer;
        private int timeoutMillis;
        private boolean isMouseOverEffect = false;

        public Balloon( final JComponent content, final ActionListener defaultAction, final ActionListener dismissAction, final int timeoutMillis ) {
            super( new GridBagLayout() );
            this.content = content;
            this.defaultAction = defaultAction;
            this.timeoutMillis = timeoutMillis;
            content.setOpaque( false );

            btnDismiss = new DismissButton();
            btnDismiss.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    BalloonManager.dismiss();
                }
            });
            if( null != dismissAction )
                btnDismiss.addActionListener(dismissAction);

            add( content, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0)); 
            add( btnDismiss, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.NORTHEAST,GridBagConstraints.NONE,new Insets(7,0,0,7),0,0)); 

            setOpaque( false );

            mouseListener = new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    BalloonManager.dismiss();
                    if( null != defaultAction )
                        defaultAction.actionPerformed( new ActionEvent( Balloon.this, 0, "", e.getWhen(), e.getModifiers() ) );
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                    if( null != defaultAction )
                        content.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
                    stopDismissTimer();
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    content.setCursor( Cursor.getDefaultCursor() );
                    if( Balloon.this.timeoutMillis > 0 )
                        startDismissTimer (ToolTipManager.sharedInstance ().getDismissDelay ());
                }
            };
            content.addMouseListener(mouseListener);
            
            if( timeoutMillis > 0 ) {
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        startDismissTimer (timeoutMillis);
                    }
                });
            }
            
            MouseListener mouseOverAdapter = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isMouseOverEffect = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isMouseOverEffect = false;
                    repaint();
                }
            };
            
            addMouseListener(mouseOverAdapter);
            content.addMouseListener(mouseOverAdapter);
            btnDismiss.addMouseListener(mouseOverAdapter);

            handleMouseOver( content, mouseOverAdapter );
        }
        
        private static final float ALPHA_DECREMENT = 0.03f;
        private static final int DISMISS_REPAINT_REPEAT = 100;

        private void handleMouseOver( Container c, MouseListener ml ) {
            c.addMouseListener(ml);
            for( Component child : c.getComponents() ) {
                child.addMouseListener(ml);
                if( child instanceof Container )
                    handleMouseOver((Container)child, ml);
            }
        }
        
        synchronized void startDismissTimer (int timeout) {
            stopDismissTimer();
            currentAlpha = 1.0f;
            dismissTimer = new Timer(DISMISS_REPAINT_REPEAT, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    currentAlpha -= ALPHA_DECREMENT;
                    if( currentAlpha <= ALPHA_DECREMENT ) {
                        stopDismissTimer();
                        dismiss();
                    }
                    repaint();
                }
            });
            dismissTimer.setInitialDelay (timeout);
            dismissTimer.start();
        }
        
        synchronized void stopDismissTimer() {
            if( null != dismissTimer ) {
                dismissTimer.stop();
                dismissTimer = null;
                currentAlpha = 1.0f;
            }
        }
        
        void setArrowLocation( int arrowLocation) {
            this.arrowLocation = arrowLocation;
            if( arrowLocation == GridBagConstraints.NORTHEAST || arrowLocation == GridBagConstraints.NORTHWEST ) {
                setBorder( BorderFactory.createEmptyBorder(0, 0, Y_OFFSET, btnDismiss.getWidth()));
            } else {
                setBorder( BorderFactory.createEmptyBorder(Y_OFFSET, 0, 0, btnDismiss.getWidth()));
            }
        }
        
        private Shape getMask( int w, int h ) {
            w--;
            w -= SHADOW_SIZE;
            GeneralPath path = new GeneralPath();
            Area area = null;
            switch( arrowLocation ) {
            case GridBagConstraints.SOUTHEAST: 
                area = new Area(new RoundRectangle2D.Float(0, Y_OFFSET, w, h-Y_OFFSET-SHADOW_SIZE, ARC, ARC));
                path.moveTo(ARC/2, 0);
                path.lineTo(ARC/2, Y_OFFSET);
                path.lineTo(ARC/2+Y_OFFSET, Y_OFFSET);
                break;
            case GridBagConstraints.NORTHEAST: 
                area = new Area(new RoundRectangle2D.Float(0, SHADOW_SIZE, w, h-Y_OFFSET-SHADOW_SIZE, ARC, ARC));
                path.moveTo(ARC/2, h-1);
                path.lineTo(ARC/2, h-1-Y_OFFSET);
                path.lineTo(ARC/2+Y_OFFSET, h-1-Y_OFFSET);
                break;
            case GridBagConstraints.SOUTHWEST: 
                area = new Area(new RoundRectangle2D.Float(0, Y_OFFSET, w, h-Y_OFFSET-SHADOW_SIZE, ARC, ARC));
                path.moveTo(w-ARC/2, 0);
                path.lineTo(w-ARC/2, Y_OFFSET);
                path.lineTo(w-ARC/2-Y_OFFSET, Y_OFFSET);
                break;
            case GridBagConstraints.NORTHWEST: 
                area = new Area(new RoundRectangle2D.Float(0, SHADOW_SIZE, w, h-Y_OFFSET-SHADOW_SIZE, ARC, ARC));
                path.moveTo(w-ARC/2, h-1);
                path.lineTo(w-ARC/2-Y_OFFSET, h-1-Y_OFFSET);
                path.lineTo(w-ARC/2, h-1-Y_OFFSET);
                break;
            }
                
            path.closePath();
            area.add(new Area(path));
            return area;
        }
        
        private Shape getShadowMask( Shape parentMask ) {
            Area area = new Area(parentMask);

            AffineTransform tx = new AffineTransform();
            tx.translate(SHADOW_SIZE, SHADOW_SIZE );//Math.sin(ANGLE)*(getHeight()+SHADOW_SIZE), 0);
            area.transform(tx);
            area.subtract(new Area(parentMask));
            return area;
        }


        @Override
        protected void paintBorder(Graphics g) {
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            
            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            
            Composite oldC = g2d.getComposite();
            Shape s = getMask( getWidth(), getHeight() );

            g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.25f*currentAlpha ) );
            g2d.setColor( Color.black );
            g2d.fill( getShadowMask(s) );
            
            g2d.setColor( UIManager.getColor( "ToolTip.background" ) ); //NOI18N
            g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, currentAlpha ) );
            Point2D p1 = s.getBounds().getLocation();
            Point2D p2 = new Point2D.Double(p1.getX(), p1.getY()+s.getBounds().getHeight());
            if( isMouseOverEffect )
                g2d.setPaint( new GradientPaint( p2, getMouseOverGradientStartColor(), p1, getMouseOverGradientFinishColor() ) );
            else
                g2d.setPaint( new GradientPaint( p2, getDefaultGradientStartColor(), p1, getDefaultGradientFinishColor() ) );
            g2d.fill(s);
            g2d.setColor( Color.black );
            g2d.draw(s);
            g2d.setComposite( oldC );
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            Composite oldC = g2d.getComposite();
            g2d.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, currentAlpha ) );
            super.paintChildren(g);
            g2d.setComposite( oldC );
        }

        private static Color mouseOverGradientStartColor = null;
        private static Color mouseOverGradientFinishColor = null;

        private static Color defaultGradientStartColor = null;
        private static Color defaultGradientFinishColor = null;

        private static final boolean isMetal = UIManager.getLookAndFeel() instanceof MetalLookAndFeel;
        private static final boolean isNimbus = "Nimbus".equals( UIManager.getLookAndFeel().getID() ); //NOI18N

        private static Color getMouseOverGradientStartColor() {
            if( null == mouseOverGradientStartColor ) {
                mouseOverGradientStartColor = UIManager.getColor("nb.core.ui.balloon.mouseOverGradientStartColor"); //NOI18N
                if( null == mouseOverGradientStartColor ) {
                    mouseOverGradientStartColor = new Color(224,224,185);
                    if( isMetal || isNimbus ) {
                        Color c = UIManager.getColor( "ToolTip.background" ); //NOI18N
                        if( null != c ) {
                            mouseOverGradientStartColor = c.darker();
                        }
                    }
                }
            }
            return mouseOverGradientStartColor;
        }

        private static Color getMouseOverGradientFinishColor() {
            if( null == mouseOverGradientFinishColor ) {
                mouseOverGradientFinishColor = UIManager.getColor("nb.core.ui.balloon.mouseOverGradientFinishColor"); //NOI18N
                if( null == mouseOverGradientFinishColor ) {
                    mouseOverGradientFinishColor = new Color(255,255,241);
                    if( isMetal || isNimbus ) {
                        Color c = UIManager.getColor( "ToolTip.background" ); //NOI18N
                        if( null != c ) {
                            mouseOverGradientFinishColor = c.brighter();
                        }
                    }
                }
            }
            return mouseOverGradientFinishColor;
        }

        private static Color getDefaultGradientStartColor() {
            if( null == defaultGradientStartColor ) {
                defaultGradientStartColor = UIManager.getColor("nb.core.ui.balloon.defaultGradientStartColor"); //NOI18N
                if( null == defaultGradientStartColor ) {
                    defaultGradientStartColor = new Color(225,225,225);
                    if( isMetal || isNimbus ) {
                        Color c = UIManager.getColor( "ToolTip.background" ); //NOI18N
                        if( null != c ) {
                            defaultGradientStartColor = c.darker();
                        }
                    }
                }
            }
            return defaultGradientStartColor;
        }


        private static Color getDefaultGradientFinishColor() {
            if( null == defaultGradientFinishColor ) {
                defaultGradientFinishColor = UIManager.getColor("nb.core.ui.balloon.defaultGradientFinishColor"); //NOI18N
                if( null == defaultGradientFinishColor ) {
                    defaultGradientFinishColor = new Color(255,255,255);
                    if( isMetal || isNimbus ) {
                        Color c = UIManager.getColor( "ToolTip.background" ); //NOI18N
                        if( null != c ) {
                            defaultGradientFinishColor = c;
                        }
                    }
                }
            }
            return defaultGradientFinishColor;
        }
    }
    
    static class DismissButton extends JButton {

        public DismissButton() {
            setIcon( ImageUtilities.loadImageIcon( "org/netbeans/core/ui/resources/dismiss_enabled.png", true ) );
            setRolloverIcon(ImageUtilities.loadImageIcon( "org/netbeans/core/ui/resources/dismiss_rollover.png", true ));
            setPressedIcon(ImageUtilities.loadImageIcon( "org/netbeans/core/ui/resources/dismiss_pressed.png", true ));

            setBorder( BorderFactory.createEmptyBorder() );
            setBorderPainted( false );
            setFocusable( false );
            setOpaque( false );
            setRolloverEnabled( true );
        }
        
        @Override
        public void paint(Graphics g) {
            Icon icon = null;
            if( getModel().isArmed() && getModel().isPressed() ) {
                icon = getPressedIcon();
            } else if( getModel().isRollover() ) {
                icon = getRolloverIcon();
            } else {
                icon = getIcon();
            }
            icon.paintIcon( this, g, 0, 0 );
        }
        
    }
}
