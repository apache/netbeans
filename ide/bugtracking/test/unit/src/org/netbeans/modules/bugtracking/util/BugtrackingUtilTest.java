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

package org.netbeans.modules.bugtracking.util;

import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.bugtracking.TestIssue;
import org.netbeans.modules.bugtracking.TestKit;
import org.netbeans.modules.bugtracking.TestQuery;
import org.netbeans.modules.bugtracking.TestRepository;
import org.netbeans.modules.bugtracking.spi.*;

/**
 *
 * @author Tomas Stupka
 */
public class BugtrackingUtilTest {

    private static final String[] VALID_HOSTS = new String [] {
        "netbeans.org/bugzilla",
        "123.netbeans.org/bugzilla",
        "netbeans123.org/bugzilla",
        "123netbeans123.org/bugzilla",
        "somenetbeans123.org/bugzilla",
        "some.netbeans123.org/bugzilla",
        "netbeans.org/123bugzilla",
        "netbeans.org/eventhis",
    };

    private static final String[] INVALID_HOSTS = new String [] {
        "n123etbeans.org/bugzilla",
        "netbeans.aorg/bugzilla",
        "netbeans.123org/bugzilla",
        "a",
        "a.b",
        "a.b.c",
    };

    public BugtrackingUtilTest() {
    }

    @Test
    public void testIsNbRepositoryStringUrl() {        
        assertFalse(NBBugzillaUtils.isNbRepository("enembenem"));
        urlTest("http://", VALID_HOSTS, false);
        urlTest("https://", VALID_HOSTS, false);
        urlTest("http://", INVALID_HOSTS, true);
        urlTest("https://", INVALID_HOSTS, true);
    }

    @Test
    public void testWrongProtocol() {
        urlTest("", VALID_HOSTS, true);
        urlTest("ht", VALID_HOSTS, true);
        urlTest("http:", VALID_HOSTS, true);
        urlTest("http:/", VALID_HOSTS, true);
        urlTest("htttp://", VALID_HOSTS, true);
    }
    
    @Test
    public void testWrongHost() {
        urlTest("http://", INVALID_HOSTS, true);
    }

    private void urlTest(String protocol, String[] hosts, boolean fail) {
        for (String url : hosts) {
            if(!fail && !NBBugzillaUtils.isNbRepository(protocol + url)) {
                fail(protocol + url + " is expected to be a netbeans bugzilla url");
            } else if(fail && NBBugzillaUtils.isNbRepository(protocol + url)) {
                fail(protocol + url + " isn't expected to be a netbeans bugzilla url");
            }
        }
    }

    @Test(expected=NullPointerException.class)
    public void testIsNbRepositoryNullString() {
        String url = null;
        NBBugzillaUtils.isNbRepository(url);
    }

    @Test
    public void testIsNbRepositoryRepo() {
        assertFalse(NBBugzillaUtils.isNbRepository(TestKit.getRepository(new BUTestRepository("enembenem")).getUrl()));
        assertTrue(NBBugzillaUtils.isNbRepository("https://netbeans.org/bugzilla"));        
    }

    @Test(expected=NullPointerException.class)
    public void testIsNbRepositoryNullRepo() {
        BUTestRepository repo = null;
        assertFalse(NBBugzillaUtils.isNbRepository(TestKit.getRepository(repo).getUrl()));
    }

    @Test(expected=NullPointerException.class)
    public void testIsNbRepositoryRepoNullUrl() {
        assertFalse(NBBugzillaUtils.isNbRepository(TestKit.getRepository(new BUTestRepository(null)).getUrl()));
    }

    private class BUTestRepository extends TestRepository {
        private final String url;
        private RepositoryInfo info;
        public BUTestRepository(String url) {
            this.url = url;
            this.info = new RepositoryInfo(null, null, url, null,null,null,null,null,null);
        }
        @Override
        public RepositoryInfo getInfo() {
            return info;
        }
    }

}
