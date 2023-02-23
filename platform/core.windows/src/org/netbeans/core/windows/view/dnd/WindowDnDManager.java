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



import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.IOException;
import java.lang.ref.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.*;
import org.netbeans.core.windows.view.ui.*;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;


/**
 * Manager for window DnD. Manages notifying all opened
 * <code>ModeContainer</code>'s to be notified about starting
 * and finished window drag operation.
 *
 *
 * @author  Peter Zavadsky
 *
 * @see TopComponentDragSupport
 * @see DropTargetGlassPane
 */
public final class WindowDnDManager
implements DropTargetGlassPane.Observer, DropTargetGlassPane.Informer {

    
    private final TopComponentDragSupport topComponentDragSupport = new TopComponentDragSupport(this);

    /** Only instance of drag source used for window system DnD. */
    private DragSource windowDragSource;

    /** Flag indicating the drag operation is in progress. */
    private boolean dragging;
    
    // XXX #21917. The flag is not correct in jdk dnd framework,
    // this field is used to workaround the problem. Be aware is 
    // valisd only for DnD of window component.
    /** Flag keeping info about last drop operation. */
    private boolean dropSuccess;

    /** Maps root panes to original glass panes. */
    private final Map<JRootPane,Component> root2glass = new HashMap<JRootPane, Component>();
    
    /** Set of floating frame types, i.e. separate windows. */
    private final Set<Component> floatingFrames = new WeakSet<Component>(4);
    
    /** Used to hack the last Drop target to clear its indication. */ 
    private Reference<DropTargetGlassPane> lastTargetWRef = new WeakReference<DropTargetGlassPane>(null);

    /** Accesses view. */
    private final ViewAccessor viewAccessor;
    
    // Helpers
    private TopComponentDroppable startingDroppable;
    private Point startingPoint;
    // TopComponent being dragged or null if the whole Mode is being dragged
    private TopComponentDraggable startingTransfer;

    /** drag feedback handler, listen to the mouse pointer motion during the drag */
    private MotionListener motionListener;

    /** Keeps ref to fake center panel droppable. */
    private static Reference<CenterPanelDroppable> centerDropWRef = 
            new WeakReference<CenterPanelDroppable>(null);

    /** Keeps ref to fake editor area droppable. */
    private static Reference<EditorAreaDroppable> editorDropWRef = 
            new WeakReference<EditorAreaDroppable>(null);
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(WindowDnDManager.class);

    
    /** Creates a new instance of <code>WindowsDnDManager</code>. */
    public WindowDnDManager(ViewAccessor viewAccessor) {
        this.viewAccessor = viewAccessor;
        
        // PENDING Be aware it is added only once.
        Toolkit.getDefaultToolkit().addAWTEventListener(
            topComponentDragSupport,
             AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
        );
    }

    /**
     * Get the location of a {@link DragSourceEvent}, incorporating a workaround for a JDK bug on
     * HiDPI screens on Windows. See NETBEANS-2954. This method should be called only while the
     * mouse pointer is still likely to be in the same position as it was when the event was
     * originally created.
     */
    public static Point getLocationWorkaround(DragSourceEvent evt) {
        Point ret = evt.getLocation();
        if (Utilities.isWindows() && ret != null) {
            /* Workaround for JDK bug where DragSourceEvent.getLocation() returns incorrect screen
            coordinates for displays with HiDPI scaling on Windows. Use MouseInfo.getPointerInfo
            instead; that one handles HiDPI displays correctly. In the JDK codebase, the bug can be
            seen by comparing the correct implementation of MouseInfo.getPointerInfo at

              java.desktop/windows/native/libawt/windows/MouseInfo.cpp
              (see Java_sun_awt_windows_WMouseInfoPeer_fillPointWithCoords )

            with the function AwtDragSource::GiveFeedback, which initiates the creation of
            DragSourceEvent objects, in

              java.desktop/windows/native/libawt/windows/awt_DnDDS.cpp
              (see GetCursorPos, call_dSCenter, and call_dSCmotion calls in
              AwtDragSource::GiveFeedback)

            In both cases the screen coordinates of the mouse pointer is retrieved using the
            "GetCursorPos" Windows API function. This one seems to return device coordinates rather
            than logical coordinates, presumably because the java.exe executable (and the NetBeans
            launcher, since NETBEANS-1227) declares itself to be fully DPI-aware. In MouseInfo.cpp,
            extra code is added to convert the device coordinates to logical coordinates based on
            the DPI scaling level. This is not done in awt_DnDDS.cpp, however. */
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            if (pointerInfo != null) {
                ret = pointerInfo.getLocation();
            }
        }
        return ret;
    }
    
    /** Indicates whether the window drag and drop is enabled. */
    public static boolean isDnDEnabled() {
        return !Constants.SWITCH_DND_DISABLE 
                && (Switches.isTopComponentDragAndDropEnabled() 
                    || Switches.isEditorModeDragAndDropEnabled()
                    || Switches.isViewModeDragAndDropEnabled());
    }

    /** Gets the only current instance of <code>DragSource</code> used in 
     * window system DnD. */
    public synchronized DragSource getWindowDragSource() {
        if(windowDragSource == null) {
            windowDragSource = new DragSource();
            windowDragSource.addDragSourceMotionListener(getMotionListener());
        }
        return windowDragSource;
    }

    /** Accessor for mouse motion listener */
    MotionListener getMotionListener () {
        if (motionListener == null) {
            motionListener = new MotionListener(this, topComponentDragSupport);
        }
        return motionListener;
    }

    /** Indicates whether the drag is in progress or not. */
    public boolean isDragging() {
        return dragging;
    } 
    
    /** Sets the <code>dropSuccess</code> flag. */
    @Override
    public void setDropSuccess(boolean dropSuccess) {
        this.dropSuccess = dropSuccess;
    }
    
    /** Indicates whether the last drop operation was successful. */
    public boolean isDropSuccess() {
        return dropSuccess;
    }
    
    /**Sets the last drop target compoent over which hovered the mouse.
     * Hacking purpose only. */
    @Override
    public void setLastDropTarget(DropTargetGlassPane target) {
        if(target != lastTargetWRef.get()) {
            lastTargetWRef = new WeakReference<DropTargetGlassPane>(target);
        }
    }
    
    // XXX try out to recover the DnD,
    // currently impossible no API see #21791, no such API.
    /** Tries to reset window DnD system in case some DnD problem occured. */
    public void resetDragSource() {
        dragFinished();
    }
    
    public TopComponentDroppable getStartingDroppable() {
        return startingDroppable;
    }
    
    public Point getStartingPoint() {
        return startingPoint;
    }
    
    public TopComponentDraggable getStartingTransfer() {
        return startingTransfer;
    }

    /** Called when there is pending drag operation to be started.
     * Informs all currently opened <code>ModeContainer</code>'s implementing
     * <code>ModeContainer.DropInidicator</code> interface about
     * starting drag operation. */
    public void dragStarting(TopComponentDroppable startingDroppable, Point startingPoint,
    TopComponentDraggable startingTransfer) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragStarting"); // NOI18N
        }
        
        this.startingDroppable = startingDroppable;
        this.startingPoint = startingPoint;
        this.startingTransfer = startingTransfer;
        
        Map<JRootPane,Component> addedRoots = new HashMap<JRootPane, Component>();
        Set<Component> addedFrames = new HashSet<Component>();

        for(Component comp: viewAccessor.getModeComponents()) {
            if(comp instanceof TopComponentDroppable) {
                // Find root pane.
                JRootPane root = null;
                if(comp instanceof RootPaneContainer) {
                    root = ((RootPaneContainer)comp).getRootPane();
                } else {
                    RootPaneContainer rootContainer = (RootPaneContainer)SwingUtilities
                            .getAncestorOfClass(RootPaneContainer.class, comp);
                    if(rootContainer != null) {
                        root = rootContainer.getRootPane();
                    }
                }
                
                if(root != null) {
                    Component originalGlass = setDropTargetGlassPane(root, this);
                    if(originalGlass != null) {
                        addedRoots.put(root, originalGlass);
                    }
                }
            }
        }
        for(Component w: viewAccessor.getSeparateModeFrames()) {
            if(w != null) {
                addedFrames.add(w);
            }
        }
        
        if(!addedRoots.isEmpty()) {
            synchronized(root2glass) {
                root2glass.putAll(addedRoots);
            }
        }
        
        if(!addedFrames.isEmpty()) {
            synchronized(floatingFrames) {
                floatingFrames.addAll(addedFrames);
            }
        }
        
        dragging = true;
        dropSuccess = false;
    }

    /** Sets <code>DropTargetGlassPane<code> for specified JRootPane.
     * @return original glass pane or null if there was already set drop target
     *      glass pane */
    private static Component setDropTargetGlassPane(
    JRootPane rootPane, WindowDnDManager windowDnDManager) {
        Component glassPane = rootPane.getGlassPane();
        if(glassPane instanceof DropTargetGlassPane) {
            // There is already our glass pane.
            return null;
        }
        
        DropTargetGlassPane dropGlass = new DropTargetGlassPane(windowDnDManager);
        // Associate with new drop target, and initialize.
        new DropTarget(
            dropGlass,
            DnDConstants.ACTION_COPY_OR_MOVE,
            dropGlass
        );
 
        rootPane.setGlassPane(dropGlass);
        // !!! Necessary to initialize it after setGlassPane(..),
        // i.e. the visibility state.
        dropGlass.initialize();
        
        return glassPane;
    }
    
    /** Called when there finished drag operation.
     * Informs all turned on <code>TopComponentDroppable</code>'s
     * about fininshed drag and drop. */
    public void dragFinished() {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragFinished"); // NOI18N
        }

        // include dragged separate view back
        /*Window w = SwingUtilities.getWindowAncestor(startingTransfer);
        if (w != null && !WindowManagerImpl.getInstance().getMainWindow().equals(w)) {
            ZOrderManager.getInstance().setExcludeFromOrder((RootPaneContainer)w, false);
        }*/

        // notify motion handler
        getMotionListener().dragFinished();

        // PENDING
        startingDroppable = null;
        startingPoint = null;
        startingTransfer = null;
        
        // Inform the drag support instance about finishing of the DnD.
        topComponentDragSupport.dragFinished();

        dragging = false;

        Map<JRootPane, Component> removedRoots;
        synchronized(root2glass) {
            removedRoots = new HashMap<JRootPane, Component>(root2glass);
            root2glass.clear();
        }

        for(Map.Entry<JRootPane, Component> entry: removedRoots.entrySet()) {
            setOriginalGlassPane(entry.getKey(), entry.getValue());
        }
    }
    
    /** Sets orgiginal glass pane to specified root pane. */
    private static void setOriginalGlassPane(
    JRootPane rootPane, Component originalGlass) {
        Component glass = rootPane.getGlassPane();
        
        if(glass instanceof DropTargetGlassPane) {
            DropTargetGlassPane dropGlass = (DropTargetGlassPane)glass;
            
            // Release the drop target, and unititialize.
            dropGlass.setDropTarget(null);
            dropGlass.uninitialize();
        }

        if(originalGlass != null) {
            rootPane.setGlassPane(originalGlass);
        }
        
        // #22962. Not selected JInternalFrame needs to have its glass pane
        // switched on to work properly, ensure it is so.
        JInternalFrame internalFrame = (JInternalFrame)SwingUtilities.
            getAncestorOfClass(JInternalFrame.class, originalGlass);
        if(internalFrame != null && !internalFrame.isSelected()
        && !originalGlass.isVisible()) {
            originalGlass.setVisible(true);
        }
    }
    
    // PENDING Better design of this issue, it needs to be performed
    // later than dragFinished.
    /** Maked post drag finished cleaning. Clears references to separated
     * modes */
    public void dragFinishedEx() {
        synchronized(floatingFrames) {
            floatingFrames.clear();
        }
    }    

    
    /** Gets set of floating frames. */
    @Override
    public Set<Component> getFloatingFrames() {
        synchronized(floatingFrames) {
            return new HashSet<Component>(floatingFrames);
        }
    }
    
    /** Checks whether the point is inside separated (floating) frame
     * droppable area. The point is relative to screen. */
    public boolean isInFloatingFrame(Point location) {
        for(Component w: getFloatingFrames()) {
            if(w.getBounds().contains(location)) {
                return true;
            }
        }
        
        return false;
    }


    // XXX
    @Override
    public boolean isCopyOperationPossible() {
        return topComponentDragSupport.isCopyOperationPossible();
    }
    
    @Override
    public Controller getController() {
        return viewAccessor.getController();
    }
    
    private static void debugLog(String message) {
        Debug.log(WindowDnDManager.class, message);
    }

    // Helpers>>
    /** Checks whether the point is inside main window droppable area. 
     * The point is relative to screen. */
    static boolean isInMainWindow(Point location) {
        return WindowManagerImpl.getInstance().getMainWindow().getBounds().contains(location);
    } 
    
    /** Indicates whether there is a droppable in main window, specified
     * by screen location. */
    private boolean isInMainWindowDroppable(Point location, TopComponentDraggable transfer) {
        return findMainWindowDroppable(location, transfer) != null;
    }
    
    /** Checks whetner the point is inside one of floating window,
     * i.e. separated modes, droppable area. The point is relative to screen. */
    private boolean isInFloatingFrameDroppable(Set<Component> floatingFrames, Point location, TopComponentDraggable transfer) {
        return findFloatingFrameDroppable(floatingFrames, location, transfer) != null;
    }
    
    /** 
     * Tests if given location is in free area or not.
     * @param location the location to test
     * @param exclude Window to exclude from test (used for fake drag-under effect window)
     * @return true when location point is on free screen, not contained in any 
     * frames or windows of the system, false otherwise
     */
    private static boolean isInFreeArea(Point location, Window exclude) {
        // prepare array of all our windows
        Window mainWindow = WindowManagerImpl.getInstance().getMainWindow();
        Window[] owned = mainWindow.getOwnedWindows();
        Window[] frames = Frame.getFrames();
        Window[] windows = new Window[owned.length + frames.length];
        System.arraycopy(frames, 0, windows, 0, frames.length);
        System.arraycopy(owned, 0, windows, frames.length, owned.length);

        for(int i = 0; i < windows.length; i++) {
            // #114064: exclude fake drar under effect window from the test
            if (windows[i] == exclude) {
                continue;
            }
            //#40782 fix. don't take the invisible frames into account when deciding what is
            // free space.
            if(windows[i].isVisible() && windows[i].getBounds().contains(location.x, location.y)) {
                return false;
            }
        }
        
        return true;
    }

    /** Finds <code>TopComponentDroppable</code> from specified screen location. */
    private TopComponentDroppable findDroppableFromScreen(
    Set<Component> floatingFrames, Point location, TopComponentDraggable transfer) {

        TopComponentDroppable droppable = findMainWindowDroppable(location, transfer);
        if(droppable != null) {
            return droppable;
        }
        
        if( transfer.isUndockingEnabled() ) {
            droppable = findFloatingFrameDroppable(floatingFrames, location, transfer);
            if(droppable != null) {
                return droppable;
            } 
        
//        // PENDING center panel area. Maybe editor empty area -> revise later.
//        if(isAroundCenterPanel(location)) {
//            return getCenterPanelDroppable();
//        }
        
            if(isInFreeArea(location, motionListener.fakeWindow)) {
                return getFreeAreaDroppable(location);
            }
        }
        return null;
    }

    private CenterSlidingDroppable lastSlideDroppable;
    
    /** Gets droppable from main window, specified by screen location.
     * Helper method. */
    private TopComponentDroppable findMainWindowDroppable(Point location, TopComponentDraggable transfer) {
        
        JFrame mainWindow = (JFrame)WindowManagerImpl.getInstance().getMainWindow();

        if (!ZOrderManager.getInstance().isOnTop(mainWindow, location)) {
            return null;
        }

        Point p = new Point(location);
        SwingUtilities.convertPointFromScreen(p, mainWindow.getContentPane());
        if( transfer.isSlidingEnabled() ) {
            if (lastSlideDroppable != null) {
                if (lastSlideDroppable.isWithinSlide(p)) {
                    return lastSlideDroppable;
                }
            }
            TopComponentDroppable droppable = findSlideDroppable(viewAccessor.getSlidingModeComponent(Constants.LEFT));
            if (droppable != null) {
                CenterSlidingDroppable drop = new CenterSlidingDroppable(viewAccessor, droppable, Constants.LEFT);
                if (drop.isWithinSlide(p)) {
                    if( !drop.supportsKind( transfer ) ) {
                        lastSlideDroppable = null;
                        return null;
                    }
                    lastSlideDroppable = drop;
                    return drop;
                }
            }
            droppable = findSlideDroppable(viewAccessor.getSlidingModeComponent(Constants.RIGHT));
            if (droppable != null) {
                CenterSlidingDroppable drop = new CenterSlidingDroppable(viewAccessor, droppable, Constants.RIGHT);
                if (drop.isWithinSlide(p)) {
                    if( !drop.supportsKind( transfer ) ) {
                        lastSlideDroppable = null;
                        return null;
                    }
                    lastSlideDroppable = drop;
                    return drop;
                }
            }
            droppable = findSlideDroppable(viewAccessor.getSlidingModeComponent(Constants.BOTTOM));
            if (droppable != null) {
                CenterSlidingDroppable drop = new CenterSlidingDroppable(viewAccessor, droppable, Constants.BOTTOM);
                if (drop.isWithinSlide(p)) {
                    if( !drop.supportsKind( transfer ) ) {
                        lastSlideDroppable = null;
                        return null;
                    }
                    lastSlideDroppable = drop;
                    return drop;
                }
            }
            droppable = findSlideDroppable(viewAccessor.getSlidingModeComponent(Constants.TOP));
            if (droppable != null) {
                CenterSlidingDroppable drop = new CenterSlidingDroppable(viewAccessor, droppable, Constants.TOP);
                if (drop.isWithinSlide(p)) {
                    if( !drop.supportsKind( transfer ) ) {
                        lastSlideDroppable = null;
                        return null;
                    }
                    lastSlideDroppable = drop;
                    return drop;
                }
            }
        }
        lastSlideDroppable = null;
        if (isNearEditorEdge(location, viewAccessor, transfer.getKind())) {
            return getEditorAreaDroppable();
        }
        if (isNearEdge(location, viewAccessor)) {
            return getCenterPanelDroppable();
        }
        Point mainP = new Point(location);
        SwingUtilities.convertPointFromScreen(mainP, mainWindow);
        return findDroppable(mainWindow, mainP, transfer);
    }
    
    private static TopComponentDroppable findSlideDroppable(Component comp) {
        if( !Switches.isDragAndDropSlidingEnabled() )
            return null;
        TopComponentDroppable droppable = null;
        if(comp instanceof TopComponentDroppable) {
            droppable = (TopComponentDroppable)comp;
        } else {
            droppable = (TopComponentDroppable)SwingUtilities.getAncestorOfClass(TopComponentDroppable.class, comp);
        }
        return droppable;
    }

    /** Gets droppable from separated (floating) window, specified
     * by screen location. Helper method. */
    private TopComponentDroppable findFloatingFrameDroppable(
    Set<Component> floatingFrames, Point location, TopComponentDraggable transfer) {
        for(Component comp: floatingFrames) {
            Rectangle bounds = comp.getBounds();
            
            if(bounds.contains(location) &&
               ZOrderManager.getInstance().isOnTop((RootPaneContainer)comp, location)) {
                TopComponentDroppable droppable = findDroppable(comp,
                        new Point(location.x - bounds.x, location.y - bounds.y), transfer);
                if(droppable != null) {
                    return droppable;
                }
            }
        }
        
        return null;
    }
    
    /** Finds <code>TopComponentDroppable</code> for the location in component.
     * The location has to be relative to the specified component. Then the
     * method finds if there is a droppable component in the hierarchy, which
     * also contains the specified location.
     * Utilitity method. */
    private TopComponentDroppable findDroppable(Component comp,
                         Point location, TopComponentDraggable transfer) {
        RootPaneContainer rpc;
        if(comp instanceof RootPaneContainer) {
            rpc = (RootPaneContainer)comp;
        } else {
            Window w = SwingUtilities.getWindowAncestor(comp);
            if(w instanceof RootPaneContainer) {
                rpc = (RootPaneContainer)w;
            } else {
                return null;
            }
        }

        Point screenLocation = new Point( location );
        SwingUtilities.convertPointToScreen( screenLocation, comp );
        Component contentPane = rpc.getContentPane();
        location = SwingUtilities.convertPoint(comp, location, contentPane);
        Component deepest = SwingUtilities.getDeepestComponentAt(
                contentPane, location.x, location.y);
        
        if( deepest instanceof MultiSplitPane ) {
            MultiSplitPane splitPane = (MultiSplitPane)deepest;
            int dx = 0, dy = 0;
            if( splitPane.isHorizontalSplit() )
                dx = splitPane.getDividerSize()+1;
            else
                dy = splitPane.getDividerSize()+1;
            Point pt = SwingUtilities.convertPoint( contentPane, location, deepest );
            deepest = SwingUtilities.getDeepestComponentAt( deepest, pt.x+dx, pt.y+dy );
        }
        
        if(deepest instanceof TopComponentDroppable) {
            TopComponentDroppable droppable = (TopComponentDroppable)deepest;
            if(droppable.supportsKind(transfer)) {
                return droppable;
            }
        }
        
        TopComponentDroppable res = null;
        while(deepest != null) {
            TopComponentDroppable nextDroppable = (TopComponentDroppable)SwingUtilities.getAncestorOfClass(
                    TopComponentDroppable.class, deepest);
            if(nextDroppable != null && nextDroppable.supportsKind(transfer)) {
                res = nextDroppable;
                break;
            }
            deepest = (Component)nextDroppable;
        }
        if( res instanceof ModeComponent && transfer.getKind() != Constants.MODE_KIND_EDITOR && ((ModeComponent)res).getKind() == Constants.MODE_KIND_EDITOR ) {
            //if user is about to drop a view component/mode at the edge of editor
            //area then make it a 'drop around editor' instead of converting
            //that view component/mode into an editor mode
            TopComponentDroppable editor = getEditorAreaDroppable();
            if( editor.supportsKind( transfer ) ) {
                Point p = new Point( screenLocation );
                SwingUtilities.convertPointFromScreen( p, res.getDropComponent() );
                Object side = res.getConstraintForLocation( p );
                p = new Point( screenLocation );
                SwingUtilities.convertPointFromScreen( p, editor.getDropComponent() );
                if( null != side && side.equals( editor.getConstraintForLocation( p ) ) ) {
                    res = editor;
                }
            }
        }
        return res;
    }
    
    /** Indicates whether the cursor is around center panel of main window.
     * In that case is needed also to provide a drop. */
    static boolean isAroundCenterPanel(Point location) {
        Component desktop = MainWindow.getInstance().getDesktop();
        if(desktop == null) {
            return false;
        }
        
        Point p = new Point(location);
        SwingUtilities.convertPointFromScreen(p, desktop.getParent());
        Rectangle centerBounds = desktop.getBounds();

        if(!centerBounds.contains(p)) {
            centerBounds.grow(Constants.DROP_AREA_SIZE, Constants.DROP_AREA_SIZE);
            if(centerBounds.contains(p)) {
                return true;
            }
        }
        return false;
    }
    
    /** Indicates whether document and non-document top components are about to be mixed
     *  when transfer drops into droppable.
     */
    static boolean isMixedTCDragDrop(TopComponentDraggable transfer, TopComponentDroppable droppable) {
        if (transfer != null && droppable != null) {
            if ((droppable.getKind() == Constants.MODE_KIND_EDITOR
                    && transfer.getKind() != Constants.MODE_KIND_EDITOR)
                    || (droppable.getKind() != Constants.MODE_KIND_EDITOR
                    && transfer.getKind() == Constants.MODE_KIND_EDITOR)) {
                return true;
            }
        }
        return false;
    }
    
    /** Indicates whether the cursor is around the editor area of the main window.
     * In that case is needed also to provide a drop. */
    static boolean isNearEditorEdge(Point location, ViewAccessor viewAccessor, int kind) {
        Component editor = WindowManagerImpl.getInstance().getEditorAreaComponent();
        if(editor == null || editor.getParent() == null) {
            return false;
        }
        Point p = new Point(location);
        SwingUtilities.convertPointFromScreen(p, editor.getParent());
        Rectangle editorBounds = editor.getBounds();
        editorBounds.y -= 10;
        editorBounds.height += 10;
        Rectangle shrinked = editor.getBounds();
        shrinked.grow(-10,0);
        shrinked.height -= 10;
        Component dr = viewAccessor.getSlidingModeComponent(Constants.RIGHT);
        if (dr != null) {
            shrinked.width = shrinked.width - dr.getBounds().width;
        }
        dr = viewAccessor.getSlidingModeComponent(Constants.BOTTOM);
        if (dr != null) {
            shrinked.height = shrinked.height - dr.getBounds().height;
        }
        return editorBounds.contains(p) && !shrinked.contains(p) && kind == Constants.MODE_KIND_EDITOR;
    }    
    
    
    /** Indicates whether the cursor is around center panel of main window.
     * In that case is needed also to provide a drop. */
    static boolean isNearEdge(Point location, ViewAccessor viewAccessor) {
        Component desktop = MainWindow.getInstance().getDesktop();
        if(desktop == null) {
            return false;
        }
        Point p = new Point(location);
        SwingUtilities.convertPointFromScreen(p, desktop);
        Rectangle centerBounds = desktop.getBounds();
        centerBounds.y -= 20;
        centerBounds.height += 20;
        Rectangle shrinked = desktop.getBounds();
        shrinked.grow(-10,0);
        shrinked.height -= 10;
        Component dr = viewAccessor.getSlidingModeComponent(Constants.LEFT);
        if (dr != null) {
            shrinked.x = shrinked.x + dr.getBounds().width;
            shrinked.width = shrinked.width - dr.getBounds().width;
        }
        dr = viewAccessor.getSlidingModeComponent(Constants.RIGHT);
        if (dr != null) {
            shrinked.width = shrinked.width - dr.getBounds().width;
        }
        dr = viewAccessor.getSlidingModeComponent(Constants.BOTTOM);
        if (dr != null) {
            shrinked.height = shrinked.height - dr.getBounds().height;
        }
        dr = viewAccessor.getSlidingModeComponent(Constants.TOP);
        if (dr != null) {
            shrinked.y += dr.getBounds().height;
        }
        boolean cont =  centerBounds.contains(p) && !shrinked.contains(p);
        
        return cont;
    }    
    
    /** Creates fake droppable for center panel. */
    TopComponentDroppable getCenterPanelDroppable() {
        CenterPanelDroppable droppable = centerDropWRef.get();
        
        if(droppable == null) {
            droppable = new CenterPanelDroppable();
            centerDropWRef = new WeakReference<CenterPanelDroppable>(droppable);
        }
        
        return droppable;
    }
    
    private static TopComponentDroppable getFreeAreaDroppable(Point location) {
        return new FreeAreaDroppable(location);
    }

    /** Creates fake droppable for editor area. */
    private TopComponentDroppable getEditorAreaDroppable() {
        EditorAreaDroppable droppable = editorDropWRef.get();
        
        if(droppable == null) {
            droppable = new EditorAreaDroppable();
            editorDropWRef = new WeakReference<EditorAreaDroppable>(droppable);
        }
        
        return droppable;
    }
    
    
    /** 
     * Tries to perform actual drop.
     * @param location screen location */
    boolean tryPerformDrop(Controller controller, Set<Component> floatingFrames,
    Point location, int dropAction, Transferable transferable) {
        TopComponentDraggable draggable = extractTopComponentDraggable(
            dropAction == DnDConstants.ACTION_COPY,
            transferable
        );
        
        if(draggable == null) {
            return false;
        }
        
        TopComponentDroppable droppable
                = findDroppableFromScreen(floatingFrames, location, draggable);
        if(droppable == null) {
            return false;
        }
        
        Component dropComponent = droppable.getDropComponent();
        if(dropComponent != null) {
            SwingUtilities.convertPointFromScreen(location, dropComponent);
        }
        return performDrop(controller, droppable, draggable, location);
    }
    
    /** Extracts <code>TopComponent</code> instance from
     * <code>Transferable</code> according the <code>dropAction</code>.
     * Utility method. */
    static TopComponentDraggable extractTopComponentDraggable(boolean clone,
    Transferable tr) {
        DataFlavor df = getDataFlavorForDropAction(clone);
        
        if(df == null) {
            // No data flavor -> unsupported drop action.
            return null;
        }

        // Test whether the requested dataflavor is supported by transferable.
        if(tr.isDataFlavorSupported(df)) {
            try {
                TopComponent tc; 

                if(clone) {
                    TopComponent.Cloneable ctc = (TopComponent.Cloneable)tr
                        .getTransferData(df);

                    // "Copy" the top component.
                    tc = ctc.cloneComponent();
                    tc.putClientProperty("windnd_cloned_tc", Boolean.TRUE);
                } else {
                    tc = (TopComponent)tr.getTransferData(df);
                }

                if( null != tc ) {
                    ModeImpl mode = ( ModeImpl ) WindowManagerImpl.getInstance().findMode( (TopComponent)tr.getTransferData(df) );
                    if( null != mode ) {
                        return new TopComponentDraggable( tc, mode );
                    }
                }
            } catch(UnsupportedFlavorException ufe) {
                Logger.getLogger(WindowDnDManager.class.getName()).log(Level.WARNING, null, ufe);
            } catch(IOException ioe) {
                Logger.getLogger(WindowDnDManager.class.getName()).log(Level.WARNING, null, ioe);
            }
        }
    
        df = new DataFlavor(TopComponentDragSupport.MIME_TOP_COMPONENT_MODE, null);
        if(tr.isDataFlavorSupported(df)) {
            try {
                ModeImpl mode = (ModeImpl)tr.getTransferData(df);
                if( null != mode )
                    return new TopComponentDraggable( mode );
            } catch(UnsupportedFlavorException ufe) {
                Logger.getLogger(WindowDnDManager.class.getName()).log(Level.WARNING, null, ufe);
            } catch(IOException ioe) {
                Logger.getLogger(WindowDnDManager.class.getName()).log(Level.WARNING, null, ioe);
            }
        }
        
        return null;
    }
    
    /** Gets <code>DataFlavor</code> for specific drop action type.
     * Helper utility method. */
    private static DataFlavor getDataFlavorForDropAction(boolean clone) {
        // Create needed dataflavor.
        DataFlavor df = null;
        ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            if(clone) {
                df = new DataFlavor(TopComponentDragSupport.MIME_TOP_COMPONENT_CLONEABLE, null, cl);
            } else {
                df = new DataFlavor(TopComponentDragSupport.MIME_TOP_COMPONENT, null, cl);
            }
        } catch( ClassNotFoundException cnfE ) {
            Logger.getLogger(WindowDnDManager.class.getName()).log(Level.INFO, null, cnfE);
        }
        
        return df;
    }    
    
    /**
     * Performs actual drop operation. Called from DropTargetListener.
     * @return <code>true</code> if the drop was successful */
    static boolean performDrop(Controller controller,
    TopComponentDroppable droppable, TopComponentDraggable draggable, Point location) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("performDrop"); // NOI18N
            debugLog("droppable=" + droppable); // NOI18N
        }

        if(draggable == null) {
            return true;
        }
        
        if(!droppable.canDrop(draggable, location)) {
            return true;
        }
        
        ViewElement viewElement = droppable.getDropViewElement();
        Object constr = droppable.getConstraintForLocation(location);

        if(viewElement instanceof EditorView)  {
            int kind = draggable.getKind();
            if(kind == Constants.MODE_KIND_EDITOR) {
                controller.userDroppedTopComponentsIntoEmptyEditor(draggable);
            } else {
                if(constr == Constants.TOP
                || constr == Constants.LEFT
                || constr == Constants.RIGHT
                || constr == Constants.BOTTOM) {
                    controller.userDroppedTopComponentsAroundEditor(draggable, (String)constr);
                } else if( draggable.isAllowedToMoveAnywhere() ) {
                    controller.userDroppedTopComponentsIntoEmptyEditor(draggable);
                }
            }
        } else if(viewElement instanceof ModeView) {
            ModeView modeView = (ModeView)viewElement;
            if(constr == Constants.TOP
            || constr == Constants.LEFT
            || constr == Constants.RIGHT
            || constr == Constants.BOTTOM) {
                controller.userDroppedTopComponents(modeView, draggable, (String)constr);
            } else if(constr instanceof Integer) {
                controller.userDroppedTopComponents(modeView, draggable, ((Integer)constr).intValue());
            } else {
                controller.userDroppedTopComponents(modeView, draggable);
            }
        } else if(viewElement == null) { // XXX around area or free area
            if(constr == Constants.TOP
            || constr == Constants.LEFT
            || constr == Constants.RIGHT
            || constr == Constants.BOTTOM) { // XXX around area
                if( droppable instanceof EditorAreaDroppable ) {
                    controller.userDroppedTopComponentsAroundEditor(draggable, (String)constr);
                } else {
                    controller.userDroppedTopComponentsAround(draggable, (String)constr);
                }
            } else if(constr instanceof Rectangle) { // XXX free area
                Rectangle bounds = (Rectangle)constr;
                // #38657 Refine bounds.
                Rectangle modeBounds = draggable.getBounds();
                if( null != modeBounds ) {
                    bounds.setSize(modeBounds.width, modeBounds.height);
                }
                
                controller.userDroppedTopComponentsIntoFreeArea(draggable, bounds);
            }
        }

        return true;
    }
    // Helpers<<

    public void startKeyboardDragAndDrop( TopComponentDraggable draggable ) {
        KeyboardDnd.start( this, draggable, viewAccessor );
    }
    
    /** Handles mouse cursors shapes and drag-under feedback during the drag.
     */
    private static class MotionListener implements DragSourceMotionListener {

        private final WindowDnDManager windowDnDManager;
        private final TopComponentDragSupport topComponentDragSupport;

        private Point previousDragLoc;

        /** window used to simulate drag under effect when dropping to free screen area */
        private Window fakeWindow;

        /** helper; true when size of fake window set and known, false otherwise */
        private boolean isSizeSet;
        
        /** Constrtucts the instance.
         * Adds the listener to the window dnd <code>DragSource</code>. */
        private MotionListener(WindowDnDManager windowDnDManager,
        TopComponentDragSupport topComponentDragSupport) {
            this.windowDnDManager = windowDnDManager;
            this.topComponentDragSupport = topComponentDragSupport;
        }
        

        /** Implements <code>DragSourceMotionListener</code>. */
        @Override
        public void dragMouseMoved(DragSourceDragEvent evt) {
            if(DEBUG) {
                debugLog("dragMouseMoved evt=" + evt); // NOI18N
            }
            
            Point location = WindowDnDManager.getLocationWorkaround(evt);
            if(location == null) {
                return;
            }

            if(windowDnDManager.startingTransfer == null)
                return;

            // move separate windows along with the mouse
            /*if (Constants.MODE_STATE_SEPARATED == mode.getState()) {
                handleWindowMove(mode, windowDnDManager.startingTransfer, evt);
            }*/
            boolean isInMainDroppable
                    = windowDnDManager.isInMainWindowDroppable(location, windowDnDManager.startingTransfer);
            boolean isInFrameDroppable
                    = windowDnDManager.isInFloatingFrameDroppable(windowDnDManager.getFloatingFrames(), location, windowDnDManager.startingTransfer)
                    && windowDnDManager.startingTransfer.isUndockingEnabled();
            boolean isAroundCenterPanel
                    = isAroundCenterPanel(location);
            boolean isMixedTCDragDrop = isMixedTCDragDrop(windowDnDManager.startingTransfer, windowDnDManager.findDroppableFromScreen(windowDnDManager.getFloatingFrames(), location, windowDnDManager.startingTransfer));
            
            if(isInMainDroppable || isInFrameDroppable || isAroundCenterPanel) {
                TopComponentDroppable droppable 
                        = windowDnDManager.findDroppableFromScreen(windowDnDManager.getFloatingFrames(), location, windowDnDManager.startingTransfer);
                //hack - can't get the bounds correctly, sometimes freearedroppable gets here..
                
                if (droppable instanceof FreeAreaDroppable) {
                    if(WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED
                        && droppable.canDrop(windowDnDManager.startingTransfer, location)) {
                        topComponentDragSupport.setSuccessCursor(true, isMixedTCDragDrop);
                    } else {
                        topComponentDragSupport.setUnsuccessCursor(isMixedTCDragDrop);
                    }                    
                    // for the status bar it's null somehow, workarounding by checking for null.. should go away..
                } else if (droppable != null) {
                    
                    // was probably forgotten to set the lastdrop target, was causing strange repaint side effects when 2 frames overlapped.
                    JComponent cp = (JComponent)droppable.getDropComponent();
                    JRootPane rootPane = cp.getRootPane();
                    if (rootPane != null) {
                        Component glass = rootPane.getGlassPane();
                        if (glass instanceof DropTargetGlassPane) {
                            windowDnDManager.setLastDropTarget((DropTargetGlassPane)glass);
                        }
                    }
                    Point p = new Point(location);
                    SwingUtilities.convertPointFromScreen(p, droppable.getDropComponent());
                    if(droppable.canDrop(windowDnDManager.startingTransfer, p)) {
                        topComponentDragSupport.setSuccessCursor(false, isMixedTCDragDrop);
                    } else {
                        topComponentDragSupport.setUnsuccessCursor(isMixedTCDragDrop);
                    }
                    dragOverDropTarget(location, droppable);
                }
            } else if(!isInMainWindow(location) 
                        && windowDnDManager.isInFloatingFrame(location)) {
                // Simulates success drop in free area.
                topComponentDragSupport.setSuccessCursor(false, isMixedTCDragDrop);
            } else if(isInFreeArea(location, fakeWindow)
                        && getFreeAreaDroppable(location).canDrop(windowDnDManager.startingTransfer, location)
                        && windowDnDManager.startingTransfer.isUndockingEnabled()) {
                topComponentDragSupport.setSuccessCursor(true, isMixedTCDragDrop);
            } else {
                topComponentDragSupport.setUnsuccessCursor(isMixedTCDragDrop);
            }
            
            if(!isInMainDroppable && !isInFrameDroppable && !isAroundCenterPanel) {
                clearExitedDropTarget();
            }
        }
        
        /** Simulates dropOver event, for glass pane to indicate possible drop
         * operation. */
        private /*static*/ void dragOverDropTarget(Point location,
        TopComponentDroppable droppable) {
            DropTargetGlassPane lastTarget
                = windowDnDManager.lastTargetWRef.get();

            if(lastTarget != null) {
                Point p = new Point(location);
                SwingUtilities.convertPointFromScreen(p, lastTarget);
                lastTarget.dragOver(p, droppable);
            }
        }

        /** Hacks drag exit from drop target (glass pane).
         * Eliminates bug, where remained drop indicator drawed
         * even for cases the cursor was away from drop target. Missing
         * drag exit event. */
        private /*static*/ void clearExitedDropTarget() {
            DropTargetGlassPane lastTarget
                = windowDnDManager.lastTargetWRef.get();

            if(lastTarget != null) {
                lastTarget.clearIndications();
                windowDnDManager.lastTargetWRef = new WeakReference<DropTargetGlassPane>(null);
            }
        }
        
        void dragFinished () {
            previousDragLoc = null;
            if (fakeWindow != null) {
                fakeWindow.dispose();
                fakeWindow = null;
            }
        }
        
    } // End of class MotionListener.
    

    // XXX
    /** Interface for accessing   */
    public interface ViewAccessor {
        public Set<Component> getModeComponents();
        public Set<Component> getSeparateModeFrames();
        public Controller getController();
        public Component getSlidingModeComponent(String side);
    } // End of ViewState.
    
    /** Fake helper droppable used when used around  */
    private class CenterPanelDroppable implements TopComponentDroppable {

        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public java.awt.Shape getIndicationForLocation(Point p) {
            Rectangle bounds = getDropComponent().getBounds();
            Rectangle res = null;
            double ratio = Constants.DROP_AROUND_RATIO;
            Object constraint = getConstraintForLocation(p);
            if(constraint == JSplitPane.LEFT) {
                res = new Rectangle(0, 0, (int)(bounds.width * ratio) - 1, bounds.height - 1);
            } else if(constraint == JSplitPane.TOP) {
                res = new Rectangle(0, 0, bounds.width - 1, (int)(bounds.height * ratio) - 1);
            } else if(constraint == JSplitPane.RIGHT) {
                res = new Rectangle(bounds.width - (int)(bounds.width * ratio), 0,
                        (int)(bounds.width * ratio) - 1, bounds.height - 1);
            } else if(constraint == JSplitPane.BOTTOM) {
                res = new Rectangle(0, bounds.height - (int)(bounds.height * ratio), bounds.width - 1,
                        (int)(bounds.height * ratio) - 1);
            }

            return res;
        }

        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public Object getConstraintForLocation(Point p) {
            Rectangle bounds = getDropComponent().getBounds();
            Component leftSlide = viewAccessor.getSlidingModeComponent(Constants.LEFT);
            Component rightSlide = viewAccessor.getSlidingModeComponent(Constants.RIGHT);
            Component bottomSlide = viewAccessor.getSlidingModeComponent(Constants.BOTTOM);
            Component topSlide = viewAccessor.getSlidingModeComponent(Constants.TOP);
            if(null != leftSlide && p.x <  leftSlide.getBounds().width + 10) {
                return javax.swing.JSplitPane.LEFT;
            } else if(p.y < bounds.y) {
                return javax.swing.JSplitPane.TOP;
            } else if(null !=rightSlide && null != leftSlide 
                      && p.x > bounds.width - 10 - rightSlide.getBounds().width - leftSlide.getBounds().width) {
                return javax.swing.JSplitPane.RIGHT;
            } else if(null != bottomSlide && p.y > bounds.height - 10 - bottomSlide.getBounds().height) {
                return javax.swing.JSplitPane.BOTTOM;
            } else if(null != topSlide && p.y < bounds.y + topSlide.getBounds().height + 10) {
                return javax.swing.JSplitPane.TOP;
            }

            return null;
        }

        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public Component getDropComponent() {
            return MainWindow.getInstance().getDesktop();
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public ViewElement getDropViewElement() {
            return null;
        }
        
        @Override
        public boolean canDrop(TopComponentDraggable transfer, Point location) {
            if( transfer.isAllowedToMoveAnywhere() ) {
                return true;
            }

            return transfer.getKind() == Constants.MODE_KIND_VIEW || transfer.getKind() == Constants.MODE_KIND_SLIDING;
        }
        
        @Override
        public boolean supportsKind(TopComponentDraggable transfer) {
            if( transfer.isAllowedToMoveAnywhere() ) {
                return true;
            }

            return transfer.getKind() == Constants.MODE_KIND_VIEW || transfer.getKind() == Constants.MODE_KIND_SLIDING;
        }

        @Override
        public int getKind() {
            return Constants.MODE_KIND_VIEW;
        }

    } // End of class CenterPanelDroppable.
    
    /** Fake helper droppable used when used around  */
    private class EditorAreaDroppable implements TopComponentDroppable {

        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public java.awt.Shape getIndicationForLocation(Point p) {
            Rectangle bounds = getDropComponent().getBounds();
            Rectangle res = null;
            double ratio = Constants.DROP_AROUND_EDITOR_RATIO;
            Object constraint = getConstraintForLocation(p);
            if(constraint == JSplitPane.LEFT) {
                res = new Rectangle(0, 0, (int)(bounds.width * ratio) - 1, bounds.height - 1);
            } else if(constraint == JSplitPane.TOP) {
                res = new Rectangle(0, 0, bounds.width - 1, (int)(bounds.height * ratio) - 1);
            } else if(constraint == JSplitPane.RIGHT) {
                res = new Rectangle(bounds.width - (int)(bounds.width * ratio), 0,
                        (int)(bounds.width * ratio) - 1, bounds.height - 1);
            } else if(constraint == JSplitPane.BOTTOM) {
                res = new Rectangle(0, bounds.height - (int)(bounds.height * ratio), bounds.width - 1,
                        (int)(bounds.height * ratio) - 1);
            }

            return res;
        }

        private static final int DROP_BORDER_WIDTH = 30;
        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public Object getConstraintForLocation(Point p) {
            Rectangle bounds = getDropComponent().getBounds();
            Component leftSlide = viewAccessor.getSlidingModeComponent(Constants.LEFT);
            Component rightSlide = viewAccessor.getSlidingModeComponent(Constants.RIGHT);
            Component bottomSlide = viewAccessor.getSlidingModeComponent(Constants.BOTTOM);
            Component topSlide = viewAccessor.getSlidingModeComponent(Constants.TOP);
            if(null != leftSlide && p.x <  leftSlide.getBounds().width + DROP_BORDER_WIDTH) {
                return javax.swing.JSplitPane.LEFT;
            } else if(p.y < bounds.y) {
                return javax.swing.JSplitPane.TOP;
            } else if(null !=rightSlide && null != leftSlide 
                      && p.x > bounds.width - DROP_BORDER_WIDTH - rightSlide.getBounds().width - leftSlide.getBounds().width) {
                return javax.swing.JSplitPane.RIGHT;
            } else if(null != bottomSlide && p.y > bounds.height - DROP_BORDER_WIDTH - bottomSlide.getBounds().height) {
                return javax.swing.JSplitPane.BOTTOM;
            } else if(null != topSlide && p.y < bounds.y + topSlide.getBounds().height + DROP_BORDER_WIDTH) {
                return javax.swing.JSplitPane.TOP;
            }

            return null;
        }

        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public Component getDropComponent() {
            return WindowManagerImpl.getInstance().getEditorAreaComponent();
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public ViewElement getDropViewElement() {
            return null;
        }
        
        @Override
        public boolean canDrop(TopComponentDraggable transfer, Point location) {
            if(transfer.isAllowedToMoveAnywhere() ) {
                return true;
            }

            return transfer.getKind() == Constants.MODE_KIND_EDITOR;
        }
        
        @Override
        public boolean supportsKind(TopComponentDraggable transfer) {
            if(transfer.isAllowedToMoveAnywhere() ) {
                return true;
            }

            return transfer.getKind() == Constants.MODE_KIND_EDITOR;
        }

        @Override
        public int getKind() {
            if( null == getStartingDroppable() )
                return Constants.MODE_KIND_EDITOR;
            return getStartingDroppable().getKind();
        }

    } // End of class EditorAreaDroppable.

    
    /** Fake helper droppable used when dropping is done into free area.  */
    private static class FreeAreaDroppable implements TopComponentDroppable {
        
        private Point location;
        
        public FreeAreaDroppable(Point location) {
            this.location = location;
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public java.awt.Shape getIndicationForLocation(Point p) {
            return null;
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public Object getConstraintForLocation(Point p) {
            return new Rectangle(location.x, location.y,
                Constants.DROP_NEW_MODE_SIZE.width, Constants.DROP_NEW_MODE_SIZE.height);
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public Component getDropComponent() {
            return null;
        }
        
        /** Implements <code>TopComponentDroppable</code>. */
        @Override
        public ViewElement getDropViewElement() {
            return null;
        }
        
        @Override
        public boolean canDrop(TopComponentDraggable transfer, Point location) {
            if (transfer.isAllowedToMoveAnywhere()) {
                return true;
            }
            ModeImpl mode = transfer.getMode();

            // don't accept drop from separated mode with single component in it,
            // it makes no sense (because such DnD into free area equals to
            // simple window move)
            if (null != mode && mode.getState() == Constants.MODE_STATE_SEPARATED &&
                mode.getOpenedTopComponents().size() == 1) {
                return false;
            }

            return true;
        }
        
        @Override
        public boolean supportsKind(TopComponentDraggable transfer) {
            return true;
        }

        @Override
        public int getKind() {
            return Constants.MODE_KIND_VIEW;
        }

    } // End of class FreeAreaDroppable.
    
    /**
     * droppable for the sliding bars, both inside and outside of the main window.
     *
     */
    private static class CenterSlidingDroppable implements TopComponentDroppable, EnhancedDragPainter {
        
        private ViewAccessor accesor;
        private TopComponentDroppable original;
        private String side;
        JPanel pan;
        private boolean isShowing;
        
        public CenterSlidingDroppable(ViewAccessor viewAccesor, TopComponentDroppable slidingBarDelegate,
                                      String side) {
            original = slidingBarDelegate;
            accesor = viewAccesor;
            this.side = side;
            pan = new JPanel();
            isShowing = false;
        }
        
        @Override
        public boolean canDrop(TopComponentDraggable transfer, Point location) {
            return original.canDrop(transfer, location);
        }

        @Override
        public Object getConstraintForLocation(Point location) {
            return original.getConstraintForLocation(location);
        }

        @Override
        public Component getDropComponent() {
            return original.getDropComponent();
        }

        @Override
        public ViewElement getDropViewElement() {
            return original.getDropViewElement();
        }

        @Override
        public Shape getIndicationForLocation(Point location) {
            Shape toReturn = original.getIndicationForLocation(location);
            Rectangle dim = original.getDropComponent().getBounds();
            if (dim.width < 10 || dim.height < 10) {
                Rectangle rect = toReturn.getBounds();
                if (Constants.LEFT.equals(side)) {
                    toReturn = new Rectangle(0, 0, Math.max(rect.width, Constants.DROP_AREA_SIZE), 
                                                   Math.max(rect.height, Constants.DROP_AREA_SIZE));
                } else if (Constants.RIGHT.equals(side)) {
                    toReturn = new Rectangle(- Constants.DROP_AREA_SIZE, 0, Math.max(rect.width, Constants.DROP_AREA_SIZE), 
                                                                           Math.max(rect.height, Constants.DROP_AREA_SIZE));
                } else if (Constants.BOTTOM.equals(side)) {
                    toReturn = new Rectangle(0, - Constants.DROP_AREA_SIZE, Math.max(rect.width, Constants.DROP_AREA_SIZE), 
                                                                           Math.max(rect.height, Constants.DROP_AREA_SIZE));
                } else if (Constants.TOP.equals(side)) {
                    toReturn = new Rectangle(0, 0, Math.max(rect.width, Constants.DROP_AREA_SIZE), 
                                                                           Math.max(rect.height, Constants.DROP_AREA_SIZE));
                }
            }
            return toReturn;
        }
        
        public boolean isWithinSlide(Point location) {
            Component root = SwingUtilities.getRootPane(original.getDropComponent());
            if( null == root || null == SwingUtilities.getWindowAncestor(original.getDropComponent()) ) {
                return false;
            }
            Point barLoc = SwingUtilities.convertPoint(root, location, original.getDropComponent());
            if (original.getDropComponent().contains(barLoc)) {
                return true;
            }
            Dimension dim = original.getDropComponent().getSize();
            if (Constants.LEFT.equals(side)) {
                int abs = Math.abs(barLoc.x);
                if (barLoc.y > - Constants.DROP_AREA_SIZE && barLoc.y < dim.height + Constants.DROP_AREA_SIZE) {
                    if (isShowing && abs < Constants.DROP_AREA_SIZE) {
                        return true;
                    }
                    if (!isShowing && barLoc.x <= 0 && barLoc.x > - Constants.DROP_AREA_SIZE) {
                        return true;
                    }
                }
            }
            else if (Constants.RIGHT.equals(side)) {
                if (barLoc.y > - Constants.DROP_AREA_SIZE && barLoc.y < dim.height + Constants.DROP_AREA_SIZE) {
                    if (isShowing && ((barLoc.x < 0 && barLoc.x > - Constants.DROP_AREA_SIZE)
                                     || barLoc.x > 0 && barLoc.x - dim.width < Constants.DROP_AREA_SIZE)) {
                        return true;
                    }
                    if (!isShowing && barLoc.x >= 0 && barLoc.x < Constants.DROP_AREA_SIZE + dim.width) {
                        return true;
                    }
                }
            } 
            else if (Constants.BOTTOM.equals(side)) {
                if (barLoc.x > - Constants.DROP_AREA_SIZE && barLoc.x < dim.width + Constants.DROP_AREA_SIZE) {
                    if (isShowing && ((barLoc.y < 0 && barLoc.y > - Constants.DROP_AREA_SIZE)
                                     || barLoc.y > 0 && barLoc.y - dim.height < Constants.DROP_AREA_SIZE)) {
                        return true;
                    }
                    if (!isShowing && barLoc.y >= 0 && barLoc.y < Constants.DROP_AREA_SIZE + dim.height) {
                        return true;
                    }
                }
            }
            else if (Constants.TOP.equals(side)) {
                if (barLoc.x > - Constants.DROP_AREA_SIZE && barLoc.x < dim.width + Constants.DROP_AREA_SIZE) {
                    if (isShowing && ((barLoc.y < 0 && barLoc.y > - Constants.DROP_AREA_SIZE)
                                     || barLoc.y > 0 && barLoc.y + dim.height < Constants.DROP_AREA_SIZE)) {
                        return true;
                    }
                    if (!isShowing && barLoc.y >= 0 && barLoc.y < Constants.DROP_AREA_SIZE + dim.height) {
                        return true;
                    }
                }
            } 
            return false;
            
        }

        @Override
        public boolean supportsKind(TopComponentDraggable transfer) {
            return original.supportsKind(transfer);
        }

        @Override
        public void additionalDragPaint(Graphics2D g) {
            Rectangle dim = original.getDropComponent().getBounds();
            if (dim.width > 10 && dim.height > 10) {
                return;
            }
            isShowing = true;
            Component glassPane = ((JComponent)original.getDropComponent()).getRootPane().getGlassPane();
            Point leftTop = SwingUtilities.convertPoint(original.getDropComponent(), 0, 0, glassPane);
            Point firstDivider;
            Point secondDevider;
                    
            if (Constants.RIGHT.equals(side)) {
                leftTop = new Point(leftTop.x - 24, leftTop.y);
                firstDivider = new Point(leftTop);
                secondDevider = new Point(leftTop.x, leftTop.y + dim.height);
            }
            else if (Constants.BOTTOM.equals(side)) {
                leftTop = new Point(0, leftTop.y - 24);
                firstDivider = new Point(leftTop);
                secondDevider = new Point(leftTop.x + glassPane.getBounds().width, leftTop.y);
            } else if (Constants.TOP.equals(side)) {
                firstDivider = new Point(leftTop.x, leftTop.y+24);
                secondDevider = new Point(leftTop.x + glassPane.getBounds().width, leftTop.y+24);
            } else {
                firstDivider = new Point(leftTop.x + 25, leftTop.y);
                secondDevider = new Point(leftTop.x + 25, leftTop.y + dim.height);
            }
            Rectangle rect = new Rectangle(leftTop.x, leftTop.y, Math.max(25, dim.width), Math.max(25, dim.height));
            if (Constants.BOTTOM.equals(side) || Constants.TOP.equals(side) ) {
                // for bottom has special hack to use the whole width
                rect.width = glassPane.getBounds().width;
            }
            
            Color col = g.getColor();
            g.setColor(pan.getBackground());
            g.fill(rect);
            g.setColor(pan.getBackground().darker());
            g.drawLine(firstDivider.x, firstDivider.y, secondDevider.x, secondDevider.y);
            g.setColor(col);
        }
        
        @Override
        public Rectangle getPaintArea() {
            Rectangle dim = original.getDropComponent().getBounds();
            if (dim.width > 10 && dim.height > 10) {
                return null;
            }
            Component glassPane = ((JComponent)original.getDropComponent()).getRootPane().getGlassPane();
            Point leftTop = SwingUtilities.convertPoint(original.getDropComponent(), 0, 0, glassPane);
                    
            if (Constants.RIGHT.equals(side)) {
                leftTop = new Point(leftTop.x - 24, leftTop.y);
            }
            else if (Constants.BOTTOM.equals(side)) {
                leftTop = new Point(0, leftTop.y - 24);
            }
            else if (Constants.TOP.equals(side)) {
                leftTop = new Point(0, leftTop.y + 24);
            }
            Rectangle rect = new Rectangle(leftTop.x, leftTop.y, Math.max(25, dim.width), Math.max(25, dim.height));
            if (Constants.BOTTOM.equals(side) || Constants.TOP.equals(side) ) {
                // for bottom has special hack to use the whole width
                rect.width = glassPane.getBounds().width;
            }
            return rect;
        }

        @Override
        public int getKind() {
            return Constants.MODE_KIND_SLIDING;
        }
    }

}

