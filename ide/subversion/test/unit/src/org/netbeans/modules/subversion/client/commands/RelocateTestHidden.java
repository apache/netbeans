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

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class RelocateTestHidden extends AbstractCommandTestCase {
    
    public RelocateTestHidden(String testName) throws Exception {
        super(testName);
    }
    
    public void testRelocateFile() throws Exception {
        // relocate must be called on the checkout root, there are no metadata in folders in 1.7
        File file = createFile("file");
        add(file);
        commit(file);
        SVNUrl repo2Url = copyRepo("testRelocateFile");

        assertInfo(file, getFileUrl(file));

        ISVNClientAdapter c = getNbClient();
        try {
            c.relocate(getRepoUrl().toString(), repo2Url.toString(), file.getParentFile().getAbsolutePath(), false);
        } catch (SVNClientException ex) {
            if (isCommandLine() && ex.getMessage().contains("--relocate and --non-recursive (-N) are mutually exclusive")) {
                // commandline client 1.7 does not allow non-recursive relocate, obviously it's nonsense because metadata are only in the top folder
                c.relocate(getRepoUrl().toString(), repo2Url.toString(), file.getParentFile().getAbsolutePath(), true);
            } else {
                throw ex;
            }
        }
        assertInfo(file, repo2Url.appendPath(getName()).appendPath(getWC().getName()).appendPath(file.getName()));

        c.relocate(repo2Url.toString(), getRepoUrl().toString(), file.getParentFile().getAbsolutePath(), true);
        assertInfo(file, getRepoUrl().appendPath(getName()).appendPath(getWC().getName()).appendPath(file.getName()));
    }

}
