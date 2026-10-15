/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Common Controller for all options categories composed by subpanels
 *
 * @author Max Sauer, Jiri Skrivanek
 */
public class TabbedController extends OptionsPanelController {

    private static final Logger LOGGER = Logger.getLogger(TabbedController.class.getName());
    private final String tabFolder;
    private Lookup.Result<AdvancedOption> options;
    private Map<String, String> id2tabTitle;
    private Map<String, OptionsPanelController> tabTitle2controller;
    private final Map<String, AdvancedOption> tabTitle2Option;
    private Lookup masterLookup;
    private final LookupListener lookupListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            readPanels();
            Mutex.EVENT.readAccess(new Runnable() {

                public void run() {
                    initTabbedPane();
                }
            });
        }
    };

    /** pane with sub-panels */
    private JTabbedPane pane;
    /** PropertyChangeSupport and listener to fire changes when switching tabs. */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final ChangeListener tabbedPaneChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            handleTabSwitched(null, null);
        }
    };

    /**
     * Creates new instance
     * @param tabFolder layer folder where subpanels (AdvancedOption instances) reside
     */
    public TabbedController(String tabFolder) {
        this.tabFolder = tabFolder;
        tabTitle2Option = Collections.synchronizedMap(new LinkedHashMap<String, AdvancedOption>());
        readPanels();
        options.addLookupListener(WeakListeners.create(LookupListener.class, lookupListener, options));
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
            // if changed (#145569) and not valid
            if (!c.isValid() && c.isChanged()) {
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
    public JComponent getComponent(Lookup masterLookup) {
        if (pane == null) {
            pane = new JTabbedPane();
            pane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TabbedController.class, "TabbedController.pane.AD"));
            this.masterLookup = masterLookup;
            initTabbedPane();
        }
        return pane;
    }

    @Override
    public void handleSuccessfulSearch(String searchText, List<String> matchedKeywords) {
        handleTabSwitched(searchText, matchedKeywords);
    }

    private void initTabbedPane() {
        if (pane != null) {
            pane.removeChangeListener(tabbedPaneChangeListener);
            pane.removeAll();
            Set<String> keySet = tabTitle2Option.keySet();
            synchronized (tabTitle2Option) {
                Iterator<String> i = keySet.iterator();
                while (i.hasNext()) {
                    String tabTitle = i.next();
                    pane.addTab(tabTitle, new JLabel(tabTitle));
                }
            }
            pane.addChangeListener(tabbedPaneChangeListener);
            handleTabSwitched(null, null);
        }
    }


    /** Replace placeholder with real panel and change help context. */
    private void handleTabSwitched(String searchText, List<String> matchedKeywords) {
        final int selectedIndex = pane.getSelectedIndex();
        if (selectedIndex != -1) {
            String tabTitle = pane.getTitleAt(selectedIndex);
            OptionsPanelController controller = tabTitle2controller.get(tabTitle);
            if (pane.getSelectedComponent() instanceof JLabel) {
                JComponent comp;
                if (controller == null) {
                    AdvancedOption advancedOption = tabTitle2Option.get(tabTitle);
                    if (advancedOption == null) {
                        LOGGER.log(Level.INFO, "AdvancedOption for {0} is not present.", tabTitle);
                        return;
                    } else {
                        controller = advancedOption.create();
                        tabTitle2controller.put(tabTitle, controller);
                        // must be here because many controllers rely on fact that getComponent() is called first than other methods
                        comp = controller.getComponent(masterLookup);
                        // add existing listeners
                        for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
                            controller.addPropertyChangeListener(pcl);
                        }
                    }
                } else {
                    comp = controller.getComponent(masterLookup);
                }
                if( null == comp.getBorder() ) {
                    comp.setBorder(BorderFactory.createEmptyBorder(11,11,11,11));
                }
                JScrollPane scroll = new JScrollPane(comp);
                scroll.setBorder(BorderFactory.createEmptyBorder());
                scroll.setOpaque(false);
                scroll.getViewport().setOpaque(false);
                scroll.getVerticalScrollBar().setUnitIncrement(Utils.ScrollBarUnitIncrement);
                scroll.getHorizontalScrollBar().setUnitIncrement(Utils.ScrollBarUnitIncrement);
                pane.setComponentAt(selectedIndex, scroll);
                controller.update();
		controller.isValid();
            }
	    if (searchText != null && matchedKeywords != null) {
		controller.handleSuccessfulSearch(searchText, matchedKeywords);
            }
            pcs.firePropertyChange(OptionsPanelController.PROP_HELP_CTX, null, null);
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        if (pane != null && pane.getSelectedIndex() != -1) {
            return getHelpCtx(pane.getTitleAt(pane.getSelectedIndex()));
        }
        return null;
    }

    private HelpCtx getHelpCtx(String tabTitle) {
        OptionsPanelController controller = tabTitle2controller.get(tabTitle);
        if (controller != null) {
            return controller.getHelpCtx();
        }
        return new HelpCtx("netbeans.optionsDialog.java");  //NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
        for (OptionsPanelController c : getControllers()) {
            c.addPropertyChangeListener(l);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
        for (OptionsPanelController c : getControllers()) {
            c.removePropertyChangeListener(l);
        }
    }

    @Override
    protected void setCurrentSubcategory(String path) {
        String subcategoryID = path.indexOf('/') == -1 ? path : path.substring(0, path.indexOf('/'));
        final String subcategorySubpath = path.indexOf('/') == -1 ? null : path.substring(path.indexOf('/')+1);
        LOGGER.fine("Set current subcategory: "+path); // NOI18N
        if(!id2tabTitle.containsKey(subcategoryID)) {
            LOGGER.warning("Subcategory "+subcategoryID+" not found.");  //NOI18N
            return;
        }
        // use tab titles because there are still might be placeholders instead of real components
        String newTabTitle = id2tabTitle.get(subcategoryID);
        String currentTabTitle = pane.getSelectedIndex() != -1 ? pane.getTitleAt(pane.getSelectedIndex()) : null;
        if (!newTabTitle.equals(currentTabTitle)) {
            for (int i = 0; i < pane.getTabCount(); i++) {
                if (pane.getTitleAt(i).equals(newTabTitle)) {
                    pane.setSelectedIndex(i);
                    break;
                }
            }
        }
        if(subcategorySubpath != null) {
            OptionsPanelControllerAccessor.getDefault().setCurrentSubcategory(tabTitle2controller.get(newTabTitle), subcategorySubpath);
        }
    }

    @Override
    public Lookup getLookup() {
        List<Lookup> lookups = new ArrayList<Lookup>();
        for (OptionsPanelController controller : getControllers()) {
            Lookup lookup = controller.getLookup();
            if (lookup != null && lookup != Lookup.EMPTY) {
                lookups.add(lookup);
            }
            if (lookup == null) {
                LOGGER.log(Level.WARNING, "{0}.getLookup() should never return null. Please, see Bug #194736.", controller.getClass().getName()); // NOI18N
                throw new NullPointerException(controller.getClass().getName() + ".getLookup() should never return null. Please, see Bug #194736."); // NOI18N
            }
        }
        if (lookups.isEmpty()) {
            return Lookup.EMPTY;
        } else {
            return new ProxyLookup(lookups.toArray(new Lookup[0]));
        }
    }

    private Collection<OptionsPanelController> getControllers() {
        return tabTitle2controller.values();
    }

    private void readPanels() {
        Lookup lookup = Lookups.forPath(tabFolder);
        options = lookup.lookup(new Lookup.Template<AdvancedOption>( AdvancedOption.class ));
        tabTitle2controller = new HashMap<String, OptionsPanelController>();
        id2tabTitle = new HashMap<String, String>();
        synchronized (tabTitle2Option) {
            for (Lookup.Item<AdvancedOption> item : options.allItems()) {
                AdvancedOption option = item.getInstance();
                String displayName = option.getDisplayName();
                if (displayName != null) {
                    tabTitle2Option.put(displayName, option);
                    String id = item.getId().substring(item.getId().lastIndexOf('/') + 1);  //NOI18N
                    id2tabTitle.put(id, displayName);
                } else {
                    LOGGER.log(Level.WARNING, "Display name not defined: {0}", item.toString());  //NOI18N
                }
            }
        }
    }
}
