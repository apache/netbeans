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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Dmitry Lipin
 */
public class CrossDependencyTest extends NbmAdvancedTestCase {
    private static final Logger LOG = Logger.getLogger(CrossDependencyTest.class.getName());
    
    public CrossDependencyTest (String testName) {
        super (testName);
    }

    public void testCrossDependentModules () throws Exception {
        checkCrossDependentModulesDependencies();
    }

    private String generateModule(String codeName, String dependentModule, String needs, String provides, String version) {
        return "<module codenamebase=\"" + codeName + "\" " +
                "homepage=\"http://au.netbeans.org/\" distribution=\"\" " +
                "license=\"standard-nbm-license.txt\" downloadsize=\"98765\" " +
                "needsrestart=\"false\" moduleauthor=\"\" " +
                "eager=\"" + false + "\" " +
                "releasedate=\"2006/02/23\">\n" +
                "<manifest OpenIDE-Module=\"" + codeName + "\" " +
                "OpenIDE-Module-Name=\"" + codeName + "\" " +
                "AutoUpdate-Show-In-Client=\"" + false + "\" " +
                (dependentModule ==null ? "" : "OpenIDE-Module-Module-Dependencies=\"" + dependentModule + "\" ") +
                (needs == null ? "" : "OpenIDE-Module-Needs=\"" + needs + "\" ") +
                (provides==null ? "" : "OpenIDE-Module-Provides=\"" + provides + "\" ") +
                "OpenIDE-Module-Specification-Version=\"" + version + "\"/>" +
                "</module>\n";
    }
    @SuppressWarnings("unchecked")
    private void checkCrossDependentModulesDependencies () throws Exception {
        //Issue #161917
        String module1 = "org.yourorghere.module1";
        String module2 = "org.yourorghere.module2";
        
        String token1 = "token1";
        String token2 = "token2";

        String version1 = "1.0";
        String version2 = "1.0";

        String module1Part = generateModule(module1, module2, token1, token2, version1);
        String module2Part = generateModule(module2, null,    token2, token1, version2);
        

        String catalog = generateCatalog (module1Part, module2Part);

        AutoupdateCatalogProvider p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateItem> updates = p.getUpdateItems ();

        // initial check of updates being and its states

        ModuleItem module1Item = (ModuleItem) Trampoline.SPI.impl (updates.get (module1 + "_" + version1));
        ModuleItem module2Item = (ModuleItem) Trampoline.SPI.impl (updates.get (module2 + "_" + version2));


        // acquire UpdateUnits for test modules
        UpdateUnitProviderFactory.getDefault ().create ("test-update-provider", "test-update-provider", generateFile (catalog));
        UpdateUnitProviderFactory.getDefault ().refreshProviders (null, true);
        
        UpdateUnit module1UU = UpdateManagerImpl.getInstance ().getUpdateUnit (module1);
        UpdateUnit module2UU = UpdateManagerImpl.getInstance ().getUpdateUnit (module2);

        // add modules to install container
        OperationContainer ic = OperationContainer.createForInstall ();
        ic.add (module1UU.getAvailableUpdates ().get (0));
        ic.add (module2UU.getAvailableUpdates ().get (0));

        final UpdateElement ue1 = module1UU.getAvailableUpdates().get(0);
        final UpdateElement ue2 = module2UU.getAvailableUpdates().get(0);
        final Collection <ModuleInfo> col = new LinkedList<ModuleInfo> ();

        col.add(module1Item.getModuleInfo());
        col.add(module2Item.getModuleInfo());

        
        final Set <Dependency> broken1 = new HashSet <Dependency> ();
        Set <UpdateElement> set1 = Utilities.findRequiredUpdateElements(ue1, col, broken1, true, null);
        LOG.info("required (1): " + set1);
        LOG.info("broken (1): " + broken1);
        final Set <Dependency> broken2 = new HashSet <Dependency> ();
        Set <UpdateElement> set2 = Utilities.findRequiredUpdateElements(ue2, col, broken2, true, null);
        LOG.info("required (2): " + set2);
        LOG.info("broken (2): " + broken2);
    }
}
