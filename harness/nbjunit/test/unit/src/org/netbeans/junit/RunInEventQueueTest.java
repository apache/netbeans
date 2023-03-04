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

package org.netbeans.junit;

import javax.swing.SwingUtilities;

/** Checks that NbTestCase really sends tests to AWT thread.
 *
 * @author Jaroslav Tulach
 */
public class RunInEventQueueTest extends NbTestCase {

    public RunInEventQueueTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
        assertTrue("setUp should be run in AWT thread.", SwingUtilities.isEventDispatchThread());
    }

    protected void tearDown () throws Exception {
        assertTrue("tearDown should be run in AWT thread.", SwingUtilities.isEventDispatchThread());
    }
    
    protected boolean runInEQ () {
        return true;
    }

    public void testRunsInAWTThread () {
        assertTrue ("We are in Event Thread", SwingUtilities.isEventDispatchThread ());
    }
    

    
}
