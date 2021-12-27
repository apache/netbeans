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

package org.netbeans.modules.gradle.javaee;

import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;

/**
 *
 * @author Laszlo Kishalmi
 */
class DummyModuleChangeReporter implements ModuleChangeReporter {

    static final ModuleChangeReporter DUMMY_REPORTER = new DummyModuleChangeReporter();

    EjbChangeDescriptor ejbChanges = new EjbChangeDescriptor() {
        @Override
        public boolean ejbsChanged() {
            return false;
        }

        @Override
        public String[] getChangedEjbs() {
            return new String[0];
        }
    };

    private  DummyModuleChangeReporter() {
    }

    @Override
    public EjbChangeDescriptor getEjbChanges(long timestamp) {
        return ejbChanges;
    }

    @Override
    public boolean isManifestChanged(long timestamp) {
        return false;
    }

}
