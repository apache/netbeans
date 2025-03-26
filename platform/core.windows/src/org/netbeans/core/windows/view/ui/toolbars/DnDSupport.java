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

package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.nativeaccess.NativeWindowSystem;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;

/**
 * Drag and drop support for toolbars and toolbar buttons.
 * 
 * @author S. Aubrecht
 */
final class DnDSupport implements DragSourceListener, DragGestureListener, DropTargetListener, DragSourceMotionListener {
    
    private final ToolbarConfiguration config;

    private static final DataFlavor buttonDataFlavor = new DataFlavor( DataObject.class, "Toolbar Item" ); //NOI18N
    private static final DataFlavor actionDataFlavor = new DataFlavor( Node.class, "Action Node" ); //NOI18N
    private static final DataFlavor toolbarDataFlavor = new DataFlavor( ToolbarContainer.class, "Toolbar Container" ); //NOI18N

    private final DragSource dragSource = DragSource.getDefaultDragSource();

    private static final Cursor dragMoveCursor = DragSource.DefaultMoveDrop;
    private static final Cursor dragNoDropCursor = DragSource.DefaultMoveNoDrop;
    private Cursor dragRemoveCursor;
    private final Map<Component, DragGestureRecognizer> recognizers = new HashMap<Component, DragGestureRecognizer>();

    private boolean buttonDndAllowed = false;

    //button drag'n'drop context
    private Toolbar currentToolbar;
    private Toolbar sourceToolbar;
    private int dropTargetButtonIndex = -1;
    private int dragSourceButtonIndex = -1;
    private boolean insertBefore = true;

    //toolbar drag'n'drop context
    private ToolbarContainer sourceContainer;
    private ToolbarRow currentRow;
    private ToolbarRow sourceRow;
    private Point startingPoint;
    private Window dragWindow;
    private Image dragImage;

    private boolean isToolbarDrag;
    private boolean isButtonDrag;

    private final Logger log = Logger.getLogger(DnDSupport.class.getName());


    public DnDSupport( ToolbarConfiguration config ) {
        this.config = config;
        dragSource.addDragSourceMotionListener(this);
        dragRemoveCursor = Utilities.createCustomCursor( ToolbarPool.getDefault(), 
                ImageUtilities.loadImage( "org/netbeans/core/windows/resources/delete.gif"), "NO_ACTION_MOVE" ); //NOI18N
    }

    public void register(Component c) {
        synchronized( recognizers ) {
            DragGestureRecognizer dgr = recognizers.get( c );
            if( null == dgr ) {
                dgr = dragSource.createDefaultDragGestureRecognizer(c, DnDConstants.ACTION_MOVE, this);
                recognizers.put( c, dgr );
            }
        }
    }

    public void unregister(Component c) {
        synchronized( recognizers ) {
            DragGestureRecognizer dgr = recognizers.get(c);
            if( null != dgr ) {
                dgr.setComponent(null);
            }
            recognizers.remove( c );
        }
    }

    public void dragEnter(DragSourceDragEvent e) {
        //handled in dragMouseMoved
    }

    public void dragOver(DragSourceDragEvent e) {
        //handled in dragMouseMoved
    }

    public void dragExit(DragSourceEvent e) {
        //handled in dragMouseMoved
        if( isButtonDrag ) {
            resetDropGesture();
        }
    }

    public void dragDropEnd(DragSourceDropEvent e) {
        if( isButtonDrag ) {
            Component sourceComponent = e.getDragSourceContext().getComponent();
            if( sourceComponent instanceof JButton ) {
                ((JButton)sourceComponent).getModel().setRollover( false );
            }
            sourceComponent.repaint();
            resetDropGesture();
            if( e.getDropSuccess() == false &&
                    !isInToolbarPanel( WindowDnDManager.getLocationWorkaround(e) ) )
            {
                //TODO catch ESC key
                removeButton( e.getDragSourceContext().getTransferable() );
            }
        } else if( isToolbarDrag ) {
            Point newLocationOnScreen = null;

            boolean save = false;
            if( null != currentRow ) {
                //the cursor is above some toolbar row, we can proceed with the drop
                newLocationOnScreen = currentRow.drop();
                if( sourceRow != currentRow ) {
                    //clean up the row which we dragged from
                    sourceRow.dragSuccess();
                    config.removeEmptyRows();
                }
                save = true;
            } else if( null != sourceRow ) {
                //cursor is outside the toolbar area, abort the drop
                newLocationOnScreen = sourceRow.dragAbort();
                save = true;
            }
            if( null != dragWindow ) {
                if( null != newLocationOnScreen ) {
                    animateDragWindow(newLocationOnScreen);
                } else {
                    dragWindow.dispose();
                }
                dragWindow = null;
            }
            config.maybeRemoveLastRow();
            if( save ) {
                config.refresh();
                config.save();
            }
        }
        isButtonDrag = false;
        isToolbarDrag = false;
    }

