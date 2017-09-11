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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tasklist.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.tasklist.impl.*;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
class Util {
    
    /** Creates a new instance of Util */
    private Util() {
    }
    
    /**
     * 
     * @param t 
     * @return 
     */
    public static Action getDefaultAction( Task t ) {
        return new OpenTaskAction( t );
    }
    
    public static JPopupMenu createPopup( TaskListTable table ) {
        JPopupMenu popup = new JPopupMenu();
        //show source
        Task t = table.getSelectedTask();
        if( null != t ) {
            popup.add( getDefaultAction(t) );
            popup.addSeparator();
            //task's custom actions
            Action[] actions = Accessor.getActions(t);
            if( null != actions ) {
                for( Action a : actions ) {
                    if( null != a ) {
                        popup.add(a);
                    } else {
                        popup.addSeparator();
                    }
                }
                if( actions.length > 0  && actions[actions.length-1] != null ) {
                    popup.addSeparator();
                }
            }
        }
        //scope
        JMenu scopeMenu = new JMenu( NbBundle.getMessage( Util.class, "LBL_Scope" ) ); //NOI18N
        ScanningScopeList scopeList = ScanningScopeList.getDefault();
        for( TaskScanningScope scope : scopeList.getTaskScanningScopes() ) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem( new SwitchScopeAction(scope) );
            item.setSelected( scope.equals( TaskManagerImpl.getInstance().getScope() ) );
            scopeMenu.add( item );
        }
        popup.add( scopeMenu );
        //filter
        JMenu filterMenu = new JMenu( NbBundle.getMessage( Util.class, "LBL_Filter" ) ); //NOI18N
        FiltersMenuButton.fillMenu( null, filterMenu );
        popup.add( filterMenu );
        
        popup.addSeparator();
        //refresh
        popup.add( new RefreshAction() );
        popup.addSeparator();
        //list options
        JMenu sortMenu = createSortMenu( table );
        popup.add( sortMenu );
        
        return popup;
    }
    
    private static JMenu createSortMenu( TaskListTable table ) {
        JMenu res = new JMenu( NbBundle.getMessage( Util.class, "LBL_SortBy" ) ); //NOI18N
        for( int i=1; i<table.getColumnCount(); i++ ) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem( new SwitchSortAction( table, i ) );
            item.setSelected( i == table.getSortColumn() );
            res.add( item );
        }
        res.addSeparator();
        JRadioButtonMenuItem item = new JRadioButtonMenuItem( new SwitchSortOrderAction(table, true) );
        item.setSelected( table.isAscendingSort() );
        res.add( item );
        item = new JRadioButtonMenuItem( new SwitchSortOrderAction(table, false) );
        item.setSelected( !table.isAscendingSort() );
        res.add( item );
        return res;
    }
    
    private static class SwitchScopeAction extends AbstractAction {
        private TaskScanningScope scope;
        public SwitchScopeAction( TaskScanningScope scope ) {
            super( Accessor.getDisplayName( scope ), new ImageIcon( Accessor.getIcon( scope ) ) );
            this.scope = scope;
        }
    
        public void actionPerformed( ActionEvent e ) {
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            tm.observe( scope, tm.getFilter() );
        }
    }
    
    private static class RefreshAction extends AbstractAction {
        public RefreshAction() {
            super( NbBundle.getMessage( Util.class, "LBL_Refresh" ) ); //NOI18N
        }
    
        public void actionPerformed( ActionEvent e ) {
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            tm.clearCache();
            tm.refresh( tm.getScope() );
        }
    }
    
    private static class SwitchSortAction extends AbstractAction {
        private TaskListTable table;
        private int col;
        
        public SwitchSortAction( TaskListTable table, int col ) {
            super( table.getModel().getColumnName(col) );
            this.table = table;
            this.col = col;
        }
    
        public void actionPerformed( ActionEvent e ) {
            if( col == table.getSortColumn() )
                table.setSortColumn( -1 );
            else
                table.setSortColumn( col );
            table.getTableHeader().repaint();
        }
    }
    
    private static class SwitchSortOrderAction extends AbstractAction {
        private TaskListTable table;
        private boolean asc;
        
        public SwitchSortOrderAction( TaskListTable table, boolean asc ) {
            super( asc
                ? NbBundle.getMessage( Util.class, "LBL_Asc" ) //NOI18N
                : NbBundle.getMessage( Util.class, "LBL_Desc" ) ); //NOI18N
            this.table = table;
            this.asc = asc;
        }
    
        public void actionPerformed( ActionEvent e ) {
            table.setAscendingSort( asc );
            table.getTableHeader().repaint();
        }
    }
}
