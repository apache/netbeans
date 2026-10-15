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
package org.netbeans.modules.subversion.client;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;

/**
 *
 * @author Ondrej Vrabec
 */
public class SvnClientInvocationHandlerTest {

    private static final Set<String> METHODS = new HashSet<>(Arrays.asList(
        "annotate", //NOI18N
        "addConflictResolutionCallback", //NOI18N
        "addDirectory", //NOI18N
        "addFile", //NOI18N
        "addKeywords", //NOI18N
        "addNotifyListener", //NOI18N
        "addPasswordCallback", //NOI18N
        "addToIgnoredPatterns", //NOI18N
        "cancelOperation", //NOI18N
        "canCommitAcrossWC", //NOI18N
        "checkout", //NOI18N
        "cleanup", //NOI18N
        "commit", //NOI18N
        "commitAcrossWC", //NOI18N
        "copy", //NOI18N
        "createPatch", //NOI18N
        "createRepository", //NOI18N
        "diff", //NOI18N
        "diffSummarize", //NOI18N
        "dispose", //NOI18N
        "doImport", //NOI18N
        "doExport", //NOI18N
        "getAdminDirectoryName", //NOI18N
        "getContent", //NOI18N
        "getDirEntry", //NOI18N
        "getIgnoredPatterns", //NOI18N
        "getInfo", //NOI18N
        "getInfoFromWorkingCopy", //NOI18N
        "getKeywords", //NOI18N
        "getList", //NOI18N
        "getListWithLocks", //NOI18N
        "getLogMessages", //NOI18N
        "getMergeInfo", //NOI18N
        "getMergeinfoLog", //NOI18N
        "getNotificationHandler", //NOI18N
        "getPostCommitError", //NOI18N
        "getProperties", //NOI18N
        "getPropertiesIncludingInherited", //NOI18N
        "getRevProperties", //NOI18N
        "getRevProperty", //NOI18N
        "getSingleStatus", //NOI18N
        "getStatus", //NOI18N
        "isAdminDirectory", //NOI18N
        "isThreadsafe", //NOI18N
        "lock", //NOI18N
        "merge", //NOI18N
        "mergeReintegrate", //NOI18N
        "mkdir", //NOI18N
        "move", //NOI18N
        "propertyDel", //NOI18N
        "propertyGet", //NOI18N
        "propertySet", //NOI18N
        "relocate", //NOI18N
        "remove", //NOI18N
        "removeKeywords", //NOI18N
        "removeNotifyListener", //NOI18N
        "resolve", //NOI18N
        "resolved", //NOI18N
        "revert", //NOI18N
        "setConfigDirectory", //NOI18N
        "setIgnoredPatterns", //NOI18N
        "setKeywords", //NOI18N
        "setPassword", //NOI18N
        "setProgressListener", //NOI18N
        "setRevProperty", //NOI18N
        "setUsername", //NOI18N
        "statusReturnsRemoteInfo", //NOI18N
        "suggestMergeSources", //NOI18N
        "switchToUrl", //NOI18N
        "unlock", //NOI18N
        "update", //NOI18N
        "upgrade"
    ));
    
    public SvnClientInvocationHandlerTest () {
    }

    /**
     * Tests all ISVNClientAdapter methods available. All WC read-only methods
     * must be picked and inserted into SvnClientInvocationHandler's
     * READ_ONLY_METHODS to ensure the methods are called under read-lock.
     */
    @Test
    public void testClientMethods () {
        Set<String> newMethods = new HashSet<>();
        for (Method m : ISVNClientAdapter.class.getDeclaredMethods()) {
            if (!METHODS.contains(m.getName())) {
                newMethods.add(m.getName());
            }
        }
        List<String> methodArray = new ArrayList<>(newMethods);
        Collections.sort(methodArray);
        Assert.assertEquals("New methods ISVNClientAdapter." + methodArray
                + ". Is there a read-only method?", 0, methodArray.size());
    }

}
