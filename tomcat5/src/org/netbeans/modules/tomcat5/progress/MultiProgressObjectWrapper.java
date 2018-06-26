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


package org.netbeans.modules.tomcat5.progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.openide.util.Parameters;

/**
 * MultiProgressObjectWrapper wraps multiple progress objects into a single one.
 * <p>
 * If all wrapped objects are in COMPLETED or FAILED state the state object
 * will be changed to:
 * <ul>
 *     <li>{@link StateType.COMPLETED} if all wrapped objects reached the COMPLETED state
 *     <li>{@link StateType.FAILED} if any of wrapped objects reached the FAILED state
 * </ul>
 * <p>
 * Note that all wrapped objects have to be in COMPLETED or FAILED state to
 * invoke the change of the state of this object. However this does not mean
 * the events from the wrapped objects are not propagated to the listeners of
 * this object.
 * <p>
 * The behaviour of {@link StateType.RELEASED} is quite unsure from JSR-88.
 * This implementation does not consider it as end state of the ProgressObject.
 *
 * @author herolds
 * @author Petr Hejl
 */
public class MultiProgressObjectWrapper implements ProgressObject, ProgressListener {

    private final ProgressEventSupport pes = new ProgressEventSupport(this);

    private final ProgressObject[] progressObjects;

    private String message = ""; // NOI18N

    private StateType state = StateType.RUNNING;

    /** Creates a new instance of MultipleOpsProgressObject */
    public MultiProgressObjectWrapper(ProgressObject[] objects) {
        Parameters.notNull("progObjs", state);

        if (objects.length == 0) {
            throw new IllegalArgumentException("At least one progress object must be passed."); // NOI18N
        }

        progressObjects = new ProgressObject[objects.length];
        System.arraycopy(objects, 0, progressObjects, 0, objects.length);

        for (int i = 0; i < objects.length; i++) {
            ProgressObject po = objects[i];
            // XXX unsafe publication
            po.addProgressListener(this);
        }

        updateState(null);
    }

    /** JSR88 method. */
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null; // PENDING
    }

    /** JSR88 method. */
    public synchronized DeploymentStatus getDeploymentStatus() {
        DeploymentStatus ds = progressObjects[0].getDeploymentStatus();
        // all deployment objects are supposed to be of the same action and command type
        return new Status(ds.getAction(), ds.getCommand(), message, state);
    }

    /** JSR88 method. */
    public TargetModuleID[] getResultTargetModuleIDs() {
        List<TargetModuleID> returnVal = new ArrayList<TargetModuleID>();
        for (int i = 0; i < progressObjects.length; i++) {
            ProgressObject po = progressObjects[i];
            if (po.getDeploymentStatus().isCompleted()) {
                returnVal.addAll(Arrays.asList(po.getResultTargetModuleIDs()));
            }
        }
        return returnVal.toArray(new TargetModuleID[returnVal.size()]);
    }

    /** JSR88 method. */
    public boolean isCancelSupported() {
        return false;
    }

    /** JSR88 method. */
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("cancel not supported in Tomcat deployment"); // NOI18N
    }

    /** JSR88 method. */
    public boolean isStopSupported() {
        return false;
    }

    /** JSR88 method. */
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("stop not supported in Tomcat deployment"); // NOI18N
    }

    /** JSR88 method. */
    public void addProgressListener(ProgressListener l) {
        pes.addProgressListener(l);
    }

    /** JSR88 method. */
    public void removeProgressListener(ProgressListener l) {
        pes.removeProgressListener(l);
    }

    /**
     * Handles the progress events from wrapped objects.
     */
    public void handleProgressEvent(ProgressEvent progressEvent) {
        updateState(progressEvent.getDeploymentStatus().getMessage());

        pes.fireHandleProgressEvent(progressEvent.getTargetModuleID(), progressEvent.getDeploymentStatus());
    }

    private synchronized void updateState(String receivedMessage) {
        if (state == StateType.COMPLETED || state == StateType.FAILED) {
            return;
        }

        boolean completed = true;
        boolean failed = false;

        for (ProgressObject progress : progressObjects) {
            DeploymentStatus status = progress.getDeploymentStatus();

            if (status == null || (!status.isCompleted() && !status.isFailed())) {
                completed = false;
                break;
            }

            if (status.isFailed()) {
                failed = true;
            }
        }

        if (completed) {
            state = failed ? StateType.FAILED : StateType.COMPLETED;
            message = receivedMessage == null ? "" : receivedMessage;
        }
    }
}
