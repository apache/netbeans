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

package org.netbeans.modules.options.generaleditor;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
@OptionsPanelController.SubRegistration(
    displayName="org.netbeans.modules.options.editor.Bundle#CTL_General_DisplayName",
    keywords="org.netbeans.modules.options.editor.Bundle#KW_General",
    keywordsCategory="Editor/General",
    id="General", // XXX used anywhere?
    location=OptionsDisplayer.EDITOR,
    position=100
//    toolTip="org.netbeans.modules.options.editor.Bundle#CTL_General_ToolTip"
)
public final class GeneralEditorPanelController extends OptionsPanelController {

    public void update () {
        getGeneralEditorPanel ().update ();
    }

    public void applyChanges () {
        getGeneralEditorPanel ().applyChanges ();
    }
    
    public void cancel () {
        getGeneralEditorPanel ().cancel ();
    }
    
    public boolean isValid () {
        return getGeneralEditorPanel ().dataValid ();
    }
    
    public boolean isChanged () {
        return getGeneralEditorPanel ().isChanged ();
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.editor.general");
    }
    
    public JComponent getComponent (Lookup masterLookup) {
        return getGeneralEditorPanel ();
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        getGeneralEditorPanel ().addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        getGeneralEditorPanel ().removePropertyChangeListener (l);
    }
    
    
    private GeneralEditorPanel generalEditorPanel;
    
    private GeneralEditorPanel getGeneralEditorPanel () {
        if (generalEditorPanel == null)
            generalEditorPanel = new GeneralEditorPanel ();
        return generalEditorPanel;
    }
}

