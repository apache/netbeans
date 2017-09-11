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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;

/**
 *
 * @author Dmitry Lipin
 */
public class ExecutablePermissionsTest extends NbmAdvancedTestCase {

    private UpdateProvider p = null;
    private String testModuleVersion = "1.0";
    private String testModuleName = "org.yourorghere.executable.permissions";

    public ExecutablePermissionsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws IOException, Exception {
        super.setUp();
    }

    @Override
    public boolean canRun() {
        return super.canRun() && !System.getProperty("os.name").startsWith("Windows");
    }

    private String generateExecutablePermissionsModuleElement() {
        String res = "\n<module codenamebase=\"" + testModuleName + "\" " +
                "homepage=\"http://www.netbeans.org/\" distribution=\"nbresloc:/org/netbeans/api/autoupdate/data/org-yourorghere-executable-permissions.nbm\" " +
                "license=\"standard-nbm-license.txt\" downloadsize=\"5122\" " +
                "needsrestart=\"false\" moduleauthor=\"\" " +
                "eager=\"false\" " +
                "releasedate=\"2006/02/23\">\n";
        res += "<manifest OpenIDE-Module=\"" + testModuleName + "\" " +
                "OpenIDE-Module-Name=\"" + testModuleName + "\" " +
                "AutoUpdate-Show-In-Client=\"true\" " +
                "OpenIDE-Module-Specification-Version=\"" + testModuleVersion + "\"/>\n";
        res += "</module>";
        return res;
    }

    public void testExecutablePermissionsModule() throws IOException {
        String os = !org.openide.util.Utilities.isUnix() ? "Windows" : "Unix";
        String catalog = generateCatalog(generateExecutablePermissionsModuleElement());

        p = createUpdateProvider(catalog);
        p.refresh(true);

        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit>();
        Map<String, UpdateItem> updates = p.getUpdateItems();
        assertNotNull("Some modules are installed.", updates);
        assertFalse("Some modules are installed.", updates.isEmpty());
        assertTrue(testModuleName + " found in parsed items.", updates.keySet().contains(testModuleName + "_" + testModuleVersion));

        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault().appendUpdateItems(unitImpls, p);
        assertNotNull("Some units found.", newImpls);
        assertFalse("Some units found.", newImpls.isEmpty());

        UpdateUnit u1 = newImpls.get(testModuleName);
        installUpdateUnit(u1);
        File f = new File(userDir, "bin/start.sh");
        assertTrue("File " + f + " should exist after module installation", f.exists());
        assertTrue("File " + f + " is not executable after module installation", f.canExecute());
    }    
}
