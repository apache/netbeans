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

package org.netbeans.jellytools.modules.web;

import org.netbeans.jellytools.Bundle;

/** Helper class for this package.
 *
 * @author Martin.Schovanek@sun.com
 */
public class Helper {

    /** Avoid to create instance, contains only static helper method */
    private Helper() {
    }


    /** Returns the "New Web Application with Existing Ant Script" wizard title.
     */
    public static String freeFormWizardTitle() {
        String newLbl = Bundle.getStringTrimmed(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_NewProjectWizard_Subtitle");
        String webAppLbl = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.freeform.ui.Bundle",
                "TXT_NewWebFreeformProjectWizardIterator_NewProjectWizardTitle");
        return Bundle.getStringTrimmed(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_NewProjectWizard_MessageFormat",
                new Object[] {newLbl, webAppLbl});
    }
}
