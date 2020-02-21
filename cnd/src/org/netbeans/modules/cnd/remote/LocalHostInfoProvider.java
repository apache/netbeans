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

package org.netbeans.modules.cnd.remote;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Utilities;

/**
 * HostInfoProvider implementation for local host
 */
/*package-local*/ class LocalHostInfoProvider extends HostInfoProvider {

    private final ExecutionEnvironment execEnv;

    LocalHostInfoProvider(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }

    @Override
    public boolean fileExists(String path) {
        if (new File(path).exists()) {
            return true;
        }
        if (Utilities.isWindows() && !path.endsWith(".lnk")) { //NOI18N
            return new File(path+".lnk").exists(); //NOI18N
        }
        return false;
    }

    @Override
    public Map<String, String> getEnv() {
        Map<String, String> result = null;

        if (HostInfoUtils.isHostInfoAvailable(execEnv)) {
            try {
                result = HostInfoUtils.getHostInfo(execEnv).getEnvironment();
            } catch (IOException ex) {
            } catch (CancellationException ex) {
            }
        }

        if (result == null) {
            result = System.getenv();
        }

        return result;
    }

    @Override
    public String getLibDir() {
        return null;
    }

    @Override
    public PathMap getMapper() {
        return new LocalPathMap();
    }

    private static class LocalPathMap extends PathMap {

        @Override
        public boolean checkRemotePaths(File[] localPaths, boolean fixMissingPath) {
            return false;
        }

        @Override
        public String getLocalPath(String rpath,boolean useDefault) {
            return rpath;
        }

        @Override
        public String getRemotePath(String lpath,boolean useDefault) {
            return lpath;
        }

        @Override
        public String getTrueLocalPath(String rpath) {
            return rpath;
        }
    }
}
