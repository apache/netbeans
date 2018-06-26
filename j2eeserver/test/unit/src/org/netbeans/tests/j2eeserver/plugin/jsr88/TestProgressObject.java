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

package org.netbeans.tests.j2eeserver.plugin.jsr88;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

/**
 *
 * @author  gfink
 * @author nn136682
 */
public class TestProgressObject extends ServerProgress {

    private TargetModuleID[] tmIDs;

    public TestProgressObject(DeploymentManager dm, Target[] targets, File archive, Object plan, ModuleType type) {
        this(dm, createTargetModuleIDs(targets, archive, type));
    }

    public TestProgressObject(DeploymentManager dm, Target[] targets, File archive, Object plan) {
        this(dm, createTargetModuleIDs(targets, archive, null));
    }

    public TestProgressObject(DeploymentManager dm, Target[] targets, Object archive, Object plan) {
        this(dm, new TargetModuleID[0]);
    }

    public TestProgressObject(DeploymentManager dm, TargetModuleID[] modules) {
        super(dm);
        tmIDs = modules;
    }

    public static TargetModuleID[] createTargetModuleIDs(Target[] targets, File archive, ModuleType type) {
        TargetModuleID [] ret = new TargetModuleID[targets.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new TestTargetModuleID(targets[i], archive.getName(), type != null ? type : getType(archive.getName()));
            ((TestTarget)targets[i]).add(ret[i]);
        }
        return ret;
    }
    static ModuleType getType(String name) {
        if (name.endsWith(".ear")) {
            return ModuleType.EAR;
        } else if (name.endsWith(".jar") || name.equals("jar")) {
            return ModuleType.EJB;
        } else if (name.endsWith(".war") || name.equals("web")) {
            return ModuleType.WAR;
        } else if (name.endsWith(".rar")) {
            return ModuleType.RAR;
        } else {
            throw new IllegalArgumentException("Invalid archive name: " + name);
        }
    }

    public void setStatusDistributeRunning(String message) {
        notify(createRunningProgressEvent(CommandType.DISTRIBUTE, message));
    }

    public void setStatusDistributeFailed(String message) {
        notify(createFailedProgressEvent(CommandType.DISTRIBUTE, message));
    }

    public void setStatusDistributeCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.DISTRIBUTE, message));
    }

    public void setStatusRedeployRunning(String message) {
        notify(createRunningProgressEvent(CommandType.REDEPLOY, message));
    }

    public void setStatusRedeployFailed(String message) {
        notify(createFailedProgressEvent(CommandType.REDEPLOY, message));
    }

    public void setStatusRedeployCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.REDEPLOY, message));
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Test plugin does not support cancel!");
    }

    @Override
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Test plugin does not support stop!");
    }

    @Override
    public boolean isCancelSupported() {
        return false;  // PENDING parameterize?
    }

    @Override
    public boolean isStopSupported() {
        return false; // PENDING see above
    }

    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null; // PENDING client support
    }

    @Override
    public DeploymentStatus getDeploymentStatus() {
        return super.getDeploymentStatus();
    }

    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        List ret = new Vector();
        for (int i = 0; i < tmIDs.length; i++) {
            if (tmIDs[i].getChildTargetModuleID() != null)
                ret.addAll(Arrays.asList(tmIDs[i].getChildTargetModuleID()));
            ret.add(tmIDs[i]);
        }
        return (TargetModuleID[]) ret.toArray(new TargetModuleID[ret.size()]);
    }
}
