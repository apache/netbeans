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
