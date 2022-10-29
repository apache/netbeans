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

package org.netbeans.modules.tomcat5.progress;

import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import junit.framework.TestCase;

/**
 *
 * @author Petr Hejl
 */
public class ProgressListenerImpl implements ProgressListener {

    private ProgressEvent[] events;

    private Object expectedSource;

    private int counter;

    public ProgressListenerImpl(ProgressEvent[] events, Object expectedSource) {
        this.events = events;
        this.expectedSource = expectedSource;
    }

    @Override
    public void handleProgressEvent(ProgressEvent evt) {
        if (counter > events.length) {
            TestCase.fail("Event arrive - unregistered listener");
        }

        ProgressEvent toCompare = events[counter++];

        TestCase.assertEquals(expectedSource, evt.getSource());
        TestCase.assertEquals(toCompare.getTargetModuleID(), evt.getTargetModuleID());
        TestCase.assertEquals(toCompare.getDeploymentStatus().getAction(), evt.getDeploymentStatus().getAction());
        TestCase.assertEquals(toCompare.getDeploymentStatus().getCommand(), evt.getDeploymentStatus().getCommand());
        TestCase.assertEquals(toCompare.getDeploymentStatus().getMessage(), evt.getDeploymentStatus().getMessage());
        TestCase.assertEquals(toCompare.getDeploymentStatus().getState(), evt.getDeploymentStatus().getState());
    }
}
