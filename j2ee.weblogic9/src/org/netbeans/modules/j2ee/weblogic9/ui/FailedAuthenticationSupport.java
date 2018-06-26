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
