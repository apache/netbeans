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

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.remote.support.RemoteProjectSupport;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
*
*/
public abstract class BaseSyncFactory extends RemoteSyncFactory {

   @Override
   public RemoteSyncWorker createNew(Lookup.Provider project, PrintWriter out, PrintWriter err) {
       ExecutionEnvironment execEnv = RemoteProjectSupport.getExecutionEnvironment(project);
       if (execEnv.isRemote()) {
                   FileObject privateStorageFile = RemoteProjectSupport.getPrivateStorage(project);
                   if (privateStorageFile != null && !privateStorageFile.isValid()) {
                       System.err.printf("Error creating directory %s%n", privateStorageFile.getPath());
                   }
                   AtomicReference<String> runDir = new AtomicReference<>();
                   List<FSPath> sourceRoots = RemoteProjectSupport.getProjectSourceDirs(project, runDir);
                   return createNew(execEnv, out, err, privateStorageFile, runDir.get(),
                           sourceRoots, RemoteProjectSupport.getBuildResults(project));
       }
       return null;
   }
}
