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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.refactoring.spi.FiltersManager;
import org.netbeans.modules.refactoring.spi.ui.FiltersDescription;
import org.openide.awt.ToolbarWithOverflow;
import org.openide.util.Mutex;

/**
 * Handles creation and manipulation with boolean state filters.
 *
 * @author Dafe Simomek
 */
public final class FiltersManagerImpl extends FiltersManager {

    private FiltersComponent comp;

    static FiltersManagerImpl create(FiltersDescription descr) {
        return new FiltersManagerImpl(descr);
    }

    /**
     * Indicates if a filter is selected.
     *
     * @param filterName the name of the filter to check
     * @return Returns true when given filter is selected, false otherwise.
     */
    @Override
    public boolean isSelected(String filterName) {
        return comp.isSelected(filterName);
    }

    /**
     * Sets boolean value of filter with given name. True means filter is
     * selected (enabled), false otherwise. Note, must be called from AWT
     * thread.
     */
    public void setSelected(String filterName, boolean value) {
        comp.setFilterSelected(filterName, value);
    }

    /**
     * @return component instance visually representing filters
     */
    public JToolBar getComponent(JToggleButton... additionalButtons) {
        if(additionalButtons != null && additionalButtons.length > 0) {
            comp.addAdditionalButtons(additionalButtons);
        }
        return comp.toolbar;
    }

    /**
     * @return Filters description
     */
    public FiltersDescription getDescription() {
        return comp.getDescription();
    }

    /**
     * Assigns listener for listening to filter changes
     */
    public void hookChangeListener(FilterChangeListener l) {
        comp.hookFilterChangeListener(l);
    }

    /**
     * Interface for listening to changes of filter states contained in
     * FIltersPanel
     */
    public interface FilterChangeListener {

        /**
         * Called whenever some changes in state of filters contained in filters
         * panel is triggered
         */
        public void filterStateChanged(ChangeEvent e);
    } // end of FilterChangeListener

    /**
     * Private, creation managed by factory method 'create'
     */
    private FiltersManagerImpl(FiltersDescription descr) {
        comp = new FiltersComponent(descr);
    }

    /**
     * Swing component representing filters in panel filled with toggle buttons.
     * Provides thread safe access to the states of filters by copying states
     * into private map, properly sync'ed.
     */
    private class FiltersComponent implements ActionListener {

        /**
         * list of <JToggleButton> visually representing filters
         */
        private List<JToggleButton> toggles;
        /**
         * description of filters
         */
        private final FiltersDescription filtersDesc;
        /**
         * lock for listener
         */
        private final Object L_LOCK = new Object();
        /**
         * listener
         */
        private FilterChangeListener clientL;
        /**
         * lock for map of filter states
         */
        private final Object STATES_LOCK = new Object();
        /**
         * copy of filter states for accessing outside AWT
         */
        private Map<String, Boolean> filterStates;
        private JToolBar toolbar;

        /**
         * Returns selected state of given filter, thread safe.
         */
        public boolean isSelected(String filterName) {
            Boolean result;
            synchronized (STATES_LOCK) {
                if (filterStates == null) {
                    // Swing toggles not initialized yet
                    int index = filterIndexForName(filterName);
                    if (index < 0) {
                        return false;
                    } else {
                        return filtersDesc.isEnabled(index) && filtersDesc.isSelected(index);
                    }
                }
                result = filterStates.get(filterName);
            }

            if (result == null) {
                throw new IllegalArgumentException("Filter " + filterName + " not found.");
            }
            return result.booleanValue();
        }

        /**
         * Sets filter value, AWT only
         */
        public void setFilterSelected(String filterName, boolean value) {
            assert SwingUtilities.isEventDispatchThread();

            int index = filterIndexForName(filterName);
            if (index < 0) {
                throw new IllegalArgumentException("Filter " + filterName + " not found.");
            }
            // update both swing control and states map
            toggles.get(index).setSelected(value);
            synchronized (STATES_LOCK) {
                filterStates.put(filterName, Boolean.valueOf(value));
            }
            // notify
            fireChange();
        }

        public void hookFilterChangeListener(FilterChangeListener l) {
            synchronized (L_LOCK) {
                clientL = l;
            }
        }

