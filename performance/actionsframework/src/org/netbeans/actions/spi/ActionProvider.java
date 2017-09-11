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
 * ActionProvider.java
 *
 * Created on January 24, 2004, 12:26 AM
 */

package org.netbeans.actions.spi;

import java.util.Map;
import javax.swing.Icon;

/** The heart of the actions framework from the application author's perspective -
 * the implementation of this class supplies the available actions in a given
 * context.  Note that this class neither constructs Action objects, nor menu
 * items or other visual components - these are only needed when something
 * needs to be shown or updated, and the engine will provide
 * items to display them as needed.
 * <p>
 * This works as follows:  When menus or toolbars need to be displayed/created/
 * updated, the Engine will ask its master ActionProvider for the actionNames
 * for each container context.  A container context is simply a programmatic
 * name for a menu, toolbar, etc.  It will then iterate those names, calling
 * the other methods like getDisplayName, to get the relevant data that is
 * needed by the presenter to display them, and use that information to
 * correctly configure the presenters.
 * <p>
 * The programmatic names that will be passed in are a private contract between
 * an implementation and its method of registering actions in the system.  For
 * example, an implementation may allow components to register menus and
 * toolbars in an XML file; so the names for, e.g., menus will be provided
 * there.  An application's documentation will specify how to register
 * actions, contexts, etc. and its implementation of ActionProvider will
 * use this registry to look up information about actions. 
 * <p>
 * The only method that is passed any state information about the application
 * is getState().  This call is used to determine if a presenter is visible
 * and enabled or disabled, which is all the information that is needed to
 * display a presenter correctly.  It is passed a Map which contains all
 * the available information about the current user context (what object
 * is selected, what window is focused, etc.)  The implementation can query
 * the map, to decide what to display and its state.  The actual contents of this map are
 * a private contract with a given implementation.  For example, in NetBeans,
 * the Map will probably be a wrapper for the selected node and its Lookup.
 *
 * @author  Tim Boudreau
 */
public abstract class ActionProvider {
    public static final int STATE_ENABLED = 1;
    public static final int STATE_VISIBLE = 2;
    public static final int STATE_SELECTED = 4;
    
    public static final int ACTION_TYPE_ITEM = 0;
    public static final int ACTION_TYPE_SUBCONTEXT = 1;
    public static final int ACTION_TYPE_TOGGLE=2;
    
    /** Get the programmatic names (not display names) for all of the actions
     * in a given context.  The result should include <strong>all</strong> 
     * actions registered for that context whether or not they're enabled/displayed/etc.
     * getState() will be called later for each to decide if they should be 
     * presented currently, or hidden. */
    public abstract String[] getActionNames (String containerCtx);
    
    /** Get the display name for a given action name returned from getActionNames,
     * in a given logical action container (toolbar, menu, etc) */
    public abstract String getDisplayName (String actionName, String containerCtx);
    
    /** Get the action type.  This will not be called for toolbars, but will be
     * called for menus to determine if a submenu presenter or a menu item
     * presenter is needed */
    public abstract int getActionType (String actionName, String containerCtx);
    
    /** Get a description for an action appropriate for use in a tooltip.  Used
     * for toolbar presenters. */
    public abstract String getDescription (String actionName, String containerCtx);
    
    /** Get the icon, if any, for a given action.  The type argument is as defined
     * in BeanInfo */
    public abstract Icon getIcon (String actionName, String containerCtx, int type);
    
    /** Get the mnemonic to be used for action text */
    public abstract int getMnemonic (String actionName, String containerCtx);
    
    /** Get the displayed mnemonic index for action text */
    public abstract int getMnemonicIndex (String actionName, String containerCtx);
    
    /** Get the enablement/visibility state of the named action, given the 
     * passed user context map. */
    public abstract int getState (String actionName, String containerCtx, Map context);
    
}