    public void dragGestureRecognized(DragGestureEvent e) {
        Component c = e.getComponent();
        if( !(c instanceof JComponent) )
            return;

        Transferable t = null;
        try {
            final DataObject dob = (DataObject) ((JComponent) c).getClientProperty("file");
            if( dob != null && c.getParent() instanceof Toolbar && buttonDndAllowed ) {
                //dragging a toolbar button
                sourceToolbar = (Toolbar) c.getParent();
                t = new ExTransferable.Single(buttonDataFlavor) {
                    public Object getData() {
                        return dob;
                    }
                };
                isToolbarDrag = false;
                isButtonDrag = true;
                dragSourceButtonIndex = sourceToolbar.getComponentIndex(c);
            } else if( Boolean.TRUE.equals( ((JComponent) c).getClientProperty(ToolbarContainer.PROP_DRAGGER) ) ) {
                //dragging the whole toolbar
                final ToolbarContainer container = (ToolbarContainer) c.getParent().getParent();
                if( container.isShowing() ) {
                    sourceContainer = container;
                    sourceRow = (ToolbarRow) container.getParent();
                    t = new ExTransferable.Single(toolbarDataFlavor) {

                        public Object getData() {
                            return container;
                        }
                    };
                    isToolbarDrag = true;
                    isButtonDrag = false;
                    startingPoint = new Point(e.getDragOrigin());
                    Rectangle bounds = new Rectangle(sourceContainer.getPreferredSize());
                    bounds.setLocation(sourceContainer.getLocationOnScreen());
                    dragImage = createContentImage(sourceContainer, bounds.getSize());
                    sourceRow.dragStarted( sourceContainer );
                    dragWindow = createDragWindow( dragImage, bounds );
                }
            }
            if( c instanceof JButton ) {
                ((JButton) c).getModel().setArmed(false);
                ((JButton) c).getModel().setPressed(false);
                ((JButton) c).getModel().setRollover(true);
            }
            if( t != null ) {
                e.startDrag(dragMoveCursor, t, this);
            }
        } catch( InvalidDnDOperationException idoE ) {
            log.log(Level.INFO, null, idoE);
        }
    }

    public void dropActionChanged (DragSourceDragEvent e) {
        //ignore
    }

    public void drop(DropTargetDropEvent dtde) {
        boolean res = false;
        try {
            if( isButtonDrag ) {
                if( validateDropPosition() ) {
                    res = handleDrop( dtde.getTransferable() );
                }
            } else if( isToolbarDrag ) {
                res = true;
                //taken care of in dragDropEnd()
            }
        } finally {
            dtde.dropComplete(res);
        }
        resetDropGesture();
    }

    public void dragExit(DropTargetEvent dte) {
        resetDropGesture();
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        //ignore
    }

    public void dragEnter(DropTargetDragEvent e) {
        if( e.isDataFlavorSupported( buttonDataFlavor ) || e.isDataFlavorSupported( actionDataFlavor ) ) {
            e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            isButtonDrag = true; //in case use is dragging something from the customizer window
        } else if( e.isDataFlavorSupported( toolbarDataFlavor ) ) {
            e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            e.rejectDrag();
        }
    }

    public void dragOver(DropTargetDragEvent e) {
        if( e.isDataFlavorSupported( buttonDataFlavor ) || e.isDataFlavorSupported( actionDataFlavor ) ) {
            updateDropGesture( e );
            if( !validateDropPosition() ) {
                e.rejectDrag();
            } else {
                e.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
            }
        } else if( e.isDataFlavorSupported( toolbarDataFlavor ) ) {
            e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        } else {
            e.rejectDrag();
        }
    }

