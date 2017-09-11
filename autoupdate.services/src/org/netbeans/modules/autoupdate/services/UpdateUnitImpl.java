/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.autoupdate.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.SpecificationVersion;


public abstract class UpdateUnitImpl extends Object {
    private String codeName;
    private UpdateElement installed;
    private List<UpdateElement> updates;
    private UpdateElement installedLocalization;
    private List<UpdateElement> localizationUpdates;
    private UpdateElement backup;
    private UpdateUnit updateUnit;
    
    private static final Logger err = Logger.getLogger (UpdateUnitImpl.class.getName ());

    public UpdateUnitImpl (String codename) {
        this.codeName = codename;
    }

    public void setUpdateUnit (UpdateUnit unit) {
        assert unit != null : "UpdateUnit cannot for " + this + " cannot be null.";
        updateUnit = unit;
    }
    
    public UpdateUnit getUpdateUnit () {
        return updateUnit;
    }
    
    public String getCodeName () {
        return codeName;
    }

    public UpdateElement getInstalled () {
        return installed;
    }

    public List<UpdateElement> getAvailableUpdates () {
        return identifyUpdates (getInstalled (), updates);
    }

    public UpdateElement getInstalledLocalization () {
        return installedLocalization;
    }

    public List<UpdateElement> getAvailableLocalizations () {
        return identifyLocalizationUpdates (installedLocalization, localizationUpdates);
    }

    public UpdateElement getBackup () {
        return backup;
    }

    public void addUpdate (UpdateElement update) {
        if (updates == null) {
            updates = new ArrayList<UpdateElement> ();
        }
        //assert ! updates.contains (update);
        //TODO: make better
        int idx = updates.indexOf(update);
        if (idx != -1) {
            updates.remove(update);
        }
        updates.add (update);
    }
            
    public void setInstalled (UpdateElement installed) {
        assert this.installed == null;
        assert installed != null;
        this.installed = installed;
    }
    
    public void setAsUninstalled () {
        assert this.installed != null;
        this.installed = null;
    }

    public void updateInstalled (UpdateElement installed) {
        //assert this.installed != null;
        this.installed = null;
        setInstalled (installed);
    }
    
    public void addLocalizationUpdate (UpdateElement update) {
        if (localizationUpdates == null) {
            localizationUpdates = new ArrayList<UpdateElement> ();
        }
        assert ! localizationUpdates.contains (update);
        localizationUpdates.add (update);
    }

    public void setInstalledLocalization (UpdateElement installed) {
        assert this.installedLocalization == null;
        this.installedLocalization = installed;
    }

    public void setBackup (UpdateElement backup) {
        assert this.backup == null;
        this.backup = backup;
    }
    
    public boolean isPending () {
        return false;
    }
    
    public abstract UpdateManager.TYPE getType ();
    
    protected List<UpdateElement> getUpdates () {
        return updates;
    }
    
    public UpdateElement findUpdateSameAsInstalled() {
        // check if potentinally updates exist
        if (updates != null && !updates.isEmpty()) {

            // check if a module is already installed
            if (installed == null) {
                return null;
            } else {
                // compare installed with optionallly update
                for (UpdateElement update : updates) {
                    String uspec = update.getSpecificationVersion();
                    String ispec = installed.getSpecificationVersion();
                    if (uspec == null && ispec == null) {
                        err.log(Level.FINE, "Installed UpdateElement w/o version " + installed.getCodeName() + "[" + ispec + "] has update w/o version " + update.getCodeName() + "[" + uspec + "] too");
                        return update;
                    } else if (uspec != null && ispec != null
                            && new SpecificationVersion(uspec).compareTo(new SpecificationVersion(ispec)) == 0) {
                        err.log(Level.FINE, "Installed UpdateElement " + installed.getCodeName() + "[" + ispec + "] has update with the same version " + update.getCodeName() + "[" + uspec + "]");
                        return update;
                    }
                }
                return null;
            }
        } else {
            return null;
        }
    }
    
    public abstract UpdateUnit getVisibleAncestor();

