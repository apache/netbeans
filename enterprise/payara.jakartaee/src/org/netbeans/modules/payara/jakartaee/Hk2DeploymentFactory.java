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

package org.netbeans.modules.payara.jakartaee;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.payara.spi.ServerUtilities;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
import org.openide.util.NbBundle;


/**
 *
 * @author Ludo
 * @author vince
 */
public class Hk2DeploymentFactory implements DeploymentFactory {

    private static Hk2DeploymentFactory preludeInstance;
    private static Hk2DeploymentFactory ee6Instance;
    private static Hk2DeploymentFactory ee7Instance;
    private static Hk2DeploymentFactory ee8Instance;
    private String[] uriFragments;
    private String version;
    private String displayName;
    private ServerUtilities su;

    private Hk2DeploymentFactory(String[] uriFragments, String version, String displayName) {
        this.uriFragments = uriFragments;
        this.version = version;
        this.displayName = displayName;
    }

    private void setServerUtilities(ServerUtilities su) {
        this.su = su;
    }

    public static synchronized DeploymentFactory createEe(PayaraVersion version) {
        if (version != null
                && PayaraVersion.ge(version, PayaraVersion.PF_5_181)) {
            return createEe8();
        } else if (version != null
                && PayaraVersion.ge(version, PayaraVersion.PF_4_1_144)) {
            return createEe7();
        }
        return createEe6();
    }
    
    /**
     *
     * @return
     */
    public static synchronized DeploymentFactory createEe6() {
        // FIXME -- these strings should come from some constant place
        if (ee6Instance == null) {
            ServerUtilities tmp = ServerUtilities.getEe6Utilities();
            ee6Instance = new Hk2DeploymentFactory(new String[]{"deployer:pfv3ee6:", "deployer:pfv3"}, "0.2", // NOI18N
                    NbBundle.getMessage(Hk2DeploymentFactory.class, "TXT_FactoryDisplayName"));  // NOI18N
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(ee6Instance);
            ee6Instance.setServerUtilities(tmp);
        }
        return ee6Instance;
    }
    
        /**
     *
     * @return
     */
    public static synchronized DeploymentFactory createEe7() {
        // FIXME -- these strings should come from some constant place
        if (ee7Instance == null) {
            ServerUtilities tmp = ServerUtilities.getEe7Utilities();
            ee7Instance = new Hk2DeploymentFactory(new String[]{"deployer:pfv4ee7:", "deployer:pfv4"}, "0.2", // NOI18N
                    NbBundle.getMessage(Hk2DeploymentFactory.class, "TXT_FactoryDisplayName"));  // NOI18N
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(ee7Instance);
            ee7Instance.setServerUtilities(tmp);
        }
        return ee7Instance;
    }
    
    /**
     *
     * @return
     */
    public static synchronized DeploymentFactory createEe8() {
        // FIXME -- these strings should come from some constant place
        if (ee8Instance == null) {
            ServerUtilities tmp = ServerUtilities.getEe8Utilities();
            ee8Instance = new Hk2DeploymentFactory(new String[]{"deployer:pfv5ee8:", "deployer:pfv5"}, "0.2", // NOI18N
                    NbBundle.getMessage(Hk2DeploymentFactory.class, "TXT_FactoryDisplayName"));  // NOI18N
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(ee8Instance);
            ee8Instance.setServerUtilities(tmp);
        }
        return ee8Instance;
    }

    /**
     * 
     * @param uri 
     * @return 
     */
    // also check the urlPattern in layer.xml when changing this
    @Override
    public boolean handlesURI(String uri) {
        if (uri == null) {
            return false;
        }
        
        if(uri.startsWith("[")) {//NOI18N
            for (String uriFragment : uriFragments) {
                if (uri.indexOf(uriFragment)!=-1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 
     * @param uri 
     * @param uname 
     * @param passwd 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException 
     */
    @Override
    public DeploymentManager getDeploymentManager(String uri, String uname, String passwd) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI:" + uri); // NOI18N
        }
        // prevent registry mismatches
        if (!su.isRegisteredUri(uri)) {
            throw new DeploymentManagerCreationException("Registry mismatch for "+uri);
        }
        return new Hk2DeploymentManager(uri, uname, passwd, su);
    }

    /**
     * 
     * @param uri 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException 
     */
    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI:" + uri); // NOI18N
        }
        // prevent registry mismatches
        if (!su.isRegisteredUri(uri)) {
            throw new DeploymentManagerCreationException("Registry mismatch for "+uri);
        }
        return new Hk2DeploymentManager(uri, null, null, su);
    }

    /**
     * 
     * @return 
     */
    @Override
    public String getProductVersion() {
        return version;
    }

    /**
     * 
     * @return 
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

}
