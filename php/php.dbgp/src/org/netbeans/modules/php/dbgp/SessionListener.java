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
package org.netbeans.modules.php.dbgp;

import java.util.List;

import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.php.dbgp.actions.KillActionProvider;
import org.netbeans.spi.debugger.ActionsProvider;

/**
 * @author ads
 *
 */
public class SessionListener extends DebuggerManagerAdapter {

    @Override
    public void sessionAdded(Session session) {
        if (session.lookupFirst(null, SessionId.class) == null) {
            return;
        }
        SessionProgress progress = SessionProgress.forSession(session);
        progress.start();
        List list = session.lookup(null, ActionsProvider.class);
        boolean found = false;
        for (Object object : list) {
            if (object instanceof KillActionProvider) {
                ((KillActionProvider) object).setEnabled(true);
                found = true;
            }
        }
        assert found;
    }

    @Override
    public void sessionRemoved(Session session) {
        super.sessionRemoved(session);
        SessionProgress progress = SessionProgress.forSession(session);
        progress.finish();
    }

}
