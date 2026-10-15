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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.ui.Containers;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.modules.Dependency;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public abstract class OperationWizardModel {
    private Set<UpdateElement> primaryElements;
    private Set<UpdateElement> requiredElements = null;
    private Set<UpdateElement> customHandledElements = null;
    private Set<UpdateElement> allElements = null;
    private HashMap<UpdateElement, Collection<UpdateElement>> required2primary = new HashMap<UpdateElement, Collection<UpdateElement>> ();
    private JButton originalCancel = null;
    private JButton originalNext = null;
    private JButton originalFinish = null;
    private boolean reconized = false;
    static Dimension PREFFERED_DIMENSION = new Dimension (530, 400);
    private static int MAX_TO_REPORT = 3;
    static String MORE_BROKEN_PLUGINS = "OperationWizardModel_MoreBrokenPlugins"; // NOI18N
    private TreeMap<String, Set<UpdateElement>> dep2plugins = null;
    abstract OperationType getOperation ();
    private Callable<OperationContainer>    refreshCallable;
    private Set<String> missingModules = new HashSet<>();
    
    /**
     * Basic container for the operation. This container represents the operation itself.
     * @return container for the operation
     */
    abstract OperationContainer getBaseContainer ();
    
    /**
     * Install container, if part of the operation.
     * If the operation does not use the install step, returns {@code null}.
     * 
     * @return install container or {@code null}
     */
    OperationContainer getInstallContainer() {
        return null;
    }
    
    abstract OperationContainer<OperationSupport> getCustomHandledContainer ();
    
    public static enum OperationType {
        /** Install <code>UpdateElement</code> */
        INSTALL,
        /** Uninstall <code>UpdateElement</code> */
        UNINSTALL,
        /** Update installed <code>UpdateElement</code> to newer version. */
        UPDATE,
        /** Rollback installed <code>UpdateElement</code> to previous version. */
        REVERT,
        /** Enable <code>UpdateElement</code> */
        ENABLE,
        /** Disable <code>UpdateElement</code> */
        DISABLE,
        /** Install or update <code>UpdateElement</code> from local NBM. */
        LOCAL_DOWNLOAD
    }
    
    public Set<UpdateElement> getPrimaryUpdateElements () {
        if (primaryElements == null) {
            primaryElements = new HashSet<UpdateElement> ();
            for (OperationInfo<?> info : getBaseInfos ()) {
                primaryElements.add (info.getUpdateElement ());
            }
        }
        return primaryElements;
    }

    public boolean hasRequiredUpdateElements () {
        return ! getRequiredUpdateElements ().isEmpty ();
    }
    
    public Set<UpdateElement> getRequiredUpdateElements () {
        if (requiredElements == null) {
            requiredElements = new HashSet<UpdateElement> ();
            dep2plugins = new TreeMap<String, Set<UpdateElement>> ();
            
            for (OperationInfo<?> info : getBaseInfos ()) {
                Set<UpdateElement> reqs = info.getRequiredElements ();
                Set<String> broken = info.getBrokenDependencies ();
                if (! broken.isEmpty()) {
                    for (String brokenDep : broken) {
                        // pay special attention to missing JDK
                        if (brokenDep.toLowerCase ().startsWith ("package")) {
                            if (brokenDep.contains("VirtualMachineManager")) {
                                brokenDep = "package";
                            } else {
                                continue;
                            }
                        } else if (brokenDep.toLowerCase().startsWith("module ")) { // NOI18N
                            // Special handling for modules - if the module is missing (there's no UpdateUnit for it), 
                            String modName = brokenDep.substring(7).trim();
                            Set<Dependency> deps = Dependency.create(Dependency.TYPE_MODULE, modName);
                            for (Dependency d : deps) {
                                if (!UpdateManager.getDefault().getUpdateUnits().stream().anyMatch((m) -> m.getCodeName().equals(d.getName()))) {
                                    missingModules.add(d.getName());
                                }
                            }
                        }
                        if (dep2plugins.get (brokenDep) == null) {
                            dep2plugins.put (brokenDep, new HashSet<UpdateElement> ());
                        }
                        dep2plugins.get (brokenDep).add (info.getUpdateElement ());
                    }
                    if (dep2plugins.keySet ().size () >= MAX_TO_REPORT) {
                        dep2plugins.put (MORE_BROKEN_PLUGINS, null);
                        break;
                    }
                }
                for (UpdateElement el : reqs) {
                    if (required2primary.get (el) == null) {
                        required2primary.put (el, new HashSet<UpdateElement> ());
                    }
                    required2primary.get (el).add (info.getUpdateElement ());
                }
                requiredElements.addAll (reqs);
            }
            
            Collection<UpdateElement> pending = new HashSet<UpdateElement> ();
            for (UpdateElement el : requiredElements) {
                if (el != null && el.getUpdateUnit () != null && el.getUpdateUnit ().isPending ()) {
                    pending.add (el);
                }
            }
            if (! pending.isEmpty ()) {
                Logger.getLogger (OperationWizardModel.class.getName ()).log (Level.INFO, "Required UpdateElements " + pending +
                        " cannot be in pending state.");
                requiredElements.removeAll (pending);
            }
            
            // add requiredElements to container
            addRequiredElements (requiredElements);
            
            // remove primary elements
            requiredElements.removeAll (getPrimaryUpdateElements ());
            
        }
        return requiredElements;
    }
    
    public boolean hasBrokenDependencies () {
        return ! getBrokenDependency2Plugins ().isEmpty ();
    }
    
    public boolean hasCustomComponents () {
        return ! getCustomHandledContainer ().listAll ().isEmpty ();
    }
    
    public boolean hasStandardComponents () {
        return ! getBaseContainer ().listAll ().isEmpty ();
    }
    
    /**
     * Set of unknown modules. May mean that the AU caches are not populated
     * with all the update centers.
     * @return missing module codenames
     */
    public Set<String> getMissingModules() {
        return missingModules;
    }
    
    public Set<UpdateElement> getCustomHandledComponents () {
        if (customHandledElements == null) {
            customHandledElements = new HashSet<UpdateElement> ();
            
            for (OperationInfo<?> info : getCustomHandledInfos ()) {
                customHandledElements.add (info.getUpdateElement ());
                customHandledElements.addAll (info.getRequiredElements ());
            }
        }
        return customHandledElements;
    }
    
    private List<OperationInfo<OperationSupport>> getCustomHandledInfos () {
        return getCustomHandledContainer ().listAll ();
    }
    
    @SuppressWarnings({"unchecked"})
    private List<OperationInfo> getBaseInfos () {
        return getBaseContainer ().listAll ();
    }
    
    public SortedMap<String, Set<UpdateElement>> getBrokenDependency2Plugins () {
        if (dep2plugins != null) {
            return dep2plugins;
        }
        
        dep2plugins = new TreeMap<String, Set<UpdateElement>> ();

        for (OperationInfo<?> info : getBaseInfos ()) {
            Set<String> broken = info.getBrokenDependencies ();
            if (! broken.isEmpty()) {
                for (String brokenDep : broken) {
                    // pay special attention to missing JDK
                    if (brokenDep.toLowerCase ().startsWith ("package")) {
                        brokenDep = "package";
                    }
                    if (dep2plugins.get (brokenDep) == null) {
                        dep2plugins.put (brokenDep, new HashSet<UpdateElement> ());
                    }
                    dep2plugins.get (brokenDep).add (info.getUpdateElement ());
                }
                if (dep2plugins.keySet ().size () >= MAX_TO_REPORT) {
                    dep2plugins.put (MORE_BROKEN_PLUGINS, null);
                    break;
                }
            }
        }
        return dep2plugins;
    }
    
    public Collection<UpdateElement> findPrimaryPlugins (UpdateElement el) {
        Collection<UpdateElement> res = new HashSet<UpdateElement> (Collections.singleton (el));
        if (required2primary.containsKey (el)) {
            res = required2primary.get (el);
        }
        return res;
    }
    
    public Set<UpdateElement> getAllUpdateElements () {
        if (allElements == null) {
            allElements = new HashSet<UpdateElement> (getPrimaryUpdateElements ());
            allElements.addAll (getRequiredUpdateElements ());
            assert allElements.size () == getPrimaryUpdateElements ().size () + getRequiredUpdateElements ().size () :
                "Primary [" + getPrimaryUpdateElements ().size () + "] plus " +
                "Required [" + getRequiredUpdateElements ().size () + "] is All [" + allElements.size () + "] ";
        }
        return allElements;
    }


    public Set<UpdateElement> getAllVisibleUpdateElements () {
        Set <UpdateElement> visible = new HashSet <UpdateElement> ();
        visible.addAll(getPrimaryVisibleUpdateElements());
        visible.addAll(getRequiredVisibleUpdateElements());
        return visible;
    }
    public Set<UpdateElement> getPrimaryVisibleUpdateElements() {
        Set <UpdateElement> primary = getPrimaryUpdateElements();
        Set <UpdateElement> visible = getVisibleUpdateElements(primary, false, getOperation());
        return visible;
    }
    public Set<UpdateElement> getRequiredVisibleUpdateElements () {
        Set <UpdateElement> required = getRequiredUpdateElements();
        Set <UpdateElement> visible = getVisibleUpdateElements(required, true, getOperation());
        return visible;
    }

    private static Set<UpdateElement> getVisibleUpdateElements (Set<UpdateElement> all, boolean canBeEmpty, OperationType operationType) {
        if (Utilities.modulesOnly () || OperationType.LOCAL_DOWNLOAD == operationType) {
            return all;
        } else if (OperationType.UPDATE == operationType) {
            Set<UpdateElement> visible = new HashSet<UpdateElement>();
            Set<UpdateUnit> visibleUnits = new HashSet<UpdateUnit>();
            for (UpdateElement el : all) {
                if (visibleUnits.contains(el.getUpdateUnit())) {
                    continue;
                }
                if (UpdateManager.TYPE.KIT_MODULE == el.getUpdateUnit().getType()) {
                    visible.add(el);
                    visibleUnits.add(el.getUpdateUnit());
                } else {
                    UpdateUnit visibleAncestor = el.getUpdateUnit().getVisibleAncestor();
                    if (visibleAncestor != null) {
                        visibleUnits.add(visibleAncestor);
                        visible.add(visibleAncestor.getInstalled());
                    } else {
                        // a fallback
                        visible.add(el);
                        visibleUnits.add(el.getUpdateUnit());
                    }
                }
            }
            if (visible.isEmpty () && ! canBeEmpty) {
                // in Downloaded tab may become all NBMs are hidden
                visible = all;
            }
            return visible;
        } else {
            Set<UpdateElement> visible = new HashSet<UpdateElement> ();
            for (UpdateElement el : all) {
                if (UpdateManager.TYPE.KIT_MODULE == el.getUpdateUnit ().getType ()) {
                    visible.add (el);
                }
            }
            if (visible.isEmpty () && ! canBeEmpty) {
                // in Downloaded tab may become all NBMs are hidden
                visible = all;
            }
            return visible;
        }
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForDoClose (WizardDescriptor wd) {
        modifyOptionsForDoClose (wd, false);
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForFailed (final WizardDescriptor wd) {
        recognizeButtons (wd);
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                wd.setOptions (new JButton [] { getOriginalCancel (wd) });
            }
        });
    }
    
    /**
     * Will modify the wizard at the end of the install operation.
     * The default will delegate to {@link #modifyOptionsForDoClose(org.openide.WizardDescriptor)}.
     * @param wd 
     */
    public void modifyOptionsForEndInstall(WizardDescriptor wd) {
        modifyOptionsForDoClose (wd, false);
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForDoClose (final WizardDescriptor wd, final boolean canCancel) {
        recognizeButtons (wd);
        final JButton b = getOriginalFinish (wd);
        Mnemonics.setLocalizedText (b, getBundle ("InstallUnitWizardModel_Buttons_Close"));
        SwingUtilities.invokeLater (new Runnable () {
            int cnt;
            @Override
            public void run () {
                b.requestFocus();
                if (cnt++ > 0) {
                    return;
                }
                
                b.setDefaultCapable(true);
                final JButton[] arr = canCancel ? new JButton [] { b, getOriginalCancel (wd) } : new JButton [] { b };
                wd.setOptions (arr);
                wd.setClosingOptions(arr);
                SwingUtilities.invokeLater(this);
            }
        });
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForStartWizard (WizardDescriptor wd) {
        recognizeButtons (wd);
        removeFinish (wd);
        Mnemonics.setLocalizedText (getOriginalNext (wd), NbBundle.getMessage (InstallUnitWizardModel.class,
                "InstallUnitWizardModel_Buttons_MnemonicNext", getBundle ("InstallUnitWizardModel_Buttons_Next")));
    }
    
    public void modifyOptionsForContinue (final WizardDescriptor wd, boolean canFinish) {
        recognizeButtons (wd);
        if (canFinish) {
            final JButton b = getOriginalFinish (wd);
            Mnemonics.setLocalizedText (b, getBundle ("InstallUnitWizardModel_Buttons_Close"));
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    wd.setOptions (new JButton [] {b});
                }
            });
        } else {
            removeFinish (wd);
            Mnemonics.setLocalizedText (getOriginalNext (wd), NbBundle.getMessage (InstallUnitWizardModel.class,
                    "InstallUnitWizardModel_Buttons_MnemonicNext", getBundle ("InstallUnitWizardModel_Buttons_Next")));
        }
    }
    
    /**
     * Brings back cancel, and sets it as a closing option.
     * Should work properly if the Cancel was completely removed, or just replaced using
     * {@link #modifyOptionsForDisabledCancel}.
     * 
     * @param wd wizard descriptor to modify
     */
    public void modifyOptionsContinueWithCancel(final WizardDescriptor wd) {
        JButton b = getOriginalNext (wd);
        JButton c = getOriginalCancel(wd);
        Object[] opts = wd.getOptions();
        final Object[] arr;
        final Object[] closingArr = new Object[] { c };
        if (!Arrays.asList(opts).contains(c)) {
            List<Object> newOpts = new ArrayList<>(Arrays.asList(opts));
            Object o = wd.getProperty("OperationWizardModel_disabledCancel");
            // replace previously disabled cancel
            int n = o == null ? -1 : newOpts.indexOf(o);
            if (n > -1) {
                newOpts.set(n, c);
            } else {
                // fallback: find 'next' and place cancel next to it
                n = newOpts.indexOf(b);
                if (n == -1) {
                    n = newOpts.size();
                }
                newOpts.add(n, c);
            }
            arr = newOpts.toArray();
        } else {
            arr = opts;
        }
        SwingUtilities.invokeLater (new Runnable () {
            int cnt;
            @Override
            public void run () {
                b.requestFocus();
                if (cnt++ > 0) {
                    return;
                }

                b.setDefaultCapable(true);
                wd.setOptions (arr);
                wd.setClosingOptions(closingArr);
                SwingUtilities.invokeLater(this);
            }
        });
    }
    
    /**
     * Changes option suitably for "install" phase. Will disable next button
     * (install is running).
     * @param wd wizar ddescriptor
     */
    public void modifyOptionsForInstall(WizardDescriptor wd) {
        recognizeButtons (wd);
        getOriginalNext (wd).setEnabled(false);
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForDoOperation (WizardDescriptor wd, int panelType) {
        recognizeButtons (wd);
        removeFinish (wd);
        switch (getOperation ()) {
        case LOCAL_DOWNLOAD :
            if (Containers.forUpdateNbms ().listAll ().isEmpty ()) {
                Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("InstallUnitWizardModel_Buttons_Install"));
            } else {
                Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("InstallUnitWizardModel_Buttons_Update"));
            }
            break;
        case INSTALL :
            Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("InstallUnitWizardModel_Buttons_Install"));
            break;
        case UPDATE :
            Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("InstallUnitWizardModel_Buttons_Update"));
            break;
        case UNINSTALL :
            Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("UninstallUnitWizardModel_Buttons_Uninstall"));
            break;
        case ENABLE :
            if (hasComponentsToInstall()) {
                // modifications for the nested install during the enable operation
                switch (panelType) {
                    case 1:
                        Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("InstallUnitWizardModel_Buttons_Install"));
                        break;
                    case 2:
                        Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("UninstallUnitWizardModel_Buttons_TurnOn"));
                        break;
                    default:
                        Mnemonics.setLocalizedText (getOriginalNext (wd), NbBundle.getMessage (InstallUnitWizardModel.class,
                            "InstallUnitWizardModel_Buttons_MnemonicNext", getBundle ("InstallUnitWizardModel_Buttons_Next")));
                        break;
                }
                break;
            }
            Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("UninstallUnitWizardModel_Buttons_TurnOn"));
            break;
        case DISABLE :
            Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("UninstallUnitWizardModel_Buttons_TurnOff"));
            break;
        default:
            assert false : "Unknown operationType " + getOperation ();
        }
        getOriginalNext (wd).setEnabled(true);
    }
    
    // XXX Hack in WizardDescriptor
    public JButton getCancelButton (WizardDescriptor wd) {
        return getOriginalCancel (wd);
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForDisabledCancel (final WizardDescriptor wd) {
        recognizeButtons (wd);
        Object [] options = wd.getOptions ();
        final List<JButton> newOptionsL = new ArrayList<JButton> ();
        List<Object> optionsL = Arrays.asList (options);
        for (Object o : optionsL) {
            assert o instanceof JButton : o + " instanceof JButton";
            if (o instanceof JButton) {
                JButton b = (JButton) o;
                if (b.equals (getOriginalCancel (wd))) {
                    JButton disabledCancel = new JButton (b.getText ());
                    wd.putProperty("OperationWizardModel_disabledCancel", disabledCancel);
                    disabledCancel.setEnabled (false);
                    newOptionsL.add (disabledCancel);
                } else {
                    newOptionsL.add (b);
                }
            }
        }
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                wd.setOptions (newOptionsL.toArray ());
            }
        });
    }
    
    public void doCleanup (boolean cancel) throws OperationException {
        getBaseContainer ().removeAll ();
        getCustomHandledContainer ().removeAll ();
    }
    
    public void recognizeButtons (WizardDescriptor wd) {
        if (! reconized) {
            Object [] options = wd.getOptions ();
            assert options != null : "options: " + options;
            assert options.length >= 4 : Arrays.asList (options) + " has lenght 4";
            assert options [1] instanceof JButton : options [1] + " instanceof JButton";
            originalNext = (JButton) options [1];
            assert options [2] instanceof JButton : options [2] + " instanceof JButton";
            originalFinish = (JButton) options [2];
            assert options [3] instanceof JButton : options [3] + " instanceof JButton";
            originalCancel = (JButton) options [3];
            reconized = true;
        }
        
    }
    
    private JButton getOriginalNext (WizardDescriptor wd) {
        return originalNext;
    }
    
    private JButton getOriginalCancel (WizardDescriptor wd) {
        return originalCancel;
    }
    
    private JButton getOriginalFinish (WizardDescriptor wd) {
        return originalFinish;
    }
    
    private void removeFinish (final WizardDescriptor wd) {
        Object [] options = wd.getOptions ();
        final List<JButton> newOptionsL = new ArrayList<JButton> ();
        List<Object> optionsL = Arrays.asList (options);
        for (Object o : optionsL) {
            assert o instanceof JButton : o + " instanceof JButton";
            if (o instanceof JButton) {
                JButton b = (JButton) o;
                if (! b.equals (originalFinish)) {
                    newOptionsL.add (b);
                }
            }
        }
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                wd.setOptions (newOptionsL.toArray ());
            }
        });
    }
    
    private void addRequiredElements (Set<UpdateElement> elems) {
        OperationContainer baseContainer = getBaseContainer();
        OperationContainer customContainer = getCustomHandledContainer();
        OperationContainer installContainer = getInstallContainer();
        for (UpdateElement el : elems) {
            if (el == null || el.getUpdateUnit () == null) {
                Logger.getLogger (OperationWizardModel.class.getName ()).log (Level.INFO, "UpdateElement " + el + " cannot be null"
                        + (el == null ? "" : " or UpdateUnit " + el.getUpdateUnit () + " cannot be null"));
                continue;
            }
            if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == el.getUpdateUnit ().getType ()) {
                customContainer.add (el);
            } else if (baseContainer.canBeAdded(el.getUpdateUnit(), el)) {
                baseContainer.add (el);
            } else if (installContainer != null && installContainer.canBeAdded(el.getUpdateUnit(), el)) {
                installContainer.add(el);
            }
        }
    }
    
    private String getBundle (String key) {
        return NbBundle.getMessage (InstallUnitWizardModel.class, key);
    }

    /**
     * Determines if there are modules to install before the main enable operation
     * may be successful.
     * @return modules to install
     */
    public boolean hasComponentsToInstall() {
        OperationContainer<InstallSupport> oc = getInstallContainer();
        if (oc == null) {
            return false;
        }
        return !oc.listAll().isEmpty();
    }

    public void setRefreshCallable(Callable<OperationContainer> refreshCallable) {
        this.refreshCallable = refreshCallable;
    }
    
    /**
     * Will trigger refresh of this model and possibly UI. If the 
     * {@link #refreshCallable} is set, the method will call it to obtain
     * a new {@link OperationContainer} contents to initialize the model.
     * {@link #refresh} will be called with the new container.
     */
    protected void performRefresh() {
        requiredElements = null;
        dep2plugins = null;
        allElements = null;
        customHandledElements = null;
        primaryElements = null;
        
        if (refreshCallable != null) {
            try {
                refresh(refreshCallable.call());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    /**
     * Subclasses should override this method to perform a data refresh.
     * The method is called from {@link #performRefresh} to reinitialize the
     * wizard model, e.g. after module set/state change during the wizard
     * 
     * @param cont the new operation container
     */
    protected void refresh(OperationContainer cont) {
    }
}
