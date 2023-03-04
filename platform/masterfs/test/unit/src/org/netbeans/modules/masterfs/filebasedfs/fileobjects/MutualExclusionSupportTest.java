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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;

public class MutualExclusionSupportTest extends NbTestCase {

    public MutualExclusionSupportTest(String testName) {
        super(testName);
    }

    public void testAddResource() throws Exception  {
        String key = "key";
        MutualExclusionSupport<String> mes = new MutualExclusionSupport<String>();
        
        MutualExclusionSupport<String>.Closeable rem1 = mes.addResource(key, true);
        MutualExclusionSupport<String>.Closeable rem2 = mes.addResource(key, true);
        
        try {
            MutualExclusionSupport<String>.Closeable rem3 = mes.addResource(key, false);
            fail ();
        } catch (IOException iox) {}
        
        rem1.close();
        
        try {
            MutualExclusionSupport<String>.Closeable rem3 = mes.addResource(key, false);
            fail ();
        } catch (IOException iox) {}
        
        rem2.close();        
        
        try {
            MutualExclusionSupport<String>.Closeable rem3 = mes.addResource(key, false);
            rem3.close();
        } catch (IOException iox) {
            fail ();
        }                        
        
        MutualExclusionSupport<String>.Closeable rem4 = mes.addResource(key, false);

        try {
            rem1 = mes.addResource(key, true);
            fail ();            
        } catch (IOException iox) {
        }                        
        
        try {
            MutualExclusionSupport<String>.Closeable rem3 = mes.addResource(key, false);
            fail ();
        } catch (IOException iox) {
        }                        
        
        rem4.close();
        rem1 = mes.addResource(key, true);
        rem1.close();
        MutualExclusionSupport<String>.Closeable rem3 = mes.addResource(key, false);
        rem3.close();
    }
    
}
