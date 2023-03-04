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

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.db.api.sql.execute.SQLExecuteOptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.BooleanStateAction;

/**
 * This action lets you toggle between creating a new tab for each execution
 * or reusing the same tab each time.
 *
 * @author David Van Couvering, Andrei Badea
 */
public class ToggleKeepOldResultTabsAction extends BooleanStateAction implements PropertyChangeListener {

    private static final String ICON_PATH = "org/netbeans/modules/db/sql/editor/resources/keepoldresulttabs.png"; // NOI18N

    private boolean initialized;

    @Override
    public String getName() {
        return NbBundle.getMessage(ToggleKeepOldResultTabsAction.class, "LBL_ToggleKeepOldResultTabsAction"); // NOI18N
    }

    @Override
    protected String iconResource() {
        return ICON_PATH;
    }

    @Override
    public boolean getBooleanState() {
        synchronized (this) {
            if (!initialized) {
                SQLExecuteOptions options = SQLExecuteOptions.getDefault();
                options.addPropertyChangeListener(WeakListeners.propertyChange(this, options));
                keepOldResultTabsChanged();
                initialized = true;
            }
        }
        return super.getBooleanState();
    }

    @Override
    public void setBooleanState(boolean value) {
        SQLExecuteOptions.getDefault().setKeepOldResultTabs(value);
        super.setBooleanState(value);
    }

    private void keepOldResultTabsChanged() {
        setBooleanState(SQLExecuteOptions.getDefault().isKeepOldResultTabs());
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ToggleKeepOldResultTabsAction.class);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        keepOldResultTabsChanged();
    }
}
