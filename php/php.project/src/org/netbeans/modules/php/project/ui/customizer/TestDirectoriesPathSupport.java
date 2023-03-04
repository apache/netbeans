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

package org.netbeans.modules.php.project.ui.customizer;

import java.util.List;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.classpath.BaseProjectPathSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.util.NbBundle;

public final class TestDirectoriesPathSupport extends BaseProjectPathSupport {

    public TestDirectoriesPathSupport(PropertyEvaluator evaluator, ReferenceHelper referenceHelper, AntProjectHelper antProjectHelper) {
        super(evaluator, referenceHelper, antProjectHelper);
    }

    @Override
    protected boolean isWellKnownPath(String p) {
        return false;
    }

    //~ Inner classes

    public static final class Validator {

        private final ValidationResult result = new ValidationResult();


        public ValidationResult getResult() {
            return new ValidationResult(result);
        }

        public Validator validatePaths(PhpProject project, List<BasePathSupport.Item> items) {
            for (Item item : items) {
                validatePath(project, item);
            }
            return this;
        }

        @NbBundle.Messages({
            "# {0} - file path",
            "# {1} - error message",
            "TestDirectoriesPathSupport.Validator.error={0}: {1}",
        })
        public Validator validatePath(PhpProject project, BasePathSupport.Item item) {
            String error = Utils.validateTestSources(project, item.getAbsoluteFilePath(project.getProjectDirectory()));
            if (error != null) {
                String filePath = item.getFilePath();
                result.addError(new ValidationResult.Message(filePath, Bundle.TestDirectoriesPathSupport_Validator_error(filePath, error)));
            }
            return this;
        }

    }

}
