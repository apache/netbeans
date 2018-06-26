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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.jboss4.nodes;

import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils.Version;
import org.openide.util.Lookup;

/**
 * This class is helper for holding some lazy initialized values that were
 * copy-pasted in several class in previous version of the code. Namely
 * {@link JBEarApplicationsChildren}, {@link JBEjbModulesChildren} and
 * {@link JBWebApplicationsChildren}.
 *
 * @author Petr Hejl
 */
public class JBAbilitiesSupport {

    private final Lookup lookup;

    private Boolean remoteManagementSupported = null;

    private Boolean isJB4x = null;
    
    private Boolean isJB6x = null;

    private Boolean isJB7x= null;
    
    /**
     * Constructs the JBAbilitiesSupport.
     *
     * @param lookup Lookup that will be asked for {@link JBDeploymentManager} if
     * necessary
     */
    public JBAbilitiesSupport(Lookup lookup) {
        assert lookup != null;
        this.lookup = lookup;
    }

    /**
     * Returns true if the JBoss has installed remote management package.
     *
     * @return true if the JBoss has installed remote management package,
     *             false otherwise
     * @see Util.isRemoteManagementSupported(Lookup)
     */
    public boolean isRemoteManagementSupported() {
        if (remoteManagementSupported == null) {
            remoteManagementSupported = Util.isRemoteManagementSupported(lookup);
        }
        return remoteManagementSupported;
    }

    /**
     * Returns true if the version of the JBoss is 4. Check is based on directory
     * layout.
     *
     * @return true if the version of the JBoss is 4, false otherwise
     * @see JBPluginUtils.isGoodJBServerLocation4x(JBDeploymentManager)
     */
    public boolean isJB4x() {
        if (isJB4x == null) {
            JBDeploymentManager dm = lookup.lookup(JBDeploymentManager.class);
            isJB4x = JBPluginUtils.isJB4(dm);
        }
        return isJB4x;
    }
    
    public boolean isJB6x() {
        if (isJB6x == null) {
            JBDeploymentManager dm = lookup.lookup(JBDeploymentManager.class);
            Version version = dm.getProperties().getServerVersion();
            isJB6x = version != null && JBPluginUtils.JBOSS_6_0_0.compareTo(version) <= 0;
        }
        return isJB6x;
    }  
    
    public boolean isJB7x() {
        if (isJB7x == null) {
            JBDeploymentManager dm = lookup.lookup(JBDeploymentManager.class);
            Version version = dm.getProperties().getServerVersion();
            isJB7x = version != null && JBPluginUtils.JBOSS_7_0_0.compareTo(version) <= 0;
        }
        return isJB7x;
    }    
}
