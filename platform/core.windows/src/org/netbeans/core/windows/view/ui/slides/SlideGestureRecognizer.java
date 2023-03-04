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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;

/* Listens to user actions that trigger sliding operation such as slide in
 * slide out or popup menu action to be invoked.
 *
 * @author Dafe Simonek
 */
final class SlideGestureRecognizer implements ActionListener, MouseListener, MouseMotionListener {
    /** container of sliding buttons */
    private SlideBar slideBar;
    /** button in which area mouse pointer is or null */
    private Component mouseInButton = null;    
    /** current location of mouse pointer */
    private int curMouseLocX, curMouseLocY;
    
    /** Listsner to timer notifications */
    private AutoSlideTrigger autoSlideTrigger = new AutoSlideTrigger();
    private ResizeGestureRecognizer resizer;
    private boolean pressingButton = false;
    
    private static final Logger LOG = Logger.getLogger(SlideGestureRecognizer.class.getName());

    SlideGestureRecognizer(SlideBar slideBar, ResizeGestureRecognizer resize) {
        this.slideBar = slideBar;
        resizer = resize;
    }

    /** Attaches given button to this recognizer, it means starts listening
     * on its various mouse and action events
     */
    public void attachButton (AbstractButton button) {
        button.addActionListener(this);
        button.addMouseListener(this);
        button.addMouseMotionListener(this);
    }
    
    /** Detaches given button from this recognizer, it means stops listening
     * on its various mouse and action events
     */
    public void detachButton (AbstractButton button) {
        button.removeActionListener(this);
        button.removeMouseListener(this);
        button.addMouseMotionListener(this);
    }

    /** Reaction to user press on some of the slide buttons */
    @Override
    public void actionPerformed(ActionEvent e) {
        slideBar.userClickedSlidingButton((Component)e.getSource());
    }

    /** Tracks mouse pointer location */
    @Override
    public void mouseMoved(MouseEvent e) {
        if (autoSlideTrigger.isEnabled()) {
            curMouseLocX = e.getX();
            curMouseLocY = e.getY();
        }
        // #54764 - start
        if (pressingButton && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == 0) {
            pressingButton = false;
            autoSlideTrigger.activateAutoSlideInGesture(); 
        }
        // #54764 - end
    }

