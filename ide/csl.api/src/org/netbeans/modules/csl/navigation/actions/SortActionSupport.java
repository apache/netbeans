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

package org.netbeans.modules.csl.navigation.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.modules.csl.navigation.ClassMemberFilters;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/** 
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * 
 * "Radio button" type action, base class designed for subclassing
 *
 * @author Dafe Simonek
 */
public abstract class SortActionSupport extends AbstractAction implements Presenter.Popup {
    
    private JRadioButtonMenuItem menuItem;
    protected ClassMemberFilters filters;
    
    /** Creates a new instance of SortByNameAction */
    public SortActionSupport ( ClassMemberFilters filters ) {
        this.filters = filters;
    }
    
    public final JMenuItem getPopupPresenter() {
        JMenuItem result = obtainMenuItem();
        updateMenuItem();
        return result;
    }
    
    protected final JRadioButtonMenuItem obtainMenuItem () {
        if (menuItem == null) {
            menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
        return menuItem;
    }
    
    protected abstract void updateMenuItem ();
    
    
    /** Enables sorting by names when selected
     */
    public static final class SortByNameAction extends SortActionSupport {
        
        public SortByNameAction ( ClassMemberFilters filters) {
            super(filters);
            putValue(Action.NAME, NbBundle.getMessage(SortByNameAction.class, "LBL_SortByName")); //NOI18N
        }
    
        public void actionPerformed (ActionEvent e) {
            filters.setNaturalSort(false);
            updateMenuItem();
        }

        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(!filters.isNaturalSort());
        }
    } // end of SortByNameAction

    /** Enables sorting by names when selected
     */
    public static final class SortBySourceAction extends SortActionSupport {
        
        public SortBySourceAction ( ClassMemberFilters filters ) {
            super(filters);
            putValue(Action.NAME, NbBundle.getMessage(SortBySourceAction.class, "LBL_SortBySource")); //NOI18N
        }
    
        public void actionPerformed (ActionEvent e) {
            filters.setNaturalSort(true);
            updateMenuItem();
        }

        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(filters.isNaturalSort());
        }
    } // end of SortBySourceAction
    
    
}
