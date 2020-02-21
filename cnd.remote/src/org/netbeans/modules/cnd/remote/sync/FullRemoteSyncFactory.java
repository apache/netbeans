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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.remote.support.RemoteProjectSupport;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory.class, position=200)
public class FullRemoteSyncFactory extends BaseSyncFactory {

    /** this factory ID -  public for test purposes */
    private static final String ID = "full"; //NOI18N
    private final PathMap pathMapper = new FullRemotePathMap();

    @Override
    public RemoteSyncWorker createNew( ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, 
            FileObject privProjectStorageDir, String workingDir, List<FSPath> files, List<FSPath> buildResults) {
        return new FullRemoteSyncWorker(executionEnvironment, out, err, files);
    }

    @Override
    public RemoteSyncWorker createNew(Lookup.Provider project, PrintWriter out, PrintWriter err) {
        ExecutionEnvironment execEnv = RemoteProjectSupport.getExecutionEnvironment(project);
        RemoteProject rp = project.getLookup().lookup(RemoteProject.class);
        if (rp == null) {
            return null;
        }
        final FileObject projDirFO = rp.getSourceBaseDirFileObject();
        FileSystem fileSystem = RemoteFileUtil.getProjectSourceFileSystem(project);
        return new FullRemoteSyncWorker(execEnv, out, err, Collections.singletonList(FSPath.toFSPath(projDirFO)));
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "FULL_Factory_Name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(getClass(), "FULL_Factory_Description");
    }


    @Override
    public String getID() {
        return ID;
    }

    @Override
    public boolean isCopying() {
        return false;
    }
    
    @Override
    public boolean isApplicable(ExecutionEnvironment execEnv) {
        // return RemoteProject.FULL_REMOTE && ! RemoteUtil.isForeign(execEnv);
        return false; // never show it in the list
    }

    @Override
    public boolean isPathMappingCustomizable() {
        return false;
    }

    @Override
    public PathMap getPathMap(ExecutionEnvironment executionEnvironment) {
        return pathMapper;
    }

    private final static class FullRemotePathMap extends PathMap {

        @Override
        public boolean checkRemotePaths(File localPaths[], boolean fixMissingPath) {
            return true;
        }

        @Override
        public String getLocalPath(String rpath, boolean useDefault) {
            return rpath;
        }

        @Override
        public String getRemotePath(String lpath, boolean useDefault) {
            return lpath;
        }

        @Override
        public String getTrueLocalPath(String rpath) {
            return null;
        }
    }
}
