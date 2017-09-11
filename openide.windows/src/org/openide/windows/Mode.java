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
 * {@link WindowManager#getModes}.<p>
 *
 * <p>
 * Each mode must have a unique name.
 *
 * <p><p>
 * <b><font color="red"><em>Important note: Do not provide implementation of this interface unless you are window system provider!</em></font></b>
 */
public interface Mode extends Serializable {
    /** Name of property for bounds of the mode */
    public static final String PROP_BOUNDS = "bounds"; // NOI18N

    /** Name of property for the unique programmatic name of this mode.
     * @deprecated Do not use. It is redundant, name can not be changed.*/
    public static final String PROP_NAME = "name"; // NOI18N

    /** Name of property for the display name of this mode.
     * @deprecated Do not use. It is redundant. */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N

    /** @deprecated Only public by accident. */

    /* public static final */ long serialVersionUID = -2650968323666215654L;

    /** Get the diplay name of the mode.
     * This name will be used by a container to create its title.
     * @return human-presentable name of the mode
     * @deprecated Do not use. It is redudant. */
    public String getDisplayName();

    /** Get the programmatic name of the mode.
     * This name should be unique, as it is used to find modes etc.
     * @return programmatic name of the mode */
    public String getName();

    /** Get the icon of the mode. It will be used by component container
     * implementations as the icon (e.g. for display in tabs).
     * @return the icon of the mode (or <code>null</code> if no icon was specified)
     * @deprecated Do not use. It is redundant. */
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
}
