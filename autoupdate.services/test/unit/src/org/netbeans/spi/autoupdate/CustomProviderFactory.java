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

package org.netbeans.spi.autoupdate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Jiri Rechtacek
 */
public class CustomProviderFactory {
    
    public static UpdateProvider getCustomUpdateProvider () {
        UpdateProvider provider = new UpdateProvider () {
            public String getName() {
                return "test-custom-provider";
            }

            public String getDisplayName() {
                return "Test Provider provides self-installed components.";
            }

            public String getDescription () {
                return null;
            }

            public Map<String, UpdateItem> getUpdateItems() {
                Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
                res.put ("test-module", createNbmModule ());
                res.put ("test-custom-component", createCustomComponent ());
                return res;
            }

            public boolean refresh(boolean force) {
                return true;
            }

            public CATEGORY getCategory() {
                return CATEGORY.COMMUNITY;
            }
        };
        return provider;
    }
    
    private static UpdateItem createNbmModule () {
        String codeName = "test-module";
        String specificationVersion = "1.0";
        URL distribution = null;
        try {
            distribution = new URL ("http://netbeans.de/module.nbm");
        } catch (MalformedURLException ex) {
            assert false : ex;
        }
        String author = "Jiri Rechtacek";
        String downloadSize = "12";
        String homepage = "http://netbeans.de";
        Manifest manifest = new Manifest ();
        Attributes mfAttrs = manifest.getMainAttributes ();
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module"), "org.test.module/1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Implementation-Version"), "060216");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Long-Description"), "Real module Hello installs Hello menu item into Help menu.");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Module-Dependencies"), "org.openide.util > 6.9.0.1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Name"), "module");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Requires"), "org.openide.modules.ModuleFormat1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Specification-Version"), "1.0");
        UpdateLicense license = UpdateLicense.createUpdateLicense ("none-license", "no-license");
        UpdateItem result = UpdateItem.createModule(codeName,
                                                    specificationVersion,
                                                    distribution, author,
                                                    downloadSize, homepage, null, "test-category",
                                                    manifest, false, false, true, true, "my-cluster",
                                                    license);
        return result;
    }
    
    private static UpdateItem createCustomComponent () {
        String codeName = "test-custom-component";
        String specificationVersion = "0.1";
        URL distribution = null;
        try {
            distribution = new URL ("http://netbeans.org/org/netbeans/api/autoupdate/data/org-yourorghere-engine-1-1.nbm");
        } catch (MalformedURLException ex) {
            assert false : ex;
        }
        String author = "Jiri Rechtacek";
        String downloadSize = "2815";
        String homepage = "http://netbeans.de";
        Manifest manifest = new Manifest ();
        Attributes mfAttrs = manifest.getMainAttributes ();
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module"), "org.test.custom.module/1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Implementation-Version"), "060216");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Long-Description"), "Real module Hello installs Hello menu item into Help menu.");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Name"), "module");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Requires"), "org.openide.modules.ModuleFormat1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Specification-Version"), "0.1");
        CustomInstaller ci = createCustomInstaller ();
        assert ci != null;
        UpdateLicense license = UpdateLicense.createUpdateLicense ("none-license", "no-license");
        UpdateItem result = UpdateItem.createModule(codeName,
                                                    specificationVersion,
                                                    distribution, author,
                                                    downloadSize, homepage, null, "test-category",
                                                    manifest, false, false, true, true, "my-cluster",
                                                    license);
        return result;
    }
    
    private static CustomInstaller createCustomInstaller () {
        return new CustomInstaller () {
            public boolean install (String codeName, String specificationVersion, ProgressHandle handle) throws OperationException {
                assert codeName != null && specificationVersion != null;
                return true;
            }
        };
    }
}
