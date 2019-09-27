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
package org.netbeans.modules.localtasks.task;

import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.localtasks.task.LocalTask.TaskReference;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Stola
 */
class SubtaskTableModel extends DefaultTableModel {
    private final Object[][] data;

    public SubtaskTableModel (List<TaskReference> subtasks) {
        super(columnNames(), subtasks.size());
        this.data = data(subtasks);
    }

    @NbBundle.Messages({
        "SubtaskTableModel.repository=Repository",
        "SubtaskTableModel.summary=Summary"
    })
    private static String[] columnNames () {
        String summary = Bundle.SubtaskTableModel_summary();
        String repository = Bundle.SubtaskTableModel_repository();
        return new String[] { summary, repository };
    }

    @Override
    public Class<?> getColumnClass (int columnIndex) {
        Class<?> clazz = String.class;
        return clazz;
    }

    @NbBundle.Messages({
        "MSG_SubtaskPanel.repository.unknown=Unknown",
        "# {0} - task id", "MSG_SubtaskPanel.task.unknown=#{0} - Unknown"
    })
    private static Object[][] data (List<TaskReference> subtasks) {
        Object[][] data = new Object[subtasks.size()][];
        int count = 0;
        for (TaskReference ref : subtasks) {
            String repositoryId = ref.getRepositoryId();
            String taskId = ref.getTaskId();
            Repository repository = null;
            for (Repository r : RepositoryManager.getInstance().getRepositories()) {
                if (repositoryId.equals(r.getId())) {
                    repository = r;
                    break;
                }
            }
            String repositoryName;
            String taskName = null;
            if (repository == null) {
                repositoryName = Bundle.MSG_SubtaskPanel_repository_unknown();
            } else {
                repositoryName = repository.getDisplayName();
                if (!taskId.isEmpty()) {
                    Issue[] tasks = repository.getIssues(taskId);
                    for (Issue t : tasks) {
                        if (taskId.equals(t.getID())) {
                            taskName = t.getDisplayName();
                        }
                    }
                }
            }
            if (taskName == null) {
                taskName = Bundle.MSG_SubtaskPanel_task_unknown(taskId);
            }

            data[count] = new Object[]{
                taskName,
                repositoryName,
                repositoryId,
                taskId
            };
            count++;
        }
        return data;
    }

    @Override
    public boolean isCellEditable (int row, int column) {
        return false;
    }

    @Override
    public Object getValueAt (int row, int column) {
        return data[row][column];
    }

}
