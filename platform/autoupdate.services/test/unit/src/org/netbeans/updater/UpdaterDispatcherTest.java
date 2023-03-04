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

package org.netbeans.updater;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
         
/**
 * 
 * @author Jiri Rechtacek
 */
public class UpdaterDispatcherTest extends NbTestCase {

    /** Default constructor.
     * @param testName name of particular test case
    */
    public UpdaterDispatcherTest(String testName) {
        super(testName);
    }
    
    File cluster = null;
    
    /** Called before every test case. */
    @Override
    public void setUp() throws IOException {
        cluster = getWorkDir ();
    }

    @Override
    protected void tearDown () throws Exception {
        clearWorkDir ();
        super.tearDown ();
    }
    
    public void testTouchLastModified () throws InterruptedException {
        File stamp = new File (cluster, UpdaterDispatcher.LAST_MODIFIED);
        assertFalse (stamp.toString () + " doesn't exist before first touch.", stamp.exists ());
        UpdaterDispatcher.touchLastModified (cluster);
        assertTrue (stamp.toString () + " exists after touch.", stamp.exists ());
        long firstTouch = stamp.lastModified ();
        assertTrue ("Was touched", firstTouch <= System.currentTimeMillis ());
        stamp = new File (cluster, UpdaterDispatcher.LAST_MODIFIED);
        Thread.sleep (5000);
        UpdaterDispatcher.touchLastModified (cluster);
        assertTrue ("Was touched again", firstTouch < stamp.lastModified ());
    }
}
