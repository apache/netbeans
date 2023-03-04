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
package org.netbeans.core.multitabs.impl;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.tabcontrol.TabDataModel;

/**
 *
 * @author S. Aubrecht
 */
public class RowPerProjectTabDisplayer extends MultiRowTabDisplayer implements ChangeListener {

    public RowPerProjectTabDisplayer( final TabDataModel tabModel, int tabsLocation ) {
        super( tabModel, tabsLocation );
    }

    @Override
    void initRows() {
        int rowCount = ProjectSupport.getDefault().getOpenProjects().length;
        if( rowCount > 1 ) {
            rowCount++; //add an extra row for non-project tabs
        }
        for( int i=0; i<rowCount; i++ ) {
            addRowTable();
        }
    }

    private void adjustRows( int projectCount ) {
        int rowCount = projectCount;
        if( rowCount > 1 ) {
            rowCount++; //add an extra row for non-project tabs
        }
        while( rowCount < rowTables.size() && rowTables.size() > 1 ) {
            removeTable();
        }
        while( rowCount > rowTables.size() ) {
            addRowTable();
        }
        layoutManager.invalidate();
    }

    private void removeTable() {
        if( rowTables.size() < 2 )
            return;
        SingleRowTabTable table = rowTables.get( rowTables.size()-1 );
        table.removeMouseWheelListener( this );
        table.removeMouseListener( controller );
        table.removeMouseListener( closeHandler );
        table.removeMouseMotionListener( closeHandler );
        table.getSelectionModel().removeListSelectionListener( this );
        table.getColumnModel().getSelectionModel().removeListSelectionListener( this );
        rowTables.remove( table );
        rowPanel.remove( table );
    }

    @Override
    public void addNotify() {
        ProjectSupport.getDefault().addChangeListener(this);
        super.addNotify();
        layoutManager.invalidate();
    }
    
    @Override
    public void removeNotify() {
        ProjectSupport.getDefault().removeChangeListener(this);
        super.removeNotify();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        final int projectCount = ProjectSupport.getDefault().getOpenProjects().length;
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                adjustRows( projectCount );
            }
        });
    }
}