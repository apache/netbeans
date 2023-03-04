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

package org.netbeans.modules.options.advanced;

import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class AdvancedPanelController extends OptionsPanelController {

    private String subpath = null;

    /* Creates new AdvancedPanelController.
     * @param subpath path to folder under OptionsDialog folder containing 
     * instances of AdvancedOption class. Path is composed from registration 
     * names divided by slash.
     */
    public AdvancedPanelController(String subpath) {
        this.subpath = subpath;
    }

    public void update () {
        getAdvancedPanel ().update ();
    }

    public void applyChanges () {
        getAdvancedPanel ().applyChanges ();
    }
    
    public void cancel () {
        getAdvancedPanel ().cancel ();
    }
    
    public boolean isValid () {
        return getAdvancedPanel ().dataValid ();
    }
    
    public boolean isChanged () {
        return getAdvancedPanel ().isChanged ();
    }
        
    @Override
    public Lookup getLookup () {
        return getAdvancedPanel ().getLookup ();
    }
    
    public JComponent getComponent (Lookup masterLookup) {
	AdvancedPanel p = getAdvancedPanel();
	p.init();
	return p;
    }
    
    @Override
    public void setCurrentSubcategory(String subpath) {
        getAdvancedPanel().setCurrentSubcategory(subpath);
    }
    
    public HelpCtx getHelpCtx () {
        return getAdvancedPanel ().getHelpCtx ();
    }
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        getAdvancedPanel().addPropertyChangeListener(l);
        getAdvancedPanel().addModelPropertyChangeListener(l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        getAdvancedPanel().removePropertyChangeListener(l);
        getAdvancedPanel().removeModelPropertyChangeListener(l);
    }

    @Override
    public void handleSuccessfulSearch(String searchText, List<String> matchedKeywords) {
        getAdvancedPanel().handleSearch(searchText, matchedKeywords);
    }

    private AdvancedPanel advancedPanel;
    
    private synchronized AdvancedPanel getAdvancedPanel () {
        if (advancedPanel == null)
            advancedPanel = new AdvancedPanel(subpath);
        return advancedPanel;
    }
}
