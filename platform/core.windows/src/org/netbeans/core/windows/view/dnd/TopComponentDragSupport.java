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



import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.ref.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.options.WinSysPrefs;
import org.openide.util.*;
import org.openide.windows.TopComponent;



/**
 * Window system drag support for <code>TopComponent</code>'s.
 * It imitates role of drag gesture recognizer, possible
 * on any kind of <code>Component</code>, currently on <code>Tabbed</code>.
 * Starts also programatically the DnD for TopComponent in container
 * when the starting gestures are Shift+Mouse Drag or Ctrl+Mouse Drag
 * respectivelly.
 * It serves as <code>DragSourceListener</code> during the DnD in progress
 * and sets dragging cursor appopriatelly.
 *
 * <em>Note:</em> There is used only one singleton instance in window system
 * DnD available via {@link #getDefault}.
 *
 *
 * @author  Peter Zavadsky
 *
 * @see java awt.dnd.DragSourceListener
 */
final class TopComponentDragSupport 
implements AWTEventListener, DragSourceListener, DragSourceMotionListener {
    
    /** Mime type for <code>TopComponent</code> <code>DataFlavor</code>. */
    public static final String MIME_TOP_COMPONENT = 
        DataFlavor.javaJVMLocalObjectMimeType
        // Note: important is the space after semicolon, thus to match
        // when comparing.
        + "; class=org.openide.windows.TopComponent"; // NOI18N

    /** Mime type for <code>TopComponent.Cloneable</code> <code>DataFlavor</code>. */
    public static final String MIME_TOP_COMPONENT_CLONEABLE = 
        DataFlavor.javaJVMLocalObjectMimeType
        + "; class=org.openide.windows.TopComponent$Cloneable"; // NOI18N
    

    /** Mime type for <code>Mode</code> <code>DataFlavor</code>. */
    public static final String MIME_TOP_COMPONENT_MODE =
        DataFlavor.javaJVMLocalObjectMimeType
        + "; class=org.netbeans.core.windows.ModeImpl"; // NOI18N

    
    /** 'Copy window' cursor type. */
    private static final int CURSOR_COPY    = 0;
    /** 'Copy_No window' cursor type. */
    private static final int CURSOR_COPY_NO = 1;
    /** 'Move window' cursor type. */
    private static final int CURSOR_MOVE    = 2;
    /** 'Move_No window' cursor type. */
    private static final int CURSOR_MOVE_NO = 3;
    /** Cursor type indicating there cannot be copy operation 
     * done, but could be done move operation. In fact is
     * the same like {@link #CURSOR_COPY_NO} with the diff name
     * to be recognized correctly when switching action over drop target */
    private static final int CURSOR_COPY_NO_MOVE = 4;
    /** Move to free area cursor type */
    private static final int CURSOR_MOVE_FREE = 5;

    /** Name for 'Copy window' cursor. */
    private static final String NAME_CURSOR_COPY         = "CursorTopComponentCopy"; // NOI18N
    /** Name for 'Copy_No window' cursor. */
    private static final String NAME_CURSOR_COPY_NO      = "CursorTopComponentCopyNo"; // NOI18N
    /** Name for 'Move window' cursor. */
    private static final String NAME_CURSOR_MOVE         = "CursorTopComponentMove"; // NOI18N
    /** Name for 'Move_No window' cursor. */
    private static final String NAME_CURSOR_MOVE_NO      = "CursorTopComponentMoveNo"; // NOI18N
    /** */
    private static final String NAME_CURSOR_COPY_NO_MOVE = "CursorTopComponentCopyNoMove"; // NOI18N
    /** Name for cursor to drop to free area. */
    private static final String NAME_CURSOR_MOVE_FREE    = "CursorTopComponentMoveFree"; // NOI18N

    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(TopComponentDragSupport.class);
    
    private final WindowDnDManager windowDnDManager;

    /** Weak reference to <code>DragSourceContext</code> used in processed
     * drag operation. Used for by fixing bugs while not passed correct
     * order of events to <code>DragSourceListener</code>. */
    private Reference<DragSourceContext> dragContextWRef = new WeakReference<DragSourceContext>(null);
    
    /** Flag indicating the current window drag operation transferable
     * can be 'copied', i.e. the dragged <code>TopComponent</code> is
     * <code>TopComponent.Cloneable</code> instance. */
    private boolean canCopy;
    
    // #21918. There is not possible to indicate drop action in "free" desktop
    // area. This field helps to workaround the problem.
    /** Flag indicating user drop action. */
    private int hackUserDropAction;

    // #21918. Determine the ESC pressed.
    /** Flag indicating the user has cancelled drag operation by pressing ESC key. */
    private boolean hackESC;

    private Point startingPoint;
    private Component startingComponent;
    private long startingTime;

    private DragAndDropFeedbackVisualizer visualizer;
    
    private boolean dropFailed = false;
    
    /** Creates a new instance of TopComponentDragSupport. */
    TopComponentDragSupport(WindowDnDManager windowDnDManager) {
        this.windowDnDManager = windowDnDManager;
    }

    
    /** Informs whether the 'copy' operation is possible. Gets valid result
     * during processed drag operation only.
     * @return <code>true</code> if the drop copy operation is possible from
     * drag source point of view
     * @see #canCopy */
    public boolean isCopyOperationPossible() {
        return canCopy;
    }

    /** Simulates drag gesture recongition valid for winsys.
     * Implements <code>AWTEventListener</code>. */
    @Override
    public void eventDispatched(AWTEvent evt) {
        MouseEvent me = (MouseEvent) evt;
        //#118828
        if (! (evt.getSource() instanceof Component)) {
            return;
        }

        // #40736: only left mouse button drag should start DnD
        if((me.getID() == MouseEvent.MOUSE_PRESSED) && SwingUtilities.isLeftMouseButton(me)) {
                startingPoint = me.getPoint();
            startingComponent = me.getComponent();
            startingTime = me.getWhen();
        } else if(me.getID() == MouseEvent.MOUSE_RELEASED) {
            startingPoint = null;
            startingComponent = null;
        }
        if(me.isConsumed())
            return;
        if(evt.getID() != MouseEvent.MOUSE_DRAGGED) {
            return;
        }
        if(windowDnDManager.isDragging()) {
            return;
        }
        if(startingPoint == null) {
            return;
        }
        if( evt.getSource() instanceof JButton ) {
            //do not initiate topcomponent drag when the mouse is dragged out of a tabcontrol button
            return;
        }
        if(!WindowDnDManager.isDnDEnabled()) {
            return;
        }

        Component srcComp = startingComponent;
        if(srcComp == null) {
            return;
        }
        
        final Point point = new Point(startingPoint);
        Point currentPoint = me.getPoint();
        Component currentComponent = me.getComponent();
        if(currentComponent == null) {
            return;
        }
        currentPoint = SwingUtilities.convertPoint(currentComponent, currentPoint, srcComp);
        if(Math.abs(currentPoint.x - point.x) <= Constants.DRAG_GESTURE_START_DISTANCE
        && Math.abs(currentPoint.y - point.y) <= Constants.DRAG_GESTURE_START_DISTANCE) {
            return;
        }
        // time check, to prevent wild mouse clicks to be considered DnD start
        if (me.getWhen() - startingTime <= Constants.DRAG_GESTURE_START_TIME) {
            return;
        }
        startingPoint = null;
        startingComponent = null;
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("eventDispatched (MOUSE_DRAGGED)"); // NOI18N
        }
        
        // XXX Do not clash with JTree (e.g. in explorer) drag.
        if((srcComp instanceof JTree)
        && ((JTree)srcComp).getPathForLocation(me.getX(), me.getY()) != null) {
            return;  
        }
        
        // #22622: AWT listener just passivelly listnens what is happenning around,
        // and we need always the deepest component to start from.
        srcComp = SwingUtilities.getDeepestComponentAt(srcComp, point.x, point.y);

        boolean ctrlDown  = me.isControlDown();
        
        TopComponent tc = null;
        Tabbed tabbed;

        if(srcComp instanceof Tabbed.Accessor) {
            tabbed = ((Tabbed.Accessor)srcComp).getTabbed();
        } else {
            Tabbed.Accessor acc = (Tabbed.Accessor)SwingUtilities.getAncestorOfClass(Tabbed.Accessor.class, srcComp);
            tabbed = acc != null ? acc.getTabbed() : null;
        }
        if(tabbed == null) {
            return;
        }

        // #22132. If in modal dialog no drag allowed.
        Dialog dlg = (Dialog)SwingUtilities.getAncestorOfClass(Dialog.class, tabbed.getComponent());
        if(dlg != null && dlg.isModal()) {
            return; 
        }
        
        Point ppp = new Point(point);
        Point p = SwingUtilities.convertPoint(srcComp, ppp, tabbed.getComponent());
        
        TopComponentDraggable draggable = null;
        // #106761: tabForCoordinate may return -1, so check is needed
        int tabIndex = tabbed.tabForCoordinate(p);
        tc = tabIndex != -1 ? tabbed.getTopComponentAt(tabIndex) : null;
        if (tc == null) {
            Rectangle tabsArea = tabbed.getTabsArea();
            if( tabsArea.contains( p ) ) {
                TopComponent[] tcs = tabbed.getTopComponents();
                if( null != tcs && tcs.length > 0 ) {
                    ModeImpl mode = ( ModeImpl ) WindowManagerImpl.getInstance().findMode( tcs[0] );
                    if( null != mode && ((mode.getKind() == Constants.MODE_KIND_EDITOR && Switches.isEditorModeDragAndDropEnabled())
                                            ||
                                        (mode.getKind() == Constants.MODE_KIND_VIEW && Switches.isViewModeDragAndDropEnabled())) ) {
                        draggable = new TopComponentDraggable( mode );
                    }
                }
            }
        } else {
            if( Switches.isTopComponentDragAndDropEnabled() && Switches.isDraggingEnabled(tc) ) {
                ModeImpl mode = ( ModeImpl ) WindowManagerImpl.getInstance().findMode( tc );
                if( null != mode )
                    draggable = new TopComponentDraggable( tc );
            }
        }

        if( null == draggable )
            return;

        // #21918. See above.
        if (ctrlDown) {
            hackUserDropAction = DnDConstants.ACTION_COPY;
        }
        else {
            hackUserDropAction = DnDConstants.ACTION_MOVE;
        }
                 

        List<MouseEvent> list = new ArrayList<MouseEvent>();
        list.add(me);

        // Get start droppable (if there) and its starting point.
        TopComponentDroppable startDroppable = (TopComponentDroppable)SwingUtilities
                            .getAncestorOfClass(TopComponentDroppable.class, null == tc ? tabbed.getComponent() : tc);
        Point startPoint;
        if (startDroppable == null && tc != null) {
            startDroppable = (TopComponentDroppable)SwingUtilities
                                .getAncestorOfClass(TopComponentDroppable.class, tabbed.getComponent());
        }
        if(startDroppable != null) {
            startPoint = point;
            Point pp = new Point(point);
            startPoint = SwingUtilities.convertPoint(srcComp, pp, (Component)startDroppable);
        } else {
            startPoint = null;
        }
        //dragSource.startDrag(event, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) ,image , new Point(-offX, -offY),text, this);

        doStartDrag(
            srcComp,
            draggable, 
            new DragGestureEvent(
                new FakeDragGestureRecognizer(windowDnDManager, me),
                hackUserDropAction,
                point,
                list
            ),
            startDroppable,
            startPoint
        );
    }
    
    /** Actually starts the drag operation. */
    private void doStartDrag(Component startingComp, TopComponentDraggable transfer, DragGestureEvent evt,
    TopComponentDroppable startingDroppable, final Point startingPoint) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("doStartDrag"); // NOI18N
        }
        
        TopComponent tc = transfer.getTopComponent();
        canCopy = tc instanceof TopComponent.Cloneable
                && !Boolean.TRUE.equals( tc.getClientProperty( TopComponent.PROP_DND_COPY_DISABLED ) );
        
        // Inform window sys there is DnD about to start.
        // XXX Using the firstTC in DnD manager is a hack.
        windowDnDManager.dragStarting(startingDroppable, startingPoint, transfer);

        Cursor cursor = hackUserDropAction == DnDConstants.ACTION_MOVE
            ? getDragCursor(startingComp, CURSOR_MOVE)
            : (canCopy 
                ? getDragCursor(startingComp, CURSOR_COPY)
                : getDragCursor(startingComp, CURSOR_COPY_NO_MOVE));

        // Sets listnening for ESC key.
        addListening();
        hackESC = false;
        
        Tabbed tabbed = null;
        Tabbed.Accessor acc = (Tabbed.Accessor) SwingUtilities.getAncestorOfClass (Tabbed.Accessor.class,
                                                                                    startingComp);
        tabbed = acc != null ? acc.getTabbed() : null;
        
        int tabIndex = -1;
        Image img = createDragImage();
        if (tabbed != null) {
            if( transfer.isTopComponentTransfer()
                && WinSysPrefs.HANDLER.getBoolean(WinSysPrefs.DND_DRAGIMAGE, 
                    Utilities.getOperatingSystem() != Utilities.OS_SOLARIS) ) {
                tabIndex = tabbed.indexOf(transfer.getTopComponent());

                visualizer = new DragAndDropFeedbackVisualizer( tabbed, tabIndex );
            }
        }
        try {
            
            Transferable transferable;
            if( transfer.isTopComponentTransfer() ) {
                transferable = new TopComponentTransferable( transfer.getTopComponent() );
            } else {
                assert transfer.isModeTransfer();
                transferable = new TopComponentModeTransferable( transfer.getMode() );
            }
            evt.startDrag(
                cursor,
                img,
                new Point (0,0), 
                transferable,
                this
            );
            evt.getDragSource().addDragSourceMotionListener( this );
            
            if( null != visualizer ) {
                visualizer.start( evt );
            }
            
        } catch(InvalidDnDOperationException idoe) {
            Logger.getLogger(TopComponentDragSupport.class.getName()).log(Level.WARNING, null, idoe);
            
            removeListening();
            windowDnDManager.resetDragSource();
            if( null != visualizer ) {
                visualizer.dispose( false );
                visualizer = null;
            }
        }
    }

    private AWTEventListener keyListener = new AWTEventListener() {
        @Override
            public void eventDispatched(AWTEvent event) {
                KeyEvent keyevent = (KeyEvent)event;
                
                if ((keyevent.getID() == KeyEvent.KEY_PRESSED || keyevent.getID() == KeyEvent.KEY_RELEASED)
                        && keyevent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hackESC = true;
                }                
            }
            
        };
    /** Adds <code>KeyListener</code> to container and its component
     * hierarchy to listen for ESC key. */
    private void addListening() {
        Toolkit.getDefaultToolkit().addAWTEventListener(keyListener, AWTEvent.KEY_EVENT_MASK);
    }
    
    /** Removes ESC listening. Helper method. */
    private void removeListening() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(keyListener);
    }
    
    // >> DragSourceListener implementation >>
    /** Implements <code>DragSourceListener</code> method.
     * It just refreshes the weak reference of <code>DragSourceContext</code>
     * for the sake of setSuccessCursor method.
     * The excpected code, changing of cursor, is done in setSuccessCursor method
     * due to an undeterministic calls of this method especially in MDI mode.
     * @see #setSuccessCursor */
    @Override
    public void dragEnter(DragSourceDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragEnter");// NOI18N
        }
            
        // Just refresh the weak ref to the context if necessary.
        // The expected code here is done by ragExitHack method called from DropTarget's.
        if(dragContextWRef.get() == null) {
            dragContextWRef = new java.lang.ref.WeakReference<DragSourceContext>(evt.getDragSourceContext());
        }
    }

    /** Dummy implementation of <code>DragSourceListener</code> method. */
    @Override
    public void dragOver(DragSourceDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragOver"); // NOI18N
        }
    }

    /** Implements <code>DragSourceListener</code> method.
     * It just refreshes the weak reference of <code>DragSourceContext</code>
     * for the sake of setUnsuccessCursor method.
     * The excpected code, changing of cursor, is done in setUnsuccessCursor method
     * due to an undeterministic calls of this method especially in MDI mode.
     * @see #setUnsuccessCursor */
    @Override
    public void dragExit(DragSourceEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragExit"); // NOI18N
        }
        
        // Just refresh the weak ref to the context if necessary.
        // The expected code here is done by ragExitHack method called from DropTarget's.
        if(dragContextWRef.get() == null) {
              dragContextWRef = new WeakReference<DragSourceContext>(evt.getDragSourceContext());
        }
    }
    
    /** Implements <code>DragSourceListener</code> method.
     * It changes the cursor type from copy to move and bakc accordting the
     * user action. */
    @Override
    public void dropActionChanged(DragSourceDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dropActionChanged"); // NOI18N
        }
        String name = evt.getDragSourceContext().getCursor().getName();
        
        if(name == null) {
            // Not our cursor??
            return;
        }

        // For us is the user action important.
        int userAction = evt.getUserAction();

        // Consider NONE action as MOVE one.
        if(userAction == DnDConstants.ACTION_NONE) {
            userAction = DnDConstants.ACTION_MOVE;
        }
        // #21918. See above.
        hackUserDropAction = userAction;
        
        int type;
        if((NAME_CURSOR_COPY.equals(name)
        || NAME_CURSOR_COPY_NO_MOVE.equals(name))
        && userAction == DnDConstants.ACTION_MOVE) {
            type = CURSOR_MOVE;
        } else if(NAME_CURSOR_COPY_NO.equals(name)
        && userAction == DnDConstants.ACTION_MOVE) {
            type = CURSOR_MOVE_NO;
        } else if(NAME_CURSOR_MOVE.equals(name)
        && userAction == DnDConstants.ACTION_COPY) {
            type = CURSOR_COPY;
        } else if(NAME_CURSOR_MOVE_NO.equals(name)
        && userAction == DnDConstants.ACTION_COPY) {
            type = CURSOR_COPY_NO;
        } else {
            return;
        }

        // There can't be copy operation performed,
        // transferreed TopComponent in not of TopComponent.Cloneable instance.
        if(type == CURSOR_COPY && !canCopy) {
            type = CURSOR_COPY_NO_MOVE;
        }

        // Check if there is already our cursor.
        if(getDragCursorName(type).equals(
        evt.getDragSourceContext().getCursor().getName())) {
            return;
        }
        
        evt.getDragSourceContext().setCursor(getDragCursor(evt.getDragSourceContext().getComponent(),type));
    }

    /** Implements <code>DragSourceListener</code> method.
     * Informs window dnd manager the drag operation finished.
     * @see WindowDnDManager#dragFinished */
    @Override
    public void dragDropEnd(final DragSourceDropEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragDropEnd"); // NOI18N
        }
        
        try {
            if(checkDropSuccess(evt)) {
                windowDnDManager.dragFinished();
        
                removeListening();
                return;
            }
            
            // Now simulate drop into "free" desktop area.
            final Set<Component> floatingFrames = windowDnDManager.getFloatingFrames();
            // Finally schedule the "drop" task later to be able to
            // detect if ESC was pressed.
            final Point location = WindowDnDManager.getLocationWorkaround(evt);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(createDropIntoFreeAreaTask(
                            evt, location, floatingFrames));
                }},
                350 // XXX #21918, Neccessary to skip after possible ESC key event.
            );
        } finally {
            windowDnDManager.dragFinishedEx();
        }
    }
    // << DragSourceListener implementation <<

    /** Checks whether there was a successfull drop. */
    private boolean checkDropSuccess(DragSourceDropEvent evt) {
        // XXX #21917.
        if(windowDnDManager.isDropSuccess()) {
            return true;
        }
        
        Point location = WindowDnDManager.getLocationWorkaround(evt);
        if(location == null) {
            return true;
        }
        
        if(WindowDnDManager.isInMainWindow(location)
        || windowDnDManager.isInFloatingFrame(location)
        || WindowDnDManager.isAroundCenterPanel(location)) {
            return false;
        }
//        else if(evt.getDropSuccess()) {
//            return true;
//        } // PENDING it seem it is not working correctly (at least on linux).
        return false;
    }
    
    /** Creates task which performs actual drop into "free area", i.e. it
     * creates new separated (floating) window. */
    private Runnable createDropIntoFreeAreaTask(final DragSourceDropEvent evt,
    final Point location, final Set<Component> floatingFrames) {
        final int dropAction = hackUserDropAction;
        return new Runnable() {
            @Override
            public void run() {
                removeListening();
                // XXX #21918. Don't move the check sooner
                // (before the enclosing blocks), it would be invalid.
                if(hackESC) {
                    windowDnDManager.dragFinished();
                    return;
                }

                TopComponentDraggable transfer = WindowDnDManager.extractTopComponentDraggable(
                    dropAction == DnDConstants.ACTION_COPY,
                    evt.getDragSourceContext().getTransferable()
                );
                
                // Provide actual drop into "free" desktop area.
                if(transfer != null) {
                    // XXX there is a problem if jdk dnd framework sets as drop action
                    // ACTION_NONE, there is not called drop event on DropTargetListener,
                    // even it is there.
                    // Performs hacked drop action, simulates ACTION_MOVE when
                    // system set ACTION_NONE (which we do not use).
                    boolean res = windowDnDManager.tryPerformDrop(
                        windowDnDManager.getController(),
                        floatingFrames,
                        location,
                        dropAction,
                        evt.getDragSourceContext().getTransferable());
                
                }
                windowDnDManager.dragFinished();
            }
        };
    }
    
    /** Hacks problems with <code>dragEnter</code> wrong method calls.
     * It plays its role. Sets the cursor from 'no-drop' state
     * to its 'drop' state sibling.
     * @param freeArea true when mouse pointer in free screen area
     * @param mixedDragDrop true when document and non-document top components are about to be mixed
     * @see #dragEnter */
    void setSuccessCursor (boolean freeArea, boolean mixedDragDrop) {
        int dropAction = hackUserDropAction;
        DragSourceContext ctx = dragContextWRef.get();
        
        if(ctx == null) {
            return;
        }
        
        if( null != visualizer )
            visualizer.setDropFeedback( true, mixedDragDrop );

        dropFailed = false;

//        int type;
//        if(dropAction == DnDConstants.ACTION_MOVE) {
//            type = freeArea ? CURSOR_MOVE_FREE : CURSOR_MOVE;
//        } else if(dropAction == DnDConstants.ACTION_COPY) {
//            if(canCopy) {
//                type = CURSOR_COPY;
//            } else {
//                type = CURSOR_COPY_NO_MOVE;
//            }
//        } else {
//            // PENDING throw exception?
//            Logger.getLogger(TopComponentDragSupport.class.getName()).log(Level.WARNING, null,
//                              new java.lang.IllegalStateException("Invalid action type->" +
//                                                                  dropAction)); // NOI18N
//            return;
//        }
//
//        // Check if there is already our cursor.
//        if(getDragCursorName(type).equals(ctx.getCursor().getName())) {
//            return;
//        }
//
//        ctx.setCursor(getDragCursor(ctx.getComponent(),type));
    }
    
    /** Hacks problems with <code>dragExit</code> wrong method calls.
     * It plays its role. Sets the cursor from 'drop' state
     * to its 'no-drop' state sibling.
     * @param mixedDragDrop true when document and non-document top components are about to be mixed
     * @see #dragExit */
    void setUnsuccessCursor(boolean mixedDragDrop) {
        DragSourceContext ctx = dragContextWRef.get();
        
        if(ctx == null) {
            return;
        }
        
        if( null != visualizer )
            visualizer.setDropFeedback( false, mixedDragDrop );
        
        String name = ctx.getCursor().getName();
        
        dropFailed = true;

//        int type;
//        if(NAME_CURSOR_COPY.equals(name)
//        || NAME_CURSOR_COPY_NO_MOVE.equals(name)) {
//            type = CURSOR_COPY_NO;
//        } else if(NAME_CURSOR_MOVE.equals(name) || NAME_CURSOR_MOVE_NO.equals(name)) {
//            type = CURSOR_MOVE_NO;
//        } else {
//            return;
//        }
//
//        ctx.setCursor(getDragCursor(ctx.getComponent(),type));
    }

    /** Provides cleanup when finished drag operation. Ideally the code
     * should reside in {@ling #dragDropEnd} method only. But that one
     * is not called in case of error in DnD framework. */
    void dragFinished() {
        dragContextWRef = new WeakReference<DragSourceContext>(null);
        if( null != visualizer ) {
            visualizer.dispose( !(dropFailed || hackESC)  );
            dropFailed = false;
            visualizer = null;
        }
    }
   
    private static void debugLog(String message) {
        Debug.log(TopComponentDragSupport.class, message);
    }
    
    // Helpers>>
    /** Gets window drag <code>Cursor</code> of specified type. Utility method.
     * @param type valid one of {@link #CURSOR_COPY}, {@link #CURSOR_COPY_NO}, 
     *             {@link #CURSOR_MOVE}, {@link #CURSOR_MOVE_NO}, {@link #CURSOR_MOVE_FREE} */
    private static String getDragCursorName(int type) {
        if(type == CURSOR_COPY) {
            return NAME_CURSOR_COPY;
        } else if(type == CURSOR_COPY_NO) {
            return NAME_CURSOR_COPY_NO;
        } else if(type == CURSOR_MOVE) {
            return NAME_CURSOR_MOVE;
        } else if(type == CURSOR_MOVE_NO) {
            return NAME_CURSOR_MOVE_NO;
        } else if(type == CURSOR_COPY_NO_MOVE) {
            return NAME_CURSOR_COPY_NO_MOVE;
        } else if(type == CURSOR_MOVE_FREE) {
            return NAME_CURSOR_MOVE_FREE;
        } else {
            return null;
        }
    }
    
    /** Gets window drag <code>Cursor</code> of specified type. Utility method.
     * @param type valid one of {@link #CURSOR_COPY}, {@link #CURSOR_COPY_NO}, 
     *             {@link #CURSOR_MOVE}, {@link #CURSOR_MOVE_NO}, {@link #CURSOR_MOVE_FREE}
     * @exception IllegalArgumentException if invalid type parameter passed in */
    private static Cursor getDragCursor( Component comp, int type ) {
        Image image = null;
        String name = null;
        
        if(type == CURSOR_COPY) {
            image = ImageUtilities.loadImage(
                "org/netbeans/core/resources/topComponentDragCopy.gif"); // NOI18N
            name = NAME_CURSOR_COPY;
        } else if(type == CURSOR_COPY_NO) {
            image = ImageUtilities.loadImage(
                "org/netbeans/core/resources/topComponentDragCopyNo.gif"); // NOI18N
            name = NAME_CURSOR_COPY_NO;
        } else if(type == CURSOR_MOVE) {
            image = ImageUtilities.loadImage(
                "org/netbeans/core/resources/topComponentDragMove.gif"); // NOI18N
            name = NAME_CURSOR_MOVE;
        } else if(type == CURSOR_MOVE_NO) {
            image = ImageUtilities.loadImage(
                "org/netbeans/core/resources/topComponentDragMoveNo.gif"); // NOI18N
            name = NAME_CURSOR_MOVE_NO;
        } else if(type == CURSOR_COPY_NO_MOVE) {
            image = ImageUtilities.loadImage(
                "org/netbeans/core/resources/topComponentDragCopyNo.gif"); // NOI18N
            name = NAME_CURSOR_COPY_NO_MOVE;
        } else if(type == CURSOR_MOVE_FREE) {
            image = ImageUtilities.loadImage(
                "org/netbeans/core/windows/resources/topComponentDragMoveFreeArea.gif"); // NOI18N
            name = NAME_CURSOR_MOVE_FREE;
        } else {
            throw new IllegalArgumentException("Unknown cursor type=" + type); // NOI18N
        }
        
        return Utilities.createCustomCursor( comp, image, name );
    }

    // Helpers<<
    
    /** <code>Transferable</code> used for <code>TopComponent</code> instances
     * to be used in window system DnD. */
    private static class TopComponentTransferable extends Object
    implements Transferable {

        // #86564: Hold TopComponent weakly to workaround AWT bug #6555816
        /** <code>TopComponent</code> to be transferred. */
        private WeakReference<TopComponent> weakTC;

        
        /** Crates <code>Transferable</code> for specified <code>TopComponent</code> */
        public TopComponentTransferable(TopComponent tc) {
            this.weakTC = new WeakReference<TopComponent>(tc);
        }

        
        // >> Transferable implementation >>
        /** Implements <code>Transferable</code> method.
         * @return <code>TopComponent</code> instance for <code>DataFlavor</code>
         * with mimetype equal to {@link #MIME_TOP_COMPONENT} or if mimetype
         * equals to {@link #MIME_CLONEABLE_TOP_COMPONENT} and the top component
         * is instance of <code>TopComponent.Cloneable</code> returns the instance */
        @Override
        public Object getTransferData(DataFlavor df) {
            TopComponent tc = weakTC.get();
            if(MIME_TOP_COMPONENT.equals(df.getMimeType())) {
                return tc;
            } else if(MIME_TOP_COMPONENT_CLONEABLE.equals(
                df.getMimeType())
            && tc instanceof TopComponent.Cloneable) {
                return tc;
            }

            return null;
        }

        /** Implements <code>Transferable</code> method.
         * @return Array of <code>DataFlavor</code> with mimetype
         * {@link #MIME_TOP_COMPONENT} and also with mimetype
         * {@link #MIME_CLONEABLE_TOP_COMPONENT}
         * if the <code>tc</code> is instance
         * of <code>TopComponent.Cloneable</code> */
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            try {
                TopComponent tc = weakTC.get();
                if(tc instanceof TopComponent.Cloneable) {
                    return new DataFlavor[]{
                        new DataFlavor(MIME_TOP_COMPONENT, null, TopComponent.class.getClassLoader()),
                        new DataFlavor(MIME_TOP_COMPONENT_CLONEABLE, null, TopComponent.Cloneable.class.getClassLoader())};
                } else {
                    return new DataFlavor[] {
                        new DataFlavor(MIME_TOP_COMPONENT, null, TopComponent.class.getClassLoader())
                    };
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TopComponentDragSupport.class.getName()).log(
                        Level.WARNING, ex.getMessage(), ex);
            }
            return new DataFlavor[0];
        }

        /** Implements <code>Transferable</code> method.
         * @return <code>true</code> for <code>DataFlavor</code> with mimetype
         * equal to {@link #MIME_TOP_COMPONENT}
         * and if <code>tc</code> is instance
         * of <code>TopComponent.Cloneable</code> also for the one
         * with mimetype {@link #MIME_TOP_COMPONENT_CLONEABLE},
         * <code>false</code> otherwise */
        @Override
        public boolean isDataFlavorSupported(DataFlavor df) {
            TopComponent tc = weakTC.get();
            if(MIME_TOP_COMPONENT.equals(df.getMimeType())) {
                return true;
            } else if(MIME_TOP_COMPONENT_CLONEABLE.equals(
                df.getMimeType())
            && tc instanceof TopComponent.Cloneable) {
                return true;
            }

            return false;
        }
        // << Transferable implementation <<
    } // End of class TopComponentTransferable.

    /** <code>Transferable</code> used for <code>ModeImpl</code> instances
     * to be used in window system DnD. */
    private static class TopComponentModeTransferable extends Object implements Transferable {

        /** <code>ModeImpl</code> to be transferred. */
        private WeakReference<ModeImpl> weakRef;

        
        /** Crates <code>Transferable</code> for specified <code>TopComponent</code> */
        public TopComponentModeTransferable(ModeImpl mode) {
            this.weakRef = new WeakReference<ModeImpl> ( mode );
        }
        
        @Override
        public Object getTransferData(DataFlavor df) {
            if(isDataFlavorSupported( df )) {
                return weakRef.get();
            }
            return null;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            try {
                return new DataFlavor[]{new DataFlavor(MIME_TOP_COMPONENT_MODE,
                                                       null,
                                                       ModeImpl.class.getClassLoader())};
            }
            catch (ClassNotFoundException ex) {
                Logger.getLogger(TopComponentDragSupport.class.getName()).log(
                        Level.WARNING, ex.getMessage(), ex);
            }
            return new DataFlavor[0];
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor df) {
            if(MIME_TOP_COMPONENT_MODE.equals(df.getMimeType())) {
                return true;
            }

            return false;
        }
        // << Transferable implementation <<
    } // End of class TopComponentModeTransferable.

    
    /** Fake <code>DragGestureRecognizer</code> used when starting
     * DnD programatically. */
    private static class FakeDragGestureRecognizer extends DragGestureRecognizer {

        /** Constructs <code>FakeDragGestureRecpgnizer</code>.
         * @param evt trigger event */
        public FakeDragGestureRecognizer(WindowDnDManager windowDnDManager, MouseEvent evt) {
            super(windowDnDManager.getWindowDragSource(),
                (Component)evt.getSource(), DnDConstants.ACTION_COPY_OR_MOVE, null);

            appendEvent(evt);
        }

        /** Dummy implementation of superclass abstract method. */
        @Override
        public void registerListeners() {}
        /** Dummy implementation of superclass abstract method. */
        @Override
        public void unregisterListeners() {}
        
    } // End of class FakeDragGestureRecognizer

    
    @Override
    public void dragMouseMoved(DragSourceDragEvent dsde) {
        if( null != visualizer )
            visualizer.update( dsde );
    }
    
    /**
     * @return An invisible (size 1x1) image to be used for dragging to replace 
     * the default one supplied by the operating system (if any).
     */
    private Image createDragImage() {
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();

        BufferedImage res = config.createCompatibleImage(1, 1);
        Graphics2D g = res.createGraphics();
        g.setColor( Color.white );
        g.fillRect(0,0,1,1);
        return res;
    }
}
