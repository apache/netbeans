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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 * A common base class for RemoteSyncWorker implementations
 */
/*package-local*/ abstract class BaseSyncWorker implements RemoteSyncWorker {

    protected final File[] files;
    protected final List<File> buildResults;
    protected final FSPath[] fsPaths;
    protected final FileSystem fileSystem;
    protected final FileObject privProjectStorageDir;
    protected final ExecutionEnvironment executionEnvironment;
    protected final PrintWriter out;
    protected final PrintWriter err;

    public BaseSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, 
            FileObject privProjectStorageDir, List<FSPath> paths, List<FSPath> buildResults) {
        this.fsPaths = paths.toArray(new FSPath[paths.size()]);
        this.fileSystem = SyncUtils.getSingleFileSystem(paths);
        this.files = SyncUtils.toFiles(this.fsPaths);
        this.buildResults = SyncUtils.toFiles(buildResults);
        this.privProjectStorageDir = privProjectStorageDir;
        this.executionEnvironment = executionEnvironment;
        this.out = out;
        this.err = err;
    }
}
