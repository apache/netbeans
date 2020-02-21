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
package org.netbeans.modules.subversion.remote.api;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class SVNNotificationHandler {
    private Set<ISVNNotifyListener> notifylisteners = new HashSet<>();
    private boolean logEnabled = true;
    private VCSFileProxy baseDir;
    
    public void notifyListenersOfChange(VCSFileProxy path) {
        if (logEnabled) {
            for(ISVNNotifyListener l: notifylisteners) {
                l.onNotify(path, SVNNodeKind.NONE);
            }
        }
    }
    
    public void logCommandLine(String commandLine) {
        if (logEnabled) {
            for(ISVNNotifyListener l: notifylisteners) {
                l.logCommandLine(commandLine);
            }
        }
    }
    public void logMessage(String message) {
        if (logEnabled) {
            for(ISVNNotifyListener l: notifylisteners) {
                l.logMessage(message);
            }
        }
    }
    
    public void logError(String message) {
        if (logEnabled) {
            for(ISVNNotifyListener l: notifylisteners) {
                l.logError(message);
            }
        }
    }
    
    public void logCompleted(String string) {
        if (logEnabled) {
            for(ISVNNotifyListener l: notifylisteners) {
                l.logCompleted(string);
            }
        }
    }

    public void add(ISVNNotifyListener l) {
        notifylisteners.add(l);
    }

    public void remove(ISVNNotifyListener l) {
        notifylisteners.remove(l);
    }
    
    public void enableLog() {
        logEnabled = true;
    }

    public void disableLog() {
        logEnabled = false;
    }
    
    public void setBaseDir(VCSFileProxy baseDir) {
        this.baseDir = baseDir;
    }
}
