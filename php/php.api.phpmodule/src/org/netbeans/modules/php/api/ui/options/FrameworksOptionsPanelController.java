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
package org.netbeans.modules.php.api.ui.options;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.lookup.Lookups;

/**
 * Options controller for Frameworks and Tools. It aggregates several other
 * options panels registered under FRAMEWORKS_AND_TOOLS_OPTIONS_PATH.
 * @see UiUtils.PhpOptionsPanelRegistration
 * @author S. Aubrecht
 * @since 2.35
 */
@OptionsPanelController.SubRegistration(
        id = "FrameworksAndTools",
        location = UiUtils.OPTIONS_PATH,
        displayName = "#LBL_FrameworksTabTitle",
        position = 10000
)
public final class FrameworksOptionsPanelController extends OptionsPanelController {

    static final String FRAMEWORKS_AND_TOOLS_OPTIONS_PATH = "PHP/OptionsDialog/FrameworksAndTools"; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    // GuardedBy("EDT")
    private final Map<String, AdvancedOption> id2option = new HashMap<>(20);

    private volatile FrameworksPanel panel;


    @Override
    public void update() {
        if (panel != null) {
            panel.update();
        }
    }

    @Override
    public void applyChanges() {
        if (panel != null) {
            panel.applyChanges();
        }
    }

    @Override
    public void cancel() {
        if (panel != null) {
            panel.cancel();
        }
    }

    @Override
    public boolean isValid() {
        if (panel == null) {
            return true;
        }
        return panel.isControllerValid();
    }

    @Override
    public boolean isChanged() {
        if (panel == null) {
            return false;
        }
        return panel.isChanged();
    }

    @Override
    public HelpCtx getHelpCtx() {
        if (panel == null) {
            return null;
        }
        OptionsPanelController selection = panel.getSelectedController();
        return null == selection ? null : selection.getHelpCtx();
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel(masterLookup);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    protected void setCurrentSubcategory(String subpath) {
        EventQueue.isDispatchThread();
        super.setCurrentSubcategory(subpath);
        if (subpath != null
                && panel != null) {
            subpath = FRAMEWORKS_AND_TOOLS_OPTIONS_PATH + "/" + subpath; //NOI18N
            AdvancedOption option = id2option.get(subpath);
            if (option != null) {
                panel.setSelecteOption(option);
            }
        }
    }

    private FrameworksPanel getPanel(Lookup lkp) {
        if (panel == null) {
            panel = new FrameworksPanel(this, lkp, loadOptions());
        }
        return panel;
    }

    private List<AdvancedOption> loadOptions() {
        EventQueue.isDispatchThread();
        Lookup lkp = Lookups.forPath(FRAMEWORKS_AND_TOOLS_OPTIONS_PATH);
        Collection<? extends Item<AdvancedOption>> allItems = lkp.lookupResult(AdvancedOption.class).allItems();
        List<AdvancedOption> options = new ArrayList<>(allItems.size());
        for (Item<AdvancedOption> item : allItems) {
            AdvancedOption option = item.getInstance();
            options.add(option);
            id2option.put(item.getId(), option);
        }
        return options;
    }

    void fireChange(PropertyChangeEvent pce) {
        propertyChangeSupport.firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
    }

    @Override
    public void handleSuccessfulSearch(String searchText, List<String> matchedKeywords) {
        if (panel != null) {
            panel.handleSearch(matchedKeywords);
        } else {
            super.handleSuccessfulSearch(searchText, matchedKeywords);
        }
    }

}
