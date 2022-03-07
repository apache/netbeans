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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.tasklist.impl.Accessor;
import org.netbeans.modules.tasklist.impl.TaskComparator;
import org.netbeans.modules.tasklist.impl.TaskList;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;

/**
 *
 * @author S. Aubrecht
 */
class FoldingTaskListModel extends TaskListModel {
    
    private final LinkedList<FoldingGroup> groups = new LinkedList<FoldingGroup>();
    private HashMap<String,FoldingGroup> groupMap = new HashMap<String,FoldingGroup>(10);
    private final Logger LOG = Logger.getLogger(this.getClass().getName());
    
    /** Creates a new instance of FoldingTaskListModel */
    public FoldingTaskListModel( TaskList taskList ) {
        super( taskList );
        tasksAdded( taskList.getTasks() );        
    }
    
    @Override
    public int getRowCount() {
        if( null == taskList )
            return 0;
        int count = 0;
        synchronized( groups ) {
            for( FoldingGroup g : groups ) {
                count += g.getRowCount();
            }
        }
        return count;
    }

    @Override
    public Class<?> getColumnClass( int column ) {
        if( COL_GROUP == column )
            return FoldingGroup.class;
        return super.getColumnClass( column );
    }
    
    @Override
    protected Task getTaskAtRow( int row ) {
        synchronized( groups ) {
            int groupRow = 0;
            for( FoldingGroup g : groups ) {
                synchronized (g.TASK_LOCK) {
                    if( row < groupRow+g.getRowCount() ) {
                        int indexInGroup = row-groupRow-1;
                        if (indexInGroup == -1) {
                            return null;
                        }
                        return g.getTaskAt( indexInGroup);
                    }
                    groupRow += g.getRowCount();
                }
            }
        }
        return null;
    }

    @Override
    public Object getValueAt(int row, int col) {
        FoldingGroup group = getGroupAtRow( row );
        if( null != group ) {
            switch( col ) {
                case COL_GROUP: {
                    return group;
                }
                default:
                    return null;
            }
        }
        return super.getValueAt( row, col );
    }
    
    FoldingGroup getGroupAtRow( int row ) {
        int groupRow = 0;
        synchronized( groups ) {
            for( FoldingGroup g : groups ) {
                if( g.isEmpty() )
                    continue;
                if( row == groupRow )
                    return g;
                groupRow += g.getRowCount();
            }
        }
        return null;
    }
    
    private Map<FoldingGroup,List<Task>> divideByGroup( List<? extends Task> tasks ) {
        Map<FoldingGroup,List<Task>> grouppedTasksMap = new HashMap<FoldingGroup,List<Task>>( groupMap.size() );
        for( Task t : tasks ) {
            TaskGroup tg = Accessor.getGroup( t );
            FoldingGroup group = groupMap.get( tg.getName() );
            if( null == group ) {
                synchronized( groups ) {
                    group = new FoldingGroup( tg );
                    groupMap.put( tg.getName(), group );
                    groups.add( group );
                    Collections.sort( groups );
                }
            }
            List<Task> tasksInGroup = grouppedTasksMap.get( group );
            if( null == tasksInGroup ) {
                tasksInGroup = new LinkedList<Task>();
                grouppedTasksMap.put( group, tasksInGroup );
            }
            tasksInGroup.add( t );
        }
        return grouppedTasksMap;
    }
    
    @Override
    public void tasksAdded( List<? extends Task> tasks ) {
        if( tasks.isEmpty() )
            return;
        Map<FoldingGroup,List<Task>> grouppedTasksMap = divideByGroup( tasks );
        for( Map.Entry<FoldingGroup, List<Task>> entry : grouppedTasksMap.entrySet() ) {
            FoldingGroup fg = entry.getKey( );
            List<Task> tasksInGroup = entry.getValue();
            fg.add( tasksInGroup );
        }
        sortTaskList();
    }

