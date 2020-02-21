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
package org.netbeans.modules.odcs.cnd.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * POJO for Jackson
 *
 */
public final class VMDescriptor {

    private Long serverLocalTime;
    private String displayName;
    private Long startRequestedTime;
    private Long proxyPort;
    private String hostname;
    private String activeOperation;
    private String host;
    private String proxyHostname;
    private String state;
    private String stateDetail;
    private Long elapsedTimeSinceStorageStarted;
    private String image;
    private Long elapsedTimeSinceInstanceStarted;
    private String orgIdentifier;
    private String shape;
    private List<Software> softwares = null;
    private Long instanceStoppedTime;
    private Long destroyRequestedTime;
    private String proxyHost;
    private Long serviceStartedTime;
    private Long elapsedTimeSinceStartRequested;
    private Long storageStartedTime;
    private String machineId;
    private Long port;
    private Long storageVolumeSize;
    private Long instanceStartedTime;
    private Long elapsedTimeSinceStopRequested;
    private Long elapsedTimeSinceDestroyRequested;
    private String accessKey;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Long getServerLocalTime() {
        return serverLocalTime;
    }

    public void setServerLocalTime(Long serverLocalTime) {
        this.serverLocalTime = serverLocalTime;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getStartRequestedTime() {
        return startRequestedTime;
    }

    public void setStartRequestedTime(Long startRequestedTime) {
        this.startRequestedTime = startRequestedTime;
    }

    public Long getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Long proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getActiveOperation() {
        return activeOperation;
    }

    public void setActiveOperation(String activeOperation) {
        this.activeOperation = activeOperation;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProxyHostname() {
        return proxyHostname;
    }

    public void setProxyHostname(String proxyHostname) {
        this.proxyHostname = proxyHostname;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateDetail() {
        return stateDetail;
    }

    public void setStateDetail(String stateDetail) {
        this.stateDetail = stateDetail;
    }

    public Long getElapsedTimeSinceStorageStarted() {
        return elapsedTimeSinceStorageStarted;
    }

    public void setElapsedTimeSinceStorageStarted(Long elapsedTimeSinceStorageStarted) {
        this.elapsedTimeSinceStorageStarted = elapsedTimeSinceStorageStarted;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getElapsedTimeSinceInstanceStarted() {
        return elapsedTimeSinceInstanceStarted;
    }

    public void setElapsedTimeSinceInstanceStarted(Long elapsedTimeSinceInstanceStarted) {
        this.elapsedTimeSinceInstanceStarted = elapsedTimeSinceInstanceStarted;
    }

    public String getOrgIdentifier() {
        return orgIdentifier;
    }

    public void setOrgIdentifier(String orgIdentifier) {
        this.orgIdentifier = orgIdentifier;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public List<Software> getSoftwares() {
        return softwares;
    }

    public void setSoftwares(List<Software> softwares) {
        this.softwares = softwares;
    }

    public Long getInstanceStoppedTime() {
        return instanceStoppedTime;
    }

    public void setInstanceStoppedTime(Long instanceStoppedTime) {
        this.instanceStoppedTime = instanceStoppedTime;
    }

    public Long getDestroyRequestedTime() {
        return destroyRequestedTime;
    }

    public void setDestroyRequestedTime(Long destroyRequestedTime) {
        this.destroyRequestedTime = destroyRequestedTime;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Long getServiceStartedTime() {
        return serviceStartedTime;
    }

    public void setServiceStartedTime(Long serviceStartedTime) {
        this.serviceStartedTime = serviceStartedTime;
    }

    public Long getElapsedTimeSinceStartRequested() {
        return elapsedTimeSinceStartRequested;
    }

    public void setElapsedTimeSinceStartRequested(Long elapsedTimeSinceStartRequested) {
        this.elapsedTimeSinceStartRequested = elapsedTimeSinceStartRequested;
    }

    public Long getStorageStartedTime() {
        return storageStartedTime;
    }

    public void setStorageStartedTime(Long storageStartedTime) {
        this.storageStartedTime = storageStartedTime;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public Long getStorageVolumeSize() {
        return storageVolumeSize;
    }

    public void setStorageVolumeSize(Long storageVolumeSize) {
        this.storageVolumeSize = storageVolumeSize;
    }

    public Long getInstanceStartedTime() {
        return instanceStartedTime;
    }

    public void setInstanceStartedTime(Long instanceStartedTime) {
        this.instanceStartedTime = instanceStartedTime;
    }

    public Long getElapsedTimeSinceStopRequested() {
        return elapsedTimeSinceStopRequested;
    }

    public void setElapsedTimeSinceStopRequested(Long elapsedTimeSinceStopRequested) {
        this.elapsedTimeSinceStopRequested = elapsedTimeSinceStopRequested;
    }

    public Long getElapsedTimeSinceDestroyRequested() {
        return elapsedTimeSinceDestroyRequested;
    }

    public void setElapsedTimeSinceDestroyRequested(Long elapsedTimeSinceDestroyRequested) {
        this.elapsedTimeSinceDestroyRequested = elapsedTimeSinceDestroyRequested;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
