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
package org.netbeans.modules.cordova.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.webkit.debugging.api.network.Network;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

@OptionsPanelController.SubRegistration(
        id = "MobilePlatforms", // NOI18N
        location = "Html5", // NOI18N
        displayName = "#AdvancedOption_DisplayName_MobilePlatforms", // NOI18N
        keywords = "#AdvancedOption_Keywords_MobilePlatforms", // NOI18N
        keywordsCategory = "Advanced/MobilePlatforms") // NOI18N
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_MobilePlatforms=Mobile Platforms",
    "AdvancedOption_Keywords_MobilePlatforms=mobile platform ios android phonegap cordova iphone ipad"
})
public final class MobilePlatformsOptionsPanelController extends OptionsPanelController {

    private MobilePlatformsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ChangeSupport cs = new ChangeSupport(this);
    private boolean changed;
    private static final RequestProcessor RP = new RequestProcessor(MobilePlatformsOptionsPanelController.class);

    @Override
    public void update() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                getPanel().load();
            }
        });
        changed = false;
    }

    @Override
    public void applyChanges() {
        getPanel().store();
        changed = false;
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }
    
    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.cordova.options.MobilePlatformsPanel");
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    

    private MobilePlatformsPanel getPanel() {
        if (panel == null) {
            panel = new MobilePlatformsPanel(this);
        }
        return panel;
    }

    void changed(boolean isChanged) {
        if (!changed) {
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        changed = isChanged;
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        cs.fireChange();
    }
}
