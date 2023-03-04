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