    @Override
    public void tasksRemoved( List<? extends Task> tasks ) {
        if( tasks.isEmpty() )
            return;
        Map<FoldingGroup,List<Task>> grouppedTasksMap = divideByGroup( tasks );
        for( Map.Entry<FoldingGroup, List<Task>> entry : grouppedTasksMap.entrySet() ) {
            FoldingGroup fg = entry.getKey( );
            List<Task> tasksInGroup = entry.getValue();
            fg.remove( tasksInGroup );
        }
    }

    @Override
    public void cleared() {
        synchronized( groups ) {
            for( FoldingGroup fg : groups ) {
                fg.clear();
            }
        }
    }
    
    public boolean isGroupRow( int row ) {
        return null != getGroupAtRow( row );
    }
    
    public void toggleGroupExpanded( int row ) {
        FoldingGroup fg = getGroupAtRow( row );
        if( null != fg )
            fg.toggleExpanded();
    }
    
    private int getFoldingGroupStartingRow( FoldingGroup fg ) {
        if( fg.isEmpty() )
            return -1;
        int startingRow = 0;
        synchronized( groups ) {
            int groupIndex = groups.indexOf( fg );
            for( int i=0; i<groupIndex; i++ ) {
                startingRow += groups.get( i ).getRowCount();
            }
        }
        return startingRow;
    }
    
    @Override
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
        if( null != groups ) {
            synchronized( groups ) {
                for( FoldingGroup fg : groups ) {
                    fg.setComparator( comparator );
                }
            }

            Settings.getDefault().setSortingColumn( sortingCol );
            Settings.getDefault().setAscendingSort( ascending );
        }
        
