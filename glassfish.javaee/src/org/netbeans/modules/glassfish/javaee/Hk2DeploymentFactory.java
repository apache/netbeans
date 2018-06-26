/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.openide.util.NbBundle;


/**
 *
 * @author Ludo
 * @author vince
 */
public class Hk2DeploymentFactory implements DeploymentFactory {

    private static Hk2DeploymentFactory preludeInstance;
    private static Hk2DeploymentFactory ee6Instance;
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

    /**
     *
     * @return
     */
    public static synchronized DeploymentFactory createEe6() {
        // FIXME -- these strings should come from some constant place
        if (ee6Instance == null) {
            ServerUtilities tmp = ServerUtilities.getEe6Utilities();
            ee6Instance = new Hk2DeploymentFactory(new String[]{"deployer:gfv3ee6:", "deployer:gfv3ee6wc:", "deployer:gfv3"}, "0.2", // NOI18N
                    NbBundle.getMessage(Hk2DeploymentFactory.class, "TXT_FactoryDisplayName"));  // NOI18N
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(ee6Instance);
            ee6Instance.setServerUtilities(tmp);
        }
        return ee6Instance;
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
