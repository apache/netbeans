/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettings;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettingsBridge;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.dbx.options.DbxProfile;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;

/**
 *
 *
 */
public final class DbxDebuggerSettings implements DebuggerSettings {

    private final RunProfile runProfile;
    private final DbgProfile dbgProfile;
    private final RtcProfile rtcProfile;

    public DbxDebuggerSettings() {
        // SHOULD these ConfigAuxObjects have a valid based dir?
        // They really are placeholders for a debug session that is not
        // associated a program. In the old days we could only get a profile
        // when we had an executable.
        // Now we're always under some project with some baseDir so perhaps
        // the baseDir SHOULD be passed all the way through DebuggerInfo?
        // It seems that the following temporary HACK suffices in practice.

        final String dummyBaseDir = "<dummyBaseDir>";	// NOI18N

        runProfile = new RunProfile(dummyBaseDir, PlatformTypes.PLATFORM_NONE, null);
        rtcProfile = new RtcProfile(dummyBaseDir);
        dbgProfile = new DbxProfile();
    }

    private DbxDebuggerSettings(RunProfile runProfile, DbgProfile dbgProfile, RtcProfile rtcProfile) {
        this.runProfile = runProfile;
        this.dbgProfile = dbgProfile;
        this.rtcProfile = rtcProfile;
    }

    public RtcProfile rtcProfile() {
        return rtcProfile;
    }

    public DbgProfile dbgProfile() {
        return dbgProfile;
    }

    public RunProfile runProfile() {
        return runProfile;
    }

    public DebuggerSettings clone(Configuration conf) {
        return create(runProfile.clone(conf),
                                (DbgProfile) dbgProfile.clone(conf),
                                (RtcProfile)rtcProfile.clone(conf));
    }

    public void attachBridge(DebuggerSettingsBridge bridge) {
        if (runProfile != null) {
            runProfile.addPropertyChangeListener(bridge);
        }
        if (dbgProfile != null) {
            dbgProfile.setValidatable(true);
        }
        if (rtcProfile != null) {
            rtcProfile.setValidatable(true);
        }
    }

    public void detachBridge(DebuggerSettingsBridge bridge) {
        if (runProfile != null) {
            runProfile.removePropertyChangeListener(bridge);
        }
        if (dbgProfile != null) {
            dbgProfile.setValidatable(false);
        }
        if (rtcProfile != null) {
            rtcProfile.setValidatable(false);
        }

    }

    /*package*/static DbxDebuggerSettings create(RunProfile runProfile, DbgProfile dbgProfile, RtcProfile rtcProfile) {
        return new DbxDebuggerSettings(runProfile, dbgProfile, rtcProfile);
    }
}
