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


package org.netbeans.core.windows.view.ui;


import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import java.text.MessageFormat;
import javax.swing.plaf.basic.BasicHTML;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.ViewElement;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.dnd.ZOrderManager;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.options.WinSysPrefs;
import org.netbeans.core.windows.view.dnd.TopComponentDraggable;
import org.netbeans.swing.tabcontrol.customtabs.TabbedComponentFactory;
import org.netbeans.swing.tabcontrol.customtabs.TabbedType;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;


/** 
 * Implementation of <code>ModeContainer</code> for separate mode kind.
 *
 * @author  Peter Zavadsky
 */
public final class DefaultSeparateContainer extends AbstractModeContainer {

    /** Separate mode represented by JFrame or null if dialog is used */
    private final ModeFrame modeFrame;
    /** Separate mode represented by JDialog or null if frame is used */
    private final ModeDialog modeDialog;

    /** Creates a DefaultSeparateContainer. */
    public DefaultSeparateContainer(final ModeView modeView, WindowDnDManager windowDnDManager, Rectangle bounds, int kind) {
        super(modeView, windowDnDManager, kind);
        // JFrame or JDialog according to the mode kind
        if (kind == Constants.MODE_KIND_EDITOR) {
            modeFrame = new ModeFrame(this, modeView);
            MainWindow.initFrameIcons(modeFrame);
            modeDialog = null;
        } else {
            modeDialog = new ModeDialog(WindowManager.getDefault().getMainWindow(), this, modeView);
            modeFrame = null;
        }
        Window w = getModeUIWindow();
        ((RootPaneContainer) w).getContentPane().add(tabbedHandler.getComponent());
        w.setBounds(bounds);
            }
    
    @Override
    public void requestAttention (TopComponent tc) {
        //not implemented
    }
    
    @Override
    public void cancelRequestAttention (TopComponent tc) {
        //not implemented
    }

    public void setAttentionHighlight(TopComponent tc, boolean highlight) {
        tabbedHandler.setAttentionHighlight( tc, highlight );
    }

    @Override
    public void makeBusy(TopComponent tc, boolean busy) {
        tabbedHandler.makeBusy( tc, busy );
    }

    /** */
    @Override
    protected Component getModeComponent() {
        return getModeUIWindow();
    }
    
    @Override
    protected Tabbed createTabbed() {
        TabbedComponentFactory factory = Lookup.getDefault().lookup(TabbedComponentFactory.class);
        TabbedType type = getKind() == Constants.MODE_KIND_EDITOR ? TabbedType.EDITOR : TabbedType.VIEW;
        return factory.createTabbedComponent( type, new TabbedAdapter.WinsysInfo(getKind()));
    }    
    
    @Override
    protected void updateTitle (String title) {
        getModeUIBase().updateTitle(title);
    }
    
    @Override
    protected void updateActive (boolean active) {
        Window w = getModeUIWindow();
        if(active) {
            if (w.isVisible() && !w.isActive()) {
                w.toFront();
            }
        } 
    }
    
    @Override
    public boolean isActive () {
        return getModeUIWindow().isActive();
    }
    
    @Override
    protected boolean isAttachingPossible() {
        return false;
    }
    
    @Override
    protected TopComponentDroppable getModeDroppable() {
        return getModeUIBase();
    }

    private Window getModeUIWindow () {
        return modeFrame != null ? modeFrame : modeDialog;
    }

    private ModeUIBase getModeUIBase () {
        return (ModeUIBase)getModeUIWindow();
    }

    /** Separate mode UI backed by JFrame.
     *
     * [dafe] Whole DnD of window system expects that ModeComponent and
     * TopComponentDroppable implementation must exist in AWT hierarchy,
     * so I have to extend Swing class here, not just use it. That's why all this
     * delegating stuff.     
     */
    private static class ModeFrame extends JFrame implements ModeUIBase {

        /** Base helper to delegate to for common things */
        private SharedModeUIBase modeBase;
   
