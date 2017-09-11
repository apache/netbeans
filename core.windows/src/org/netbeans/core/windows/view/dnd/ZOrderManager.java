/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

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

