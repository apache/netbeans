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

package org.netbeans.api.autoupdate;

import java.util.List;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.modules.autoupdate.services.UpdateUnitImpl;

/** Instances provided by the <code>UpdateManager</code> which represents wrapper of
 * <code>UpdateElement</code>. The one unit contains all available elements of
 * the same type. For example, Editor module version 1.1 is installed in the IDE; a server with a module
 * update can contain Editor module version 1.2; and Editor version 1.0 is in
 * IDE backup.
 * 
 * @author Jiri Rechtacek (jrechtacek@netbeans.org)
 */
public final class UpdateUnit {
    static {
        Trampoline.API = new TrampolineAPI();
    }
    
    final UpdateUnitImpl impl;
    
    UpdateUnit (UpdateUnitImpl i) {
        this.impl = i;
    }

    /** Return code name of unit, it's unique among rest of another units.
     * 
     * @return code name
     */
    public String getCodeName () {
        return impl.getCodeName();
    }
    
    /** Returns installed <code>UpdateElement</code> if any or null if
     * no element which unit's code name is already installed.
     * 
     * @return installed <code>UpdateElement</code>
     */
    public UpdateElement getInstalled () {
        return impl.getInstalled();
    }
    
    /** Returns list of available element which are not installed in IDE
     * and has higher version then installed element (is any). These elements
     * can be installed as new one element or as update of already installed element.
     * 
     * @return list of available and not installed <code>UpdateElement</code>
     */
    public List<UpdateElement> getAvailableUpdates () {
        return impl.getAvailableUpdates();
    }
    
    /** Returns <code>UpdateElement</code> in IDE backup if any or null. The element
     * can found in backup if any another element did update them.
     * 
     * @return <code>UpdateElement</code> from backup
     */
    public UpdateElement getBackup () {
        return impl.getBackup();
    }

    /** Returns localization <code>UpdateElement</code> active with current <code>Locale</code>
     * or null.
     * 
     * @return localization <code>UpdateElement</code> installed in IDE
     */
    public UpdateElement getInstalledLocalization () {
        return impl.getInstalledLocalization ();
    }
    
    /** Returns list of available localization active with current <code>Locale</code>,
     * the localization are not installed in IDE and has higher version then
     * installed localization (is any). These elements can be installed as new one element
     * or as update of already installed element.
     * 
     * @return list of available and not installed localization <code>UpdateElement</code>
     */
    public List<UpdateElement> getAvailableLocalizations () {
        return impl.getAvailableLocalizations ();
    }
    
    public UpdateManager.TYPE getType () {
        return impl.getType ();
    }
    
    /** Returns if the <code>UpdateUnit</code> is in a pending state.
     * State of this unit will be changed while restart the application.
     * 
     * @return true of UpdateUnit is pending
     */
    public boolean isPending () {
        return impl.isPending ();
    }
    
    /** Returns a nearest plugin depending upon this unit which is supposed
     * to be visible in application UI.
     * Works only for units which are already installed in the application.
     * 
     * @since 1.37
     * @return UpdateUnit or null
     */
    public UpdateUnit getVisibleAncestor() {
        if (getInstalled() != null) {
            return impl.getVisibleAncestor();
        } else {
            return null;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final UpdateUnit other = (UpdateUnit) obj;

        if (this.impl != other.impl &&
            (this.impl == null || !this.impl.equals(other.impl)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;

        hash = 59 * hash + (this.impl != null ? this.impl.hashCode()
                                              : 0);
        return hash;
    }

    @Override
    public String toString() {
        return impl.getCodeName();
    }

}
