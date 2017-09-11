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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Abstract definition of breakpoint.
 *
 * @author   Jan Jancura
 */
public abstract class Breakpoint {


    /** Property name for enabled status of the breakpoint. */
    public static final String          PROP_ENABLED = "enabled"; // NOI18N
    /** Property name for disposed state of the breakpoint. */
    public static final String          PROP_DISPOSED = "disposed"; // NOI18N
    /** Property name for name of group of the breakpoint. */
    public static final String          PROP_GROUP_NAME = "groupName"; // NOI18N
    /** Property name for other group properties of the breakpoint. 
     * @since 1.25 */
    public static final String          PROP_GROUP_PROPERTIES = "groupProperties"; // NOI18N
    /** Property name for breakpoint validity */
    public static final String          PROP_VALIDITY = "validity"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_HIT_COUNT_FILTER = "hitCountFilter"; // NOI18N
    
    /** Validity values */
    public static enum                  VALIDITY { UNKNOWN, VALID, INVALID }
    
    /** The style of filtering of hit counts.
     * The breakpoint is reported when the actual hit count is "equal to",
     * "greater than" or "multiple of" the number specified by the hit count filter. */
    public static enum                  HIT_COUNT_FILTERING_STYLE { EQUAL, GREATER, MULTIPLE }
    
    /** Support for property listeners. */
    private PropertyChangeSupport       pcs;
    private String                      groupName = "";
    private VALIDITY                    validity = VALIDITY.UNKNOWN;
    private String                      validityMessage;
    private int                         hitCountFilter;
    private HIT_COUNT_FILTERING_STYLE   hitCountFilteringStyle;
    private volatile Set<Breakpoint>    breakpointsToEnable = Collections.EMPTY_SET;
    private volatile Set<Breakpoint>    breakpointsToDisable = Collections.EMPTY_SET;
    
    { pcs = new PropertyChangeSupport (this); }

    /**
     * Called when breakpoint is removed.
     */
    protected void dispose () {}

    /**
     * Test whether the breakpoint is enabled.
     *
     * @return <code>true</code> if so
     */
    public abstract boolean isEnabled ();
    
    /**
     * Disables the breakpoint.
     */
    public abstract void disable ();
    
    /**
     * Enables the breakpoint.
     */
    public abstract void enable ();
    
    /**
     * Get the validity of this breakpoint.
     * @return The breakpoint validity.
     */
    public final synchronized VALIDITY getValidity() {
        return validity;
    }
    
    /**
     * Get the message describing the current validity. For invalid breakpoints
     * this should describe the reason why it is invalid.<p>
     * Intended for use by ui implementation code, NodeModel.getShortDescription(), for example.
     * @return The validity message.
     */
    public final synchronized String getValidityMessage() {
        return validityMessage;
    }
    
    /**
     * Set the validity of this breakpoint.
     * @param validity The new breakpoint validity.
     * @param reason The message describing why is this validity being set, or <code>null</code>.
     */
    protected final void setValidity(VALIDITY validity, String reason) {
        VALIDITY old;
        synchronized (this) {
            this.validityMessage = reason;
            if (this.validity == validity) return ;
            old = this.validity;
            this.validity = validity;
        }
        firePropertyChange(PROP_VALIDITY, old, validity);
    }
    
    /**
     * Get the hit count filter.
     * @return a positive hit count filter, or <code>zero</code> when no hit count filter is set.
     */
    public final synchronized int getHitCountFilter() {
        return hitCountFilter;
    }
    
    /**
     * Get the style of hit count filtering.
     * @return the style of hit count filtering, or <cpde>null</code> when no count filter is set.
     */
    public final synchronized HIT_COUNT_FILTERING_STYLE getHitCountFilteringStyle() {
        return hitCountFilteringStyle;
    }
    
