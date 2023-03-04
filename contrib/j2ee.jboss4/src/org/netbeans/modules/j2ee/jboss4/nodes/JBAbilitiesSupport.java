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

package org.netbeans.modules.j2ee.jboss4.nodes;

import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils.Version;
import org.openide.util.Lookup;

/**
 * This class is helper for holding some lazy initialized values that were
 * copy-pasted in several class in previous version of the code. Namely
 * {@link JBEarApplicationsChildren}, {@link JBEjbModulesChildren} and
 * {@link JBWebApplicationsChildren}.
 *
 * @author Petr Hejl
 */
public class JBAbilitiesSupport {

    private final Lookup lookup;

    private Boolean remoteManagementSupported = null;

    private Boolean isJB4x = null;
    
    private Boolean isJB6x = null;

    private Boolean isJB7x= null;
    
    /**
     * Constructs the JBAbilitiesSupport.
     *
     * @param lookup Lookup that will be asked for {@link JBDeploymentManager} if
     * necessary
     */
    public JBAbilitiesSupport(Lookup lookup) {
        assert lookup != null;
        this.lookup = lookup;
    }

    /**
     * Returns true if the JBoss has installed remote management package.
     *
     * @return true if the JBoss has installed remote management package,
     *             false otherwise
     * @see Util.isRemoteManagementSupported(Lookup)
     */
    public boolean isRemoteManagementSupported() {
        if (remoteManagementSupported == null) {
            remoteManagementSupported = Util.isRemoteManagementSupported(lookup);
        }
        return remoteManagementSupported;
    }

    /**
     * Returns true if the version of the JBoss is 4. Check is based on directory
     * layout.
     *
     * @return true if the version of the JBoss is 4, false otherwise
     * @see JBPluginUtils.isGoodJBServerLocation4x(JBDeploymentManager)
     */
    public boolean isJB4x() {
        if (isJB4x == null) {
            JBDeploymentManager dm = lookup.lookup(JBDeploymentManager.class);
            isJB4x = JBPluginUtils.isJB4(dm);
        }
        return isJB4x;
    }
    
    public boolean isJB6x() {
        if (isJB6x == null) {
            JBDeploymentManager dm = lookup.lookup(JBDeploymentManager.class);
            Version version = dm.getProperties().getServerVersion();
            isJB6x = version != null && JBPluginUtils.JBOSS_6_0_0.compareTo(version) <= 0;
        }
        return isJB6x;
    }  
    
    public boolean isJB7x() {
        if (isJB7x == null) {
            JBDeploymentManager dm = lookup.lookup(JBDeploymentManager.class);
            Version version = dm.getProperties().getServerVersion();
            isJB7x = version != null && JBPluginUtils.JBOSS_7_0_0.compareTo(version) <= 0;
        }
        return isJB7x;
    }    
}
