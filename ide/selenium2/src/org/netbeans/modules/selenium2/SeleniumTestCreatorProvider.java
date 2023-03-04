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
package org.netbeans.modules.selenium2;

import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider.Context;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider.Registration;
import org.netbeans.modules.selenium2.api.Selenium2Support;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Theofanis Oikonomou
 */
@Registration(displayName = TestCreatorProvider.FRAMEWORK_SELENIUM, identifier = TestCreatorProvider.IDENTIFIER_SELENIUM)
public class SeleniumTestCreatorProvider extends TestCreatorProvider {

    @Override
    public boolean enable(FileObject[] activatedFOs) {
        return Selenium2Support.isSupportEnabled(activatedFOs);
    }

    @Override
    public void createTests(Context context) {
        Selenium2Support.createTests(context);
    }

}
