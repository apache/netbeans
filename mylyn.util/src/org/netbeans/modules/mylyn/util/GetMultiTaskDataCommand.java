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

package org.netbeans.modules.mylyn.util;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

/**
 * Retrieves the TaskData for all given issue ids
 * 
 * @author Tomas Stupka
 */
public class GetMultiTaskDataCommand extends BugtrackingCommand {

    private final AbstractRepositoryConnector repositoryConnector;
    private final TaskRepository taskRepository;
    private final Set<String> ids;
    private final TaskDataCollector collector;

    public GetMultiTaskDataCommand(AbstractRepositoryConnector repositoryConnector, TaskRepository taskRepository, TaskDataCollector collector, Set<String> ids) {
        this.taskRepository = taskRepository;
        this.repositoryConnector = repositoryConnector;
        this.ids = ids;
        this.collector = collector;
    }

    @Override
    public void execute() throws CoreException {
        
        Logger log = Logger.getLogger(this.getClass().getName());
        if(log.isLoggable(Level.FINE)) {
            log.log(
                Level.FINE, 
                "executing GetMultiTaskDataCommand for tasks: {0}", // NOI18N
                print(ids));    
        }
        
        repositoryConnector.getTaskDataHandler().getMultiTaskData(
                taskRepository,
                ids,
                collector,
                new NullProgressMonitor());
    }

    private String print(Set<String> ids) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String string : ids) {
            sb.append(string);
            if(++i < ids.size()) {
                sb.append(",");                                                 // NOI18N
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GetMultiTaskDataCommand [repository=");                      // NOI18N
        sb.append(taskRepository.getUrl());
        sb.append(",...]");                                                     // NOI18N
        return sb.toString();
    }

}
