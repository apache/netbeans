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

package org.netbeans.modules.cnd.api.remote.ui;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 * Displays Edit Servers List dialog.
 *
 * It was created as a replacement of the show() method
 * of the ServerList class (ServerList is a model, so it shouldn't be mixed with UI)
 *
 */
public abstract class ServerListUI {

    public static boolean showServerListDialog() {
        return getDefault().showServerListDialogImpl();
    }

    public static boolean showServerListDialog(AtomicReference<ExecutionEnvironment> selectedEnv) {
        return getDefault().showServerListDialogImpl(selectedEnv);
    }

    public static boolean showServerRecordPropertiesDialog(ExecutionEnvironment env) {
        return getDefault().showServerRecordPropertiesDialogImpl(env);
    }

    /**
     * Checks whether the record is initialized,
     * if it is not, ask user whether (s)he wants to (re)connect,
     * asks password if needs, connects
     * @param record record to check
     * @return true in the case record is connected (or it wasn't, user agreed, and it's now connected);
     * otherwise false
     */
    public static boolean ensureRecordOnline(ExecutionEnvironment env) {
        return getDefault().ensureRecordOnlineImpl(env);
    }

    /**
     * The same as ensureRecordOnline(ServerRecord record),
     * but allows to specify a message instead of default one
     * @param message message to display in the case the record is not connected
     * @return
     */
    public static boolean ensureRecordOnline(ExecutionEnvironment env, String message) {
        return getDefault().ensureRecordOnlineImpl(env, message);
    }


    private static ServerListUI getDefault() {
        ServerListUI result = Lookup.getDefault().lookup(ServerListUI.class);
        if (result == null) {
            return new Dummy();
        }
        return result;
    }

    /**
     * Displays server list dialog.
     * Allows to add, remove or modify servers in the list
     * @return true in the case user pressed OK, otherwise
     */
    protected abstract boolean showServerListDialogImpl();

    protected abstract boolean showServerListDialogImpl(AtomicReference<ExecutionEnvironment> selectedEnv);

    protected abstract boolean showServerRecordPropertiesDialogImpl(ExecutionEnvironment env);

    protected abstract boolean ensureRecordOnlineImpl(ExecutionEnvironment env, String message);

    protected abstract boolean ensureRecordOnlineImpl(ExecutionEnvironment env);

    private static class Dummy extends ServerListUI {

        private void warning() {
            Logger.getLogger("cnd.remote.logger").log( //NOI18N
                    Level.WARNING, "Can not find {0}", ServerListUI.class.getSimpleName()); //NOI18N
        }

        @Override
        protected boolean showServerListDialogImpl() {
            warning();
            return false;
        }

        @Override
        protected boolean showServerListDialogImpl(AtomicReference<ExecutionEnvironment> selectedEnv) {
            warning();
            return false;
        }

        @Override
        protected boolean showServerRecordPropertiesDialogImpl(ExecutionEnvironment env) {
            warning();
            return false;
        }

        @Override
        protected boolean ensureRecordOnlineImpl(ExecutionEnvironment env, String message) {
            warning();
            return false;
        }

        @Override
        protected boolean ensureRecordOnlineImpl(ExecutionEnvironment env) {
            warning();
            return false;
        }
    }
}
