/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
