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

package org.netbeans.modules.projectimport.eclipse.gui;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ImporterWizard  extends JellyTestCase {
    public ImporterWizard(String testName) {
        super(testName);
    }

    @Override
    public void setUp() {
        new ActionNoBlock(ImporterMenu.menuItemString, null).performMenu();
    }

    public void testImporterWizard() {
        String caption = Bundle.getStringTrimmed("org.netbeans.modules.projectimport.eclipse.core.wizard.Bundle", "CTL_WizardTitle");
        NbDialogOperator importWizard = new NbDialogOperator(caption);
        importWizard.cancel();
    }
}