        public ModeFrame (AbstractModeContainer abstractModeContainer, ModeView view) {
            super();
            // To be able to activate on mouse click.
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
            modeBase = new SharedModeUIBaseImpl(abstractModeContainer, view, this);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }

        @Override
        public ModeView getModeView() {
            return modeBase.getModeView();
        }

        @Override
        public int getKind() {
            return modeBase.getKind();
        }

        @Override
        public Shape getIndicationForLocation(Point location) {
            return modeBase.getIndicationForLocation(location);
        }

        @Override
        public Object getConstraintForLocation(Point location) {
            return modeBase.getConstraintForLocation(location);
        }

        @Override
        public Component getDropComponent() {
            return modeBase.getDropComponent();
        }

        @Override
        public ViewElement getDropViewElement() {
            return modeBase.getDropViewElement();
        }

        @Override
        public boolean canDrop(TopComponentDraggable transfer, Point location) {
            return modeBase.canDrop(transfer, location);
        }

        @Override
        public boolean supportsKind(TopComponentDraggable transfer) {
            return modeBase.supportsKind(transfer);
        }

        /** Actually sets title for the frame
         */
        @Override
        public void updateTitle(String title) {
            // extract HTML from text - Output window (and soon others) uses it
            if (BasicHTML.isHTMLString(title)) {
                char[] c = title.toCharArray();
                StringBuffer sb = new StringBuffer(title.length());
                boolean inTag = false;
                for (int i=0; i < c.length; i++) {
                    if (inTag && c[i] == '>') { //NOI18N
                        inTag = false;
                        continue;
                    }
                    if (!inTag && c[i] == '<') { //NOI18N
                        inTag = true;
                        continue;
                    }
                    if (!inTag) {
                        sb.append(c[i]);
                    }
                }
                //XXX, would be nicer to support the full complement of entities...
                title = sb.toString().replace("&nbsp;", " "); //NOI18N
            }
            String completeTitle = MessageFormat.format(
                    NbBundle.getMessage(DefaultSeparateContainer.class, "CTL_SeparateEditorTitle"),
                    title);
            setTitle(completeTitle);
        }

    } // end of ModeFrame

    /** Separate mode UI backed by JFrame.
     *
     * [dafe] Whole DnD of window system expects that ModeComponent and
     * TopComponentDroppable implementation must exist in AWT hierarchy,
     * so I have to extend Swing class here, not just use it. That's why all this
     * delegating stuff.
     */     
    private static class ModeDialog extends JDialog implements ModeUIBase {
        
        /** Base helper to delegate to for common things */
        private SharedModeUIBase modeBase;
        private WindowSnapper snapper;
        private boolean ignoreMovedEvents = false;
        
        public ModeDialog (Frame owner, AbstractModeContainer abstractModeContainer, ModeView view) {
            super(owner);
            // To be able to activate on mouse click.
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
            modeBase = new SharedModeUIBaseImpl(abstractModeContainer, view, this);
            
            try {
                snapper = new WindowSnapper();
            } catch (AWTException e) {
                snapper = null;
                Logger.getLogger( ModeDialog.class.getName() ).log( Level.INFO, null, e );
            }
            addComponentListener( new ComponentAdapter() {
                @Override
                public void componentMoved( ComponentEvent ce ) {
                    if( ignoreMovedEvents || null == snapper || 
                            !WinSysPrefs.HANDLER.getBoolean(WinSysPrefs.SNAPPING, true) )
                        return;
                    
                    snapWindow();
                    
                    snapper.cursorMoved();
                }
            });
            setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        }
        
        private void snapWindow() {
            Rectangle myBounds = getBounds();

            WindowManagerImpl wm = WindowManagerImpl.getInstance();
            Set<? extends ModeImpl> modes = wm.getModes();
            for( ModeImpl m : modes ) {
                if( m.getState() != Constants.MODE_STATE_SEPARATED )
                    continue;
                TopComponent tc = m.getSelectedTopComponent();
                if( null == tc )
                    continue;
                Window w = SwingUtilities.getWindowAncestor( tc );
                if( w == ModeDialog.this )
                    continue;
                Rectangle targetBounds = w.getBounds();
                if( snapper.snapTo(myBounds, targetBounds) )
                    return;
            }

            if( WinSysPrefs.HANDLER.getBoolean(WinSysPrefs.SNAPPING_SCREENEDGES, true) ) {
                snapper.snapToScreenEdges(myBounds);
            }
        }

