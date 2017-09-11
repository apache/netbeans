/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.autoupdate.services;

import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.updater.UpdateTracking;

/** Issue http://www.netbeans.org/issues/show_bug.cgi?id=111701
 *
 * @author Jiri Rechtacek
 */
@RandomlyFails
public class TargetClusterTest extends TargetClusterTestCase {
    
    public TargetClusterTest (String testName) {
        super (testName);
    }

    @Override protected Level logLevel() {
        return Level.FINE;
    }

    @Override protected String logRoot() {
        return "org.netbeans.modules.autoupdate";
    }
    
    public void testInstallGloballyNewIntoDeclaredPlatform () throws IOException, OperationException {
        // Otherwise (new module), if a cluster name is specified in NBM, put it there
        assertEquals ("Goes into " + platformDir.getName (), platformDir.getName (), getTargetCluster (platformDir.getName (), true).getName ());
    }

    @RandomlyFails // org.yourorghere.platform.null - UpdateUnit found.
    public void testInstallNewIntoDeclaredPlatform () throws IOException, OperationException {
        // Otherwise (new module), if a cluster name is specified in NBM, put it there
        assertEquals ("Goes into " + platformDir.getName (), platformDir.getName (), getTargetCluster (platformDir.getName (), null).getName ());
    }
    
    @RandomlyFails
    public void testInstallNewIntoDeclaredNextCluster () throws IOException, OperationException {
        // Otherwise (new module), if a cluster name is specified in NBM, put it there
        assertEquals ("Goes into " + nextDir.getName (), nextDir.getName (), getTargetCluster (nextDir.getName (), null).getName ());
    }

    @RandomlyFails
    public void testInstallNewIntoDeclaredNextClusterAndFalseGlobal () throws IOException, OperationException {
        // target cluster has precedence than global
        assertEquals ("Goes into " + nextDir.getName (), nextDir.getName (), getTargetCluster (nextDir.getName (), null).getName ());
    }
    
    public void testInstallGloballyNew () throws IOException, OperationException {
        // Otherwise (no cluster name specified), if marked global, maybe put it into an "extra" cluster
        assertEquals ("Goes into " + UpdateTracking.EXTRA_CLUSTER_NAME,
                UpdateTracking.EXTRA_CLUSTER_NAME,
                getTargetCluster (null, true).getName ());
    }
    
    public void testInstallLocallyNew () throws IOException, OperationException {
        // Otherwise (global="false" or unspecified), put it in user dir
        assertEquals ("Goes into " + userDir.getName (),
                userDir.getName (),
                getTargetCluster (null, null).getName ());
    }
    
    public void testInstallNoDeclaredGlobalNew () throws IOException, OperationException {
        // Otherwise (global="false" or unspecified), put it in user dir
        assertEquals ("Goes into " + userDir.getName (),
                userDir.getName (),
                getTargetCluster (null, null).getName ());
    }
    
    public void testInstallDeclaredClusterForceLocal() throws IOException, OperationException {
        // Otherwise (global="false" or unspecified), put it in user dir
        assertEquals("Goes into " + userDir.getName(),
                userDir.getName(),
                getTargetCluster(UpdateTracking.EXTRA_CLUSTER_NAME, false).getName());
    }

    public void testInstallDeclaredClusterDefaultLocation() throws IOException, OperationException {
        // Otherwise (global="false" or unspecified), put it in user dir
        assertEquals("Goes into " + userDir.getName(),
                UpdateTracking.EXTRA_CLUSTER_NAME,
                getTargetCluster(UpdateTracking.EXTRA_CLUSTER_NAME, null).getName());
    }

}
