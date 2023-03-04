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

package org.netbeans.modules.java.navigation;

import java.awt.event.*;
import java.awt.*;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Customized copy of javax.swing.ToolTipManager
 * 
 * @author S. Aubrecht
 */
final class ToolTipManagerEx extends MouseAdapter implements MouseMotionListener, Callable<Boolean>  {
    
    private static final Logger LOG = Logger.getLogger(ToolTipManagerEx.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(ToolTipManagerEx.class.getName(), 1, false, false);
    
    private Timer enterTimer;
    private Timer  exitTimer;
    private String toolTipText;
    private JComponent insideComponent;
    private MouseEvent mouseEvent;
    private boolean showImmediately;
    private transient Popup tipWindow;
    private volatile boolean cancelled;
    /** The Window tip is being displayed in. This will be non-null if
     * the Window tip is in differs from that of insideComponent's Window.
     */
    private Window window;
    private ToolTipEx tip;

    private Rectangle popupRect = null;

    boolean enabled = true;
    private boolean tipShowing = false;
   
    private MouseMotionListener moveBeforeEnterListener = null;

    private ToolTipProvider provider;
    
    private static final String WAITING_TEXT = NbBundle.getMessage( ToolTipManagerEx.class, "LBL_PleaseWait" ); //NOI18N
    
    private AWTEventListener awtListener;
    
    /** holds last object for which the tooltip was built */
    private Rectangle lastTooltipForRect;
    private String lastTooltipText;
    /** task that calculates tooltip */
    private RequestProcessor.Task tooltipTask;
    /** data lock for tooltip calculations */
    private static final Object TOOLTIP_DATA_LOCK = new Object();

    static interface ToolTipProvider {
        JComponent getComponent();
        Rectangle getToolTipSourceBounds( Point loc );
        Point getToolTipLocation( Point mouseLocation, Dimension toolTipSize );
        void invokeUserAction( MouseEvent me );
        @CheckForNull
        Node findNode(@NonNull Point loc);
        @CheckForNull
        String getToolTipText(@NonNull Node node);
    }

    public ToolTipManagerEx( ToolTipProvider provider ) {
        assert null != provider;
        this.provider = provider;
        
        enterTimer = new Timer(750, new insideTimerAction());
        enterTimer.setRepeats(false);
        exitTimer = new Timer(500, new outsideTimerAction());
        exitTimer.setRepeats(false);

	moveBeforeEnterListener = new MoveBeforeEnterListener();
        
        registerComponent( provider.getComponent() );
    }

    /**
     * Enables or disables the tooltip.
     *
     * @param flag  true to enable the tip, false otherwise
     */
    public void setEnabled(boolean flag) {
        enabled = flag;
        if (!flag) {
            hideTipWindow();
        }
    }

    /**
     * Returns true if this object is enabled.
     *
     * @return true if this object is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }


    /**
     * Specifies the initial delay value.
     *
     * @param milliseconds  the number of milliseconds to delay
     *        (after the cursor has paused) before displaying the
     *        tooltip
     * @see #getInitialDelay
     */
    public void setInitialDelay(int milliseconds) {
        enterTimer.setInitialDelay(milliseconds);
    }

    /**
     * Returns the initial delay value.
     *
     * @return an integer representing the initial delay value,
     *		in milliseconds
     * @see #setInitialDelay
     */
    public int getInitialDelay() {
        return enterTimer.getInitialDelay();
    }

    /**
     * Used to specify the amount of time before the user has to wait
     * <code>initialDelay</code> milliseconds before a tooltip will be
     * shown. That is, if the tooltip is hidden, and the user moves into
     * a region of the same Component that has a valid tooltip within
     * <code>milliseconds</code> milliseconds the tooltip will immediately
     * be shown. Otherwise, if the user moves into a region with a valid
     * tooltip after <code>milliseconds</code> milliseconds, the user
     * will have to wait an additional <code>initialDelay</code>
     * milliseconds before the tooltip is shown again.
     *
     * @param milliseconds time in milliseconds
     * @see #getReshowDelay
     */
    public void setReshowDelay(int milliseconds) {
        exitTimer.setInitialDelay(milliseconds);
    }

    /**
     * Returns the reshow delay property.
     *
     * @return reshown delay property
     * @see #setReshowDelay
     */
    public int getReshowDelay() {
        return exitTimer.getInitialDelay();
    }
    
    @Override
    public Boolean call() throws Exception {
        return cancelled;
    }

    protected void showTipWindow() {
        if(insideComponent == null || !insideComponent.isShowing())
            return;
        cancelled = false;
        LOG.fine("cancelled=false");    //NOI18N
	for (Container p = insideComponent.getParent(); p != null; p = p.getParent()) {
            if (p instanceof JPopupMenu) break;
	    if (p instanceof Window) {
		if (!((Window)p).isFocused()) {
		    return;
		}
		break;
	    }
	}
        if (enabled) {
            Dimension size;
            
            // Just to be paranoid
            hideTipWindow();

            tip = createToolTip();
            tip.setTipText(toolTipText);
            size = tip.getPreferredSize();

            Point location = provider.getToolTipLocation( mouseEvent.getPoint(), size );

	    // we do not adjust x/y when using awt.Window tips
	    if (popupRect == null){
		popupRect = new Rectangle();
	    }
	    popupRect.setBounds( location.x, location.y, size.width, size.height );
	    
            PopupFactory popupFactory = PopupFactory.getSharedInstance();

	    tipWindow = popupFactory.getPopup(insideComponent, tip,
					      location.x,
					      location.y);
	    tipWindow.show();

            Window componentWindow = SwingUtilities.windowForComponent(
                                                    insideComponent);

            window = SwingUtilities.windowForComponent(tip);
            if (window != null && window != componentWindow) {
                window.addMouseListener(this);
            }
            else {
                window = null;
            }

            Toolkit.getDefaultToolkit().addAWTEventListener( getAWTListener(), AWTEvent.KEY_EVENT_MASK );
	    tipShowing = true;
        }
    }
    
    protected void hideTipWindow() {
        if (tipWindow != null) {
            if (window != null) {
                window.removeMouseListener(this);
                window = null;
            }
            cancelled = true;
            LOG.fine("cancelled=true");    //NOI18N
            tipWindow.hide();
	    tipWindow = null;
	    tipShowing = false;
	    (tip.getUI()).uninstallUI(tip);
            tip = null;
            if( null != awtListener )
                Toolkit.getDefaultToolkit().removeAWTEventListener( getAWTListener() );
        }
    }
    
    // add keylistener here to trigger tip for access
    /**
     * Registers a component for tooltip management.
     * <p>
     * This will register key bindings to show and hide the tooltip text
     * only if <code>component</code> has focus bindings. This is done
     * so that components that are not normally focus traversable, such
     * as <code>JLabel</code>, are not made focus traversable as a result
     * of invoking this method.
     *
     * @param component  a <code>JComponent</code> object to add
     * @see JComponent#isFocusTraversable
     */
    protected void registerComponent(JComponent component) {
        component.removeMouseListener(this);
        component.addMouseListener(this);
        component.removeMouseMotionListener(moveBeforeEnterListener);
	component.addMouseMotionListener(moveBeforeEnterListener);

	if (shouldRegisterBindings(component)) {
	    // register our accessibility keybindings for this component
	    // this will apply globally across L&F
	    // Post Tip: Ctrl+F1
	    // Unpost Tip: Esc and Ctrl+F1
	    InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
	    ActionMap actionMap = component.getActionMap();

	    if (inputMap != null && actionMap != null) {
                //XXX remove
	    }
	}
    }

    /**
     * Removes a component from tooltip control.
     *
     * @param component  a <code>JComponent</code> object to remove
     */
    protected void unregisterComponent(JComponent component) {
        component.removeMouseListener(this);
	component.removeMouseMotionListener(moveBeforeEnterListener);

	if (shouldRegisterBindings(component)) {
	    InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
	    ActionMap actionMap = component.getActionMap();

	    if (inputMap != null && actionMap != null) {
                //XXX remove
	    }
	}
    }

    /**
     * Returns whether or not bindings should be registered on the given
     * <code>JComponent</code>. This is implemented to return true if the
     * tool tip manager has a binding in any one of the
     * <code>InputMaps</code> registered under the condition
     * <code>WHEN_FOCUSED</code>.
     * <p>
     * This does not use <code>isFocusTraversable</code> as
     * some components may override <code>isFocusTraversable</code> and
     * base the return value on something other than bindings. For example,
     * <code>JButton</code> bases its return value on its enabled state.
     *
     * @param component  the <code>JComponent</code> in question
     */
    private boolean shouldRegisterBindings(JComponent component) {
	InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
	while (inputMap != null && inputMap.size() == 0) {
	    inputMap = inputMap.getParent();
	}
	return (inputMap != null);
    }

    // implements java.awt.event.MouseListener
    /**
     *  Called when the mouse enters the region of a component.
     *  This determines whether the tool tip should be shown.
     *
     *  @param event  the event in question
     */
    public @Override void mouseEntered(MouseEvent event) {
        initiateToolTip(event);
    }

    private void initiateToolTip(MouseEvent event) {
        if (event.getSource() == window) {
            return;
        }
        JComponent component = (JComponent)event.getSource();
	component.removeMouseMotionListener(moveBeforeEnterListener);

        exitTimer.stop();

	Point location = event.getPoint();
	// ensure tooltip shows only in proper place
	if (location.x < 0 || 
	    location.x >=component.getWidth() ||
	    location.y < 0 ||
	    location.y >= component.getHeight()) {
	    return;
	}

        if (insideComponent != null) {
            enterTimer.stop();
        }
	// A component in an unactive internal frame is sent two
	// mouseEntered events, make sure we don't end up adding
	// ourselves an extra time.
        component.removeMouseMotionListener(this);
        component.addMouseMotionListener(this);

        boolean sameComponent = (insideComponent == component);

        insideComponent = component;
	if (tipWindow != null){
            mouseEvent = event;
            if (showImmediately) {
                Rectangle rect = provider.getToolTipSourceBounds( event.getPoint() );
                if( null != rect ) {
                    String newToolTipText = startToolTipCalculation( rect, event.getPoint() );

                    if (!sameComponent || !toolTipText.equals(newToolTipText) /*|| 
                             !sameLoc*/) {
                        toolTipText = newToolTipText;
                        showTipWindow();
                    }
                }
            } else {
                enterTimer.start();
            }
        }
    }

    // implements java.awt.event.MouseListener
    /**
     *  Called when the mouse exits the region of a component.
     *  Any tool tip showing should be hidden.
     *
     *  @param event  the event in question
     */
    public @Override void mouseExited(MouseEvent event) {
        boolean shouldHide = true;
        if (insideComponent == null) {
            // Drag exit
        } 
        else if (window != null && event.getSource() == window) {
	  // if we get an exit and have a heavy window
	  // we need to check if it if overlapping the inside component
            Container insideComponentWindow = insideComponent.getTopLevelAncestor();
            if (insideComponentWindow != null) {
                Point location = event.getPoint();
                SwingUtilities.convertPointToScreen(location, window);

                location.x -= insideComponentWindow.getX();
                location.y -= insideComponentWindow.getY();

                location = SwingUtilities.convertPoint(null,location,insideComponent);
                if (location.x >= 0 && location.x < insideComponent.getWidth() &&
                   location.y >= 0 && location.y < insideComponent.getHeight()) {
                    shouldHide = false;
                } else {
                    shouldHide = true;
                }
            }
        } else if(event.getSource() == insideComponent && tipWindow != null) {
	    Window win = SwingUtilities.getWindowAncestor(insideComponent);
	    if (win != null) {	// insideComponent may have been hidden (e.g. in a menu)
		Point location = SwingUtilities.convertPoint(insideComponent,
							     event.getPoint(),
							     win);
		Rectangle bounds = insideComponent.getTopLevelAncestor().getBounds();
		location.x += bounds.x;
		location.y += bounds.y;
		
		Point loc = new Point(0, 0);
		SwingUtilities.convertPointToScreen(loc, tip);
		bounds.x = loc.x;
		bounds.y = loc.y;
		bounds.width = tip.getWidth();
		bounds.height = tip.getHeight();
// issue #158925, no need to preserve window if mouse entered in.
//		if (location.x >= bounds.x && location.x < (bounds.x + bounds.width) &&
//		    location.y >= bounds.y && location.y < (bounds.y + bounds.height)) {
//		    shouldHide = false;
//		} else {
		    shouldHide = true;
//		}
	    }
        } 
        
        if (shouldHide) {        
            enterTimer.stop();
	    if (insideComponent != null) {
	        insideComponent.removeMouseMotionListener(this);
	    }
            insideComponent = null;
            toolTipText = null;
            mouseEvent = null;
            hideTipWindow();
            exitTimer.restart();
        }
    }

    // implements java.awt.event.MouseListener
    /**
     *  Called when the mouse is pressed.
     *  Any tool tip showing should be hidden.
     *
     *  @param event  the event in question
     */
    public @Override void mousePressed(MouseEvent event) {
        hideTipWindow();
        enterTimer.stop();
        showImmediately = false;
        insideComponent = null;
        mouseEvent = null;
    }

    // implements java.awt.event.MouseMotionListener
    /**
     *  Called when the mouse is pressed and dragged.
     *  Does nothing.
     *
     *  @param event  the event in question
     */
    public void mouseDragged(MouseEvent event) {
    }

    // implements java.awt.event.MouseMotionListener
    /**
     *  Called when the mouse is moved.
     *  Determines whether the tool tip should be displayed.
     *
     *  @param event  the event in question
     */
    public void mouseMoved(MouseEvent event) {
        if (tipShowing) {
            checkForTipChange(event);
        }
        else if (showImmediately) {
            Rectangle rect = provider.getToolTipSourceBounds( event.getPoint() );
            if( null != rect ) {
                JComponent component = (JComponent)event.getSource();
                toolTipText = startToolTipCalculation(rect, event.getPoint());
                if (toolTipText != null) {
                    mouseEvent = event;
                    insideComponent = component;
                    exitTimer.stop();
                    showTipWindow();
                }
            }
        }
        else {
            // Lazily lookup the values from within insideTimerAction
            insideComponent = (JComponent)event.getSource();
            mouseEvent = event;
            toolTipText = null;
            enterTimer.restart();
        }
    }

    /**
     * Checks to see if the tooltip needs to be changed in response to
     * the MouseMoved event <code>event</code>.
     */
    private void checkForTipChange(MouseEvent event) {
        JComponent component = (JComponent)event.getSource();
        Rectangle newRect = provider.getToolTipSourceBounds( event.getPoint() );//component.getToolTipLocation(event);

        if ( newRect != null) {
            mouseEvent = event;
            if ( newRect.equals( lastTooltipForRect ) ) {
                if (tipWindow == null) {
                    enterTimer.restart();
                }
            } else {
                toolTipText = startToolTipCalculation(newRect, event.getPoint());
                if (showImmediately) {
                    hideTipWindow();
                    showTipWindow();
                    exitTimer.stop();
                } else {
                    enterTimer.restart();
                }
            }
        } else {
            toolTipText = null;
            mouseEvent = null;
            insideComponent = null;
            hideTipWindow();
            enterTimer.stop();
            exitTimer.restart();
        }
    }

    protected class insideTimerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(insideComponent != null && insideComponent.isShowing()) {
                // Lazy lookup
                if (toolTipText == null && mouseEvent != null) {
                    Rectangle rect = provider.getToolTipSourceBounds( mouseEvent.getPoint() );
                    if( null != rect ) {
                        toolTipText = startToolTipCalculation(rect, mouseEvent.getPoint());
                    }
                }
                if(toolTipText != null) {
                    showImmediately = true;
                    showTipWindow();
                }
                else {
                    insideComponent = null;
                    toolTipText = null;
                    mouseEvent = null;
                    hideTipWindow();
                }
            }
        }
    }

    protected class outsideTimerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            showImmediately = false;
        }
    }

  /* This listener is registered when the tooltip is first registered
   * on a component in order to catch the situation where the tooltip
   * was turned on while the mouse was already within the bounds of
   * the component.  This way, the tooltip will be initiated on a
   * mouse-entered or mouse-moved, whichever occurs first.  Once the
   * tooltip has been initiated, we can remove this listener and rely
   * solely on mouse-entered to initiate the tooltip.
   */
    private class MoveBeforeEnterListener extends MouseMotionAdapter {
        public @Override void mouseMoved(MouseEvent e) {
	    initiateToolTip(e);
	}
    }

    protected ToolTipEx createToolTip() {
        return new ToolTipEx();
    }
    
    protected AWTEventListener getAWTListener() {
        if( null == awtListener ) {
            awtListener = new AWTEventListener() {
                boolean armed = false;
                public void eventDispatched( AWTEvent e ) {
                    if( e instanceof KeyEvent ) {
                        KeyEvent ke = (KeyEvent)e;
                        if( ke.getKeyCode() == KeyEvent.VK_F1 && (ke.isControlDown() || ke.isMetaDown()) ) {
                            if( ke.getID() == KeyEvent.KEY_PRESSED ) {
                                armed = true;
                                return;
                            } else if( ke.getID() == KeyEvent.KEY_RELEASED && armed ) {
                                ke.consume();
                                armed = false;
                                provider.invokeUserAction( mouseEvent );
                                hideTipWindow();
                                return;
                            }
                        } else if( !(ke.getKeyCode() == KeyEvent.VK_CONTROL || ke.getKeyCode() == KeyEvent.VK_META) ) {
                            armed = false;
                        }
                    }
                }
            };
        }
        return awtListener;
    }
    
    private String startToolTipCalculation( Rectangle tooltipForRect, Point loc ) {
        synchronized (TOOLTIP_DATA_LOCK) {
            if( tooltipForRect.equals( lastTooltipForRect ) ) {
                // no further activity, because tooltip is just being calculated or already displayed
                return lastTooltipText;
            }
            // cancel previous now invalid task
            if (tooltipTask != null) {
                boolean cancelled = tooltipTask.cancel();
                tooltipTask = null;
            }
            lastTooltipForRect = new Rectangle( tooltipForRect );
        }
        // start full tooltip calculation in request processor
        final TooltipCalculator tc = new TooltipCalculator( tooltipForRect, loc );
        if (tc.isValid()) {
            synchronized (TOOLTIP_DATA_LOCK) {
                tooltipTask = RP.post(tc);
            }
            return WAITING_TEXT;
        } else {
            return null;
        }
    }

    /** calculates tooltip and invokes tooltip refresh */
    private final class TooltipCalculator implements Runnable {
        
        private Node node;
        private Rectangle tooltipForRect;
        
        TooltipCalculator( Rectangle tooltipForRect, Point loc ) {
            this.tooltipForRect = tooltipForRect;
            this.node = provider.findNode(loc);
        }

        boolean isValid() {
            return this.node != null;
        }

        /** actually calculates tooltip for given item */
        @Override
        public void run () {
            final String result = provider.getToolTipText(node);
            if( null == result ) {
                return;
            }
            synchronized (TOOLTIP_DATA_LOCK) {
                tooltipTask = null;
                // cancel if not needed (tooltip for another object was requested later)
                if( lastTooltipForRect == null || !tooltipForRect.equals( lastTooltipForRect ) ) {
                    return;
                }
                lastTooltipText = result;
            }
            // invoke tooltip
            SwingUtilities.invokeLater(()-> {
                toolTipText = result;
                if( null != tip ) {
                    tip.setTipText(toolTipText);
                    tip.invalidate();
                    tip.revalidate();
                    tip.repaint();
                }
            });
        }
    }
    
    private Dimension getDefaultToolTipSize() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        String size = prefs.get(SimpleValueNames.JAVADOC_PREFERRED_SIZE, null);
        Dimension dim = size == null ? null : parseDimension(size);
        return dim != null ? dim : new Dimension(500,300);
    }
    
    private static Dimension parseDimension(String s) {
        StringTokenizer st = new StringTokenizer(s, ","); // NOI18N

        int arr[] = new int[2];
        int i = 0;
        while (st.hasMoreElements()) {
            if (i > 1) {
                return null;
            }
            try {
                arr[i] = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException nfe) {
                LOG.log(Level.WARNING, null, nfe);
                return null;
            }
            i++;
        }
        if (i != 2) {
            return null;
        } else {
            return new Dimension(arr[0], arr[1]);
        }
    }
    
    private class ToolTipEx extends JPanel {
        private HTMLDocView content;
        private JLabel shortcut;
        public ToolTipEx() {
            super( new GridBagLayout() );
            setPreferredSize( getDefaultToolTipSize() );
            // Color background = getDefaultToolTipBackground();
            
            Color background = new JEditorPane().getBackground();
            background = new Color(
                    Math.max(background.getRed() - 8, 0 ), 
                    Math.max(background.getGreen() - 8, 0 ), 
                    background.getBlue());
            
            setBackground( background );
            content = new HTMLDocView( background );
            JScrollPane scroll = new JScrollPane( content );
            scroll.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
            add( scroll, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
            shortcut = new JLabel( NbBundle.getMessage( ToolTipManagerEx.class, "HINT_EnlargeJavaDocToolip", //NOI18N
                    Utilities.isMac() ? KeyEvent.getKeyText(KeyEvent.VK_META)+"+F1" : "Ctrl+F1" ) ); //NOI18N //NOI18N
            shortcut.setHorizontalAlignment( JLabel.CENTER );
            shortcut.setBorder( BorderFactory.createLineBorder(Color.black) );
            add( shortcut, new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
        }

        public void setTipText(String text) {
            if( WAITING_TEXT.equals(text) ) {
                content.setContent( WAITING_TEXT, null );
                shortcut.setVisible( false );
            } else {
                content.setContent( text, null );
                shortcut.setVisible( true );
            }
        }
    }
}
