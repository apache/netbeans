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


import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.view.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.geom.AffineTransform;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Set;


/**
 * Glass pane which is used for <code>DefaultContainerImpl</code>
 * as a component associated with <code>DropTarget</code> to be able
 * to paint 'drag under' indications for that container. 
 *
 *
 * @author  Peter Zavadsky
 *
 * @see java.awt.dnd.DropTarget
 * @see org.netbeans.core.windows.DefaultContainerImpl
 */
public final class DropTargetGlassPane extends JPanel implements DropTargetListener {

    // XXX PENDING
    private final Observer observer;
    // XXX PENDING
    private final Informer informer;
    
    private final WindowDnDManager windowDragAndDrop;
    
    /** Current location of cursor in over the glass pane,
     * or <code>null</code> in the case there it is not above
     * this component currently. */
    private Point location;
    
    /** <code>TopComponentDroppable</code> used in paint to get indication
     * rectangle. */
    private TopComponentDroppable droppable;

    private Reference<Autoscroll> lastAutoscroll = null;
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(DropTargetGlassPane.class);

    

    /** Creates non initialized <code>DropTargetGlassPane</code>. */
    public DropTargetGlassPane(WindowDnDManager wdnd) {
        this.observer = wdnd;
        this.informer = wdnd;
        windowDragAndDrop = wdnd;
        
        setOpaque(false);
    }

    
    /** Called when started drag operation, to save the old visibility state. */
    public void initialize() {
        if(isVisible()) {
            // For unselected internal frame the visibility could
            // be already set, but due to a bug is needed to revalidate it.
            revalidate();
        } else {
            setVisible(true);
        }
    }

    /** Called when finished drag operation, to reset the old visibility state. */
    public void uninitialize() {
        if(location != null) {
            // #22123. Not removed drop inidication.
            dragFinished();
        }

        setVisible(false);
        stopAutoscroll();
    }
    
    /** Called when the drag operation performed over this drop target. */
    void dragOver(Point location, TopComponentDroppable droppable) {
        this.droppable = droppable;
        setDragLocation (location);
        autoscroll( droppable, location );
    }
    
    
    private Point dragLocation = null;
    private void setDragLocation (Point p) {
        Point old = dragLocation;
        dragLocation = p;
        if (p != null && p.equals(old)) {
            return;
        } else if (p == null) {
            //XXX clear?
            return;
        }
        //#234429 - make sure we're still visible - reseting the global wait cursor hides the glass pane
        setVisible( true );
        
        if (droppable != null) {
            Rectangle repaintRectangle = null;
            if( null != currentDropIndication ) {
                repaintRectangle = currentDropIndication.getBounds();
                repaintRectangle = SwingUtilities.convertRectangle(componentUnderCursor, repaintRectangle, this );
                
                if( null != currentPainter ) {
                    Rectangle rect = currentPainter.getPaintArea();
                    if( null != rect )
                        repaintRectangle.add(rect);
                }
            }
            Component c = droppable.getDropComponent();
            
            Shape s = droppable.getIndicationForLocation (
                SwingUtilities.convertPoint(this, p, c));
            if( null != s && s.equals( currentDropIndication ) ) {
                return;
            }
            
            if (droppable instanceof EnhancedDragPainter edp) {
                currentPainter = edp;
            } else {
                currentPainter = null;
            }
            currentDropIndication = s;
            componentUnderCursor = c; 
            if( null != currentDropIndication ) {
                Rectangle rect = currentDropIndication.getBounds();
                rect = SwingUtilities.convertRectangle(c, rect, this );
                if( null != repaintRectangle )
                    repaintRectangle.add( rect );
                else
                    repaintRectangle = rect;
                
                if( null != currentPainter ) {
                    rect = currentPainter.getPaintArea();
                    if( null != rect )
                        repaintRectangle.add( rect );
                }
            }
            if( null != repaintRectangle ) {
                repaintRectangle.grow(2, 2);
                repaint( repaintRectangle );
            }
        } else {
            if( null != currentDropIndication ) {
                Rectangle repaintRect = currentDropIndication.getBounds();
                currentDropIndication = null;
                if( null != currentPainter ) {
                    Rectangle rect = currentPainter.getPaintArea();
                    if( null != rect )
                        repaintRect = repaintRect.union( rect );
                    currentPainter = null;
                }
                repaint( repaintRect );
            }
        }
        
    }
    

    
    /** Called when the drag operation exited from this drop target. */
    private void dragExited() {
        clear();
    }
    
