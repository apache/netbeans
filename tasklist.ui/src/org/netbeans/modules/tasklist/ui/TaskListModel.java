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

package org.netbeans.modules.tasklist.ui;

import java.awt.EventQueue;
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
    // internal list of tasks - needed to recognize deleted row (deleted tasks aren't in taskList when taskRemoved method "arrive") #204655
    private List<? extends Task> listOfTasks;
    private final Object lock = new Object();

            
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
        final List<? extends Task> list = taskList.getTasks();
        return list.size();
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
        synchronized (lock) {
            final List<? extends Task> list = taskList.getTasks();
            if (list.size() > row) {
                return list.get(row);
            } else {
                return null;
            }
        }
    }

    @Override
    public void tasksAdded( final List<? extends Task> tasks ) {
        if( tasks.isEmpty() )
            return;
        final int startRow;
        final int endRow;
        synchronized (lock) {
            startRow = taskList.getTasks().indexOf(tasks.get(0));
            endRow = taskList.getTasks().indexOf(tasks.get(tasks.size() - 1));
            listOfTasks = taskList.getTasks();
        }
        if( startRow > -1 && endRow > -1 ) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireTableRowsInserted(startRow, endRow);
                }
            });
        }
    }

    @Override
    public void tasksRemoved( final List<? extends Task> tasks ) {
        if( tasks.isEmpty() )
            return;

        final int startRow;
        final int endRow;
        synchronized (lock) {
            startRow = listOfTasks.indexOf(tasks.get(0));
            endRow = listOfTasks.indexOf(tasks.get(tasks.size() - 1));
            listOfTasks = taskList.getTasks();
        }
        if( startRow > -1 && endRow > -1 ) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireTableRowsDeleted( startRow, endRow );
                }
            });
        }
    }

    @Override
    public void cleared() {
        synchronized (lock) {
            listOfTasks = taskList.getTasks();
        }
        fireTableDataChanged();
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
        taskList.setComparator( comparator );
        synchronized (lock) {
            listOfTasks = taskList.getTasks();
        }
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
