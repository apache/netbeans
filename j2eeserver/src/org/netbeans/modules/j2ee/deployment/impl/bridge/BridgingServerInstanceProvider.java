/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Petr Hejl
 */
public class BridgingServerInstanceProvider implements org.netbeans.spi.server.ServerInstanceProvider, InstanceListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final Server server;

    private Map<org.netbeans.modules.j2ee.deployment.impl.ServerInstance, BridgingServerInstance> instances =
            new HashMap<org.netbeans.modules.j2ee.deployment.impl.ServerInstance, BridgingServerInstance>();

    public BridgingServerInstanceProvider(Server server) {
        assert server != null : "Server must not be null"; // NOI18N
        this.server = server;
    }

    public final void addInstanceListener() {
        ServerRegistry.getInstance().addInstanceListener(this);
    }

    public final void removeInstanceListener() {
        ServerRegistry.getInstance().removeInstanceListener(this);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {

    }

    public void instanceAdded(String serverInstanceID) {
        if (server.handlesUri(serverInstanceID)) {
            changeSupport.fireChange();
        }
    }

    public void instanceRemoved(String serverInstanceID) {
        InstanceProperties props = InstanceProperties.getInstanceProperties(serverInstanceID);
        if (server.handlesUri(serverInstanceID) && (props == null || isRegisteredWithUI(props))) {
            changeSupport.fireChange();
        }
    }

    // TODO we could slightly optimize this by cacheing
    public synchronized List<ServerInstance> getInstances() {
        refreshCache();
        List<ServerInstance> instancesList = new  ArrayList<ServerInstance>(instances.size());
        for (BridgingServerInstance instance : instances.values()) {
            instancesList.add(instance.getCommonInstance());
        }
        return instancesList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BridgingServerInstanceProvider other = (BridgingServerInstanceProvider) obj;
        if (this.server != other.server && (this.server == null || !this.server.equals(other.server))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.server != null ? this.server.hashCode() : 0);
        return hash;
    }

    public synchronized ServerInstance getBridge(org.netbeans.modules.j2ee.deployment.impl.ServerInstance instance) {
        refreshCache();
        BridgingServerInstance bridgingInstance = instances.get(instance);
        return bridgingInstance == null ? null : bridgingInstance.getCommonInstance();
    }

    private synchronized void refreshCache() {
        List<org.netbeans.modules.j2ee.deployment.impl.ServerInstance> toRemove = new ArrayList<org.netbeans.modules.j2ee.deployment.impl.ServerInstance>(instances.keySet());

        for (org.netbeans.modules.j2ee.deployment.impl.ServerInstance instance : ServerRegistry.getInstance().getServerInstances()) {
            if (instance.getServer().equals(server) && isRegisteredWithUI(instance.getInstanceProperties())) {
                if (!instances.containsKey(instance)) {
                    instances.put(instance, BridgingServerInstance.createInstance(instance));
                } else {
                    toRemove.remove(instance);
                }
            }
        }

        instances.keySet().removeAll(toRemove);
    }

    private boolean isRegisteredWithUI(InstanceProperties props) {
        String withoutUI = props.getProperty(InstanceProperties.REGISTERED_WITHOUT_UI);
        if (withoutUI == null) {
            return true;
        }
        return !Boolean.valueOf(withoutUI);
    }
}
