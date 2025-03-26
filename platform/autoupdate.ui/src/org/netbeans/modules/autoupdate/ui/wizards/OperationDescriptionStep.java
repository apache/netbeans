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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.awt.Component;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.ui.Containers;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.netbeans.modules.autoupdate.ui.actions.AutoupdateCheckScheduler;
import org.netbeans.modules.autoupdate.ui.actions.Installer;
import org.netbeans.modules.autoupdate.ui.wizards.LazyInstallUnitWizardIterator.LazyUnit;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.WizardDescriptor;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Rechtacek
 */
public class OperationDescriptionStep implements WizardDescriptor.Panel<WizardDescriptor> {
    private static final String HEAD = "OperationDescriptionStep_Header_Head";
    private static final String CONTENT = "OperationDescriptionStep_Header_Content";
    private static final String TABLE_TITLE_INSTALL = "OperationDescriptionStep_TableInstall_Title";
    private static final String TABLE_TITLE_UPDATE = "OperationDescriptionStep_TableUpdate_Title";
    private static final String HEAD_UNINSTALL = "OperationDescriptionStep_HeaderUninstall_Head";
    private static final String CONTENT_UNINSTALL = "OperationDescriptionStep_HeaderUninstall_Content";
    private static final String TABLE_TITLE_UNINSTALL = "OperationDescriptionStep_TableUninstall_Title";
    private static final String HEAD_ACTIVATE = "OperationDescriptionStep_HeaderActivate_Head";
    private static final String CONTENT_ACTIVATE = "OperationDescriptionStep_HeaderActivate_Content";
    private static final String TABLE_TITLE_ACTIVATE = "OperationDescriptionStepActivate_Table_Title";
    private static final String HEAD_DEACTIVATE = "OperationDescriptionStep_HeaderDeativate_Head";
    private static final String CONTENT_DEACTIVATE = "OperationDescriptionStep_HeaderDeativate_Content";
    private static final String TABLE_TITLE_DEACTIVATE = "OperationDescriptionStep_TableDeativate_Title";
    private static final String DEPENDENCIES_TITLE_INSTALL = "DependenciesResolutionStep_Table_Title";
    private static final String DEPENDENCIES_TITLE_UPDATE = "DependenciesResolutionStep_Table_Title";
    private static final String DEPENDENCIES_TITLE_UNINSTALL = "UninstallDependenciesResolutionStep_Table_Title";
    private static final String DEPENDENCIES_TITLE_ACTIVATE = "OperationDescriptionStep_TableInstall_Title";
    private static final String DEPENDENCIES_TITLE_DEACTIVATE = "UninstallDependenciesResolutionStep_Table_Title";
    
    private volatile PanelBodyContainer component;
    private OperationWizardModel model = null;
    private boolean readyToGo = false;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private RequestProcessor.Task lazyDependingTask = null;
    WizardDescriptor lastWD;
    
    /** Creates a new instance of OperationDescriptionStep */
    public OperationDescriptionStep (OperationWizardModel model) {
        this.model = model;
    }
    
