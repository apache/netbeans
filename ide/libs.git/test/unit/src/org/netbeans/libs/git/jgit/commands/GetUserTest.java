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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author Tomas Stupka
 */
public class GetUserTest extends AbstractGitTestCase {

    public GetUserTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetUser() throws Exception {
        File workDir = getWorkingDirectory();
        File cofig = new File(new File(workDir, ".git"), "config");
        
        FileOutputStream fos = new FileOutputStream(cofig, true);
        try {
            fos.write("\n[user]\n\tname = A U Thor\n\temail = author@example.com\n".getBytes());
            fos.close();
        } finally {
            if(fos != null) {
                fos.close();
            }
        }        
        clearRepositoryPool(); // ensure old config isn't cached already
        
        GitClient client = getClient(workDir);
        GitUser user = client.getUser();
        assertEquals("A U Thor", user.getName());
        assertEquals("author@example.com", user.getEmailAddress());
    }
}