        public FiltersDescription getDescription() {
            return filtersDesc;
        }

        /**
         * Not public, instances created using factory method createPanel
         */
        FiltersComponent(FiltersDescription descr) {
            this.filtersDesc = descr;
            // always create swing content in AWT thread
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    initPanel();
                }
            });
        }

        /**
         * Called only from AWT
         */
        private void initPanel() {
            // configure toolbar
            toolbar = new ToolbarWithOverflow(JToolBar.VERTICAL);
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(true);
            toolbar.setBorder(
                    javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0,
                    javax.swing.UIManager.getDefaults().getColor("Separator.foreground")), //NOI18N
                    javax.swing.BorderFactory.createEmptyBorder(0, 1, 0, 0)));
            if ("Aqua".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
                toolbar.setBackground(UIManager.getColor("NbExplorerView.background"));
            }
//            toolbar.setFocusable(false);
            // create toggle buttons
            int filterCount = filtersDesc.getFilterCount();
            toggles = new ArrayList<JToggleButton>(filterCount);
            JToggleButton toggleButton;

            Map<String, Boolean> fStates = new HashMap<String, Boolean>(filterCount * 2);

            for (int i = 0; i < filterCount; i++) {
                toggleButton = createToggle(fStates, i);
                toggles.add(toggleButton);
            }

            // add toggle buttons
            JToggleButton curToggle;
            for (int i = 0; i < toggles.size(); i++) {
                curToggle = toggles.get(i);
                curToggle.addActionListener(this);
                toolbar.add(curToggle);
            }

            // initialize member states map
            synchronized (STATES_LOCK) {
                filterStates = fStates;
            }
        }

        private void addAdditionalButtons(JToggleButton[] sortButtons) {
            Dimension space = new Dimension(3, 0);
            for (JToggleButton button : sortButtons) {
                Dimension size = new Dimension(21, 21); // 3 less than other buttons
                button.setMaximumSize(size);
                button.setMinimumSize(size);
                button.setPreferredSize(size);
//                button.setMargin(new Insets(2, 3, 2, 3));
                toolbar.addSeparator(space);
                toolbar.add(button);
            }
        }

        private JToggleButton createToggle(Map<String, Boolean> fStates, int index) {
            boolean isSelected = filtersDesc.isSelected(index);
            boolean enabled = filtersDesc.isEnabled(index);
            Icon icon = filtersDesc.getIcon(index);
            // ensure small size, just for the icon
            JToggleButton result = new JToggleButton(icon, enabled && isSelected);
            Dimension size = new Dimension(21, 21); // 3 less than other buttons
            result.setMaximumSize(size);
            result.setMinimumSize(size);
            result.setPreferredSize(size);
            if ("Aqua".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
                result.setBorderPainted(true);
            } else {
                result.setBorderPainted(false);
            }
//            result.setMargin(new Insets(2, 3, 2, 3));
            result.setToolTipText(filtersDesc.getTooltip(index));
            result.setEnabled(enabled);
            fStates.put(filtersDesc.getKey(index), Boolean.valueOf(isSelected));

            return result;
        }

        /**
         * Finds and returns index of filter with given name or -1 if no such
         * filter exists.
         */
        private int filterIndexForName(String filterName) {
            int filterCount = filtersDesc.getFilterCount();
            String curName;
            for (int i = 0; i < filterCount; i++) {
                curName = filtersDesc.getKey(i);
                if (filterName.equals(curName)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Reactions to toggle button click,
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // copy changed state first
            JToggleButton toggle = (JToggleButton) e.getSource();
            int index = toggles.indexOf(e.getSource());
            synchronized (STATES_LOCK) {
                filterStates.put(filtersDesc.getKey(index),
                        Boolean.valueOf(toggle.isSelected()));
            }
            // notify
            fireChange();
        }

        private void fireChange() {
            FilterChangeListener lCopy;
            synchronized (L_LOCK) {
                // no listener = no notification
                if (clientL == null) {
                    return;
                }
                lCopy = clientL;
            }

            // notify listener
            lCopy.filterStateChanged(new ChangeEvent(FiltersManagerImpl.this));
        }
    } // end of FiltersComponent
}
