/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.autoupdate.cli;

import java.util.List;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Status {

    static String[] toArray(UpdateUnit uu) {
        if (uu.getInstalled() != null) {
            return new String[] { 
                uu.getCodeName(),
                uu.getInstalled().getSpecificationVersion(), 
                Status.installed(uu).toString()
            };
        } else {
            List<UpdateElement> updates = uu.getAvailableUpdates();
            if (updates.isEmpty()) {
                return new String[] {
                    uu.getCodeName(),
                    "N/A", "N/A"
                };
            }
            UpdateElement first = updates.get(0);
            return new String[] { 
                uu.getCodeName(),
                first.getSpecificationVersion(), 
                Status.update(uu).toString()
            };
        }
    }

    final String txt;

    private Status(String txt) {
        this.txt = txt;
    }
    
    @NbBundle.Messages({
        "MSG_Enabled=Enabled",
        "MSG_Disabled=Installed",
        "# {0} - available version",
        "MSG_UpdateAvailable=Upgrade to {0}",
        "MSG_Unknown=Unknown state"
    })
    private static Status installed(UpdateUnit uu) {
        assert uu.getInstalled() != null;
        String install;
        ModuleInfo mi = find(uu.getCodeName());
        List<UpdateElement> updates = uu.getAvailableUpdates();
        if (!updates.isEmpty()) {
            install = Bundle.MSG_UpdateAvailable(updates.get(0).getSpecificationVersion());
        } else {
            if (mi == null) {
                install = Bundle.MSG_Unknown();
            } else {
                install = mi.isEnabled() ? Bundle.MSG_Enabled() : Bundle.MSG_Disabled();
            }
        }
        
        return new Status(install);
    }
    @NbBundle.Messages({
        "MSG_Updateable=Available"
    })
    private static Status update(UpdateUnit uu) {
        assert uu.getInstalled() == null && !uu.getAvailableUpdates().isEmpty();
        return new Status(Bundle.MSG_Updateable());
    }

    @Override
    public String toString() {
        return txt;
    }
    
    private static ModuleInfo find(String cnb) {
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (cnb.equals(mi.getCodeNameBase())) {
                return mi;
            }
        }
        return null;
    }
}
