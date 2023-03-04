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

package org.netbeans.modules.autoupdate.ui;

import java.awt.Image;
import java.text.Collator;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.ui.UnitCategoryTableModel.Type;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public abstract class Unit {
    UpdateUnit updateUnit = null;
    private boolean isVisible;
    private String filter;
    private String categoryName;
    static final Logger log = Logger.getLogger (Unit.class.getName ());
    private String displayDate = null;
    
    protected abstract UpdateElement getRelevantElement ();
    public abstract boolean isMarked ();
    public abstract void setMarked (boolean marked);
    public abstract int getCompleteSize ();
    
    Unit (String categoryName) {
        this.categoryName = categoryName;
    }
    
    public abstract UnitCategoryTableModel.Type getModelType();
    public void initState() {
        if (UnitCategoryTableModel.isMarkedAsDefault(getModelType())) {
            if (!isMarked() && canBeMarked()) {
                setMarked(true);
            }
        }
    }
    
    public String getCategoryName () {
        return categoryName;
    }
    
    public boolean canBeMarked () {
        RequestProcessor.Task t = PluginManagerUI.getRunningTask ();
        return t == null || t.isFinished ();
    }
    
    public String getDisplayName () {
        return getRelevantElement ().getDisplayName ();
    }
    
    public final boolean isVisible (final String filter) {
        if (this.filter != null && this.filter.equals (filter)) {
            return isVisible;
        }
        this.filter = filter;
        isVisible = filter.length () == 0;
        if (isVisible) {
            return isVisible;
        }
        Iterable<String> iterable = details ();
        for (String detail : iterable) {
            isVisible = detail.toLowerCase ().contains (filter);
            if (isVisible) {
                break;
            }
        }
        return isVisible;
    }
    
    private Iterable<String> details () {
        Iterable<String> retval = new Iterable<String>(){
            @Override
            public Iterator<String> iterator () {
                return new Iterator<String>() {
                    int step = 0;
                    @Override
                    public boolean hasNext () {
                        return step <= 6;
                    }
                    
                    @Override
                    public String next () {
                        String next = null;
                        switch(step++) {
                        case 0:
                            next = getDisplayName ();break;
                        case 1:
                            next = getCategoryName ();break;
                        case 2:
                            next = getDescription ();break;
                        case 3:
                            next = updateUnit.getCodeName ();break;
                        case 4:
                            next = getDisplayVersion ();break;
                        case 5:
                            next = getAuthor ();break;
                        case 6:
                            next = getHomepage ();break;
                        }
                        return next != null ? next : "";//NOI18N
                    }
                    
                    @Override
                    public void remove () {
                        throw new UnsupportedOperationException ("Not supported yet.");
                    }
                };
            }
        };
        return retval;
    }     
    
    public String getFilter() {
        return filter;
    }
    
    public String getDescription () {
        return getRelevantElement ().getDescription ();
    }
    
    public String getNotification () {
        return getRelevantElement ().getNotification ();
    }
    
    public String getAuthor () {
        return getRelevantElement ().getAuthor ();
    }
    
    public String getHomepage () {
        return getRelevantElement ().getHomepage ();
    }
    
    public String getSource () {
        return getRelevantElement ().getSource ();
    }
    
    public String getDisplayVersion () {
        return getRelevantElement ().getSpecificationVersion ();
    }
    
    public String getDisplayDate () {
        if (displayDate == null) {
            String sd = getRelevantElement ().getDate ();
            if (sd != null) {
                try {
                    Date d = Utilities.DATE_FORMAT.parse (sd);
                    displayDate = DateFormat.getDateInstance (DateFormat.SHORT, Locale.getDefault ()).format (d);
                } catch (ParseException pe) {
                    log.log (Level.INFO, "ParseException while parsing date " + sd, pe);
                }
            }
        }
        return displayDate;
    }
    
    public static int compareDisplayNames (Unit unit1, Unit unit2) {
        //if (!Utilities.modulesOnly()) {
        return Utilities.getCategoryComparator().compare(unit1.getDisplayName(), unit2.getDisplayName());
        //}
        //return Collator.getInstance().compare(unit1.getDisplayName(), unit2.getDisplayName());
    }
    
    public static int compareCategories (Unit unit1, Unit unit2) {
        return Utilities.getCategoryComparator ().compare (unit1.getCategoryName (), unit2.getCategoryName ());
    }
    
    public static int compareSimpleFormatDates (Unit u1, Unit u2) {
        
        if (u1.getRelevantElement ().getDate () == null) {
            if (u2.getRelevantElement ().getDate () == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (u2.getRelevantElement ().getDate () == null) {
            return 1;
        }
        
        Date d1;
        Date d2;
        try {
            d1 = Utilities.DATE_FORMAT.parse (u1.getRelevantElement ().getDate ());
        } catch (ParseException pe) {
            log.log (Level.INFO, "ParseException while parsing date " + u1.getRelevantElement ().getDate (), pe);
            return -1;
        }
        try {
            d2 = Utilities.DATE_FORMAT.parse (u2.getRelevantElement ().getDate ());
        } catch (ParseException pe) {
            log.log (Level.INFO, "ParseException while parsing date " + u2.getRelevantElement ().getDate (), pe);
            return 1;
        }
        return d1.compareTo (d2);
    }
    
    public static int compareDisplayVersions (Unit unit1, Unit unit2) {
        if (unit1.getDisplayVersion () == null) {
            if (unit2.getDisplayVersion () == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (unit2.getDisplayVersion () == null) {
            return 1;
        }
        return new SpecificationVersion (unit1.getDisplayVersion ()).compareTo (new SpecificationVersion (unit2.getDisplayVersion ()));
    }
    
    public static int compareCompleteSizes (Unit unit1, Unit unit2) {
        return Integer.valueOf (unit1.getCompleteSize ()).compareTo (unit2.getCompleteSize ());
    }
    
    public static class Installed extends Unit {
        
        private UpdateElement installEl = null;
        private UpdateElement backupEl = null;
        private boolean uninstallationAllowed;
        private boolean deactivationAllowed;
        private boolean activationAllowed;
        
        public static boolean isOperationAllowed (UpdateUnit uUnit, UpdateElement element, OperationContainer<OperationSupport> container) {
            return container.canBeAdded (uUnit, element);
        }
        
        public Installed (UpdateUnit unit, String categoryName) {
            super (categoryName);
            this.updateUnit = unit;
            if (unit.getInstalled () == null && unit.isPending ()) {
                this.installEl = unit.getAvailableUpdates ().get (0);
                assert installEl != null : "Pending UpdateUnit " + unit + " has UpdateElement for update.";
            } else {
                this.installEl = unit.getInstalled ();
                assert installEl != null : "Installed UpdateUnit " + unit + " has Installed UpdateElement.";
            }
            this.backupEl = unit.getBackup ();
            OperationContainer<OperationSupport> container;
            if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomUninstall ();
            } else {
                container = Containers.forUninstall ();
            }
            uninstallationAllowed = isOperationAllowed (this.updateUnit, installEl, container);
            deactivationAllowed = isOperationAllowed (this.updateUnit, installEl, Containers.forDisable());
            activationAllowed   = isOperationAllowed (this.updateUnit, installEl, Containers.forEnable());
                
            initState();
        }

        public boolean isUninstallAllowed() {
            return uninstallationAllowed;
        }
        public boolean isDeactivationAllowed() {
            return deactivationAllowed;
        }
        public boolean isActivationAllowed() {
            return activationAllowed;
        }
        
        @Override
        public boolean isMarked () {
            boolean uninstallMarked;
            OperationContainer container;
            if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomUninstall ();
            } else {
                container = Containers.forUninstall ();
            }
            uninstallMarked = container.contains (installEl);
            boolean deactivateMarked = Containers.forDisable().contains (installEl);
            boolean activateMarked = Containers.forEnable().contains (installEl);
            return deactivateMarked || uninstallMarked || activateMarked;
        }
        
        @Override
        public void setMarked (boolean marked) {
            assert marked != isMarked ();
            if (isUninstallAllowed()) {
                OperationContainer container;
                if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                    container = Containers.forCustomUninstall ();
                } else {
                    container = Containers.forUninstall ();
                }
                if (marked) {
                    container.add (updateUnit, installEl);
                } else {
                    container.remove (installEl);
                }
            }
            if (isDeactivationAllowed()) {
                OperationContainer container = Containers.forDisable();
                if (marked) {
                    container.add (updateUnit, installEl);
                } else {
                    container.remove (installEl);
                }
            } else if (isActivationAllowed()) {
                OperationContainer container = Containers.forEnable();
                if (marked) {
                    container.add (updateUnit, installEl);
                } else {
                    container.remove (installEl);
                }
            }
        }
        
        public static int compareEnabledState (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Installed && u2 instanceof Unit.Installed) {
                Unit.Installed unit1 = (Unit.Installed )u1;
                Unit.Installed unit2 = (Unit.Installed )u2;
                final int retval = Boolean.valueOf(unit1.getRelevantElement().isEnabled()).compareTo(unit2.getRelevantElement().isEnabled());
                return (retval == 0) ? Boolean.valueOf(unit1.updateUnit.isPending()).compareTo(unit2.updateUnit.isPending()) : retval;
            }
            return Unit.compareDisplayVersions (u1, u2);
        }
        
        public static int compareInstalledVersions (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Installed && u2 instanceof Unit.Installed) {
                Unit.Installed unit1 = (Unit.Installed )u1;
                Unit.Installed unit2 = (Unit.Installed )u2;
                if (unit1.getInstalledVersion () == null) {
                    if (unit2.getInstalledVersion () == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else if (unit2.getInstalledVersion () == null) {
                    return 1;
                }
                return new SpecificationVersion (unit1.getInstalledVersion ()).compareTo (new SpecificationVersion (unit2.getInstalledVersion ()));
            }
            return Unit.compareDisplayVersions (u1, u2);
        }

        @Override
        public boolean canBeMarked () {
            return super.canBeMarked () && (isDeactivationAllowed() || isUninstallAllowed() || isActivationAllowed());
        }
        
        public String getInstalledVersion () {
            return installEl.getSpecificationVersion ();
        }
        
        public String getBackupVersion () {
            return backupEl == null ? "-" : backupEl.getSpecificationVersion ();
        }
        
        public Integer getMyRating () {
            return null;
        }
        
        @Override
        public UpdateElement getRelevantElement () {
            return installEl;
        }
        
        @Override
        public int getCompleteSize () {
            return -1;
        }

        @Override
        public Type getModelType() {
            return UnitCategoryTableModel.Type.INSTALLED;
        }
        
    }

    public static class CompoundUpdate extends Unit.Update  {
        
        private TreeSet<UpdateUnit> internalUpdates;

        public CompoundUpdate(UpdateUnit updateUnit, String categoryName) {
            super(updateUnit, false, categoryName);
        }

        public TreeSet<UpdateUnit> getUpdateUnits() {
            if (internalUpdates == null) {
                internalUpdates = new TreeSet<UpdateUnit>(new Comparator<UpdateUnit>() {
                    @Override
                    public int compare(UpdateUnit uu1, UpdateUnit uu2) {
                        UpdateElement ue1 = uu1.getInstalled() != null ? uu1.getInstalled() : uu1.getAvailableUpdates().get(0);
                        UpdateElement ue2 = uu2.getInstalled() != null ? uu2.getInstalled() : uu2.getAvailableUpdates().get(0);
                        return ue1.getDisplayName().compareTo(ue2.getDisplayName());
                    }
                });
            }
            return internalUpdates;
        }
        
        public UpdateElement getRealUpdate() {
            return hasInternalsOnly() ? null : updateUnit.getAvailableUpdates().get(0);
        }
        
        @Override
        public UpdateElement getRelevantElement() {
            return hasInternalsOnly() ? updateUnit.getInstalled() : updateUnit.getAvailableUpdates().get(0);
        }

        @Override
        public boolean isMarked() {
            OperationContainer container = Containers.forUpdate ();
            for(UpdateUnit invisible : getUpdateUnits()) {
                if(!container.contains(invisible.getAvailableUpdates().get(0))) {
                    return false;
                }
            }
            if (! hasInternalsOnly()) {
                if (! container.contains(getRelevantElement())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getAvailableVersion () {
            if (updateUnit.getAvailableUpdates().isEmpty()) {
                return getInstalledVersion() + " " + getBundle("Unit_InternalUpdates_Version");
            } else {
                return super.getAvailableVersion();
            }
        }
        @Override
        public void setMarked(boolean marked) {
            if (marked == isMarked()) {
                log.info("Not necessary mark "  + this + " as " + marked + " if it'is marked as " + isMarked());
                return ;
            }
            OperationContainer container = Containers.forUpdate();
            for (UpdateUnit invisible : getUpdateUnits()) {
                if (marked) {
                    if (container.canBeAdded(invisible, invisible.getAvailableUpdates().get(0))) {
                        container.add(invisible, invisible.getAvailableUpdates().get(0));
                    }
                } else {
                    container.remove(invisible.getAvailableUpdates().get(0));
                }
            }
            if (! hasInternalsOnly()) {
                if (marked) {
                    if (container.canBeAdded(updateUnit, getRelevantElement())) {
                        container.add(updateUnit, getRelevantElement());
                    }
                } else {
                    container.remove(getRelevantElement());
                }
            }
        }

        @Override
        public int getCompleteSize() {
            if (size == -1) {
                size = 0;
                for (UpdateUnit u : getUpdateUnits()) {
                    size += u.getAvailableUpdates().get(0).getDownloadSize();
                }

            }
            return size;
        }

        @Override
        public String getSize () {
            return Utilities.getDownloadSizeAsString (getCompleteSize());
        }

        @Override
        public Type getModelType() {
            return Type.UPDATE;
        }
        
        private boolean hasInternalsOnly() {
            return updateUnit.getAvailableUpdates().isEmpty();
        }
        
    }
    
    public static class Update extends Unit {
        private UpdateElement installEl = null;
        private UpdateElement updateEl = null;
        private boolean isNbms;
        protected int size = -1;
        
        public Update (UpdateUnit unit, boolean isNbms, String categoryName) {
            super (categoryName);
            this.isNbms = isNbms;
            this.updateUnit = unit;
            this.installEl = unit.getInstalled ();
            assert installEl != null : "Updateable UpdateUnit " + unit + " has Installed UpdateElement.";
            if(unit.getAvailableUpdates().size() > 0) {
                this.updateEl = unit.getAvailableUpdates ().get (0);
                assert updateEl != null : "Updateable UpdateUnit " + unit + " has UpdateElement for update.";
            }
            initState();
        }
        
        @Override
        public boolean isMarked () {
            OperationContainer container;
            if (isNbms) {
                container = Containers.forUpdateNbms ();
            } else if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomInstall ();
            } else {
                container = Containers.forUpdate ();
            }
            return container.contains (updateEl);
        }
        
        @Override
        public void setMarked (boolean marked) {
            if (marked == isMarked()) {
                return;
            }
            OperationContainer container;
            if (isNbms) {
                container = Containers.forUpdateNbms ();
            } else if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomInstall ();
            } else {
                container = Containers.forUpdate ();
            }
            if (marked) {
                try {
                    container.add (updateUnit, updateEl);
                } catch (IllegalArgumentException ex) {
                    log.log(Level.WARNING, ex.getMessage());
                }
            } else {
                container.remove (updateEl);
            }
        }
        
        public static int compareInstalledVersions (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Update && u2 instanceof Unit.Update) {
                Unit.Update unit1 = (Unit.Update)u1;
                Unit.Update unit2 = (Unit.Update)u2;
                return new SpecificationVersion (unit1.getInstalledVersion ()).compareTo (new SpecificationVersion (unit2.getInstalledVersion ()));
            }
            return Unit.compareDisplayVersions (u1, u2);
        }
        
        public static int compareAvailableVersions (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Update && u2 instanceof Unit.Update) {
                Unit.Update unit1 = (Unit.Update)u1;
                Unit.Update unit2 = (Unit.Update)u2;
                return new SpecificationVersion (unit1.getAvailableVersion ()).compareTo (new SpecificationVersion (unit2.getAvailableVersion ()));
            }
            return Unit.compareDisplayVersions (u1, u2);
        }
        
        
        public String getInstalledVersion () {
            return installEl.getSpecificationVersion ();
        }
        
        public String getAvailableVersion () {
            return getRelevantElement().getSpecificationVersion ();
        }
        
        public String getSize () {
            return Utilities.getDownloadSizeAsString (updateEl.getDownloadSize ());
        }
        
        @Override
        public UpdateElement getRelevantElement () {
            return updateEl;
        }
        
        @Override
        public int getCompleteSize () {
            if (size == -1) {
                size = 0;
                OperationContainer<OperationSupport> c = OperationContainer.createForDirectUpdate ();
                OperationInfo<OperationSupport> i = c.add (getRelevantElement ());
                Set<UpdateElement> elems = i.getRequiredElements ();
                for (UpdateElement el : elems) {
                    size += el.getDownloadSize ();
                }
                size += getRelevantElement ().getDownloadSize ();
                c.removeAll ();
            }
            return size;
        }

        @Override
        public Type getModelType() {
            return (isNbms) ? UnitCategoryTableModel.Type.LOCAL : UnitCategoryTableModel.Type.UPDATE;            
        }
        
    }
    
    public static class Available extends Unit {
        private UpdateElement updateEl = null;
        private boolean isNbms;
        private int size = -1;
        
        public Available (UpdateUnit unit, boolean isNbms,String categoryName) {
            super (categoryName);
            this.isNbms = isNbms;
            this.updateUnit = unit;
            this.updateEl = unit.getAvailableUpdates ().get (0);
            assert updateEl != null : "Updateable UpdateUnit " + unit + " has UpdateElement for update.";
            initState();
        }
        
        @Override
        public boolean isMarked () {
            OperationContainer container;
            if (isNbms) {
                container = Containers.forAvailableNbms();
            } else if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomInstall ();
            } else {
                container = Containers.forAvailable ();
            }
            return container.contains (updateEl);
        }
        
        @Override
        public void setMarked (boolean marked) {
            assert marked != isMarked ();
            OperationContainer container;
            if (isNbms) {
                container = Containers.forAvailableNbms();
            } else if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == updateUnit.getType ()) {
                container = Containers.forCustomInstall ();
            } else {
                container = Containers.forAvailable ();
            }
            if (marked) {
                try {
                    container.add (updateUnit, updateEl);
                } catch (IllegalArgumentException iae) {
                    log.warning(iae.getMessage());
                }
            } else {
                container.remove (updateEl);
            }
        }
        
        public static int compareAvailableVersion (Unit u1, Unit u2) {
            if (u1 instanceof Unit.Available && u2 instanceof Unit.Available) {
                Unit.Available unit1 = (Unit.Available)u1;
                Unit.Available unit2 = (Unit.Available)u2;
                return new SpecificationVersion (unit1.getAvailableVersion ()).compareTo (new SpecificationVersion (unit2.getAvailableVersion ()));
            }
            return Unit.compareDisplayVersions (u1, u2);
        }

        public static int compareSourceCategories(Unit u1, Unit u2) {
            if (u1 instanceof Unit.Available && u2 instanceof Unit.Available) {
                Unit.Available unit1 = (Unit.Available)u1;
                Unit.Available unit2 = (Unit.Available)u2;
                return Collator.getInstance().compare(unit1.getSourceDescription(), unit2.getSourceDescription());
            }
            
            throw new IllegalStateException();
        }
        
        public String getAvailableVersion () {
            return updateEl.getSpecificationVersion ();
        }
        
        public Integer getMyRating () {
            return null;
        }
        
        public String getSize () {
            return Utilities.getDownloadSizeAsString (updateEl.getDownloadSize ());
        }
        
        @Override
        public UpdateElement getRelevantElement () {
            return updateEl;
        }
        
        @Override
        public int getCompleteSize () {
            if (size == -1) {
                size = 0;
                OperationContainer<OperationSupport> c = OperationContainer.createForDirectInstall ();
                OperationInfo<OperationSupport> i = c.add (getRelevantElement ());
                Set<UpdateElement> elems = i.getRequiredElements ();
                for (UpdateElement el : elems) {
                    size += el.getDownloadSize ();
                }
                size += getRelevantElement ().getDownloadSize ();
                c.removeAll ();
            }
            return size;
        }

        @Override
        public Type getModelType() {
            return (isNbms) ? UnitCategoryTableModel.Type.LOCAL : UnitCategoryTableModel.Type.AVAILABLE;
        }        
        
        public Image getSourceIcon() {
            return updateEl.getSourceIcon();
        }
        public String getSourceDescription() {
            return updateEl.getSourceDescription();
        }
    }

    private static String getBundle (String key) {
        return NbBundle.getMessage (Unit.class, key);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getDisplayName() + "]";
    }
}
