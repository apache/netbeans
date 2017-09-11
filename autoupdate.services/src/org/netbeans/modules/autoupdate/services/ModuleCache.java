/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.autoupdate.services;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.openide.modules.ModuleInfo;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Checks and caches code name base to module info mapping.
 *
 * @author Jirka Rechtacek
 */
final class ModuleCache
implements PropertyChangeListener, LookupListener {
    private static ModuleCache INSTANCE;

    private final Lookup.Result<ModuleInfo> result;
    private final ChangeSupport support;
    private Map<String,ModuleInfo> infos;

    private ModuleCache() {
        support = new ChangeSupport(this);
        result = Lookup.getDefault().lookupResult(ModuleInfo.class);
        result.addLookupListener(this);
        resultChanged(null);
    }

    public static synchronized ModuleCache getInstance () {
        if (INSTANCE == null) {
            INSTANCE = new ModuleCache();
        }
        return INSTANCE;
    }
    
    public void addChangeListener(ChangeListener l) {
        support.addChangeListener(l);
    }
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
    }
    
    public ModuleInfo find(String cnb) {
        return infos.get(cnb);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        for (ModuleInfo m : result.allInstances()) {
            m.removePropertyChangeListener(this);
            m.addPropertyChangeListener(this);
        }
        Map<String,ModuleInfo> tmp = new HashMap<String,ModuleInfo>();
        for (ModuleInfo mi : result.allInstances()) {
            tmp.put(mi.getCodeNameBase(), mi);
        }
        infos = tmp;
        if (ev != null) {
            fireChange();
        }
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
            ModuleInfo mi = (ModuleInfo)evt.getSource();

            /*
            fireChange();
            if (mi.isEnabled()) {
                enabledCnbs.add(mi.getCodeNameBase());
            } else {
                enabledCnbs.remove(mi.getCodeNameBase());
            }
             */
        }
    }

    private void fireChange() {
        support.fireChange();
    }

}
