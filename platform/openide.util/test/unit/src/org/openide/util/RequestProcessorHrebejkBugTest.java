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

import java.util.concurrent.CountDownLatch;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.RequestProcessor;



public class RequestProcessorHrebejkBugTest extends NbTestCase {

    public RequestProcessorHrebejkBugTest(String name) {
        super(name);
    }
    
    public void testBug() throws Exception {
        RequestProcessor rp = new RequestProcessor("TestProcessor", 3, true); 
        
        R1 r1 = new R1();
        R2 r2 = new R2(r1);
        
        r1.submit(rp);

        r1.in.await();
        
        RequestProcessor.Task t = rp.post(r2);
        
        r1.goOn.countDown();
        
        t.waitFinished(); 
        
        if (r1.count != 1) {
            throw r1.wrong;
        }
    }
    
    
    private static class R1 implements Runnable {
        private volatile Exception wrong;
        private volatile int count;
        private RequestProcessor.Task task;
        final CountDownLatch in = new CountDownLatch(1);
        final CountDownLatch goOn = new CountDownLatch(1);

        @Override
        public void run() {
            count ++;
            if (wrong == null) {
                wrong = new Exception("First call");
            } else {
                wrong = (Exception) wrong.initCause(new Exception("Next call " + count));
            }
            in.countDown();
            try {
                goOn.await();
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
            long until = System.currentTimeMillis() + 1000;
            for (;;) {
                long missing = until - System.currentTimeMillis();
                if (missing <= 0) {
                    break;
                }
                try {
                    Thread.sleep(missing);
                } catch (InterruptedException ex) {
                    // OK, will be interrupted likely
                }
            }
        }
        
        void submit(RequestProcessor rp) {
            task = rp.post(this);
        }
    
        void cancel() {
            task.cancel();
        }
                
        void check() {
            if ( !task.isFinished() ) {
                task.waitFinished();
            }
        }
            
        
    }
    
    
    private static class R2 implements Runnable {
        
        R1 r1;
        
        R2( R1 r1 ) {
            this.r1 = r1;
        }
        
        @Override
        public void run() {
            
            r1.cancel();
            r1.check();
            
        }
        
    }
    
}

