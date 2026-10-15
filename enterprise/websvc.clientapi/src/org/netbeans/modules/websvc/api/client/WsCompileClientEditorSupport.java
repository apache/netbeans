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

package org.netbeans.modules.websvc.api.client;

import java.util.List;
import java.util.Collections;

import javax.swing.JPanel;

import org.openide.WizardValidationException;

/**
 *
 * @author Peter Williams
 */
public interface WsCompileClientEditorSupport {

    /** Editor fires this property event when the user edits a feature list
     */
    public static final String PROP_FEATURES_CHANGED = "featuresChanged"; //NOI18N
    public static final String PROP_OPTIONS_CHANGED = "optionsChanged"; //NOI18N
    
    public WsCompileClientEditorSupport.Panel getWsCompileSupport();
    
    public interface Panel {

        /** The panel for the host
         */
        public JPanel getComponent();
        
        /** Call to initialize the properties in the editor panel
         */
        public void initValues(List/*ServiceSettings*/ settings);
        
        /** Validation entry point.
         */
        public void validatePanel() throws WizardValidationException;
    }
    
    public final class ServiceSettings {
        private String name;
        private ClientStubDescriptor stubType;
        private List<String> availableFeatures;
        private List<String> importantFeatures;
        private String currentFeatures;
        private String newFeatures;
        private boolean[] options;
        
        public ServiceSettings(String sn, ClientStubDescriptor st, boolean[] options, String c, List a, List i) {
            name = sn;
            stubType = st;
            this.options=options;
            currentFeatures = newFeatures = c;
            availableFeatures = Collections.unmodifiableList(a);
            importantFeatures = Collections.unmodifiableList(i);
        }
        
        public String getServiceName() {
            return name;
        }
        
        public ClientStubDescriptor getClientStubDescriptor() {
            return stubType;
        }
        
        public String getCurrentFeatures() {
            return currentFeatures;
        }
        
        public boolean[] getOptions() {
            return options;
        }
        
        public void setOptions(boolean[] options) {
            this.options=options;
        }
        
        public String getNewFeatures() {
            return newFeatures;
        }
        
        public List<String> getAvailableFeatures() {
            return availableFeatures;
        }
        
        public List<String> getImportantFeatures() {
            return importantFeatures;
        }
        
        public String toString() {
            return getServiceName();
        }
        
        public void setNewFeatures(String nf) {
            newFeatures = nf;
        }
    }
    
    public final class FeatureDescriptor {
        
        private String serviceName;
        private String features;
        
        public FeatureDescriptor(String serviceName, String features) {
            this.serviceName = serviceName;
            this.features = features;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public String getFeatures() {
            return features;
        }
    }
    
    public final class OptionDescriptor {
        private String serviceName;
        private boolean[] options;
        
        public OptionDescriptor(String serviceName, boolean[] options) {
            this.serviceName = serviceName;
            this.options = options;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public boolean[] getOptions() {
            return options;
        }
    }
}
