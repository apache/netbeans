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

package org.netbeans.modules.web.jsf.refactoring;

import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 *
 * @author Petr Pisl
 */
public class OccurrencesTest extends TestCase {
    
    public OccurrencesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetNewFQCN() {
        assertEquals("aa.b.c.T", Occurrences.getNewFQCN("aa", "a", "a.b.c.T"));
        assertEquals("a.bb.c.T", Occurrences.getNewFQCN("a.bb", "a.b", "a.b.c.T"));
        assertEquals("a.b.cc.T", Occurrences.getNewFQCN("a.b.cc", "a.b.c", "a.b.c.T"));
        assertEquals("aa.T", Occurrences.getNewFQCN("aa", "a", "a.T"));
        assertEquals("aa.T", Occurrences.getNewFQCN("aa", "a.b.c", "a.b.c.T"));
        assertEquals("a.b.cc.T", Occurrences.getNewFQCN("a.b.cc", "a.b.c", "a.b.c.T"));
        assertEquals("a.b.T", Occurrences.getNewFQCN("a.b", "a.b.c", "a.b.c.T"));
        assertEquals("b.T", Occurrences.getNewFQCN("b", "a.b", "a.b.T"));
        assertEquals("T", Occurrences.getNewFQCN("", "a.b", "a.b.T"));
    }
    
}