    /**
     * Set the hit count filter and the style of filtering.
     * @param hitCountFilter a positive hit count filter, or <code>zero</code> to unset the filter.
     * @param hitCountFilteringStyle the style of hit count filtering.
     *        Can be <code>null</code> only when <code>hitCountFilter == 0</code>.
     */
    public final void setHitCountFilter(int hitCountFilter, HIT_COUNT_FILTERING_STYLE hitCountFilteringStyle) {
        Object[] old;
        Object[] newProp;
        synchronized (this) {
            if (hitCountFilter == this.hitCountFilter && hitCountFilteringStyle == this.hitCountFilteringStyle) {
                return ;
            }
            if (hitCountFilteringStyle == null && hitCountFilter > 0) {
                throw new NullPointerException("hitCountFilteringStyle must not be null.");
            }
            if (hitCountFilter == 0) {
                hitCountFilteringStyle = null;
            }
            if (this.hitCountFilter == 0) {
                old = null;
            } else {
                old = new Object[] { this.hitCountFilter, this.hitCountFilteringStyle };
            }
            if (hitCountFilter == 0) {
                newProp = null;
            } else {
                newProp = new Object[] { hitCountFilter, hitCountFilteringStyle };
            }
            this.hitCountFilter = hitCountFilter;
            this.hitCountFilteringStyle = hitCountFilteringStyle;
        }
        firePropertyChange(PROP_HIT_COUNT_FILTER, old, newProp);
    }

    /**
     * Get the name of a user-created group for this breakpoint.
     */
    public String getGroupName () {
        return groupName;
    }
    
    /**
     * Set the name of a user-created group for this breakpoint.
     */
    public void setGroupName (String newGroupName) {
        if (groupName.equals (newGroupName)) return;
        String old = groupName;
        groupName = newGroupName.intern();
        firePropertyChange (PROP_GROUP_NAME, old, newGroupName);
    }

    /**
     * Get group properties of the breakpoint.
     * These are implementation-defined group properties as oposed to {@link #getGroupName()},
     * which returns user-defined group name.
     * <p>
     * These properties are used by the Breakpoint Window to show a tree
     * hierarchy of groups and associated breakpoints.
     * Implementation should fire {@link #PROP_GROUP_PROPERTIES} event when
     * the group properties change.
     * @return {@link GroupProperties} or <code>null</code> when no group properties
     * are defined.
     * @since 1.25
     */
    public GroupProperties getGroupProperties() {
        return null;
    }
    
    /**
     * Determines if the breakpoint supports dependent breakpoints.
     * If true, get/setBreakpointsToEnable/Disable methods can be used to get
     * or set dependent breakpoints.
     * If false, the methods throw an UnsupportedOperationException.
     * @return <code>true</code> if the dependent breakpoints are supported,
     * <code>false</code> otherwise.
     * @since 1.35
     */
    public boolean canHaveDependentBreakpoints() {
        return false;
    }
    
    /**
     * Get the set of breakpoints that will be enabled after this breakpoint
     * is hit.
     * <p>
     * Not all breakpoint implementations honor dependent breakpoints.
     * Use {@link #canHaveDependentBreakpoints()} to determine if the operation is supported.
     * @return The set of breakpoints.
     * @throws UnsupportedOperationException if the breakpoint does not support
     * dependent breakpoints - see {@link #canHaveDependentBreakpoints()}.
     * @since 1.35
     */
    @NonNull
    public Set<Breakpoint> getBreakpointsToEnable() {
        if (!canHaveDependentBreakpoints()) {
            throw new UnsupportedOperationException("Cannot have dependent breakpoints."); // NOI18N
        }
        return breakpointsToEnable;
    }
    
    /**
     * Get the set of breakpoints that will be disabled after this breakpoint
     * is hit.
     * <p>
     * Not all breakpoint implementations honor dependent breakpoints.
     * Use {@link #canHaveDependentBreakpoints()} to determine if the operation is supported.
     * @throws UnsupportedOperationException if the breakpoint does not support
     * dependent breakpoints - see {@link #canHaveDependentBreakpoints()}.
     * @return The set of breakpoints.
     * @since 1.35
     */
    @NonNull
    public Set<Breakpoint> getBreakpointsToDisable() {
        if (!canHaveDependentBreakpoints()) {
            throw new UnsupportedOperationException("Cannot have dependent breakpoints."); // NOI18N
        }
        return breakpointsToDisable;
    }
    