    /** Hacks the problem when exiting of drop target, sometimes the framework
     * "forgets" to send drag exit event (when moved from the drop target too
     * quickly??) thus the indication rectangle remains visible. Used to fix
     * this problem. */
    public void clearIndications() {
        currentDropIndication = null;
        currentPainter = null;
        componentUnderCursor = null;
        repaint();
        clear();
    }

    /** Called when changed drag action. */
    private void dragActionChanged(Point location) {
        setDragLocation(location);
    }

    /** Called when drag operation finished. */
    private void dragFinished() {
        clear();
    }
    
    /** Clears glass pane. */
    private void clear() {
        stopAutoscroll();
        this.droppable = null;
        
        setDragLocation(null);
    }

    private Shape currentDropIndication;
    private EnhancedDragPainter currentPainter;
    private Component componentUnderCursor;

    @Override
    public void paint(Graphics g) {
        if( null != currentDropIndication ) {
            Graphics2D g2d = (Graphics2D)g.create();
            
            if( null != currentPainter )
                currentPainter.additionalDragPaint(g2d);
            
            Color c = UIManager.getColor("Panel.dropTargetGlassPane");
            if (c == null) {
                c = Color.red;
            }
            g2d.setColor(c);        	
	
            Point p = new Point (0,0);

            p = SwingUtilities.convertPoint(componentUnderCursor, p, 
                this);
            AffineTransform at = AffineTransform.getTranslateInstance(p.x, p.y);
            g2d.transform(at);
            
            g2d.setStroke(getIndicationStroke());
            g2d.setPaint(getIndicationPaint());
            Color fillColor = Constants.SWITCH_DROP_INDICATION_FADE ? FILL_COLOR : null; 
            g2d.draw(currentDropIndication);
            if( null != fillColor )
                g2d.fill( currentDropIndication );
            g2d.dispose();
        }
    }

