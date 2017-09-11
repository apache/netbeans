/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
        Class clazz = String.class;
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
