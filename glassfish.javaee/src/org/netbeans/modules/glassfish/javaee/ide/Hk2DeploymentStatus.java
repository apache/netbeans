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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee.ide;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

/** 
 * This should have been included in JSR-88 as a helper class.
 *
 * @author Peter Williams
 */
public class Hk2DeploymentStatus implements DeploymentStatus {

    private final CommandType command;
    private final StateType state;
    private final ActionType action;
    private final String message;
    
    /**
     * Create DeploymentStatus object
     * 
     * @param state State of command being performed (running, completed, failed)
     * @param command Command being performed (start, stop, deploy, etc.)
     * @param action Action represented by this status event (command is executing,
     *   cancelled, or stopped.)
     * @param message Informational message for the user describing this status object.
     */
    public Hk2DeploymentStatus(final CommandType command, final StateType state, 
            final ActionType action, final String message) {
        this.command = command;
        this.state = state;
        this.action = action;
        this.message = message;
    }
    
    public CommandType getCommand() {
        return command;
    }

    public StateType getState() {
        return state;
    }

    public ActionType getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public boolean isCompleted() {
        return StateType.COMPLETED.equals(state);
    }

    public boolean isFailed() {
        return StateType.FAILED.equals(state);
    }

    public boolean isRunning() {
        return StateType.RUNNING.equals(state);
    }

    @Override
    public String toString() {
        return message;
    }

}
