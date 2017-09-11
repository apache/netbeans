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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
