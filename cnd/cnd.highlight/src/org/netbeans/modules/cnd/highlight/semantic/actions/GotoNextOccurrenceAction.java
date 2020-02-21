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
package org.netbeans.modules.cnd.highlight.semantic.actions;

import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.editor.BaseAction;
import org.openide.util.NbBundle;

/**
 * Action to navigate to next (regarding cursor position) highlighting made
 * by last Mark Occurrences process.
 *
 */
public final class GotoNextOccurrenceAction extends BaseAction {

    private static final String actionName = EditorActionNames.gotoNextOccurrence; // NOI18N
    private static GotoNextOccurrenceAction instance;

    public static synchronized GotoNextOccurrenceAction getInstance() {
        if (instance == null) {
            instance = new GotoNextOccurrenceAction();
        }
        return instance;
    }

    public GotoNextOccurrenceAction() {
        super(actionName);
        putValue(SHORT_DESCRIPTION, getDefaultShortDescription());
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent txt) {
        SemanticUtils.navigateToOccurrence(true);
    }

    @Override
    protected Object getDefaultShortDescription() {
        return NbBundle.getMessage(GotoNextOccurrenceAction.class, actionName);
    }
}
