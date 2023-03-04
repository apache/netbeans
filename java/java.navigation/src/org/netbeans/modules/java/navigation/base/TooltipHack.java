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

package org.netbeans.modules.java.navigation.base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

/**
 * Hack to invoke tooltip on given component on given position immediatelly
 * at a request.
 *
 * XXX - hack is not reliable, could stop working in future JDK releases.
 * Navigator should better handle tooltips totally itself,
 * without Swing TooltipManager, to get rid of such hacks.
 *
 * @author Dafe Simonek
 */
public final class TooltipHack implements ActionListener {

    private static TooltipHack instance;
    
    /** holds previous dismiss tooltip value */
    private static int prevDismiss = -1;
    
    private TooltipHack() {
    }

    /** Hack to invoke tooltip on given JComponent, with given dismiss delay.
     * Triggers <br>
     * <code>comp.getToolTipText(MouseEvent)</code> and 
     * <code>comp.getToolTipLocation(MouseEvent)</code> with fake mousemoved 
     * MouseEvent, set to given coordinates.
     */
    public static void invokeTip (JComponent comp, int x, int y, int dismissDelay) {
        final ToolTipManager ttm = ToolTipManager.sharedInstance();
        final int prevInit = ttm.getInitialDelay();
        prevDismiss = ttm.getDismissDelay();
        ttm.setInitialDelay(0);
        ttm.setDismissDelay(dismissDelay);
        
        MouseEvent fakeEvt = new MouseEvent(
                comp, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 
                0, x, y, 0, false);
        ttm.mouseMoved(fakeEvt);
        
        ttm.setInitialDelay(prevInit);
        Timer timer = new Timer(20, instance());
        timer.setRepeats(false);
        timer.start();
    }
    
    /** impl of ActionListener, reacts on timer and restores Dismiss value.
     * Don't call from outside classes.
     */
    public void actionPerformed(ActionEvent e) {
        if (prevDismiss > 0) {
            ToolTipManager.sharedInstance().setDismissDelay(prevDismiss);
            prevDismiss = -1;
        }
    }
    
    private static TooltipHack instance () {
        if (instance == null) {
            instance = new TooltipHack();
        }
        return instance;
    }
    
}