    private TexturePaint texturePaint;
    private int modeKind = -1;
    private TexturePaint getIndicationPaint() {
        if (droppable != null && droppable.getKind() != modeKind) {
            BufferedImage image = new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            Color c = UIManager.getColor("Panel.dropTargetGlassPane");
            boolean isModeMixing = (droppable.getKind() == Constants.MODE_KIND_EDITOR
                        && windowDragAndDrop.getStartingTransfer().getKind() != Constants.MODE_KIND_EDITOR) ||
                        (droppable.getKind() != Constants.MODE_KIND_EDITOR
                        && windowDragAndDrop.getStartingTransfer().getKind() == Constants.MODE_KIND_EDITOR);
            if (c == null) {
                c = new Color(255, 90, 0);
            }
            if( isModeMixing ) {
                g2.setColor(c);
                g2.fillRect(0,0,1,1);
                g2.fillRect(1,1,1,1);
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
                g2.fillRect(1,0,1,1);
                g2.fillRect(0,1,1,1);
            } else {
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200));
                g2.fillRect( 0, 0, 2, 2);
            }
            texturePaint = new TexturePaint(image, new Rectangle(0,0,2,2));
            modeKind = droppable.getKind();
        }
        return texturePaint;
    }
    
    private Stroke stroke;
    private Stroke getIndicationStroke() {
        if (stroke == null) {
            int strokeWidth = UIManager.getInt("Panel.dropTargetGlassPane.strokeWidth");
            strokeWidth = strokeWidth < 1 ? 3 : strokeWidth;
            stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f);
        }
        return stroke;
    }        
    
    // PENDING Take the color from UI Defaults
    private static final Color FILL_COLOR = new Color( 200, 200, 200, 120 );
    
    
    // >> DropTargetListener implementation >>
    /** Implements <code>DropTargetListener</code> method.
     * accepts/rejects the drag operation if move or copy operation
     * is specified. */
    @Override
    public void dragEnter(DropTargetDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragEnter"); // NO18N
        }
        
        int dropAction = evt.getDropAction();
        // Mask action NONE to MOVE one.
        if(dropAction == DnDConstants.ACTION_NONE) {
            dropAction = DnDConstants.ACTION_MOVE;
        }
        
        if((dropAction & DnDConstants.ACTION_COPY_OR_MOVE) > 0) {
            evt.acceptDrag(dropAction);
        } else {
            evt.rejectDrag();
        }
    }

    /** Implements <code>DropTargetListener</code> method.
     * Unsets the glass pane to show 'drag under' gestures. */
    @Override
    public void dragExit(DropTargetEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragExit"); // NO18N
        }
        
        Component c = evt.getDropTargetContext().getComponent();
        if(c == this) {
            this.dragExited();
            stopAutoscroll();
        }
    }
    
    /** Implements <code>DropTargetListener</code> method.
     * Informs the glass pane about the location of dragged cursor above
     * the component. */
    @Override
    public void dragOver(DropTargetDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragOver"); // NOI18N
        }
        
        // XXX Eliminate bug, see dragExitedHack.
        observer.setLastDropTarget(this);
    }

    void autoscroll( TopComponentDroppable droppable, Point location ) {
        Component c = droppable.getDropComponent();
        location = SwingUtilities.convertPoint( this, location, c );
        Component child = SwingUtilities.getDeepestComponentAt( c, location.x, location.y );
        Autoscroll as;
        if( child instanceof Autoscroll ) {
            as = ( Autoscroll ) child;
        } else {
            as = ( Autoscroll ) SwingUtilities.getAncestorOfClass( Autoscroll.class, child );
        }
        Autoscroll prev = null == lastAutoscroll ? null : lastAutoscroll.get();
        if( null != prev && prev != as ) {
            prev.autoscroll( new Point(Integer.MIN_VALUE, Integer.MIN_VALUE) );
        }
        if( as != null ) {
            as.autoscroll( location );
            lastAutoscroll = new WeakReference<>(as);
        } else {
            lastAutoscroll = null;
        }
    }

    void stopAutoscroll() {
        Autoscroll as = null == lastAutoscroll ? null : lastAutoscroll.get();
        lastAutoscroll = null;
        if( as != null ) {
            as.autoscroll( new Point(Integer.MIN_VALUE, Integer.MIN_VALUE) );
        }
    }

    /** Implements <code>DropTargetListener</code> method.
     * When changed the drag action accepts/rejects the drag operation
     * appropriately */
    @Override
    public void dropActionChanged(DropTargetDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dropActionChanged"); // NOI18N
        }
        
        int dropAction = evt.getDropAction();
        boolean acceptDrag;
        
        if((dropAction == DnDConstants.ACTION_MOVE)
        || (dropAction == DnDConstants.ACTION_COPY
            && informer.isCopyOperationPossible())) {
                
            acceptDrag = true;
        } else {
            acceptDrag = false;
        }

        if(acceptDrag) {
            evt.acceptDrag(dropAction);
        } else {
            evt.rejectDrag();
        }
        
        Component c = evt.getDropTargetContext().getComponent();
        if(c == this) {
            this.dragActionChanged(acceptDrag ? evt.getLocation() : null);
        }
    }

    /** Implements <code>DropTargetListener</code> method. 
     * Performs the actual drop operation. */
    @Override
    public void drop(DropTargetDropEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("drop"); // NOI18N
        }
        
        // Inform glass pane about finished drag operation.
        Component c = evt.getDropTargetContext().getComponent();
        if(c == this) {
            this.dragFinished();
        }

        int dropAction = evt.getDropAction();
        if(dropAction != DnDConstants.ACTION_MOVE
        && dropAction != DnDConstants.ACTION_COPY) {
            // Not supported dnd operation.
            evt.rejectDrop();
            return;
        }
        
        // Accepts drop operation.
        evt.acceptDrop(dropAction);
        
        boolean success = false;

        try {
            Point loc = evt.getLocation();
            // Checks whetger it is in around center panel area.
            // In that case the drop will be tried later.
            // PENDING unify it.
            SwingUtilities.convertPointToScreen(loc, c);
            if(WindowDnDManager.isAroundCenterPanel(loc)) {
                return;
            }

            success = windowDragAndDrop.tryPerformDrop(
                    informer.getController(), informer.getFloatingFrames(),
                    loc, dropAction, evt.getTransferable());
        } finally {
            // Complete the drop operation.
            // XXX #21917.
            observer.setDropSuccess(success);
            evt.dropComplete(false);
            //evt.dropComplete(success);
            SwingUtilities.invokeLater(() -> {
                windowDragAndDrop.dragFinished();
                windowDragAndDrop.dragFinishedEx();
            });
        }
    }
    // >> DropTargetListener implementation >>



    private static void debugLog(String message) {
        Debug.log(DropTargetGlassPane.class, message);
    }
    
    
    // XXX
    /** Glass pane uses this interface to inform about changes. */
    interface Observer {
        public void setDropSuccess(boolean success);
        public void setLastDropTarget(DropTargetGlassPane glassPane);
    } // End of Observer.

    // XXX
    interface Informer {
        public boolean isCopyOperationPossible();
        public Controller getController();
        public Set<Component> getFloatingFrames();
    }
    
}
