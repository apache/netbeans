/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.nbimpl;

import java.io.File;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.ProfilerModule;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service=Profiler.class)
public class NetBeansProfiler extends org.netbeans.modules.profiler.NetBeansProfiler {
    
    // Emits PROFILING_INACTIVE event to all listeners in case the profiling session
    // is not started/running after [millis], even though this is not a state change.
    // Used to detect & notify that non-observable starting of profiling session failed.
    public void checkAliveAfter(int millis) {
        if (getProfilingState() == PROFILING_INACTIVE) ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
            public void run() {
                if (getProfilingState() == PROFILING_INACTIVE)
                    fireProfilingStateChange(PROFILING_IN_TRANSITION, PROFILING_INACTIVE);
            }
        }, millis);
    }

    @Override
    public String getLibsDir() {
        final File dir = InstalledFileLocator.getDefault()
                                             .locate(ProfilerModule.LIBS_DIR + "/jfluid-server.jar", //NOI18N
                                                     "org.netbeans.lib.profiler", false); //NOI18N

        if (dir == null) {
            return null;
        } else {
            return dir.getParentFile().getPath();
        }
    }    


    @Override
    public boolean rerunAvailable() {
        int state = getProfilingState();
        return (state == Profiler.PROFILING_INACTIVE || state == Profiler.PROFILING_STOPPED) ? ProfilerLauncher.canRelaunch() : false;
    }

    @Override
    public boolean modifyAvailable() {
        return getProfilingState() == Profiler.PROFILING_RUNNING;
    }

    @Override
    public void rerunLastProfiling() {
        ProfilerLauncher.Session s = ProfilerLauncher.getLastSession();
        if (s != null) {
            s.run();
        }
    }
    
}
