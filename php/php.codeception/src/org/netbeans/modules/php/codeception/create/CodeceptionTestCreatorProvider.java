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
package org.netbeans.modules.php.codeception.create;

import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.php.codeception.CodeceptionTestingProvider;
import org.netbeans.modules.php.spi.testing.create.CreateTestsSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

@NbBundle.Messages("CodeceptionTestCreatorProvider.name=Codeception")
@TestCreatorProvider.Registration(identifier = CodeceptionTestingProvider.IDENTIFIER, displayName = "#CodeceptionTestCreatorProvider.name")
public class CodeceptionTestCreatorProvider extends TestCreatorProvider {

    @Override
    public boolean enable(FileObject[] activatedFOs) {
        return CreateTestsSupport.create(CodeceptionTestingProvider.getInstance(), activatedFOs)
                .isEnabled();
    }

    @Override
    public void createTests(Context context) {
        CreateTestsSupport.create(CodeceptionTestingProvider.getInstance(), context.getActivatedFOs())
                .createTests(context.getConfigurationPanelProperties());
    }

}
