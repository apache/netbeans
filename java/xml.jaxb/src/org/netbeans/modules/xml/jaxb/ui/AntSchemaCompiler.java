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

package org.netbeans.modules.xml.jaxb.ui;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.spi.SchemaCompiler;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.WizardDescriptor;

/**
 *
 * @author mkuchtiak
 */
public class AntSchemaCompiler implements SchemaCompiler {
    private Project project;

    public AntSchemaCompiler(Project project) {
        this.project = project;
    }

    @Override
    public void compileSchema(WizardDescriptor wiz) {
        ProjectHelper.compileXSDs(project, true);
    }

    @Override
    public void importResources(WizardDescriptor wiz) throws java.io.IOException {
        Schema nSchema = ProjectHelper.importResources(project,
                wiz, null);
        ProjectHelper.addSchema2Model(project, nSchema);
    }

}
