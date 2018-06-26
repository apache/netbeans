/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.j2ee.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.openide.util.NbBundle;

/**
 * A simple registry-like access point for server-defined JavaPlatform
 * @author Jaroslav Bachorik
 */
public class ServerJavaPlatform {
    private static final String KEY_PLATFORM = "platform.ant.name"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(ServerJavaPlatform.class.getName());
    
    @NbBundle.Messages({
        "# {0} - the server instance id",
        "MSG_MissingServerJavaPlatform=Missing JavaPlatform for server {0}.\nUsing the IDE default platform instead."
    })
    /**
     * @since 1.20
     */
    public static @NonNull JavaPlatform getPlatform(String serverInstanceId) {
        ServerInstance si = Deployment.getDefault().getServerInstance(serverInstanceId);

        JavaPlatform newjp = null;
        try {
            if (si != null) {
                J2eePlatform jeep = si.getJ2eePlatform();
                if (jeep != null) {
                    org.netbeans.api.java.platform.JavaPlatform pjp = jeep.getJavaPlatform();
                    newjp = pjp != null ? JavaPlatform.getJavaPlatformById(pjp.getProperties().get(KEY_PLATFORM)) : null;
                }
            }
        } catch (InstanceRemovedException e) {
        }
        
        if (newjp == null) {
            LOG.log(Level.INFO, Bundle.MSG_MissingServerJavaPlatform(serverInstanceId));
            newjp = JavaPlatform.getDefaultPlatform();
        }
        
        return newjp != null ? newjp : JavaPlatform.getDefaultPlatform();
    }

}
