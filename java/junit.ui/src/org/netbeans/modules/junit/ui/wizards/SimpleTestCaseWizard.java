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

import org.openide.loaders.TemplateWizard;

/**
 *
 * @author  Marian Petras
 */
public class SimpleTestCaseWizard extends TemplateWizard {

    static final String PROP_CLASS_TO_TEST = "classToTest";             //NOI18N
    static final String PROP_TEST_ROOT_FOLDER = "testRootFolder";       //NOI18N

    /** Creates a new instance of SimpleTestCaseWizard */
    public SimpleTestCaseWizard() {
    }

    /**
     * initializes the settings for the settings panel
     */
    @Override
    protected void initialize() {
    }
    
}
