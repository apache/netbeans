/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.status;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.GlassFishStatus;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishStatusCheck.LOCATIONS;
import static org.netbeans.modules.glassfish.tooling.data.GlassFishStatusCheck.VERSION;
import org.netbeans.modules.glassfish.tooling.data.GlassFishStatusTask;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.ui.GlassFishCredentials;
import org.openide.util.NbBundle;

/**
 * Handle authorization failures in administration command calls during server
 * status monitoring.
 * <p/>
 * Will ask user to supply valid username and password for server instance
 * being monitored. For every GlassFish server instance being monitored there
 * must be its own <code>AuthFailureStateListener</code> instance because of
 * pop up window locking. Opening password pop up window may temporary suspend
 * server status checking threads. 
 * <p/>
 * @author Tomas Kraus
 */
public class AuthFailureStateListener extends BasicStateListener {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(AuthFailureStateListener.class);

    /** Minimal delay between displaying pop up windows [ms].
     *  <p/>
     *  Currently it shall not open pop up again sooner than after 
     *  30 seconds. */
    private static final long POPUP_DELAY = 30000;

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Pop up processing lock to avoid displaying pop up window more
     *  than once.
     *  <p/>
     *  Used in double checked pattern so it is <code>volatile</code>.
     */
    private volatile boolean popUpLock;

    /** Timestamp of last pop up window. */
    private long lastTm;

    /** Allow to display pop up window for GF v4. */
    private final boolean allowPopup;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command calls authorization
     * failures handler.
     */
    public AuthFailureStateListener(final boolean allowPopup) {
        super();
        this.popUpLock = false;
        this.lastTm = 0;
        this.allowPopup = allowPopup;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Callback to notify about current server status after every check
     * when enabled.
     * <p/>
     * Current server status notification is not registered.
     * <p/>
     * @param server GlassFish server instance being monitored.
     * @param status Current server status.
     * @param task   Last GlassFish server status check task details.
     */
    @Override
    public void currentState(final GlassFishServer server,
            final GlassFishStatus status, final GlassFishStatusTask task) {
        // Not used yet.
    }

    /**
     * Callback to notify about server status change when enabled.
     * <p/>
     * Listens on <code>STARTUP</code> state changes to clean up pop up window
     * cancel button effect and allow it to display again.
     * <p/>
     * @param server GlassFish server instance being monitored.
     * @param status Current server status.
     * @param task   Last GlassFish server status check task details.
     */    
    @Override
    public void newState(final GlassFishServer server,
            final GlassFishStatus status, final GlassFishStatusTask task) {
        if (popUpLock) {
            synchronized (this) {
                 if (popUpLock) {
                     popUpLock = false;
                 }
            }
        }
    }

    /**
     * Callback to notify about server status check failures.
     * <p/>
     * Handle authorization failures to ask for new username and password.
     * <p/>
     * @param server GlassFish server instance being monitored.
     * @param task   GlassFish server status check task details.
     */
    @Override
    public void error(final GlassFishServer server,
            final GlassFishStatusTask task) {
        switch (task.getType()) {
            case LOCATIONS: case VERSION:
                switch (task.getEvent()) {
                    case AUTH_FAILED_HTTP:
                        GlassFishStatus.suspend(server);
                        break;
                    case AUTH_FAILED:
                        // NetBeans credentiuals pop up window
                        if (allowPopup) {
                            // Double checked pattern on popUpLock
                            // to avoid locking.
                            if (!popUpLock) {
                                updateCredentials(server);
                            }
                        // java.net.Authenticator pop up window
                        } else {
                            GlassFishStatus.suspend(server);
                        }
                        break;
                }
                break;
        }
    }
    
    /**
     * Display pop up window and update GlassFish credentials.
     * <p/>
     * Locks this object instance while GUI pop up window is shown so it may
     * take long time and block other status checking threads on this lock.
     * <p/>
     * @param server GlassFish server instance to update credentials.
     */
    private void updateCredentials(final GlassFishServer server) {
        boolean update = true;
        synchronized (this) {
            if (!popUpLock
                    && lastTm + POPUP_DELAY < System.currentTimeMillis()) {
                popUpLock = true;
                if (server instanceof GlassfishInstance) {
                    try {
                        GlassfishInstance instance = (GlassfishInstance) server;
                        String message = NbBundle.getMessage(
                                AuthFailureStateListener.class,
                                "AuthFailureStateListener.message",
                                instance.getDisplayName());
                        update = GlassFishCredentials
                                .setCredentials(instance, message);
                    } finally {
                        // Cancel will block pop up window until next start.
                        popUpLock = !update;
                        lastTm = System.currentTimeMillis();
                    }
                }
            }
        }
        if (!update) {
            GlassFishStatus.suspend(server);
        }

    }

}
