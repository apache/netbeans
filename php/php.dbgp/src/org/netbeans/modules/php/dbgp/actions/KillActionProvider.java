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
package org.netbeans.modules.php.dbgp.actions;

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * @author ads
 *
 */
public class KillActionProvider extends AbstractActionProvider {

    public KillActionProvider(ContextProvider contextProvider) {
        super(contextProvider);
    }

    @Override
    public void doAction(Object action) {
        Session session = (Session) getContextProvider().lookupFirst(null, Session.class);
        SessionManager.getInstance().stopSession(session);
        setEnabled(false);
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_KILL);
    }

}
