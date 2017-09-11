/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.ui.models;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author martin
 */
public class BreakpointsGroupExpansionFilter implements TreeExpansionModelFilter {

    private final Set<BreakpointGroupInfo> expanded = new HashSet<BreakpointGroupInfo>();
    
    @Override
    public boolean isExpanded(TreeExpansionModel original, Object node) throws UnknownTypeException {
        if (node instanceof BreakpointGroup) {
            BreakpointGroupInfo bgi = new BreakpointGroupInfo((BreakpointGroup) node);
            synchronized (expanded) {
                return expanded.contains(bgi);
            }
        } else {
            return original.isExpanded(node);
        }
    }

    @Override
    public void nodeExpanded(Object node) {
        if (node instanceof BreakpointGroup) {
            BreakpointGroupInfo bgi = new BreakpointGroupInfo((BreakpointGroup) node);
            synchronized (expanded) {
                expanded.add(bgi);
            }
        }
    }

    @Override
    public void nodeCollapsed(Object node) {
        if (node instanceof BreakpointGroup) {
            BreakpointGroupInfo bgi = new BreakpointGroupInfo((BreakpointGroup) node);
            synchronized (expanded) {
                expanded.remove(bgi);
            }
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
    }
    
    private static final class BreakpointGroupInfo {
        
        private final WeakReference<Object> idRef;
        private final String name;
        private final BreakpointGroup.Group group;
        
        BreakpointGroupInfo(BreakpointGroup bg) {
            idRef = new WeakReference<Object>(bg.getId());
            name = bg.getName();
            group = bg.getGroup();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 67 * hash + (this.group != null ? this.group.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BreakpointGroupInfo other = (BreakpointGroupInfo) obj;
            Object id = this.idRef.get();
            Object otherId = other.idRef.get();
            if (id != otherId && (id == null || !id.equals(otherId))) {
                return false;
            }
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.group != other.group) {
                return false;
            }
            return true;
        }
    }
    
}
