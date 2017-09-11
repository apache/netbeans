/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
