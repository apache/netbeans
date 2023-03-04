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
package org.netbeans.modules.timers;

import java.lang.ref.WeakReference;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hrebejk
 */
public class InstanceWatcherTest extends NbTestCase {

    public InstanceWatcherTest(String testName) {
    super(testName);
    }

    public void testFiring() throws Exception {
        System.out.println("addChangeListener");

        QueueListener listener = new QueueListener();
        InstanceWatcher iw = new InstanceWatcher();
        
        iw.addChangeListener(listener);
        
        Integer ts1 = new Integer( 20 );
        iw.add( ts1 );
               
        WeakReference tmp; // For forcing GC
        
        tmp = new WeakReference<Object>( new Object() );
        assertGC( "", tmp );
        
        assertEquals( "There should be no change in the queue", 0, listener.changeCount );
        
        ts1 = null; // Remove hard reference
        
        tmp = new WeakReference<Object>( new Object() );        
        assertGC( "", tmp ); // Do garbage collect
        
        assertEquals( "There should be one change in the queue", 1, listener.changeCount );
                
    }

    
    private static class QueueListener implements ChangeListener {
        
        int changeCount;
        
        public void stateChanged( ChangeEvent e ) {
            changeCount ++;
        }
        
    }
    
}
