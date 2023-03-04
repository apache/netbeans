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

package org.netbeans.modules.debugger.ui.views.debugging;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dan
 */
class ClickableIcon extends JLabel implements MouseListener {

    public static final int CLICKABLE_ICON_WIDTH = 24;
    public static final int CLICKABLE_ICON_HEIGHT = 16;
    
    private static final int STATE_NORMAL = 0;
    private static final int STATE_FOCUSED = 1;
    private static final int STATE_PRESSED = 2;
    
    private ImageIcon resumeIcon;
    private ImageIcon focusedResumeIcon;
    private ImageIcon pressedResumeIcon;
    private ImageIcon suspendIcon;
    private ImageIcon focusedSuspendIcon;
    private ImageIcon pressedSuspendIcon;
    
    private int state;
    private boolean isThreadSupended;
    private DVThread dvThread;
    private DebugTreeView tree;
    
    ClickableIcon(ImageIcon normalR, ImageIcon focusedR, ImageIcon pressedR,
            ImageIcon normalS, ImageIcon focusedS, ImageIcon pressedS, DVThread dvThread, DebugTreeView tree) {
        this.tree = tree;
        this.resumeIcon = normalR;
        this.focusedResumeIcon = focusedR;
        this.pressedResumeIcon = pressedR;
        
        this.suspendIcon = normalS;
        this.focusedSuspendIcon = focusedS;
        this.pressedSuspendIcon = pressedS;
        
        isThreadSupended = dvThread.isSuspended();
        this.dvThread = dvThread;
        
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        
        setPreferredSize(new Dimension(CLICKABLE_ICON_WIDTH, CLICKABLE_ICON_HEIGHT));
    }

    void initializeState(Container container, int sx, int sy, int width, int height) {
        Point point = null;
        try { // workaround for issue #146185, getMousePosition() may throw NPE
            point = container.getMousePosition(true);
        } catch (NullPointerException e) {
        }
        state = point != null && sx <= point.x && point.x < sx + width && sy <= point.y && point.y < sy + height
                ? STATE_FOCUSED : STATE_NORMAL;
        setFocusedThread();
        changeIcon();
        addMouseListener(this);
    }
    
    void changeThread(DVThread newThread, Container container, int sx, int sy, int width, int height) {
        boolean suspended = newThread.isSuspended();
        if (dvThread == newThread && suspended == isThreadSupended) {
            return;
        }
        isThreadSupended = newThread.isSuspended();
        this.dvThread = newThread;
        Point point = container.getMousePosition(true);
        state = point != null && sx <= point.x && point.x < sx + width && sy <= point.y && point.y < sy + height
                ? STATE_FOCUSED : STATE_NORMAL;
        setFocusedThread();
        changeIcon();
    }
    
    private void setFocusedThread() {
        if (state != STATE_NORMAL) {
            if (tree != null && tree.threadFocuseGained(dvThread)) {
                getParent().repaint();
            }
        } else {
            if (tree != null && tree.threadFocuseLost(dvThread)) {
                getParent().repaint();
            }
        }
    }
    
    private ImageIcon computeIcon() {
        ImageIcon icon = null;
        switch(state) {
            case STATE_NORMAL:
                icon = isThreadSupended ? resumeIcon : suspendIcon;
            break;
            case STATE_FOCUSED:
                icon = isThreadSupended ? focusedResumeIcon : focusedSuspendIcon;
            break;
            case STATE_PRESSED:
                icon = isThreadSupended ? pressedResumeIcon : pressedSuspendIcon;
            break;
        }
        return icon;
    }
    
    private void changeIcon() {
        setIcon(computeIcon());
        String key = isThreadSupended ? "LBL_RESUME_THREAD" : "LBL_SUSPEND_THREAD"; // NOI18N
        String text = NbBundle.getMessage(ClickableIcon.class, key, dvThread.getName());
        setToolTipText(text);
    }
    
    private void invokeAction() {
        final boolean suspended = isThreadSupended;
        RequestProcessor rp;
        try {
            Session s = dvThread.getDVSupport().getSession();
            rp = s.lookupFirst(null, RequestProcessor.class);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return ;
        }
        if (rp == null) {
            // debugger finishing?
            rp = RequestProcessor.getDefault();
        }
        rp.post(new Runnable() {
            public void run() {
                if (suspended) {
                    dvThread.resume();
                } else {
                    dvThread.suspend();
                }
            }
        });
        isThreadSupended = !isThreadSupended;
    }
    
    // **************************************************************************
    // MouseListener
    // **************************************************************************
    
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        state = STATE_PRESSED;
        changeIcon();
    }

    public void mouseReleased(MouseEvent e) {
        if (state == STATE_PRESSED) {
            state = STATE_FOCUSED;
            invokeAction();
            changeIcon();
        }
    }

    public void mouseEntered(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            state = STATE_NORMAL;
        } else {
            state = STATE_FOCUSED;
        }
        setFocusedThread();
        changeIcon();
    }

    public void mouseExited(MouseEvent e) {
        state = STATE_NORMAL;
        setFocusedThread();
        changeIcon();
    }

}
