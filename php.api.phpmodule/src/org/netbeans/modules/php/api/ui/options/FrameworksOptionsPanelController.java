/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
