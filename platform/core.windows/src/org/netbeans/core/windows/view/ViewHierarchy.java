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


package org.netbeans.core.windows.view;


import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.dnd.TopComponentDraggable;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.DesktopImpl;
import org.netbeans.core.windows.view.ui.EditorAreaFrame;
import org.netbeans.core.windows.view.ui.MainWindow;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;
import org.openide.windows.TopComponent;

/**
 * Class which manages GUI components.
 *
 * @author  Peter Zavadsky
 */
final class ViewHierarchy {

    /** Observes user changes to view hierarchy. */
    private final Controller controller;
    
    private final WindowDnDManager windowDnDManager;

    /** desktop component maintainer */
    private DesktopImpl desktop = null;
    /** Map of separate mode views (view <-> accessor). */
    private final Map<ModeView, ModeAccessor> separateModeViews = 
            new HashMap<ModeView, ModeAccessor>(10);
    /** Map of sliding mode views (view <-> accessor) */
    private final Map<SlidingView, SlidingAccessor>  slidingModeViews = 
            new HashMap<SlidingView, SlidingAccessor>(10);
    
    /** Component in which is editor area, when the editor state is separated. */
    private EditorAreaFrame editorAreaFrame;

    /** Active mode view. */
    private ModeView activeModeView;
    /** Maximized mode view. */
    private ModeView maximizedModeView;
    private ViewElement currentSplitRoot;
    
    /** Last non sliding mode view that were active in the past, or null if no such exists */
    private WeakReference<ModeView> lastNonSlidingActive;
    
    /** */
    private final Map<ElementAccessor,ViewElement> accessor2view = 
            new HashMap<ElementAccessor,ViewElement>(10);
    /** */
    private final Map<ViewElement,ElementAccessor> view2accessor = 
            new HashMap<ViewElement,ElementAccessor>(10);
    
    private MainWindow mainWindow;

    private final MainWindowListener mainWindowListener;
    
    
    
    /** Creates a new instance of ViewHierarchy. */
    public ViewHierarchy(Controller controller, WindowDnDManager windowDnDManager) {
        this.controller = controller;
        this.windowDnDManager = windowDnDManager;
        
        this.mainWindowListener = new MainWindowListener(controller, this);
    }
    

    public boolean isDragInProgress() {
        return windowDnDManager.isDragging();
    }
    
