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
package org.netbeans.modules.java.navigation.hierarchy;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.navigation.base.Filters;
import org.netbeans.modules.java.navigation.base.FiltersDescription;
import org.netbeans.modules.java.navigation.base.FiltersManager;

/**
 *
 * @author Tomas Zezula
 */
final class HierarchyFilters extends Filters<Object> {

    static final String PROP_NATURAL_SORT = "naturalSort";  //NOI18N
    static final String PROP_FQN = "fqn";                   //NOI18N

    private final PropertyChangeSupport support;    


    HierarchyFilters() {        
        this.support = new PropertyChangeSupport(this);
    }

    @Override
    public Collection<Object> filter(Collection<?> original) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    protected void sortUpdated() {
        support.firePropertyChange(PROP_NATURAL_SORT,null,null);
    }

    @Override
    protected void fqnUpdated() {
        support.firePropertyChange(PROP_FQN,null,null);
    }

    @Override
    protected FiltersManager createFilters() {
        FiltersDescription desc = new FiltersDescription();
        return FiltersDescription.createManager(desc);
    }
}
