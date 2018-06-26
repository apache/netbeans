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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.tomcat5;

import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sherold
 */
public class AntDeploymentProviderImpl implements AntDeploymentProvider {
    
    private final TomcatManager tm;
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.tomcat5"); // NOI18N
    
    public AntDeploymentProviderImpl(DeploymentManager dm) {
        tm = (TomcatManager)dm;
    }

    public void writeDeploymentScript(OutputStream os, Object moduleType) throws IOException {
        String name = null;
        switch (tm.getTomcatVersion()) {
            case TOMCAT_70:
                name = "resources/tomcat-ant-deploy70.xml";
                break;
            case TOMCAT_60:
                name = "resources/tomcat-ant-deploy60.xml";
                break;
            case TOMCAT_55:
            case TOMCAT_50:
            default:
                name = "resources/tomcat-ant-deploy.xml";
        }
        
        InputStream is = AntDeploymentProviderImpl.class.getResourceAsStream(name); // NOI18N
        if (is == null) {
            // this should never happen, but better make sure
            LOGGER.log(Level.SEVERE, "Missing resource {0}.", name); // NOI18N
            return;
        }
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
        }
    }

    public File getDeploymentPropertiesFile() {
        TomcatProperties tp = tm.getTomcatProperties();
        File file = tp.getAntDeploymentPropertiesFile();
        if (!file.exists()) {
            // generate the deployment properties file only if it does not exist
            try {
                tp.storeAntDeploymentProperties(file, true);
            } catch (IOException ioe) {
                Logger.getLogger(AntDeploymentProviderImpl.class.getName()).log(Level.INFO, null, ioe);
            }
        }
        return file;
    }
}
