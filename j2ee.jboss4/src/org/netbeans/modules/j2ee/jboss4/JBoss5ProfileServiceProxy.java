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

package org.netbeans.modules.j2ee.jboss4;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper around JBoss ProfileService providing the methods we need.
 *
 * @author Petr Hejl
 */
public final class JBoss5ProfileServiceProxy {

    private static final Logger LOGGER = Logger.getLogger(JBoss5ProfileServiceProxy.class.getName());

    private final Object profileService;

    public JBoss5ProfileServiceProxy(Object profileService) {
        this.profileService = profileService;
    }

    public boolean startAndWait(String appName, long timeout) throws Exception {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(profileService.getClass().getClassLoader());
            Method getDeploymentManager = profileService.getClass().getDeclaredMethod(
                    "getDeploymentManager"); // NOI18N
            Object deploymentManager = getDeploymentManager.invoke(profileService, new Object[] {});
            if (deploymentManager != null) {
                Method start = deploymentManager.getClass().getDeclaredMethod("start", String[].class); // NOI18N
                Object progress = start.invoke(deploymentManager, new Object[] {new String[] {appName}});
                Method run = progress.getClass().getDeclaredMethod("run"); // NOI18N
                run.invoke(progress);
                Method getDeploymentStatus = progress.getClass().getDeclaredMethod("getDeploymentStatus"); // NOI18N
                boolean done = false;
                long startTime = System.nanoTime();
                while (!done || ((System.nanoTime() - startTime) / 1000000) > timeout) {
                    Object deploymentStatus = getDeploymentStatus.invoke(progress);
                    Method isRunning = deploymentStatus.getClass().getDeclaredMethod("isRunning"); // NOI18N
                    Boolean val = (Boolean) isRunning.invoke(deploymentStatus);
                    done = !val.booleanValue();
                    if (!done) {
                        Thread.sleep(100);
                    }
                }
                return done;
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
        return false;
    }

    public boolean isReady(String appName) throws Exception {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(profileService.getClass().getClassLoader());
            Method getViewManager = profileService.getClass().getDeclaredMethod(
                    "getViewManager"); // NOI18N
            Object viewManager = getViewManager.invoke(profileService, new Object[] {});
            if (viewManager != null) {
                Method load = viewManager.getClass().getDeclaredMethod("load");
                load.invoke(viewManager);
                Method getDeployment = viewManager.getClass().getDeclaredMethod("getDeployment", String.class); // NOI18N
                Object deployment = getDeployment.invoke(viewManager, new Object[] {appName});
                Method getDeploymentState = deployment.getClass().getDeclaredMethod("getDeploymentState"); // NOI18N
                Object deploymentState = getDeploymentState.invoke(deployment);
                return deploymentState.equals(deploymentState.getClass().getDeclaredField("STARTED").get(deployment));
            }
        } catch (Exception ex) {
            if (ex.getClass().getName().equals("org.jboss.profileservice.spi.NoSuchDeploymentException")) { // NOI18N
                LOGGER.log(Level.FINE, null, ex);
            } else {
                throw ex;
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
        return false;
    }
}
