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

package org.netbeans.modules.uihandler;

import junit.framework.TestCase;

/**
 *
 * @author Petr Zajac
 */
public class TimeToFailureTest extends TestCase {
    public void testLog() throws InterruptedException {
        System.setProperty("java.util.prefs.PreferencesFactory","org.netbeans.junit.internal.MemoryPreferencesFactory");//NOI18N
        TimeToFailure.logAction();
        Thread.sleep(10); 
        TimeToFailure.logAction();
        assertTrue("More then 5 ms", 5 <= TimeToFailure.totalTime);        
        assertNotNull(TimeToFailure.logFailure());
        assertEquals(0,TimeToFailure.totalTime);
    }
    
}
