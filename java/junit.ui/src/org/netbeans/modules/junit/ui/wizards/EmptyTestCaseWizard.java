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

package org.netbeans.modules.junit.ui.wizards;

import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.junit.api.JUnitSettings;
import org.openide.loaders.TemplateWizard;

/**
 * Wizard for an empty test case.
 *
 * @author  Marian Petras
 */
public class EmptyTestCaseWizard extends TemplateWizard {

    /** name of property &quot;package&quot; */
    static final String PROP_PACKAGE = "package";                       //NOI18N
    /** name of property &quot;class name&quot; */
    static final String PROP_CLASS_NAME = "className";                  //NOI18N
    
    /**
     * initializes the settings for the settings panel
     */
    @Override
    public void initialize() {
        JUnitSettings settings = JUnitSettings.getDefault();
        
        putProperty(GuiUtils.CHK_SETUP,
                    Boolean.valueOf(settings.isGenerateSetUp()));
        putProperty(GuiUtils.CHK_TEARDOWN,
                    Boolean.valueOf(settings.isGenerateTearDown()));
        putProperty(GuiUtils.CHK_HINTS,
                    Boolean.valueOf(settings.isBodyComments()));
    }
    
}
