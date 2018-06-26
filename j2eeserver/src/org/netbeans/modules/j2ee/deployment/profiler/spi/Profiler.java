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

package org.netbeans.modules.j2ee.deployment.profiler.spi;

import java.util.Map;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;

/**
 * Profiler has to implement this interface and register it in the default Lookup.
 *
 * @author sherold
 */
public interface Profiler {

    /**
     * Inform the profiler that some server is starting in the profile mode. It
     * allows the Profiler to correctly detect STATE_STARTING.
     */
    void notifyStarting();
    
    /**
     * This method is used from the <code>nbstartprofiledserver</code>
     * task to connect the Profiler to a server ready for profiling.
     *
     * @param projectProperties properties of project the <code>nbstartprofiledserver</code>
     *                          ant task was started from.
     *
     * @return <code>true</code> if the Profiler successfully attached to the server.
     */
    boolean attachProfiler(Map projectProperties);
    
    /**
     * This method is used from the Runtime tab to obtain settings for starting 
     * the server. It displays dialog and let the user choose required mode 
     * (direct/dynamic attach) and other settings for the server startup.
     *
     * @param   serverInstanceID ID of the server instance that is going to be started
     *
     * @return  required settings or <code>null</code> if user cancelled starting 
     *          the server.
     * 
     * @deprecated 
     */
    @Deprecated
    ProfilerServerSettings getSettings(String serverInstanceID);

    /**
     * This method is used from the Runtime tab to obtain settings for starting
     * the server. It displays dialog and let the user choose required mode
     * (direct/dynamic attach) and other settings for the server startup.
     *
     * @param   serverInstanceID ID of the server instance that is going to be started
     * @param   verbose Whether to show the informational dialog
     *
     * @return  required settings or <code>null</code> if user cancelled starting
     *          the server.
     * 
     * @deprecated
     */
    @Deprecated
    ProfilerServerSettings getSettings(String serverInstanceID, boolean verbose);
    
    /**
     * Returns state of Profiler agent instance started from the IDE. It detects 
     * possible response from an unknown (not started from the IDE) Profiler
     * agent, in this case it returns STATE_INACTIVE.
     *
     * @return state of Profiler agent instance.
     */
    int getState();
    
    /**
     * Stops execution of the application (its JVM) currently being profiled.
     * Shutdown is performed by the Profiler agent when in STATE_BLOCKED, STATE_RUNNING
     * or STATE_PROFILING state.
     *
     * @return object used to monitor progress of shutdown.
     */
    ProgressObject shutdown();
}