        @Override
        public ModeView getModeView() {
            return modeBase.getModeView();
        }

        @Override
        public int getKind() {
            return modeBase.getKind();
        }

        @Override
        public Shape getIndicationForLocation(Point location) {
            return modeBase.getIndicationForLocation(location);
        }

        @Override
        public Object getConstraintForLocation(Point location) {
            return modeBase.getConstraintForLocation(location);
        }

        @Override
        public Component getDropComponent() {
            return modeBase.getDropComponent();
        }

        @Override
        public ViewElement getDropViewElement() {
            return modeBase.getDropViewElement();
        }

        @Override
        public boolean canDrop(TopComponentDraggable transfer, Point location) {
            return modeBase.canDrop(transfer, location);
        }

        @Override
        public boolean supportsKind(TopComponentDraggable transfer) {
            return modeBase.supportsKind(transfer);
        }

        @Override
        public void updateTitle(String title) {
            // noop - no title for dialogs
        }

        @Override
        public void setBounds( int x, int y, int w, int h ) {
            ignoreMovedEvents = true;
            super.setBounds(x,y,w,h);
            ignoreMovedEvents = false;
        }
        
        @Override
        public void setBounds( Rectangle r ) {
            ignoreMovedEvents = true;
            super.setBounds(r);
            ignoreMovedEvents = false;
        }
        
        @Override
        public void setLocation( Point p ) {
            ignoreMovedEvents = true;
            super.setLocation( p );
            ignoreMovedEvents = false;
        }
        
        @Override
        public void setLocation( int x, int y ) {
            ignoreMovedEvents = true;
            super.setLocation( x, y );
            ignoreMovedEvents = false;
        }
    } // end of ModeDialog

    /** Defines shared common attributes of UI element for separate mode. */
    public interface SharedModeUIBase extends ModeComponent, TopComponentDroppable {
    }

    /** Defines base of UI element for separate mode, containing extras
     * in which JDialog and JFrame separate mode differs
     */
    public interface ModeUIBase extends ModeComponent, TopComponentDroppable {
        public void updateTitle (String title);
    }

    /** Base impl of separate UI element, used as delegatee for shared things.
     */
    private static class SharedModeUIBaseImpl implements SharedModeUIBase {
        
        private final AbstractModeContainer abstractModeContainer;
        private final ModeView modeView;
        private long frametimestamp = 0;

        /** UI representation of separate window */
        private Window window;
        
        public SharedModeUIBaseImpl (AbstractModeContainer abstractModeContainer, ModeView view, Window window) {
            this.abstractModeContainer = abstractModeContainer;
            this.modeView = view;
            this.window = window;
            initWindow(window);
            attachListeners(window);
        }

        /** Creates and returns window appropriate for type of dragged TC;
         * either frame or dialog.
         */
        private void initWindow (Window w) {
            // mark this as separate window, so that ShortcutAndMenuKeyEventProcessor
            // allows normal shorcut processing like inside main window
            ((RootPaneContainer)w).getRootPane().putClientProperty(
                    Constants.SEPARATE_WINDOW_PROPERTY, Boolean.TRUE);

            // register in z-order mng
            ZOrderManager.getInstance().attachWindow((RootPaneContainer)w);
        }

        private void attachListeners (Window w) {
            w.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evt) {
                    WindowManagerImpl wm = WindowManagerImpl.getInstance();
                    for( TopComponent tc : modeView.getTopComponents() ) {
                        if( !Switches.isEditorTopComponentClosingEnabled() && wm.isEditorTopComponent( tc ) )
                            return;
                        if( !Switches.isViewTopComponentClosingEnabled() && !wm.isEditorTopComponent( tc ) )
                            return;
                        if( !Switches.isClosingEnabled( tc ) )
                            return;
                        if( !tc.close() )
                            return;
                    }
                    modeView.getController().userClosingMode(modeView);
                    ZOrderManager.getInstance().detachWindow((RootPaneContainer)window);
                }

