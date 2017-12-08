/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.execution;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import junit.framework.TestCase;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/** A piece of the test compatibility suite for the execution APIs.
 *
 * @author Jaroslav Tulach
 */
public class ExecutionEngineHid extends TestCase {
    
    public ExecutionEngineHid(String testName) {
        super(testName);
    }
    
    public void testGetDefault() {
        ExecutionEngine result = ExecutionEngine.getDefault();
        assertNotNull(result);
    }
    
    public void testExecuteIsNonBlocking() throws Exception {
        class Block implements Runnable {
            public boolean here;
            
            
            public synchronized void run() {
                here = true;
                notifyAll();
                try {
                    wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            
            public synchronized void waitForRun() throws InterruptedException {
                while(!here) {
                    wait();
                }
                notifyAll();
            }
        }
        Block block = new Block();
        
        
        ExecutionEngine instance = ExecutionEngine.getDefault();
        ExecutorTask result = instance.execute("My task", block, InputOutput.NULL);
        
        assertFalse("Of course it is not finished, as it is blocked", result.isFinished());
        block.waitForRun();
        
        int r = result.result();
        assertEquals("Default result is 0", 0, r);
    }
    
}
