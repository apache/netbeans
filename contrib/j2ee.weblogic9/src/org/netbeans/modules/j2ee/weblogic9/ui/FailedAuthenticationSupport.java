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

package org.netbeans.modules.j2ee.weblogic9.ui;

import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public final class FailedAuthenticationSupport {

    /** Last time in ms when the Failed Authentication alert was shown. */
    private static long failedAuthenticationAlertLastTime = 0;

    /** Is Failed Authentication alert shown now? */
    private static boolean failedAuthenticationAlertShown = false;

    /** Timeout within which request to show alert will be ignored. */
    private static int FAILED_AUTHENTICATION_ALERT_TIMEOUT = 1000;

    private FailedAuthenticationSupport() {
        super();
    }

    public static void checkFailedAuthentication(final WLDeploymentManager dm, String line) {
        if (line != null && line.contains("failed to be authenticated")) { // NOI18N
            synchronized (FailedAuthenticationSupport.class) {
                if (failedAuthenticationAlertShown
                        || failedAuthenticationAlertLastTime + FAILED_AUTHENTICATION_ALERT_TIMEOUT > System.currentTimeMillis()
                        || !WLPluginProperties.isFailedAuthenticationReported()) {
                    return;
                }

                failedAuthenticationAlertShown = true;
            }

            Mutex.EVENT.readAccess(new Runnable() {

                @Override
                public void run() {
                    try {
                        FailedAuthenticationAlertPanel alert = new FailedAuthenticationAlertPanel();
                        DialogDescriptor dd = new DialogDescriptor(
                                alert,
                                NbBundle.getMessage(FailedAuthenticationSupport.class, "LBL_Failed_Authentication_Title"),
                                true,
                                DialogDescriptor.YES_NO_OPTION,
                                null,
                                null);
                        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);

                        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.YES_OPTION) {
                            ServerManager.showCustomizer(dm.getUri());
                        }
                    } finally {
                        synchronized (FailedAuthenticationSupport.class) {
                            failedAuthenticationAlertLastTime = System.currentTimeMillis();
                            failedAuthenticationAlertShown = false;
                        }
                    }
                }
            });
        }
    }

}
