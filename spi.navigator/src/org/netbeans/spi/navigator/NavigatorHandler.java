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

package org.netbeans.spi.navigator;

import org.netbeans.modules.navigator.NavigatorController;
import org.netbeans.modules.navigator.NavigatorTC;
import org.openide.util.Lookup;

/**
 * Set of methods for driving navigator behaviour.
 *
 * @author Dafe Simonek
 */
public final class NavigatorHandler {

    private static NavigatorController controller;

    /** No external instantiation allowed.
     */
    private NavigatorHandler () {
    }
    
    /** 
     * Activates and shows given panel in navigator view. Panel must be one of  
     * available panels at the time this method is called, which means that 
     * panel must be registered (either through mime type in xml layer or NavigatorLookupHint)
     * for currently activated node in the system.  
     * Previously activated panel is deactivated and hidden.
     * <p>
     * Typical use case is to set preferred navigator panel in a situation 
     * when multiple panels are registered for multiple data types.   
     * <p>
     * This method must be called from EventQueue thread.
     * 
     * @param panel Navigator panel to be activated
     * @throws IllegalArgumentException if given panel is not available 
     */ 
    public static void activatePanel (NavigatorPanel panel) {
        getController().activatePanel(panel);
    }

    /**
     * If there is a custom {@link NavigatorDisplayer} implementation, it should call
     * this method just before its UI shows up (before the enclosing
     * TopComponent is opened) to actually initialize the navigator. From this
     * point the navigator observes the TopComponent and once it is opened, it
     * starts collecting panels from the providers and passing them to the
     * displayer.
     * <p>
     * If there is no custom displayer registered, the navigator's own
     * (default) TopComponent will be used and it also takes care of
     * initializing the navigator automatically. No need to call this method then.
     * <p>
     * @since 1.19
     */
    public static void activateNavigator() {
        getController();
    }

    private static NavigatorController getController() {
        if (controller == null) {
            NavigatorDisplayer display = Lookup.getDefault().lookup(NavigatorDisplayer.class);
            if (display != null) {
                controller = new NavigatorController(display);
            } else { // use the navigator's own TopComponent
                controller = NavigatorTC.getInstance().getController();
            }
        }
        return controller;
    }
}
