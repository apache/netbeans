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
package org.netbeans.modules.javaee.wildfly.nodes.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.config.WildflyMessageDestination;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Michal Mocnak
 */
public class UndeployModuleCookieImpl implements UndeployModuleCookie {

    private static final RequestProcessor PROCESSOR = new RequestProcessor("JBoss undeploy", 1); // NOI18N

    private final String fileName;

    private final Lookup lookup;

    private final ResourceType type;

    private boolean isRunning;

    public UndeployModuleCookieImpl(String fileName, Lookup lookup) {
        this(fileName, ResourceType.EJB, lookup);
    }

    public UndeployModuleCookieImpl(String fileName, ResourceType type, Lookup lookup) {
        this.lookup = lookup;
        this.fileName = fileName;
        this.type = type;
        this.isRunning = false;
    }

    @Override
    public Task undeploy() {
        final WildflyDeploymentManager dm = (WildflyDeploymentManager) lookup.lookup(WildflyDeploymentManager.class);
        
        final String nameWoExt;
        if(fileName.indexOf('.') > 0) {
            nameWoExt = fileName.substring(0, fileName.lastIndexOf('.'));
        } else {
            nameWoExt = fileName;
        }
        final ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(UndeployModuleCookieImpl.class,
                "LBL_UndeployProgress", nameWoExt));

        Runnable r = new Runnable() {
            public void run() {
                isRunning = true;
                try {
                    switch(type) {
                        case EJB:
                        case EAR:
                        case CAR:
                        case RAR:
                        case WAR:
                            dm.getClient().undeploy(fileName);
                            break;
                        case QUEUE:
                            dm.getClient().removeMessageDestination(new WildflyMessageDestination(fileName, MessageDestination.Type.QUEUE));
                            break;
                        case TOPIC:
                            dm.getClient().removeMessageDestination(new WildflyMessageDestination(fileName, MessageDestination.Type.TOPIC));
                            break;
                        case DATASOURCE:
                            dm.getClient().removeDatasource(fileName);
                            break;
                        
                    }
                } catch (IOException ex) {
                    Logger.getLogger(UndeployModuleCookieImpl.class.getName()).log(Level.INFO, null, ex);
                }
                handle.finish();
                isRunning = false;
            }
        };
        handle.start();
        return PROCESSOR.post(r);
    }

    public boolean isRunning() {
        return isRunning;
    }

}
