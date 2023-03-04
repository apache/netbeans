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

package org.netbeans.modules.bugzilla.issue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author tomas
 */
public class CurrentNBVersionTest extends NbTestCase {

    public CurrentNBVersionTest(String arg0) {
        super(arg0);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
    }

    public void testVersionParsing() {
        
        assertEquals("Dev", IssuePanel.parseProductVersion("NetBeans IDE Dev 101010-000000000000"));
        assertEquals("Dev", IssuePanel.parseProductVersion("NetBeans IDE Dev"));
        assertEquals("Dev", IssuePanel.parseProductVersion("NetBeans IDE Dev "));
        assertEquals("6.9", IssuePanel.parseProductVersion("NetBeans IDE 6.9 101010-000000000000"));
        assertEquals("6.9.1", IssuePanel.parseProductVersion("NetBeans IDE 6.9.1 101010-000000000000"));
        assertEquals("6.9", IssuePanel.parseProductVersion("NetBeans IDE 6.9"));
        assertEquals("6.9", IssuePanel.parseProductVersion("NetBeans IDE 6.9 "));
        assertEquals("6.9.1", IssuePanel.parseProductVersion("NetBeans IDE 6.9.1"));        
        assertEquals("6.9.1", IssuePanel.parseProductVersion("NetBeans IDE 6.9.1 "));        
        assertEquals("6.9.1", IssuePanel.parseProductVersion("NetBeans IDE 6.9.1 6.9.1"));                
        
        assertNull(IssuePanel.parseProductVersion("xxx"));
        assertNull(IssuePanel.parseProductVersion("NetBeans IDE"));
    }
    
}
