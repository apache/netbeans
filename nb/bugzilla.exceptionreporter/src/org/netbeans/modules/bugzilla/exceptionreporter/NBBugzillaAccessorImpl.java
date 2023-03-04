/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.bugzilla.exceptionreporter;

import org.netbeans.lib.uihandler.BugTrackingAccessor;
import org.netbeans.modules.bugzilla.api.NBBugzillaUtils;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.lib.uihandler.BugTrackingAccessor.class)
public class NBBugzillaAccessorImpl extends BugTrackingAccessor {

    private RequestProcessor rp;

    @Override
    public void openIssue(final String issueID) {
        getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                NBBugzillaUtils.openIssue(issueID);
            }
        });
    }

    @Override
    public String getUsername() {
        return NBBugzillaUtils.getNBUsername();
    }

    @Override
    public char[] getPassword() {
        return NBBugzillaUtils.getNBPassword();
    }

    @Override
    public void saveUsername(String username) {
        NBBugzillaUtils.saveNBUsername(username);
    }

    @Override
    public void savePassword(char[] password) {
        NBBugzillaUtils.saveNBPassword(password);
    }

    private RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("NBBugzilaReports"); // NOI18N
        }
        return rp;
    }

}
