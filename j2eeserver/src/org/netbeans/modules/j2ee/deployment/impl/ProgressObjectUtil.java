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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;

/**
 * A utility class for asynchronous monitoring of progress objects.
 * 
 * @author sherold
 */
public class ProgressObjectUtil {
    
    private static final Logger LOGGER = Logger.getLogger(ProgressObjectUtil.class.getName());
    
    private ProgressObjectUtil() {
    }
    
    /**
     * Waits till the progress object is in final state or till the timeout runs out.
     *
     * @param ui progress ui which will be notified about progress object changes .
     * @param po progress object which will be tracked.
     * @param timeout timeout in millis. Zero timeout means unlimited timeout.
     *
     * @return true if the progress object completed successfully, false otherwise.
     *         This is a workaround for issue 82428.
     * 
     * @throws TimedOutException when the task times out.
     */
    public static boolean trackProgressObject(ProgressUI ui, final ProgressObject po, long timeout) throws TimeoutException {
        assert po != null;
        assert ui != null;
        // There may be a problem if server reports RELEASED as final state
        // however there is no way how to handle it here, because some POs has
        // RELEASED as initial state
        final AtomicBoolean completed = new AtomicBoolean();
        ui.setProgressObject(po);
        try {
            final CountDownLatch progressFinished = new CountDownLatch(1);
            ProgressListener listener = new ProgressListener() {
                @Override
                public void handleProgressEvent(ProgressEvent progressEvent) {
                    DeploymentStatus status = progressEvent.getDeploymentStatus();
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Received progress state {0} from {1}",
                                new Object[] {status.getState(), progressEvent});
                    }
                    if (status.isCompleted()) {
                        completed.set(true);
                    }
                    if (status.isCompleted() || status.isFailed()) {
                        progressFinished.countDown();
                    }
                }
            };
            LOGGER.log(Level.FINEST, "Adding progress listener {0}", listener);
            po.addProgressListener(listener);
            try {
                // the completion event might have arrived before the progress listener 
                // was registered, wait only if not yet finished
                DeploymentStatus status = po.getDeploymentStatus();
                if (!status.isCompleted() && !status.isFailed()) {
                    try {
                        if (timeout == 0) {
                            progressFinished.await();
                        } else {
                            boolean passed = progressFinished.await(timeout, TimeUnit.MILLISECONDS);
                            if (!passed) {
                                throw new TimeoutException();
                            }
                        }
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.INFO, null, e);
                        Thread.currentThread().interrupt();
                        return false;
                    }
                } else if (status.isCompleted()) {
                    completed.set(true);
                }
            } finally {
                po.removeProgressListener(listener);
            }
        } finally {
            ui.setProgressObject(null);
        }
        return completed.get();
    }
    
}
