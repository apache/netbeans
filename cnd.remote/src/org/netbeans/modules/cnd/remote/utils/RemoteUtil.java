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
package org.netbeans.modules.cnd.remote.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.support.RemoteConnectionSupport;
import org.netbeans.modules.cnd.remote.support.RemoteLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Misc. utiliy finctions
 */
public class RemoteUtil {

    public static final Logger LOGGER = RemoteLogger.getInstance();

    public static class PrefixedLogger {

        private final String prefix;

        public PrefixedLogger(String prefix) {
            this.prefix = prefix;
        }

        public void log(Level level, Throwable exception, String format, Object... args) {
            if (LOGGER.isLoggable(level)) {
                String text = String.format(format, args);
                text = prefix + ": " + text; // NOI18N
                LOGGER.log(level, text, exception);
            }
        }

        public void log(Level level, String format, Object... args) {
            if (LOGGER.isLoggable(level)) {
                String text = String.format(format, args);
                text = prefix + ": " + text; // NOI18N
                LOGGER.log(level, text);
            }
        }

        public Level getLevel() {
            return LOGGER.getLevel();
        }

        public boolean isLoggable(Level level) {
            return LOGGER.isLoggable(level);
        }
    }

    private RemoteUtil() {}

    /**
     * FIXUP: * Need this hack for Cloud stuff:
     * we have to distinguish "normal", i.e. CND environments
     * from "foreign", i.e. "cloud" ones.
     */
    public static boolean isForeign(ExecutionEnvironment execEnv ) {
        if (execEnv == null) {
            return false;
        }
        String id = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        String protocolSeparator = "://"; //NOI18N
        if (id.indexOf(protocolSeparator) < 0) {
            // old-style environment - no protocol
            return false;
        } else {
            // there is a protocol and it equals to ssh
            return ! id.startsWith("ssh" + protocolSeparator); //NOI18N
        }
    }

    public static String getDisplayName(ExecutionEnvironment execEnv) {
        ServerRecord rec = ServerList.get(execEnv);
        if (rec == null) {
            return execEnv.getDisplayName();
        } else {
            return rec.getDisplayName();
        }
    }

    public static void checkSetupAfterConnection(ExecutionEnvironment env) {
        RemoteServerRecord record = (RemoteServerRecord) ServerList.get(env);
        checkSetupAfterConnection(record);
    }

    public static void checkSetupAfterConnection(RemoteServerRecord record) {
        if (!record.isOnline()) {
            record.resetOfflineState();
            record.init(null);
            if (record.isOnline()) {
                ToolsCacheManager cacheManager = ToolsCacheManager.createInstance(true);
                CompilerSetManager csm = cacheManager.getCompilerSetManagerCopy(record.getExecutionEnvironment(), false);
                csm.initialize(false, true, null);
                cacheManager.applyChanges();
            }
        }
    }
    
    public static boolean isWindows(ExecutionEnvironment env) {
        return env.isLocal() && Utilities.isWindows();
    }
    
    public static HostInfo.OSFamily getOSFamilyIfAvailable(ExecutionEnvironment env) {
        if (HostInfoUtils.isHostInfoAvailable(env)) {
            try {
                return HostInfoUtils.getHostInfo(env).getOSFamily();
            } catch (IOException | ConnectionManager.CancellationException ex) {
                return null;
            }
        }
        return null;
    }

    public static String getMessage(IOException e) {
        String result;
        String reason = e.getMessage();
        if (e instanceof UnknownHostException) {
            result = NbBundle.getMessage(RemoteConnectionSupport.class, "REASON_UnknownHost", e.getMessage());
        } else if (reason.startsWith("Auth fail")) { // NOI18N
            result = NbBundle.getMessage(RemoteConnectionSupport.class, "REASON_AuthFailed");
        } else {
            result = reason;
}
        return result;
    }    
    
    // see #268771 - Can't build project in simple remote mode with IPv6
    public static String hostNameToLocalFileName(String hostName) {
        try {
            return URLEncoder.encode(hostName, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex); // can not happen
            return hostName.replace(":", "_"); //NOI18N
        }
    }

    // For now I decided to leave remote mirror and other host-related remote files as is
    // since Unix has much less filename limitations and changing can affect users
    // see #268771 - Can't build project in simple remote mode with IPv6    
    public static String hostNameToRemoteFileName(String hostName) {
        return hostName;
    }
}
