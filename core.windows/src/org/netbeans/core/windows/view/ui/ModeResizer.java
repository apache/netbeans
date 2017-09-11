/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core.windows.view.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.windows.TopComponent;

/**
 * Adds AWT event listener and moves the given splitters when arrows keys are
 * pressed.
 * 
 * @author S. Aubrecht
 */
final class ModeResizer implements AWTEventListener, PropertyChangeListener {
    
    private static final int STEP_SMALL = 1;
    private static final int STEP_NORMAL = 10;
    private static final int STEP_LARGE = 50;
    private static ModeResizer currentResizer;
    
    private final Component resizingComponent;
    private final MultiSplitDivider splitter;
    private final MultiSplitDivider parentSplitter;
    private final Point originalLocation;
    private final Point originalParentLocation;
    
    private Component oldGlass = null;
    private GlassPane glass = null;
    private JFrame frame = null;
    
    private ModeResizer( Component c, MultiSplitDivider splitter, MultiSplitDivider parentSplitter ) {
        this.resizingComponent = c;
        this.splitter = splitter;
        this.parentSplitter = parentSplitter;
        originalLocation = splitter.initDragMinMax();
        originalParentLocation = null == parentSplitter ? null : parentSplitter.initDragMinMax();
    }
    
    static void start( Component c, MultiSplitDivider splitter, MultiSplitDivider parentSplitter ) {
        if( !SwingUtilities.isEventDispatchThread() ) {
            throw new IllegalStateException( "This method must be called from EDT."); //NOI18N
        }
        if( null != currentResizer ) {
            currentResizer.stop( false );
            currentResizer = null;
        }
        currentResizer = new ModeResizer( c, splitter, parentSplitter );
        currentResizer.start();
    }
    
    private void start() {
        Toolkit.getDefaultToolkit().addAWTEventListener( this, KeyEvent.KEY_EVENT_MASK );
        TopComponent.getRegistry().addPropertyChangeListener( this );
        
        Window w = SwingUtilities.getWindowAncestor( resizingComponent );
        if( w instanceof JFrame ) {
            frame = ( JFrame ) w;
            oldGlass = frame.getGlassPane();
            glass = new GlassPane( resizingComponent );
            frame.setGlassPane( glass );
            glass.setVisible( true );
            glass.invalidate();
            glass.revalidate();
            glass.repaint();
            glass.refresh();
            
        }
    }
    
    static void stop() {
        if( null != currentResizer )
            currentResizer.stop( true );
    }

    @Override
    public void eventDispatched( AWTEvent e ) {
        if( !(e instanceof KeyEvent) )
            return;
        KeyEvent ke = ( KeyEvent ) e;
        ke.consume();
        
        if( e.getID() == KeyEvent.KEY_PRESSED ) {
            int nStep = 0;

            switch( ke.getModifiers() ) {
                case InputEvent.SHIFT_MASK:
                    nStep = STEP_SMALL;
                    break;

                case 0:
                    nStep = STEP_NORMAL;
                    break;

                case InputEvent.CTRL_MASK:
                    nStep = STEP_LARGE;
                    break;
            }

            if( nStep != 0 ) {
                switch( ke.getKeyCode() ) {
                    case KeyEvent.VK_LEFT:
                        moveBy( -nStep, 0 );
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveBy( nStep, 0 );
                        break;
                    case KeyEvent.VK_UP:
                        moveBy( 0, -nStep );
                        break;
                    case KeyEvent.VK_DOWN:
                        moveBy( 0, nStep );
                        break;
                    case KeyEvent.VK_ENTER:
                        stop( true );
                        break;
                    case KeyEvent.VK_ESCAPE:
                        stop( true );
                        break;
                }
            }
        }
    }
    
    private void moveBy( int deltaX, int deltaY ) {
        if( deltaX != 0 ) {
            if( splitter.isHorizontal() ) {
                splitter.resize( deltaX );
            } else if( null != parentSplitter && parentSplitter.isHorizontal() ) {
                parentSplitter.resize( deltaX );
            }
        }
        if( deltaY != 0 ) {
            if( splitter.isVertical() ) {
                splitter.resize( deltaY );
            } else if( null != parentSplitter && parentSplitter.isVertical() ) {
                parentSplitter.resize( deltaY );
            }
        }
        if( null != glass )
            glass.refresh();
    }

    @Override
    public void propertyChange( PropertyChangeEvent evt ) {
        if( TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() )
            || TopComponent.Registry.PROP_OPENED.equals( evt.getPropertyName() ) ) {
            stop(true);
        }
    }
    
    private void stop( boolean commitChanges ) {
        Toolkit.getDefaultToolkit().removeAWTEventListener( this );
        TopComponent.getRegistry().removePropertyChangeListener( this );
        if( null != frame ) {
            boolean glassVisible = oldGlass.isVisible();
            frame.setGlassPane( oldGlass);
            oldGlass.setVisible( glassVisible );
        }
        if( !commitChanges ) {
            if( null != parentSplitter ) {
                parentSplitter.finishDraggingTo( originalParentLocation );
            }
            splitter.finishDraggingTo( originalLocation );
        }
        if( currentResizer == this )
            currentResizer = null;
    }
 
    private static class GlassPane extends JPanel {
        
        private final Component resizingComponent;
        private final JLabel lbl;
        private final JPanel panel;
        
        public GlassPane( Component resizingComponent ) {
            super( null );
            setOpaque( false );
            this.resizingComponent = resizingComponent;
            lbl = new JLabel();
            panel = new JPanel( new BorderLayout() );
            panel.setBorder( BorderFactory.createEtchedBorder() );
            lbl.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
            panel.add( lbl );
            add( panel );
        }
        
        public void refresh() {
            Point p = resizingComponent.getLocationOnScreen();
            int width = resizingComponent.getWidth();
            int height = resizingComponent.getHeight();
            
            p.x += width/2;
            p.y += height/2;
            
            lbl.setText( width + " x " + height ); //NOI18N
            
            SwingUtilities.convertPointFromScreen( p, this );
            Dimension size = panel.getPreferredSize();
            p.x -= size.width/2;
            p.y -= size.height/2;
            panel.setLocation( p );
            panel.setSize( size );
        }
    }
}
