/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
