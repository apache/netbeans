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

package org.netbeans.modules.j2ee.deployment.impl;

import java.util.concurrent.TimeoutException;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sherold
 */
public class ProgressObjectUtilTest extends NbTestCase {
    
    /** Creates a new instance of ProgressObjectUtilTest */
    public ProgressObjectUtilTest(String testName) {
        super(testName);
    }
    
    public void testTrackProgressObject() throws Exception {
        ProgressUI ui = new ProgressUI("alreadyFinishedSuccessfully", false);
        ui.start();
        ProgressObject po = alreadyFinishedSuccessfully();
        assertTrue(ProgressObjectUtil.trackProgressObject(ui, po, 1000));
        
        ui = new ProgressUI("alreadyFinishedWithFailure", false);
        ui.start();
        po = alreadyFinishedWithFailure();
        assertFalse(ProgressObjectUtil.trackProgressObject(ui, po, 1000));
        
        ui = new ProgressUI("willNeverFinish", false);
        ui.start();
        po = willNeverFinish();
        try {
            assertFalse(ProgressObjectUtil.trackProgressObject(ui, po, 1000));
            fail("the task should time out");
        } catch (TimeoutException e) {
            // exception should be thrown
        }
        
        ui = new ProgressUI("willFinishSuccessfully", false);
        ui.start();
        po = willFinishSuccessfully();
        assertTrue(ProgressObjectUtil.trackProgressObject(ui, po, 5000));
        
        ui = new ProgressUI("willFail", false);
        ui.start();
        po = willFail();
        assertFalse(ProgressObjectUtil.trackProgressObject(ui, po, 5000));
        
        ui = new ProgressUI("willReleaseAndFail", false);
        ui.start();
        po = willReleaseAndFail();
        assertFalse(ProgressObjectUtil.trackProgressObject(ui, po, 5000));
    }
    
    private ProgressObject neverEndingTask() {
        return null;
    }
    
    private ProgressObject alreadyFinishedSuccessfully() {
        ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "finished", StateType.COMPLETED));
        return po;
    }
    
    private ProgressObject alreadyFinishedWithFailure() {
        ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "failed", StateType.FAILED));
        return po;
    }
    
    private ProgressObject willNeverFinish() {
        ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "running", StateType.RUNNING));
        return po;
    }
    
    private ProgressObject willFinishSuccessfully() {
        final ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "running", StateType.RUNNING));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "finished", StateType.COMPLETED));
            }
        }, 1000);
        return po;
    }
    
    private ProgressObject willFail() {
        final ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "running", StateType.RUNNING));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "failed", StateType.FAILED));
            }
        }, 1000);
        return po;
    }
    
    private ProgressObject willReleaseAndFail() {
        final ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "running", StateType.RUNNING));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "released", StateType.RELEASED));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "failed", StateType.FAILED));
            }
        }, 1000);
        return po;
    }
    
    private static class ProgressObjectImpl implements ProgressObject {
        
        private final ProgressEventSupport progressEventSupport = new ProgressEventSupport(this);
        
        public ProgressEventSupport getProgressEventSupport() {
            return progressEventSupport;
        }       

        public DeploymentStatus getDeploymentStatus() {
            return progressEventSupport.getDeploymentStatus();
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isCancelSupported() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void cancel() throws OperationUnsupportedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isStopSupported() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void stop() throws OperationUnsupportedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addProgressListener(ProgressListener arg0) {
            progressEventSupport.addProgressListener(arg0);
        }

        public void removeProgressListener(ProgressListener arg0) {
            progressEventSupport.removeProgressListener(arg0);
        }
    }
}
