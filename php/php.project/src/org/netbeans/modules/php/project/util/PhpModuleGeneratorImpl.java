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
package org.netbeans.modules.php.project.util;

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = PhpModuleGenerator.class, position = 100)
public class PhpModuleGeneratorImpl implements PhpModuleGenerator {

    @Override
    public PhpModule createModule(CreateProperties properties) throws IOException {
        checkProperties(properties);

        AntProjectHelper projectHelper = PhpProjectGenerator.createProject(map(properties), null);
        Project project = FileOwnerQuery.getOwner(projectHelper.getProjectDirectory());
        assert project != null;
        PhpProject phpProject = project.getLookup().lookup(PhpProject.class);
        if (phpProject == null) {
            throw new IllegalStateException("PHP project needed but found " + project.getClass().getName());
        }
        return phpProject.getPhpModule();
    }

    //~ Mappers

    private void checkProperties(CreateProperties properties) {
        ValidationResult result = new CreatePropertiesValidator()
                .validate(properties)
                .getResult();
        if (result.hasErrors()) {
            throw new IllegalArgumentException(result.getErrors().get(0).getMessage());
        }
        if (result.hasWarnings()) {
            throw new IllegalArgumentException(result.getWarnings().get(0).getMessage());
        }
    }

    private PhpProjectGenerator.ProjectProperties map(CreateProperties properties) {
        PhpProjectGenerator.ProjectProperties projectProperties = new PhpProjectGenerator.ProjectProperties();
        projectProperties.setName(properties.getName());
        projectProperties.setProjectDirectory(properties.getProjectDirectory());
        projectProperties.setSourcesDirectory(properties.getSourcesDirectory());
        projectProperties.setPhpVersion(properties.getPhpVersion());
        projectProperties.setCharset(properties.getCharset());
        projectProperties.setAutoconfigured(properties.isAutoconfigured());
        // default props
        projectProperties.setUrl("http://localhost/"); // NOI18N
        return projectProperties;
    }

}
