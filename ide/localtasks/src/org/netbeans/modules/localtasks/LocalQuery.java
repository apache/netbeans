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

package org.netbeans.modules.localtasks;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.localtasks.task.LocalTask;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Vrabec
 */
public final class LocalQuery {
    
    private static LocalQuery instance;
    private final PropertyChangeSupport support;
    private QueryProvider.IssueContainer<LocalTask> delegatingIssueContainer;
    
    static LocalQuery getInstance () {
        if (instance == null) {
            instance = new LocalQuery();
        }
        return instance;
    }

    private LocalQuery () {
        support = new PropertyChangeSupport(this);
    }

    @NbBundle.Messages({
        "LBL_LocalQuery.displayName=All Tasks"
    })
    String getDisplayName () {
        return Bundle.LBL_LocalQuery_displayName();
    }

    @NbBundle.Messages({
        "LBL_LocalQuery.tooltip=All tasks from the local repository"
    })
    String getTooltip () {
        return Bundle.LBL_LocalQuery_tooltip();
    }

    Collection<LocalTask> getIssues () {
        return LocalRepository.getInstance().getTasks();
    }

    void refresh () {
        try {
            if(delegatingIssueContainer != null) {
                delegatingIssueContainer.refreshingStarted();
                for(LocalTask t : getIssues()) {
                    delegatingIssueContainer.add(t);
                }
            }
        } finally {
            fireFinished();
        }
    }

    void addPropertyChangeListener (PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    void removePropertyChangeListener (PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    void setIssueContainer(QueryProvider.IssueContainer<LocalTask> c) {
        delegatingIssueContainer = c;
    }

    void addTask(LocalTask lt) {
        if(delegatingIssueContainer != null) {
            delegatingIssueContainer.add(lt);
        }
        fireFinished();
    }

    void removeTask(LocalTask lt) {
        if(delegatingIssueContainer != null) {
            delegatingIssueContainer.remove(lt);
        }
        fireFinished();
    }
    
    void fireFinished() {
        if(delegatingIssueContainer != null) {
            delegatingIssueContainer.refreshingFinished();
        }        
    }
    
}