        fireTableDataChanged();
    }
    
    class FoldingGroup implements Comparable<FoldingTaskListModel.FoldingGroup> {
        private TaskGroup tg;
        
        private final Object TASK_LOCK = new Object();
        private TreeSet<Task> sortedTasks = new TreeSet<Task>( getComparator() );
        private ArrayList<Task> tasksList;
        
        private boolean isExpanded;
        private Comparator<Task> comparator;
            
        public FoldingGroup( TaskGroup tg ) {
            this.tg = tg;
            isExpanded = Settings.getDefault().isGroupExpanded( tg.getName() );
        }
        
        public void add( List<Task> newTasks ) {
            boolean wasEmpty = isEmpty();
            
            synchronized( TASK_LOCK ) {
                sortedTasks.addAll( newTasks );
                tasksList = null;
            }
            int startingRow = getFoldingGroupStartingRow( this );
            
            if( wasEmpty ) {
                fireTableRowsInserted( startingRow, startingRow+getRowCount() );
            } else {
                if( isExpanded ) {
                    int firstRow = Integer.MAX_VALUE;
                    int lastRow = Integer.MIN_VALUE;
                    for( Task t : newTasks ) {
                        int index = getTasksList().indexOf( t );
                        if( index < firstRow )
                            firstRow = index;
                        if( index > lastRow )
                            lastRow = index;
                    }
                    fireTableRowsInserted( firstRow+startingRow+1, lastRow+startingRow+1 );
                }
                fireTableCellUpdated( startingRow, COL_DESCRIPTION );
            }
        }
        
        public void remove( List<Task> removedTasks ) {
            int firstRow = Integer.MAX_VALUE;
            int lastRow = Integer.MIN_VALUE;
            int rowCount = getRowCount();
            if( isExpanded ) {
                for( Task t : removedTasks ) {
                    int index = getTasksList().indexOf( t );
                    if( index < firstRow )
                        firstRow = index;
                    if( index > lastRow )
                        lastRow = index;
                }
            }
            synchronized( TASK_LOCK ) {
                sortedTasks.removeAll( removedTasks );
                tasksList = null;
            }            
            int startingRow = getFoldingGroupStartingRow( this );
            if( isEmpty() ) {
                fireTableRowsDeleted( startingRow, startingRow+rowCount );
            } else {
                if( isExpanded ) {
                    fireTableRowsDeleted( firstRow+startingRow+1, lastRow+startingRow+1 );
                }
                fireTableCellUpdated( startingRow, COL_DESCRIPTION );
            }
        }
        
        public void clear() {
            if( isEmpty() )
                return;
            
            final int rowCount = getRowCount();
            final int startingRow = getFoldingGroupStartingRow( this );
            synchronized( TASK_LOCK ) {
                sortedTasks.clear();
                tasksList = null;
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireTableRowsDeleted( startingRow, startingRow+rowCount );
                }
            });
        }
        
        public boolean isEmpty() {
            synchronized( TASK_LOCK ) {
                return sortedTasks.isEmpty();
            }
        }
        
        public void setExpanded( boolean expand ) {
            if( isExpanded == expand )
                return;
            toggleExpanded();
        }
        
        public void toggleExpanded() {
            this.isExpanded = !isExpanded;
            
            Settings.getDefault().setGroupExpanded( tg.getName(), isExpanded );
            
            int firstRow = 0;
            synchronized( groups ) {
                int groupIndex = groups.indexOf( this );
                for( int i=0; i<groupIndex; i++ ) {
                    firstRow += groups.get( i ).getRowCount();
                }
            }
            int lastRow = firstRow + getTaskCount();
            firstRow += 1;
            
            if( isExpanded )
                fireTableRowsInserted( firstRow, lastRow );
            else
                fireTableRowsDeleted( firstRow, lastRow );
            fireTableCellUpdated( firstRow-1, COL_GROUP );
        }
        
        public int getRowCount() {
            synchronized( TASK_LOCK ) {
                return isEmpty() ? 0 : (isExpanded ? 1+sortedTasks.size() : 1);
            }
        }
        
        public int getTaskCount() {
            synchronized( TASK_LOCK ) {
                return sortedTasks.size();
            }
        }
        
        public Task getTaskAt( int index ) {
            synchronized( TASK_LOCK ) {
                return getTasksList().get( index );
            }
        }
    
        @Override
        public int compareTo(org.netbeans.modules.tasklist.ui.FoldingTaskListModel.FoldingGroup other) {
            List<? extends TaskGroup> groupList = TaskGroup.getGroups();
            int myIndex = groupList.indexOf( tg );
            int otherIndex = groupList.indexOf( other.tg );
            return myIndex - otherIndex;
        }
        
        public boolean isExpanded() {
            return isExpanded;
        }
        
        public TaskGroup getGroup() {
            return tg;
        }
        
        private List<Task> getTasksList() {
            synchronized ( TASK_LOCK ) {
                if(tasksList == null) {
                    tasksList = new ArrayList<Task>(sortedTasks);
                }
                return tasksList;
            }
        }
    
        private Comparator<Task> getComparator() {
            if( null == comparator )
                comparator = TaskComparator.getDefault();
            return comparator;
        }
        
        private void setComparator( Comparator<Task> newComparator ) {
            if( getComparator().equals( newComparator ) )
                return;
            comparator = newComparator;
            synchronized( TASK_LOCK ) {
                if( !sortedTasks.isEmpty() ) {
                    
                    TreeSet<Task> s = sortedTasks;
                    sortedTasks = new TreeSet<Task>(comparator);
                    sortedTasks.addAll(s);
                    tasksList = null;
        
                    if( isExpanded() ) {
                        int firstRow = 0;
                        synchronized( groups ) {
                            int groupIndex = groups.indexOf( this );
                            for( int i=0; i<groupIndex; i++ ) {
                                firstRow += groups.get( i ).getRowCount();
                            }
                        }
                        int lastRow = firstRow + getTaskCount();
                        firstRow += 1;

                        fireTableRowsUpdated( firstRow, lastRow );
                    }
                }
            }
        }
    }
}
