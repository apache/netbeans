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

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.awt.Component;
import org.openide.util.HelpCtx;

public final class DBSchemaConnectionWizardPanel extends DBSchemaPanel {

  /** aggregation, instance of UI component of this wizard panel */
    private DBSchemaConnectionPanel panelUI;

    public DBSchemaConnectionWizardPanel(DBSchemaWizardData data) {
        this.data = data;
    }

    /** @return AWT component which represents UI of this wizard panel */
    @Override
    public Component getComponent() {
        return getPanelUI();
    }

    /** @return UI component of this wizard panel. Creates new one if
     * accessed for the first time
     */    
    private DBSchemaConnectionPanel getPanelUI () {
        if (panelUI == null) {
            panelUI = new DBSchemaConnectionPanel(data, list);
        }
        return panelUI;
    }
    
    @Override
    public boolean isValid () {
        return getPanelUI().isInputValid();
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("dbschema_ctxhelp_wizard"); // NOI18N
    }

}
