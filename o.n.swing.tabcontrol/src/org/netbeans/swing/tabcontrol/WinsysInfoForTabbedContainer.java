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

package org.netbeans.swing.tabcontrol;

import java.awt.Component;
import org.netbeans.swing.tabcontrol.customtabs.TabbedComponentFactory;
import org.openide.windows.TopComponent;


/**
 * Interface that provides external information (provided by window system)
 * that TabbedContainers need to know in order to work fully.<p>
 *
 * Tab control uses info to provide for example tab buttons reacting on 
 * the position of the container or on maximization state.
 *
 * @see TabbedContainer#TabbedContainer
 * @see TabbedComponentFactory
 *
 * @author S. Aubrecht
 */
public abstract class WinsysInfoForTabbedContainer implements WinsysInfoForTabbed {

    /**
     * 
     * @return True if TopComponents can be slided out, false to remove Minimize button
     * from TopComponent's header.
     */
    public boolean isTopComponentSlidingEnabled() {
        return true;
    }
    
    /**
     * 
     * @return True if TopComponents can be closed, false to remove Close button
     * from TopComponent's header.
     */
    public boolean isTopComponentClosingEnabled() {
        return true;
    }

    /**
     * 
     * @return True if TopComponents can be maximized, false to remove Maximize/Restore
     * button from TopComponent's header./
     */
    public boolean isTopComponentMaximizationEnabled() {
        return true;
    }
    
    /**
     * @return True if given TopComponent can be closed, false to remove Close button
     * from TopComponent's header.
     * @since 1.15
     */
    public boolean isTopComponentClosingEnabled( TopComponent tc ) {
        return true;
    }

    /**
     * @return True if given TopComponent can be slided-out, false to remove 'Minimize Window'
     * action from TopComponent's popup menu.
     * @since 1.15
     */
    public boolean isTopComponentSlidingEnabled( TopComponent tc ) {
        return true;
    }

    /**
     * @return True if given TopComponent can be maximized.
     * @since 1.15
     */
    public boolean isTopComponentMaximizationEnabled( TopComponent tc ) {
        return true;
    }

    /**
     * @return True if it is possible to minimize the whole group of TopCOmponents.
     * @since 1.27
     */
    public boolean isModeSlidingEnabled() {
        return true;
    }
    
    /**
     * @return True if this container is currently slided out (and contains a single window)
     * @since 1.27
     */
    public boolean isSlidedOutContainer() {
        return false;
    }

    /**
     * Check if the header of given TopComponent should be painted in a special
     * way to indicate that some process is running in that TopComponent.
     * @param tc
     * @return True to indicate process being run in the TopComponent, false otherwise.
     * @since 1.34
     */
    public boolean isTopComponentBusy( TopComponent tc ) {
        return false;
    }

    public static WinsysInfoForTabbedContainer getDefault( WinsysInfoForTabbed winsysInfo ) {
        return new DefaultWinsysInfoForTabbedContainer( winsysInfo );
    }

    private static class DefaultWinsysInfoForTabbedContainer extends WinsysInfoForTabbedContainer {
        
        private WinsysInfoForTabbed delegate;
        
        public DefaultWinsysInfoForTabbedContainer( WinsysInfoForTabbed winsysInfo ) {
            this.delegate = winsysInfo;
        }

        public Object getOrientation(Component comp) {
            return null == delegate ? TabDisplayer.ORIENTATION_CENTER : delegate.getOrientation(comp);
        }

        public boolean inMaximizedMode(Component comp) {
            return null == delegate ? false : delegate.inMaximizedMode(comp);
        }
    }
}
