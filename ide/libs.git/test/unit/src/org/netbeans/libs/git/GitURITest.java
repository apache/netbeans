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

package org.netbeans.libs.git;

import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class GitURITest extends AbstractGitTestCase {

    public GitURITest (String testName) throws IOException {
        super(testName);
    }

    public void testUriInvalidEscapeSequence () throws Exception {
        String failingUri = "http://aaa/abc%2test";
        try {
            new URIish(failingUri);
        } catch (URISyntaxException ex) {
            // OK
        }
        try {
            new GitURI(failingUri);
        } catch (URISyntaxException ex) {
            // OK
        }
    }
    
}
