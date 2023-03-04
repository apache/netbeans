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

package org.netbeans.modules.subversion.ui.actions;

import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.*;
import org.netbeans.modules.subversion.util.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

/**
 * Abstract base for Show All Changes, Show All Diffs,
 * Update All and Commit All actions.
 *
 * <p>TODO add context listening and resetting logic
 * It means that opened Subversion view, Diff view
 * should react to newly opened/closed projects.
 *
 * @author Petr Kuzel
 */
public abstract class AbstractAllAction extends SystemAction {
    
    /** Creates a new instance of AbstractAllAction */
    public AbstractAllAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N                
    }

    /**
     * Enabled for at least one opened project
     */
    public boolean isEnabled() {
        if (super.isEnabled()) {
            Project projects[] = OpenProjects.getDefault().getOpenProjects();
            for (int i = 0; i < projects.length; i++) {
                Project project = projects[i];
                if (SvnUtils.isVersionedProject(project, false)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }
    
}