    /**
     * Set the set of breakpoints that will be enabled after this breakpoint
     * is hit.
     * <p>
     * Not all breakpoint implementations honor dependent breakpoints.
     * Use {@link #canHaveDependentBreakpoints()} to determine if the operation is supported.
     * @param breakpointsToEnable The set of breakpoints.
     * @throws UnsupportedOperationException if the breakpoint does not support
     * dependent breakpoints - see {@link #canHaveDependentBreakpoints()}.
     * @since 1.35
     */
    public void setBreakpointsToEnable(@NonNull Set<Breakpoint> breakpointsToEnable) {
        if (!canHaveDependentBreakpoints()) {
            throw new UnsupportedOperationException("Cannot have dependent breakpoints."); // NOI18N
        }
        this.breakpointsToEnable = breakpointsToEnable;
    }
    
    /**
     * Set the set of breakpoints that will be disabled after this breakpoint
     * is hit.
     * <p>
     * Not all breakpoint implementations honor dependent breakpoints.
     * Use {@link #canHaveDependentBreakpoints()} to determine if the operation is supported.
     * @param breakpointsToEnable The set of breakpoints.
     * @throws UnsupportedOperationException if the breakpoint does not support
     * dependent breakpoints - see {@link #canHaveDependentBreakpoints()}.
     * @since 1.35
     */
    public void setBreakpointsToDisable(@NonNull Set<Breakpoint> breakpointsToDisable) {
        if (!canHaveDependentBreakpoints()) {
            throw new UnsupportedOperationException("Cannot have dependent breakpoints."); // NOI18N
        }
        this.breakpointsToDisable = breakpointsToDisable;
    }
    
    /** 
     * Add a listener to property changes.
     *
     * @param listener the listener to add
     */
    public synchronized void addPropertyChangeListener (
        PropertyChangeListener listener
    ) {
        pcs.addPropertyChangeListener (listener);
    }

    /** 
     * Remove a listener to property changes.
     *
     * @param listener the listener to remove
     */
    public synchronized void removePropertyChangeListener (
        PropertyChangeListener listener
    ) {
        pcs.removePropertyChangeListener (listener);
    }

    /**
     * Adds a property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l the listener to add
     */
    public void addPropertyChangeListener (
        String propertyName, PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
     * Removes a property change listener.
     *
     * @param propertyName a name of property to stop listening on
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (
        String propertyName, PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (propertyName, l);
    }

    /**
     * Fire property change.
     *
     * @param name name of property
     * @param o old value of property
     * @param n new value of property
     */
    protected void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
    }
    
    /**
     * Called when breakpoint is removed.
     */
    void disposeOut () {
        dispose ();
        firePropertyChange (PROP_DISPOSED, Boolean.FALSE, Boolean.TRUE);
    }
    

    /**
     * Group properties of breakpoint.
     * These are used by the Breakpoint Window to show a tree hierarchy of
     * groups and associated breakpoints.
     * @since 1.25
     */
    public static abstract class GroupProperties {

        /**
         * Get the language of the source file with the breakpoint.
         * @return The human-readable language of the breakpoint source file or <code>null</code>
         * when this does not apply.
         * @see <code>org.netbeans.spi.debugger.ui.BreakpointType.getCategoryDisplayName()</code>
         */
        public abstract String getLanguage();

        /**
         * Get the breakpoint type.
         * @return The human-readable type of the breakpoint or <code>null</code>
         * when this does not apply.
         * @see <code>org.netbeans.spi.debugger.ui.BreakpointType.getTypeDisplayName()</code>
         */
        public abstract String getType();

        /**
         * Get the source files containing this breakpoint.
         * @return The source files where this breakpoint is submitted or <code>null</code>
         * when this does not apply.
         */
        public abstract FileObject[] getFiles();

        /**
         * Get the projects containing this breakpoint.
         * @return The projects in which this breakpoint is submitted or <code>null</code>
         * when this does not apply.
         */
        public abstract Project[] getProjects();

        /**
         * Get the debugger engines that are currently actively using this breakpoint.
         * @return The engines in which this breakpoint is active or <code>null</code>
         * when this does not apply.
         */
        public abstract DebuggerEngine[] getEngines();

        /**
         * Test is this breakpoint is hidden (not visible to the user).
         * @return <code>true</code> when this breakpoint is hidden, <code>false</code> otherwise.
         */
        public abstract boolean isHidden();
    }
}
