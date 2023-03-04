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

package org.netbeans.modules.search;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;

/**
 * The same as the {@link FindInFilesAction} except that this action asks for
 * a replacement string and allows to replace some or all matching substrings
 * with the given replacement string.
 *
 * @author  Marian Petras
 */
@ActionID(id = "org.netbeans.modules.search.ReplaceInFilesAction", category = "Edit")
@ActionRegistration(lazy = false, displayName = "#LBL_Action_ReplaceInProjects")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-H"),
    @ActionReference(path = "Menu/Edit", position = 2500)
})
public class ReplaceInFilesAction extends FindInFilesAction {

    static final long serialVersionUID = 4554342565076372612L;

    public ReplaceInFilesAction() {
        this(false);
    }

    protected ReplaceInFilesAction(boolean preferScopeSelection) {
        super("LBL_Action_ReplaceInProjects", preferScopeSelection);    //NOI18N
    }

    @Override
    protected void initialize() {
        super.initialize();

        putProperty(REPLACING, Boolean.TRUE, false);
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/find.gif";    //PENDING   //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(
                "org.netbeans.modules.search.ReplaceInFilesAction");    //NOI18N
    }

    public static class Selection extends ReplaceInFilesAction {

        public Selection() {
            super(true);
        }
    }
}
