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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.tests.j2eeserver.plugin;

import java.awt.Image;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Hejl
 */
public class TestPlatform extends J2eePlatformImpl {

    private final File platformRoot;

    public TestPlatform(File platformRoot) {
        this.platformRoot = platformRoot;
    }

    @Override
    public String getDisplayName() {
        return "Test platform";
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/tests/j2eeserver/plugin/registry/plugin.gif");
    }

    @Override
    public JavaPlatform getJavaPlatform() {
        return JavaPlatformManager.getDefault().getDefaultPlatform();
    }

    @Override
    public LibraryImplementation[] getLibraries() {
        return new LibraryImplementation[]{};
    }

    @Override
    public File[] getPlatformRoots() {
        return new File[] {platformRoot};
    }

    @Override
    public Set getSupportedJavaPlatformVersions() {
        Set versions = new HashSet();
        versions.add("1.4"); // NOI18N
        versions.add("1.5"); // NOI18N
        return versions;
    }

    @Override
    public Set<Type> getSupportedTypes() {
        Set<Type> types = new HashSet<Type>();
        types.add(Type.WAR);
        types.add(Type.EJB);
        types.add(Type.EAR);
        types.add(Type.CAR);
        types.add(Type.RAR);
        return types;
    }

    @Override
    public Set<Profile> getSupportedProfiles() {
        Set<Profile> profiles = new HashSet<Profile>();
        profiles.add(Profile.J2EE_13);
        profiles.add(Profile.J2EE_14);
        profiles.add(Profile.JAVA_EE_5);
        profiles.add(Profile.JAVA_EE_6_FULL);
        profiles.add(Profile.JAVA_EE_6_WEB);
        return profiles;
    }

    @Override
    public File[] getToolClasspathEntries(String toolName) {
        return new File[] {};
    }

    @Override
    public boolean isToolSupported(String toolName) {
        return false;
    }

}
