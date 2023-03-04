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
package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 *
 * @author Petr Hejl
 */
public class LazyDeploymentManager implements DeploymentManager {

    private final DeploymentManagerProvider provider;

    private javax.enterprise.deploy.spi.DeploymentManager dm;

    public LazyDeploymentManager(DeploymentManagerProvider provider) {
        this.provider = provider;
    }

    public static javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager(String url) {
        ServerRegistry registry = ServerRegistry.getInstance();
        ServerInstance inst = registry.getServerInstance(url);
        return inst.getDeploymentManager();
    }

    @Override
    public ProgressObject undeploy(TargetModuleID[] tmids) throws IllegalStateException {
        return getDeploymentManager().undeploy(tmids);
    }

    @Override
    public ProgressObject stop(TargetModuleID[] tmids) throws IllegalStateException {
        return getDeploymentManager().stop(tmids);
    }

    @Override
    public ProgressObject start(TargetModuleID[] tmids) throws IllegalStateException {
        return getDeploymentManager().start(tmids);
    }

    @Override
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        getDeploymentManager().setLocale(locale);
    }

    @Override
    public void setDConfigBeanVersion(DConfigBeanVersionType dcbvt) throws DConfigBeanVersionUnsupportedException {
        getDeploymentManager().setDConfigBeanVersion(dcbvt);
    }

    @Override
    public void release() {
        getDeploymentManager().release();
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, InputStream in, InputStream in1) throws UnsupportedOperationException, IllegalStateException {
        return getDeploymentManager().redeploy(tmids, in, in1);
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, File file, File file1) throws UnsupportedOperationException, IllegalStateException {
        return getDeploymentManager().redeploy(tmids, file, file1);
    }

    @Override
    public boolean isRedeploySupported() {
        return getDeploymentManager().isRedeploySupported();
    }

    @Override
    public boolean isLocaleSupported(Locale locale) {
        return getDeploymentManager().isLocaleSupported(locale);
    }

    @Override
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dcbvt) {
        return getDeploymentManager().isDConfigBeanVersionSupported(dcbvt);
    }

    @Override
    public Target[] getTargets() throws IllegalStateException {
        return getDeploymentManager().getTargets();
    }

    @Override
    public Locale[] getSupportedLocales() {
        return getDeploymentManager().getSupportedLocales();
    }

    @Override
    public TargetModuleID[] getRunningModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        return getDeploymentManager().getRunningModules(mt, targets);
    }

    @Override
    public TargetModuleID[] getNonRunningModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        return getDeploymentManager().getNonRunningModules(mt, targets);
    }

    @Override
    public Locale getDefaultLocale() {
        return getDeploymentManager().getDefaultLocale();
    }

    @Override
    public DConfigBeanVersionType getDConfigBeanVersion() {
        return getDeploymentManager().getDConfigBeanVersion();
    }

    @Override
    public Locale getCurrentLocale() {
        return getDeploymentManager().getCurrentLocale();
    }

    @Override
    public TargetModuleID[] getAvailableModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        return getDeploymentManager().getAvailableModules(mt, targets);
    }

    @Override
    public ProgressObject distribute(Target[] targets, ModuleType mt, InputStream in, InputStream in1) throws IllegalStateException {
        return getDeploymentManager().distribute(targets, mt, in, in1);
    }

    @Override
    public ProgressObject distribute(Target[] targets, InputStream in, InputStream in1) throws IllegalStateException {
        return getDeploymentManager().distribute(targets, in, in1);
    }

    @Override
    public ProgressObject distribute(Target[] targets, File file, File file1) throws IllegalStateException {
        return getDeploymentManager().distribute(targets, file, file1);
    }

    @Override
    public DeploymentConfiguration createConfiguration(DeployableObject d) throws InvalidModuleException {
        return getDeploymentManager().createConfiguration(d);
    }

    private javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager() {
        synchronized (this) {
            if (dm != null) {
                return dm;
            }
            dm = provider.getDeploymentManager();
            return dm;
        }
    }

    public static interface DeploymentManagerProvider {

        DeploymentManager getDeploymentManager();

    }
}
