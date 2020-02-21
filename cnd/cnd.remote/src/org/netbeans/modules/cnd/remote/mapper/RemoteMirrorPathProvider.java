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

package org.netbeans.modules.cnd.remote.mapper;

import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.setup.MirrorPathProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.remote.spi.FileSystemCacheProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.util.Exceptions;

/**
 * MirrorPathProvider implementation
 */
@org.openide.util.lookup.ServiceProvider(service=MirrorPathProvider.class, position=100)
public class RemoteMirrorPathProvider implements MirrorPathProvider {
    private static final String POSTFIX = System.getProperty("cnd.remote.sync.root.postfix"); //NOI18N

    /** Service provider contract */
    public RemoteMirrorPathProvider() {
    }

    @Override
    public String getLocalMirror(ExecutionEnvironment executionEnvironment) {
        return FileSystemCacheProvider.getCacheRoot(executionEnvironment);
    }

    @Override
    public String getRemoteMirror(ExecutionEnvironment executionEnvironment) throws ConnectException, IOException, ConnectionManager.CancellationException {
        String root;
        root = System.getProperty("cnd.remote.sync.root." + RemoteUtil.hostNameToRemoteFileName(executionEnvironment.getHost())); //NOI18N
        if (root != null) {
            return root;
        }
        root = System.getProperty("cnd.remote.sync.root"); //NOI18N
        if (root != null) {
            return root;
        }
        
        if (!HostInfoUtils.isHostInfoAvailable(executionEnvironment)) {
            return null;
        }
        String home = HostInfoUtils.getHostInfo(executionEnvironment).getUserDir();
        
        final ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();
        MacroExpander expander = MacroExpanderFactory.getExpander(local);
        String localHostID = local.getHost();
        try {
            localHostID = expander.expandPredefinedMacros("${hostname}-${osname}-${platform}${_isa}"); // NOI18N
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        localHostID = RemoteUtil.hostNameToLocalFileName(localHostID);
        // each local host maps into own remote folder to prevent collisions on path mapping level
        // remote hosts should be separated as well since they can share home directory
        String result = home + "/.netbeans/remote/" + RemoteUtil.hostNameToRemoteFileName(executionEnvironment.getHost()) + "/" + localHostID; //NOI18N
        if (POSTFIX != null) {
            result += '-' + POSTFIX;
        }
        String canonical = FileSystemProvider.getCanonicalPath(executionEnvironment, result);
        return (canonical == null) ? result : canonical;
    }
}
