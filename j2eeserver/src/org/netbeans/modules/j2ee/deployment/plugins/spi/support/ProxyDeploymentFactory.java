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
import java.util.regex.Pattern;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.util.Parameters;

/**
 * Provides a proxying implementation of {@link DeploymentFactory}. Handles
 * optional <code>urlPattern</code> attribute as precheck for url the plugin
 * can handle. Designed to be used via XML layer filesystem.
 * <p>
 * The <code>urlPattern</code> attribute is expected to be regexp string.
 * If it is present the server instance uri must match the regexp otherwise
 * {@link #handlesURI(java.lang.String)} will automatically return false
 * (before querying the delegate).
 *
 * @author Petr Hejl
 * @since 1.65
 */
public final class ProxyDeploymentFactory implements DeploymentFactory {

    private final Map attributes;

    private final Pattern urlPattern;

    private DeploymentFactory delegate;

    private ProxyDeploymentFactory(Map attributes) {
        this.attributes = attributes;

        String pattern = (String) attributes.get("urlPattern"); // NOI18N
        if (pattern != null) {
            urlPattern = Pattern.compile(pattern);
        } else {
            urlPattern = null;
        }
    }

    public static ProxyDeploymentFactory create(Map map) {
        return new ProxyDeploymentFactory(map);
    }

    @Override
    public boolean handlesURI(String string) {
        if (string == null) {
            return false;
        }
        if (urlPattern != null && !urlPattern.matcher(string).matches()) {
            return false;
        }
        return getDelegate().handlesURI(string);
    }

    @Override
    public String getProductVersion() {
        return getDelegate().getProductVersion();
    }

    @Override
    public String getDisplayName() {
        return getDelegate().getDisplayName();
    }

    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String string) throws DeploymentManagerCreationException {
        return getDelegate().getDisconnectedDeploymentManager(string);
    }

    @Override
    public DeploymentManager getDeploymentManager(String string, String string1, String string2) throws DeploymentManagerCreationException {
        return getDelegate().getDeploymentManager(string, string1, string2);
    }

    private DeploymentFactory getDelegate() {
        synchronized (this) {
            if (delegate != null) {
                return delegate;
            }
        }

        DeploymentFactory factory = (DeploymentFactory) attributes.get("delegate"); // NOI18N
        Parameters.notNull("delegate", factory); // NOI18N

        synchronized (this) {
            if (delegate == null) {
                delegate = factory;
            }
            return delegate;
        }
    }
}
