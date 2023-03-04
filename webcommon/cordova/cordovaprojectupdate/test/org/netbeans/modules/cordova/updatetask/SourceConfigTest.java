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
package org.netbeans.modules.cordova.updatetask;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jan Becicka
 */
public class SourceConfigTest {
    
    public SourceConfigTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getName method, of class Config.
     */
    @Test
    public void testOut() throws Exception{
        SourceConfig instance = new SourceConfig(SourceConfig.class.getResourceAsStream("config.xml"));
        assertEquals(instance.getAccess(), "*");
        assertEquals(instance.getName(), "$name");
        assertEquals(instance.getAuthor().trim(), "$author");
        assertEquals(instance.getDescription().trim(), "$description");
        
        instance.setAuthor("be");
        instance.setName("aaa");
        instance.setDescription("desc");
        
        assertEquals(instance.getIcon("android"), "res/icon/android/icon-96-xhdpi.png");
        assertEquals(instance.getPreference("target-device"), "universal");
        assertEquals(instance.getSplash("ios", 320, 480), "res/screen/ios/screen-iphone-portrait.png");
        assertEquals(instance.getId(), "$id");
        
        instance.setSplash("ios", 32, 48, "test");
        instance.printDocument(System.out);
    }
    
}