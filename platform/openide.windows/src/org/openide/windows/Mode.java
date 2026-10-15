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

import java.awt.Image;
import java.awt.Rectangle;

import java.beans.PropertyChangeListener;

import java.io.Serializable;


/** Instances of this interface represent places or containers
 * which <code>TopComponent</code> has to be added to
 * in order to be managed by window system.
 *
 * <p>There is always present default document mode named "editor".
 * Modules can add their own modes by declaring them using XML.
 * <P>
 * Modules can get a set of current modes by calling
 * {@link WindowManager#getModes}.
 *
 * <p>
 * Each mode must have a unique name.
 *
 * <p>
 * <b><span style="color:red"><em>Important note: Do not provide implementation of this interface unless you are window system provider!</em></span></b>
 */
public interface Mode extends Serializable {
    /** Name of property for bounds of the mode */
    public static final String PROP_BOUNDS = "bounds"; // NOI18N

    /** Name of property for the unique programmatic name of this mode.
     * @deprecated Do not use. It is redundant, name can not be changed.*/
    @Deprecated
    public static final String PROP_NAME = "name"; // NOI18N

    /** Name of property for the display name of this mode.
     * @deprecated Do not use. It is redundant. */
    @Deprecated
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N

    /** @deprecated Only public by accident. */

    @Deprecated
    /* public static final */ long serialVersionUID = -2650968323666215654L;

    /** Get the diplay name of the mode.
     * This name will be used by a container to create its title.
     * @return human-presentable name of the mode
     * @deprecated Do not use. It is redudant. */
    @Deprecated
    public String getDisplayName();

    /** Get the programmatic name of the mode.
     * This name should be unique, as it is used to find modes etc.
     * @return programmatic name of the mode */
    public String getName();

    /** Get the icon of the mode. It will be used by component container
     * implementations as the icon (e.g. for display in tabs).
     * @return the icon of the mode (or <code>null</code> if no icon was specified)
     * @deprecated Do not use. It is redundant. */
    @Deprecated
    public Image getIcon();

    /** Attaches a component to a mode for this workspace.
    * If the component is in different mode on this workspace, it is
    * removed from the original and moved to this one.
    *
    * @param c component
    * @return true if top component was succesfully docked to this mode, false otherwise
    */
    public boolean dockInto(TopComponent c);

    /** Allows implementor to specify some restrictive policy as to which
     * top components can be docked into this mode.
     * @return true if a given top component can be docked into this mode,
     *         false otherwise
     */
    public boolean canDock(TopComponent tc);

    /** Sets the bounds of the mode.
    * @param s the bounds for the mode
    */
    public void setBounds(Rectangle s);

    /** Getter for current bounds of the mode.
    * @return the bounds of the mode
    */
    public Rectangle getBounds();

    /** Getter for asociated workspace.
     * @return The workspace instance to which is this mode asociated.
     * @deprecated Do not use. Worskpaces are not supporeted anymore. */
    @Deprecated
    public Workspace getWorkspace();

    /** Get all top components currently docked into this mode.
     * @return the list of components; might be empty, but not null
    */
    public TopComponent[] getTopComponents();

    /** Add a property change listener.
    * @param list the listener to add
    */
    public void addPropertyChangeListener(PropertyChangeListener list);

    /** Remove a property change listener.
    * @param list the listener to remove
    */
    public void removePropertyChangeListener(PropertyChangeListener list);

    /** Gets selected <code>TopComponent</code> in this mode.
     * @since 4.13 */
    public TopComponent getSelectedTopComponent();
    
    /**
     * Extension to provide exposure of XML configuration.
     * 
     * @see <a href="http://wiki.apidesign.org/wiki/ExtendingInterfaces">ExtendingInterfaces</a>
     * @since 6.82
     */
    public interface Xml extends Mode {
        
        /**
         * Generates the Mode configuration as XML.
         * @return an XML representation of the Mode's configuration.
         * @since 6.82
         */
        public String toXml();
    }
}
