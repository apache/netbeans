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

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.IllegalArgumentException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import org.openide.windows.WindowManager;

/** Holds and manages z-order of attached windows.
 *
 * Note, manager is NetBeans specific, not general. It automatically attaches
 * main window and expects that all registered zOrder are always above
 * this main window.
 *
 * Not thread safe, must be called from EQT.
 *
 * @author Dafe Simonek
 */
public final class ZOrderManager extends WindowAdapter {

    /** Singleton instance */
    private static ZOrderManager instance;

    private static Logger logger = Logger.getLogger("org.netbeans.core.windows.view.dnd");

    /** Z-order list of pane containers (windows) */
    private List<WeakReference<RootPaneContainer>> zOrder = new ArrayList<WeakReference<RootPaneContainer>>();

    /** Set of pane containers to temporarily exclude from z-ordering. */
    private Set<WeakReference<RootPaneContainer>> excludeSet = new HashSet<WeakReference<RootPaneContainer>>();


    /** Creates a new instance of ZOrderManager */
    private ZOrderManager() {
    }

    /** Returns singleton instance of ZOrderManager */
    public static ZOrderManager getInstance () {
        if (instance == null) {
            instance = new ZOrderManager();
        }
        return instance;
    }

    /** Adds given window (RootPaneContainer) to the set of windows which are tracked.
     */
    public void attachWindow (RootPaneContainer rpc) {
        logger.entering(getClass().getName(), "attachWindow");

        if (!(rpc instanceof Window)) {
            throw new IllegalArgumentException("Argument must be subclas of java.awt.Window: " + rpc);   //NOI18N
        }
        if (getWeak(rpc) != null) {
            throw new IllegalArgumentException("Window already attached: " + rpc);   //NOI18N
        }

        zOrder.add(new WeakReference<RootPaneContainer>(rpc));
        ((Window)rpc).addWindowListener(this);
    }

    /** Stops to track given window (RootPaneContainer).
     */
    public boolean detachWindow (RootPaneContainer rpc) {
        logger.entering(getClass().getName(), "detachWindow");

        if (!(rpc instanceof Window)) {
            throw new IllegalArgumentException("Argument must be subclas of java.awt.Window: " + rpc);   //NOI18N
        }

        WeakReference<RootPaneContainer> ww = getWeak(rpc);
        if (ww == null) {
            return false;
        }

        ((Window)rpc).removeWindowListener(this);
        return zOrder.remove(ww);
    }

    /** Excludes/reincludes given RootPaneContainer from z-ordering. Excluded RootPaneContainer
     * never returns true from isOnTop call, even if it is on top of window stack.
     * RootPaneContainer that is second on top is returned in such situation.
     *
     * Used to distinguish RootPaneContainer that is being dragged.
     *
     * @param rpc Pane container to exlude or include back into rthe z-ordering.
     * @param exclude true when exclusion is needed, false when normal default
     * behaviour is desirable.
     */
    public void setExcludeFromOrder (RootPaneContainer rpc, boolean exclude) {
        if (exclude) {
            excludeSet.add(new WeakReference<RootPaneContainer>(rpc));
        } else {
            WeakReference<RootPaneContainer> ww = getExcludedWeak(rpc);
            if (ww != null) {
                excludeSet.remove(ww);
            }
        }
    }

    /* Stops to track all windows registered before.
     */
    public void clear () {
        RootPaneContainer rpc;
        for (WeakReference<RootPaneContainer> elem : zOrder) {
            rpc = elem.get();
            if (rpc != null) {
                ((Window)rpc).removeWindowListener(this);
            }
        }
        zOrder.clear();
    }

    /** Finds out whether given pane container (window) is not under any other
     * window registered in this manager at given screen point.
     *
     * @param rpc Pane container (window)
     * @param screenLoc point relative to screen
     * @return true when given window is on top of other registered windows at given point
     */
    public boolean isOnTop (RootPaneContainer rpc, Point screenLoc) {
        logger.entering(getClass().getName(), "isOnTop");
        
        /*JComponent cp = (JComponent) rpc.getContentPane();
        // false if point in dirty region - probably overlapped by other window
        if (RepaintManager.currentManager(cp).getDirtyRegion(cp).contains(screenLoc)) {
            return false;
        }*/

        int size = zOrder.size();
        WeakReference<RootPaneContainer> curWeakW = null;
        RootPaneContainer curRpc = null;
        for (int i = size - 1; i >= 0; i--) {
            curWeakW = zOrder.get(i);
            if (curWeakW == null) {
                continue;
            }
            curRpc = curWeakW.get();
            // ignore excluded ones
            if (getExcludedWeak(curRpc) != null) {
                continue;
            }
            // return top one
            if (curRpc == rpc) {
                return true;
            }
            // safe cast, assured by checks in attachWindow method
            Window curW = (Window) curRpc;
            Point loc = new Point(screenLoc);
            SwingUtilities.convertPointFromScreen(loc, curW);
            if (curW.contains(loc)) {
                    // && !RepaintManager.currentManager(curComp).getDirtyRegion(curComp).contains(screenLoc)) {
                return false;
            }
        }

        // take main window automatically as last window to check
        if (rpc == WindowManager.getDefault().getMainWindow()) {
            return true;
        }

        // not found
        return false;
    }

    /*** Implementation of WindowListener ******/

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
        logger.entering(getClass().getName(), "windowActivated");

        WeakReference<RootPaneContainer> ww = getWeak((RootPaneContainer)e.getWindow());
        if (ww != null) {
            // place as last item in zOrder list
            zOrder.remove(ww);
            zOrder.add(ww);
        } else {
            throw new IllegalArgumentException("Window not attached: " + e.getWindow()); //NOI18N
        }
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }


    private WeakReference<RootPaneContainer> getWeak (RootPaneContainer rpc) {
        for (WeakReference<RootPaneContainer> elem : zOrder) {
            if (elem.get() == rpc) {
                return elem;
            }
        }
        return null;
    }

    private WeakReference<RootPaneContainer> getExcludedWeak (RootPaneContainer rpc) {
        for (WeakReference<RootPaneContainer> elem : excludeSet) {
            if (elem.get() == rpc) {
                return elem;
            }
        }
        return null;
    }

}

