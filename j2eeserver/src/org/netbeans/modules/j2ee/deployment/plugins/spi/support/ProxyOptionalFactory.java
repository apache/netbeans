/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi.support;

import java.util.Map;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 * Provides a proxying implementation of {@link OptionalDeploymentManagerFactory}.
 * Handles optional <code>noInitializationFinish</code> attribute as a way
 * to prevent delegate's {@link #finishServerInitialization()} to be called.
 * This can be useful in situation when we want to prevent loading unnecessary
 * code (from delegate) being loaded during the {@link #finishServerInitialization()}
 * call. Designed to be used via XML layer filesystem.
 *
 * @author Petr Hejl
 * @since 1.66
 */
public final class ProxyOptionalFactory extends OptionalDeploymentManagerFactory {

    private final Map attributes;

    private final boolean noInitializationFinish;

    /* GuardedBy("this") */
    private OptionalDeploymentManagerFactory delegate;

    private ProxyOptionalFactory(Map attributes) {
        this.attributes = attributes;

        this.noInitializationFinish = Boolean.TRUE.equals(
                attributes.get("noInitializationFinish")); // NOI18N
    }

    public static ProxyOptionalFactory create(Map map) {
        return new ProxyOptionalFactory(map);
    }

    @Override
    public boolean isCommonUIRequired() {
        return getDelegate().isCommonUIRequired();
    }

    @Override
    public TargetModuleIDResolver getTargetModuleIDResolver(DeploymentManager dm) {
        return getDelegate().getTargetModuleIDResolver(dm);
    }

    @Override
    public StartServer getStartServer(DeploymentManager dm) {
        return getDelegate().getStartServer(dm);
    }

    @Override
    public ServerInstanceDescriptor getServerInstanceDescriptor(DeploymentManager dm) {
        return getDelegate().getServerInstanceDescriptor(dm);
    }

    @Override
    public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        return getDelegate().getMessageDestinationDeployment(dm);
    }

    @Override
    public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
        return getDelegate().getJDBCDriverDeployer(dm);
    }

    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return getDelegate().getIncrementalDeployment(dm);
    }

    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return getDelegate().getFindJSPServlet(dm);
    }

    @Override
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return getDelegate().getDatasourceManager(dm);
    }

    @Override
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        return getDelegate().getAntDeploymentProvider(dm);
    }

    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return getDelegate().getAddInstanceIterator();
    }

    @Override
    public void finishServerInitialization() throws ServerInitializationException {
        if (!noInitializationFinish) {
            getDelegate().finishServerInitialization();
        }
    }

    @Override
    public ServerLibraryManager getServerLibraryManager(DeploymentManager dm) {
        return getDelegate().getServerLibraryManager(dm);
    }

    private OptionalDeploymentManagerFactory getDelegate() {
        synchronized (this) {
            if (delegate != null) {
                return delegate;
            }
        }

        OptionalDeploymentManagerFactory factory = (OptionalDeploymentManagerFactory) attributes.get("delegate"); // NOI18N
        assert factory != null : "Delegate is null";
        
        synchronized (this) {
            if (delegate == null) {
                delegate = factory;
            }
            return delegate;
        }
    }

}
