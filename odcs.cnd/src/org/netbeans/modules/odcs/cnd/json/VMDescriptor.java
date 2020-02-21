/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