                @Override
                public void windowClosed (WindowEvent evt) {
                    ZOrderManager.getInstance().detachWindow((RootPaneContainer)window);
                }

                @Override
                public void windowActivated(WindowEvent event) {
                    if (frametimestamp != 0 && System.currentTimeMillis() > frametimestamp + 500) {
                        modeView.getController().userActivatedModeWindow(modeView);
                    }
                    frametimestamp = System.currentTimeMillis();
                }
                @Override
                public void windowOpened(WindowEvent event) {
                    frametimestamp = System.currentTimeMillis();
                }
            });  // end of WindowListener

            w.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent evt) {
                    /*if(DefaultSeparateContainer.this.frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                        // Ignore changes when the frame is in maximized state.
                        return;
                    }*/

                    modeView.getController().userResizedModeBounds(modeView, window.getBounds());
                }

                @Override
                public void componentMoved(ComponentEvent evt) {
                    /*if(DefaultSeparateContainer.this.frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                        // Ignore changes when the frame is in maximized state.
                        return;
                    }*/

                    modeView.getController().userResizedModeBounds(modeView, window.getBounds());
                }

            }); // end of ComponentListener
        
        
            window.addWindowStateListener(new WindowStateListener() {
                @Override
                public void windowStateChanged(WindowEvent evt) {
                    if (!Constants.AUTO_ICONIFY) {
                        modeView.getController().userChangedFrameStateMode(modeView, evt.getNewState());
                    } else {
                         // All the timestamping is a a workaround beause of buggy GNOME
                        // and of its kind who iconify the windows on leaving the desktop.
                        Component comp = modeView.getComponent();
                        if (comp instanceof Frame /*&& comp.isVisible() */) {
                            long currentStamp = System.currentTimeMillis();
                            if (currentStamp > (modeView.getUserStamp() + 500) && currentStamp > (modeView.getMainWindowStamp() + 1000)) {
                                modeView.getController().userChangedFrameStateMode(modeView, evt.getNewState());
                            } else {
                                modeView.setUserStamp(0);
                                modeView.setMainWindowStamp(0);
                                modeView.updateFrameState();
                            }
                            long stamp = System.currentTimeMillis();
                            modeView.setUserStamp(stamp);
                        }
                    }
                }
            }); // end of WindowStateListener

        }

        public void setVisible(boolean visible) {
            frametimestamp = System.currentTimeMillis();
            window.setVisible(visible);
        }
        
        public void toFront() {
            frametimestamp = System.currentTimeMillis();
            window.toFront();
        }
        
        @Override
        public ModeView getModeView() {
            return abstractModeContainer.getModeView();
        }
        
        @Override
        public int getKind() {
            return abstractModeContainer.getKind();
        }

        // TopComponentDroppable>>
        @Override
        public Shape getIndicationForLocation(Point location) {
            return abstractModeContainer.getIndicationForLocation(location);
        }
        
        @Override
        public Object getConstraintForLocation(Point location) {
            return abstractModeContainer.getConstraintForLocation(location);
        }
        
        @Override
        public Component getDropComponent() {
            return abstractModeContainer.getDropComponent();
        }
        
        @Override
        public ViewElement getDropViewElement() {
            return abstractModeContainer.getDropModeView();
        }
        
        @Override
        public boolean canDrop(TopComponentDraggable transfer, Point location) {
            return abstractModeContainer.canDrop(transfer);
        }
        
        @Override
        public boolean supportsKind(TopComponentDraggable transfer) {
            // this is not a typo, yes it should be the same as canDrop
            return abstractModeContainer.canDrop(transfer);
            //return true;
            /*
             if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
            || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
                return true;
            }

            return kind == Constants.MODE_KIND_VIEW || kind == Constants.MODE_KIND_SLIDING;
             */
        }
        // TopComponentDroppable<<


    } // End of ModeWindow.
    
}

