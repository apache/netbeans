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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
