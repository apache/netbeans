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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Platform;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIConst;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MITListItem;
import org.netbeans.modules.cnd.debugger.gdb2.mi.MIValue;

/**
 * Contains actions which may vary in different versions of gdb
 *
 */
public class GdbVersionPeculiarity {
    private final Version version;
    private final Platform platform;
    private final Set<String> features = Collections.synchronizedSet(new HashSet<String>());
    private final boolean lldb = "true".equals(System.getProperty("cnd.debugger.lldb")); // NOI18N
    
    public static final class Version {

        final int before;
        final int after;

        public Version(int before, int after) {
            this.before = before;
            this.after = after;
        }

        public int compareTo(int before, int after) {
            int beforeDiff = this.before - before;
            if (beforeDiff == 0) {
                return this.after - after;
            } else {
                return beforeDiff;
            }
        }

        @Override
        public String toString() {
            return String.format("%d.%d", before, after); // NOI18N
        }
    }

    private GdbVersionPeculiarity(Version version, Platform platform) {
        this.version = version;
        this.platform = platform;
    }

    public static GdbVersionPeculiarity create(Version version, Platform platform) {
        return new GdbVersionPeculiarity(version, platform);
    }

    public String environmentDirectoryCommand() {
        if (version.compareTo(6, 3) > 0 || platform == Platform.MacOSX_x86) {
            return "-environment-directory"; // NOI18N
        } else {
            return "directory"; // NOI18N
        }
    }

    public String environmentCdCommand() {
        if (version.compareTo(6, 3) > 0) {
            return "-environment-cd"; // NOI18N
        } else {
             return "cd"; // NOI18N
        }
    }

    public String execAbortCommand() {
        if (version.compareTo(6, 6) > 0) {
            return "-exec-abort"; // NOI18N
        } else {
            return "kill"; // NOI18N
        }
    }
    
    public String execStepCommand(String thread) {
        if (version.compareTo(7, 4) >= 0 || lldb) {
            return "-exec-step --thread " + thread; // NOI18N
        } else {
            return "-exec-step"; // NOI18N
        }
    }
    
    public String execNextCommand(String thread) {
        if (version.compareTo(7, 4) >= 0 || lldb) {
            return "-exec-next --thread " + thread; // NOI18N
        } else {
            return "-exec-next"; // NOI18N
        }
    }
    
    public String execStepInstCommand(String thread) {
        if (version.compareTo(7, 4) >= 0 || lldb) {
            return "-exec-step-instruction --thread " + thread; // NOI18N
        } else {
            return "-exec-step-instruction"; // NOI18N
        }
    }
    
    public String execNextInstCommand(String thread) {
        if (version.compareTo(7, 4) >= 0 || lldb) {
            return "-exec-next-instruction --thread " + thread; // NOI18N
        } else {
            return "-exec-next-instruction"; // NOI18N
        }
    }
    
    public String execFinishCommand(String thread) {
        if (lldb) {
            return "-exec-finish --thread " + thread; // NOI18N
        } else {
            return "-exec-finish"; // NOI18N
        }
    }
    
    public String listChildrenCommand(String expr, int start, int end) {
        StringBuilder retVal = new StringBuilder();
        if (lldb) {
            retVal.append("-var-list-children --all-values ").append(expr); // NOI18N
        } else {
            retVal.append("-var-list-children --all-values \"").append(expr).append("\""); // NOI18N
        }
        
        if (version.compareTo(6, 8) > 0) {
            retVal.append(" ").append(start).append(" ").append(end); // NOI18N
        }
        
        return retVal.toString();
    }
    
    public String showAttributesCommand(String expr) {
        if (lldb) {
            return "-var-show-attributes " + expr; // NOI18N
        } else {
            return "-var-show-attributes \"" + expr + "\""; // NOI18N
        }
    }

    public boolean isLldb() {
        return lldb;
    }
    
    public boolean isThreadsOutputUnusual() {
        return platform == Platform.MacOSX_x86;
    }
    
    public boolean isLocalsOutputUnusual() {
        return platform == Platform.MacOSX_x86 && version.compareTo(6, 3) <= 0;
    }
    
    public boolean isSyscallBreakpointsSupported() {
        return platform != Platform.MacOSX_x86;
    }

    private static final boolean DISABLE_PENDING = Boolean.getBoolean("gdb.breakpoints.pending.disabled"); //NOI18N

    public String breakPendingFlag() {
        if (!DISABLE_PENDING
                && (version.compareTo(6, 8) >= 0 || platform == Platform.MacOSX_x86)) {
            return " -f"; // NOI18N
        } else {
            return "";
        }
    }
    
    public String breakDisabledFlag() {
        if (version.compareTo(6, 8) >= 0 || platform == Platform.MacOSX_x86) {
            return " -d"; // NOI18N
        } else {
            return "";
        }
    }
    
    public String createVarCommand(String expr, String thread, String frame) {
        if (lldb) {
            return "-var-create - @ " + expr + " --thread " + thread + " --frame " + frame; // NOI18N
        } else {
            return "-var-create - @ " + expr; // NOI18N
        }
    }
    
    public String stackListFramesCommand(String thread) {
        if (version.compareTo(7, 4) >= 0 || lldb) {
            return "-stack-list-frames --thread " + thread; // NOI18N
        } else {
            return "-stack-list-frames";  // NOI18N
        }
    }
    
    public String stackListLocalsCommand() {
        return "-stack-list-variables --no-values"; // NOI18N
    }

    public boolean isSupported() {
        return (version.compareTo(6, 8) >= 0) || (platform == Platform.MacOSX_x86 && version.compareTo(6, 3) >= 0);
    }
    
    // gdb features
    public static enum Feature {
        THREAD_INFO("thread-info"), //NOI18N
        BREAKPOINT_NOTIFICATIONS("breakpoint-notifications"); //NOI18N
        
        private final String command;
        Feature(String command) {
            this.command = command;
        }
    }
    
    public boolean supports(Feature feature) {
        return features.contains(feature.command);
    }
    
    void setFeatures(MIRecord result) {
        synchronized (features) {
            features.clear();
            try {
                MITList results = result.results();
                MIValue value = ((MIResult)results.get(0)).value();
                for (MITListItem item : value.asList()) {
                    features.add(((MIConst)item).value());
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }
}
