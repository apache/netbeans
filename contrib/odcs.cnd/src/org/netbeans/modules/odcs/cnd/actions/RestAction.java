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
package org.netbeans.modules.odcs.cnd.actions;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapter;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapterFactory;
import org.openide.util.RequestProcessor;

/**
 *
 */
public abstract class RestAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(RestAction.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("REST request to Oracle Cloud", 3); // NOI18N

    private final String serverUrl;

    public RestAction(String serverUrl, String name) {
        super(name);
        this.serverUrl = serverUrl;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RP.submit(() -> {
            try {
                actionPerformedImpl(HttpClientAdapterFactory.get(serverUrl), e);
            } catch (Exception ex) {
                LOG.log(Level.FINE, "Exception in REST request", ex);
            }
        });
    }

    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Will be invoked not from EDT
     */
    public abstract void actionPerformedImpl(HttpClientAdapter client, ActionEvent e);

    public abstract String getRestUrl();

    protected String formatUrl(String template, String... params) {
        String result = template;

        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            result = result.replace("{" + i + "}", param); // NOI18N
        }

        return result;
    }
}
