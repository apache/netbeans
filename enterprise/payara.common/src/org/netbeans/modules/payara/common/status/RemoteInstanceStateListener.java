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
package org.netbeans.modules.payara.common.status;

import org.netbeans.modules.payara.tooling.PayaraStatus;
import static org.netbeans.modules.payara.tooling.data.PayaraStatusCheck.VERSION;
import org.netbeans.modules.payara.tooling.TaskEvent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraStatusTask;

/**
 * Handle version mismatch and connection/URL failures in administration command
 * calls during server status monitoring.
 * <p/>
 * Displays a warning notification to the user in three cases:
 * <ul>
 *   <li>The version reported by a remote Payara server differs from the
 *       locally registered installation ({@link TaskEvent#VERSION_MISMATCH}).</li>
 *   <li>A connection failure occurred reaching the administration interface —
 *       typically caused by a wrong host name or port number configured in the
 *       server registration ({@link TaskEvent#CONNECTION_FAILED}).</li>
 *   <li>An exception was thrown while constructing the administration command —
 *       the configured host/port values may be malformed
 *       ({@link TaskEvent#CMD_EXCEPTION}).</li>
 * </ul>
 * <p/>
 * For every Payara server instance being monitored there must be its own
 * {@code RemoteInstanceStateListener} instance to avoid duplicate popups.
 * <p/>
 * @author Gaurav Gupta
 */
public class RemoteInstanceStateListener extends BasicStateListener {

    // Class attributes                                                       //
    /** Minimal delay between displaying warning popups [ms].
     *  <p/>
     *  Currently it shall not open a popup again sooner than after 60 seconds
     *  to avoid spamming the user during periodic status checks. */
    private static final long POPUP_DELAY = 60000;

    // Instance attributes                                                    //
    /** Timestamp of last popup window. */
    private volatile long lastTm;

    // Constructors                                                           //
    /**
     * Constructs an instance of the remote instance state notification handler.
     */
    public RemoteInstanceStateListener() {
        super();
        this.lastTm = 0;
    }

    // Methods                                                                //
    /**
     * Callback to notify about current server status after every check
     * when enabled.
     * <p/>
     * Not used.
     * <p/>
     * @param server Payara server instance being monitored.
     * @param status Current server status.
     * @param task   Last Payara server status check task details.
     */
    @Override
    public void currentState(final PayaraServer server,
            final PayaraStatus status, final PayaraStatusTask task) {
        // Not used.
    }

    /**
     * Callback to notify about server status change when enabled.
     * <p/>
     * Not used.
     * <p/>
     * @param server Payara server instance being monitored.
     * @param status Current server status.
     * @param task   Last Payara server status check task details.
     */
    @Override
    public void newState(final PayaraServer server,
            final PayaraStatus status, final PayaraStatusTask task) {
        // Not used.
    }

    /**
     * Callback to notify about server status check failures.
     * <p/>
     * Shows a non-blocking warning popup on the VERSION check for:
     * <ul>
     *   <li>{@link TaskEvent#VERSION_MISMATCH} — server version differs from
     *       the locally registered installation.</li>
     *   <li>{@link TaskEvent#CONNECTION_FAILED} — connection failed; likely caused by
     *       a wrong host name or administration port.</li>
     *   <li>{@link TaskEvent#CMD_EXCEPTION} — an exception was thrown while
     *       constructing the administration command; host/port may be malformed.</li>
     * </ul>
     * <p/>
     * @param server Payara server instance being monitored.
     * @param task   Payara server status check task details.
     */
    @Override
    public void error(final PayaraServer server,
            final PayaraStatusTask task) {
        if (task.getType() != VERSION) {
            return;
        }
        TaskEvent event = task.getEvent();
        if (event == TaskEvent.VERSION_MISMATCH) {
            showWarningNotification(server, "RemoteInstanceStateListener.versionMismatch",
                    server.getName());
        } else if (event == TaskEvent.CONNECTION_FAILED) {
            // Connection failed — wrong host or port configured for the server.
            showWarningNotification(server, "RemoteInstanceStateListener.connectionFailed",
                    server.getName(), server.getHost(),
                    Integer.toString(server.getAdminPort()));
        } else if (event == TaskEvent.CMD_EXCEPTION) {
            // constructCommandUrl() threw a CommandException — host/port
            // value may be malformed.
            showWarningNotification(server, "RemoteInstanceStateListener.commandException",
                    server.getName(), server.getHost(),
                    Integer.toString(server.getAdminPort()));
        }
    }

    /**
     * Shows a non-blocking warning notification at most once per
     * {@link #POPUP_DELAY} milliseconds.
     * <p/>
     * @param server     Payara server instance (unused, for future use).
     * @param bundleKey  NbBundle key for the message text.
     * @param args       Arguments to the bundle message.
     */
    private void showWarningNotification(final PayaraServer server,
            final String bundleKey, final Object... args) {
        long now = System.currentTimeMillis();
        synchronized (this) {
            if (now - lastTm < POPUP_DELAY) {
                return;
            }
            lastTm = now;
        }
        String message = NbBundle.getMessage(
                RemoteInstanceStateListener.class, bundleKey, args);
        NotifyDescriptor nd = new NotifyDescriptor.Message(
                message, NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(nd);
    }

}
