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

package org.netbeans.core.ui.notifications;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/** 
 *
 * An icon representing the last Notification on status bar. Clicking the icon
 * shows a list of all Notifications. A balloon-like tooltip is shown for this icon
 * when a new Notification is created.
 * When balloons are disabled (-Dnb.notification.balloon.disable=true) then this
 * icon is flashing for a moment when a new Notification is created.
 *
 * @author S. Aubrecht
 */
class FlashingIcon extends JLabel implements MouseListener, PropertyChangeListener {
    
    protected int STOP_FLASHING_DELAY = 5 * 1000;
    protected int DISAPPEAR_DELAY_MILLIS = STOP_FLASHING_DELAY + 50 * 1000;
    protected int FLASHING_FREQUENCY = 500;
    
    private boolean keepRunning = false;
    private boolean isIconVisible = false;
    private boolean keepFlashing = true;
    private long startTime = 0;
    private Task timerTask;

    private NotificationImpl currentNotification;
    
    /** 
     * Creates a new instance of FlashingIcon 
     *
     * @param icon The icon that will be flashing (blinking)
     */
    protected FlashingIcon() {
        addMouseListener( this );
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    }

    @Override
    public void addNotify() {
        super.addNotify();
        NotificationDisplayerImpl displayer = NotificationDisplayerImpl.getInstance();
        int notificationCount = displayer.size();
        setText( notificationCount > 1 ? String.valueOf(notificationCount) : null );
        currentNotification = displayer.getTopNotification();
        if( null != currentNotification ) {
            setIcon(currentNotification.getIcon());
            setToolTipText(currentNotification.getTitle());
        }
        setVisible( displayer.size() > 0 );
        displayer.addPropertyChangeListener(this);
    }

    @Override
    public void removeNotify() {
        NotificationDisplayerImpl displayer = NotificationDisplayerImpl.getInstance();
        if (displayer != null) {
            displayer.removePropertyChangeListener(this);
        }
        currentNotification = null;
        super.removeNotify();
    }


    /**
     * Start flashing of the icon. If the icon is already flashing, the timer
     * is reset.
     * If the icon is visible but not flashing, it starts flashing again
     * and the disappear timer is reset.
     */
    public void startFlashing() {
        synchronized( this ) {
            startTime = System.currentTimeMillis();
            isIconVisible = !isIconVisible;
            keepRunning = true;
            keepFlashing = true;
            if( null == timerTask ) {
                timerTask = RequestProcessor.getDefault ().post (new Timer ());
            } else {
                timerTask.run ();
            }
            this.setVisible (true);
        }
        repaint();
    }
    
    /**
     * Stop the flashing and hide the icon.
     */
    public void disappear() {
        synchronized( this ) {
            keepRunning = false;
            isIconVisible = false;
            keepFlashing = false;
            if( null != timerTask )
                timerTask.cancel();
            timerTask = null;
            setToolTipText( null );
            this.setVisible (false);
        }
        repaint();
    }
    
    /**
     * Stop flashing of the icon. The icon remains visible and active (listens 
     * for mouse clicks and displays tooltip) until the disappear timer expires.
     */
    public void stopFlashing() {
        synchronized( this ) {
            if( keepRunning && !isIconVisible ) {
                isIconVisible = true;
                repaint();
            }
        }
        keepFlashing = false;
        isIconVisible = true;
    }
    
    /**
     * Switch the current image and repaint
     */
    protected void flashIcon() {
        isIconVisible = !isIconVisible;

        invalidate();
        revalidate();
        repaint();
    }

    @Override
    public void setIcon( Icon icon ) {
        if( null != icon ) {
            icon = new MyIcon(icon);
            isIconVisible = true;
        }
        super.setIcon(icon);
    }

    public void mouseReleased(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        stopFlashing();
    }

    public void mouseExited(MouseEvent e) {
        stopFlashing();
    }

    public void mouseEntered(MouseEvent e) {
        stopFlashing();
    }

    public void mouseClicked(MouseEvent e) {
        if( isIconVisible ) {
            //disappear();
            onMouseClick();
        }
    }
    
