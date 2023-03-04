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

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTabbedPane;
import javax.swing.table.AbstractTableModel;
import org.netbeans.swing.tabcontrol.TabDataModel;

/**
 *
 * @author S. Aubrecht
 */
class SingleRowTabTable extends TabTable {

    private final ArrayList<Integer> tabIndexes;

    public SingleRowTabTable( TabDataModel tabModel ) {
        this( tabModel, new ArrayList<Integer>(30) );
    }

    private SingleRowTabTable( TabDataModel tabModel, ArrayList<Integer> tabIndexes ) {
        super( TabTableModel.create( tabModel, tabIndexes ), JTabbedPane.HORIZONTAL );
        this.tabIndexes = tabIndexes;
    }

    void setTabs( List<Integer> tabIndexes ) {
        this.tabIndexes.clear();
        this.tabIndexes.addAll( tabIndexes );
        ((AbstractTableModel)getModel()).fireTableStructureChanged();
    }

    boolean hasTabIndex( int tabIndex ) {
        return tabIndexes.contains( tabIndex );
    }
}
