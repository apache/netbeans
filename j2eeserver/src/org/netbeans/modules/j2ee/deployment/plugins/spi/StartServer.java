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


package org.netbeans.modules.j2ee.deployment.plugins.spi;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;

/**
 * Server lifecycle services from the IDE.  J2eeserver will use these
 * services to automatically start or stop admin server and managed (virtual)
 * target servers (in debug mode) during deployment or debugging execution.
 *
 * @see OptionalDeploymentManagerFactory
 * @author George FinKlang
 * @author  nn136682
 * @version 1.0
 */
public abstract class StartServer {
    
    /**
     * Returns true if the admin server is also the given target server (share the same vm).
     * Start/stopping/debug apply to both servers.  When the given target server is null,
     * service should return true when admin server is also some target.
     * @param target the target server in question; could be null.
     * @return true when admin is also target server
     */
    public abstract boolean isAlsoTargetServer(Target target);
    
    /**
     * Returns true if the admin server can be started through this spi.
     */
    public abstract boolean supportsStartDeploymentManager();
    
    /**
     * Returns if the plugin can start/stop single target servers.
     * This should be overwritten as needed.
     * @param target the target server in question, could be null in which
     * case the answer should probably be false.
     * @return true if this plugin can currently handle state management for 
     * the specific target.
     * @since 1.6
     */
    public boolean supportsStartTarget(Target target) {
        return false;
    }
    
    /**
     * Can be the specified target server started in the debug mode? If the 
     * target is also an admin server can be the admin server started in the
     * debug mode?
     *
     * @param  target the target server in question, null implies the case where 
     *         target is also an admin server.
     *
     * @return true if the target server can be started in the debug mode, false
     *         otherwise. The default return value is false.
     *
     * @since  1.7
     */
    public boolean supportsStartDebugging(Target target) {
       return false;
    }
    
    /**
     * Can be the specified target server started in profile mode? If the 
     * target is also an admin server can be the admin server started in
     * profile mode?
     *
     * @param  target the target server in question, null implies the case where 
     *         target is also an admin server.
     *
     * @return true if the target server can be started in profile mode, false
     *         otherwise. The default return value is false.
     *
     * @since  1.9
     */
    public boolean supportsStartProfiling(Target target) {
        return false;
    }
    
    /**
     * Starts the admin server. Note that this means that the DeploymentManager
     * was originally created disconnected. After calling this, the DeploymentManager
     * will be connected, so any old cached DeploymentManager will be discarded.
     * All diagnostics should be communicated through ProgressObject without exceptions thrown.
     *
     * @return ProgressObject object used to monitor start server progress
     */
    public abstract ProgressObject startDeploymentManager();
    
    /**
     * Stops the admin server. The DeploymentManager object will be disconnected.
     * All diagnostic should be communicated through ServerProgres with no
     * exceptions thrown.
     * @return ServerProgress object used to monitor start server progress
     */
    public abstract ProgressObject stopDeploymentManager();
    
    /** Optional method. This implementation does nothing.
     *
     * Stops the admin server. The DeploymentManager object will be disconnected.
     * The call should terminate immediately and not wait for the server to stop.
     * <div class="nonnormative">
     * This will be used at IDE shutdown so that the server shutdown does not block the IDE.
     * </div>
     */
    public void stopDeploymentManagerSilently() {
        //do nothing
    }
    
    /** See {@link #stopDeploymentManagerSilently}
     * @return override and return true if stopDeploymentManagerSilently is implemented
     */
    public boolean canStopDeploymentManagerSilently () {
       return false; 
    }
    
    /**
     * Returns true if the admin server should be started before server deployment configuration.
     */
    public abstract boolean needsStartForConfigure();
    
    /**
     * Returns true if the admin server should be started before asking for
     * target list.
     */
    public abstract boolean needsStartForTargetList();
    
    /**
     * Returns true if the admin server should be started before admininistrative configuration.
     */
    public abstract boolean needsStartForAdminConfig();
    
    /**
     * Returns true if this admin server is running.
     */
    public abstract boolean isRunning();
    
    /**
     * Returns the running state of a specific target. This should be
     * overwritten by plugins which support multiple target servers via
     * one admin server.
     * @param target the target server in question; null value implies 
     * the query is against the admin server.
     * @return true if the server is question is running.
     * @since 1.6
     */
    public boolean isRunning(Target target) {
        if (target == null || isAlsoTargetServer(target)) {
            return isRunning();
        }
        
        return false;
    }
    
    /**
     * Returns true if the given target is in debug mode.
     */
    public abstract boolean isDebuggable(Target target);
    
    /**
     * Starts the target server asynchronously and reports the status
     * through the returned <code>ProgressObject</code>. This should be
     * overwritten by plugins which support the state management of
     * different target servers.
     * @param target a non-null target server to be started
     * @return a ProgressObject which is used to communicate the
     * progess/state of this action.  Should not be null when supportsStartTarget 
     * returns true on same target.
     * @since 1.6
     */
    public ProgressObject startTarget(Target target) {
        return null;
    }
    
    /**
     * Stops the target server asynchronously and reports the status
     * through the returned <code>ProgressObject</code>. This should be
     * overwritten by plugins which support the state management of
     * different target servers.
     * @param target a non-null target server to be stopped
     * @return a ProgressObject which is used to communicate the
     * progess/state of this action.  Should not be null when supportsStartTarget 
     * return true on the same target.
     * @since 1.6
     */
    public ProgressObject stopTarget(Target target) {
        return null;
    }

    /**
     * Start or restart the target in debug mode.
     * If target is also domain admin, the amdin is restarted in debug mode.
     * All diagnostic should be communicated through ServerProgres with no exceptions thrown.
     * @param target the target server
     * @return ServerProgress object to monitor progress on start operation
     */
    public abstract ProgressObject startDebugging(Target target);
    
    /**
     * Start the target in profile mode, null target implies the admin server.
     *
     * @param target          the target server in question, null target implies 
     *                        the admin server.
     *
     * @return ServerProgress object to monitor progress on start operation.
     *
     * @since 1.9
     */
    public ProgressObject startProfiling(Target target) {
        throw new UnsupportedOperationException("Starting in profile mode is not supported by this server."); // NIO18N
    }
    
    /**
     * Returns the host/port necessary for connecting to the server's debug information.
     */
    public abstract ServerDebugInfo getDebugInfo(Target target);
    
    /**
     * Returns true if target server needs a restart for last configuration changes to 
     * take effect.  Implementation should override when communication about this 
     * server state is needed.
     *
     * @param target target server; null implies the case where target is also admin server.
     */
    public boolean needsRestart(Target target) {
         return false;
    }
}
