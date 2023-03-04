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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.navigation.NoBorderToolBar;
import org.openide.util.Mutex;
import org.openide.util.NbPreferences;

/** Handles creation and manipulation with boolean state filters. 
 *
 * @author Dafe Simomek
 */
public final class FiltersManager {
    
    private FiltersComponent comp;
    
    static FiltersManager create (FiltersDescription descr) {
        return new FiltersManager(descr);
    }
    
    /** Returns true when given filter is selected, false otherwise.
     * Note that this method is thread safe, can be called from any thread
     * (and usually will, as clients will call this from loadContent naturally)
     */
    public boolean isSelected (String filterName) {
        return comp.isSelected(filterName);
    }
    
    /** Sets boolean value of filter with given name. True means filter is
     * selected (enabled), false otherwise. Note, must be called from AWT thread.
     */
    public void setSelected (String filterName, boolean value) {
        comp.setFilterSelected(filterName, value);
    }

    /** @return component instance visually representing filters */ 
    public JComponent getComponent (@NonNull final Iterable<? extends AbstractButton> sortButtons ) {
        comp.addSortButtons( sortButtons );
        return comp;
    }
    
    /** @return Filters description */
    public FiltersDescription getDescription () {
        return comp.getDescription();
    }

    /** Assigns listener for listening to filter changes */ 
    public void hookChangeListener (FilterChangeListener l) {
        comp.hookFilterChangeListener(l);
    }
    
    /** Interface for listening to changes of filter states contained in FIltersPanel
     */
    public interface FilterChangeListener {
        /** Called whenever some changes in state of filters contained in
         * filters panel is triggered
         */
        public void filterStateChanged(ChangeEvent e);
        
    } // end of FilterChangeListener
    

    /** Private, creation managed by factory method 'create' */
    private FiltersManager (FiltersDescription descr) {
        comp = new FiltersComponent(descr);
    }
    
    /** Swing component representing filters in panel filled with toggle buttons.
     * Provides thread safe access to the states of filters by copying states
     * into private map, properly sync'ed.
     */
    private class FiltersComponent extends Box implements ActionListener {
        
        /** list of <JToggleButton> visually representing filters */
        private List<JToggleButton> toggles;
        /** description of filters */
        private final FiltersDescription filtersDesc;
 
        /** lock for listener */
        private Object L_LOCK = new Object();
        /** listener */
        private FilterChangeListener clientL;

        /** lock for map of filter states */
        private Object STATES_LOCK = new Object();
        /** copy of filter states for accessing outside AWT */
        private Map<String,Boolean> filterStates;
        
        private JToolBar toolbar;

        /** Returns selected state of given filter, thread safe.
         */
        public boolean isSelected (String filterName) {
            Boolean result;
            synchronized (STATES_LOCK) {
                if (filterStates == null) {
                    // Swing toggles not initialized yet
                    int index = filterIndexForName(filterName);
                    if (index < 0) {
                        return false;
                    } else {
                        return filtersDesc.isSelected(index);
                    }
                }
                result = filterStates.get(filterName);
            }
            
            if (result == null) {
                throw new IllegalArgumentException("Filter " + filterName + " not found.");
            }
            return result.booleanValue();
        }
        
