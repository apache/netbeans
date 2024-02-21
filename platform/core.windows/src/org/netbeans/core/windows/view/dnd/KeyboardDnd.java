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
package org.netbeans.core.windows.view.dnd;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.SlidingView;
import org.netbeans.core.windows.view.ui.ModeComponent;
import org.openide.windows.TopComponent;

/**
 * Adds AWT event listener and paints drag and drop feedback over the main window
 * as if when dragging a window/mode using mouse. Performs the actual drop when
 * Enter key is pressed.
 * 
 * @author S. Aubrecht
 */
final class KeyboardDnd implements AWTEventListener, PropertyChangeListener {
    
    private static KeyboardDnd currentDnd;
    
    private final WindowDnDManager dndManager;
    private final TopComponentDraggable draggable;
    private final WindowDnDManager.ViewAccessor viewAccessor;
    private final ArrayList<TopComponentDroppable> targets = new ArrayList<TopComponentDroppable>( 30 );
    private int currentIndex;
    private ComponentSide currentSide = new ComponentSide(Side.center);
    private DropTargetGlassPane lastGlass = null;
    
    private KeyboardDnd( WindowDnDManager dndManager, TopComponentDraggable draggable, WindowDnDManager.ViewAccessor viewAccessor ) {
        this.dndManager = dndManager;
        this.draggable = draggable;
        this.viewAccessor = viewAccessor;
    }
    
    static void start( WindowDnDManager dndManager, TopComponentDraggable draggable, WindowDnDManager.ViewAccessor viewAccessor ) {
        if( !SwingUtilities.isEventDispatchThread() ) {
            throw new IllegalStateException( "This method must be called from EDT."); //NOI18N
        }
        if( null != currentDnd ) {
            currentDnd.stop( false );
            currentDnd = null;
        }
        currentDnd = new KeyboardDnd( dndManager, draggable, viewAccessor );
        currentDnd.start();
    }
    
    private void start() {
        Toolkit.getDefaultToolkit().addAWTEventListener( this, KeyEvent.KEY_EVENT_MASK );
        TopComponent.getRegistry().addPropertyChangeListener( this );
        dndManager.dragStarting( null, new Point(0,0), draggable );
        
        for( Component c : viewAccessor.getModeComponents() ) {
            if( !(c instanceof ModeComponent && c instanceof TopComponentDroppable) )
                continue;
            ModeComponent mc = ( ModeComponent ) c;
            ModeView mv = mc.getModeView();
            if( mv instanceof SlidingView )
                continue;
            TopComponent tc = mv.getSelectedTopComponent();
            if( null == tc )
                continue;
            TopComponentDroppable droppable = ( TopComponentDroppable ) c;
            if( droppable.supportsKind( draggable ) )
                targets.add( droppable );
        }
        targets.sort(new Comparator<TopComponentDroppable>() {
            @Override
            public int compare( TopComponentDroppable d1, TopComponentDroppable d2 ) {
                boolean floating1 = isDroppableFloating(d1);
                boolean floating2 = isDroppableFloating(d2);
                if( floating1 && !floating2 )
                    return 1;
                if( !floating1 && floating2 )
                    return -1;
                //#JDEV 13062386
                if( !d1.getDropComponent().isShowing()
                        || !d2.getDropComponent().isShowing() ) {
                    return 0;
                }
                Point loc1 = d1.getDropComponent().getLocationOnScreen();
                Point loc2 = d2.getDropComponent().getLocationOnScreen();
                int res = loc1.x - loc2.x;
                if( res == 0 )
                    res = loc1.y - loc2.y;
                return res;
            }
        });
        targets.add( dndManager.getCenterPanelDroppable() );
        currentIndex = 0;
        refresh();
    }
    
    static void abort() {
        if( null != currentDnd )
            currentDnd.stop( false );
    }
    
    private void refresh() {
        TopComponentDroppable droppable = targets.get( currentIndex );
        DropTargetGlassPane glass = findGlassPane( droppable );
        if( null == glass )
            return;
        Point dropLocation = currentSide.getDropLocation( droppable );
        dropLocation = SwingUtilities.convertPoint( droppable.getDropComponent(), dropLocation, glass );
        glass.dragOver( dropLocation, droppable );
        if( null != lastGlass && lastGlass != glass ) {
            lastGlass.clearIndications();
        }
        lastGlass = glass;
    }
    
    private DropTargetGlassPane findGlassPane( TopComponentDroppable droppable ) {
        Component dropC = droppable.getDropComponent();
        if( dropC instanceof JComponent ) {
            Component c = ((JComponent)dropC).getRootPane().getGlassPane();
            if( c instanceof DropTargetGlassPane )
                return ( DropTargetGlassPane ) c;
        }
        return null;
    }