    public void dragMouseMoved(DragSourceDragEvent e) {
        Point location = WindowDnDManager.getLocationWorkaround(e);
        DragSourceContext context = e.getDragSourceContext();
        if( isButtonDrag ) {
            int action = e.getDropAction();
            if ((action & DnDConstants.ACTION_MOVE) != 0) {
                context.setCursor( dragMoveCursor );
            } else {
                if( isInToolbarPanel( location ) ) {
                    context.setCursor( dragNoDropCursor );
                } else {
                    context.setCursor( dragRemoveCursor );
                }
            }
        } else if( isToolbarDrag && null != dragWindow ) {
            Point p = new Point( location );
            p.x -= startingPoint.x;
            p.y -= startingPoint.y;
            dragWindow.setLocation(p);
            context.setCursor( Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) );

            ToolbarRow row = config.getToolbarRowAt( location );
            if( null == row && (sourceRow.countVisibleToolbars() > 1 || !config.isLastRow(sourceRow)) ) {
                row = config.maybeAddEmptyRow( location );
            }

            ToolbarRow oldRow = currentRow;
            currentRow = row;
            if( null != oldRow && oldRow != currentRow ) {
                oldRow.hideDropFeedback();
                config.repaint();
            }
            if( null != currentRow )
                currentRow.showDropFeedback( sourceContainer, location, dragImage );
            if( !config.isLastRow(currentRow) )
                config.maybeRemoveLastRow();
        }
    }

    void setButtonDragAndDropAllowed(boolean buttonDndAllowed) {
        this.buttonDndAllowed = buttonDndAllowed;
    }

    boolean isButtonDragAndDropAllowed() {
        return this.buttonDndAllowed;
    }

    private Window createDragWindow( Image dragImage, Rectangle bounds ) {
        Window w = new Window( SwingUtilities.windowForComponent(sourceRow) );
        w.add(new JLabel(ImageUtilities.image2Icon(dragImage)));
        w.setBounds(bounds);
        w.setVisible(true);
        NativeWindowSystem nws = NativeWindowSystem.getDefault();
        if( nws.isUndecoratedWindowAlphaSupported() ) {
            nws.setWindowAlpha(w, 0.7f);
        }
        return w;
    }

    private BufferedImage createContentImage( JComponent c, Dimension contentSize ) {
        GraphicsConfiguration cfg = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();

        boolean opaque = c.isOpaque();
        c.setOpaque(true);
        BufferedImage res = cfg.createCompatibleImage(contentSize.width, contentSize.height);
        Graphics2D g = res.createGraphics();
        g.setColor( c.getBackground() );
        g.fillRect(0, 0, contentSize.width, contentSize.height);
        g.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f ));
        c.paint(g);
        c.setOpaque(opaque);
        return res;
    }

    private static final int SLIDE_INTERVAL = 1000/30;
    private void animateDragWindow( final Point newLocationOnScreen ) {
        final javax.swing.Timer timer = new javax.swing.Timer(SLIDE_INTERVAL, null);
        final Window returningWindow = dragWindow;
        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Point location = returningWindow.getLocationOnScreen();
                Point dst = new Point(newLocationOnScreen);
                int dx = (dst.x - location.x)/2;
                int dy = (dst.y - location.y)/2;
                if (dx != 0 || dy != 0) {
                    location.translate(dx, dy);
                    returningWindow.setLocation(location);
                }
                else {
                    timer.stop();
                    returningWindow.dispose();
                }
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    private boolean isInToolbarPanel( Point p ) {
        Component c = ToolbarPool.getDefault();
        SwingUtilities.convertPointFromScreen( p, c );
        return c.contains( p );
    }

    private DataFolder getBackingFolder( Toolbar bar ) {
        Object res = bar.getClientProperty("folder"); //NOI18N
        if( res instanceof DataFolder )
            return (DataFolder) res;
        return null;
    }
    /**
     * Add a new toolbar button represented by the given DataObject.
     */
    private boolean addButton( DataObject dobj, int dropIndex, boolean dropBefore ) throws IOException {
        if( null == dobj )
            return false;
        //check if the dropped button (action) already exists in this toolbar
        String objName = dobj.getName();
        DataFolder backingFolder = getBackingFolder(currentToolbar);
        DataObject[] children = backingFolder.getChildren();
        for( int i=0; i<children.length; i++ ) {
            //TODO is comparing DataObject names ok?
            if( objName.equals( children[i].getName() ) ) {
                //user dropped to toolbat a new button that already exists in this toolbar
                //just move the existing button to a new position
                return moveButton( children[i], dropIndex, dropBefore );
            }
        }

        DataObject objUnderCursor = getDataObjectUnderDropCursor( dropIndex-1, dropBefore );

        DataShadow shadow = DataShadow.create( backingFolder, dobj );
        // use some fake position, so getChildren don't complain
        shadow.getPrimaryFile().setAttribute("position", 100001); // NOI18N

        //find the added object
        DataObject newObj = null;
        children = backingFolder.getChildren();
        for( int i=0; i<children.length; i++ ) {
            if( objName.equals( children[i].getName() ) ) {
                newObj = children[i];
                break;
            }
        }

        if( null != newObj )
            reorderButtons( newObj, objUnderCursor ); //put the button to its proper position

        return true;
    }

    /**
     * Move toolbar button to a new position.
     */
    private boolean moveButton( DataObject ob, int dropIndex, boolean dropBefore ) throws IOException {
        //find out which button is currently under the drag cursor
        DataObject objUnderCursor = getDataObjectUnderDropCursor( dropIndex-1, dropBefore );

        if( sourceToolbar != currentToolbar ) {
            //move button to the new toolbar
            ob.move(getBackingFolder(currentToolbar));
        }

        reorderButtons( ob, objUnderCursor );
        //else we're dragging a button to an empty toolbar
        return true;
    }

    private void reorderButtons( DataObject objToMove, DataObject objUnderCursor ) throws IOException {
        DataFolder backingFolder = getBackingFolder(currentToolbar);
        List<DataObject> children = new ArrayList<DataObject>( Arrays.asList( backingFolder.getChildren() ) );
        if( null == objUnderCursor ) {
            children.remove( objToMove );
            children.add( objToMove );
        } else {
            int targetIndex = children.indexOf( objUnderCursor );
            int currentIndex = children.indexOf( objToMove );
            if( currentIndex < targetIndex )
                targetIndex--;
            targetIndex = Math.max( 0, targetIndex );
            targetIndex = Math.min( children.size(), targetIndex );
            children.remove( objToMove );
            children.add( targetIndex, objToMove );
        }

        backingFolder.setOrder(children.toArray(new DataObject[0]) );
    }

    private DataObject getDataObjectUnderDropCursor( int dropIndex, boolean dropBefore ) {
        DataObject[] buttons = getBackingFolder(currentToolbar).getChildren();
        DataObject objUnderCursor = null;
        if( buttons.length > 0 ) {
            if( !dropBefore )
                dropIndex++;
            if( dropIndex < buttons.length && dropIndex >= 0 ) {
                objUnderCursor = buttons[dropIndex];
            }
        }
        return objUnderCursor;
    }

    private boolean validateDropPosition() {
       //the drag cursor cannot be positioned above toolbar's drag handle
        return dropTargetButtonIndex >= 0;
    }



    private void updateDropGesture( DropTargetDragEvent e ) {
        Point p = e.getLocation();
        Component c = e.getDropTargetContext().getComponent();
        if( c instanceof Toolbar ) {
            Toolbar bar = (Toolbar) c;
            Component button = bar.getComponentAt(p);
            int index = bar.getComponentIndex(button);
            //find out whether we want to drop before or after this component
            boolean b = p.x <= button.getLocation().x + button.getWidth() / 2;
            if( index != dropTargetButtonIndex || b != insertBefore ) {
                dropTargetButtonIndex = index;
                insertBefore = b;
            }

            if( null != currentToolbar ) {
                ToolbarContainer container = getContainer( currentToolbar );
                container.setDropGesture( -1, false );
            }
            currentToolbar = bar;
            ToolbarContainer container = getContainer( currentToolbar );
            container.setDropGesture( dropTargetButtonIndex, insertBefore );
        } else {
            resetDropGesture();
            currentToolbar = null;
        }

    }

    private void resetDropGesture() {
        dropTargetButtonIndex = -1;
        if( null != currentToolbar ) {
            ToolbarContainer container = getContainer( currentToolbar );
            container.setDropGesture( -1, false );
        }
    }

    private ToolbarContainer getContainer( Toolbar bar ) {
        return (ToolbarContainer) bar.getParent();
    }

    /**
     * Remove a toolbar button represented by the given Transferable.
     */
    private void removeButton( Transferable t ) {
        try {
            Object o = null;
            if( t.isDataFlavorSupported(buttonDataFlavor) ) {
                o = t.getTransferData(buttonDataFlavor);
            }
            if(o instanceof DataObject) {
                ((DataObject) o).delete();
                sourceToolbar.repaint();
            }
        } catch( UnsupportedFlavorException e ) {
            log.log( Level.INFO, null, e );
        } catch( IOException ioE ) {
            log.log( Level.INFO, null, ioE );
        }
    }

    /**
     * Perform the drop operation.
     *
     * @return True if the drop has been successful.
     */
    private boolean handleDrop( final Transferable t ) {
        final boolean[] res = { false };
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                res[0] = handleDropImpl(t);
            }
        });
        return res[0];
    }
    
    private boolean handleDropImpl(Transferable t) {       
        try {
            Object o;
            if( t.isDataFlavorSupported( actionDataFlavor ) ) {
                o = t.getTransferData( actionDataFlavor );
                if( o instanceof Node ) {
                    DataObject dobj = ((Node)o).getLookup().lookup( DataObject.class );
                    return addButton( dobj, dropTargetButtonIndex, insertBefore );
                }
            } else {
                o = t.getTransferData( buttonDataFlavor );
                if( o instanceof DataObject ) {
                    return moveButton( (DataObject)o, dropTargetButtonIndex, insertBefore );
                }
            }
        } catch( UnsupportedFlavorException e ) {
            log.log( Level.INFO, null, e );
        } catch( IOException ioE ) {
            log.log( Level.INFO, null, ioE );
        }
        return false;
    }
}
