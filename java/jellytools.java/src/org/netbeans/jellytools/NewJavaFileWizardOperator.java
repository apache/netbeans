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

package org.netbeans.jellytools;

/**
 * Java-specific extension to NewFileWizardOperator.
 *
 * @author Vojtech.Sigler@sun.com
 */
public class NewJavaFileWizardOperator extends NewFileWizardOperator
{

    /** Creates a new object from template. It invokes new file wizard,
     * sets given project, category and file type. On the next panel it
     * sets package and object name. If package name is null or empty, it lets
     * the default one.
     * @param projectName name of project in which new object should be created
     * @param category category to be selected
     * @param fileType file type to be selected
     * @param packageName package name of new object
     * @param name name of created object
     */
    public static void create(String projectName, String category, String fileType, String packageName, String name) {
        String wizardTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle",
                                              "LBL_NewFileWizard_Title");
        NewFileWizardOperator nfwo = invoke(wizardTitle);
        nfwo.selectProject(projectName);
        nfwo.selectCategory(category);
        nfwo.selectFileType(fileType);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName(name);
        if(packageName != null && !"".equals(packageName)) {
            nfnlso.setPackage(packageName);
        }
        nfnlso.finish();
    }

}
