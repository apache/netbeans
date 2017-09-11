/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.client;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
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
