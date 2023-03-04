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
package org.netbeans.modules.php.project.ui.actions.support;

import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.internalserver.InternalWebServer;
import org.netbeans.modules.php.project.runconfigs.RunConfigInternal;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigInternalValidator;
import org.openide.util.Lookup;

class ConfigActionInternal extends ConfigActionLocal {

    ConfigActionInternal(PhpProject project) {
        super(project);
    }

    @Override
    public boolean isProjectValid() {
        return isValid();
    }

    @Override
    public boolean isFileValid() {
        return isValid();
    }

    private boolean isValid() {
        boolean valid = RunConfigInternalValidator.validateConfigAction(RunConfigInternal.forProject(project)) == null;
        if (!valid) {
            showCustomizer();
        }
        return valid;
    }

    @Override
    public void debugFile(Lookup context) {
        if (!startInternalServer()) {
            return;
        }
        super.debugFile(context);
    }

    @Override
    public void debugProject() {
        if (!startInternalServer()) {
            return;
        }
        super.debugProject();
    }

    @Override
    public void runFile(Lookup context) {
        if (!startInternalServer()) {
            return;
        }
        super.runFile(context);
    }

    @Override
    public void runProject() {
        if (!startInternalServer()) {
            return;
        }
        super.runProject();
    }

    private boolean startInternalServer() {
        return project.getLookup().lookup(InternalWebServer.class).start();
    }

}
