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

package org.netbeans.modules.csl.navigation.base;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * Handles creation and manipulation with boolean state filters. 
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
    public JComponent getComponent () {
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
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable () {
                    public void run () {
                        initPanel();                        
                    }
                });
            } else {
                initPanel();
            }
        }

        /** Called only from AWT */
        private void initPanel () {
            setBorder(new EmptyBorder(1, 2, 3, 5));

            // configure toolbar
            JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorderPainted(false);
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
            Dimension space = new Dimension(3, 0);
            for (int i = 0; i < toggles.size(); i++) {
                curToggle = toggles.get(i);
                curToggle.addActionListener(this);
                toolbar.add(curToggle);
                if (i != toggles.size() - 1) {
                    toolbar.addSeparator(space);
                }
            }
            
            add(toolbar);
            
            // initialize member states map
            synchronized (STATES_LOCK) {
                filterStates = fStates;
            }
        }
        
        private JToggleButton createToggle (Map<String,Boolean> fStates, int index) {
            boolean isSelected = filtersDesc.isSelected(index);
            Icon icon = filtersDesc.getSelectedIcon(index);
            // ensure small size, just for the icon
            JToggleButton result = new JToggleButton(icon, isSelected);
            Dimension size = new Dimension(icon.getIconWidth() + 6, icon.getIconHeight() + 4);
            result.setPreferredSize(size);
            result.setMargin(new Insets(2,3,2,3));
            result.setToolTipText(filtersDesc.getTooltip(index));
            
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
    
    } // end of FiltersComponent
    
}
