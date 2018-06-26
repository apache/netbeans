/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

        DescriptionStep step = new DescriptionStep(true);
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
