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

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class MemoryInstancePropertiesImpl extends DeletableInstanceProperties implements InstanceListener {

    private Map<String, String> properties = new HashMap<String, String>();

    private final String url;

    public MemoryInstancePropertiesImpl(ServerInstance instance) {
        this(instance.getUrl());
    }

    public MemoryInstancePropertiesImpl(String url) {
        this.url = url;
    }

    @Override
    public DeploymentManager getDeploymentManager() {
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled) {
            return new LazyDeploymentManager(new LazyDeploymentManager.DeploymentManagerProvider() {

                @Override
                public DeploymentManager getDeploymentManager() {
                    return LazyDeploymentManager.getDeploymentManager(url);
                }
            });
        }
        return LazyDeploymentManager.getDeploymentManager(url);
    }

    @Override
    public String getProperty(String propname) throws IllegalStateException {
        synchronized (this) {
            return getProperties().get(propname);
        }
    }

    @Override
    public Enumeration propertyNames() throws IllegalStateException {
        synchronized (this) {
            return Collections.enumeration(new HashSet<String>(getProperties().keySet()));
        }
    }

    @Override
    public void refreshServerInstance() {
        ServerRegistry registry = ServerRegistry.getInstance();
        ServerInstance inst = registry.getServerInstance(url);
        if (inst != null) {
            inst.refresh();
        }
    }

    @Override
    public void setProperties(Properties props) throws IllegalStateException {
        getProperties(); // eager check we can manipulati it

        java.util.Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            String propValue = props.getProperty(propName);
            setProperty(propName, propValue);
        }
    }

    @Override
    public void setProperty(String propname, String value) throws IllegalStateException {
        String oldValue = null;
        synchronized (this) {
            oldValue = getProperties().put(propname, value);
        }
        firePropertyChange(new PropertyChangeEvent(this, propname, oldValue, value));
    }

    @Override
    public void instanceAdded(String serverInstanceID) {
        // noop
    }

    @Override
    public void instanceRemoved(String serverInstanceID) {
        if (serverInstanceID != null && url.equals(serverInstanceID)) {
            // we are just defensive
            synchronized (this) {
                properties = null;
            }
        }
    }

    @Override
    boolean isDeleted() {
        synchronized (this) {
            return properties == null;
        }
    }

    private synchronized Map<String, String> getProperties() {
        if (properties == null) {
            throw new IllegalStateException(
                (NbBundle.getMessage(MemoryInstancePropertiesImpl.class, "MSG_InstanceNotExists", url)));
        }
        return properties;
    }
}
