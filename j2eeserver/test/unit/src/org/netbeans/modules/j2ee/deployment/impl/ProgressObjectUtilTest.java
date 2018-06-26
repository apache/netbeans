/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