    /**
     * Invoked when the user clicks the icon.
     */
    protected void onMouseClick() {
        PopupList.show(this);
    }

    /**
     * Invoked when the disappear timer expired.
     */
    protected void timeout() {

    }

    @Override
    public Cursor getCursor() {

        if( isIconVisible ) {
            return Cursor.getPredefinedCursor( Cursor.HAND_CURSOR );
        }
        return Cursor.getDefaultCursor();
    }

    @Override
    public Point getToolTipLocation( MouseEvent event ) {

        JToolTip tip = createToolTip();
        tip.setTipText( getToolTipText() );
        Dimension d = tip.getPreferredSize();
        
        
        Point retValue = new Point( getWidth()-d.width, -d.height );
        return retValue;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if( NotificationDisplayerImpl.PROP_NOTIFICATION_ADDED.equals(evt.getPropertyName()) ) {
            final NotificationImpl ni = (NotificationImpl) evt.getNewValue();
            setNotification( ni, ni.showBallon() );
            PopupList.dismiss();
        } else if( NotificationDisplayerImpl.PROP_NOTIFICATION_REMOVED.equals(evt.getPropertyName()) ) {
            NotificationImpl removed = (NotificationImpl)evt.getNewValue();
            if( removed.equals(currentNotification) ) {
                NotificationImpl top = NotificationDisplayerImpl.getInstance().getTopNotification();
                setNotification( top, false );
                BalloonManager.dismiss();
                stopFlashing();
            } else {
                int notificationCount = NotificationDisplayerImpl.getInstance().size();
                setText( notificationCount > 1 ? String.valueOf(notificationCount) : null );
            }
        }
    }

    private boolean canShowBalloon() {
        return !Boolean.getBoolean("nb.notification.balloon.disable");
    }

    private void setNotification(final NotificationImpl n, boolean showBalloon ) {
        NotificationDisplayerImpl displayer = NotificationDisplayerImpl.getInstance();
        int notificationCount = displayer.size();
        setText( notificationCount > 1 ? String.valueOf(notificationCount) : null );
        currentNotification = n;
        if( null != currentNotification ) {
            setIcon(currentNotification.getIcon());
            setToolTipText(currentNotification.getTitle());
            if( showBalloon ) {
                if( canShowBalloon() ) {
                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            if( null == currentNotification || null == currentNotification.getBalloonComp() )
                                return;
                            BalloonManager.show(FlashingIcon.this,
                                    currentNotification.getBalloonComp(), 
                                    null,
                                    new ActionListener() {
                                @Override
                                        public void actionPerformed(ActionEvent e) {
                                            n.clear();
                                        }
                                    }, 3*1000);
                        }
                    });
                } else {
                    startFlashing();
                }
            }
        } else {
            BalloonManager.dismiss();
            stopFlashing();
        }
        setVisible( displayer.size() > 0 );
    }

    private class Timer implements Runnable {
        public void run() {
            synchronized( FlashingIcon.this ) {
                long currentTime = System.currentTimeMillis();
                if( keepFlashing ) {
                    if( currentTime - startTime < STOP_FLASHING_DELAY ) {
                        flashIcon();
                    } else {
                        stopFlashing();
                        if (DISAPPEAR_DELAY_MILLIS == -1) {
                            timerTask = null;
                        }
                    }
                }
                if( DISAPPEAR_DELAY_MILLIS > 0 && currentTime - startTime >= DISAPPEAR_DELAY_MILLIS ) {
//                    disappear();
                    timeout();
                } else {
                    if( null != timerTask )
                        timerTask.schedule( FLASHING_FREQUENCY );
                }
            }
        }
    }

    private class MyIcon implements Icon {

        private Icon orig;

        public MyIcon( Icon orig ) {
            this.orig = orig;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if( isIconVisible )
                orig.paintIcon(c, g, x, y);
        }

        public int getIconWidth() {
            return orig.getIconWidth();
        }

        public int getIconHeight() {
            return orig.getIconHeight();
        }

    }
}
