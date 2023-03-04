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

/*
 * WSDesignNavigatorPanel.java
 *
 * Created on April 9, 2007, 5:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.navigator;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author rico
 */
public class WSDesignNavigatorPanel implements NavigatorPanel, LookupListener{
    
    private WSDesignViewNavigatorContent navigator;
    private Lookup.Result<DataObject> selection;
    
    /** Creates a new instance of WSDesignNavigatorPanel */
    public WSDesignNavigatorPanel() {
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(WSDesignNavigatorPanel.class,
                "LBL_WSDesignNavigatorPanel_Name");
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(WSDesignNavigatorPanel.class,
                "LBL_WSDesignNavigatorPanel_Hint");
    }
    
    public JComponent getComponent() {
        if(navigator == null){
            navigator = new WSDesignViewNavigatorContent();
        }
        return navigator;
    }
    
    public void panelActivated(Lookup context) {
        getComponent();
        TopComponent.getRegistry().addPropertyChangeListener(navigator);
        selection = context.lookup(new Lookup.Template<DataObject>(DataObject.class));
        selection.addLookupListener(this);
        resultChanged(null);
    }
    
    public void panelDeactivated() {
        TopComponent.getRegistry().removePropertyChangeListener(navigator);
        selection.removeLookupListener(this);
        selection = null;
    }
    
    public Lookup getLookup() {
        return null;
    }
    
    public void resultChanged(LookupEvent ev) {
        Collection<? extends DataObject> selected = selection.allInstances();
        if (selected.size() == 1) {
            DataObject dobj = selected.iterator().next();
            navigator.navigate(dobj);
        }
    }
    
}
