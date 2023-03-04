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

package org.netbeans.modules.tasklist.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.modules.tasklist.filter.FilterEditor;
import org.netbeans.modules.tasklist.filter.FilterRepository;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.modules.tasklist.impl.TaskManagerImpl;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
public class FiltersMenuButton extends MenuToggleButton implements PropertyChangeListener {
    
    private TaskManagerImpl taskManager;
    
    /** Creates a new instance of FiltersMenuButton */
    public FiltersMenuButton( TaskFilter currentFilter ) {
        super( ImageUtilities.loadImageIcon("org/netbeans/modules/tasklist/ui/resources/filter.png", false), ImageUtilities.loadImageIcon("org/netbeans/modules/tasklist/ui/resources/filter_rollover.png", false), 4 );  //NOI18N
        taskManager = TaskManagerImpl.getInstance();
        
        updateState( currentFilter );

        addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if( !isSelected() ) {
                    taskManager.observe( taskManager.getScope(), TaskFilter.EMPTY );
                } else {
                    openFilterEditor();
                    updateState( taskManager.getFilter() );
                }
            }
        });
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        taskManager.addPropertyChangeListener( TaskManagerImpl.PROP_FILTER, this );
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        taskManager.removePropertyChangeListener( TaskManagerImpl.PROP_FILTER, this );
    }
    
    @Override
    protected JPopupMenu getPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        fillMenu( popup, null );
        
        return popup;
    }
    
    static void fillMenu( JPopupMenu popup, JMenu menu ) {
        assert null != popup || null != menu;
        FilterRepository filterRep = FilterRepository.getDefault();
        TaskManagerImpl tm = TaskManagerImpl.getInstance();
        TaskFilter activeFilter = tm.getFilter();
        
        JRadioButtonMenuItem item = new JRadioButtonMenuItem( new CancelFilterAction() );
        item.setSelected( TaskFilter.EMPTY.equals( activeFilter ) );
        if( null == popup )
            menu.add( item );
        else
            popup.add( item );
        
        if( null == popup )
            menu.addSeparator();
        else
            popup.addSeparator();
        
        List<TaskFilter> allFilters = filterRep.getAllFilters();
        for( TaskFilter tf : allFilters ) {
            item = new JRadioButtonMenuItem( new SetFilterAction( tf ) );
            item.setSelected( activeFilter.equals( tf ) );
            if( null == popup )
                menu.add( item );
            else
                popup.add( item );
        }
        if( allFilters.size() > 0 ) {
            if( null == popup )
                menu.addSeparator();
            else
                popup.addSeparator();
        }
        
        if( null == popup )
            menu.add( new ManageFiltersAction() );
        else
            popup.add( new ManageFiltersAction() );
    }
    
    private static class CancelFilterAction extends AbstractAction {
        
        public CancelFilterAction() {
            super( NbBundle.getMessage( FiltersMenuButton.class, "LBL_CancelFilter" ) ); //NOI18N
        }
    
        public void actionPerformed(ActionEvent e) {
            FilterRepository.getDefault().setActive( null );
            try {
                FilterRepository.getDefault().save();
            } catch( IOException ioE ) {
                Logger.getLogger( FiltersMenuButton.class.getName() ).log( Level.INFO, ioE.getMessage(), ioE );
            }
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            tm.observe( tm.getScope(), TaskFilter.EMPTY );
        }
    }
    
    private static class SetFilterAction extends AbstractAction {
        private TaskFilter filter;
        public SetFilterAction( TaskFilter filter ) {
            super( filter.getName() );
            this.filter = filter;
        }
    
        public void actionPerformed(ActionEvent e) {
            FilterRepository.getDefault().setActive( filter );
            try {
                FilterRepository.getDefault().save();
            } catch( IOException ioE ) {
                Logger.getLogger( FiltersMenuButton.class.getName() ).log( Level.INFO, ioE.getMessage(), ioE );
            }
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            tm.observe( tm.getScope(), filter );
        }
    }
    
    private static class ManageFiltersAction extends AbstractAction {
        public ManageFiltersAction() {
            super( NbBundle.getMessage( FiltersMenuButton.class, "LBL_EditFilters" ) ); //NOI18N
        }
    
        public void actionPerformed(ActionEvent arg0) {
            openFilterEditor();
        }
    }

    public void propertyChange( PropertyChangeEvent e ) {
        updateState( taskManager.getFilter() );
    }
    
    private void updateState( TaskFilter filter ) {
        if( null == filter || TaskFilter.EMPTY.equals( filter ) ) {
            setSelected( false );
            setToolTipText( NbBundle.getMessage( FiltersMenuButton.class, "HINT_SelectFilter" ) ); //NOI18N
            FilterRepository.getDefault().setActive( null );
        } else {
            setSelected( true );
            setToolTipText( filter.getName() );
            FilterRepository.getDefault().setActive( filter );
        }
    }

    
    private static void openFilterEditor() {
        FilterRepository filterRep = FilterRepository.getDefault();
        FilterRepository clone = (FilterRepository)filterRep.clone();
        FilterEditor fe = new FilterEditor( clone );
        if( fe.showWindow() ) {
            filterRep.assign(clone);
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            tm.observe( tm.getScope(), filterRep.getActive() );
            try {
                filterRep.save();
            } catch( IOException ioE ) {
                Logger.getLogger( FiltersMenuButton.class.getName() ).log( Level.INFO, ioE.getMessage(), ioE );
            }
        }
    }
}
