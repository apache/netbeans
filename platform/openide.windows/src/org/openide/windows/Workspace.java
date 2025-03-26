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
 * <p>
 * <b><span style="color:red"><em>Important note: Do not provide implementation of this interface unless you are window system provider!</em></span></b>
 *
 * @author Jaroslav Tulach
 * @deprecated Do not use any more. Use {@link WindowManager} methods directly,
 * e.g. {@link WindowManager#getModes()} etc.
 */
@Deprecated
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
