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

package org.netbeans.modules.bugtracking.spi;

import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbPreferences;

/**
 *
 * @author tomas
 */
public class RepositoryInfoTest extends NbTestCase {
    

    public RepositoryInfoTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
    }

    @Override
    protected void tearDown() throws Exception {   
    }

    public void testCreate() {
        RepositoryInfo info = 
            new RepositoryInfo(
                "id", 
                "cid", 
                "http://url", 
                "displayName", 
                "tooltip", 
                "user", 
                "httpUser", 
                "password".toCharArray(), 
                "httpPassword".toCharArray());
        
        assertEquals("id", info.getID());
        assertEquals("cid", info.getConnectorId()); 
        assertEquals("http://url", info.getUrl());
        assertEquals("displayName", info.getDisplayName());
        assertEquals("tooltip", info.getTooltip());
        assertEquals("user", info.getUsername());
        assertEquals("httpUser", info.getHttpUsername());
        assertEquals("password", new String(info.getPassword()));
        assertEquals("httpPassword", new String(info.getHttpPassword()));
    }
    
    public void testValues() {
        RepositoryInfo info = 
            new RepositoryInfo(
                "id", 
                "cid", 
                "http://url", 
                "displayName", 
                "tooltip", 
                "user", 
                "httpUser", 
                "password".toCharArray(), 
                "httpPassword".toCharArray());
        
        info.putValue("key1", "value1");
        info.putValue("key2", "value2");
        assertEquals("value1", info.getValue("key1"));
        assertEquals("value2", info.getValue("key2"));
    }
    
    public void testStoreAndRead() {
        RepositoryInfo storedInfo = 
            new RepositoryInfo(
                "id", 
                "cid", 
                "http://url", 
                "displayName", 
                "tooltip", 
                "user", 
                "httpUser", 
                "password".toCharArray(), 
                "httpPassword".toCharArray());
        storedInfo.putValue("key1", "value1");
        storedInfo.putValue("key2", "value2");
        
        Preferences pref = NbPreferences.forModule(RepositoryInfo.class);
        storedInfo.store(pref, "key");
        RepositoryInfo readInfo = RepositoryInfo.read(pref, "key");
        
        assertEquals(storedInfo.getID(), readInfo.getID());
        assertEquals(storedInfo.getConnectorId(), readInfo.getConnectorId()); 
        assertEquals(storedInfo.getUrl(), readInfo.getUrl());
        assertEquals(storedInfo.getDisplayName(), readInfo.getDisplayName());
        assertEquals(storedInfo.getTooltip(), readInfo.getTooltip());
        assertEquals(storedInfo.getUsername(), readInfo.getUsername());
        assertEquals(storedInfo.getHttpUsername(), readInfo.getHttpUsername());
        assertEquals(new String(storedInfo.getPassword()), new String(readInfo.getPassword()));
        assertEquals(new String(storedInfo.getHttpPassword()), new String(readInfo.getHttpPassword()));
        assertEquals(storedInfo.getValue("key1"), readInfo.getValue("key1"));
        assertEquals(storedInfo.getValue("key2"), readInfo.getValue("key2"));
    }
    
    public void testNoPassword() {
        RepositoryInfo info = 
            new RepositoryInfo(
                "id", 
                "cid", 
                "http://url", 
                "displayName", 
                "tooltip", 
                "user", 
                "httpUser", 
                null, 
                null);        
        assertEquals(0, info.getHttpPassword().length);
        assertEquals(0, info.getHttpPassword().length);
        
        info = 
            new RepositoryInfo(
                "id", 
                "cid", 
                "http://url", 
                "displayName", 
                "tooltip", 
                "user", 
                "httpUser", 
                new char[0], 
                new char[0]);        
        assertEquals(0, info.getHttpPassword().length);
        assertEquals(0, info.getHttpPassword().length);
    }
}
