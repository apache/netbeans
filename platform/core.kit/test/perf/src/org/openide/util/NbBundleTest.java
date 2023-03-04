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

package org.openide.util;

import org.openide.utildata.UtilClass;
import org.netbeans.performance.Benchmark;
import java.util.ResourceBundle;

public class NbBundleTest extends Benchmark {

    public NbBundleTest(String name) {
        super( name, new Integer[] {
            new Integer(1), new Integer(10), new Integer(100), new Integer(1000)
        });
    }

    private String[] keys;

    protected void setUp() {
        int count = getIterationCount();
        int param = ((Integer)getArgument()).intValue();
        keys = new String[param];
        for( int i=0; i<param; i++ ) {
            keys[i] = "MSG_BundleTest_" + i;
        }
    }
    
    protected void tearDown() {
        keys=null;
    }
        
    public void testGetMessageUsingClass() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                NbBundle.getMessage( UtilClass.class, keys[number] );
            }
        }
    }    

    public void testGetMessageUsingClassFullBrand() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
	NbBundle.setBranding("brand1");

        while( count-- > 0 ) {
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                NbBundle.getMessage( UtilClass.class, keys[number] );
            }
        }
	NbBundle.setBranding(null);
    }    

    public void testGetMessageUsingEmptyBrand() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
	NbBundle.setBranding("brand2");

        while( count-- > 0 ) {
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                NbBundle.getMessage( UtilClass.class, keys[number] );
            }
        }
	
	NbBundle.setBranding(null);
    }    

    private ResourceBundle bundle;
    private synchronized ResourceBundle getBundle() {
        if( bundle == null ) {
            bundle = NbBundle.getBundle( UtilClass.class );
        }
        return bundle;
    }
    
    private synchronized void clearBundle() {
        bundle = null;
    }
    
    public void testGetMessageUsingLazyCache() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                getBundle().getString( keys[number] );
            }
            clearBundle();
        }
    }    

    public void testGetMessageUsingCachedBundle() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
            ResourceBundle bundle = NbBundle.getBundle( UtilClass.class );
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                bundle.getString( keys[number] );
            }
        }
    }

    public void testGetMessageUsingCachedBundleFullBrand() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
	NbBundle.setBranding("brand1");

        while( count-- > 0 ) {
            ResourceBundle bundle = NbBundle.getBundle( UtilClass.class );
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                bundle.getString( keys[number] );
            }
        }
	NbBundle.setBranding(null);
    }


    public void testGetMessageUsingCachedBundleEmptyBrand() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
	NbBundle.setBranding("brand2");

        while( count-- > 0 ) {
            ResourceBundle bundle = NbBundle.getBundle( UtilClass.class );
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                bundle.getString( keys[number] );
            }
        }
	NbBundle.setBranding(null);
    }
    
    public static void main(String[] args) {
	simpleRun( NbBundleTest.class );
    }
}
