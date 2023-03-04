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

package org.netbeans.lib.editor.codetemplates.storage.ui;

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
    location="Editor",
    id="CodeTemplates",
    displayName="#CTL_CodeTemplates_DisplayName",
    keywords="#KW_CodeTemplates",
    keywordsCategory="Editor/CodeTemplates",
    position=300
//    toolTip="#CTL_CodeTemplates_ToolTip"
)
public final class CodeTemplatesPanelController extends OptionsPanelController {

    public void update () {
        getCodeTemplatesPanel ().update ();
    }

    public void applyChanges () {
        getCodeTemplatesPanel ().applyChanges ();
    }
    
    public void cancel () {
        getCodeTemplatesPanel ().cancel ();
    }
    
    public boolean isValid () {
        return getCodeTemplatesPanel ().dataValid ();
    }
    
    public boolean isChanged () {
        return getCodeTemplatesPanel ().isChanged ();
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.editor.codeTemplates"); //NOI18N
    }
    
    public JComponent getComponent (Lookup masterLookup) {
        return getCodeTemplatesPanel ();
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        getCodeTemplatesPanel ().addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        getCodeTemplatesPanel ().removePropertyChangeListener (l);
    }

    
    private CodeTemplatesPanel codeTemplatesPanel;
    
    private CodeTemplatesPanel getCodeTemplatesPanel () {
        if (codeTemplatesPanel == null)
            codeTemplatesPanel = new CodeTemplatesPanel ();
        return codeTemplatesPanel;
    }
}
