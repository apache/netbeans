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

package org.netbeans.modules.options.keymap;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
@OptionsPanelController.TopLevelRegistration(
    id=OptionsDisplayer.KEYMAPS,
    categoryName="#CTL_Keymap_Options",
    iconBase="org/netbeans/modules/options/keymap/keymap.png",
    keywords="#KW_KeymapOptions",
    keywordsCategory="Keymaps",
    position=600
//    title="#CTL_Keymap_Options_Title",
//    description="#CTL_Keymap_Options_Description"
)
public final class KeymapPanelController extends OptionsPanelController {


    public void update () {
        getKeymapPanel ().update ();
    }

    public void applyChanges() {
        getKeymapPanel ().applyChanges ();
    }
    
    public void cancel () {
        getKeymapPanel ().cancel ();
    }
    
    public boolean isValid () {
        return getKeymapPanel ().dataValid ();
    }
    
    public boolean isChanged () {
        return getKeymapPanel ().isChanged ();
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.keymaps");
    }
    
    @Override
    public Lookup getLookup () {
        return Lookups.singleton (getKeymapPanel().getModel ());
    }
    
    public JComponent getComponent (Lookup masterLookup) {
        return getKeymapPanel ();
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        getKeymapPanel ().addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        getKeymapPanel ().removePropertyChangeListener (l);
    }
    

    private KeymapPanel keymapPanel;

    private synchronized KeymapPanel getKeymapPanel () {
        if (keymapPanel == null)
            keymapPanel = new KeymapPanel ();
        return keymapPanel;
    }
}
