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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;


/** XXX <code>UpdateProvider</code> providers items for Autoupdate infrastructure. The items
 * are available on e.g. Update Center. Items can represents NetBeans Module,
 * its Localization, Feature as group of NetBeans Modules or special
 * components which needs own native installer to make them accessible in NetBeans product.
 * The infrastructure finds out <code>UpdateProvider</code> in <code>Lookup.defaultProvider()</code>,
 * the provider can be registring declaratively in XML layer.
 * Note: the former Autoupdate module allows declaration of former <code>AutoupdateType</code> on XML
 * layer, these declaration are read as new one UpdateProvider by reason of backward compatability.
 *
 * @author Jiri Rechtacek
 */
public abstract class InstalledUpdateProvider {
    protected InstalledUpdateProvider() {
    }
    
    /** Method for subclasses to provide their own list of installed modules.
     */
    protected abstract Map<String, ModuleInfo> getModuleInfos (boolean force);
    

    /** Handy method for obtaining list of installed modules.
     */
    public static Map<String, ModuleInfo> getInstalledModules () {
        return defaultProvider ().getModuleInfos (false);
    }
    
    private static InstalledUpdateProvider defaultProvider () {
        InstalledUpdateProvider iup = Lookup.getDefault().lookup(InstalledUpdateProvider.class);
        assert iup != null;
        return iup;
    }

    /** The {@link UpdateProvider} that represents the installed modules
     * as returned by {@link #getModuleInfos(boolean)}.
     */
    public static UpdateProvider getDefault() {
        return UP.DEFAULT;
    }
    
    private static final class UP implements UpdateProvider {
        static final UP DEFAULT = new UP();
        
        private UP() {
        }
        
        @Override
        public String getName () {
            return "installed-module-provider";
        }

        @Override
        public String getDisplayName () {
            return getName ();
        }

        @Override
        public String getDescription () {
            return null;
        }

        @Override
        public CATEGORY getCategory() {
            return CATEGORY.COMMUNITY;
        }


        @Override
        public Map<String, UpdateItem> getUpdateItems () throws IOException {
            Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
            for (ModuleInfo info : defaultProvider().getModuleInfos (true).values ()) {
                Date time = null; // XXX: it's too expensive, should be extracted lazy - Utilities.readInstallTimeFromUpdateTracking (info);
                String installTime = null;
                if (time != null) {
                    installTime = Utilities.formatDate(time);
                }
                UpdateItemImpl impl = new InstalledModuleItem (
                        info.getCodeNameBase (),
                        info.getSpecificationVersion () == null ? null : info.getSpecificationVersion ().toString (),
                        info,
                        null, // XXX author
                        null, // installed cluster
                        installTime

                        );

                UpdateItem updateItem = Utilities.createUpdateItem (impl);
                res.put (info.getCodeName () + '_' + info.getSpecificationVersion (), updateItem);
            }
            return res;
        }

        @Override
        public boolean refresh (boolean force) throws IOException {
            defaultProvider().getModuleInfos(false);
            return true;
        }
    }
}
