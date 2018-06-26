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
import junit.framework.Test;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;



/**
 *
 * @author Jiri Rechtacek <jrechtacek@netbeans.org>
 */
public class DisableJ2EEKeepsMobilityEnabledTest extends NbTestCase {

    private FeatureInfo j2ee = null;
    private FeatureInfo mobility = null;
    private UpdateElement j2eeUE = null;
    private UpdateElement mobilityUE = null;
    
    public DisableJ2EEKeepsMobilityEnabledTest(String name) {
        super(name);
    }

    public static Test suite() {
        Test test = NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(DisableJ2EEKeepsMobilityEnabledTest.class).
            gui(false).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules("ide[0-9]*", ".*").
            honorAutoloadEager(true)
        );
        return test;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        for (FeatureInfo f : FeatureManager.features()) {
            if (f.getCodeNames().contains("org.netbeans.modules.j2ee.kit")) {
                j2ee = f;
            }
            if (f.getCodeNames().contains("org.netbeans.modules.j2me.kit")) {
                mobility = f;
            }
        }
        assertNotNull("j2ee feature found", j2ee);
        assertNotNull("mobility feature found", mobility);

        assertFalse("j2ee disabled", j2ee.isEnabled());
        assertFalse("mobility disabled", mobility.isEnabled());

        List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE);
        
        for (UpdateUnit uu : units) {
            if (uu.getCodeName().equals("fod.org.netbeans.modules.j2ee.kit")) {
                j2eeUE = uu.getInstalled();
            }
            if (uu.getCodeName().equals("fod.org.netbeans.modules.j2me.kit")) {
                mobilityUE = uu.getInstalled();
            }
        }
        assertNotNull("J2EE found", j2eeUE);
        assertNotNull("Mobility found", mobilityUE);
        
        OperationContainer<OperationSupport> cc = OperationContainer.createForEnable();
        OperationInfo<OperationSupport> info;
        info = cc.add(j2eeUE);
        cc.add(info.getRequiredElements());
        info = cc.add(mobilityUE);
        cc.add(info.getRequiredElements());
        cc.getSupport().doOperation(null);

        assertTrue("j2ee enabled", j2ee.isEnabled());
        assertTrue("mobility enabled", mobility.isEnabled());
    }

    public void testEnablingJ2EEEnablesJavaViaAutoUpdateManager() throws Exception {
        OperationContainer<OperationSupport> cc = OperationContainer.createForDisable();
        OperationInfo<OperationSupport> info;
        info = cc.add(j2eeUE);
        assertFalse("Mobility remains enabled so far", cc.listAll().toString().contains("org.netbeans.modules.mobility.kit"));
        cc.add(info.getRequiredElements());
        assertFalse("Mobility remains enabled finally", cc.listAll().toString().contains("org.netbeans.modules.mobility.kit"));
    }

}
