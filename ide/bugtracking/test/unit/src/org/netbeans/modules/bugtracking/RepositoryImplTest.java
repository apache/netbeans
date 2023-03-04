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

package org.netbeans.modules.bugtracking;

import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author tomas
 */
public class RepositoryImplTest extends NbTestCase {

    public RepositoryImplTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();                
    }

    public void testcompareID() {
        RepositoryImpl r = TestKit.getRepository("defaulttestrepository");
        assertTrue(r.compareID("1", "1") == 0);
        assertTrue(r.compareID("123", "123") == 0);
        assertTrue(r.compareID("1", "2") < 0);
        assertTrue(r.compareID("123", "234") < 0);
        assertTrue(r.compareID("2", "1") > 0);
        assertTrue(r.compareID("234", "123") > 0);
        
        assertTrue(r.compareID("a", "a") == 0);
        assertTrue(r.compareID("abc", "abc") == 0);
        assertTrue(r.compareID("a", "b") < 0);
        assertTrue(r.compareID("b", "a") > 0);
        assertTrue(r.compareID("a1", "a2") < 0);
        assertTrue(r.compareID("a2", "a1") > 0);
        
        assertTrue(r.compareID("a-1", "a-2") < 0);
        assertTrue(r.compareID("b-1", "a-1") > 0);
        assertTrue(r.compareID("a-123", "a-200") < 0);
        assertTrue(r.compareID("b-123", "a-200") > 0);
        assertTrue(r.compareID("bcd-123", "b-200") > 0);
        
        assertTrue(r.compareID("1-a", "2-a") < 0);
        assertTrue(r.compareID("2-a", "1-a") > 0);
        assertTrue(r.compareID("123-a", "200-a") < 0);
        assertTrue(r.compareID("200-b", "123-a") > 0);
        assertTrue(r.compareID("200-bcd", "200-b") > 0);        
        
        assertTrue(r.compareID("-a", "-a") == 0);        
        assertTrue(r.compareID("-a", "-b") < 0);        
        assertTrue(r.compareID("-abc", "-abc") == 0);        
        assertTrue(r.compareID("-abc", "-abd") < 0);   
        
        assertTrue(r.compareID("-1", "-1") == 0);        
        assertTrue(r.compareID("-1", "-2") < 0);        
        assertTrue(r.compareID("-123", "-123") == 0);        
        assertTrue(r.compareID("-123", "-124") < 0);   
        
        assertTrue(r.compareID("a-", "a-") == 0);   
        assertTrue(r.compareID("a-", "b-") < 0);   
        assertTrue(r.compareID("b-", "a-") > 0);   
        assertTrue(r.compareID("abc-", "abd-") < 0);   
        
        assertTrue(r.compareID("1-", "1-") == 0);   
        assertTrue(r.compareID("1-", "2-") < 0);   
        assertTrue(r.compareID("2-", "1-") > 0);   
        assertTrue(r.compareID("123-", "124-") < 0);   
        
        
    }
    
}
