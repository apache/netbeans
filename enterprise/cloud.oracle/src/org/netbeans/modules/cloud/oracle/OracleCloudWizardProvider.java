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
package org.netbeans.modules.cloud.oracle;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "DisplayName=Oracle Cloud"
})
public class OracleCloudWizardProvider implements ServerWizardProvider {

    @Override
    public String getDisplayName() {
        return Bundle.DisplayName();
    }

    @Override
    public WizardDescriptor.InstantiatingIterator getInstantiatingIterator() {
        return OCIManager.loadDefaultConfigProfiles() ? 
                new SimpleIterator() :
                new OracleCloudWizardIterator();
    }
    
    private static class SimpleIterator implements WizardDescriptor.InstantiatingIterator {

        @Override
        public Set instantiate() throws IOException {
            // force profiles collection
            OCIManager.getDefault().getConnectedProfiles();
            return Collections.emptySet();
        }

        @Override
        public void initialize(WizardDescriptor wizard) {
        }

        @Override
        public void uninitialize(WizardDescriptor wizard) {
        }

        @Override
        public WizardDescriptor.Panel current() {
            return null;
        }

        @Override
        public String name() {
            return Bundle.LBL_OC();
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public void nextPanel() {
        }

        @Override
        public void previousPanel() {
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }
    }
}
