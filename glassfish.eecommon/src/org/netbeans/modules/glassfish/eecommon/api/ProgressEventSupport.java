// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
//</editor-fold>

package org.netbeans.modules.glassfish.eecommon.api;

import org.netbeans.modules.glassfish.eecommon.Status;
import java.util.Vector;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;

import org.openide.util.RequestProcessor;

/**
 * This is a utility class that can be used by ProgressObject's,
 * You can use an instance of this class as a member field
 * of your ProgressObject and delegate various work to it.
 */
public class ProgressEventSupport {

    /** Source object. */
    private Object obj;
    
    private Vector<ProgressListener> listeners;
    
    private DeploymentStatus status;
    
    private TargetModuleID tmID;
    
    /**
     * Constructs a <code>ProgressEventSupport</code> object.
     *
     * @param o Source for any events.
     */
    public ProgressEventSupport (Object o) {
        if (o == null) {
            throw new NullPointerException ();
        }
        obj = o;
    }
    
    /** Add a ProgressListener to the listener list. */
    public synchronized void addProgressListener (ProgressListener lsnr) {
        boolean notify = false;
        if (listeners == null) {
            listeners = new java.util.Vector<ProgressListener>();
        }
        listeners.addElement(lsnr);
        if (status != null && !status.isRunning ()) {
            notify = true;
        }
        if (notify) {
            // not to miss completion event
            RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    fireHandleProgressEvent (tmID, status);
                }
            });
        }
    }
    
    /** Remove a ProgressListener from the listener list. */
    public synchronized void removeProgressListener (ProgressListener lsnr) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(lsnr);
    }

    /** Report event to any registered listeners. */
    public void fireHandleProgressEvent (TargetModuleID targetModuleID,
                                         DeploymentStatus sCode) {
        ProgressEvent evt = new ProgressEvent (obj, targetModuleID, sCode);
        status = sCode;
        tmID = targetModuleID;
        
	Vector targets = null;
	synchronized (this) {
	    if (listeners != null) {
	        targets = (Vector) listeners.clone();
	    }
	}

	if (targets != null) {
	    for (int i = 0; i < targets.size(); i++) {
	        ProgressListener target = (ProgressListener)targets.elementAt(i);
	        target.handleProgressEvent (evt);
	    }
	}
    }
    
    /** Returns last DeploymentStatus notified by {@@link fireHandleProgressEvent}
     */
    public synchronized DeploymentStatus getDeploymentStatus () {
        return status;
    }

    public synchronized void clearProgressListener() {
        listeners = null;
    }
    
    public static DeploymentStatus createStatus(ActionType type, CommandType command, String msg, StateType state) {
        return new Status(type,command,msg,state);
    }
}

