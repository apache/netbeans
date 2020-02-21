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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.spi.remote.setup.support.RemoteSyncNotifier;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileSystem;

/**
 *
 */
/*package-local*/ class SharedSyncWorker implements RemoteSyncWorker {

    private final File[] files;
    private final FSPath[] fsPaths;
    private final FileSystem fileSystem;
    private final String workingDir;
    private final ExecutionEnvironment executionEnvironment;

    public SharedSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, 
            String workingDir, List<FSPath> paths, List<FSPath> buildResults) {
        this.fileSystem = SyncUtils.getSingleFileSystem(paths);
        this.fsPaths = paths.toArray(new FSPath[paths.size()]);
        this.files = SyncUtils.toFiles(this.fsPaths);
        this.executionEnvironment = executionEnvironment;
        this.workingDir = workingDir;
    }
    
    @Override
    public boolean startup(Map<String, String> env2add) {

        if (SyncUtils.isDoubleRemote(executionEnvironment, fileSystem)) {
            RemoteSyncNotifier.getInstance().warnDoubleRemote(executionEnvironment, fileSystem);
            return false;
        }

        PathMap mapper = HostInfoProvider.getMapper(executionEnvironment);
        if (files != null && files.length > 0) {
            File[] filesToCheck;
            if (workingDir == null || (!new File(workingDir).exists())) {
                filesToCheck = files;
            } else {
                filesToCheck = new File[files.length + 1];
                System.arraycopy(files, 0, filesToCheck, 0, files.length);
                filesToCheck[files.length] = new File(workingDir);
            }
            // or is filtering inexistent paths a responsibiity of path mapper?
            // it seems it's rather not since it's too common
            return mapper.checkRemotePaths(filterInexistent(filesToCheck), true);
        }
        return true;
    }

    private static File[] filterInexistent(File[] files) {
        boolean inexistentFound = false;
        for (File file : files) {
            if (!file.exists()) {
                inexistentFound = true;
                break;
            }
        }
        if (inexistentFound) {
            List<File> l = new ArrayList<>();
            for (File file : files) {
                if (file.exists()) {
                    l.add(file);
                }
            }
            return l.toArray(new File[l.size()]);
        }
        return files;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean cancel() {
        return false;
    }
}
