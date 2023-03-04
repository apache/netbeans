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
package org.netbeans.modules.cloud.amazon.serverplugin;

import com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.InstanceState;

/**
 *
 */
public class AmazonJ2EEInstance {
   
    private AmazonInstance amazonInstance;
    private String applicationName;
    private String environmentName;
    private String environmentId;
    private InstanceState state;
    private String containerType;
    private ServerInstance instance;

    public AmazonJ2EEInstance(AmazonInstance amazonInstance, String applicationName, 
            String environmentName, String environmentId, String containerType) {
        this.amazonInstance = amazonInstance;
        this.applicationName = applicationName;
        this.environmentName = environmentName;
        this.environmentId = environmentId;
        this.state = InstanceState.READY;
        this.containerType = containerType;
    }

    public AmazonInstance getAmazonInstance() {
        return amazonInstance;
    }

    public void updateState(String stateDesc) {
        switch (EnvironmentStatus.valueOf(stateDesc)) {
            case Launching:
                state = InstanceState.LAUNCHING;
                break;
            case Ready:
                state = InstanceState.READY;
                break;
            case Terminated:
                state = InstanceState.TERMINATED;
                break;
            case Terminating:
                state = InstanceState.TERMINATING;
                break;
            case Updating:
                state = InstanceState.UPDATING;
                break;
        }
    }
    
    public ServerInstance getInstance() {
        return instance;
    }

    void setInstance(ServerInstance instance) {
        this.instance = instance;
    }

    public InstanceState getState() {
        return state;
    }
    
    public void setAmazonInstance(AmazonInstance amazonInstance) {
        this.amazonInstance = amazonInstance;
    }

    public String getId() {
        return createURL(getApplicationName(), getEnvironmentId(), getContainerType());
    }
    
    public static String createURL(String appName, String envID, String container) {
        return AmazonDeploymentFactory.AMAZON_URI+appName + "-" +envID+"-"+container;
    }
    
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public String getEnvironmentId() {
        return environmentId;
    }
    
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getContainerType() {
        return containerType;
    }

    @Override
    public String toString() {
        return "AmazonJ2EEInstance{" + "amazonInstance=" + amazonInstance + ", applicationName=" + applicationName + ", environmentName=" + environmentName + ", environmentId=" + environmentId + '}';
    }

    public String getDisplayName() {
        return getEnvironmentName()+"/"+getApplicationName()+" on "+getAmazonInstance().getName();
    }
    
    
}
