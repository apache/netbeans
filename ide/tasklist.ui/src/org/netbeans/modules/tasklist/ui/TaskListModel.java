/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tasklist.ui;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.tasklist.impl.Accessor;
import org.netbeans.modules.tasklist.impl.TaskComparator;
import org.netbeans.modules.tasklist.impl.TaskList;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.netbeans.spi.tasklist.Task;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
class TaskListModel extends AbstractTableModel implements TaskList.Listener {
    
    protected TaskList taskList;

    protected static final int COL_GROUP = 0;
    protected static final int COL_DESCRIPTION = 1;
    protected static final int COL_FILE = 2;
    protected static final int COL_LOCATION = 3;
    // listOfTasks holds the list of tasks relevant for the Swing components. It
    // is a copy of the task held by the TaskList instance
    private List<? extends Task> listOfTasks;

            
    /** Creates a new instance of TaskListModel */
    public TaskListModel( TaskList taskList ) {
        this.taskList = taskList;
        listOfTasks = taskList.getTasks();
        sortingCol = Settings.getDefault().getSortingColumn();
        ascending = Settings.getDefault().isAscendingSort();
        sortTaskList();
    }
    
    @Override
    public int getRowCount() {
        return listOfTasks.size();
    }
    
    @Override
    public int getColumnCount() {
        return 4;
    }
    
    @Override
    public Class<?> getColumnClass( int column ) {
        if( COL_GROUP == column )
            return TaskGroup.class;
        return super.getColumnClass( column );
    }
    
    @Override
    public String getColumnName(int column) {
        switch( column ) {
            case COL_GROUP: //group icon
                return ""; //NOI18N
            case COL_DESCRIPTION:
                return NbBundle.getMessage( TaskListModel.class, "LBL_COL_Description" ); //NOI18N
            case COL_FILE:
                return NbBundle.getMessage( TaskListModel.class, "LBL_COL_File" ); //NOI18N
            case COL_LOCATION:
                return NbBundle.getMessage( TaskListModel.class, "LBL_COL_Location" ); //NOI18N
        }
        return super.getColumnName( column );
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        Task t = getTaskAtRow( row );
        if( null != t ) {
            switch( col ) {
                case COL_GROUP: //group icon
                    return Accessor.getGroup( t );
                case COL_DESCRIPTION:
                    return Accessor.getDescription( t );
                case COL_FILE: {
                    return Accessor.getFileNameExt( t );
                }
                case COL_LOCATION: {
                    return Accessor.getLocation( t );
                }
            }
        }
        return null;
    }
    
    protected Task getTaskAtRow( int row ) {
        if (listOfTasks.size() > row) {
            return listOfTasks.get(row);
        } else {
            return null;
        }
    }

    @Override
    public void tasksAdded(final List<? extends Task> tasks) {
        if (! tasks.isEmpty()) {
            fireTasksChanged(true, tasks, listOfTasks, taskList.getTasks());
        }
    }

    @Override
    public void tasksRemoved(final List<? extends Task> tasks) {
        if (! tasks.isEmpty()) {
            fireTasksChanged(false, tasks, listOfTasks, taskList.getTasks());
        }
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    private void fireTasksChanged(
            boolean add,
            List<? extends Task> changedTasks,
            List<? extends Task> oldList,
            List<? extends Task> newList
    ) {
        EventQueue.invokeLater(() -> {
            // Get the right list for index identification, for adding the
            // indices are taken from the new list, for removal from the old
            // list
            List<? extends Task> indexTaskList = add ? newList : oldList;
            // Find the tasks that were added/removed in the old list (these are the
            // relevant indices.
            int[] indices = new int[changedTasks.size()];
            for (int i = 0; i < changedTasks.size(); i++) {
                indices[i] = indexTaskList.indexOf(changedTasks.get(i));
            }
            // Update the list of tasks to the new list
            listOfTasks = newList;

            // Ensure the indices are sorted ascending
            Arrays.sort(indices);

            // Check that all tasks are found and that they are consecutive. If
            // that is the case use fireTableRowsInserted/fireTableRowsDeleted
            // with range, else indicate all data has changed
            int lastIdx = indices.length - 1;
            if (indices[0] < 0 || ((indices[lastIdx] - indices[0]) + 1) != indices.length) {
                fireTableDataChanged();
            } else {
                if(add) {
                    fireTableRowsInserted(indices[0], indices[lastIdx]);
                } else {
                    fireTableRowsDeleted(indices[0], indices[lastIdx]);
                }
            }
        });
    }

    @Override
    public void cleared() {
        EventQueue.invokeLater(() -> {
            listOfTasks = taskList.getTasks();
            fireTableDataChanged();
        });
    }
    
    protected int sortingCol = -1;
    protected boolean ascending = true;
    
    public void toggleSort( int column ) {
        if( column != sortingCol ) {
            sortingCol = column;
            ascending = true;
        } else {
            if( ascending ) {
                ascending = false;
            } else {
                sortingCol = -1;
            }
        }
        
        sortTaskList();
    }
    
    protected void sortTaskList() {
        Comparator<Task> comparator;
        switch( sortingCol ) {
        case COL_DESCRIPTION:
            comparator = TaskComparator.getDescriptionComparator( ascending );
            break;
        case COL_LOCATION:
            comparator = TaskComparator.getLocationComparator( ascending );
            break;
        case COL_FILE:
            comparator = TaskComparator.getFileComparator( ascending );
            break;
        default:
            comparator = TaskComparator.getDefault();
            break;
        }
        // This happens on the EDT (mouseClick) so no need to push it to the
        // EDT explicitly
        taskList.setComparator( comparator );
        listOfTasks = taskList.getTasks();
        Settings.getDefault().setSortingColumn( sortingCol );
        Settings.getDefault().setAscendingSort( ascending );

        fireTableDataChanged();
    }
    
    public int getSortingColumnn() {
        return sortingCol;
    }
    
    public boolean isAscendingSort() {
        return ascending;
    }
    
    public void setAscendingSort( boolean asc ) {
        if( sortingCol >= 0 ) {
            ascending = asc;
        
            sortTaskList();
        }
    }
    
    void attach() {
        taskList.addListener( this );
    }
    
    void detach() {
        taskList.removeListener( this );
    }
}
