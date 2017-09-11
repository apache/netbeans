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

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.TestBase;

/**
 * Tests {@link AddModulePanel}.
 *
 * @author Martin Krauskopf
 */
public class AddModulePanelTest extends TestBase {

    private AddModulePanel amp;

    public AddModulePanelTest(String name) {
        super(name);
    }

    public void testDependenciesFiltering() throws Exception {
        /* XXX have to rewrite to run without GUI:
        NbModuleProject p = generateStandaloneModule("module1");
        final SingleModuleProperties props = SingleModulePropertiesTest.loadProperties(p);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                amp = new AddModulePanel(props);
            }
        });
        while (amp == null || !amp.filterValue.isEnabled()) {
            Thread.sleep(400);
        }
        int all = amp.moduleList.getModel().getSize();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                // fire multiple events to EQ
                amp.filterValue.setText("o");
                amp.filterValue.setText("or");
                amp.filterValue.setText("org");
                amp.filterValue.setText("org.");
                amp.filterValue.setText("org.o");
                amp.filterValue.setText("org.op");
            }
        });
        // wait until filter is applied
        final AtomicBoolean done = new AtomicBoolean();
        while (!done.get()) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    done.set(!CustomizerComponentFactory.isWaitModel(amp.moduleList.getModel()));
                }
            });
            Thread.sleep(200);
        }
        ListModel model = amp.moduleList.getModel();
        int filtered = model.getSize();
        final int EXPECTED_MAX = 50; // XXX really should be computed
        assertTrue("filter was successfull (" + all + " > " + filtered + ")", all > filtered);
        assertTrue("filter was successfull (" + filtered + " > " + EXPECTED_MAX + ")", filtered < EXPECTED_MAX);
        assertTrue("non-wait model", !CustomizerComponentFactory.isWaitModel(amp.moduleList.getModel()));
        assertTrue("non-empty model", !CustomizerComponentFactory.hasOnlyValue(amp.moduleList.getModel(), CustomizerComponentFactory.EMPTY_VALUE));
         */
    }
    
}