    /** Activates automatic slide in system */
    @Override
    public void mouseEntered(MouseEvent e) {
        if (!slideBar.isHoveringAllowed()) {
            // don't even try to trigger automatic sliding when focused slide is active
            return;
        }
        mouseInButton = (Component)e.getSource();
        curMouseLocX = e.getX();
        curMouseLocY = e.getY();
        pressingButton =false;
        // #54764 - start
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
            pressingButton = true;
            return;
        }
        // #54764 - end
        autoSlideTrigger.activateAutoSlideInGesture();
    }

    /** Deactivates automatic slide in listening */
    @Override
    public void mouseExited(MouseEvent e) {
        mouseInButton = null;
        pressingButton = false;
        autoSlideTrigger.deactivateAutoSlideInGesture(e);
    }
    
    /** Reacts to popup triggers on sliding buttons */
    @Override
    public void mousePressed(MouseEvent e) {
        autoSlideTrigger.deactivateAutoSlideInGesture(e);
        handlePopupRequests(e);
    }
    
    /** Reacts to popup triggers on sliding buttons */
    @Override
    public void mouseReleased(MouseEvent e) {
        autoSlideTrigger.deactivateAutoSlideInGesture(e);
        handlePopupRequests(e);
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        // no operation
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // no operation
        if( e.getButton() == MouseEvent.BUTTON2 ) {
            slideBar.userMiddleClickedSlidingButton(e.getComponent());
        }
    }
    
    /** Sends message to show popup menu on button if conditions are met */
    private void handlePopupRequests (MouseEvent e) {
        // don't react on popup triggers on whole bar
        if (e.getSource().equals(slideBar)) {
            return;
        }
        
        if (e.isPopupTrigger()) {
            slideBar.userTriggeredPopup(e, (Component)e.getSource());
        }
    }

    /** Listen to timer notifications and mouse AWT events to start/stop
     * auto slide in/slide out operation */
    private final class AutoSlideTrigger implements ActionListener, AWTEventListener {
        
        /** timer for triggering slide in after mouse stops for a while */
        private Timer slideInTimer;
        /** timer for automatic slide out */
        private Timer slideOutTimer;
        /** location of mouse pointer in last timer cycle */
        private int initialX, initialY;
        /** true when auto slide-in was performed and is visible, false ootherwise */
        private boolean autoSlideActive = false;
        /** union of slide bar and slided component bounds; 
         escape of mouse pointer from this area means auto slide out to be triggered */
        private Rectangle activeArea;
        
        AutoSlideTrigger() {
            super();
            slideInTimer = new Timer(200, this);
            slideInTimer.setRepeats(true);
            slideInTimer.setCoalesce(true);
        }

        /** Starts listening to user events that may lead to automatic slide in */
        public void activateAutoSlideInGesture() {
            initialX = curMouseLocX;
            initialY = curMouseLocY;
            slideInTimer.start();
        }
        
        /** Stops listening to user events that may lead to automatic slide in */
        public void deactivateAutoSlideInGesture (MouseEvent evt) {
            slideInTimer.stop();
            notifySlideOutTimer(evt);
        }

        /** @return true when auto slide system is listening and active, false ootherwise */
        public boolean isEnabled() {
            return autoSlideActive || slideInTimer.isRunning();
        }

        /** Action listener implementation - reacts to timer notification, which
         * means we should check conditions and perform auto slide in or auto
         * slide out if appropriate
         */
        @Override
        public void actionPerformed(ActionEvent evt) {
            if (slideInTimer.equals(evt.getSource())) {
                slideInTimerReaction(evt);
            } else {
                slideOutTimerReaction(evt);
            }
        }
        
        private void slideInTimerReaction (ActionEvent evt) {
            if (isSlideInGesture()) {
                slideInTimer.stop();
                // multiple auto slide in requests, get rid of old one first
                if (autoSlideActive) {
                    autoSlideOut();
                }
                autoSlideActive = true;
                // #45494 - rarely, mouseInButton value can be out of sync
                // with SlideBar buttons array, and we don't slide in in such case
                if (slideBar.userTriggeredAutoSlideIn(mouseInButton)) {
                    Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_MOTION_EVENT_MASK);
                } else {
                    autoSlideActive = false;
                }
            } else {
                initialX = curMouseLocX;
                initialY = curMouseLocY;
            }
        }
        
        private void slideOutTimerReaction (ActionEvent evt) {
            LOG.fine("slideOutTimerReaction entered, trying to auto slide out");
            slideOutTimer.stop();
            autoSlideOutIfNeeded();
        }

        /** AWTEventListener implementation. Analyzes incoming mouse motion
         * and initiates automatic slide out when needed.
         */
        @Override
        public void eventDispatched(AWTEvent event) {
            notifySlideOutTimer((MouseEvent)event);
        }
        
        /** Checks conditions and runs auto slide out if needed.
         */
        private void autoSlideOutIfNeeded () {
            if (!autoSlideActive) {
                // ignore pending events that came later after cleanup
                return;
            }
            if (slideBar.isActive()) {
                // if slide bar is active (focused), we should do no more automatic things
                cleanup();
                return;
            }
            
            cleanup();
            autoSlideOut();
        }

        /** Actually performs slide out by notifying slide bar */
        private void autoSlideOut() {
            slideBar.userTriggeredAutoSlideOut();
        }

        /** Removea all attached listeners and generally stops to try run 
         * sliding automatically */
        private void cleanup() {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
            autoSlideActive = false;
            activeArea = null;
        }
        
        /** @return true when conditions for auto slide IN were met, false otherwise */
        private boolean isSlideInGesture () {
            if (mouseInButton == null) {
                return false;
            }
            
            int diffX = Math.abs(initialX - curMouseLocX);
            int diffY = Math.abs(initialY - curMouseLocY);
            
            return (diffX <= 2) && (diffY <= 2);
        }
        
        /** @return true when conditions for auto slide OUT were met, false otherwise */
        private boolean isSlideOutGesture(MouseEvent evt) {
            if (resizer.isDragging()) {
                activeArea = null;
                return false;
            }
            if (activeArea == null) {
                activeArea = computeActiveArea();
                // comps are not yet ready, so do nothing
                if (activeArea == null) {
                    return false;
                }
            }
            Point mouseLoc = evt.getPoint();
            //#118828
            if (! (evt.getSource() instanceof Component)) {
                return false;
            }
            
            SwingUtilities.convertPointToScreen(mouseLoc, (Component)evt.getSource());
            
            boolean isMouseOut = !activeArea.contains(mouseLoc);
            
            return isMouseOut;
        }

        /** @return Area in which automatic slide in is preserved. Can return
         * null signalizing that components making active area bounds are not yet 
         * ready or showing.
         */
        private Rectangle computeActiveArea() {
            Component slidedComp = slideBar.getSlidedComp();
            if (slidedComp == null || !slidedComp.isShowing()) {
                return null;
            }
            
            Point slideBarLoc = slideBar.getLocationOnScreen();
            Rectangle actArea = new Rectangle(slideBarLoc.x - 1, slideBarLoc.y - 1,
                                    slideBar.getWidth() - 1, slideBar.getHeight() - 1);
            
            Point slidedCompLoc = slidedComp.getLocationOnScreen();
            
            int slidex = slidedCompLoc.x;
            int slidey = slidedCompLoc.y;
            int slideh = slidedComp.getHeight();
            int slidew = slidedComp.getWidth();
            int orientation = slideBar.getModel().getOrientation();
            if (orientation == SlideBarDataModel.WEST) {
                slidew = slidew + ResizeGestureRecognizer.RESIZE_BUFFER;
            }
            if (orientation == SlideBarDataModel.EAST) {
                slidew = slidew + ResizeGestureRecognizer.RESIZE_BUFFER;
                slidex = slidex - ResizeGestureRecognizer.RESIZE_BUFFER;
            }
            if (orientation == SlideBarDataModel.SOUTH) {
                slideh = slideh + ResizeGestureRecognizer.RESIZE_BUFFER;
                slidey = slidey - ResizeGestureRecognizer.RESIZE_BUFFER;
            }
            if (orientation == SlideBarDataModel.NORTH) {
                slideh = slideh + ResizeGestureRecognizer.RESIZE_BUFFER;
            }
            actArea = SwingUtilities.computeUnion(
                slidex, slidey, slidew,
                slideh, actArea);
            
            return actArea;
        }

        /** 
         * Handles start or stop of timer for correct auto slide out
         * functionality.
         * 
         * @param evt Mouse event to analyze
         */
        private void notifySlideOutTimer (MouseEvent evt) {
            if (!autoSlideActive) {
                return;
            }
            // stop automatic slide out if slide out gesture not satisfied
            if (!isSlideOutGesture(evt)) {
                if (slideOutTimer != null && slideOutTimer.isRunning()) {
                    slideOutTimer.stop();
                    LOG.fine("notifySlideOutTimer: slide out gesture not satisfied, stopping auto slide out");
                }
                return;
            }
            
            if (slideOutTimer == null) {
                slideOutTimer = new Timer(400, this);
                slideOutTimer.setRepeats(false);
                LOG.fine("notifySlideOutTimer: created slideOutTimer");
            }

            if (!slideOutTimer.isRunning()) {
                slideOutTimer.start();
                LOG.fine("notifySlideOutTimer: started slideoutTimer");
            }
        }
        
        
    } // AutoSlideTrigger
    
}
