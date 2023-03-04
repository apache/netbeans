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

package org.netbeans.modules.tomcat5.progress;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class ProgressEventSupportTest extends NbTestCase {
    
    public ProgressEventSupportTest(String testName) {
        super(testName);
    }
    
    public void testListeners() {
        ProgressEventSupport pes = new ProgressEventSupport(this);
        
        ProgressEvent evt1 = new ProgressEvent("fake", new TargetModuleIDImpl("test","test", "test", "http://localhost"),
                new Status(ActionType.EXECUTE, CommandType.START, "test1", StateType.RUNNING));
        ProgressEvent evt2 = new ProgressEvent("fake", new TargetModuleIDImpl("test","test", "test", "http://localhost"),
                new Status(ActionType.CANCEL, CommandType.DISTRIBUTE, "test2", StateType.RUNNING));
        
        ProgressListener listener1 = new ProgressListenerImpl(new ProgressEvent[] {evt1, evt2 }, this);
        ProgressListener listener2 = new ProgressListenerImpl(new ProgressEvent[] {evt1, evt2 }, this);
        
        pes.addProgressListener(listener1);
        pes.addProgressListener(listener2);
        
        pes.fireHandleProgressEvent(evt1.getTargetModuleID(), evt1.getDeploymentStatus());
        pes.fireHandleProgressEvent(evt2.getTargetModuleID(), evt2.getDeploymentStatus());
        
        pes.removeProgressListener(listener1);
        pes.removeProgressListener(listener2);
        
        pes.fireHandleProgressEvent(evt1.getTargetModuleID(), evt1.getDeploymentStatus());
    }
    
}
