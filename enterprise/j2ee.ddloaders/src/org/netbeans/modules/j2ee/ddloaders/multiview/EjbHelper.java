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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;

/**
 * @author pfiala
 */
public class EjbHelper {
    private EjbJarMultiViewDataObject dataObject;
    private Ejb ejb;

    public EjbHelper(EjbJarMultiViewDataObject dataObject, Ejb ejb) {
        this.dataObject = dataObject;
        this.ejb = ejb;
    }

    public EnvEntryHelper getEnvEntryHelper(int rowIndex) {
        return new EnvEntryHelper(ejb.getEnvEntry(rowIndex));
    }

    public int getEnvEntryCount() {
        return ejb.getEnvEntry().length;
    }

    public EnvEntryHelper newEnvEntry() {
        EnvEntry entry = ejb.newEnvEntry();
        ejb.addEnvEntry(entry);
        modelUpdatedFromUI();
        return new EnvEntryHelper(entry);
    }

    public void removeEnvEntry(int row) {
        ejb.removeEnvEntry(ejb.getEnvEntry(row));
        modelUpdatedFromUI();
    }

    private void modelUpdatedFromUI() {
        dataObject.modelUpdatedFromUI();
    }

    public class EnvEntryHelper {
        private EnvEntry envEntry;

        public EnvEntryHelper(EnvEntry envEntry) {
            this.envEntry = envEntry;
            modelUpdatedFromUI();
        }

        public void setEnvEntryName(String value) {
            envEntry.setEnvEntryName(value);
            modelUpdatedFromUI();
        }

        public void setEnvEntryType(String value) {
            envEntry.setEnvEntryType(value);
            modelUpdatedFromUI();
        }

        public void setEnvEntryValue(String value) {
            envEntry.setEnvEntryValue(value);
            modelUpdatedFromUI();
        }

        public void setDescription(String description) {
            envEntry.setDescription(description);
            modelUpdatedFromUI();
        }

        public String getEnvEntryName() {
            return envEntry.getEnvEntryName();
        }

        public String getEnvEntryType() {
            return envEntry.getEnvEntryType();
        }

        public String getEnvEntryValue() {
            return envEntry.getEnvEntryValue();
        }

        public String getDefaultDescription() {
            return envEntry.getDefaultDescription();
        }
    }
}
