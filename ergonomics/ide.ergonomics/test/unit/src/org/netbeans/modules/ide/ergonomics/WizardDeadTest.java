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

package org.netbeans.modules.ide.ergonomics;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComponent;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.newproject.DescriptionStep;
import org.netbeans.modules.ide.ergonomics.newproject.FeatureOnDemandWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Pavel Flaska
 */
public class WizardDeadTest extends NbTestCase {

    public WizardDeadTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testDescriptionStep() throws InterruptedException, InvocationTargetException, IOException {
        @SuppressWarnings("unchecked")
        final WizardDescriptor wd = new WizardDescriptor(new Panel[0]);
        FileObject fob = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        assertNotNull("Template found", fob);
        wd.putProperty(FeatureOnDemandWizardIterator.CHOSEN_TEMPLATE, fob);

        DescriptionStep step = new DescriptionStep();
        step.readSettings(wd);
        JComponent panel = (JComponent) step.getComponent();
        assertFalse("Module not yet found", isModuleEnabled());
        panel.firePropertyChange("finding-modules", 0, 1);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(500);
        }
        assertTrue("Module found", isModuleEnabled());
    }

    private boolean isModuleEnabled() {
        for (ModuleInfo inf : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if ("org.netbeans.modules.java.kit".equals(inf.getCodeNameBase())) {
                return inf.isEnabled();
            }
        }
        fail("Java Kit not found!");
        return false;
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(WizardDeadTest.class).
            gui(false).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules("ide[0-9]*", ".*").
            honorAutoloadEager(true)
        );
    }

}
