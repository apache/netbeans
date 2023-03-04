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
