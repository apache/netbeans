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

package org.netbeans.modules.form;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
@OptionsPanelController.SubRegistration(
    location="Java",
    id="FormEditor",
    displayName="#Form_Editor",
    keywords="#KW_FormOptions",
    keywordsCategory="Java/FormEditor")
public final class FormEditorPanelController extends OptionsPanelController {

    private FormEditorCustomizer customizer = new FormEditorCustomizer ();
    private boolean initialized = false;


    @Override
    public void update () {
        initialized = true;
        customizer.update ();
    }
    
    @Override
    public void applyChanges () {
        if (initialized) {
            customizer.applyChanges ();
        }
        initialized = false;
    }
    
    @Override
    public void cancel () {
        customizer.cancel ();
        initialized = false;
    }
    
    @Override
    public boolean isValid () {
        return customizer.dataValid ();
    }
    
    @Override
    public boolean isChanged () {
        return customizer.isChanged ();
    }
    
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.advanced.formEditor"); // NOI18N
    }
    
    @Override
    public JComponent getComponent (Lookup masterLookup) {
        return customizer;
    }

    @Override
    public void addPropertyChangeListener (PropertyChangeListener l) {
        customizer.addPropertyChangeListener (l);
    }

    @Override
    public void removePropertyChangeListener (PropertyChangeListener l) {
        customizer.removePropertyChangeListener (l);
    }
}
