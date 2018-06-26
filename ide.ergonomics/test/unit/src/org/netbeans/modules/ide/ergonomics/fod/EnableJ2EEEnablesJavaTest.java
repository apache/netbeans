/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ide.ergonomics.fod;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import junit.framework.Test;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.Dependency;

import org.openide.util.Lookup;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class EnableJ2EEEnablesJavaTest extends NbTestCase {

    public EnableJ2EEEnablesJavaTest(String name) {
        super(name);
    }

    public static Test suite() {
        Test test = NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(EnableJ2EEEnablesJavaTest.class).
            gui(false).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules("ide[0-9]*", ".*").
            honorAutoloadEager(true)
        );
        return test;
    }

    public void testEnablingJ2EEEnablesJavaViaAutoUpdateManager() throws Exception {
        FeatureInfo j2ee = null;
        FeatureInfo java = null;
        for (FeatureInfo f : FeatureManager.features()) {
            if (f.getCodeNames().contains("org.netbeans.modules.j2ee.kit")) {
                j2ee = f;
            }
            if (f.getCodeNames().contains("org.netbeans.modules.java.kit")) {
                java = f;
            }
        }
        assertNotNull("j2ee feature found", j2ee);
        assertNotNull("java feature found", java);

        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE);
        UpdateElement j2eeUE = null;
        StringBuilder sb = new StringBuilder();
        for (UpdateUnit uu : units) {
            sb.append(uu.getCodeName()).append('\n');
            if (uu.getCodeName().equals("fod.org.netbeans.modules.j2ee.kit")) {
                j2eeUE = uu.getInstalled();
            }
        }
        assertNotNull("J2EE found: " + sb, j2eeUE);
        OperationContainer<OperationSupport> cc = OperationContainer.createForEnable();
        OperationInfo<OperationSupport> info = cc.add(j2eeUE);
        cc.add(info.getRequiredElements());
        cc.getSupport().doOperation(null);


        Set<String> expectedNames = new HashSet<String>(java.getCodeNames());
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (mi.isEnabled()) {
                expectedNames.remove(mi.getCodeNameBase());
            } else {
                for (Dependency d : mi.getDependencies()) {
                    if (d.getType() == Dependency.TYPE_JAVA) {
                        SpecificationVersion v1 = new SpecificationVersion(d.getVersion());
                        SpecificationVersion v2 = Dependency.JAVA_SPEC;
                        if (v2.compareTo(v1) < 0) {
                            // test is running insufficient runtime
                            expectedNames.remove(mi.getCodeNameBase());
                        }
                    }
                }
            }
        }
        if (!expectedNames.isEmpty()) {
            fail(
                "java cluster shall be fully enabled, but this was missing:\n" +
                expectedNames.toString().replace(',', '\n')
            );
        }
    }

}