    void reset() {
       component = null;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            readyToGo = false;
            JPanel body;
            String tableTitle = null;
            String dependenciesTitle = null;
            String head = null;
            String content = null;
            switch (model.getOperation ()) {
            case LOCAL_DOWNLOAD :
                if (Containers.forUpdateNbms ().listAll ().isEmpty ()) {
                    tableTitle = getBundle (TABLE_TITLE_INSTALL);
                    dependenciesTitle = getBundle (DEPENDENCIES_TITLE_INSTALL);
                } else {
                    tableTitle = getBundle (TABLE_TITLE_UPDATE);
                    dependenciesTitle = getBundle (DEPENDENCIES_TITLE_UPDATE);
                }
                head = getBundle (HEAD);
                content = getBundle (CONTENT);
                break;
            case INSTALL :
                tableTitle = getBundle (TABLE_TITLE_INSTALL);
                dependenciesTitle = getBundle (DEPENDENCIES_TITLE_INSTALL);
                head = getBundle (HEAD);
                content = getBundle (CONTENT);
                break;
            case UPDATE :
                tableTitle = getBundle (TABLE_TITLE_UPDATE);
                dependenciesTitle = getBundle (DEPENDENCIES_TITLE_UPDATE);
                head = getBundle (HEAD);
                content = getBundle (CONTENT);
                break;
            case UNINSTALL :
                tableTitle = getBundle (TABLE_TITLE_UNINSTALL);
                dependenciesTitle = getBundle (DEPENDENCIES_TITLE_UNINSTALL);
                head = getBundle (HEAD_UNINSTALL);
                content = getBundle (CONTENT_UNINSTALL);
                break;
            case ENABLE :
                tableTitle = getBundle (TABLE_TITLE_ACTIVATE);
                dependenciesTitle = getBundle (DEPENDENCIES_TITLE_ACTIVATE);
                head = getBundle (HEAD_ACTIVATE);
                content = getBundle (CONTENT_ACTIVATE);
                break;
            case DISABLE :
                tableTitle = getBundle (TABLE_TITLE_DEACTIVATE);
                dependenciesTitle = getBundle (DEPENDENCIES_TITLE_DEACTIVATE);
                head = getBundle (HEAD_DEACTIVATE);
                content = getBundle (CONTENT_DEACTIVATE);
                break;
            }
            body = new OperationDescriptionPanel (tableTitle,
                    preparePluginsForShow (
                    model.getPrimaryUpdateElements(),
                    model.getPrimaryVisibleUpdateElements(),
                        model.getCustomHandledComponents (),
                        model.getOperation ()),
                    "",
                    "",
                    true);
            component = new PanelBodyContainer (head, content, body);
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
            component.setWaitingState (true);
            appendDependingLazy (tableTitle, dependenciesTitle);
        }
        return component;
    }

    private void appendDependingLazy (final String tableTitle, final String dependenciesTitle) {
        lazyDependingTask = Installer.RP.post (new Runnable () {
            @Override
            public void run () {
                JPanel body = null;
                // init required elements
                model.getRequiredUpdateElements ();
                if (model instanceof InstallUnitWizardModel) {
                    ((InstallUnitWizardModel) model).allLicensesApproved ();
                    ((InstallUnitWizardModel) model).hasCustomComponents ();
                    ((InstallUnitWizardModel) model).hasStandardComponents ();
                }
                boolean hasBrokenDependencies = model.hasBrokenDependencies ();
                final String[] args = { null, null, null, null };
                final boolean[] barg = { false };
                if (hasBrokenDependencies) {
                    args[0] = "";
                    args[1] = "";
                    args[2] = prepareBrokenDependenciesForShow (model);
                    args[3] = "";
                    barg[0] = true;
                } else {
                    args[0] = tableTitle;
                    args[1] = preparePluginsForShow (
                                model.getPrimaryUpdateElements(),
                                model.getPrimaryVisibleUpdateElements(),
                                model.getCustomHandledComponents (),
                                model.getOperation ());
                    args[2] = dependenciesTitle;
                    args[3] = preparePluginsForShow (
                                model.getRequiredUpdateElements(),
                                model.getRequiredVisibleUpdateElements(),
                                null,
                                model.getOperation ());
                    barg[0] = !model.getRequiredVisibleUpdateElements().isEmpty();
                }
                readyToGo = model != null && ! hasBrokenDependencies;
                SwingUtilities.invokeLater (new Runnable () {
                    @Override
                    public void run () {
                        JPanel p = new OperationDescriptionPanel(
                            args[0], args[1], args[2], args[3], barg[0]
                        );
                        component.setBody (p);
                        component.setWaitingState (false);
                        if (lastWD != null) {
                            updateButtons(lastWD);
                        }
                        fireChange ();
                    }
                });
            }
        });
    }
    
    static String prepareBrokenDependenciesForShow (OperationWizardModel model) {
        String s = new String ();
        boolean moreBroken = false;
        SortedMap<String, Set<UpdateElement>> dep2plugins = model.getBrokenDependency2Plugins ();

        for (Map.Entry<String, Set<UpdateElement>> entry : dep2plugins.entrySet()) {
            if (OperationWizardModel.MORE_BROKEN_PLUGINS.equals(entry.getKey())) {
                moreBroken = true;
                continue;
            }
            s += getPresentationName(entry.getKey());
            if (entry.getValue() != null) {
                Set<UpdateElement> sset = new HashSet<>(entry.getValue());
                Set<UpdateElement> uniqueElements = new HashSet<UpdateElement> ();
                for (UpdateElement plugin : sset) {
                    uniqueElements.addAll (model.findPrimaryPlugins (plugin));
                }
                TreeSet<String> uniqueNames = new TreeSet<String> ();
                for (UpdateElement plugin : uniqueElements) {
                    uniqueNames.add (plugin.getDisplayName ());
                }
                s += uniqueNames.size () == 1 ? getBundle ("OperationDescriptionStep_AffectedPluginByBrokenDepOnce") :
                    getBundle ("OperationDescriptionStep_AffectedPluginByBrokenDep");
                for (String name : uniqueNames) {
                    s += getBundle ("OperationDescriptionStep_AffectedPlugin", name);
                }
            }
        }
        if (moreBroken) {
            s += getBundle (OperationWizardModel.MORE_BROKEN_PLUGINS);
        }
        // present a note directing user to the Check for Updates action
        if (!model.getMissingModules().isEmpty() && !Utilities.hasBuiltDefaultCaches()) {
            s += getBundle ("OperationDescriptionStep_NoteCachesNotBuilt");
        }
        return s.trim ();
    }
    
    private static String getPresentationName (String dep) {
        String presentationName = null;
        boolean isPending = false;
        String reason = null;
        if (dep != null && dep.startsWith ("module")) { // NOI18N
            String codeName = dep.substring (6).trim ();
            int end = codeName.indexOf ('/'); // NOI18N
            String releaseVersion = null;
            if (end == -1) {
                end = codeName.indexOf (' '); // NOI18N
            } else {
                int spaceIndex = codeName.indexOf(' ');
                int index = (spaceIndex != -1) ? spaceIndex : codeName.length();
                releaseVersion = codeName.substring(end + 1, index).trim();
            }
            if (end != -1) {
                codeName = codeName.substring (0, end);
            }
            int greater = dep.indexOf ('>');
            int equals = dep.indexOf ('=');
            int idx = Math.max (greater, equals);
            String version = null;
            if (idx > 0) {
                version = dep.substring (idx + 2).trim ();
            }
            UpdateElement other = null;
            for (UpdateUnit u : UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE)) {
                if (codeName.equals (u.getCodeName ())) {
                    if (u.getInstalled () != null) {
                        other = u.getInstalled ();
                    } else if (u.getAvailableUpdates ().size () > 0) {
                        other = u.getAvailableUpdates ().get (0);
                    }
                    if (u != null) {
                        isPending = u.isPending ();
                    }
                    break;
                }
            }
            if (idx == -1) {
                // COMPARE_ANY
                // The module named {0} was needed and not found.
                reason = getBundle ("OperationDescriptionStep_BrokenModuleNameDep", other == null ? codeName : other.getDisplayName ());
            } else if (greater > 0) {
                // COMPARE_SPEC
                if (version != null && other != null && version.equals (other.getSpecificationVersion ())) {
                    // The module {0} would also need to be installed.
                    reason = getBundle ("OperationDescriptionStep_BrokenModuleDep", other.getDisplayName ());
                } else if (version != null) {
                    if (other != null) {
                        // The module {0} was requested in version >= {1} but only {2} was found.
                        int compare = new SpecificationVersion(other.getSpecificationVersion()).compareTo(new SpecificationVersion(version));
                        if(releaseVersion!=null && (equals > 0 ? (compare>=0) : (compare>0)) ) {
                            reason = getBundle ("OperationDescriptionStep_BrokenModuleReleaseVersionDep",
                                other.getDisplayName (),
                                version,
                                releaseVersion,
                                other.getSpecificationVersion ());

                        } else {
                        reason = getBundle ("OperationDescriptionStep_BrokenModuleVersionDep",
                                other.getDisplayName (),
                                version,
                                other.getSpecificationVersion ());
                        }
                    } else {
                        // The module {0} was requested in version >= {1}.
                        reason = getBundle ("OperationDescriptionStep_BrokenModuleOnlyVersionDep",
                                codeName,
                                version);
                    }
                } else {
                    // The module {0} would also need to be installed.
                    reason = getBundle ("OperationDescriptionStep_BrokenModuleDep", other == null ? codeName : other.getDisplayName ());
                }
            } else if (equals > 0) {
                // COMPARE_IMPL
                // The module {0} was requested in implementation version "{1}".
                if (version != null) {
                    reason = getBundle ("OperationDescriptionStep_BrokenModuleImplDep",
                            other == null ? codeName : other.getDisplayName (),
                            version);
                } else {
                    reason = getBundle ("OperationDescriptionStep_BrokenModuleDep", other == null ? codeName : other.getDisplayName ());
                }
            }
            if (isPending) {
                presentationName = getBundle ("OperationDescriptionStep_BrokenPendingModuleDepInit", other == null ? codeName : other.getDisplayName (), // NOI18N
                        reason);
            } else {
                presentationName = getBundle ("OperationDescriptionStep_BrokenModuleDepInit", other == null ? codeName : other.getDisplayName (), // NOI18N
                        reason);
            }
        } else if (dep != null && (dep.toLowerCase ().startsWith ("requires") || dep.toLowerCase ().startsWith ("needs"))) { // NOI18N
            // No module providing the capability {0} could be found.
            String token = dep.substring (dep.indexOf (' ') + 1);
            presentationName = getBundle ("OperationDescriptionStep_BrokenRequireDepInit", token,
                    getBundle ("OperationDescriptionStep_BrokenRequiresDep", token));
        } else if (dep != null && dep.toLowerCase ().startsWith ("java")) { // NOI18N
            presentationName = getBundle ("OperationDescriptionStep_BrokenJavaDepInit",
                    dep,
                    getBundle ("OperationDescriptionStep_PluginBrokesJavaDependency", dep, Dependency.JAVA_SPEC));
        } else if (dep != null && dep.toLowerCase ().startsWith ("package")) { // NOI18N
            presentationName = getBundle ("OperationDescriptionStep_BrokenPackageDepInit");
        }
        return presentationName == null ? dep : presentationName;
    }
    
    private String preparePluginsForShow (Set <UpdateElement> allPlugins, Set<UpdateElement> visiblePlugins, Set<UpdateElement> customHandled, OperationType type) {
        String s = new String ();
        List<String> names = new ArrayList<String> ();
        if (OperationWizardModel.OperationType.UPDATE != type) {
            if (visiblePlugins != null && !visiblePlugins.isEmpty()) {
                for (UpdateElement el : visiblePlugins) {
                    String updatename = "<b>" + el.getDisplayName() + "</b> "; // NOI18N
                    updatename += getBundle("OperationDescriptionStep_PluginVersionFormat", // NOI18N
                            el.getSpecificationVersion());
                    updatename += "<br>"; // NOI18N
                    String notification = el.getNotification();
                    if (notification != null && notification.length() > 0) {
                        updatename += "<font color=\"red\">" + notification + "</font><br><br>";  // NOI18N
                    }
                    names.add(updatename);
                }
                names.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return Collator.getInstance().compare(o1, o2);
                    }
                });
                for (String name : names) {
                    s += name;
                }
            }
        } else {
            SortedMap<UpdateUnit, TreeSet<UpdateElement>> visible2internals = 
                    new TreeMap<UpdateUnit, TreeSet<UpdateElement>> (new Comparator<UpdateUnit>() {
                @Override
                public int compare(UpdateUnit o1, UpdateUnit o2) {
                    UpdateElement ue1 = o1.getInstalled() != null ? o1.getInstalled() : o1.getAvailableUpdates().get(0);
                    UpdateElement ue2 = o2.getInstalled() != null ? o2.getInstalled() : o2.getAvailableUpdates().get(0);
                    return ue1.getDisplayName().compareTo(ue2.getDisplayName());
                }
            });
            for (UpdateElement el : allPlugins) {
                // a kit
                if (UpdateManager.TYPE.KIT_MODULE.equals(el.getUpdateUnit().getType())) {
                    if (! visible2internals.containsKey(el.getUpdateUnit())) {
                        TreeSet<UpdateElement> elements = new TreeSet<UpdateElement>(new Comparator<UpdateElement>() {
                            @Override
                            public int compare(UpdateElement o1, UpdateElement o2) {
                                return o1.getDisplayName().compareTo(o2.getDisplayName());
                            }
                        });
                        visible2internals.put(el.getUpdateUnit(), elements);
                    }
                } else {
                    UpdateUnit visibleUnit = el.getUpdateUnit().getVisibleAncestor();
                    // w/ visible ancestor
                    if (visibleUnit != null) {
                        if (! visible2internals.containsKey(visibleUnit)) {
                            TreeSet<UpdateElement> elements = new TreeSet<UpdateElement>(new Comparator<UpdateElement>() {
                                @Override
                                public int compare(UpdateElement o1, UpdateElement o2) {
                                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                                }
                            });
                            visible2internals.put(visibleUnit, elements);
                        }
                        visible2internals.get(visibleUnit).add(el);
                    } else {
                        // a fallback, w/o visible ancestor
                        if (! visible2internals.containsKey(el.getUpdateUnit())) {
                            TreeSet<UpdateElement> elements = new TreeSet<UpdateElement>(new Comparator<UpdateElement>() {
                                @Override
                                public int compare(UpdateElement o1, UpdateElement o2) {
                                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                                }
                            });
                            visible2internals.put(el.getUpdateUnit(), elements);
                        }
                    }
                }
            }

            // prepare for show
            UpdateElement element4showing = null;
            for (Map.Entry<UpdateUnit, TreeSet<UpdateElement>> entry : visible2internals.entrySet()) {
                UpdateUnit unit = entry.getKey();

                String updatename = "<b>";
                if (unit.getInstalled() != null) {
                    updatename += unit.getInstalled().getDisplayName() + "</b> "; // NOI18N
                } else {
                    updatename += unit.getAvailableUpdates().get(0).getDisplayName() + "</b> "; // NOI18N
                }
                if (entry.getValue().isEmpty()) {
                    element4showing = unit.getAvailableUpdates().get(0);
                    if (unit.getInstalled() != null) {
                        String oldVersion = unit.getInstalled().getSpecificationVersion();
                        String newVersion = element4showing.getSpecificationVersion();
                        updatename += getBundle("OperationDescriptionStep_UpdatePluginVersionFormat", oldVersion, newVersion);
                    } else {
                        String newVersion = element4showing.getSpecificationVersion();
                        updatename += getBundle("OperationDescriptionStep_PluginVersionFormat", newVersion);
                    }
                } else {
                    for (UpdateElement el : entry.getValue()) {
                        element4showing = el;
                        if (el.getUpdateUnit().getInstalled() != null) {
                            updatename += "<br>&nbsp;&nbsp;&nbsp;&nbsp;" + el.getDisplayName()
                                    + " ["
                                    + el.getUpdateUnit().getInstalled().getSpecificationVersion() + " -> "
                                    + el.getSpecificationVersion()
                                    + "]";
                        } else {
                            updatename += "<br>&nbsp;&nbsp;&nbsp;&nbsp;" + el.getDisplayName()
                                    + " ["
                                    + unit.getAvailableUpdates().get(0).getSpecificationVersion()
                                    + "]";
                        }
                    }
                }
                updatename += "<br>"; // NOI18N
                String notification = element4showing == null ? "" : element4showing.getNotification();
                if (notification != null && notification.length() > 0) {
                    updatename += "<font color=\"red\">" + notification + "</font><br><br>";  // NOI18N
                }
                names.add(updatename);
            }
            for (String name : names) {
                s += name;
            }
        }
        if (customHandled != null && ! customHandled.isEmpty ()) {
            names = new ArrayList<String> ();
            for (UpdateElement el : customHandled) {
                names.add ("<b>"  + el.getDisplayName () + "</b> " // NOI18N
                        + getBundle ("OperationDescriptionStep_PluginVersionFormat", // NOI18N
                        el.getSpecificationVersion ()) + "<br>"); // NOI18N
            }
            Collections.sort (names);
            s += "<br>" + getBundle ("OperationDescriptionStep_CustomHandled_Head", customHandled.size ()) + "<br>"; // NOI18N
            for (String name : names) {
                s += name;
            }
        }
        return s.trim ();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor wd) {
        updateButtons(wd);
    }
    
    /**
     * Updates wizard buttons as appropriate for the phase.
     * @param wd 
     */
    private void updateButtons(WizardDescriptor wd) {
        lastWD = wd;
        boolean doOperation = ! (model instanceof InstallUnitWizardModel);
        if (doOperation) {
            // will 
            model.modifyOptionsForDoOperation (wd, 0);
        } else {
            model.modifyOptionsForStartWizard (wd);
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wd) {
        if (WizardDescriptor.CANCEL_OPTION.equals (wd.getValue ()) || WizardDescriptor.CLOSED_OPTION.equals (wd.getValue ())) {
            try {
                if (lazyDependingTask != null && ! lazyDependingTask.isFinished ()) {
                    lazyDependingTask.cancel ();
                }
                AutoupdateCheckScheduler.notifyAvailable(LazyUnit.loadLazyUnits (model.getOperation()), model.getOperation());
                model.doCleanup (true);
            } catch (OperationException x) {
                Logger.getLogger (InstallUnitWizardModel.class.getName ()).log (Level.INFO, x.getMessage (), x);
            }
        }
    }

    @Override
    public boolean isValid () {
        return readyToGo;
    }

    @Override
    public synchronized void addChangeListener (ChangeListener l) {
        listeners.add (l);
    }

    @Override
    public synchronized void removeChangeListener (ChangeListener l) {
        listeners.remove (l);
    }

    private void fireChange () {
        ChangeEvent e = new ChangeEvent (this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
        for (ChangeListener l : templist) {
            l.stateChanged (e);
        }
    }

    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (OperationDescriptionPanel.class, key, params);
    }

}
