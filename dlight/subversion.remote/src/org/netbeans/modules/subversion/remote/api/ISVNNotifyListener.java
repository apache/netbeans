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

import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public interface ISVNNotifyListener {
    public enum Command {
        UNDEFINED,
        ADD,
        CHECKOUT,
        COMMIT,
        UPDATE,
        MOVE,
        COPY,
        REMOVE,
        EXPORT,
        IMPORT,
        MKDIR,
        LS,
        STATUS,
        LOG,
        PROPSET,
        PROPDEL,
        REVERT,
        DIFF,
        CAT,
        INFO,
        PROPGET,
        PROPLIST,
        RESOLVED,
        CREATE_REPOSITORY,
        CLEANUP,
        ANNOTATE,
        SWITCH,
        MERGE,
        LOCK,
        UNLOCK,
        RELOCATE,
        RESOLVE,
        MERGEINFO,
        UPGRADE;
    }

    void setCommand(ISVNNotifyListener.Command i);

    void logCommandLine(String string);

    void logMessage(String string);

    void logError(String string);

    void logRevision(long l, String string);

    void logCompleted(String string);

    void onNotify(VCSFileProxy file, SVNNodeKind svnnk);
}