        /** Sets filter value, AWT only */
        public void setFilterSelected (String filterName, boolean value) {
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
        
        public void hookFilterChangeListener (FilterChangeListener l) {
            synchronized (L_LOCK) {
                clientL = l;
            }
        }
        
        public FiltersDescription getDescription () {
            return filtersDesc;
        }
    
        /** Not public, instances created using factory method createPanel */
        FiltersComponent(FiltersDescription descr) {
            super(BoxLayout.X_AXIS);
            this.filtersDesc = descr;
            // always create swing content in AWT thread
            Mutex.EVENT.readAccess(new Runnable () {
                public void run () {
                    initPanel();                        
                }
            });
        }

        /** Called only from AWT */
        private void initPanel () {
            setBorder(new EmptyBorder(1, 2, 3, 5));

            // configure toolbar
            toolbar = new NoBorderToolBar(JToolBar.HORIZONTAL);
            toolbar.setFloatable(false);
            toolbar.setRollover(true);            
            toolbar.setBorderPainted(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            toolbar.setOpaque(false);
            toolbar.setFocusable(false);
            // create toggle buttons
            int filterCount = filtersDesc.getFilterCount();
            toggles = new ArrayList<JToggleButton>(filterCount);
            JToggleButton toggleButton = null;
            
            Map<String,Boolean> fStates = new HashMap<String, Boolean>(filterCount * 2);

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
                if (i != toggles.size() - 1) {
                    toolbar.add(new Space());
                }
            }
            
            add(toolbar);
            
            // initialize member states map
            synchronized (STATES_LOCK) {
                filterStates = fStates;
            }
        }
        
        private void addSortButtons(@NonNull final Iterable<? extends AbstractButton> buttons ) {
            assert toolbar != null : "toolbar initialized"; //NOI18N
            if (!toggles.isEmpty()) {
                toolbar.addSeparator();
            }
            final Dimension space = new Dimension(3, 0);
            boolean addSeparator = false;
            for(AbstractButton button : buttons) {
                if (button == null) {
                    if (addSeparator) {
                        toolbar.addSeparator();
                        addSeparator = false;
                    } else {
                        addSeparator = true;
                    }
                } else {
                    if (addSeparator) {
                        toolbar.addSeparator(space);
                    } else {
                        addSeparator = true;
                    }
                    Icon icon = button.getIcon();
                    Dimension size = new Dimension(icon.getIconWidth() + 6, icon.getIconHeight() + 4);
                    button.setPreferredSize(size);
                    button.setMargin(new Insets(2,3,2,3));
                    toolbar.add( button );
                }
            }
        }
        
        private Preferences getPreferences() {
            return NbPreferences.forModule( FiltersManager.class );
        }
        
        private JToggleButton createToggle (Map<String,Boolean> fStates, int index) {
            boolean isSelected = getPreferences().getBoolean( filtersDesc.getName(index), filtersDesc.isSelected(index) );
            Icon icon = filtersDesc.getSelectedIcon(index);
            // ensure small size, just for the icon
            JToggleButton result = new JToggleButton(icon, isSelected);
            Dimension size = new Dimension(icon.getIconWidth() + 6, icon.getIconHeight() + 4);
            result.setPreferredSize(size);
            result.setMargin(new Insets(2,3,2,3));
            result.setToolTipText(filtersDesc.getTooltip(index));
            result.setFocusable(false);
            
            fStates.put(filtersDesc.getName(index), Boolean.valueOf(isSelected));
            
            return result;
        }

        /** Finds and returns index of filter with given name or -1 if no
         * such filter exists.
         */
        private int filterIndexForName (String filterName) {
            int filterCount = filtersDesc.getFilterCount();
            String curName;
            for (int i = 0; i < filterCount; i++) {
                curName = filtersDesc.getName(i);
                if (filterName.equals(curName)) {
                    return i;
                }
            }
            return -1;
        }

        /** Reactions to toggle button click,  */
        public void actionPerformed(ActionEvent e) {
            // copy changed state first
            JToggleButton toggle = (JToggleButton)e.getSource();
            int index = toggles.indexOf(e.getSource());
            synchronized (STATES_LOCK) {
                filterStates.put(filtersDesc.getName(index),
                                Boolean.valueOf(toggle.isSelected()));
            }
            // notify
            fireChange();
        }
        
        private void fireChange () {
            FilterChangeListener lCopy;
            synchronized (L_LOCK) {
                // no listener = no notification
                if (clientL == null) {
                    return;
                }
                lCopy = clientL;
            }
            
            // notify listener
            lCopy.filterStateChanged(new ChangeEvent(FiltersManager.this));
        }

        @Override
        public void removeNotify() {
            //remember filter settings
            if( null != filterStates ) {
                Preferences prefs = getPreferences();
                for( String filterName : filterStates.keySet() ) {
                    prefs.putBoolean( filterName, filterStates.get( filterName ) );
                }
            }
            super.removeNotify();
        }
    
    } // end of FiltersComponent

    private static class Space extends JComponent {

        private static final Dimension SPACE = new Dimension(3, 3);

        @Override
        public Dimension getPreferredSize() {
            return getMinimumSize();
        }
        @Override
        public Dimension getMaximumSize() {
            return getMinimumSize();
        }
        @Override
        public Dimension getMinimumSize() {
            return SPACE;
        }
    }
    
}
