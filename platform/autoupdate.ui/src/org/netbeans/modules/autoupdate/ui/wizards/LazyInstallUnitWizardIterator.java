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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.actions.AutoupdateCheckScheduler;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public final class LazyInstallUnitWizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {
    
    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>> ();
    private Collection<LazyUnit> installModel;
    private OperationType doOperation;
    private boolean forceReload;

    public LazyInstallUnitWizardIterator (Collection<LazyUnit> model, OperationType doOperation, boolean forceReload) {
        this.installModel = model;
        this.doOperation = doOperation;
        this.forceReload = forceReload;
        createPanels ();
        index = 0;
    }
    
    private void createPanels () {
        assert panels != null && panels.isEmpty() : "Panels are still empty";
        panels.add (new LazyOperationDescriptionStep (installModel, doOperation, forceReload));
    }
    
    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current () {
        assert panels != null;
        return panels.get (index);
    }
    
    @Override
    public String name () {
        return NbBundle.getMessage (LazyInstallUnitWizardIterator.class, "InstallUnitWizard_Title");
    }
    
    @Override
    public boolean hasNext () {
        return false;
    }
    
    @Override
    public boolean hasPrevious () {
        return false;
    }
    
    @Override
    public void nextPanel () {}
    
    @Override
    public void previousPanel () {}
    
    @Override
    public synchronized void addChangeListener(ChangeListener l) {}

    @Override
    public synchronized void removeChangeListener(ChangeListener l) {}

    public static class LazyUnit extends Object {
        private String codeName;
        private String displayName;
        private String oldVersion;
        private String newVersion;
        private String notification;
        
        private static final String DELIMETER = "|";
        
        private LazyUnit (String codeName, String displayName, String oldVersion, String newVersion, String notification) {
            this.codeName = codeName;
            this.displayName = displayName;
            this.oldVersion = oldVersion;
            this.newVersion = newVersion;
            this.notification = notification;
        }
        
        public static void storeUpdateElements (OperationType operationType, Collection<UpdateElement> elements) {
            Preferences p = getPreferences (operationType);
            try {
                if (p.keys ().length > 0) {
                    p.clear ();
                }
            } catch (BackingStoreException ex) {
                Logger.getLogger (LazyInstallUnitWizardIterator.class.getName ()).log (Level.WARNING, ex.getLocalizedMessage (), ex);
            }
            if (elements == null) {
                return ;
            }
            for (UpdateElement el : elements) {
                p.put (el.getCodeName (), toString (el));
            }
        }
        
        public static void storeLazyUnits (OperationType operationType, Collection<LazyUnit> units) {
            Preferences p = getPreferences (operationType);
            try {
                if (p.keys ().length > 0) {
                    p.clear ();
                }
            } catch (BackingStoreException ex) {
                Logger.getLogger (LazyInstallUnitWizardIterator.class.getName ()).log (Level.WARNING, ex.getLocalizedMessage (), ex);
            }
            if (units == null) {
                return ;
            }
            for (LazyUnit u : units) {
                p.put (u.getCodeName (), u.toString ());
            }
        }
        
        public static Collection<LazyUnit> loadLazyUnits (OperationType operationType) {
            Preferences p = getPreferences (operationType);
            Collection<LazyUnit> units = new HashSet<LazyUnit> ();
            try {
                for (String cn : p.keys ()) {
                    units.add (parseLazyUnit (p.get (cn, null)));
                }
            } catch (BackingStoreException ex) {
                Logger.getLogger (LazyInstallUnitWizardIterator.class.getName ()).log (Level.WARNING, ex.getLocalizedMessage (), ex);
                return null;
            }
            return units;
        }
        
        public String getCodeName () {
            return codeName;
        }
        
        public String getDisplayName () {
            return displayName == null ? codeName : displayName;
        }
        
        public String getOldVersion () {
            return oldVersion == null ? "" : oldVersion.trim ();
        }
        
        public String getNewVersion () {
            return newVersion == null ? "" : newVersion.trim ();
        }
        
        public String getNotification () {
            return notification == null ? "" : notification.trim ();
        }
        
        @Override
        public String toString () {
            return  codeName + DELIMETER +
                    (displayName == null ? codeName : displayName) + DELIMETER +
                    (oldVersion == null ? " " : oldVersion) + DELIMETER +
                    (newVersion == null ? " " : newVersion) + DELIMETER +
                    (notification == null ? " " : notification);
        }
        
        public static String toString (UpdateElement el) {
            return  el.getCodeName () + DELIMETER +
                    (el.getDisplayName () == null ? el.getCodeName () : el.getDisplayName ()) + DELIMETER +
                    (el.getUpdateUnit ().getInstalled () == null ? " " : el.getUpdateUnit ().getInstalled ().getSpecificationVersion ()) + DELIMETER +
                    (el.getSpecificationVersion () == null ? " " : el.getSpecificationVersion ()) + DELIMETER +
                    (el.getNotification () == null ? " " : el.getNotification ());
        }
        
        private static LazyUnit parseLazyUnit (String s) {
            StringTokenizer tokenizer = new StringTokenizer (s, DELIMETER);
            assert 5 == tokenizer.countTokens () : "5 tokens for " + s;
            String codeName = tokenizer.nextToken ();
            String displayName = tokenizer.nextToken ();
            String oldVersion = tokenizer.nextToken ();
            String newVersion = tokenizer.nextToken ();
            String notification = tokenizer.nextToken ();
            return new LazyUnit (codeName, displayName, oldVersion, newVersion, notification);
        }
        
        private static Preferences getPreferences (OperationType type) {
            return NbPreferences.forModule (AutoupdateCheckScheduler.class).node (type.toString ());
        }
    
    }

}
