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

package org.netbeans.modules.derby;

import junit.framework.*;

/**
 *
 * @author pj97932
 */
public class SearchUtilTest extends TestCase {

    public SearchUtilTest(String testName) {
        super(testName);
    }

    /**
     * Test of checkForString method, of class org.netbeans.modules.derby.SearchUtil.
     */
    public void testCheckForString() {
        String searchedFor;
        int searchStart;
        char[] buf;
        int bufLen;

        searchedFor = "12345";
        searchStart = 0;
        buf = new char[] {'a', 'b', '1', '2', '3', 'x', 'x'};
        bufLen = 5;
        assertEquals(3, SearchUtil.checkForString(searchedFor, searchStart, buf, bufLen));
        
        searchedFor = "12345";
        searchStart = 2;
        buf = new char[] {'3', '4', '5', 'a', 'b', 'x'};
        bufLen = 5;
        assertEquals(SearchUtil.FOUND, SearchUtil.checkForString(searchedFor, searchStart, buf, bufLen));
        
        searchedFor = "12345";
        searchStart = 0;
        buf = new char[] {'3', '4', '5', 'a', 'b', 'x'};
        bufLen = 5;
        assertEquals(0, SearchUtil.checkForString(searchedFor, searchStart, buf, bufLen));
        
        searchedFor = "12345";
        searchStart = 0;
        buf = new char[] {'a', 'b', 'c', '1', '2', 'x'};
        bufLen = 5;
        assertEquals(2, SearchUtil.checkForString(searchedFor, searchStart, buf, bufLen));
        
    }

    /**
     * Test of checkPosition method, of class org.netbeans.modules.derby.SearchUtil.
     */
    public void testCheckPosition() {
        String searchedFor = "12345";
        int searchStart = 0;
        char[] buf = new char[] {'a', 'b', '1', '2', '3', 'x', 'x'};
        int bufLen = 5;
        int bufFrom = 2;
        assertEquals(3, SearchUtil.checkPosition(searchedFor, searchStart, buf, bufLen, bufFrom));
        
    }
    
}
