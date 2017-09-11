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

package org.netbeans.modules.java.platform;

import java.lang.ref.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.netbeans.spi.java.platform.GeneralPlatformInstall;

import org.netbeans.spi.java.platform.PlatformInstall;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Simple helper class, which keeps track of registered PlatformInstallers.
 * It caches its [singleton] instance for a while.
 *
 * @author Svata Dedic
 */
public class InstallerRegistry {
    
    private static final String INSTALLER_REGISTRY_FOLDER = "org-netbeans-api-java/platform/installers"; // NOI18N    
    private static Reference<InstallerRegistry> defaultInstance = new WeakReference<InstallerRegistry>(null);
    private static final Logger LOG = Logger.getLogger(InstallerRegistry.class.getName());
    
    private final Lookup lookup;
    private final List<GeneralPlatformInstall> platformInstalls;      //Used by unit test
    
    InstallerRegistry() {
        this.lookup = Lookups.forPath(INSTALLER_REGISTRY_FOLDER);
        this.platformInstalls = null;
    }
    
    /**
     * Used only by unit tests
     */
    InstallerRegistry (GeneralPlatformInstall[] platformInstalls) {
        assert platformInstalls != null;
        this.platformInstalls = Arrays.asList(platformInstalls);
        this.lookup = null;
    }
    
    /**
     * Returns all registered Java platform installers, in the order as
     * they are specified by the module layer(s).
     */
    public List<PlatformInstall> getInstallers () {
        return filter(getAllInstallers(),PlatformInstall.class);
    }
    
    public List<CustomPlatformInstall> getCustomInstallers () {
        return filter(getAllInstallers(),CustomPlatformInstall.class);
    }
    
    public List<GeneralPlatformInstall> getAllInstallers () {
        if (this.platformInstalls != null) {
            //In the unit test
            return platformInstalls;
        }
        else {
            this.lookup.lookupAll(CustomPlatformInstall.class);
            this.lookup.lookupAll(PlatformInstall.class);
            final List<GeneralPlatformInstall> installs =
                Collections.unmodifiableList(new ArrayList<GeneralPlatformInstall>(
                    this.lookup.lookupAll(GeneralPlatformInstall.class)));
            LOG.log(
                Level.FINE,
                "Installers: {0}",  //NOI18N
                installs);
            return installs;
        }
    }
    
    

    /**
     * Creates/acquires an instance of InstallerRegistry
     */
    public static InstallerRegistry getDefault() {
        InstallerRegistry regs = defaultInstance.get();
        if (regs != null)
            return regs;
        regs = new InstallerRegistry();
        defaultInstance = new WeakReference<InstallerRegistry>(regs);
        return regs;
    }
    
    
    /**
     * Used only by Unit tests.
     * Sets the {@link InstallerRegistry#defaultInstance} to the new InstallerRegistry instance which 
     * always returns the given GeneralPlatformInstalls
     * @return an instance of InstallerRegistry which has to be hold by strong reference during the test
     */
    static InstallerRegistry prepareForUnitTest (GeneralPlatformInstall[] platformInstalls) {
        InstallerRegistry regs = new InstallerRegistry (platformInstalls);
        defaultInstance = new WeakReference<InstallerRegistry>(regs);
        return regs;
    }
        
    
    private static <T> List<T> filter(List<?> list, Class<T> clazz) {
        List<T> result = new ArrayList<T>(list.size());
        for (Object item : list) {
            if (clazz.isInstance(item)) {
                result.add(clazz.cast(item));
            }
        }
        return result;
    }        
}
