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
package org.openide.windows;

import java.awt.Rectangle;

import java.beans.PropertyChangeListener;

import java.io.Serializable;

import java.net.URL;

import java.util.Set;


/** Represents one user workspace that holds a list of modes into which
 * components can be assigned.
 * Created by WindowManager.
 * When serialized only keeps "weak" reference to this workspace does not
 * stores the content of the workspace (it is responsibility of window manager).
 *
 * <p><p>
 * <b><font color="red"><em>Important note: Do not provide implementation of this interface unless you are window system provider!</em></font></b>
 *
 * @author Jaroslav Tulach
 * @deprecated Do not use any more. Use {@link WindowManager} methods directly,
 * e.g. {@link WindowManager#getModes()} etc.
 */
public interface Workspace extends Serializable {
    /** Name of property for modes in the workspace.
     * @deprecated Use {@link WindowManager#PROP_MODES} instead. */
    public static final String PROP_MODES = WindowManager.PROP_MODES; // TEMP

    /** Name of property for the programmatic name of this workspace.
      * @deprecated Do no use. It is redundant. */
    public static final String PROP_NAME = "name"; // NOI18N

    /** Name of property for the display name of this workspace.
     * @deprecated Do no use. It is redundant. */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N

    /**
     * Do not use.
     * @deprecated Only public by accident.
     */

    /* public static final */ long serialVersionUID = 2987897537843190271L;

    /**
     * Gets the unique programmatic name of this workspace.
     * Used e.g. by {@link WindowManager#findWorkspace}.
     * @return the code name of this workspace
     * @deprecated Do no use. It is redundant. */
    public String getName();

    /** Get human-presentable name of the workspace which
     * will be used for displaying.
     * @return the display name of the workspace
     * @deprecated Do no use. It is redundant. */
    public String getDisplayName();

    /**
     * Gets a list of all modes on this workspace.
     * @return a set of all {@link Mode}s known on this workspace
     * @deprecated Use {@link WindowManager#getModes} instead. */
    public Set<? extends Mode> getModes();

    /** Get bounds of the workspace. Returned value has slighly different
     * meaning for SDI and MDI mode. Modules should use this method for
     * correct positioning of their windows.
     * @return In SDI, returns bounds relative to whole screen, returns bounds
     * of the part of screen below main window (or above main window, if main
     * window is on bottom part of the screen).<br>
     * In MDI, bounds are relative to the main window; returned value represents
     * 'client area' of the main window
     * @deprecated Do no use. It is redundant. */
    public Rectangle getBounds();

    /** Activates this workspace to be current one.
     * This leads to change of current workspace of the WindowManager.
     * @deprecated Do no use. It is redundant. */
    public void activate();

    /** Create a new mode.
    * @param name a unique programmatic name of the mode
    * @param displayName a human presentable (probably localized) name
    *                    of the mode (may be used by
                         the <b>Dock&nbsp;Into</b> submenu, e.g.)
    * @param icon a URL to the icon to use for the mode (e.g. on a tab or window corner);
    *             may be <code>null</code>
    * @return the new mode
    * @deprecated Do no use. It is redundant. Currently it returns default predefined <code>Mode</code> instance. */
    public Mode createMode(String name, String displayName, URL icon);

    /** Search all modes on this workspace by name.
     * @param name the name of the mode to search for
     * @return the mode with that name, or <code>null</code> if no such mode
     *         can be found
     * @deprecated Use {@link WindowManager#findMode(String)} instead. */
    public Mode findMode(String name);

    /** Finds mode the component is in on this workspace.
     *
     * @param c component to find mode for
     * @return the mode or null if the component is not visible on this workspace
     * @deprecated Use {@link WindowManager#findMode(TopComponent)} instead. */
    public Mode findMode(TopComponent c);

    /** Removes this workspace from set of workspaces
     * in window manager.
     * @deprecated Do no use. It is redundant. */
    public void remove();

    /** Add a property change listener.
     * @param list the listener to add
     * @deprecated Use {@link WindowManager#addPropertyChangeListener} instead. */
    public void addPropertyChangeListener(PropertyChangeListener list);

    /** Remove a property change listener.
     * @param list the listener to remove
     * @deprecated Use {@link WindowManager#removePropertyChangeListener} instead. */
    public void removePropertyChangeListener(PropertyChangeListener list);
}
