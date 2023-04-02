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
package org.netbeans.modules.httpserver;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.apache.naming.java.javaURLContextFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

import static org.netbeans.modules.httpserver.Bundle.LBL_NBWebserver;
import static org.netbeans.modules.httpserver.Bundle.LBL_NBWebserver_Active;
import static org.netbeans.modules.httpserver.Bundle.TIP_NBWebserver;
import static org.netbeans.modules.httpserver.Bundle.TIP_NBWebserver_Active;
import static org.netbeans.modules.httpserver.Bundle.ACTION_ServerStart;
import static org.netbeans.modules.httpserver.Bundle.ACTION_ServerStop;

@NbBundle.Messages({
    "LBL_NBWebserver=Internal Webserver (stopped)",
    "LBL_NBWebserver_Active=Internal Webserver (running)",
    "TIP_NBWebserver=Internal Webserver for HTTP services of the IDE.",
    "# {0} - Port the webservice is listening on",
    "TIP_NBWebserver_Active=Internal Webserver for HTTP services of the IDE.\n\nRunning on port {0,number,0}",
    "ACTION_ServerStart=Start",
    "ACTION_ServerStop=Stop"
})
public class ServerControlNode extends AbstractNode {

    static final String NODE_NAME = "nbhttpserver"; // NOI18N
    static final String ICON_BASE = "org/netbeans/modules/httpserver/httpserver.png"; // NOI18N
    static final String ICON_BASE_ACTIVE = "org/netbeans/modules/httpserver/httpserver-active.png"; // NOI18N

    private static final Action ACTION_START = new AbstractAction(ACTION_ServerStart()) {
        @Override
        public void actionPerformed(ActionEvent e) {
            HttpServerSettings.getDefault().setRunning(true);
        }
    };

    private static final Action ACTION_STOP = new AbstractAction(ACTION_ServerStop()) {
        @Override
        public void actionPerformed(ActionEvent e) {
            HttpServerSettings.getDefault().setRunning(false);
        }
    };

    private static final Action[] ACTIONS = {ACTION_START, ACTION_STOP};

    private static final ServerControlNode INSTANCE = new ServerControlNode();

    public static final ServerControlNode getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ServerControlNode() {
        super(Children.LEAF);
        setName(NODE_NAME);
        setDisplayName(LBL_NBWebserver());
        setShortDescription(TIP_NBWebserver());
        setIconBaseWithExtension(ICON_BASE);
        updateNodeState();
    }

    @Override
    public Action[] getActions(boolean context) {
        return Arrays.copyOf(ACTIONS, ACTIONS.length);
    }

    private void updateNodeState0() {
        if (HttpServerSettings.getDefault().isRunning()) {
            setDisplayName(LBL_NBWebserver_Active());
            setShortDescription(TIP_NBWebserver_Active(HttpServerSettings.getDefault().getPort()));
            ACTION_START.setEnabled(false);
            ACTION_STOP.setEnabled(true);
            setIconBaseWithExtension(ICON_BASE_ACTIVE);
        } else {
            setDisplayName(LBL_NBWebserver());
            setShortDescription(TIP_NBWebserver());
            ACTION_START.setEnabled(true);
            ACTION_STOP.setEnabled(false);
            setIconBaseWithExtension(ICON_BASE);
        }
    }

    void updateNodeState() {
        if(SwingUtilities.isEventDispatchThread()) {
            updateNodeState0();
        } else {
            SwingUtilities.invokeLater(() -> updateNodeState0());
        }
    }
}