    public MainWindow getMainWindow() {
        if (mainWindow == null) {
            JFrame mainFrame = null;
            for( Frame f : Frame.getFrames() ) {
                if( f instanceof JFrame ) {
                    JFrame frame = (JFrame)f;
                    if( "NbMainWindow".equals(frame.getName())) { //NOI18N
                        mainFrame = frame;
                        break;
                    }
                }
            }
            if( null == mainFrame ) {
                mainFrame = new JFrame();
                mainFrame.setName( "NbMainWindow" ); //NOI18N
                if (!Constants.AUTO_FOCUS) {
                    mainFrame.setAutoRequestFocus(false);
                }
            }
            if( "Aqua".equals(UIManager.getLookAndFeel().getID())
                    && null == System.getProperty("apple.awt.brushMetalLook") ) {//NOI18N 
                JRootPane root = mainFrame.getRootPane();
                if( null != root )
                    root.putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE); //NOI18N
            }
            Logger.getLogger(MainWindow.class.getName()).log(Level.FINE, "Installing MainWindow into " + mainFrame); //NOI18N
            mainWindow = MainWindow.install(mainFrame);
        }
        return mainWindow;
    }
    
    public void installMainWindowListeners() {
        mainWindow.getFrame().addComponentListener(mainWindowListener);
        mainWindow.getFrame().addWindowStateListener(mainWindowListener);
    }
    
    public void uninstallMainWindowListeners() {
        mainWindow.getFrame().removeComponentListener(mainWindowListener);
        mainWindow.getFrame().removeWindowStateListener(mainWindowListener);
    }
    
    /** Updates the view hierarchy according to new structure. */
    public void updateViewHierarchy(ModeStructureAccessor modeStructureAccessor) {
        updateAccessors(modeStructureAccessor);
        //(re)create gridsplit model
        currentSplitRoot = updateViewForAccessor(modeStructureAccessor.getSplitRootAccessor());
//        System.out.println("updateViewHierarchy... elem=" + elem);
//        if (maximizedModeView == null) {
////            System.out.println("updateViewHierarchy...splitoroot=" + elem);
//            setSplitRootIntoDesktop(elem);
//        } else {
////            System.out.println("updateViewHierarchy...mazimized=" + maximizedModeView);
//            setMaximizedViewIntoDesktop(maximizedModeView);
//        }
        if( null == currentSplitRoot && shouldUseFakeSplitRoot() ) {
            currentSplitRoot = getFakeSplitRoot();
        }
        if (getDesktop().getSplitRoot() == null) {
            setSplitRootIntoDesktop(currentSplitRoot);
        }
        updateSeparateViews(modeStructureAccessor.getSeparateModeAccessors());
        updateSlidingViews(modeStructureAccessor.getSlidingModeAccessors());
    }
    
    /** Puts new instances of accessors in and reuses the old relevant views. */
    public void updateAccessors(ModeStructureAccessor modeStructureAccessor) {
        Map<ElementAccessor,ViewElement> a2v = 
                new HashMap<ElementAccessor,ViewElement>(accessor2view);
        
        accessor2view.clear();
        view2accessor.clear();

        Set<ElementAccessor> accessors  = getAllAccessorsForTree(modeStructureAccessor.getSplitRootAccessor());
        accessors.addAll(Arrays.asList(modeStructureAccessor.getSeparateModeAccessors()));
        accessors.addAll(Arrays.asList(modeStructureAccessor.getSlidingModeAccessors()));
        
        for(ElementAccessor accessor: accessors) {
            ElementAccessor similar = findSimilarAccessor(accessor, a2v);
            if(similar != null) {
                ViewElement view = a2v.get(similar);
                accessor2view.put(accessor, view);
                view2accessor.put(view, accessor);
            }
        }
    }
    
    private Set<ElementAccessor> getAllAccessorsForTree(ElementAccessor accessor) {
        Set<ElementAccessor> s = new HashSet<ElementAccessor>();
        if(accessor instanceof ModeAccessor) {
            s.add(accessor);
        } else if(accessor instanceof SplitAccessor) {
            SplitAccessor sa = (SplitAccessor)accessor;
            s.add(sa);
            ElementAccessor[] children = sa.getChildren();
            for( int i=0; i<children.length; i++ ) {
                s.addAll(getAllAccessorsForTree(children[i]));
            }
        } else if(accessor instanceof EditorAccessor) {
            EditorAccessor ea = (EditorAccessor)accessor;
            s.add(ea);
            s.addAll(getAllAccessorsForTree(ea.getEditorAreaAccessor()));
        }
        
        return s;
    }
    
    private ElementAccessor findSimilarAccessor(ElementAccessor accessor, Map a2v) {
        for(Iterator it = a2v.keySet().iterator(); it.hasNext(); ) {
            ElementAccessor next = (ElementAccessor)it.next();
            if(accessor.originatorEquals(next)) {
                return next;
            }
        }
        
        return null;
    }

    
    private ViewElement updateViewForAccessor(ElementAccessor patternAccessor) {
        if(patternAccessor == null) {
            return null;
        }
        
        ViewElement view = accessor2view.get(patternAccessor);
        
        if(view != null) {
            if(patternAccessor instanceof SplitAccessor) {
                SplitAccessor sa = (SplitAccessor)patternAccessor;
                ElementAccessor[] childAccessors = sa.getChildren();
                ArrayList<ViewElement> childViews = new ArrayList<ViewElement>( childAccessors.length );
                for( int i=0; i<childAccessors.length; i++ ) {
                    childViews.add( updateViewForAccessor( childAccessors[i] ) );
                }
                double[] splitWeights = sa.getSplitWeights();
                ArrayList<Double> weights = new ArrayList<Double>( splitWeights.length );
                for( int i=0; i<splitWeights.length; i++ ) {
                    weights.add( Double.valueOf( splitWeights[i] ) );
                }
                SplitView sv = (SplitView)view;
                sv.setOrientation( sa.getOrientation() );
                sv.setSplitWeights( weights );
                sv.setChildren( childViews );
                return sv;
            } else if(patternAccessor instanceof EditorAccessor) {
                EditorAccessor ea = (EditorAccessor)patternAccessor;
                EditorView ev = (EditorView)view;
                ev.setEditorArea(updateViewForAccessor(ea.getEditorAreaAccessor()));
                return ev;
            } else if(patternAccessor instanceof SlidingAccessor) {
                SlidingAccessor sa = (SlidingAccessor)patternAccessor;
                SlidingView sv = (SlidingView)view;
                sv.setTopComponents(sa.getOpenedTopComponents(), sa.getSelectedTopComponent());
                sv.setSlideBounds(sa.getBounds());
                sv.setSlideInSizes(sa.getSlideInSizes());
                return sv;
            } else if(patternAccessor instanceof ModeAccessor) {
                // It is a ModeView.
                ModeAccessor ma = (ModeAccessor)patternAccessor;
                ModeView mv = (ModeView)view;
                mv.setTopComponents(ma.getOpenedTopComponents(), ma.getSelectedTopComponent());
                if(ma.getState() == Constants.MODE_STATE_SEPARATED) {
                    mv.setFrameState(ma.getFrameState());
                }
                return mv;
            }
        } else {
            if(patternAccessor instanceof SplitAccessor) {
                SplitAccessor sa = (SplitAccessor)patternAccessor;
                ArrayList<Double> weights = new ArrayList<Double>( sa.getSplitWeights().length );
                for( int i=0; i<sa.getSplitWeights().length; i++ ) {
                    weights.add( Double.valueOf( sa.getSplitWeights()[i]) );
                }
                ArrayList<ViewElement> children = new ArrayList<ViewElement>( sa.getChildren().length );
                for( int i=0; i<sa.getChildren().length; i++ ) {
                    children.add( updateViewForAccessor( sa.getChildren()[i] ) );
                }
                SplitView sv = new SplitView(controller, sa.getResizeWeight(),
                    sa.getOrientation(), weights, children );
                accessor2view.put(patternAccessor, sv);
                view2accessor.put(sv, patternAccessor);
                return sv;
            } else if(patternAccessor instanceof SlidingAccessor) {
                SlidingAccessor sa = (SlidingAccessor)patternAccessor;
                SlidingView sv = new SlidingView(controller, windowDnDManager, 
                            sa.getOpenedTopComponents(),sa.getSelectedTopComponent(),
                            sa.getSide(),
                            sa.getSlideInSizes());
                sv.setSlideBounds(sa.getBounds());
                accessor2view.put(patternAccessor, sv);
                view2accessor.put(sv, patternAccessor);
                return sv;
            } else if(patternAccessor instanceof ModeAccessor) {
                ModeAccessor ma = (ModeAccessor)patternAccessor;
                ModeView mv;
                if(ma.getState() == Constants.MODE_STATE_JOINED) {
                    mv = new ModeView(controller, windowDnDManager, ma.getResizeWeight(), ma.getKind(), 
                            ma.getOpenedTopComponents(), ma.getSelectedTopComponent());
                } else {
                    mv = new ModeView(controller, windowDnDManager, ma.getBounds(), ma.getKind(), ma.getFrameState(), 
                            ma.getOpenedTopComponents(), ma.getSelectedTopComponent());
                }
                accessor2view.put(patternAccessor, mv);
                view2accessor.put(mv, patternAccessor);
                return mv;
            } else if(patternAccessor instanceof EditorAccessor) {
                // Editor accesssor indicates there is a editor area (possible split subtree of editor modes).
                EditorAccessor editorAccessor = (EditorAccessor)patternAccessor;
                EditorView ev = new EditorView(controller, windowDnDManager, 
                                editorAccessor.getResizeWeight(), updateViewForAccessor(editorAccessor.getEditorAreaAccessor()));
                accessor2view.put(patternAccessor, ev);
                view2accessor.put(ev, patternAccessor);
                return ev;
            }
        }
        
        throw new IllegalStateException("Unknown accessor type, accessor=" + patternAccessor); // NOI18N
    }
    
    
    private void updateSeparateViews(ModeAccessor[] separateModeAccessors) {
        Map<ModeView, ModeAccessor> newViews = new HashMap<ModeView, ModeAccessor>();
        for(int i = 0; i < separateModeAccessors.length; i++) {
            ModeAccessor ma = separateModeAccessors[i];
            ModeView mv = (ModeView)updateViewForAccessor(ma);
            newViews.put(mv, ma);
        }
        
        Set<ModeView> oldViews = new HashSet<ModeView>(separateModeViews.keySet());
        oldViews.removeAll(newViews.keySet());
        
        separateModeViews.clear();
        separateModeViews.putAll(newViews);
        
        //#212590 - main window must be visible before showing child Dialogs
        if( WindowManagerImpl.getInstance().getMainWindow().isVisible() ) {
            // Close all old views.
            for(Iterator it = oldViews.iterator(); it.hasNext(); ) {
                ModeView mv = (ModeView)it.next();
                Component comp = mv.getComponent();
                if(comp.isVisible()) {
                    comp.setVisible(false);
                }
                ((Window) comp).dispose();
            }
        
            // Open all new views.
            for(Iterator it = newViews.keySet().iterator(); it.hasNext(); ) {
                ModeView mv = (ModeView)it.next();
                Component comp = mv.getComponent();
                // #37463, it is needed to provide a check, otherwise the window would 
                // get fronted each time.
                if(!comp.isVisible()) {
                    comp.setVisible(true);
                }
            }
        }
    }
    
    private void updateSlidingViews(SlidingAccessor[] slidingModeAccessors) {
        Map<SlidingView, SlidingAccessor> newViews = new HashMap<SlidingView, SlidingAccessor>();
        for(int i = 0; i < slidingModeAccessors.length; i++) {
            SlidingAccessor sa = slidingModeAccessors[i];
            SlidingView sv = (SlidingView)updateViewForAccessor(sa);
            newViews.put(sv, sa);
        }
        
        Set<SlidingView> oldViews = new HashSet<SlidingView>(slidingModeViews.keySet());
        oldViews.removeAll(newViews.keySet());
    
        Set<SlidingView> addedViews = new HashSet<SlidingView>(newViews.keySet());
        addedViews.removeAll(slidingModeViews.keySet());

        slidingModeViews.clear();
        slidingModeViews.putAll(newViews);
        
        // remove old views.
        for(SlidingView curSv: oldViews) {
            getDesktop().removeSlidingView(curSv);
        }
        // add all new views.
        for(SlidingView curSv: addedViews) {
            getDesktop().addSlidingView(curSv);
        }
        
        getDesktop().updateCorners();
    }
    
    
    
    public ModeView getModeViewForAccessor(ModeAccessor modeAccessor) {
        return (ModeView)accessor2view.get(modeAccessor);
    }
    
    public ElementAccessor getAccessorForView(ViewElement view) {
        return (ElementAccessor)view2accessor.get(view);
    }

    public void activateMode(ModeAccessor activeModeAccessor) {
        ModeView activeModeV = getModeViewForAccessor(activeModeAccessor);
        activateModeView(activeModeV);
    }

    private void activateModeView(ModeView modeView) {
        setActiveModeView(modeView);
        if(modeView != null) {
            modeView.focusSelectedTopComponent();
            // remember last non sliding active view
            if (!(modeView instanceof SlidingView)) {
                lastNonSlidingActive = new WeakReference<ModeView>(modeView);
            }
        }
    }
    
    /** Set active mode view. */
    private void setActiveModeView(ModeView modeView) {
        //#39729 fix - when main window has focus, do not discard (in SDI the actual component can be hidden
        if(modeView == activeModeView && activeModeView != null && activeModeView.isActive()) {
            return;
        }
        if(activeModeView != null && modeView != activeModeView) {
            activeModeView.setActive(false);
        }
        
        activeModeView = modeView;
        
        if(activeModeView != null) {
            activeModeView.setActive(true);
        }
    }

    /** Gets active mode view. */
    public ModeView getActiveModeView() {
        return activeModeView;
    }
    
    /** Gets last non sliding mode view that was active in the past or null
     * if no such exists
     */
    ModeView getLastNonSlidingActiveModeView() {
        return lastNonSlidingActive == null ? null : lastNonSlidingActive.get();
    }
    
    public void setMaximizedModeView(ModeView modeView) {
        if(modeView == maximizedModeView) {
            return;
        }

        maximizedModeView = modeView;
    }
    
    public ModeView getMaximizedModeView() {
        return maximizedModeView;
    }
    
    public void removeModeView(ModeView modeView) {
        if(!view2accessor.containsKey(modeView)) {
            return;
        }
        
        Object accessor = view2accessor.remove(modeView);
        accessor2view.remove(accessor);

        Set<ModeView> modeViews = separateModeViews.keySet();
        if(modeViews.contains(modeView)) {
            modeViews.remove(modeView);
            modeView.getComponent().setVisible(false);
            return;
        }
        
        setSplitRootIntoDesktop((SplitView)removeModeViewFromElement(getDesktop().getSplitRoot(), modeView));
    }
    
    /** Gets set of all mode view components. */
    public Set<Component> getModeComponents() {
        Set<Component> set = new HashSet<Component>();
        for(ViewElement next:view2accessor.keySet()) {
            if(next instanceof ModeView) {
                ModeView modeView = (ModeView)next;
                set.add(modeView.getComponent());
            }
        }
        
        return set;
    }
    
    public Component getSlidingModeComponent(String side) {
        Iterator it = slidingModeViews.keySet().iterator();
        while (it.hasNext()) {
            SlidingView mod = (SlidingView)it.next();
            if (mod.getSide().equals(side)) {
                return mod.getComponent();
            }
        }
        return null;
    }
    
    /** Gets set of separate mode view frames and editor frame (if separated). */
    public Set<Component> getSeparateModeFrames() {
        Set<Component> s = new HashSet<Component>();
        for(ModeView modeView: separateModeViews.keySet()) {
            s.add(modeView.getComponent());
        }
        
        if(editorAreaFrame != null) {
            s.add(editorAreaFrame);
        }
        
        return s;
    }
    
    
    private ViewElement removeModeViewFromElement(ViewElement view, ModeView modeView) {
        if(view == modeView) {
            return null;
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            List<ViewElement> children = sv.getChildren();
            ArrayList<ViewElement> newChildren = new ArrayList<ViewElement>( children.size() );
            ViewElement removedView = null;
            for(ViewElement child: children) {
                ViewElement newChild = removeModeViewFromElement( child, modeView );
                if( newChild != child ) {
                    removedView = child;
                }
                if( null != newChild )
                    newChildren.add( newChild );
            }
            if( newChildren.size() == 0 ) {
                //the view is not split anymore
                return newChildren.get( 0 );
            }
            if( null != removedView ) {
                sv.remove( removedView );
            }
            sv.setChildren( newChildren );
            return sv;
        } else if(view instanceof EditorView) {
            EditorView ev = (EditorView)view;
            ev.setEditorArea(removeModeViewFromElement(ev.getEditorArea(), modeView));
            return ev;
        }
        
        return view;
    }
    
    private Component getDesktopComponent() {
        return currentSplitRoot == null ? null : getDesktop().getDesktopComponent();
    }    

    public ViewElement getSplitRootElement() {
        return currentSplitRoot;
//        return desktop.getSplitRoot();
    }
    
    public void releaseAll() {
        setSplitRootIntoDesktop(null);
        separateModeViews.clear();
        activeModeView = null;
        accessor2view.clear();
    }
    
    public void setSplitModesVisible(boolean visible) {
        setVisibleModeElement(getDesktop().getSplitRoot(), visible);
    }
    
    private static void setVisibleModeElement(ViewElement view, boolean visible) {
        if(view instanceof ModeView) {
            view.getComponent().setVisible(visible);
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            List children = sv.getChildren();
            for( Iterator i=children.iterator(); i.hasNext(); ) {
                ViewElement child = (ViewElement)i.next();
                setVisibleModeElement( child, visible );
            }
        } else if(view instanceof EditorView) {
            setVisibleModeElement(((EditorView)view).getEditorArea(), visible);
        }
    }
    
    public void setSeparateModesVisible(boolean visible) {
        if(editorAreaFrame != null) {
            if (editorAreaFrame.isVisible() != visible) {
                //#48829 the visible check needed because of this issue
                editorAreaFrame.setVisible(visible);
            }
        } 
        
        for(ModeView mv: separateModeViews.keySet()) {
            if (mv.getComponent().isVisible() != visible) {
                //#48829 the visible check needed because of this issue
                mv.getComponent().setVisible(visible);
            }
        }
        }

    public void updateEditorAreaFrameState(int frameState) {
        if(editorAreaFrame != null) {
            editorAreaFrame.setExtendedState(frameState);
        }
    }
    
    public void updateFrameStates() {
        for(ModeView mv: separateModeViews.keySet()) {
            mv.updateFrameState();
        }
    }
    
    public void updateMainWindowBounds(WindowSystemAccessor wsa) {
        JFrame frame = mainWindow.getFrame();
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            frame.setBounds(wsa.getMainWindowBoundsJoined());
        } else {
            // #45832 clear the desktop when going to SDI,
            setMainWindowDesktop(null);
            // invalidate to recalculate the main window's preffered size..
            frame.invalidate();
            frame.setBounds(wsa.getMainWindowBoundsSeparated());
        }
        // #38146 So the updateSplit works with correct size.
        frame.validate();
        // PENDING consider better handling this event so there is not doubled
        // validation (one in MainWindow - needs to be provided here) and this as second one.
    }
    
    private void setMaximizedViewIntoDesktop(ViewElement elem) {
        boolean revalidate = elem.updateAWTHierarchy(getDesktop().getInnerPaneDimension());
        
        getDesktop().setMaximizedView(elem);
        
        if (revalidate) {
            getDesktop().getDesktopComponent().invalidate();
            ((JComponent)getDesktop().getDesktopComponent()).revalidate();
            getDesktop().getDesktopComponent().repaint();
        }
    }
    
    
    private void setSplitRootIntoDesktop(ViewElement root) {
        boolean revalidate = false;
        getDesktop().setSplitRoot(root);
        if (root != null) {
            Dimension dim = getDesktop().getInnerPaneDimension();
//            debugLog("innerpanedidim=" + dim + " currentsize=" + root.getComponent().getSize());
            revalidate = root.updateAWTHierarchy(dim);
        }
        
        if (revalidate) {
            getDesktop().getDesktopComponent().invalidate();
            ((JComponent)getDesktop().getDesktopComponent()).revalidate();
            getDesktop().getDesktopComponent().repaint();
//            debugLog("revalidating..size=" + desktop.getDesktopComponent().getSize() + "innerpane=" + desktop.getInnerPaneDimension());
        } 
    }

    // PENDING Revise, updating desktop and editor area, bounds... separate this method.
    public void updateDesktop(WindowSystemAccessor wsa) {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        List<Component> focusOwnerAWTHierarchyChain; // To find out whether there was a change in AWT hierarchy according to focusOwner.
        if(focusOwner != null) {
            focusOwnerAWTHierarchyChain = getComponentAWTHierarchyChain(focusOwner);
        } else {
            focusOwnerAWTHierarchyChain = Collections.emptyList();
        }
        
        try {
            if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
                if(maximizedModeView != null) {
                    setMainWindowDesktop(getDesktopComponent());
                    setMaximizedViewIntoDesktop(maximizedModeView);
                    return;
                }
            }
            
            int editorAreaState = wsa.getEditorAreaState();
            if(editorAreaState == Constants.EDITOR_AREA_JOINED) {
                if(editorAreaFrame != null) {
                    editorAreaFrame.setVisible(false);
                    editorAreaFrame = null;
                }
                setMainWindowDesktop(getDesktopComponent());
                setSplitRootIntoDesktop(getSplitRootElement());
                
            } else {
                boolean showEditorFrame = hasEditorAreaVisibleView();
                
                if(editorAreaFrame == null && showEditorFrame) {
                    editorAreaFrame = createEditorAreaFrame();
                    Rectangle editorAreaBounds = wsa.getEditorAreaBounds();
                    if(editorAreaBounds != null) {
                        editorAreaFrame.setBounds(editorAreaBounds);
                    }
                } else if(editorAreaFrame != null && !showEditorFrame) { // XXX
                    editorAreaFrame.setVisible(false);
                    editorAreaFrame = null;
                }
                
                setMainWindowDesktop(null);
                if(showEditorFrame) {
                    setSplitRootIntoDesktop(getSplitRootElement());
                    setEditorAreaDesktop(getDesktopComponent());
                    // #39755 restore the framestate of the previously closed editorArea.
                    updateEditorAreaFrameState(wsa.getEditorAreaFrameState());
                }
            }
        } finally {
            // XXX #37239, #37632 Preserve focus in case the focusOwner component
            // was 'shifted' in AWT hierarchy. I.e. when removed/added it loses focus,
            // but we need to keep it, e.g. for case when its parent split is removing.
            if(focusOwner != null
                && !focusOwnerAWTHierarchyChain.equals(getComponentAWTHierarchyChain(focusOwner))
                /** #64189 */ && SwingUtilities.getAncestorOfClass(Window.class, focusOwner) != null) {
                focusOwner.requestFocus();
            }
        }
    }
    
    public void updateDesktop() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        List focusOwnerAWTHierarchyChain; // To find out whether there was a change in AWT hierarchy according to focusOwner.
        if(focusOwner != null) {
            focusOwnerAWTHierarchyChain = getComponentAWTHierarchyChain(focusOwner);
        } else {
            focusOwnerAWTHierarchyChain = Collections.EMPTY_LIST;
        }
        try {
            //        System.out.println("updatedesktop()");
            if(mainWindow.hasDesktop()) {
                setMainWindowDesktop(getDesktopComponent());
                if(maximizedModeView != null) {
                    //                System.out.println("viewhierarchy: have maximized=" + maximizedModeView.getClass());
                    setMaximizedViewIntoDesktop(maximizedModeView);
                    //                setMainWindowDesktop();
                } else {
                    //                System.out.println("viewhierarchy: no maximized");
                    setSplitRootIntoDesktop(getSplitRootElement());
                    //                setMainWindowDesktop(getDesktopComponent());
                }
            } else {
                boolean showEditorFrame = hasEditorAreaVisibleView();
                
                if(editorAreaFrame != null) {
                    if(showEditorFrame) {
                        editorAreaFrame.setDesktop(getDesktopComponent());
                    } else { // XXX
                        editorAreaFrame.setVisible(false);
                        editorAreaFrame = null;
                    }
                }
            }
        } finally {
            // XXX #37239, #37632 Preserve focus in case the focusOwner component
            // was 'shifted' in AWT hierarchy. I.e. when removed/added it loses focus,
            // but we need to keep it, e.g. for case when its parent split is removing.
            if(focusOwner != null
                && !focusOwnerAWTHierarchyChain.equals(getComponentAWTHierarchyChain(focusOwner))
                /** #64189 */ && SwingUtilities.getAncestorOfClass(Window.class, focusOwner) != null) {
                focusOwner.requestFocus();
            }
        }
    }
    
    public void performSlideIn(SlideOperation operation) {
        getDesktop().performSlideIn(operation, getPureEditorAreaBounds());
    }
    
    public void performSlideOut(SlideOperation operation) {
        getDesktop().performSlideOut(operation, getPureEditorAreaBounds());
    }
    
    public void performSlideIntoDesktop(SlideOperation operation) {
        getDesktop().performSlideIntoDesktop(operation, getPureEditorAreaBounds());
    }
    
    public void performSlideIntoEdge(SlideOperation operation) {
        getDesktop().performSlideIntoEdge(operation, getPureEditorAreaBounds());
    }
    
    public void performSlideResize(SlideOperation operation) {
        getDesktop().performSlideResize(operation);
    }
    
    public void performSlideToggleMaximize( TopComponent tc, String side ) {
        getDesktop().performSlideToggleMaximize( tc, side, getPureEditorAreaBounds());
    }
    
    private void setMainWindowDesktop(Component component) {
        setDesktop(component, true);
    }
    
    private void setEditorAreaDesktop(Component component) {
        setDesktop(component, false);
    }
    
    private void setDesktop(Component component, boolean toMainWindow) {
        
        if(toMainWindow) {
            mainWindow.setDesktop(component);
        } else {
            editorAreaFrame.setDesktop(component);
        }

    }
    
    
    private List<Component> getComponentAWTHierarchyChain(Component comp) {
        List<Component> l = new ArrayList<Component>();
        Component c = comp;
        while(c != null) {
            l.add(c);
            c = c.getParent();
        }
        
        Collections.reverse(l);
        return l;
    }
    
    private boolean hasEditorAreaVisibleView() {
        //#41875 fix, checking for null EditorView, can be null when using the netbeans.winsys.hideEmptyDocArea command line property
        EditorView view = findEditorAreaElement();
        return (view != null ? (view.getEditorArea() != null) : false);
    }
    
    
    private EditorAreaFrame createEditorAreaFrame() {
        final EditorAreaFrame frame = new EditorAreaFrame();
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                if(frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    // Ignore changes when the frame is in maximized state.
                    return;
                }
                controller.userResizedEditorArea(frame.getBounds());
            }
            
            @Override
            public void componentMoved(ComponentEvent evt) {
                if(frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    // Ignore changes when the frame is in maximized state.
                    return;
                }
                controller.userResizedEditorArea(frame.getBounds());
            }
        });
        frame.setWindowActivationListener(controller);
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                closeEditorModes();
            }
        });
        
        frame.addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent evt) {
     // All the timestamping is a a workaround beause of buggy GNOME and of its kind who iconify the windows on leaving the desktop.
                long currentStamp = System.currentTimeMillis();
                if (currentStamp > (frame.getUserStamp() + 500) && currentStamp > (frame.getMainWindowStamp() + 1000)) {
                    controller.userChangedFrameStateEditorArea(evt.getNewState());
                    long stamp = System.currentTimeMillis();
                    frame.setUserStamp(stamp);
                } else {
                    frame.setUserStamp(0);
                    frame.setMainWindowStamp(0);
                    frame.setExtendedState(evt.getOldState());
                    //frame.setExtendedState(evt.getOldState());
                }
                
            }
        });
        
        return frame;
    }
    
    private void closeEditorModes() {
        closeModeForView(findEditorAreaElement().getEditorArea());
    }
    
    private void closeModeForView(ViewElement view) {
        if(view instanceof ModeView) {
            controller.userClosingMode((ModeView)view);
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            List children = sv.getChildren();
            for( Iterator i=children.iterator(); i.hasNext(); ) {
                ViewElement child = (ViewElement)i.next();
                closeModeForView( child );
            }
        }
    }
    
    
    public void updateEditorAreaBounds(Rectangle bounds) {
        if(editorAreaFrame != null) {
            editorAreaFrame.setBounds(bounds);
        }
    }

    // XXX
    public Rectangle getPureEditorAreaBounds() {
        EditorView editorView = findEditorAreaElement();
        if(editorView == null) {
            return new Rectangle();
        } else {
            return editorView.getPureBounds();
        }
    }
    
    private EditorView findEditorAreaElement() {
        return findEditorViewForElement(getSplitRootElement());
    }
    
    Component getEditorAreaComponent() {
        EditorView editor = findEditorAreaElement();
        if( null != editor )
            return editor.getComponent();
        return null;
    }
    
    private EditorView findEditorViewForElement(ViewElement view) {
        if(view instanceof EditorView) {
            return (EditorView)view;
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            List children = sv.getChildren();
            for( Iterator i=children.iterator(); i.hasNext(); ) {
                ViewElement child = (ViewElement)i.next();
                EditorView ev = findEditorViewForElement( child );
                if( null != ev )
                    return ev;
            }
        }
        
        return null;
    }
    
    public void updateUI() {
        SwingUtilities.updateComponentTreeUI(mainWindow.getFrame());
        if(editorAreaFrame != null) {
            SwingUtilities.updateComponentTreeUI(editorAreaFrame);
        }
        for(ModeView mv: separateModeViews.keySet()) {
            SwingUtilities.updateComponentTreeUI(mv.getComponent());
        }
    }
    
    public Set<TopComponent> getShowingTopComponents() {
        Set<TopComponent> s = new HashSet<TopComponent>();
        for(ElementAccessor accessor: accessor2view.keySet()) {
            if(accessor instanceof ModeAccessor) {
                s.add(((ModeAccessor)accessor).getSelectedTopComponent());
            }
        }
        for(ModeAccessor accessor: separateModeViews.values()) {
            s.add(accessor.getSelectedTopComponent());
        }
        
        return s;
    }
    
    @Override
    public String toString() {
        return dumpElement(getDesktop().getSplitRoot(), 0) + "\nseparateViews=" + separateModeViews.keySet(); // NOI18N
    }
    
    private String dumpElement(ViewElement view, int indent) {
        String indentString = createIndentString(indent);
        StringBuffer sb = new StringBuffer();
        
        if(view instanceof ModeView) {
            sb.append(indentString + view + "->" + view.getComponent().getClass() + "@"  + view.getComponent().hashCode());
        } else if(view instanceof EditorView) {
            sb.append(indentString + view);
            sb.append("\n" + dumpElement(((EditorView)view).getEditorArea(), ++indent));
        } else if(view instanceof SplitView) {
            sb.append(indentString + view + "->" + view.getComponent().getClass() + "@"  + view.getComponent().hashCode());
            indent++;
            List children = ((SplitView)view).getChildren();
            for( Iterator i=children.iterator(); i.hasNext(); ) {
                ViewElement child = (ViewElement)i.next();
                sb.append("\n" + dumpElement(child, indent));
            }
        }
         
        return sb.toString();
    }
    
    private static String createIndentString(int indent) {
        StringBuffer sb = new StringBuffer(indent);
        for(int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        
        return sb.toString();
    }

    private String dumpAccessors() {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<ElementAccessor, ViewElement> entry : accessor2view.entrySet()) {
            sb.append("accessor="+entry.getKey() + "\tview="+entry.getValue() + "\n"); // NOI18N
        }
        
        return sb.toString();
    }

    private void changeStateOfSeparateViews(boolean iconify) {
     // All the timestamping is a a workaround beause of buggy GNOME and of its kind who iconify the windows on leaving the desktop.
        long mainStamp = System.currentTimeMillis();
        if(editorAreaFrame != null) {
            if (iconify) {
                if (mainStamp < (editorAreaFrame.getUserStamp() + 500)) {
                    int newState = editorAreaFrame.getExtendedState() &  ~Frame.ICONIFIED;
                    controller.userChangedFrameStateEditorArea(newState);
                    editorAreaFrame.setExtendedState(newState);
                }
            }
            editorAreaFrame.setMainWindowStamp(mainStamp);
            editorAreaFrame.setVisible(!iconify);
        }
        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            Component comp = mv.getComponent();
            if(comp instanceof Frame) {
                Frame fr = (Frame)comp;
                if (iconify) {
                    if (mainStamp < (mv.getUserStamp() + 500)) {
                        int newState = fr.getExtendedState() &  ~Frame.ICONIFIED;
                        controller.userChangedFrameStateMode(mv,  newState);
                        mv.setFrameState(newState);
                    }
                }
                mv.setMainWindowStamp(mainStamp);
                fr.setVisible(!iconify);
            }
        }
    }

    void userStartedKeyboardDragAndDrop( TopComponentDraggable draggable ) {
        windowDnDManager.startKeyboardDragAndDrop( draggable );
    }

    /** Main window listener. */
    private static class MainWindowListener extends ComponentAdapter
    implements WindowStateListener {
        
        private final Controller controller;
        private final ViewHierarchy hierarchy;
        
        public MainWindowListener(Controller controller, ViewHierarchy hierarchy) {
            this.controller = controller;
            this.hierarchy  = hierarchy;
        }
        
        @Override
        public void componentResized(ComponentEvent evt) {
            controller.userResizedMainWindow(evt.getComponent().getBounds());
        }
        
        @Override
        public void componentMoved(ComponentEvent evt) {
            controller.userMovedMainWindow(evt.getComponent().getBounds());
        }
        
        @Override
        public void windowStateChanged(WindowEvent evt) {
            int oldState = evt.getOldState();
            int newState = evt.getNewState();
            controller.userChangedFrameStateMainWindow(newState);
            
            if (Constants.AUTO_ICONIFY) {
                if (((oldState & Frame.ICONIFIED) == 0) &&
                    ((newState & Frame.ICONIFIED) == Frame.ICONIFIED)) {
                    hierarchy.changeStateOfSeparateViews(true);
                } else if (((oldState & Frame.ICONIFIED) == Frame.ICONIFIED) && 
                           ((newState & Frame.ICONIFIED) == 0 )) {
                    hierarchy.changeStateOfSeparateViews(false);
                }
            }
        }
    } // End of main window listener.
    
    
    private static void debugLog(String message) {
        Debug.log(ViewHierarchy.class, message);
    }

    private boolean shouldUseFakeSplitRoot() {
        return Constants.SWITCH_HIDE_EMPTY_DOCUMENT_AREA;
    }

    private ViewElement fakeSplitRoot;

    //#209678 - with option SWITCH_HIDE_EMPTY_DOCUMENT_AREA closing all editor
    //windows will hide all sliding bars, so let's use a dummy split root
    //to keep sliding bars visible when no window is docked
    private ViewElement getFakeSplitRoot() {
        if( null == fakeSplitRoot ) {
            final JPanel panel = new JPanel();
            panel.setOpaque( false );
            fakeSplitRoot = new ViewElement( controller, 1.0 ) {

                @Override
                public Component getComponent() {
                    return panel;
                }

                @Override
                public boolean updateAWTHierarchy( Dimension availableSpace ) {
                    return false;
                }
            };
        }
        return fakeSplitRoot;
    }

    private DesktopImpl getDesktop() {
        synchronized( this ) {
            if( null == desktop ) {
                desktop = new DesktopImpl();
            }
        }
        return desktop;
    }
}

