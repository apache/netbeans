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

package org.openide.util.lookup;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Tests the fix of #237149
 *
 * @author stan
 */
public class LookupListenerListTest extends NbTestCase {
        
    public LookupListenerListTest(String name) {
        super(name);
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    @Override
    public void setUp() {
    }
    
    @After
    @Override
    public void tearDown() {
    }

    /**
     * Test of remove method, of class LookupListenerList.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        LookupListener l = new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        LookupListener l2 = new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        LookupListenerList instance = new LookupListenerList();
        instance.add(l);
        instance.add(l2);
        
        instance.remove(l);
        instance.remove(l2);
        //don't throw any exception
        instance.remove(l);
    }
    
}
