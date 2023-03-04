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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import org.openide.util.Parameters;

/**
 * This is a utility class that can be used by ProgressObject's,
 * You can use an instance of this class as a member field
 * of your ProgressObject and delegate various work to it.
 *
 * @author  Radim Kubacki
 */
public class ProgressEventSupport {

    /** Source object. */
    private Object obj;

    private java.util.Vector listeners;

    private DeploymentStatus status;

    /**
     * Constructs a <code>ProgressEventSupport</code> object.
     *
     * @param source Source for any events.
     */
    public ProgressEventSupport (Object source) {
        Parameters.notNull("source", source);

        obj = source;
    }

    /** Add a ProgressListener to the listener list. */
    public synchronized void addProgressListener (ProgressListener lsnr) {
        if (listeners == null) {
            listeners = new java.util.Vector();
        }
        listeners.addElement(lsnr);
    }

    /** Remove a ProgressListener from the listener list. */
    public synchronized void removeProgressListener (ProgressListener lsnr) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(lsnr);
    }

    /** Report event to any registered listeners. */
    public void fireHandleProgressEvent (TargetModuleID targetModuleID, DeploymentStatus sCode) {
        Logger.getLogger(ProgressEventSupport.class.getName()).log(Level.FINE, "progress event from {0} status {1}", new Object[]{obj, sCode}); // NOI18N
        synchronized (this) {
            status = sCode;
        }
        ProgressEvent evt = new ProgressEvent (obj, targetModuleID, sCode);
	java.util.Vector targets = null;
	synchronized (this) {
	    if (listeners != null) {
	        targets = (java.util.Vector) listeners.clone();
	    }
	}

	if (targets != null) {
	    for (int i = 0; i < targets.size(); i++) {
	        ProgressListener target = (ProgressListener)targets.elementAt(i);
	        target.handleProgressEvent (evt);
	    }
	}
    }

    /** Returns last DeploymentStatus notified by {@link fireHandleProgressEvent}
     */
    public synchronized DeploymentStatus getDeploymentStatus () {
        return status;
    }
}
