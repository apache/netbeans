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

package org.netbeans.modules.j2ee.genericserver.ide;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Adamek
 */
public class GSJ2eePlatformFactory extends J2eePlatformFactory {
    
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        return new J2eePlatformImplImpl();
    }
    
    private class J2eePlatformImplImpl extends J2eePlatformImpl {
        
        public boolean isToolSupported(String toolName) {
            return false;
        }
        
        public File[] getToolClasspathEntries(String toolName) {
            return new File[0];
        }
        
        public Set getSupportedSpecVersions() {
            Set result = new HashSet();
            result.add(J2eeModule.J2EE_14);
            return result;
        }
        
        public java.util.Set getSupportedModuleTypes() {
            Set result = new HashSet();
//            result.add(J2eeModule.EAR);
//            result.add(J2eeModule.WAR);
            result.add(J2eeModule.EJB);
//            result.add(J2eeModule.CONN);
//            result.add(J2eeModule.CLIENT);
            return result;
        }
        
        public Set/*<String>*/ getSupportedJavaPlatformVersions() {
            Set versions = new HashSet();
            versions.add("1.4"); // NOI18N
            versions.add("1.5"); // NOI18N
            versions.add("1.6"); // NOI18N
            return versions;
        }
        
        public JavaPlatform getJavaPlatform() {
            return null;
        }
        
        public java.io.File[] getPlatformRoots() {
            return new File[0];
        }
        
        public LibraryImplementation[] getLibraries() {
            return new LibraryImplementation[0];
        }
        
        public java.awt.Image getIcon() {
            return ImageUtilities.loadImage("org/netbeans/modules/j2ee/genericserver/resources/GSInstanceIcon.gif"); // NOI18N
        }
        
        public String getDisplayName() {
            return "Generic Server Platform";
        }
        
    }
    
}
