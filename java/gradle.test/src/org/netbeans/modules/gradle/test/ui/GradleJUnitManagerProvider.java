/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.gradle.test.ui;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gsf.testrunner.api.CommonUtils;
import org.netbeans.modules.gsf.testrunner.api.CoreManager;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.java.testrunner.ui.api.JavaManager;

/**
 *
 * @author Laszlo Kishalmi
 */
@CoreManager.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE, testingFramework = CommonUtils.JUNIT_TF)
public class GradleJUnitManagerProvider extends JavaManager {
    
    @Override
    public void registerNodeFactory() {
        Manager.getInstance().setNodeFactory(new GradleTestRunnerNodeFactory());
}

}
