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

package org.netbeans.core.windows.view.ui;

import org.netbeans.junit.NbTestCase;
import org.openide.util.NbBundle;

/**
 * Some basic tests for MainWindow.
 */
public class MainWindowTest extends NbTestCase {

    public MainWindowTest(String testName) {
        super(testName);
    }

    protected boolean runInEQ () {
        return true;
    }

    protected void setUp() throws Exception {
    }

    public void testBrandingTokensExist() {
        // API support creates branding for these tokens to set application title
        assertNotNull("Main window title without projects exists", 
                NbBundle.getMessage(MainWindow.class, "CTL_MainWindow_Title_No_Project"));
        assertNotNull("Main window title without projects exists", 
                NbBundle.getMessage(MainWindow.class, "CTL_MainWindow_Title"));
    }
    
}
