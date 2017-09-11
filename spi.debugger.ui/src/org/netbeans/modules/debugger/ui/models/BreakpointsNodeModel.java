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

package org.netbeans.modules.debugger.ui.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.viewmodel.CheckNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 * @author   Jan Jancura
 */
public class BreakpointsNodeModel implements CheckNodeModel  {

    public static final String BREAKPOINT_GROUP =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";

    private final Collection modelListeners = new ArrayList();

    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(BreakpointsNodeModel.class).getString("CTL_BreakpointModel_Column_Name_Name");
        } else
        if (o instanceof BreakpointGroup) {
            return ((BreakpointGroup) o).getName();
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return TreeModel.ROOT;
        } else
        if (o instanceof BreakpointGroup) {
            return NbBundle.getBundle(BreakpointsNodeModel.class).getString("CTL_BreakpointModel_Column_GroupName_Desc");
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return BREAKPOINT_GROUP;
        } else
        if (o instanceof BreakpointGroup) {
            return BREAKPOINT_GROUP;
        } else
        throw new UnknownTypeException (o);
    }

    /**
     * Registers given listener.
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    /**
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }

    private void fireModelEvent(ModelEvent ev) {
        Collection listeners;
        synchronized (modelListeners) {
            listeners = new ArrayList(modelListeners);
        }
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
            ModelListener l = (ModelListener) it.next();
            l.modelChanged(ev);
        }
    }

    public boolean isCheckable(Object node) throws UnknownTypeException {
        return true;
    }

    public boolean isCheckEnabled(Object node) throws UnknownTypeException {
        return true;
    }

    public Boolean isSelected(Object node) throws UnknownTypeException {
        if (node instanceof Breakpoint) {
            return ((Breakpoint) node).isEnabled();
        } else if (node instanceof BreakpointGroup) {
            BreakpointGroup group = (BreakpointGroup) node;
            Enabled enabled = isEnabled(group);
            if (enabled == Enabled.YES) {
                return Boolean.TRUE;
            }
            if (enabled == Enabled.NO) {
                return Boolean.FALSE;
            }
            return null;
        }
        throw new UnknownTypeException (node);
    }
    
    enum Enabled { YES, NO, NA }

    private static Enabled isEnabled(BreakpointGroup group) {
        Enabled enabled = null; // We do not know anything
        for (BreakpointGroup g : group.getSubGroups()) {
            Enabled ge = isEnabled(g);
            if (enabled == null) {
                enabled = ge;
            } else if (ge != null) {
                enabled = and(enabled, ge);
                if (enabled == Enabled.NA) {
                    return Enabled.NA;
                }
            }
        }
        for (Breakpoint b : group.getBreakpoints()) {
            boolean be = b.isEnabled();
            if (enabled == null) {
                enabled = be ? Enabled.YES : Enabled.NO;
            } else {
                if (enabled == Enabled.YES && !be ||
                    enabled == Enabled.NO && be) {
                    return Enabled.NA;
                }
            }
        }
        return enabled;
    }

    private static Enabled and(Enabled e1, Enabled e2) {
        if (e1 == e2) {
            return e1;
        } else {
            return Enabled.NA;
        }
    }

    public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
        if (selected != null) {
            if (node instanceof Breakpoint) {
                Breakpoint bp = (Breakpoint) node;
                
                if (selected)
                    bp.enable ();
                else
                    bp.disable ();
                
                fireModelEvent(new ModelEvent.NodeChanged(
                        BreakpointsNodeModel.this,
                        bp));
                // re-calculate the enabled state of the BP group
                // TODO
                String groupName = bp.getGroupName();
                if (groupName != null) {
                    fireModelEvent(new ModelEvent.NodeChanged(
                        BreakpointsNodeModel.this,
                        groupName));
                }
            } else if (node instanceof BreakpointGroup) {
                BreakpointGroup group = (BreakpointGroup) node;
                setSelected(group, selected);
            }
        }
    }

    private void setSelected(BreakpointGroup group, Boolean selected) {
        for (BreakpointGroup g : group.getSubGroups()) {
            setSelected(g, selected);
        }
        for (Breakpoint b : group.getBreakpoints()) {
            if (selected)
                b.enable ();
            else
                b.disable ();

            fireModelEvent(new ModelEvent.NodeChanged(
                    BreakpointsNodeModel.this,
                    b));

        }
        fireModelEvent(new ModelEvent.NodeChanged(
            BreakpointsNodeModel.this,
            group));
    }
    
}
