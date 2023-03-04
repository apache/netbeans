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
package org.netbeans.modules.versioning.util;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

// XXX could be replaced by a @OptionsPanelController.ContainerRegistration
// if OptionsDialog/Versioning could be used instead of VersioningOptionsDialog
// and a specialized GUI were not required

/**
 * Versioning options panel combined from panels for various settings.
 *
 * @author Pavel Buzek
 */
@OptionsPanelController.SubRegistration(
    id=VcsAdvancedOptions.ID,
    displayName="#LBL_OptionsPanelName",
    location="Team",
    keywords="#KW_VersioningOptions",
    keywordsCategory="Team/Versioning")
public class VcsAdvancedOptions extends OptionsPanelController {
    
    public static final String ID = "Versioning"; //NOI18N
    private VcsAdvancedOptionsPanel panel;
    private boolean initialized = false;
    private final Map<String, OptionsPanelController> categoryToController = new HashMap<String, OptionsPanelController>();

    private void init(Lookup masterLookup) {
        if (initialized) return;
        initialized = true;
        panel = new VcsAdvancedOptionsPanel();

        Lookup lookup = Lookups.forPath("VersioningOptionsDialog"); // NOI18N
        Iterator<? extends AdvancedOption> it = lookup.lookup(new Lookup.Template<AdvancedOption> (AdvancedOption.class)).
                allInstances().iterator();
        while (it.hasNext()) {
            AdvancedOption option = it.next();
            registerOption(option, masterLookup);
        }
    }

    private void registerOption (AdvancedOption option, Lookup masterLookup) {
        String category = option.getDisplayName();
        OptionsPanelController controller = option.create();
        synchronized (categoryToController) {
            categoryToController.put(category, controller);
        }
        panel.addPanel(category, controller.getComponent(masterLookup));
        if ("org.netbeans.modules.versioning.ui.options.GeneralAdvancedOption".equals(option.getClass().getName())) {
            panel.addPanel(category, controller.getComponent(masterLookup));
        }
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        init(masterLookup);
        return panel;
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void update() {
        for (OptionsPanelController c : getControllers()) {
            c.update();
        }
    }

    @Override
    public void applyChanges() {
        for (OptionsPanelController c : getControllers()) {
            c.applyChanges();
        }
    }

    @Override
    public void cancel() {
        for (OptionsPanelController c : getControllers()) {
            c.cancel();
        }
    }

    @Override
    public boolean isValid() {
        for (OptionsPanelController c : getControllers()) {
            if (!c.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isChanged() {
        for (OptionsPanelController c : getControllers()) {
            if (c.isChanged()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.versioning.util.VcsAdvancedOptions"); //NOI18N
    }

    @Override
    public void handleSuccessfulSearch (String searchText, List<String> matchedKeywords) {
        Map<String, OptionsPanelController> m;
        synchronized (categoryToController) {
            m = new HashMap<String, OptionsPanelController>(categoryToController);
        }
        for (Map.Entry<String, OptionsPanelController> e : m.entrySet()) {
            OptionsPanelController c = e.getValue();
            if (c instanceof VCSOptionsKeywordsProvider) {
                if (((VCSOptionsKeywordsProvider) c).acceptKeywords(matchedKeywords)) {
                    panel.selectCategory(e.getKey());
                    break;
                }
            }
        }
    }

    @Override
    protected void setCurrentSubcategory (String subpath) {
        Map<String, OptionsPanelController> m;
        synchronized (categoryToController) {
            m = new HashMap<String, OptionsPanelController>(categoryToController);
        }
        for (Map.Entry<String, OptionsPanelController> e : m.entrySet()) {
            if (e.getKey().equals(subpath)) {
                panel.selectCategory(e.getKey());
                break;
            }
        }
    }

    private OptionsPanelController[] getControllers () {
        synchronized (categoryToController) {
            return categoryToController.values().toArray(new OptionsPanelController[categoryToController.values().size()]);
        }
    }
}
