/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
/*
 * ContainerProvider.java
 *
 * Created on January 24, 2004, 2:32 PM
 */

package org.netbeans.actions.spi;

import java.util.Map;

/** Provides the names of available action containers in the system, such
 * as toolbars and menus.  Uses a predefined context name for the context
 * menu context, of which there is only ever one.
 *
 * @author  Tim Boudreau
 */
public abstract class ContainerProvider {
    public static final String CONTEXTMENU_CONTEXT = "contextMenu"; //NOI18N
    public static final Object TYPE_TOOLBAR = new Integer (1);
    public static final Object TYPE_MENU = new Integer (2);

    protected ContainerProvider() {
    }

    /** Return the names of all the menu container contexts in the system. */
    public abstract String[] getMenuContainerContexts();

    /** Return the names of all the menu container contexts in the system. */
    public abstract String[] getToolbarContainerContexts();

    /** returns the predefined name for the context menu container context */
    public final String getContextMenuContainerContext() {
        return CONTEXTMENU_CONTEXT;
    }
    
    /** Determine if the contents of the context can change over the life of
     * the application.  Return true <strong>only</strong> if items which are
     * truly unknown at startup will be added.  If there's a known set of items,
     * but some appear and disappear, simply return that the hidden items are
     * invisible from your ActionProvider's getState method */
    public boolean isDynamicContext (Object containerType, String containerCtx) {
        //XXX Support for dynamic contexts pending
        return false;
    }
    
    /** Get the enablement/visibility of the named container context.
     * @param containerType The type of container - either TYPE_MENU or TYPE_TOOLBAR,
     *  or some other object that the implementation and caller agree is a valid
     *  context type.  
     * @param containerCtx The programmatic, unique name of the container.
     * @param context The user context (selected object, active window, etc.,
     *  as agreed upon between the implementation and application).
     */
    public abstract int getContainerState (Object containerType, 
        String containerCtx, Map context);
    
    
    public abstract String getDisplayName (Object containerType, String containerCtx);
}
