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

package org.netbeans.modules.tasklist.todo.settings;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author S. Aubrecht
 */
@OptionsPanelController.SubRegistration(
    id=ToDoOptionsController.OPTIONS_PATH,
    displayName="#LBL_Options",
    location="Team",
//    toolTip="#HINT_Options"
    keywords="#KW_ToDo",
    keywordsCategory="Advanced/ToDo"
)
public class ToDoOptionsController extends OptionsPanelController {

    public static final String OPTIONS_PATH = "ToDo"; // NOI18N
    
    public void update() {
        getCustomizer().update();
    }

    public void applyChanges() {
        getCustomizer().applyChanges();
    }

    public void cancel() {
        getCustomizer().cancel();
    }

    public boolean isValid() {
        return getCustomizer().isDataValid();
    }

    public boolean isChanged() {
        return getCustomizer().isChanged();
    }

    public JComponent getComponent(Lookup masterLookup) {
        return getCustomizer();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx ("netbeans.optionsDialog.advanced.todo");
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getCustomizer().addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        getCustomizer().removePropertyChangeListener( l );
    }

    private ToDoCustomizer customizer;
    
    private ToDoCustomizer getCustomizer() {
        if( null == customizer ) {
            customizer = new ToDoCustomizer();
        }
        return customizer;
    }
}
