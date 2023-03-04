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

import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;

import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;

/**
 * @author  Petr Nejedly, adapted to test by Jaroslav Tulach
 */
@RandomlyFails // NB-Core-Build #1847
public class SimpleProxyLookupSpeedIssue42244Test extends NbTestCase {

    public SimpleProxyLookupSpeedIssue42244Test (String name) {
        super (name);
    }

    public void testCompareTheSpeed () {
        String content1 = "String1";
        String content2 = "String2";
        
        Lookup fixed1 = Lookups.singleton(content1);
        Lookup fixed2 = Lookups.singleton(content2);
        
        MyProvider provider = new MyProvider();
        provider.setLookup(fixed1);
        
        Lookup top = Lookups.proxy(provider);

        Lookup.Result<String> r0 = top.lookupResult(String.class);
        r0.allInstances();

        long time = System.currentTimeMillis();
        top.lookupAll(String.class);
        long withOneResult = System.currentTimeMillis() - time;

     
        Set<Object> results = new HashSet<Object>();
        for (int i=0; i<10000; i++) {
            Lookup.Result<String> res = top.lookupResult(String.class);
            results.add (res);
            res.allInstances();
        }
        
        provider.setLookup(fixed2);

        time = System.currentTimeMillis();
        top.lookupAll(String.class);
        long withManyResults = System.currentTimeMillis() - time;
        
        // if the measurement takes less then 10ms, pretend 10ms
        if (withManyResults < 10) {
            withManyResults = 10;
        }
        if (withOneResult < 10) {
            withOneResult = 10;
        }

        if (withManyResults >= 10 * withOneResult) {
            fail ("With many results the test runs too long.\n With many: " + withManyResults + "\n With one : " + withOneResult);
        }
    }
    
    private static class MyProvider implements Lookup.Provider {
        private Lookup lookup;
        public Lookup getLookup() {
            return lookup;
        }
        
        void setLookup(Lookup lookup) {
            this.lookup = lookup;
        }
    }
    
}
