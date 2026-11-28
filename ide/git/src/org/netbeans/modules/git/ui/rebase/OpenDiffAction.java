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

package org.netbeans.modules.git.ui.rebase;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTable;
import org.openide.util.NbBundle;

/**
 * The action opening the diff of the row selected in a table. 
 *
 * @author mbien
 */
@NbBundle.Messages({
    "CTL_OpenDiffAction=Diff..."
})
public final class OpenDiffAction extends AbstractAction {

    private final JTable table;
    private final InteractiveRebasePanel panel;

    private OpenDiffAction(JTable table, InteractiveRebasePanel panel) {
        this.table = table;
        this.panel = panel;
        putValue(NAME, Bundle.CTL_OpenDiffAction());
    }

    /**
     * @param table the table the action reads the selected row of
     * @param panel the panel opening the diff of the rows of the table
     * @return the action
     */
    public static OpenDiffAction getAction(JTable table, InteractiveRebasePanel panel) {
        return new OpenDiffAction(table, panel);
    }

    /**
     * Whether the diff of the row selected in the table can be opened.
     *
     * @return <code>true</code> if a row is selected and the panel can open
     *         the diff of it
     */
    public boolean canOpenDiff() {
        int row = table.getSelectedRow();
        return row >= 0 && panel.canOpenDiff(row);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (canOpenDiff()) {
            panel.openDiff(table.getSelectedRow());
        }
    }
}
