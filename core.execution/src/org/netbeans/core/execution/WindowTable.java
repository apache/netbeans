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

package org.netbeans.core.execution;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

/**
*
* @author Ales Novak
*/
final class WindowTable extends HashMap<Window,TaskThreadGroup> {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1494996298725028533L;

    /** window listener */
    private WindowListener winListener;

    /** maps ThreadGroup:ArrayList, ArrayList keeps windows */
    private HashMap<ThreadGroup,ArrayList<Window>> windowMap;

    /**
    *default constructor
    */
    public WindowTable () {
        super(16);
        windowMap = new HashMap<ThreadGroup,ArrayList<Window>>(16);
        winListener = new WindowAdapter() {
                          public void windowClosed(WindowEvent ev) {
                              Window win;
                              removeWindow(win = (Window)ev.getSource());
                              win.removeWindowListener(this);
                          }
                      };
    }

    public synchronized void putTaskWindow(Window win, TaskThreadGroup grp) {
        ArrayList<Window> vec;
        if ((vec = windowMap.get(grp)) == null) {
            vec = new ArrayList<Window>();
            windowMap.put(grp, vec);
        }
        vec.add(win);
        win.addWindowListener(winListener);
        super.put(win, grp);
    }

    public TaskThreadGroup getThreadGroup(Window win) {
        return super.get(win);
    }

    /** closes windows opened by grp ThreadGroup */
    void closeGroup(ThreadGroup grp) {
        Window win;
        ArrayList<Window> vec = windowMap.get(grp);
        if (vec == null) return;
        Iterator<Window> ee = vec.iterator();
        while (ee.hasNext()) {
            (win = ee.next()).setVisible(false);
            remove(win);
            if (win != getSharedOwnerFrame()) {
                win.dispose();
            }
        }
        windowMap.remove(grp);
    }
    
    // XXX todo nasty hack into Swing
    private static java.awt.Frame shOwnerFrame;
    private static java.awt.Frame getSharedOwnerFrame() {
        if (shOwnerFrame != null) {
            return shOwnerFrame;
        }
        
        try {
            Class swUtil = Class.forName("javax.swing.SwingUtilities"); // NOI18N
            java.lang.reflect.Method getter = swUtil.getDeclaredMethod("getSharedOwnerFrame", new Class[] {}); // NOI18N
            getter.setAccessible(true);
            
            shOwnerFrame = (java.awt.Frame) getter.invoke(null, new Object[] {});
        } catch (Exception e) {
            // do nothing
        }
        
        return shOwnerFrame;
    }

    /** return true if the ThreadGroup has any windows */
    boolean hasWindows(ThreadGroup grp) {
        ArrayList<Window> vec = windowMap.get(grp);
        if ((vec == null) || (vec.size() == 0) || hiddenWindows(vec)) {
            return false;
        }
        return true;
    }

    /**
    * @param vec is a ArrayList of windows
    * @param grp is a ThreadGroup that belongs to the ArrayList
    * @return true if all windows in the ArrayList vec are invisible
    */
    private boolean hiddenWindows(ArrayList<Window> vec) {
        Iterator<Window> ee = vec.iterator();
        Window win;
        while (ee.hasNext()) {
            win = ee.next();
            if (win.isVisible()) return false;
        }
        // windows will be removed later
        return true;
    }

    /** removes given window */
    private void removeWindow(Window win) {
        Object obj = get(win); // obj is threadgroup
        if (obj == null) return;
        remove(win);
        ArrayList<Window> vec = windowMap.get(obj);
        if (vec == null) return;
        vec.remove(win);
    }
}
