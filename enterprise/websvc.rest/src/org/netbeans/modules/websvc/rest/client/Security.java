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

package org.netbeans.modules.websvc.rest.client;

import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class Security {

    private boolean ssl;
    private Authentication authentization;
    private SecurityParams securityParams;
    private String projectType; // desktop, nb-project, web
    private FileObject deploymentDescriptor;

    public Security(boolean ssl, Authentication authentization) {
        this.ssl = ssl;
        this.authentization = authentization;
    }

    public Authentication getAuthentication() {
        return authentization;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }
    
    public boolean isSSL() {
        return ssl;
    }

    public SecurityParams getSecurityParams() {
        return securityParams;
    }

    public void setSecurityParams(SecurityParams securityParams) {
        this.securityParams = securityParams;
    }

    public FileObject getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    public void setDeploymentDescriptor(FileObject deploymentDescriptor) {
        this.deploymentDescriptor = deploymentDescriptor;
    }

    public static enum Authentication {
        NONE("auth_none"),
        BASIC("auth_basic"),
        OAUTH("auth_oauth"),
        SESSION_KEY("auth_session_key");

        private String displayName;

        Authentication(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return NbBundle.getMessage(Security.class, displayName);
        }

    }

}
