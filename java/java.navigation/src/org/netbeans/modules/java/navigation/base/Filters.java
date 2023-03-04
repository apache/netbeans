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
package org.netbeans.modules.java.navigation.base;

import org.netbeans.modules.java.navigation.actions.NameActions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.navigation.actions.SortActions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Zezula
 */
public abstract class Filters<T> {
    
    private static final String PROP_NATURAL_SORT = "naturalSort";  //NOI18N
    private static final String PROP_FQN = "fqn";                   //NOI18N

    private volatile boolean naturalSort;
    private volatile boolean fqn;
    //@GuardedBy("this")
    private FiltersManager filtersManager;
    //@NotThreadSafe
    private JToggleButton sortByNameButton;
    //@NotThreadSafe
    private JToggleButton sortByPositionButton;
    //@NotThreadSafe
    private JToggleButton fqNameButton;

    protected Filters() {
        naturalSort = NbPreferences.forModule(this.getClass()).getBoolean( PROP_NATURAL_SORT, false );
        fqn = NbPreferences.forModule(this.getClass()).getBoolean(PROP_FQN, false);
    }

    public final boolean isNaturalSort() {
        return naturalSort;
    }

    public final void setNaturalSort(final boolean naturalSort) {        
        this.naturalSort = naturalSort;
        NbPreferences.forModule(this.getClass()).putBoolean( PROP_NATURAL_SORT, naturalSort );
        if( null != sortByNameButton ) {
            sortByNameButton.setSelected(!naturalSort);
        }
        if( null != sortByPositionButton ) {
            sortByPositionButton.setSelected(naturalSort);
        }
        sortUpdated();
    }

    public boolean isFqn() {
        return fqn;
    }

    public void setFqn(final boolean fqn) {
        this.fqn = fqn;
        NbPreferences.forModule(this.getClass()).putBoolean(PROP_FQN, fqn);
        if(null != fqNameButton) {
            fqNameButton.setSelected(fqn);
        }
        fqnUpdated();
    }

    public final JComponent getComponent() {
        final FiltersManager fm = getFiltersManager();
        final AbstractButton[] nameButtons = createNameButtons();
        final AbstractButton[] sortButtons = createSortButtons();
        final AbstractButton[] customButtons = createCustomButtons();
        final List<AbstractButton> buttons = new ArrayList<AbstractButton>(nameButtons.length + customButtons.length + sortButtons.length + 2);
        buttons.addAll(Arrays.asList(nameButtons));
        if (!buttons.isEmpty() && sortButtons.length > 0) {
            buttons.add(null);
        }
        buttons.addAll(Arrays.asList(sortButtons));
        if (!buttons.isEmpty() && customButtons.length > 0) {
            buttons.add(null);
        }
        buttons.addAll(Arrays.asList(customButtons));
        return fm.getComponent(buttons);
    }

    public final synchronized FiltersManager getFiltersManager() {
        if (filtersManager == null) {
            filtersManager = createFilters();
        }
        return filtersManager;
    }

    public abstract Collection<T> filter( Collection<? extends T> original);
    
    protected abstract FiltersManager createFilters ();
    
    protected abstract void sortUpdated();

    protected abstract void fqnUpdated();

    @NonNull
    protected AbstractButton[] createNameButtons() {
        assert SwingUtilities.isEventDispatchThread();
        AbstractButton[] res = new AbstractButton[1];
        if(null == fqNameButton) {
            fqNameButton = new JToggleButton(NameActions.createFullyQualifiedNameAction(this));
            fqNameButton.setToolTipText(fqNameButton.getText());
            fqNameButton.setText(null);
            fqNameButton.setSelected(isFqn());
            fqNameButton.setFocusable( false );
        }
        res[0] = fqNameButton;
        return res;
    }

    @NonNull
    protected AbstractButton[] createSortButtons() {
        assert SwingUtilities.isEventDispatchThread();
        JToggleButton[] res = new JToggleButton[2];
        if( null == sortByNameButton ) {
            sortByNameButton = new JToggleButton(SortActions.createSortByNameAction(this));
            sortByNameButton.setToolTipText(sortByNameButton.getText());
            sortByNameButton.setText(null);
            sortByNameButton.setSelected( !isNaturalSort());
            sortByNameButton.setFocusable( false );
        }
        res[0] = sortByNameButton;

        if( null == sortByPositionButton ) {
            sortByPositionButton = new JToggleButton(SortActions.createSortBySourceAction(this));
            sortByPositionButton.setToolTipText(sortByPositionButton.getText());
            sortByPositionButton.setText(null);
            sortByPositionButton.setSelected(isNaturalSort());
            sortByPositionButton.setFocusable( false );
        }
        res[1] = sortByPositionButton;
        return res;
    }

    @NonNull
    protected AbstractButton[] createCustomButtons() {
        return new AbstractButton[0];
    }
}
