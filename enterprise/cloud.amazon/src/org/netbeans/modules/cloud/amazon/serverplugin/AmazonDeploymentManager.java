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
package org.netbeans.modules.cloud.amazon.serverplugin;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.Future;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.DeploymentStatus;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.ProgressObjectImpl;
import org.netbeans.modules.cloud.common.spi.support.serverplugin.TargetImpl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2;
import org.openide.util.NbBundle;

/**
 *
 */
public class AmazonDeploymentManager implements DeploymentManager2 {

    private String appName;
    private String envID;
    private String keyId;
    private String key;
    private String containerType;
    private String regionUrl;
    private String regionCode;

    public AmazonDeploymentManager(String appName, String envID, String keyId, String key, String containerType, String regionUrl, String regionCode) {
        this.appName = appName;
        this.envID = envID;
        this.keyId = keyId;
        this.key = key;
        this.containerType = containerType;
        this.regionUrl = regionUrl;
        this.regionCode = regionCode;
    }
    
    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, DeploymentContext deployment) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public ProgressObject distribute(Target[] targets, DeploymentContext deployment) {
        File f = deployment.getModuleFile();
        ProgressObjectImpl po = new ProgressObjectImpl(NbBundle.getMessage(AmazonDeploymentManager.class, "AmazonDeploymentManager.distributing"), false);
        AmazonInstance.deployAsync(f, appName, envID, keyId, key, po, regionCode);
        return po;
    }

    @Override
    public Target[] getTargets() throws IllegalStateException {
        return new Target[]{TargetImpl.SOME};
    }

    @Override
    public TargetModuleID[] getRunningModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public TargetModuleID[] getNonRunningModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public TargetModuleID[] getAvailableModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        return new TargetModuleID[0];
    }

    @Override
    public DeploymentConfiguration createConfiguration(DeployableObject d) throws InvalidModuleException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public ProgressObject distribute(Target[] targets, File file, File file1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    @Deprecated
    public ProgressObject distribute(Target[] targets, InputStream in, InputStream in1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public ProgressObject distribute(Target[] targets, ModuleType mt, InputStream in, InputStream in1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public ProgressObject start(TargetModuleID[] tmids) throws IllegalStateException {
        return new ProgressObjectImpl("", true); // NOI18N
    }

    @Override
    public ProgressObject stop(TargetModuleID[] tmids) throws IllegalStateException {
        return new ProgressObjectImpl("", true); // NOI18N
    }

    @Override
    public ProgressObject undeploy(TargetModuleID[] tmids) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public boolean isRedeploySupported() {
        return true;
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, File file, File file1) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, InputStream in, InputStream in1) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void release() {
    }

    @Override
    public Locale getDefaultLocale() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Locale getCurrentLocale() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Locale[] getSupportedLocales() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public boolean isLocaleSupported(Locale locale) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public DConfigBeanVersionType getDConfigBeanVersion() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dcbvt) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void setDConfigBeanVersion(DConfigBeanVersionType dcbvt) throws DConfigBeanVersionUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getContainerType() {
        return containerType;
    }
    
}
