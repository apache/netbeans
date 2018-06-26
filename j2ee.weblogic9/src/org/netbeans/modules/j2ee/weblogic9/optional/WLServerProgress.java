/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.optional;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentStatus;

/**
 *
 * @author Petr Hejl
 */
public final class WLServerProgress implements ProgressObject {

    /**
     * Progress events source
     */
    private final Object source;

    private final List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();

    /**
     * Current startus of the startup/shutdown process
     */
    private DeploymentStatus deploymentStatus;

    /**
     * Creates a new instance of WSServerProgress. The source supplied will
     * be used as the source for all the events. Ususally it is the parent
     * WSStartServerObject
     *
     * @param source source of events
     */
    public WLServerProgress(Object source) {
        this.source = source;
    }

    /**
     * Sends a startup event to the listeners.
     *
     * @param state the new state of the startup process
     * @param message the attached string message
     */
    public void notifyStart(StateType state, String message) {
        notify(new WLDeploymentStatus(ActionType.EXECUTE, CommandType.START, state, message));
    }

    /**
     * Sends a shutdown event to the listeners.
     *
     * @param state the new state of the shutdown process
     * @param message the attached string message
     */
    public void notifyStop(StateType state, String message) {
        notify(new WLDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, state, message));
    }

    /**
     * Notifies the listeners of the new process status.
     *
     * @param deploymentStatus the new status of the startup/shutdown
     *      process
     */
    public void notify(DeploymentStatus deploymentStatus) {
        // construct a new progress event from the source and the supplied
        // new process status
        ProgressEvent evt = new ProgressEvent(source, null, deploymentStatus);

        // update the saved process status
        this.deploymentStatus = deploymentStatus;

        for (ProgressListener listener : listeners) {
            listener.handleProgressEvent(evt);
        }
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }

    /**
     * Removes the registered listener.
     *
     * @param progressListener the listener to be removed
     */
    public void removeProgressListener(ProgressListener progressListener) {
        listeners.remove(progressListener);
    }

    /**
     * Adds a new listener.
     *
     * @param progressListener the listener to be added
     */
    public void addProgressListener(ProgressListener progressListener) {
        listeners.add(progressListener);
    }

    /**
     * Returns the current state of the startup/shutdown process.
     *
     * @return current state of the process
     */
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[] {};
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public boolean isStopSupported() {
        return false;
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Operation not supported"); // NOI18N
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public boolean isCancelSupported() {
        return false;
    }

    /**
     * A dummy implementation of the ProgressObject method, since this
     * method is not used anywhere, we omit the reasonable implementation.
     */
    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Operation not supported"); // NOI18N
    }
}