    private List<UpdateElement> identifyUpdates (UpdateElement installed, List<UpdateElement> updates) {
        List<UpdateElement> res = null;

        // check if potentinally updates exist
        if (updates != null && ! updates.isEmpty ()) {

            // check if a module is already installed
            if (installed == null) {
                if (updates != null) {
                    res = updates;
                }
            } else {
                // compare installed with optionallly update
                String moduleId = installed.getCodeName ();
                List<UpdateElement> realUpdates = new ArrayList<UpdateElement> ();
                for (UpdateElement update : updates) {
                    String uspec = update.getSpecificationVersion ();
                    String ispec = installed.getSpecificationVersion ();
                    if (uspec != null && ispec == null) {
                        err.log (Level.FINE, "UpdateElement " + moduleId + "[" + ispec + "] has update " + moduleId + "[" + uspec + "]");
                        realUpdates.add (update);
                    } else if (uspec == null || ispec == null) {
                        // no update
                    } else if (new SpecificationVersion (uspec).compareTo (new SpecificationVersion (ispec)) > 0) {
                        err.log (Level.FINE, "UpdateElement " + moduleId + "[" + ispec + "] has update " + moduleId + "[" + uspec + "]");
                        realUpdates.add (update);
                    }
                }
                if (! realUpdates.isEmpty ()) {
                    res = realUpdates;
                }
            }
        }

        if (res == null) {
            res = Collections.emptyList();
        } else if (res.size() > 1) {
        Collections.sort(res,new Comparator<UpdateElement>(){
            @Override
            public int compare(UpdateElement o1, UpdateElement o2) {
                String sv1 = o1.getSpecificationVersion ();
                String sv2 = o2.getSpecificationVersion ();
                if (sv1 == null) {
                    if (sv2 == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else if (sv2 == null) {
                    return 1;
                }
                return new SpecificationVersion (sv2).compareTo (new SpecificationVersion (sv1));
            }
        });
        }
        return res; 
    }

    private List<UpdateElement> identifyLocalizationUpdates (UpdateElement installed, List<UpdateElement> updates) {
        // XXX: handle identifyLocalizationUpdates
        List<UpdateElement> res = null;

        // check if potentinally updates exist
        if (updates != null && ! updates.isEmpty ()) {

            // check if a module is already installed
            if (installed == null) {
                return updates;

            } else {
                // compare installed with optionallly update
                String moduleId = installed.getCodeName ();
                List<UpdateElement> realUpdates = new ArrayList<UpdateElement> ();
                for (UpdateElement update : updates) {
                    if (update.getSpecificationVersion ().compareTo (installed.getSpecificationVersion ()) > 0) {
                        err.log (Level.FINE, "Module " + moduleId + "[" + installed.getSpecificationVersion () + "] has update " + moduleId + "[" + update.getSpecificationVersion () + "]");
                        realUpdates.add (update);
                    }
                }
                if (! realUpdates.isEmpty ()) {
                    res = realUpdates;
                }
            }
        }

        return res;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final UpdateUnitImpl other = (UpdateUnitImpl) obj;

        if (this.codeName != other.codeName &&
            (this.codeName == null || !this.codeName.equals(other.codeName)))
            return false;
        if (this.installed != other.installed &&
            (this.installed == null || !this.installed.equals(other.installed)))
            return false;
         
        if (this.updates != other.updates &&
            (this.updates == null || !this.updates.equals(other.updates)))
            return false;
        if (this.installedLocalization != other.installedLocalization &&
            (this.installedLocalization == null ||
             !this.installedLocalization.equals(other.installedLocalization)))
            return false;
        if (this.localizationUpdates != other.localizationUpdates &&
            (this.localizationUpdates == null ||
             !this.localizationUpdates.equals(other.localizationUpdates)))
            return false;
        if (this.backup != other.backup &&
            (this.backup == null || !this.backup.equals(other.backup)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 53 * hash + (this.codeName != null ? this.codeName.hashCode()
                                                  : 0);
        hash = 53 * hash +
               (this.installed != null ? this.installed.hashCode()
                                       : 0);
        hash = 53 * hash + (this.updates != null ? this.updates.hashCode()
                                                 : 0);
        hash = 53 * hash +
               (this.installedLocalization != null ? this.installedLocalization.hashCode()
                                                   : 0);
        hash = 53 * hash +
               (this.localizationUpdates != null ? this.localizationUpdates.hashCode()
                                                 : 0);
        hash = 53 * hash + (this.backup != null ? this.backup.hashCode()
                                                : 0);
        return hash;
    }

}

