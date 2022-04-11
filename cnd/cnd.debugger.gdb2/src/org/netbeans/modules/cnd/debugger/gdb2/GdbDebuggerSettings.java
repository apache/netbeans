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

package org.netbeans.modules.cnd.debugger.gdb2;

import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettings;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettingsBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.gdb2.options.GdbProfile;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;

/**
 *
 */
public final class GdbDebuggerSettings implements DebuggerSettings {

    private final RunProfile runProfile;
    private final DbgProfile dbgProfile;
    public GdbDebuggerSettings() {
        // SHOULD these ConfigAuxObjects have a valid based dir?
        // They really are placeholders for a debug session that is not
        // associated a program. In the old days we could only get a profile
        // when we had an executable.
        // Now we're always under some project with some baseDir so perhaps
        // the baseDir SHOULD be passed all the way through DebuggerInfo?
        // It seems that the following temporary HACK suffices in practice.

        final String dummyBaseDir = "<dummyBaseDir>";	// NOI18N

        runProfile = new RunProfile(dummyBaseDir, PlatformTypes.PLATFORM_NONE, null);
        dbgProfile = new GdbProfile();
    }

    private GdbDebuggerSettings(RunProfile runProfile, DbgProfile dbgProfile) {
        this.runProfile = runProfile;
        this.dbgProfile = dbgProfile;
    }

    @Override
    public DbgProfile dbgProfile() {
        return dbgProfile;
    }

    @Override
    public RunProfile runProfile() {
        return runProfile;
    }

    @Override
    public DebuggerSettings clone(Configuration conf) {
        return create(runProfile.clone(conf), (DbgProfile) dbgProfile.clone(conf));
    }

    @Override
    public void attachBridge(DebuggerSettingsBridge bridge) {
        if (runProfile != null) {
            runProfile.addPropertyChangeListener(bridge);
        }
        if (dbgProfile != null) {
            dbgProfile.setValidatable(true);
        }
    }

    @Override
    public void detachBridge(DebuggerSettingsBridge bridge) {
        if (runProfile != null) {
            runProfile.removePropertyChangeListener(bridge);
        }
        if (dbgProfile != null) {
            dbgProfile.setValidatable(false);
        }
    }

    /*package*/static GdbDebuggerSettings create(RunProfile newRunProfile, DbgProfile newDbgProfile) {
        return new GdbDebuggerSettings(newRunProfile, newDbgProfile);
    }
}
