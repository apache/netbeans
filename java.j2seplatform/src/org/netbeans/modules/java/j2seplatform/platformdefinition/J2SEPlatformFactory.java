/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.IOException;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.j2seplatform.wizard.NewJ2SEPlatform;
import org.netbeans.spi.java.platform.JavaPlatformFactory;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
public class J2SEPlatformFactory implements JavaPlatformFactory {
    
    private static volatile J2SEPlatformFactory instance;
    
    private J2SEPlatformFactory() {}

    @Override
    @NonNull
    public JavaPlatform create(
            @NonNull final FileObject installFolder,
            @NonNull final String name,
            final boolean persistent) throws IOException {
        Parameters.notNull("installFolder", installFolder); //NOI18N
        Parameters.notNull("name", name);   //NOI18N
        assertValidPlatformName(name);
        return createImpl(installFolder, name, persistent);
    }
    
    @NonNull
    public JavaPlatform create(@NonNull final FileObject installFolder) throws IOException {
        Parameters.notNull("installFolder", installFolder); //NOI18N
        return createImpl(installFolder, null, true);
    }
    
    private JavaPlatform createImpl (
            @NonNull final FileObject installFolder,
            @NullAllowed String name,
            final boolean persistent) throws IOException {
        NewJ2SEPlatform plat = NewJ2SEPlatform.create(installFolder);
        plat.run();
        if (!plat.isValid()) {
            throw new IOException("Invalid J2SE platform in " + installFolder); // NOI18N
        }
        if (name == null) {
            name = createPlatformDisplayName(plat);
        }
        final String antName = createPlatformAntName(name);
        plat.setDisplayName(name);
        plat.setAntName(antName);
        return persistent ?
                PlatformConvertor.create(plat):
                plat;
    }
    
    private static void assertValidPlatformName(@NonNull final String platformName) {        
        for (JavaPlatform jp : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if (platformName.equals(jp.getDisplayName())) {
                throw new IllegalArgumentException(platformName);
            }
        }
    }
    
    @NonNull
    private static String createPlatformDisplayName(@NonNull final JavaPlatform plat) {
        final Map<String, String> m = plat.getSystemProperties();
        final String vmVersion = m.get("java.specification.version"); // NOI18N
        final StringBuilder displayName = new StringBuilder("JDK "); // NOI18N
        if (vmVersion != null) {
            displayName.append(vmVersion);
        }
        return displayName.toString();
    }
    
    @NonNull
    private static String createPlatformAntName(@NonNull final String displayName) {
        assert displayName != null && displayName.length() > 0;
        String antName = PropertyUtils.getUsablePropertyName(displayName);
        if (platformExists(antName)) {
            final String baseName = antName;
            int index = 1;
            antName = baseName + Integer.toString(index);
            while (platformExists(antName)) {
                index ++;
                antName = baseName + Integer.toString(index);
            }
        }
        return antName;
    }
    
    /**
     * Checks if the platform of given antName is already installed
     */
    private static boolean platformExists(@NonNull final String antName) {
        assert antName != null && antName.length() > 0;
        for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            final String otherName = p.getProperties().get("platform.ant.name");  // NOI18N
            if (antName.equals(otherName)) {
                return true;
            }
        }
        return false;
    }
    
    @NonNull
    public static J2SEPlatformFactory getInstance() {
        J2SEPlatformFactory res = instance;
        if (res == null) {
            res = instance = new J2SEPlatformFactory();
        }
        return res;
    }
    
    @ServiceProvider(service = JavaPlatformFactory.Provider.class)
    public static final class Provider implements JavaPlatformFactory.Provider {
        @CheckForNull
        @Override
        public JavaPlatformFactory forType(@NonNull final String platformType) {
            if (J2SEPlatformImpl.PLATFORM_J2SE.equals(platformType)) {
                return J2SEPlatformFactory.getInstance();
            }
            return null;
        }        
    }
}