    @Override
    public void eventDispatched( AWTEvent e ) {
        if( !(e instanceof KeyEvent) )
            return;
        KeyEvent ke = ( KeyEvent ) e;
        ke.consume();
        
        if( e.getID() == KeyEvent.KEY_PRESSED ) {

            switch( ke.getKeyCode() ) {
                case KeyEvent.VK_LEFT:
                    do {
                        currentSide = currentSide.moveLeft();
                    } while( !checkDropLocation() );
                    refresh();
                    break;
                case KeyEvent.VK_RIGHT:
                    do {
                        currentSide = currentSide.moveRight();
                    } while( !checkDropLocation() );
                    refresh();
                    break;
                case KeyEvent.VK_UP:
                    do {
                        currentSide = currentSide.moveUp();
                    } while( !checkDropLocation() );
                    refresh();
                    break;
                case KeyEvent.VK_DOWN:
                    do {
                        currentSide = currentSide.moveDown();
                    } while( !checkDropLocation() );
                    refresh();
                    break;
                case KeyEvent.VK_ENTER:
                    stop( true );
                    break;
                case KeyEvent.VK_ESCAPE:
                    abort();
                    break;
            }
        }
    }
    
    @Override
    public void propertyChange( PropertyChangeEvent evt ) {
        if( TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() )
            || TopComponent.Registry.PROP_OPENED.equals( evt.getPropertyName() ) ) {
            abort();
        }
    }
    
    private void stop( boolean commitChanges ) {
        Toolkit.getDefaultToolkit().removeAWTEventListener( this );
        TopComponent.getRegistry().removePropertyChangeListener( this );
        try {
            if( commitChanges ) {
                TopComponentDroppable droppable = targets.get( currentIndex );
                Point dropLocation = currentSide.getDropLocation( droppable );
                if( droppable.canDrop( draggable, dropLocation ) ) {
                    dndManager.performDrop( dndManager.getController(), droppable, draggable, dropLocation );
                }
            }
        } finally {
            dndManager.dragFinished();
            dndManager.dragFinishedEx();
            if( currentDnd == this )
                currentDnd = null;
        }
    }
    
    private boolean checkDropLocation() {
        TopComponentDroppable droppable = targets.get( currentIndex );
        DropTargetGlassPane glass = findGlassPane( droppable );
        if( null == glass )
            return false;
        Point dropLocation = currentSide.getDropLocation( droppable );
        if( !droppable.canDrop( draggable, dropLocation ) )
            return false;
        return droppable.getIndicationForLocation( dropLocation ) != null;
    }
    
    private void decrementIndex() {
        currentIndex--;
        if( currentIndex < 0 )
            currentIndex = targets.size()-1;
    }
    
    private void incrementIndex() {
        currentIndex++;
        if( currentIndex > targets.size()-1 )
            currentIndex = 0;
    }
    
    private boolean isCurrentDroppableFloating() {
        TopComponentDroppable droppable = targets.get( currentIndex );
        return isDroppableFloating( droppable );
    }
    
    private static boolean isDroppableFloating( TopComponentDroppable droppable ) {
        return droppable instanceof JDialog;
    }
    
    private class ComponentSide {
        private final Side side;

        public ComponentSide( Side side ) {
            this.side = side;
        }
        
        ComponentSide moveLeft() {
            if( isCurrentDroppableFloating() ) {
                decrementIndex();
                return new ComponentSide( Side.center );
            }
            switch( side ) {
                case right:
                    return new ComponentSide( Side.center );
                case left:
                    decrementIndex();
                    return new ComponentSide( Side.right );
            }
            return new ComponentSide( Side.left );
        }
        
        ComponentSide moveRight() {
            if( isCurrentDroppableFloating() ) {
                incrementIndex();
                return new ComponentSide( Side.center );
            }
            switch( side ) {
                case right:
                    incrementIndex();
                    return new ComponentSide( Side.left );
                case left:
                    return new ComponentSide( Side.center );
            }
            return new ComponentSide( Side.right );
        }
        
        ComponentSide moveDown() {
            if( isCurrentDroppableFloating() ) {
                incrementIndex();
                return new ComponentSide( Side.center );
            }
            switch( side ) {
                case bottom:
                    incrementIndex();
                    return new ComponentSide( Side.top );
                case top:
                    return new ComponentSide( Side.center );
            }
            return new ComponentSide( Side.bottom );
        }
        
        ComponentSide moveUp() {
            if( isCurrentDroppableFloating() ) {
                decrementIndex();
                return new ComponentSide( Side.center );
            }
            switch( side ) {
                case top:
                    decrementIndex();
                    return new ComponentSide( Side.top );
                case bottom:
                    return new ComponentSide( Side.center );
            }
            return new ComponentSide( Side.top );
        }
        
        Point getDropLocation( TopComponentDroppable droppable ) {
            Point res = getDropLocation( droppable, 30 );
            Shape indication = droppable.getIndicationForLocation( res );
            if( null == indication )
                res = getDropLocation( droppable, 5 );
            return res;
        }
        
        Point getDropLocation( TopComponentDroppable droppable, int dropMargin ) {
            Dimension size = droppable.getDropComponent().getSize();
            Point res = new Point();
            switch( side ) {
                case center:
                    res.x = size.width/2;
                    res.y = size.height/2;
                    break;
                case top:
                    res.x = size.width/2;
                    res.y = dropMargin;
                    break;
                case bottom:
                    res.x = size.width/2;
                    res.y = size.height-dropMargin;
                    break;
                case left:
                    res.x = dropMargin;
                    res.y = size.height/2;
                    break;
                case right:
                    res.x = size.width-dropMargin;
                    res.y = size.height/2;
                    break;
            }
            return res;
        }
    }
    
    private enum Side {
        left,
        right,
        top,
        bottom,
        center
    }
}
